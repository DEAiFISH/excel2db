package com.deaifish.excel2db.mapper;


import com.deaifish.excel2db.bean.QkzlmbaPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description zd_qkzlmba
 *
 * @author cxx
 * @date 2025-09-30 12:05
 */
public interface QkzlmbaMapper {

    void saveAll(List<QkzlmbaPO> dataList);

    Integer getMaxId();

    /**
     * 去除重复数据
     */
    void removeDuplicates();

    void updateQybm();

    void backupData(@Param("tableName") String tableName);
}