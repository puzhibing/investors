package com.puzhibing.investors.util.http;

import lombok.Data;

/**
 * http请求返回封装
 */
@Data
public class HttpResult {
    /**
     * 返回状态码
     */
    private Integer code;
    /**
     * 返回结果
     */
    private String data;

    /**
     * 返回封装结果
     * @param code
     * @param data
     * @return
     */
    public static HttpResult getHttpResult(Integer code, String data){
        HttpResult httpResult = new HttpResult();
        httpResult.setCode(code);
        httpResult.setData(data);
        return httpResult;
    }
}
