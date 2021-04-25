package com.puzhibing.investors.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.puzhibing.investors.dao.SecuritiesMapper;
import com.puzhibing.investors.dao.SecuritiesMarketMapper;
import com.puzhibing.investors.pojo.Securities;
import com.puzhibing.investors.pojo.SecuritiesCategory;
import com.puzhibing.investors.pojo.SecuritiesMarket;
import com.puzhibing.investors.service.ISecuritiesCategoryService;
import com.puzhibing.investors.service.ISecuritiesMarketService;
import com.puzhibing.investors.service.ISecuritiesService;
import com.puzhibing.investors.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;


@Service
public class SecuritiesMarketServiceImpl implements ISecuritiesMarketService {

    @Resource
    private SecuritiesMarketMapper securitiesMarketMapper;

    @Resource
    private SecuritiesMapper securitiesMapper;

    @Autowired
    private HttpClientUtil httpClientUtil;

    @Autowired
    private ISecuritiesCategoryService securitiesCategoryService;

    private Integer pageSize = 5000;


    /**
     * 获取并添加证券日行情数据
     * @throws Exception
     */
    @Override
    public void pullSecuritiesMarket() throws Exception {
        /**
         * 获取【上海证券交易所A股日行情】数据
         */
        String urlSHA = "http://yunhq.sse.com.cn:32041//v1/sh1/list/exchange/ashare?select=code%2Cname%2Copen%2Chigh%2Clow%2Clast%2Cprev_close%2Cchg_rate%2Cvolume%2Camount%2Ctradephase%2Cchange%2Camp_rate%2Ccpxxsubtype%2Ccpxxprodusta&begin=0&end=" + pageSize;
        Map<String, String> header = new HashMap<>();
        header.put("Referer", "http://www.sse.com.cn/");
        String get = httpClientUtil.pushHttpRequset("GET", urlSHA, null, header, null);
        JSONObject jsonObject = JSON.parseObject(get);
        String date = jsonObject.getString("date");
        JSONArray list = jsonObject.getJSONArray("list");
        SecuritiesCategory sh_a = securitiesCategoryService.queryByCode("sh_a");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        for(int i = 0; i < list.size(); i++){
            JSONArray jsonArray = list.getJSONArray(i);
            String code = jsonArray.getString(0);
            String kpj = jsonArray.getString(2);//开盘价
            String zgj = jsonArray.getString(3);//最高价
            String zdj = jsonArray.getString(4);//最低价
            String spj = jsonArray.getString(5);//收盘价
            String sqspj = jsonArray.getString(6);//上期收盘价
            String zdl = jsonArray.getString(7);//涨跌率（%）
            String cjl = jsonArray.getString(8);//成交量（股）
            String cjje = jsonArray.getString(9);//成交金额（元）
            String zdje = jsonArray.getString(11);//涨跌金额
            String zfl = jsonArray.getString(12);//振幅率（%）

            Securities securities = securitiesMapper.queryByCodeAndSecuritiesCategory(code, sh_a.getId());
            SecuritiesMarket securitiesMarket = securitiesMarketMapper.queryBySecuritiesIdAndDate(securities.getId(), sdf.parse(date));
            if(null == securitiesMarket){
                securitiesMarket = new SecuritiesMarket();
                securitiesMarket.setSecuritiesId(securities.getId());
                securitiesMarket.setTradeDate(sdf.parse(date));
                securitiesMarket.setLastClosingPrice(sqspj);
                securitiesMarket.setClosingPrice(spj);
                securitiesMarket.setRiseFallPrice(zdje);
                securitiesMarket.setRiseFallRatio(zdl);
                securitiesMarket.setOpeningPrice(kpj);
                securitiesMarket.setTopPrice(zgj);
                securitiesMarket.setLowestPrice(zdj);
                securitiesMarket.setAmplitude(zfl);
                securitiesMarket.setVolume(cjl);
                securitiesMarket.setDealAmount(cjje);
                securitiesMarketMapper.insert(securitiesMarket);
            }
        }


        /**
         * 获取【上海证券交易所B股日行情】数据
         */
        String urlSHB = "http://yunhq.sse.com.cn:32041//v1/sh1/list/exchange/bshare?select=code%2Cname%2Copen%2Chigh%2Clow%2Clast%2Cprev_close%2Cchg_rate%2Cvolume%2Camount%2Ctradephase%2Cchange%2Camp_rate%2Ccpxxsubtype%2Ccpxxprodusta&begin=0&end=" + pageSize;
        header = new HashMap<>();
        header.put("Referer", "http://www.sse.com.cn/");
        get = httpClientUtil.pushHttpRequset("GET", urlSHB, null, header, null);
        jsonObject = JSON.parseObject(get);
        date = jsonObject.getString("date");
        list = jsonObject.getJSONArray("list");
        SecuritiesCategory sh_b = securitiesCategoryService.queryByCode("sh_b");
        for(int i = 0; i < list.size(); i++){
            JSONArray jsonArray = list.getJSONArray(i);
            String code = jsonArray.getString(0);
            String kpj = jsonArray.getString(2);//开盘价
            String zgj = jsonArray.getString(3);//最高价
            String zdj = jsonArray.getString(4);//最低价
            String spj = jsonArray.getString(5);//收盘价
            String sqspj = jsonArray.getString(6);//上期收盘价
            String zdl = jsonArray.getString(7);//涨跌率（%）
            String cjl = jsonArray.getString(8);//成交量（股）
            String cjje = jsonArray.getString(9);//成交金额（元）
            String zdje = jsonArray.getString(11);//涨跌金额
            String zfl = jsonArray.getString(12);//振幅率（%）

            Securities securities = securitiesMapper.queryByCodeAndSecuritiesCategory(code, sh_b.getId());
            SecuritiesMarket securitiesMarket = securitiesMarketMapper.queryBySecuritiesIdAndDate(securities.getId(), sdf.parse(date));
            if(null == securitiesMarket){
                securitiesMarket = new SecuritiesMarket();
                securitiesMarket.setSecuritiesId(securities.getId());
                securitiesMarket.setTradeDate(sdf.parse(date));
                securitiesMarket.setLastClosingPrice(sqspj);
                securitiesMarket.setClosingPrice(spj);
                securitiesMarket.setRiseFallPrice(zdje);
                securitiesMarket.setRiseFallRatio(zdl);
                securitiesMarket.setOpeningPrice(kpj);
                securitiesMarket.setTopPrice(zgj);
                securitiesMarket.setLowestPrice(zdj);
                securitiesMarket.setAmplitude(zfl);
                securitiesMarket.setVolume(cjl);
                securitiesMarket.setDealAmount(cjje);
                securitiesMarketMapper.insert(securitiesMarket);
            }
        }
    }
}
