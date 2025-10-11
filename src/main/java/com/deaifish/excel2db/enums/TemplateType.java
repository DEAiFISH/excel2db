package com.deaifish.excel2db.enums;

/**
 * @description 模板类型枚举
 * @author cxx
 * @date 2025-10-11
 */
public enum TemplateType {
    /**
     * 语言模板
     */
    LANGUAGE("language", "语言模板"),
    
    /**
     * 全科诊疗模板
     */
    QKZLMA("qkzlmba", "全科诊疗模板");
    
    /**
     * 模板代码
     */
    private final String code;
    
    /**
     * 模板名称
     */
    private final String name;
    
    TemplateType(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * 根据代码获取模板类型
     * @param code 模板代码
     * @return 模板类型
     */
    public static TemplateType fromCode(String code) {
        for (TemplateType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的模板类型: " + code);
    }
}

