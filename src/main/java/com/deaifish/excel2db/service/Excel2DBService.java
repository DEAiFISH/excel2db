package com.deaifish.excel2db.service;

import com.deaifish.excel2db.bean.BasePO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author cxx
 * @description Excel2DB服务接口（支持泛型）
 * @date 2025-10-11 14:42
 * @param <T> 数据实体类型，必须继承BasePO
 */
public interface Excel2DBService<T extends BasePO> {

    /**
     * 获取Excel模板
     * @param response HTTP响应
     */
    void getTemplate(HttpServletResponse response);

    /**
     * 读取Excel文件（仅读取不保存）
     * @param file MultipartFile上传文件
     * @return 是否读取成功
     */
    List<T> readExcel(MultipartFile file);

    /**
     * 导入Excel数据到数据库
     * 注意：实现类必须添加 @Transactional(rollbackFor = Exception.class) 注解
     * @param file MultipartFile上传文件
     * @return 导入的数据条数
     */
    int importExcel(MultipartFile file);

    /**
     * 批量保存数据
     * @param dataList 数据列表
     */
    void saveDataBatch(List<T> dataList);
}
