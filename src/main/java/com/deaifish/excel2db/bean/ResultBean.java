package com.deaifish.excel2db.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description 统一返回结果
 *
 * @author cxx
 * @date 2025-09-30 15:07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultBean<T> {
    /**
     * 响应码
     */
    private String code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应（带数据）
     * @param data 响应数据
     * @return ResultBean对象
     */
    public static <T> ResultBean<T> success(T data) {
        return new ResultBean<>("200", "操作成功", data);
    }

    /**
     * 成功响应（不带数据）
     * @return ResultBean对象
     */
    public static <T> ResultBean<T> success() {
        return new ResultBean<>("200", "操作成功", null);
    }

    /**
     * 成功响应（自定义消息）
     * @param msg 响应消息
     * @param data 响应数据
     * @return ResultBean对象
     */
    public static <T> ResultBean<T> success(String msg, T data) {
        return new ResultBean<>("200", msg, data);
    }

    /**
     * 失败响应（默认错误码）
     * @param msg 错误消息
     * @return ResultBean对象
     */
    public static <T> ResultBean<T> error(String msg) {
        return new ResultBean<>("500", msg, null);
    }

    /**
     * 失败响应（自定义错误码）
     * @param code 错误码
     * @param msg 错误消息
     * @return ResultBean对象
     */
    public static <T> ResultBean<T> error(String code, String msg) {
        return new ResultBean<>(code, msg, null);
    }

    /**
     * 失败响应（带数据）
     * @param code 错误码
     * @param msg 错误消息
     * @param data 响应数据
     * @return ResultBean对象
     */
    public static <T> ResultBean<T> error(String code, String msg, T data) {
        return new ResultBean<>(code, msg, data);
    }
}
