package com.deaifish.excel2db.bean;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cxx
 * @description zd_qkzlmba 导入
 * @date 2025-10-11 11:51
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ColumnWidth(20)
public class QkzlmbaPO extends BasePO{
    /**
     * 系统ID
     */
    @ExcelIgnore
    private Integer sysid;
    /**
     * 代号
     */
    @ExcelIgnore
    private String dh;
    /**
     * 名称
     */
    @ExcelProperty("名称")
    private String mc;
    /**
     * 状态
     */
    @ExcelIgnore
    private Integer state = 1;
    /**
     * 字典类型
     */
    @ExcelProperty("字典类型")
    private String zdlx;
    /**
     * 编码
     */
    @ExcelProperty("编码")
    private String bm;
    /**
     *
     */
    @ExcelIgnore
    private Integer fCqjkwt = 0;
    /**
     * 是否传染病
     */
    @ExcelProperty("是否传染病")
    private String fcrb = "1";
    /**
     * 备注
     */
    @ExcelProperty("备注")
    private String remark;

}
