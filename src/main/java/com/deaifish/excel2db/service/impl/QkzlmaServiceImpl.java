package com.deaifish.excel2db.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.deaifish.excel2db.bean.QkzlmbaPO;
import com.deaifish.excel2db.config.Excel2DBConfig;
import com.deaifish.excel2db.enums.TemplateType;
import com.deaifish.excel2db.factory.Excel2DBServiceFactory;
import com.deaifish.excel2db.mapper.QkzlmbaMapper;
import com.deaifish.excel2db.service.Excel2DBService;
import com.deaifish.excel2db.util.ExcelUtil;
import com.deaifish.excel2db.util.PinYinUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
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
 * @description zd_qkzlmba 全科诊疗模板导入实现类
 * @date 2025-10-11 12:01
 */
@Slf4j
@Service
public class QkzlmaServiceImpl implements Excel2DBService<QkzlmbaPO>, ApplicationListener<ApplicationReadyEvent> {
    private final QkzlmbaMapper qkzlmbaMapper;
    private final Excel2DBConfig excel2DBConfig;
    private final ApplicationContext applicationContext;

    @Lazy
    @Autowired
    private Excel2DBServiceFactory serviceFactory;

    public QkzlmaServiceImpl(QkzlmbaMapper qkzlmbaMapper, Excel2DBConfig excel2DBConfig, ApplicationContext applicationContext) {
        this.qkzlmbaMapper = qkzlmbaMapper;
        this.excel2DBConfig = excel2DBConfig;
        this.applicationContext = applicationContext;
    }

    /**
     * 在应用完全启动后注册到工厂
     * 此时所有 Bean 已完成初始化，AOP 代理已创建
     * 通过 ApplicationContext 获取代理对象，而不是使用 this
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 从 ApplicationContext 获取代理对象
        QkzlmaServiceImpl proxyBean = applicationContext.getBean(QkzlmaServiceImpl.class);
        serviceFactory.registerService(TemplateType.QKZLMA, proxyBean);
        log.info("QkzlmaServiceImpl 已注册到 Excel2DBServiceFactory (通过 ApplicationReadyEvent，注册的是代理对象)");
    }


    @Override
    public void getTemplate(HttpServletResponse response) {
        File tempFile = null;
        try {
            // 创建临时文件
            tempFile = FileUtil.createTempFile(excel2DBConfig.getQkzlmbaFileName(), ".xlsx", true);

            // 生成Excel模板
            EasyExcel.write(tempFile, QkzlmbaPO.class)
                    .sheet("模板")
                    .doWrite(Collections::emptyList);

            // 读取临时文件并写入响应流
            try (InputStream is = new FileInputStream(tempFile);
                 OutputStream os = response.getOutputStream()) {

                // 设置响应头
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setCharacterEncoding("UTF-8");

                String filename = excel2DBConfig.getQkzlmbaFileName() + ".xlsx";
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

    @Override
    public List<QkzlmbaPO> readExcel(MultipartFile file) {
        // 校验文件
        ExcelUtil.validateMultipartFile(file);

        File tempFile = null;
        List<QkzlmbaPO> poList = new ArrayList<>();
        AtomicInteger id = new AtomicInteger(qkzlmbaMapper.getMaxId());
        try {
            // 将MultipartFile转换为临时文件
            tempFile = ExcelUtil.convertToFile(file);

            // 使用EasyExcel读取
            AtomicInteger count = new AtomicInteger(0);
            EasyExcel.read(tempFile, QkzlmbaPO.class, new PageReadListener<QkzlmbaPO>(dataList -> {
                for (QkzlmbaPO data : dataList) {
                    // 数据校验
                    if (StrUtil.isBlank(data.getMc()) || StrUtil.isBlank(data.getZdlx()) || StrUtil.isBlank(data.getBm())) {
//                        log.info("跳过无效数据: {}", JSONUtil.toJsonStr(data));
                        continue;
                    }
                    // 设置代号
                    String mc = data.getMc();
                    String pinyin = PinYinUtil.getPinyin(mc);
                    data.setDh(pinyin);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importExcel(MultipartFile file) {
        // 备份数据
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String backupTableName = "zd_qkzlmba_" + currentDate + "_back_up";
        qkzlmbaMapper.backupData(backupTableName);

        log.info("开始导入Excel文件");
        List<QkzlmbaPO> dataList = readExcel(file);

        if (dataList.isEmpty()) {
            log.warn("没有有效的数据");
            return 0;
        }

        // 生成自增id
        AtomicInteger id = new AtomicInteger(qkzlmbaMapper.getMaxId());
        dataList.forEach(data -> {
            data.setSysid(id.incrementAndGet());
        });
        // 保存数据
        saveDataBatch(dataList);
        // 去除重复数据
        qkzlmbaMapper.removeDuplicates();
        // 更新社区编码
        qkzlmbaMapper.updateQybm();

        log.info("Excel数据导入完成，共导入 {} 条有效数据", dataList.size());
        return dataList.size();
    }

    @Override
    public void saveDataBatch(List<QkzlmbaPO> dataList) {
        try {
            // 批量保存到数据库
            qkzlmbaMapper.saveAll(dataList);
            log.info("批量保存 {} 条数据成功", dataList.size());
        } catch (Exception e) {
            log.error("批量保存数据失败", e);
            throw new RuntimeException("批量保存数据失败: " + e.getMessage(), e);
        }
    }
}
