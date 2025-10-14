package com.deaifish.excel2db.service.impl;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.deaifish.excel2db.config.Excel2DBConfig;
import com.deaifish.excel2db.bean.LanguagePO;
import com.deaifish.excel2db.enums.TemplateType;
import com.deaifish.excel2db.factory.Excel2DBServiceFactory;
import com.deaifish.excel2db.mapper.LanguageMapper;
import com.deaifish.excel2db.service.Excel2DBService;
import com.deaifish.excel2db.util.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author cxx
 * @description zd_language 语言模板导入实现类
 * @date 2025-09-28 17:45
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageServiceImpl implements Excel2DBService<LanguagePO>, ApplicationListener<ApplicationReadyEvent> {

    private final Excel2DBConfig excel2DBConfig;
    private final LanguageMapper languageMapper;
    private final ApplicationContext applicationContext;
    private final Excel2DBServiceFactory serviceFactory;

    /**
     * 在应用完全启动后注册到工厂
     * 此时所有 Bean 已完成初始化，AOP 代理已创建
     * 通过 ApplicationContext 获取代理对象，而不是使用 this
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 从 ApplicationContext 获取代理对象
        LanguageServiceImpl proxyBean = applicationContext.getBean(LanguageServiceImpl.class);
        serviceFactory.registerService(TemplateType.LANGUAGE, proxyBean);
        log.info("LanguageServiceImpl 已注册到 Excel2DBServiceFactory (通过 ApplicationReadyEvent，注册的是代理对象)");
    }

    /**
     * 获取Excel模板
     *
     * @param response HTTP响应
     */
    @Override
    public void getTemplate(HttpServletResponse response) {
        File tempFile = null;
        try {
            // 创建临时文件
            tempFile = FileUtil.createTempFile(excel2DBConfig.getLanguageFileName(), ".xlsx", true);

            // 生成Excel模板
            EasyExcel.write(tempFile, LanguagePO.class)
                    .sheet("模板")
                    .doWrite(Collections::emptyList);

            // 读取临时文件并写入响应流
            try (InputStream is = new FileInputStream(tempFile);
                 OutputStream os = response.getOutputStream()) {

                // 设置响应头
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setCharacterEncoding("UTF-8");

                String filename = excel2DBConfig.getLanguageFileName() + ".xlsx";
                String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                        .replaceAll("\\+", "%20");
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);

                // 将文件流写入响应输出流
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();

                log.info("模板文件下载成功: {}", filename);
            }
        } catch (IOException e) {
            log.error("生成或下载模板文件失败", e);
            throw new RuntimeException("生成模板文件失败: " + e.getMessage(), e);
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                FileUtil.del(tempFile);
            }
        }
    }

    /**
     * 读取Excel文件（仅读取不保存）
     *
     * @param file MultipartFile上传文件
     * @return 是否读取成功
     */
    @Override
    public List<LanguagePO> readExcel(MultipartFile file) {
        // 校验文件
        ExcelUtil.validateMultipartFile(file);

        File tempFile = null;
        List<LanguagePO> poList = new ArrayList<>();
        try {
            // 将MultipartFile转换为临时文件
            tempFile = ExcelUtil.convertToFile(file);

            // 使用EasyExcel读取
            AtomicInteger count = new AtomicInteger(0);
            EasyExcel.read(tempFile, LanguagePO.class, new PageReadListener<LanguagePO>(dataList -> {
                for (LanguagePO data : dataList) {
                    // 数据校验
                    if (StrUtil.isBlank(data.getChinese()) || StrUtil.isBlank(data.getOther())) {
//                        log.warn("跳过无效数据: {}", JSONUtil.toJsonStr(data));
                        continue;
                    }
                    data.setSqbm("-2");
                    poList.add(data);
//                    log.info("读取到一条数据: {}", JSONUtil.toJsonStr(data));
                    count.incrementAndGet();
                }
            })).sheet().doRead();

            log.info("Excel文件读取完成，共读取 {} 条有效数据", count.get());
            return poList;

        } catch (Exception e) {
            log.error("读取Excel文件失败", e);
            throw new RuntimeException("读取Excel文件失败: " + e.getMessage(), e);
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                FileUtil.del(tempFile);
            }
        }
    }

    /**
     * 导入Excel数据到数据库
     *
     * @param file MultipartFile上传文件
     * @return 导入的数据条数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importExcel(MultipartFile file) {
        // 备份数据
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String backupTableName = "zd_language_" + currentDate + "_back_up";
        languageMapper.backupData(backupTableName);

        log.info("开始导入Excel文件");

        List<LanguagePO> dataList = readExcel(file);

        // 生成自增ID
        AtomicInteger id = new AtomicInteger(languageMapper.getMaxId());
        for (LanguagePO data : dataList) {
            data.setId(id.incrementAndGet());
        }
        saveDataBatch(dataList);

        // 去除重复数据
        languageMapper.removeDuplicates();
        // 更新社区编码
        languageMapper.updateSqbm();

        log.info("Excel数据导入完成，共导入 {} 条有效数据", dataList.size());
        return dataList.size();
    }

    /**
     * 批量保存数据
     *
     * @param dataList 数据列表
     */
    @Override
    public void saveDataBatch(List<LanguagePO> dataList) {
        try {
            // 批量保存到数据库
            languageMapper.saveAll(dataList);
            log.info("批量保存 {} 条数据成功", dataList.size());
        } catch (Exception e) {
            log.error("批量保存数据失败", e);
            throw new RuntimeException("批量保存数据失败: " + e.getMessage(), e);
        }
    }
}
