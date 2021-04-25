package com.puzhibing.investors.util;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private CloseableHttpResponse httpResponse;

    private RequestConfig requestConfig;


    /**
     * 创建一个httpClient对象
     */
    private void getHttpCline(){
        //1.创建连接池管理器
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(60000,
                TimeUnit.MILLISECONDS);
        connectionManager.setMaxTotal(1000);
        connectionManager.setDefaultMaxPerRoute(50);

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
    private void setPostHttpRequset(String url, Map<String, Object> params, Map<String, String> header, String contentType){
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(this.getRequestConfig());
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
            this.close();
        }
    }


    /**
     * 获取get请求实例
     * @param url       请求地址
     * @param params    请求参数
     */
    private void setGetHttpRequset(String url, Map<String, Object> params, Map<String, String> header){
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
            this.close();
        }
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
    public String pushHttpRequset(String mothed, String url, Map<String, Object> params, Map<String, String> header, String contentType){
        String content = null;
        switch (mothed){
            case "GET":
                this.setGetHttpRequset(url, params, header);
                break;
            case "POST":
                this.setPostHttpRequset(url, params, header, contentType);
                break;
        }
        if(httpResponse.getStatusLine().getStatusCode() == 200){
            try {
                content = EntityUtils.toString(httpResponse.getEntity());
                this.close();
                return content;
            } catch (IOException e) {
                e.printStackTrace();
                this.close();
            }
        }
        if(httpResponse.getStatusLine().getStatusCode() == 201){
            content = "{\"status\":201}";
            this.close();
            return content;
        }else{
            try {
                System.err.println("返回状态码：" + httpResponse.getStatusLine() + "。");
                content = EntityUtils.toString(httpResponse.getEntity());
                this.close();
                return content;
            } catch (IOException e) {
                e.printStackTrace();
                this.close();
            }
        }
        this.close();
        return content;
    }


    /**
     * 发送XML请求
     * @param url       请求地址
     * @param xml       XML数据
     * @param header    自定义请求头
     * @return
     */
    public String pushHttpRequsetXml(String url, String xml, Map<String, String> header){
        HttpPost httpPost = new HttpPost(url);
        for(String key : header.keySet()){
            httpPost.setHeader(key, header.get(key));
        }
        httpPost.setHeader("Content-Type", "application/xml");
        try {
            httpPost.setEntity(new StringEntity(xml, "UTF-8"));
            this.getHttpCline();
            if(null == this.httpClient){
                this.getHttpCline();
            }
            httpResponse = this.httpClient.execute(httpPost);
            String content = null;
            if(httpResponse.getStatusLine().getStatusCode() == 200){
                try {
                    content = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                    this.close();
                    return content;
                } catch (IOException e) {
                    e.printStackTrace();
                    this.close();
                }
            }else{
                try {
                    content = "返回状态码：" + httpResponse.getStatusLine() + "。" + EntityUtils.toString(httpResponse.getEntity());
                    this.close();
                    return content;
                } catch (IOException e) {
                    e.printStackTrace();
                    this.close();
                }
            }
            this.close();
            return content;
        } catch (IOException e) {
            e.printStackTrace();
            this.close();
        }
        return null;
    }



    /**
     * 关闭资源
     */
    private void close(){
        try {
            if(null != httpClient){
                httpClient.close();
            }
            if(null != httpResponse){
                httpResponse.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(null != httpClient){
                    httpClient.close();
                }
                if(null != httpResponse){
                    httpResponse.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
