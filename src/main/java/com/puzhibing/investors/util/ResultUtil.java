package com.puzhibing.investors.util;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * 定义统一返回对象
 */
@Data
public class ResultUtil<T> {

    public static final Integer SUCCESS = 200;

    public static final Integer PARAM_ERROR = 300;

    public static final Integer RUNTIME_ERROR = 400;

    public static final Integer ERROR = 500;

    public static final Integer TOKEN_ERROR = 600;

    public static final String Token = "请登录";

    private Integer code;//状态码

    private String msg;//返回说明

    private T data;//返回数据



    private ResultUtil(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private ResultUtil(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }



    public static <T> ResultUtil<T> getResult(Integer code, String msg){
        return new ResultUtil<>(code, msg);
    }

    public static <T> ResultUtil<T> getResult(Integer code, String msg, T data){
        return new ResultUtil<>(code, msg, data);
    }

    /**
     * 错误信息
     * @return
     */
    public static ResultUtil error(String mag){
        return ResultUtil.getResult(ResultUtil.ERROR, mag, new JSONObject());
    }

    /**
     * 错误信息
     * @return
     */
    public static <T> ResultUtil <T> error(String mag, T obj){
        return ResultUtil.getResult(ResultUtil.ERROR, mag, obj);
    }

    /**
     * token失效
     * @return
     */
    public static ResultUtil tokenErr(){
        return ResultUtil.getResult(ResultUtil.TOKEN_ERROR, ResultUtil.Token, new JSONObject());
    }

    /**
     * token失效
     * @return
     */
    public static ResultUtil tokenErr(String msg){
        return ResultUtil.getResult(ResultUtil.TOKEN_ERROR, msg, new JSONObject());
    }

    /**
     * 参数异常
     * @return
     */
    public static  ResultUtil paranErr(){
        return ResultUtil.getResult(ResultUtil.PARAM_ERROR, "参数异常", new JSONObject());
    }

    /**
     * 参数异常
     * @return
     */
    public static <T> ResultUtil<T> paranErr(T data){
        return ResultUtil.getResult(ResultUtil.PARAM_ERROR, "参数异常", data);
    }

    /**
     * 运行异常
     * @return
     */
    public static ResultUtil runErr(){
        return ResultUtil.getResult(ResultUtil.RUNTIME_ERROR, "服务器运行异常", new JSONObject());
    }


    /**
     * 运行异常
     * @return
     */
    public static <T>ResultUtil<T> runErr(T data){
        return ResultUtil.getResult(ResultUtil.RUNTIME_ERROR, "服务器运行异常", data);
    }


    /**
     * 返回成功
     * @param
     * @return
     */
    public static ResultUtil success(){
        return ResultUtil.getResult(ResultUtil.SUCCESS, "成功", new JSONObject());
    }


    /**
     * 返回成功
     * @param data
     * @param <T>
     * @return
     */
    public static <T> ResultUtil<T> success(T data){
        return ResultUtil.getResult(ResultUtil.SUCCESS, "成功", data);
    }


    public static <T> ResultUtil<T> success(String msg, T data){
        return ResultUtil.getResult(ResultUtil.SUCCESS, msg, data);
    }

}
