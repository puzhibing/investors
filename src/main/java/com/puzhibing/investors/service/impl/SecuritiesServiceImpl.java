package com.puzhibing.investors.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.puzhibing.investors.dao.SecuritiesMapper;
import com.puzhibing.investors.pojo.Securities;
import com.puzhibing.investors.pojo.SecuritiesCategory;
import com.puzhibing.investors.service.ISecuritiesCategoryService;
import com.puzhibing.investors.service.ISecuritiesService;
import com.puzhibing.investors.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;


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
        /**
         * 获取并添加【上海证券交易所A股】证券数据
         */
        String urlSHA = "http://query.sse.com.cn/security/stock/getStockListData.do?stockType=1&pageHelp.cacheSize=1&pageHelp.beginPage=1&pageHelp.pageSize=" + pageSize + "&pageHelp.pageNo=1";
        Map<String, String> header = new HashMap<>();
        header.put("Referer", "http://www.sse.com.cn/");
        String get = httpClientUtil.pushHttpRequset("GET", urlSHA, null, header, null);
        JSONArray result = JSON.parseObject(get).getJSONArray("result");
        SecuritiesCategory sh_a = securitiesCategoryService.queryByCode("sh_a");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for(int i = 0; i < result.size(); i++){
            JSONObject jsonObject = result.getJSONObject(i);
            String name = jsonObject.getString("SECURITY_ABBR_A");
            String code = jsonObject.getString("SECURITY_CODE_A");
            String listing_date = jsonObject.getString("LISTING_DATE");

            Securities securities = securitiesMapper.queryByCodeAndSecuritiesCategory(code, sh_a.getId());
            if(null == securities){
                securities = new Securities();
                securities.setCode(code);
                securities.setName(name);
                securities.setSecuritiesCategoryId(sh_a.getId());
                securities.setMarketTime("".equals(listing_date) ? null : sdf.parse(listing_date));
                securities.setMarketAddress("上海证券交易所");
                securitiesMapper.insert(securities);
            }
        }


        /**
         * 获取并添加【上海证券交易所B股】证券数据
         */
        String urlSHB = "http://query.sse.com.cn/security/stock/getStockListData.do?stockType=2&pageHelp.cacheSize=1&pageHelp.beginPage=1&pageHelp.pageSize=" + pageSize + "&pageHelp.pageNo=1";
        header = new HashMap<>();
        header.put("Referer", "http://www.sse.com.cn/");
        get = httpClientUtil.pushHttpRequset("GET", urlSHB, null, header, null);
        result = JSON.parseObject(get).getJSONArray("result");
        SecuritiesCategory sh_b = securitiesCategoryService.queryByCode("sh_b");
        for(int i = 0; i < result.size(); i++){
            JSONObject jsonObject = result.getJSONObject(i);
            String name = jsonObject.getString("SECURITY_ABBR_B");
            String code = jsonObject.getString("SECURITY_CODE_B");
            String listing_date = jsonObject.getString("LISTING_DATE");

            Securities securities = securitiesMapper.queryByCodeAndSecuritiesCategory(code, sh_b.getId());
            if(null == securities){
                securities = new Securities();
                securities.setCode(code);
                securities.setName(name);
                securities.setSecuritiesCategoryId(sh_b.getId());
                securities.setMarketTime("".equals(listing_date) ? null : sdf.parse(listing_date));
                securities.setMarketAddress("上海证券交易所");
                securitiesMapper.insert(securities);
            }
        }
    }
}
