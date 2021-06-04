package com.puzhibing.investors.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.puzhibing.investors.dao.SecuritiesMapper;
import com.puzhibing.investors.pojo.Securities;
import com.puzhibing.investors.pojo.SecuritiesCategory;
import com.puzhibing.investors.service.ISecuritiesCategoryService;
import com.puzhibing.investors.service.ISecuritiesService;
import com.puzhibing.investors.util.http.HttpClientUtil;
import com.puzhibing.investors.util.http.HttpResult;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


@Service
public class SecuritiesServiceImpl implements ISecuritiesService {

    @Resource
    private SecuritiesMapper securitiesMapper;

    @Autowired
    private HttpClientUtil httpClientUtil;

    @Autowired
    private ISecuritiesCategoryService securitiesCategoryService;

    private Integer pageSize = 5000;



    /**
     * 获取证券数据并添加到数据库
     * @throws Exception
     */
    @Override
    public void pullSecurities() throws Exception {
        SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(sdf_.format(new Date()) + "------更新证券基础数据任务开始。");
        /**
         * 获取并添加【上海证券交易所A股】证券数据
         */
        String urlSHA = "http://query.sse.com.cn/security/stock/getStockListData.do?stockType=1&pageHelp.cacheSize=1&pageHelp.beginPage=1&pageHelp.pageSize=" + pageSize + "&pageHelp.pageNo=1";
        Map<String, String> header = new HashMap<>();
        header.put("Referer", "http://www.sse.com.cn/");
        HttpResult httpResult = httpClientUtil.pushHttpRequset("GET", urlSHA, null, header, null);
        if(null == httpResult){
            System.err.println("数据请求异常");
        }
        if(httpResult.getCode() != 200){
            System.err.println(httpResult.getData());
        }
        JSONArray result = JSON.parseObject(httpResult.getData()).getJSONArray("result");
        SecuritiesCategory sh_a = securitiesCategoryService.queryByCode("sh_a");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(int i = 0; i < result.size(); i++){
            JSONObject jsonObject = result.getJSONObject(i);
            String name = jsonObject.getString("SECURITY_ABBR_A");
            String code = jsonObject.getString("SECURITY_CODE_A");
            String listing_date = jsonObject.getString("LISTING_DATE");
            String companyCode = jsonObject.getString("COMPANY_CODE");//公司代码

            Securities securities = securitiesMapper.queryByCodeAndSecuritiesCategory(code, sh_a.getId());
            if(null == securities){
                securities = new Securities();
                securities.setSystemCode("sh_a_" + code);
                securities.setCode(code);
                securities.setName(name);
                securities.setSecuritiesCategoryId(sh_a.getId());
                securities.setMarketTime("".equals(listing_date) ? null : sdf.parse(listing_date));
                securities.setMarketAddress("上海证券交易所");
                securitiesMapper.insert(securities);
            }
            //更新股本数据
            String urlSHA_ = "http://query.sse.com.cn/commonQuery.do?isPagination=false&sqlId=COMMON_SSE_CP_GPLB_GPGK_GBJG_C&companyCode=" + companyCode;
            header = new HashMap<>();
            header.put("Referer", "http://www.sse.com.cn/");
            httpResult = httpClientUtil.pushHttpRequset("GET", urlSHA_, null, header, null);
            if(null == httpResult){
                System.err.println("数据请求异常");
            }
            if(httpResult.getCode() != 200){
                System.err.println(httpResult.getData());
            }
            JSONArray result1 = JSON.parseObject(httpResult.getData()).getJSONArray("result");
            JSONObject jsonObject1 = result1.getJSONObject(0);
            Long flowEquity = Double.valueOf(jsonObject1.getDouble("UNLIMITED_A_SHARES") * 10000).longValue();
            securities.setFlowEquity(flowEquity);
            securitiesMapper.updateById(securities);
            Thread.sleep(new Random().nextInt(10) * 1000);//暂停10内随机秒，防止因频繁调用被限制IP
        }


        /**
         * 获取并添加【上海证券交易所B股】证券数据
         */
        String urlSHB = "http://query.sse.com.cn/security/stock/getStockListData.do?stockType=2&pageHelp.cacheSize=1&pageHelp.beginPage=1&pageHelp.pageSize=" + pageSize + "&pageHelp.pageNo=1";
        header = new HashMap<>();
        header.put("Referer", "http://www.sse.com.cn/");
        httpResult = httpClientUtil.pushHttpRequset("GET", urlSHB, null, header, null);
        if(null == httpResult){
            System.err.println("数据请求异常");
        }
        if(httpResult.getCode() != 200){
            System.err.println(httpResult.getData());
        }
        result = JSON.parseObject(httpResult.getData()).getJSONArray("result");
        SecuritiesCategory sh_b = securitiesCategoryService.queryByCode("sh_b");
        for(int i = 0; i < result.size(); i++){
            JSONObject jsonObject = result.getJSONObject(i);
            String name = jsonObject.getString("SECURITY_ABBR_B");
            String code = jsonObject.getString("SECURITY_CODE_B");
            String listing_date = jsonObject.getString("LISTING_DATE");
            String companyCode = jsonObject.getString("COMPANY_CODE");//公司代码

            Securities securities = securitiesMapper.queryByCodeAndSecuritiesCategory(code, sh_b.getId());
            if(null == securities){
                securities = new Securities();
                securities.setSystemCode("sh_b_" + code);
                securities.setCode(code);
                securities.setName(name);
                securities.setSecuritiesCategoryId(sh_b.getId());
                securities.setMarketTime("".equals(listing_date) ? null : sdf.parse(listing_date));
                securities.setMarketAddress("上海证券交易所");
                securitiesMapper.insert(securities);
            }
            //更新股本数据
            String urlSHB_ = "http://query.sse.com.cn/commonQuery.do?isPagination=false&sqlId=COMMON_SSE_CP_GPLB_GPGK_GBJG_C&companyCode=" + companyCode;
            header = new HashMap<>();
            header.put("Referer", "http://www.sse.com.cn/");
            httpResult = httpClientUtil.pushHttpRequset("GET", urlSHB_, null, header, null);
            if(null == httpResult){
                System.err.println("数据请求异常");
            }
            if(httpResult.getCode() != 200){
                System.err.println(httpResult.getData());
            }
            JSONArray result1 = JSON.parseObject(httpResult.getData()).getJSONArray("result");
            if(result1.size() == 0){
                securities.setFlowEquity(0L);
                securitiesMapper.updateById(securities);
                continue;
            }
            JSONObject jsonObject1 = result1.getJSONObject(0);
            Long flowEquity = Double.valueOf(jsonObject1.getDouble("B_SHARES") * 10000).longValue();
            securities.setFlowEquity(flowEquity);
            securitiesMapper.updateById(securities);
            Thread.sleep(new Random().nextInt(10) * 1000);//暂停10内随机秒，防止因频繁调用被限制IP
        }

        /**
         * 获取并添加【深证证券交易所A股】证券数据
         */
        int l = 1;
        while (true){
            String urlSZA = "http://www.szse.cn/api/report/ShowReport/data?SHOWTYPE=JSON&CATALOGID=1110&TABKEY=tab1&PAGENO=" + l + "&PAGESIZE=20";
            header = new HashMap<>();
            httpResult = httpClientUtil.pushHttpRequset("GET", urlSZA, null, header, null);
            if(null == httpResult){
                System.err.println("数据请求异常");
                continue;
            }
            if(httpResult.getCode() != 200){
                System.err.println(httpResult.getData());
                continue;
            }
            result = JSON.parseArray(httpResult.getData()).getJSONObject(0).getJSONArray("data");
            SecuritiesCategory sz_a = securitiesCategoryService.queryByCode("sz_a");
            for(int i = 0; i < result.size(); i++){
                JSONObject jsonObject = result.getJSONObject(i);
                String name = jsonObject.getString("agjc");
                name = Jsoup.parse(name).getElementsByTag("u").text();
                String code = jsonObject.getString("agdm");
                String listing_date = jsonObject.getString("agssrq");

                Securities securities = securitiesMapper.queryByCodeAndSecuritiesCategory(code, sz_a.getId());
                if(null == securities){
                    securities = new Securities();
                    securities.setSystemCode("sz_a_" + code);
                    securities.setCode(code);
                    securities.setName(name);
                    securities.setSecuritiesCategoryId(sz_a.getId());
                    securities.setMarketTime("".equals(listing_date) ? null : sdf.parse(listing_date));
                    securities.setMarketAddress("深证证券交易所");
                    securitiesMapper.insert(securities);
                }
                //更新股本数据
                String urlSZA_ = "http://www.szse.cn/api/report/index/companyGeneralization?secCode=" + code;
                httpResult = httpClientUtil.pushHttpRequset("GET", urlSZA_, null, header, null);
                if(null == httpResult){
                    System.err.println("数据请求异常");
                }
                if(httpResult.getCode() != 200){
                    System.err.println(httpResult.getData());
                }
                JSONObject data = JSON.parseObject(httpResult.getData()).getJSONObject("data");
                String agltgb = data.getString("agltgb").replaceAll(",", "");
                Long flowEquity = Double.valueOf(Double.valueOf(agltgb) * 10000).longValue();
                securities.setFlowEquity(flowEquity);
                securitiesMapper.updateById(securities);
            }
            if(result.size() == 0){
                break;
            }
            l++;
            Thread.sleep(new Random().nextInt(10) * 1000);//暂停10内随机秒，防止因频繁调用被限制IP
        }

        /**
         * 获取并添加【深证证券交易所B股】证券数据
         */
        l = 1;
        while (true){
            String urlSZB = "http://www.szse.cn/api/report/ShowReport/data?SHOWTYPE=JSON&CATALOGID=1110&TABKEY=tab2&PAGENO=" + l + "&PAGESIZE=20";
            header = new HashMap<>();
            httpResult = httpClientUtil.pushHttpRequset("GET", urlSZB, null, header, null);
            if(null == httpResult){
                System.err.println("数据请求异常");
                continue;
            }
            if(httpResult.getCode() != 200){
                System.err.println(httpResult.getData());
                continue;
            }
            result = JSON.parseArray(httpResult.getData()).getJSONObject(1).getJSONArray("data");
            SecuritiesCategory sz_b = securitiesCategoryService.queryByCode("sz_b");
            for(int i = 0; i < result.size(); i++){
                JSONObject jsonObject = result.getJSONObject(i);
                String name = jsonObject.getString("bgjc");
                name = Jsoup.parse(name).getElementsByTag("u").text();
                String code = jsonObject.getString("bgdm");
                String listing_date = jsonObject.getString("bgssrq");

                Securities securities = securitiesMapper.queryByCodeAndSecuritiesCategory(code, sz_b.getId());
                if(null == securities){
                    securities = new Securities();
                    securities.setSystemCode("sz_b_" + code);
                    securities.setCode(code);
                    securities.setName(name);
                    securities.setSecuritiesCategoryId(sz_b.getId());
                    securities.setMarketTime("".equals(listing_date) ? null : sdf.parse(listing_date));
                    securities.setMarketAddress("深证证券交易所");
                    securitiesMapper.insert(securities);
                }
                //更新股本数据
                String urlSZB_ = "http://www.szse.cn/api/report/index/companyGeneralization?secCode=" + code;
                httpResult = httpClientUtil.pushHttpRequset("GET", urlSZB_, null, header, null);
                if(null == httpResult){
                    System.err.println("数据请求异常");
                }
                if(httpResult.getCode() != 200){
                    System.err.println(httpResult.getData());
                }
                JSONObject data = JSON.parseObject(httpResult.getData()).getJSONObject("data");
                String bgltgb = data.getString("bgltgb").replaceAll(",", "");
                Long flowEquity = Double.valueOf(Double.valueOf(bgltgb) * 10000).longValue();
                securities.setFlowEquity(flowEquity);
                securitiesMapper.updateById(securities);
            }
            if(result.size() == 0){
                break;
            }
            l++;
            Thread.sleep(new Random().nextInt(10) * 1000);//暂停10内随机秒，防止因频繁调用被限制IP
        }
        System.err.println(sdf_.format(new Date()) + "更新证券基础数据任务结束。");
    }

}
