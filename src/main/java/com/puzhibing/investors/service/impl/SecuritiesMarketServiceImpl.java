package com.puzhibing.investors.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.puzhibing.investors.dao.*;
import com.puzhibing.investors.pojo.*;
import com.puzhibing.investors.service.ISecuritiesCategoryService;
import com.puzhibing.investors.service.ISecuritiesMarketService;
import com.puzhibing.investors.util.DateUtil;
import com.puzhibing.investors.util.HttpClientUtil;
import com.puzhibing.investors.util.ResultUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class SecuritiesMarketServiceImpl implements ISecuritiesMarketService {

    @Resource
    private SecuritiesMapper securitiesMapper;

    @Autowired
    private HttpClientUtil httpClientUtil;

    @Autowired
    private ISecuritiesCategoryService securitiesCategoryService;

    @Resource
    private SHASecuritiesMarketMapper shaSecuritiesMarketMapper;

    @Resource
    private SHBSecuritiesMarketMapper shbSecuritiesMarketMapper;

    @Resource
    private SZASecuritiesMarketMapper szaSecuritiesMarketMapper;

    @Resource
    private SZBSecuritiesMarketMapper szbSecuritiesMarketMapper;

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
        String urlSHA = "http://yunhq.sse.com.cn:32041//v1/sh1/list/exchange/ashare?select=code%2Cname%2Copen%2C" +
                "high%2Clow%2Clast%2Cprev_close%2Cchg_rate%2Cvolume%2Camount%2Ctradephase%2Cchange%2Camp_rate%2C" +
                "cpxxsubtype%2Ccpxxprodusta&begin=0&end=" + pageSize;
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
            SHASecuritiesMarket shaSecuritiesMarket = shaSecuritiesMarketMapper.queryBySecuritiesIdAndDate(securities.getId(), sdf.parse(date));
            if(null == shaSecuritiesMarket){
                shaSecuritiesMarket = new SHASecuritiesMarket();
                shaSecuritiesMarket.setSecuritiesId(securities.getId());
                shaSecuritiesMarket.setTradeDate(sdf.parse(date));
                shaSecuritiesMarket.setClosingPrice(spj);
                shaSecuritiesMarket.setRiseFallPrice(zdje);
                shaSecuritiesMarket.setRiseFallRatio(zdl);
                shaSecuritiesMarket.setOpeningPrice(kpj);
                shaSecuritiesMarket.setTopPrice(zgj);
                shaSecuritiesMarket.setLowestPrice(zdj);
                shaSecuritiesMarket.setAmplitude(zfl);
                shaSecuritiesMarket.setVolume(cjl);
                shaSecuritiesMarket.setDealAmount(cjje);
                shaSecuritiesMarket.setLastClosingPrice(sqspj);
                shaSecuritiesMarketMapper.insert(shaSecuritiesMarket);
            }
        }

        /**
         * 获取【上海证券交易所B股日行情】数据
         */
        String urlSHB = "http://yunhq.sse.com.cn:32041//v1/sh1/list/exchange/bshare?select=code%2Cname%2Copen%2Chigh%2C" +
                "low%2Clast%2Cprev_close%2Cchg_rate%2Cvolume%2Camount%2Ctradephase%2Cchange%2Camp_rate%2Ccpxxsubtype%2C" +
                "cpxxprodusta&begin=0&end=" + pageSize;
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
            SHBSecuritiesMarket shbSecuritiesMarket = shbSecuritiesMarketMapper.queryBySecuritiesIdAndDate(securities.getId(), sdf.parse(date));
            if(null == shbSecuritiesMarket){
                shbSecuritiesMarket = new SHBSecuritiesMarket();
                shbSecuritiesMarket.setSecuritiesId(securities.getId());
                shbSecuritiesMarket.setTradeDate(sdf.parse(date));
                shbSecuritiesMarket.setClosingPrice(spj);
                shbSecuritiesMarket.setRiseFallPrice(zdje);
                shbSecuritiesMarket.setRiseFallRatio(zdl);
                shbSecuritiesMarket.setOpeningPrice(kpj);
                shbSecuritiesMarket.setTopPrice(zgj);
                shbSecuritiesMarket.setLowestPrice(zdj);
                shbSecuritiesMarket.setAmplitude(zfl);
                shbSecuritiesMarket.setVolume(cjl);
                shbSecuritiesMarket.setDealAmount(cjje);
                shbSecuritiesMarket.setLastClosingPrice(sqspj);
                shbSecuritiesMarketMapper.insert(shbSecuritiesMarket);
            }
        }

        /**
         * 获取【深证证券交易所A股日行情】数据
         */
        SecuritiesCategory sz_a = securitiesCategoryService.queryByCode("sz_a");
        List<Securities> securities1 = securitiesMapper.queryList(null, sz_a.getId(), null, null);
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

            SZASecuritiesMarket szaSecuritiesMarket = szaSecuritiesMarketMapper.queryBySecuritiesIdAndDate(securities.getId(), sdf1.parse(datetime));
            if(null == szaSecuritiesMarket){
                szaSecuritiesMarket = new SZASecuritiesMarket();
                szaSecuritiesMarket.setSecuritiesId(securities.getId());
                szaSecuritiesMarket.setTradeDate(sdf1.parse(datetime));
                szaSecuritiesMarket.setClosingPrice(spj);
                szaSecuritiesMarket.setRiseFallPrice(zdje);
                szaSecuritiesMarket.setRiseFallRatio(zdl);
                szaSecuritiesMarket.setOpeningPrice(kpj);
                szaSecuritiesMarket.setTopPrice(zgj);
                szaSecuritiesMarket.setLowestPrice(zdj);
                szaSecuritiesMarket.setAmplitude(zfl);
                szaSecuritiesMarket.setVolume(cjl);
                szaSecuritiesMarket.setDealAmount(cjje);
                szaSecuritiesMarket.setLastClosingPrice(sqspj);
                szaSecuritiesMarketMapper.insert(szaSecuritiesMarket);
            }

        }


        /**
         * 获取【深证证券交易所B股日行情】数据
         */
        SecuritiesCategory sz_b = securitiesCategoryService.queryByCode("sz_b");
        List<Securities> securities2 = securitiesMapper.queryList(null, sz_b.getId(), null, null);
        for(Securities securities : securities2){
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

            SZBSecuritiesMarket szbSecuritiesMarket = szbSecuritiesMarketMapper.queryBySecuritiesIdAndDate(securities.getId(), sdf1.parse(datetime));
            if(null == szbSecuritiesMarket){
                szbSecuritiesMarket = new SZBSecuritiesMarket();
                szbSecuritiesMarket.setSecuritiesId(securities.getId());
                szbSecuritiesMarket.setTradeDate(sdf1.parse(datetime));
                szbSecuritiesMarket.setClosingPrice(spj);
                szbSecuritiesMarket.setRiseFallPrice(zdje);
                szbSecuritiesMarket.setRiseFallRatio(zdl);
                szbSecuritiesMarket.setOpeningPrice(kpj);
                szbSecuritiesMarket.setTopPrice(zgj);
                szbSecuritiesMarket.setLowestPrice(zdj);
                szbSecuritiesMarket.setAmplitude(zfl);
                szbSecuritiesMarket.setVolume(cjl);
                szbSecuritiesMarket.setDealAmount(cjje);
                szbSecuritiesMarket.setLastClosingPrice(sqspj);
                szbSecuritiesMarketMapper.insert(szbSecuritiesMarket);
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
        List<Map<String, Object>> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
        for(Securities s : securities){
            Map<String, Object> map = new HashMap<>();
            map.put("name", s.getName() + "(" + s.getCode() + ")");
            SecuritiesCategory securitiesCategory = securitiesCategoryService.selectById(s.getSecuritiesCategoryId());
            List<String> d = new ArrayList<>();
            List<Double> v = new ArrayList<>();
            switch (securitiesCategory.getCode()){
                case "sh_a":
                    List<SHASecuritiesMarket> shaSecuritiesMarkets = shaSecuritiesMarketMapper.queryList(s.getId(), start, end);
                    for(SHASecuritiesMarket sha : shaSecuritiesMarkets){
                        d.add(sdf.format(sha.getTradeDate()));
                        v.add(Double.valueOf(sha.getClosingPrice()));
                    }
                    break;
                case "sh_b":
                    List<SHBSecuritiesMarket> shbSecuritiesMarkets = shbSecuritiesMarketMapper.queryList(s.getId(), start, end);
                    for(SHBSecuritiesMarket shb : shbSecuritiesMarkets){
                        d.add(sdf.format(shb.getTradeDate()));
                        v.add(Double.valueOf(shb.getClosingPrice()));
                    }
                    break;
                case "sz_a":
                    List<SZASecuritiesMarket> szaSecuritiesMarkets = szaSecuritiesMarketMapper.queryList(s.getId(), start, end);
                    for(SZASecuritiesMarket sza : szaSecuritiesMarkets){
                        d.add(sdf.format(sza.getTradeDate()));
                        v.add(Double.valueOf(sza.getClosingPrice()));
                    }
                    break;
                case "sz_b":
                    List<SZBSecuritiesMarket> szbSecuritiesMarkets = szbSecuritiesMarketMapper.queryList(s.getId(), start, end);
                    for(SZBSecuritiesMarket szb : szbSecuritiesMarkets){
                        d.add(sdf.format(szb.getTradeDate()));
                        v.add(Double.valueOf(szb.getClosingPrice()));
                    }
                    break;
            }
            map.put("latitude", d);
            map.put("value", v);
            list.add(map);
        }
        return list;
    }

    /**
     * 同步历史交易数据
     * @return
     * @throws Exception
     */
    @Override
    public ResultUtil synchronizeHistoricalData() throws Exception {
        List<Securities> sh_a = securitiesMapper.querySecuritiesList(null, "sh_a");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        for(Securities s : sh_a){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            Date date = new Date();
            Integer year = Integer.valueOf(sdf.format(date));//年份
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            while (true){
                int quarter = DateUtil.createDate(date).QUARTER;//季节
                boolean b = false;
                while (true){
                    String url = "http://quotes.money.163.com/trade/lsjysj_" + s.getCode() + ".html?year=" + year + "&season=" + quarter;
                    String data = httpClientUtil.pushHttpRequset("GET", url, null, null, "json");
                    Document document = Jsoup.parse(data);
                    Element element = document.getElementsByClass("table_bg001").get(0);
                    Elements tr = element.getElementsByTag("tr");
                    if(tr.size() == 0){//没有数据
                        b = false;
                        break;
                    }
                    //解析数据
                    for(int i = 0; i < tr.size(); i++){
                        Elements td = tr.get(i).getElementsByTag("td");
                        if(td.size() == 0){
                            continue;
                        }
                        String rq = td.get(0).text();//日期
                        String kpj = td.get(1).text();//开盘价
                        String zgj = td.get(2).text();//最高价
                        String zdj = td.get(3).text();//最低价
                        String spj = td.get(4).text();//收盘价
                        String zde = td.get(5).text();//涨跌额
                        String zdf = td.get(6).text();//涨跌幅(%)
                        String cjl = td.get(7).text().replaceAll(",", "");//成交量(手)
                        String cjje = td.get(8).text().replaceAll(",", "");//成交金额(万元)
                        String zf = td.get(9).text();//振幅(%)
                        String hsl = td.get(10).text();//换手率(%)
                        String sqspj = String.valueOf(Double.valueOf(spj) - Double.valueOf(zde));//上期收盘价

                        SHASecuritiesMarket shaSecuritiesMarket = shaSecuritiesMarketMapper.queryBySecuritiesIdAndDate(s.getId(), sdf1.parse(rq));
                        if(null != shaSecuritiesMarket){
                            if(!StringUtils.hasLength(shaSecuritiesMarket.getLastClosingPrice())){
                                shaSecuritiesMarket.setLastClosingPrice(sqspj);
                            }
                            if(!StringUtils.hasLength(shaSecuritiesMarket.getClosingPrice())){
                                shaSecuritiesMarket.setClosingPrice(spj);
                            }
                            if(!StringUtils.hasLength(shaSecuritiesMarket.getRiseFallPrice())){
                                shaSecuritiesMarket.setRiseFallPrice(zde);
                            }
                            if(!StringUtils.hasLength(shaSecuritiesMarket.getRiseFallRatio())){
                                shaSecuritiesMarket.setRiseFallRatio(zdf);
                            }
                            if(!StringUtils.hasLength(shaSecuritiesMarket.getOpeningPrice())){
                                shaSecuritiesMarket.setOpeningPrice(kpj);
                            }
                            if(!StringUtils.hasLength(shaSecuritiesMarket.getTopPrice())){
                                shaSecuritiesMarket.setTopPrice(zgj);
                            }
                            if(!StringUtils.hasLength(shaSecuritiesMarket.getLowestPrice())){
                                shaSecuritiesMarket.setLowestPrice(zdj);
                            }
                            if(!StringUtils.hasLength(shaSecuritiesMarket.getAmplitude())){
                                shaSecuritiesMarket.setAmplitude(zf);
                            }
                            if(!StringUtils.hasLength(shaSecuritiesMarket.getVolume())){
                                shaSecuritiesMarket.setVolume(String.valueOf(Integer.valueOf(cjl) * 100));
                            }
                            if(!StringUtils.hasLength(shaSecuritiesMarket.getDealAmount())){
                                shaSecuritiesMarket.setDealAmount(String.valueOf(Integer.valueOf(cjje) * 10000));
                            }
                            if(!StringUtils.hasLength(shaSecuritiesMarket.getTurnoverRate())){
                                shaSecuritiesMarket.setTurnoverRate(hsl);
                            }
                            shaSecuritiesMarketMapper.update(shaSecuritiesMarket);
                        }else{
                            shaSecuritiesMarket.setSecuritiesId(s.getId());
                            shaSecuritiesMarket.setTradeDate(sdf1.parse(rq));
                            shaSecuritiesMarket.setLastClosingPrice(sqspj);
                            shaSecuritiesMarket.setClosingPrice(spj);
                            shaSecuritiesMarket.setRiseFallPrice(zde);
                            shaSecuritiesMarket.setRiseFallRatio(zdf);
                            shaSecuritiesMarket.setOpeningPrice(kpj);
                            shaSecuritiesMarket.setTopPrice(zgj);
                            shaSecuritiesMarket.setLowestPrice(zdj);
                            shaSecuritiesMarket.setAmplitude(zf);
                            shaSecuritiesMarket.setVolume(String.valueOf(Integer.valueOf(cjl) * 100));
                            shaSecuritiesMarket.setDealAmount(String.valueOf(Integer.valueOf(cjje) * 10000));
                            shaSecuritiesMarket.setTurnoverRate(hsl);
                            shaSecuritiesMarketMapper.insert(shaSecuritiesMarket);
                        }

                    }




                    quarter--;
                    if(quarter == 0){
                        break;
                    }
                }
                if(b){//没有数据可采集。
                    break;
                }
                year--;
            }


        }


        return null;
    }
}
