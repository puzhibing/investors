package com.puzhibing.investors.util.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * http工具类
 */
@Component
public class HttpClientUtil {

    private CloseableHttpClient httpClient;

    private RequestConfig requestConfig;


    /**
     * 创建一个httpClient对象
     */
    private void getHttpCline(){
        //1.创建连接池管理器
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(60000,
                TimeUnit.MILLISECONDS);
        connectionManager.setMaxTotal(1000);
        connectionManager.setDefaultMaxPerRoute(100);

        //2.创建httpclient对象
        this.httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .disableAutomaticRetries()
                .build();
    }

    private RequestConfig getRequestConfig(){
        return RequestConfig.custom()
                .setConnectTimeout(60000)
                .setSocketTimeout(60000)
                .build();
    }



    /**
     * 创建一个POST请求实例
     * @param url       请求地址
     * @param params    请求参数
     */
    private CloseableHttpResponse setPostHttpRequset(String url, Map<String, Object> params, Map<String, String> header, String contentType){
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(this.getRequestConfig());
        CloseableHttpResponse httpResponse = null;
        if(null != header){
            for(String key : header.keySet()){
                httpPost.setHeader(key, header.get(key));
            }
        }
        List<NameValuePair> list = new ArrayList<>();
        if(null != params){
            Set<String> keys = params.keySet();
            for(String key : keys){
                list.add(new BasicNameValuePair(key, params.get(key).toString()));
            }
        }
        try {
            switch (contentType){
                case "form":
                    httpPost.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
                    break;
                case "json":
                    ObjectMapper objectMapper = new ObjectMapper();
                    String s =objectMapper.writeValueAsString(params);
                    System.err.println(s);
                    httpPost.setEntity(new StringEntity(s, Charset.forName("UTF-8")));
                    break;
            }
            this.getHttpCline();
            if(null == this.httpClient){
                this.getHttpCline();
            }
            httpResponse = this.httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return httpResponse;
    }


    /**
     * 获取get请求实例
     * @param url       请求地址
     * @param params    请求参数
     */
    private CloseableHttpResponse setGetHttpRequset(String url, Map<String, Object> params, Map<String, String> header){
        StringBuffer sb = new StringBuffer();
        String p = "";
        if(null != params){
            Set<String> keys = params.keySet();
            for(String key : keys){
                sb.append(key + "=" + params.get(key) + "&");
            }
            p = "?" + sb.substring(0, sb.length() - 1);
        }
        HttpGet httpGet = new HttpGet(url + p);
        CloseableHttpResponse httpResponse = null;
        if(null != header){
            for(String key : header.keySet()){
                httpGet.setHeader(key, header.get(key));
            }
        }
        this.getHttpCline();
        if(null == this.httpClient){
            this.getHttpCline();
        }
        try {
            httpResponse = this.httpClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return httpResponse;
    }


    /**
     * 发送http请求
     * @param mothed        "GET、POST、PUT、HEAD、DELETE、HEAD、OPTIONS"
     * @param url           请求地址
     * @param params        请求参数
     * @param header        请求头
     * @param contentType   参数请求方式form/json
     * @return
     */
    public HttpResult pushHttpRequset(String mothed, String url, Map<String, Object> params, Map<String, String> header, String contentType){
        CloseableHttpResponse httpResponse = null;
        switch (mothed){
            case "GET":
                httpResponse = this.setGetHttpRequset(url, params, header);
                break;
            case "POST":
                httpResponse = this.setPostHttpRequset(url, params, header, contentType);
                break;
        }
        HttpResult httpResult = null;
        try {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String content = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            System.err.println(statusCode);
            httpResult = HttpResult.getHttpResult(statusCode, content);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(null != httpResponse){
                    httpResponse.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return httpResult;
    }


    /**
     * 发送XML请求
     * @param url       请求地址
     * @param xml       XML数据
     * @param header    自定义请求头
     * @return
     */
    public HttpResult pushHttpRequsetXml(String url, String xml, Map<String, String> header){
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse httpResponse = null;
        for(String key : header.keySet()){
            httpPost.setHeader(key, header.get(key));
        }
        httpPost.setHeader("Content-Type", "application/xml");
        HttpResult httpResult = null;
        try {
            httpPost.setEntity(new StringEntity(xml, "UTF-8"));
            this.getHttpCline();
            if(null == this.httpClient){
                this.getHttpCline();
            }
            httpResponse = this.httpClient.execute(httpPost);
            try {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                String content = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                httpResult = HttpResult.getHttpResult(statusCode, content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(null != httpPost){
                    httpPost.releaseConnection();
                }
                if(null != httpResponse){
                    httpResponse.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return httpResult;
    }
}
