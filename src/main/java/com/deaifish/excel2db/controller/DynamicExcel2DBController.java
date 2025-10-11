package com.deaifish.excel2db.controller;

import com.deaifish.excel2db.bean.BasePO;
import com.deaifish.excel2db.bean.ResultBean;
import com.deaifish.excel2db.factory.Excel2DBServiceFactory;
import com.deaifish.excel2db.service.Excel2DBService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @description 动态Excel2DB控制器
 * 通过模板类型参数动态调用不同模板的Service实现
 *
 * 使用示例：
 * - 获取语言模板: GET /api/excel2db/language/getTemplate
 * - 获取全科诊疗模板: GET /api/excel2db/qkzlma/getTemplate
 * - 导入语言数据: POST /api/excel2db/language/importExcel
 * - 导入情况资料码表数据: POST /api/excel2db/qkzlma/importExcel
 *
 * @author cxx
 * @date 2025-10-11
 */
@Slf4j
@RestController
@RequestMapping("/api/excel2db")
@RequiredArgsConstructor
public class DynamicExcel2DBController {

    private final Excel2DBServiceFactory serviceFactory;
    
    /**
     * 获取Excel模板
     * @param templateType 模板类型（language、qkzlmba等）
     * @param response HTTP响应
     */
    @GetMapping("/{templateType}/getTemplate")
    public void getTemplate(
            @PathVariable("templateType") String templateType,
            HttpServletResponse response) {
        try {
            log.info("获取模板，模板类型: {}", templateType);
            
            // 根据模板类型获取对应的Service
            Excel2DBService<? extends BasePO> service = serviceFactory.getService(templateType);
            
            // 调用Service方法
            service.getTemplate(response);
            
            log.info("模板获取成功，模板类型: {}", templateType);
        } catch (IllegalArgumentException e) {
            log.error("模板类型不存在: {}", templateType, e);
            throw new RuntimeException("模板类型不存在: " + templateType);
        } catch (Exception e) {
            log.error("获取模板失败，模板类型: {}", templateType, e);
            throw new RuntimeException("获取模板失败: " + e.getMessage());
        }
    }
    
    /**
     * 读取并解析Excel文件（仅读取不保存）
     * @param templateType 模板类型（language、qkzlma等）
     * @param file 上传的Excel文件
     * @return 处理结果
     */
    @PostMapping("/{templateType}/readExcel")
    public ResultBean<List<? extends BasePO>> readExcel(
            @PathVariable("templateType") String templateType,
            @RequestParam("file") MultipartFile file) {
        try {
            // 参数校验
            if (file == null || file.isEmpty()) {
                return ResultBean.error("文件不能为空");
            }
            
            // 校验文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null ||
                (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
                return ResultBean.error("文件格式不正确，仅支持.xlsx和.xls格式");
            }
            
            // 校验文件大小（限制10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResultBean.error("文件大小不能超过10MB");
            }
            
            log.info("开始处理Excel文件，模板类型: {}, 文件名: {}, 大小: {} bytes", 
                    templateType, originalFilename, file.getSize());
            
            // 根据模板类型获取对应的Service
            Excel2DBService<? extends BasePO> service = serviceFactory.getService(templateType);
            
            // 读取并处理Excel
            return ResultBean.success(service.readExcel(file));
            
        } catch (IllegalArgumentException e) {
            log.error("模板类型不存在: {}", templateType, e);
            return ResultBean.error("模板类型不存在: " + templateType);
        } catch (Exception e) {
            log.error("读取Excel文件异常，模板类型: {}", templateType, e);
            return ResultBean.error("读取Excel文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 导入Excel数据到数据库
     * @param templateType 模板类型（language、qkzlma等）
     * @param file 上传的Excel文件
     * @return 导入结果
     */
    @PostMapping("/{templateType}/importExcel")
    public ResultBean<String> importExcel(
            @PathVariable("templateType") String templateType,
            @RequestParam("file") MultipartFile file) {
        try {
            // 参数校验
            if (file == null || file.isEmpty()) {
                return ResultBean.error("文件不能为空");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null ||
                (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
                return ResultBean.error("文件格式不正确，仅支持.xlsx和.xls格式");
            }

            log.info("开始导入Excel数据，模板类型: {}, 文件名: {}", templateType, originalFilename);

            // 根据模板类型获取对应的Service
            Excel2DBService<? extends BasePO> service = serviceFactory.getService(templateType);

            // 导入数据
            int count = service.importExcel(file);

            String message = String.format("成功导入 %d 条数据", count);
            log.info("Excel数据导入完成，模板类型: {}, 文件名: {}, {}",
                    templateType, originalFilename, message);

            return ResultBean.success(message);

        } catch (IllegalArgumentException e) {
            log.error("模板类型不存在: {}", templateType, e);
            return ResultBean.error("模板类型不存在: " + templateType);
        } catch (Exception e) {
            log.error("导入Excel数据异常，模板类型: {}", templateType, e);
            return ResultBean.error("导入Excel数据失败: " + e.getMessage());
        }
    }
}

