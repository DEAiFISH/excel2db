package com.deaifish.excel2db.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @description Excel2DB配置类
 *
 * @author cxx
 * @date 2025-09-30 12:31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "excel2db.config.template")
public class Excel2DBConfig {
    private String languageFileName = "language_template";
    private String qkzlmbaFileName = "qkzlmba_template";
}
