package com.deaifish.excel2db.mapper;


import com.deaifish.excel2db.bean.LanguagePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description zd_language 语言数据访问层
 *
 * @author cxx
 * @date 2025-09-30 12:05
 */
public interface LanguageMapper {

    void saveAll(List<LanguagePO> dataList);

    Integer getMaxId();

    /**
     * 去除重复数据
     */
    void removeDuplicates();

    void updateSqbm();

    void backupData(@Param("tableName") String tableName);
}