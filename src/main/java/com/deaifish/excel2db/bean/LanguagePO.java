package com.deaifish.excel2db.bean;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description 表 zd_language 语言
 *
 * @author cxx
 * @date 2025-09-28 17:31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ColumnWidth(20)
public class LanguagePO extends BasePO{
    @ExcelIgnore
    private Integer id;

    @ExcelProperty("中文")
    private String chinese;

    @ExcelProperty("其他语言")
    private String other;

    @ExcelProperty("社区编码")
    @ExcelIgnore
    private String sqbm = "-2";
}
