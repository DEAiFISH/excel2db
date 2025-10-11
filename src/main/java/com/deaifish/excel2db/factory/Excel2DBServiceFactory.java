package com.deaifish.excel2db.factory;

import com.deaifish.excel2db.bean.BasePO;
import com.deaifish.excel2db.enums.TemplateType;
import com.deaifish.excel2db.service.Excel2DBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description Excel2DB服务工厂
 * 根据模板类型动态获取对应的Service实现
 * @author cxx
 * @date 2025-10-11
 */
@Slf4j
@Component
public class Excel2DBServiceFactory {

    /**
     * 存储所有Service实现的Map
     * key: 模板类型代码
     * value: 对应的Service实现（使用通配符泛型）
     */
    private final Map<String, Excel2DBService<? extends BasePO>> serviceMap = new ConcurrentHashMap<>();
    
    /**
     * 注册Service实现
     * @param templateType 模板类型
     * @param service Service实现
     * @param <T> 数据实体类型
     */
    public <T extends BasePO> void registerService(TemplateType templateType, Excel2DBService<T> service) {
        serviceMap.put(templateType.getCode(), service);
        log.info("注册Service: {} -> {}", templateType.getCode(), service.getClass().getSimpleName());
    }

    /**
     * 根据模板类型获取对应的Service实现
     * @param templateType 模板类型
     * @return Service实现
     */
    public Excel2DBService<? extends BasePO> getService(TemplateType templateType) {
        Excel2DBService<? extends BasePO> service = serviceMap.get(templateType.getCode());
        if (service == null) {
            throw new IllegalArgumentException("未找到模板类型 " + templateType.getCode() + " 对应的Service实现");
        }
        return service;
    }

    /**
     * 根据模板类型代码获取对应的Service实现
     * @param templateCode 模板类型代码
     * @return Service实现
     */
    public Excel2DBService<? extends BasePO> getService(String templateCode) {
        TemplateType templateType = TemplateType.fromCode(templateCode);
        return getService(templateType);
    }
}

