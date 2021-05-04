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
import com.puzhibing.investors.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;


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
                securitiesMarket.setClosingPrice(spj);
                securitiesMarket.setRiseFallPrice(zdje);
                securitiesMarket.setRiseFallRatio(zdl);
                securitiesMarket.setOpeningPrice(kpj);
                securitiesMarket.setTopPrice(zgj);
                securitiesMarket.setLowestPrice(zdj);
                securitiesMarket.setAmplitude(zfl);
                securitiesMarket.setVolume(cjl);
                securitiesMarket.setDealAmount(cjje);
                securitiesMarket.setLastClosingPrice(sqspj);
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
                securitiesMarket.setClosingPrice(spj);
                securitiesMarket.setRiseFallPrice(zdje);
                securitiesMarket.setRiseFallRatio(zdl);
                securitiesMarket.setOpeningPrice(kpj);
                securitiesMarket.setTopPrice(zgj);
                securitiesMarket.setLowestPrice(zdj);
                securitiesMarket.setAmplitude(zfl);
                securitiesMarket.setVolume(cjl);
                securitiesMarket.setDealAmount(cjje);
                securitiesMarket.setLastClosingPrice(sqspj);
                securitiesMarketMapper.insert(securitiesMarket);
            }
        }

        /**
         * 获取【深证证券交易所A、B股日行情】数据
         */
        SecuritiesCategory sz_a = securitiesCategoryService.queryByCode("sz_a");
        List<Securities> securities1 = securitiesMapper.queryList(null, sz_a.getId(), null, null);
        SecuritiesCategory sz_b = securitiesCategoryService.queryByCode("sz_b");
        List<Securities> securities2 = securitiesMapper.queryList(null, sz_b.getId(), null, null);
        securities1.addAll(securities2);
        for(Securities securities : securities1){
            String urlSZ = "http://www.szse.cn/api/market/ssjjhq/getTimeData?marketId=1&code=" + securities.getCode();
            header = new HashMap<>();
            get = httpClientUtil.pushHttpRequset("GET", urlSZ, null, header, null);
            JSONObject jsonObject1 = JSON.parseObject(get);
            jsonObject = jsonObject1.getJSONObject("data");
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String datetime = jsonObject1.getString("datetime");
            String code = jsonObject.getString("code");
            String kpj = jsonObject.getString("open");//开盘价
            String zgj = jsonObject.getString("high");//最高价
            String zdj = jsonObject.getString("low");//最低价
            String spj = jsonObject.getString("now");//收盘价
            String sqspj = jsonObject.getString("close");//上期收盘价
            String zdl = jsonObject.getString("deltaPercent");//涨跌率（%）
            String cjl = jsonObject.getString("volume");//成交量（股）
            String cjje = jsonObject.getString("amount");//成交金额（元）
            String zdje = jsonObject.getString("delta");//涨跌金额
            String zfl = null;
            if(null == kpj){//停牌
                kpj = sqspj;//开盘价
                zgj = sqspj;//最高价
                zdj = sqspj;//最低价
                spj = sqspj;//收盘价
                zdl = "0";//涨跌率（%）
                cjl = "0";//成交量（股）
                cjje = "0";//成交金额（元）
                zdje = "0";//涨跌金额
                zfl = "0";//振幅率（%）
            }else{
                BigDecimal divide = new BigDecimal(Double.valueOf(zgj) - Double.valueOf(zdj)).multiply(new BigDecimal(100)).divide(new BigDecimal(sqspj), new MathContext(2, RoundingMode.HALF_EVEN));
                zfl = divide.toString();//振幅率（%）
            }
            JSONArray picdowndata = jsonObject.getJSONArray("picdowndata");
            Long inout = 0L;
            if(null != picdowndata){
                for(int i = 0; i < picdowndata.size(); i++){
                    JSONArray jsonArray = picdowndata.getJSONArray(i);
                    String time = jsonArray.getString(0);
                    Integer num = jsonArray.getInteger(1);
                    String direction = jsonArray.getString(2);
                    inout = ("plus".equals(direction) ? (inout + num) : (inout - num));
                }
            }

            SecuritiesMarket securitiesMarket = securitiesMarketMapper.queryBySecuritiesIdAndDate(securities.getId(), sdf1.parse(datetime));
            if(null == securitiesMarket){
                securitiesMarket = new SecuritiesMarket();
                securitiesMarket.setSecuritiesId(securities.getId());
                securitiesMarket.setTradeDate(sdf1.parse(datetime));
                securitiesMarket.setClosingPrice(spj);
                securitiesMarket.setRiseFallPrice(zdje);
                securitiesMarket.setRiseFallRatio(zdl);
                securitiesMarket.setOpeningPrice(kpj);
                securitiesMarket.setTopPrice(zgj);
                securitiesMarket.setLowestPrice(zdj);
                securitiesMarket.setAmplitude(zfl);
                securitiesMarket.setVolume(cjl);
                securitiesMarket.setDealAmount(cjje);
                securitiesMarket.setLastClosingPrice(sqspj);
                securitiesMarketMapper.insert(securitiesMarket);
            }

        }



    }


    /**
     * 获取指定时间范围内的数据
     * @param code
     * @param date
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> queryAllData(String code, Integer securitiesCategoryId, String date, Integer pageNo, Integer pageSize) throws Exception {
        Date start = null;
        Date end = null;
        pageNo = (pageNo - 1) * pageSize;
        if(!"".equals(date) && null != date){
            String[] split = date.split(" - ");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            start = sdf.parse(split[0] + " 00:00:00");
            end = sdf.parse(split[1] + " 23:59:59");
        }
        List<Securities> securities = securitiesMapper.queryList(code, securitiesCategoryId, pageNo, pageSize);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        List<Map<String, Object>> datas = new ArrayList<>();//存储数据集合
        for(Securities s : securities){
            Map<String, Object> data = new HashMap<>();
            Set<String> d = new HashSet<>();//存储日期
            Map<String, Object> value = new HashMap<>();//存储数据
            List<Double> v1 = new ArrayList<>();
            List<Double> v2 = new ArrayList<>();
            List<SecuritiesMarket> securitiesMarkets = securitiesMarketMapper.queryList(s.getId(), start, end);
            Double avg = securitiesMarketMapper.queryClosingPriceAvg(s.getId());
            for(SecuritiesMarket sm : securitiesMarkets){
                d.add(sdf.format(sm.getTradeDate()));
                v1.add(avg);
                v2.add(Double.valueOf(sm.getClosingPrice()));
            }
            value.put("avg", v1);
            value.put("data", v2);
            data.put("date", d);
            data.put("value", value);
            datas.add(data);
        }
        return datas;
    }
}
