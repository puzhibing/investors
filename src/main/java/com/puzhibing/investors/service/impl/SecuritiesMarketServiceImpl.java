package com.puzhibing.investors.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.puzhibing.investors.dao.*;
import com.puzhibing.investors.pojo.*;
import com.puzhibing.investors.pojo.vo.MarketMovingAverageVo;
import com.puzhibing.investors.pojo.vo.SecuritiesMarketVo;
import com.puzhibing.investors.service.*;
import com.puzhibing.investors.util.DateUtil;
import com.puzhibing.investors.util.FileUtil;
import com.puzhibing.investors.util.http.HttpClientUtil;
import com.puzhibing.investors.util.ResultUtil;
import com.puzhibing.investors.util.http.HttpResult;
import com.puzhibing.investors.util.redis.RedisUtil;
import org.apache.commons.lang3.math.NumberUtils;
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

    @Autowired
    private ISHASecuritiesMarketService shaSecuritiesMarketService;

    @Autowired
    private ISHBSecuritiesMarketService shbSecuritiesMarketService;

    @Autowired
    private ISZASecuritiesMarketService szaSecuritiesMarketService;

    @Autowired
    private ISZBSecuritiesMarketService szbSecuritiesMarketService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private FileUtil fileUtil;

    private Integer pageSize = 5000;


    /**
     * 获取并添加证券日行情数据
     * @throws Exception
     */
    @Override
    public void pullSecuritiesMarket() throws Exception {
        System.err.println("更新日行情数据任务开始。");
        /**
         * 获取【上海证券交易所A股日行情】数据
         */
        String urlSHA = "http://yunhq.sse.com.cn:32041//v1/sh1/list/exchange/ashare?select=code%2Cname%2Copen%2C" +
                "high%2Clow%2Clast%2Cprev_close%2Cchg_rate%2Cvolume%2Camount%2Ctradephase%2Cchange%2Camp_rate%2C" +
                "cpxxsubtype%2Ccpxxprodusta&begin=0&end=" + pageSize;
        Map<String, String> header = new HashMap<>();
        header.put("Referer", "http://www.sse.com.cn/");
        HttpResult httpResult = httpClientUtil.pushHttpRequset("GET", urlSHA, null, header, null);
        if(null == httpResult){
            System.err.println("数据请求异常");
        }
        if(httpResult.getCode() != 200){
            System.err.println(httpResult.getData());
        }
        JSONObject jsonObject = JSON.parseObject(httpResult.getData());
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
            SHASecuritiesMarket shaSecuritiesMarket = new SHASecuritiesMarket();
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
            //计算换手率（成交量/流通股）
            String string = new BigDecimal(cjl).divide(new BigDecimal(securities.getFlowEquity()), new MathContext(2, RoundingMode.HALF_EVEN)).toString();
            shaSecuritiesMarket.setTurnoverRate(string);
            saveMarketToFile(securities.getSystemCode(), shaSecuritiesMarket, shaSecuritiesMarket.getTradeDate());//保存数据到文件中
        }

        /**
         * 获取【上海证券交易所B股日行情】数据
         */
        String urlSHB = "http://yunhq.sse.com.cn:32041//v1/sh1/list/exchange/bshare?select=code%2Cname%2Copen%2Chigh%2C" +
                "low%2Clast%2Cprev_close%2Cchg_rate%2Cvolume%2Camount%2Ctradephase%2Cchange%2Camp_rate%2Ccpxxsubtype%2C" +
                "cpxxprodusta&begin=0&end=" + pageSize;
        header = new HashMap<>();
        header.put("Referer", "http://www.sse.com.cn/");
        httpResult = httpClientUtil.pushHttpRequset("GET", urlSHB, null, header, null);
        if(null == httpResult){
            System.err.println("数据请求异常");
        }
        if(httpResult.getCode() != 200){
            System.err.println(httpResult.getData());
        }
        jsonObject = JSON.parseObject(httpResult.getData());
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
            SHBSecuritiesMarket shbSecuritiesMarket = new SHBSecuritiesMarket();
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
            //计算换手率（成交量/流通股）
            String string = new BigDecimal(cjl).divide(new BigDecimal(securities.getFlowEquity()), new MathContext(2, RoundingMode.HALF_EVEN)).toString();
            shbSecuritiesMarket.setTurnoverRate(string);
            saveMarketToFile(securities.getSystemCode(), shbSecuritiesMarket, shbSecuritiesMarket.getTradeDate());//保存数据到文件中
        }

        /**
         * 获取【深证证券交易所A股日行情】数据
         */
        SecuritiesCategory sz_a = securitiesCategoryService.queryByCode("sz_a");
        List<Securities> securities1 = securitiesMapper.queryList(null, sz_a.getId(), null, null);
        for(Securities securities : securities1){
            String urlSZ = "http://www.szse.cn/api/market/ssjjhq/getTimeData?marketId=1&code=" + securities.getCode();
            header = new HashMap<>();
            httpResult = httpClientUtil.pushHttpRequset("GET", urlSZ, null, header, null);
            if(null == httpResult){
                System.err.println("数据请求异常");
                continue;
            }
            if(httpResult.getCode() != 200){
                System.err.println(httpResult.getData());
                continue;
            }
            JSONObject jsonObject1 = JSON.parseObject(httpResult.getData());
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

            SZASecuritiesMarket szaSecuritiesMarket = new SZASecuritiesMarket();
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
            //计算换手率（成交量/流通股）
            String string = new BigDecimal(cjl).divide(new BigDecimal(securities.getFlowEquity()), new MathContext(2, RoundingMode.HALF_EVEN)).toString();
            szaSecuritiesMarket.setTurnoverRate(string);
            saveMarketToFile(securities.getSystemCode(), szaSecuritiesMarket, szaSecuritiesMarket.getTradeDate());//保存数据到文件中

            Thread.sleep(new Random().nextInt(10) * 1000);//暂停10内随机秒，防止因频繁调用被限制IP
        }


        /**
         * 获取【深证证券交易所B股日行情】数据
         */
        SecuritiesCategory sz_b = securitiesCategoryService.queryByCode("sz_b");
        List<Securities> securities2 = securitiesMapper.queryList(null, sz_b.getId(), null, null);
        for(Securities securities : securities2){
            String urlSZ = "http://www.szse.cn/api/market/ssjjhq/getTimeData?marketId=1&code=" + securities.getCode();
            header = new HashMap<>();
            httpResult = httpClientUtil.pushHttpRequset("GET", urlSZ, null, header, null);
            if(null == httpResult){
                System.err.println("数据请求异常");
                continue;
            }
            if(httpResult.getCode() != 200){
                System.err.println(httpResult.getData());
                continue;
            }
            JSONObject jsonObject1 = JSON.parseObject(httpResult.getData());
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
            SZBSecuritiesMarket szbSecuritiesMarket = new SZBSecuritiesMarket();
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
            String string = new BigDecimal(cjl).divide(new BigDecimal(securities.getFlowEquity()), new MathContext(2, RoundingMode.HALF_EVEN)).toString();
            szbSecuritiesMarket.setTurnoverRate(string);
            saveMarketToFile(securities.getSystemCode(), szbSecuritiesMarket, szbSecuritiesMarket.getTradeDate());//保存数据到文件中

            Thread.sleep(new Random().nextInt(10) * 1000);//暂停10内随机秒，防止因频繁调用被限制IP
        }
        System.err.println("更新日行情数据任务结束。");

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    SecuritiesMarketServiceImpl.this.movingAverage(5);
//                    SecuritiesMarketServiceImpl.this.movingAverage(15);
//                    SecuritiesMarketServiceImpl.this.movingAverage(30);
//                    SecuritiesMarketServiceImpl.this.movingAverage(90);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }


    /**
     * 计算移动平均值并进行存储
     * @param days
     * @throws Exception
     */
    public void movingAverage(Integer days) throws Exception{
        List<Securities> list = securitiesMapper.querySecuritiesList(null, null);
        for(Securities s : list){
            List<MarketMovingAverageVo> avgList = new ArrayList<>();
            SecuritiesCategory securitiesCategory = securitiesCategoryService.selectById(s.getSecuritiesCategoryId());
            String value = redisUtil.getValue(s.getSystemCode());
            JSONObject jsonObject = JSON.parseObject(value);
            JSONArray market = jsonObject.getJSONArray("market");
            for(int i = 0; i < market.size(); i++){
                SecuritiesMarketVo object = market.getObject(i, SecuritiesMarketVo.class);
                Double avgClosingPrice = getAvgClosingPrice(securitiesCategory.getCode(), s.getId(), object.getTradeDate(), days);
                MarketMovingAverageVo m = new MarketMovingAverageVo();
                m.setAvg(avgClosingPrice.toString());
                m.setDay(object.getTradeDate());
                avgList.add(m);
            }
            jsonObject.put(days + "movingAverage", avgList);
            redisUtil.setStrValue(s.getSystemCode(), jsonObject.toJSONString());
            fileUtil.write(s.getSystemCode() + ".json", jsonObject.toJSONString(), false);
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
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
        int y = Integer.valueOf(sdf1.format(new Date())) - 0;
        for(Securities s : securities){
            Map<String, Object> map = new HashMap<>();
            map.put("name", s.getName() + "(" + s.getCode() + ")");
            SecuritiesCategory securitiesCategory = securitiesCategoryService.selectById(s.getSecuritiesCategoryId());
            List<String> d = new ArrayList<>();//日期
            List<Double> day = new ArrayList<>();//1天收盘价
            List<Double> h = new ArrayList<>();//换手率
            List<Double> weeks = new ArrayList<>();//5天移动平均值
            List<Double> months = new ArrayList<>();//30天移动平均值
            List<Double> years = new ArrayList<>();//365天移动平均值
            List<Double> civilYear = new ArrayList<>();//自然年的平均值
            int year = 0;//年份
            Double yearAvg = 0D;
            String startTime = "";
            switch (securitiesCategory.getCode()){
                case "sh_a":
                    List<SHASecuritiesMarket> shaSecuritiesMarkets = shaSecuritiesMarketService.queryList(s.getId(), start, end);;
                    for(SHASecuritiesMarket sha : shaSecuritiesMarkets){
                        d.add(sdf.format(sha.getTradeDate()));
                        day.add(Double.valueOf(sha.getClosingPrice()));//收盘价
                        h.add(null == sha.getTurnoverRate() ? 0 : Double.valueOf(sha.getTurnoverRate()));//换手率
                        int y1 = Integer.valueOf(sdf1.format(sha.getTradeDate())).intValue();
                        if(!StringUtils.hasLength(startTime) && y == y1){
                            startTime = sdf.format(sha.getTradeDate());
                        }
                        Double week = getAvgClosingPrice(securitiesCategory.getCode(), s.getId(), sha.getTradeDate(), 5);
                        weeks.add(week);

                        Double month = getAvgClosingPrice(securitiesCategory.getCode(), s.getId(), sha.getTradeDate(), 30);
                        months.add(month);

                        Double yy = getAvgClosingPrice(securitiesCategory.getCode(), s.getId(), sha.getTradeDate(), 365);
                        years.add(yy);

                        int ye = Integer.valueOf(sdf1.format(sha.getTradeDate())).intValue();
                        if(year != ye){
                            yearAvg = getYearAvgClosingPrice(securitiesCategory.getCode(), s.getId(), sha.getTradeDate());
                            year = ye;
                        }
                        civilYear.add(yearAvg);

                    }
                    break;
                case "sh_b":
                    List<SHBSecuritiesMarket> shbSecuritiesMarkets = shbSecuritiesMarketService.queryList(s.getId(), start, end);
                    for(SHBSecuritiesMarket shb : shbSecuritiesMarkets){
                        d.add(sdf.format(shb.getTradeDate()));
                        day.add(Double.valueOf(shb.getClosingPrice()));//收盘价
                        h.add(null == shb.getTurnoverRate() ? 0 : Double.valueOf(shb.getTurnoverRate()));//换手率
                        int y1 = Integer.valueOf(sdf1.format(shb.getTradeDate())).intValue();
                        if(!StringUtils.hasLength(startTime) && y == y1){
                            startTime = sdf.format(shb.getTradeDate());
                        }
                        Double week = getAvgClosingPrice(securitiesCategory.getCode(), s.getId(), shb.getTradeDate(), 5);
                        weeks.add(week);

                        Double month = getAvgClosingPrice(securitiesCategory.getCode(), s.getId(), shb.getTradeDate(), 30);
                        months.add(month);

                        Double yy = getAvgClosingPrice(securitiesCategory.getCode(), s.getId(), shb.getTradeDate(), 365);
                        years.add(yy);

                        int ye = Integer.valueOf(sdf1.format(shb.getTradeDate())).intValue();
                        if(year != ye){
                            yearAvg = getYearAvgClosingPrice(securitiesCategory.getCode(), s.getId(), shb.getTradeDate());
                            year = ye;
                        }
                        civilYear.add(yearAvg);
                    }
                    break;
                case "sz_a":
                    List<SZASecuritiesMarket> szaSecuritiesMarkets = szaSecuritiesMarketService.queryList(s.getId(), start, end);
                    for(SZASecuritiesMarket sza : szaSecuritiesMarkets){
                        d.add(sdf.format(sza.getTradeDate()));
                        day.add(Double.valueOf(sza.getClosingPrice()));//收盘价
                        h.add(null == sza.getTurnoverRate() ? 0 : Double.valueOf(sza.getTurnoverRate()));//换手率
                        int y1 = Integer.valueOf(sdf1.format(sza.getTradeDate())).intValue();
                        if(!StringUtils.hasLength(startTime) && y == y1){
                            startTime = sdf.format(sza.getTradeDate());
                        }
                        Double week = getAvgClosingPrice(securitiesCategory.getCode(), s.getId(), sza.getTradeDate(), 5);
                        weeks.add(week);

                        Double month = getAvgClosingPrice(securitiesCategory.getCode(), s.getId(), sza.getTradeDate(), 30);
                        months.add(month);

                        Double yy = getAvgClosingPrice(securitiesCategory.getCode(), s.getId(), sza.getTradeDate(), 365);
                        years.add(yy);

                        int ye = Integer.valueOf(sdf1.format(sza.getTradeDate())).intValue();
                        if(year != ye){
                            yearAvg = getYearAvgClosingPrice(securitiesCategory.getCode(), s.getId(), sza.getTradeDate());
                            year = ye;
                        }
                        civilYear.add(yearAvg);
                    }
                    break;
                case "sz_b":
                    List<SZBSecuritiesMarket> szbSecuritiesMarkets = szbSecuritiesMarketService.queryList(s.getId(), start, end);
                    for(SZBSecuritiesMarket szb : szbSecuritiesMarkets){
                        d.add(sdf.format(szb.getTradeDate()));
                        day.add(Double.valueOf(szb.getClosingPrice()));//收盘价
                        h.add(null == szb.getTurnoverRate() ? 0 : Double.valueOf(szb.getTurnoverRate()));//换手率
                        int y1 = Integer.valueOf(sdf1.format(szb.getTradeDate())).intValue();
                        if(!StringUtils.hasLength(startTime) && y == y1){
                            startTime = sdf.format(szb.getTradeDate());
                        }
                        Double week = getAvgClosingPrice(securitiesCategory.getCode(), s.getId(), szb.getTradeDate(), 5);
                        weeks.add(week);

                        Double month = getAvgClosingPrice(securitiesCategory.getCode(), s.getId(), szb.getTradeDate(), 30);
                        months.add(month);

                        Double yy = getAvgClosingPrice(securitiesCategory.getCode(), s.getId(), szb.getTradeDate(), 365);
                        years.add(yy);

                        int ye = Integer.valueOf(sdf1.format(szb.getTradeDate())).intValue();
                        if(year != ye){
                            yearAvg = getYearAvgClosingPrice(securitiesCategory.getCode(), s.getId(), szb.getTradeDate());
                            year = ye;
                        }
                        civilYear.add(yearAvg);
                    }
                    break;
            }
            map.put("id", s.getId());
            map.put("latitude", d);
            map.put("closingPrice", day);
            map.put("turnoverRate", h);
            map.put("weekAvgClosingPrice", weeks);
            map.put("monthAvgClosingPrice", months);
            map.put("yearAvgClosingPrice", years);
            map.put("civilYearAvgClosingPrice", civilYear);
            map.put("startTime", startTime);
            list.add(map);
            break;// TODO: 2021/5/19 先查询一条数据
        }
        return list;
    }


    /**
     * 获取时间区间的平均收盘价
     * @param type
     * @param securitiesId
     * @param date
     * @param days
     * @return
     * @throws Exception
     */
    public Double getAvgClosingPrice(String type, Integer securitiesId, Date date, Integer days) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - days);
        Date start = sdf.parse((sdf1.format(calendar.getTime()) + " 00:00:00"));
        Date end = sdf.parse((sdf1.format(date) + " 23:59:59"));
        Double avg = 0D;
        BigDecimal sum = new BigDecimal(0);
        switch (type){
            case "sh_a":
                List<SHASecuritiesMarket> shaSecuritiesMarkets = shaSecuritiesMarketService.queryList(securitiesId, start, end);
                for (SHASecuritiesMarket shaSecuritiesMarket : shaSecuritiesMarkets) {
                    sum = sum.add(new BigDecimal(shaSecuritiesMarket.getClosingPrice()));
                }
                avg = sum.divide(new BigDecimal(shaSecuritiesMarkets.size()), 2, RoundingMode.HALF_EVEN).doubleValue();
                break;
            case "sh_b":
                List<SHBSecuritiesMarket> shbSecuritiesMarkets = shbSecuritiesMarketService.queryList(securitiesId, start, end);
                for (SHBSecuritiesMarket shbSecuritiesMarket : shbSecuritiesMarkets) {
                    sum = sum.add(new BigDecimal(shbSecuritiesMarket.getClosingPrice()));
                }
                avg = sum.divide(new BigDecimal(shbSecuritiesMarkets.size()), 2, RoundingMode.HALF_EVEN).doubleValue();
                break;
            case "sz_a":
                List<SZASecuritiesMarket> szaSecuritiesMarkets = szaSecuritiesMarketService.queryList(securitiesId, start, end);
                for (SZASecuritiesMarket szaSecuritiesMarket : szaSecuritiesMarkets) {
                    sum = sum.add(new BigDecimal(szaSecuritiesMarket.getClosingPrice()));
                }
                avg = sum.divide(new BigDecimal(szaSecuritiesMarkets.size()), 2, RoundingMode.HALF_EVEN).doubleValue();
                break;
            case "sz_b":
                List<SZBSecuritiesMarket> szbSecuritiesMarkets = szbSecuritiesMarketService.queryList(securitiesId, start, end);
                for (SZBSecuritiesMarket szbSecuritiesMarket : szbSecuritiesMarkets) {
                    sum = sum.add(new BigDecimal(szbSecuritiesMarket.getClosingPrice()));
                }
                avg = sum.divide(new BigDecimal(szbSecuritiesMarkets.size()), 2, RoundingMode.HALF_EVEN).doubleValue();
                break;
        }
        return avg;
    }


    /**
     * 获取自然年的收盘均价
     * @param type
     * @param securitiesId
     * @param date
     * @return
     * @throws Exception
     */
    public Double getYearAvgClosingPrice(String type, Integer securitiesId, Date date) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Date start = sdf.parse((sdf1.format(calendar.getTime()) + "-01-01 00:00:00"));
        Date end = sdf.parse((sdf1.format(calendar.getTime()) + "-12-31 23:59:59"));
        Double avg = 0D;
        BigDecimal sum = new BigDecimal(0);
        switch (type){
            case "sh_a":
                List<SHASecuritiesMarket> shaSecuritiesMarkets = shaSecuritiesMarketService.queryList(securitiesId, start, end);
                for (SHASecuritiesMarket shaSecuritiesMarket : shaSecuritiesMarkets) {
                    sum = sum.add(new BigDecimal(shaSecuritiesMarket.getClosingPrice()));
                }
                avg = sum.divide(new BigDecimal(shaSecuritiesMarkets.size()), 2, RoundingMode.HALF_EVEN).doubleValue();
                break;
            case "sh_b":
                List<SHBSecuritiesMarket> shbSecuritiesMarkets = shbSecuritiesMarketService.queryList(securitiesId, start, end);
                for (SHBSecuritiesMarket shbSecuritiesMarket : shbSecuritiesMarkets) {
                    sum = sum.add(new BigDecimal(shbSecuritiesMarket.getClosingPrice()));
                }
                avg = sum.divide(new BigDecimal(shbSecuritiesMarkets.size()), 2, RoundingMode.HALF_EVEN).doubleValue();
                break;
            case "sz_a":
                List<SZASecuritiesMarket> szaSecuritiesMarkets = szaSecuritiesMarketService.queryList(securitiesId, start, end);
                for (SZASecuritiesMarket szaSecuritiesMarket : szaSecuritiesMarkets) {
                    sum = sum.add(new BigDecimal(szaSecuritiesMarket.getClosingPrice()));
                }
                avg = sum.divide(new BigDecimal(szaSecuritiesMarkets.size()), 2, RoundingMode.HALF_EVEN).doubleValue();
                break;
            case "sz_b":
                List<SZBSecuritiesMarket> szbSecuritiesMarkets = szbSecuritiesMarketService.queryList(securitiesId, start, end);
                for (SZBSecuritiesMarket szbSecuritiesMarket : szbSecuritiesMarkets) {
                    sum = sum.add(new BigDecimal(szbSecuritiesMarket.getClosingPrice()));
                }
                avg = sum.divide(new BigDecimal(szbSecuritiesMarkets.size()), 2, RoundingMode.HALF_EVEN).doubleValue();
                break;
        }
        return avg;
    }





    /**
     * 同步历史交易数据（多线程数据）
     * @return
     * @throws Exception
     */
    @Override
    public ResultUtil synchronizeHistoricalData() {
        List<Securities> securities = securitiesMapper.querySecuritiesList(null, null);
        String securitiesId = redisUtil.getValue("securitiesId");
        Set<Integer> integers = new HashSet<>(JSON.parseArray(securitiesId, Integer.class));
        List<Securities> list = new ArrayList<>();
        for(Securities securities1 : securities){
            if(!integers.contains(securities1.getId())){
                list.add(securities1);
            }
        }
        int base = 1000;//数据分隔基数
        int num = (list.size() / base) + 1;//计算需要的线程数
        System.err.println("同步历史数据：线程总数--------------" + num);
        for(int n = 0; n < num; n++){
            int start = n * base;
            int end = (n + 1) * base;//结束坐标
            if(end > list.size()){
                end = list.size();
            }
            List<Securities> securities1 = list.subList(start, end);
            int finalN = n;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Set<Integer> ids = new HashSet<>();
                        for(Securities s : securities1){
                            JSONObject jsonObject = new JSONObject();
                            List<SecuritiesMarketVo> securitiesMarketVos = queryHistoricalMatket(s);
                            jsonObject.put("market", securitiesMarketVos);
                            //处理完数据化保存到文件中
                            redisUtil.setStrValue(s.getSystemCode(), jsonObject.toJSONString());
                            fileUtil.write(s.getSystemCode() + ".json", jsonObject.toJSONString(), false);//写入
                            String value = redisUtil.getValue("securitiesId");
                            if(StringUtils.hasLength(value)){
                                Set<Integer> integers1 = new HashSet<>(JSON.parseArray(value, Integer.class));
                                ids.addAll(integers1);
                            }
                            ids.add(s.getId());
                            redisUtil.setStrValue("securitiesId", JSON.toJSONString(ids));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    System.err.println("同步历史数据：线程 " + (finalN + 1) + " 任务结束。");
                }
            }).start();
        }
        return ResultUtil.success();
    }


    /**
     * 盈亏量化数据（增量式：v = d1 + (d1 + d2) + (d1 + d2 + d3) + ...）
     * @param code
     * @param securitiesCategoryId
     * @param date
     * @param pageNo
     * @param pageSize
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> profitAndLossOfQuantitative(String code, Integer securitiesCategoryId, String date, Integer pageNo, Integer pageSize) throws Exception {
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
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
        int y = Integer.valueOf(sdf1.format(new Date())) - 4;
        for(Securities s : securities){
            Map<String, Object> map = new HashMap<>();
            map.put("name", s.getName() + "(" + s.getCode() + ")");
            SecuritiesCategory securitiesCategory = securitiesCategoryService.selectById(s.getSecuritiesCategoryId());
            List<String> d = new ArrayList<>();
            List<Double> riseFallPrice = new ArrayList<>();
            String startTime = "";
            switch (securitiesCategory.getCode()){
                case "sh_a":
                    List<SHASecuritiesMarket> shaSecuritiesMarkets = shaSecuritiesMarketService.queryList(s.getId(), start, end);
                    BigDecimal sh_a_v = new BigDecimal(0);
                    for(SHASecuritiesMarket sha : shaSecuritiesMarkets){
                        d.add(sdf.format(sha.getTradeDate()));
                        sh_a_v = sh_a_v.add(new BigDecimal(sha.getRiseFallPrice()));
                        riseFallPrice.add(sh_a_v.setScale(2, RoundingMode.HALF_EVEN).doubleValue());
                        int y1 = Integer.valueOf(sdf1.format(sha.getTradeDate())).intValue();
                        if(!StringUtils.hasLength(startTime) && y == y1){
                            startTime = sdf.format(sha.getTradeDate());
                        }
                    }
                    break;
                case "sh_b":
                    List<SHBSecuritiesMarket> shbSecuritiesMarkets = shbSecuritiesMarketService.queryList(s.getId(), start, end);
                    BigDecimal sh_b_v = new BigDecimal(0);
                    for(SHBSecuritiesMarket shb : shbSecuritiesMarkets){
                        d.add(sdf.format(shb.getTradeDate()));
                        sh_b_v = sh_b_v.add(new BigDecimal(shb.getRiseFallPrice()));
                        riseFallPrice.add(sh_b_v.setScale(2, RoundingMode.HALF_EVEN).doubleValue());
                        int y1 = Integer.valueOf(sdf1.format(shb.getTradeDate())).intValue();
                        if(!StringUtils.hasLength(startTime) && y == y1){
                            startTime = sdf.format(shb.getTradeDate());
                        }
                    }
                    break;
                case "sz_a":
                    List<SZASecuritiesMarket> szaSecuritiesMarkets = szaSecuritiesMarketService.queryList(s.getId(), start, end);
                    BigDecimal sz_a_v = new BigDecimal(0);
                    for(SZASecuritiesMarket sza : szaSecuritiesMarkets){
                        d.add(sdf.format(sza.getTradeDate()));
                        sz_a_v = sz_a_v.add(new BigDecimal(sza.getRiseFallPrice()));
                        riseFallPrice.add(sz_a_v.setScale(2, RoundingMode.HALF_EVEN).doubleValue());
                        int y1 = Integer.valueOf(sdf1.format(sza.getTradeDate())).intValue();
                        if(!StringUtils.hasLength(startTime) && y == y1){
                            startTime = sdf.format(sza.getTradeDate());
                        }

                    }
                    break;
                case "sz_b":
                    List<SZBSecuritiesMarket> szbSecuritiesMarkets = szbSecuritiesMarketService.queryList(s.getId(), start, end);
                    BigDecimal sz_b_v = new BigDecimal(0);
                    for(SZBSecuritiesMarket szb : szbSecuritiesMarkets){
                        d.add(sdf.format(szb.getTradeDate()));
                        sz_b_v = sz_b_v.add(new BigDecimal(szb.getRiseFallPrice()));
                        riseFallPrice.add(sz_b_v.setScale(2, RoundingMode.HALF_EVEN).doubleValue());
                        int y1 = Integer.valueOf(sdf1.format(szb.getTradeDate())).intValue();
                        if(!StringUtils.hasLength(startTime) && y == y1){
                            startTime = sdf.format(szb.getTradeDate());
                        }
                    }
                    break;
            }
            map.put("id", s.getId());
            map.put("latitude", d);
            map.put("riseFallPrice", riseFallPrice);
            map.put("startTime", startTime);
            list.add(map);
            break;// TODO: 2021/5/19 先查询一条数据
        }
        return list;
    }


    /**
     * 保存新数据到缓存中
     * @param fileName
     * @param o
     * @param tradeDate
     */
    public void saveMarketToFile(String fileName, Object o, Date tradeDate){
        try {
            String value = redisUtil.getValue(fileName);
            JSONObject jsonObject1 = JSON.parseObject(value);
            if(null != jsonObject1){
                JSONArray market = jsonObject1.getJSONArray("market");
                if(null == market){
                    market = new JSONArray();
                }
                boolean b = true;
                for(int j = 0; j < market.size(); j++){//判断数据是否需要插入
                    Long tradeDate1 = market.getJSONObject(j).getLong("tradeDate");
                    if(tradeDate.getTime() == tradeDate1){
                        b = false;
                        break;
                    }
                }
                if(b){//需要插入新数据到数据集合中
                    market.add(o);
                }
                jsonObject1.put("market", market);
            }else{
                jsonObject1 = new JSONObject();
                jsonObject1.put("market", JSON.toJSONString(Arrays.asList(o)));
            }
            redisUtil.setStrValue(fileName, jsonObject1.toJSONString());
            fileUtil.write(fileName + ".json", jsonObject1.toJSONString(), false);//更新到本地文件中
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 同步历史数据到缓存中
     * @param fileName
     * @param o
     * @param tradeDate
     */
    public void synchronizeHistoricalMarket (String fileName, Object o, Date tradeDate){
        try {
            String value = redisUtil.getValue(fileName);
            JSONObject jsonObject = JSON.parseObject(value);
            if(null != jsonObject){
                JSONArray market = jsonObject.getJSONArray("market");
                if(null == market){
                    market = new JSONArray();
                }
                int b = 0;
                int i = 0;
                for(int j = 0; j < market.size(); j++){//判断数据处理方式（0=插入新数据，1=更新历史数据，2=不处理）
                    Long tradeDate1 = market.getJSONObject(j).getLong("tradeDate");
                    String turnoverRate = market.getJSONObject(j).getString("turnoverRate");
                    if(!StringUtils.hasLength(turnoverRate) && tradeDate.getTime() == tradeDate1){//更新
                        i = j;
                        b = 1;
                        break;
                    }
                    if(StringUtils.hasLength(turnoverRate) && tradeDate.getTime() == tradeDate1){//不处理
                        b = 2;
                        break;
                    }
                }
                if(b == 0){//新增
                    market.add(o);
                }
                if(b == 1){//更新
                    market.remove(i);
                    market.add(o);
                }
                if(b == 2){//不处理
                    return;
                }
            }else{
                jsonObject = new JSONObject();
                jsonObject.put("market", Arrays.asList(o));
            }
            redisUtil.setStrValue(fileName, sort(jsonObject, o.getClass()));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 对数据进行排序
     * @param jsonObject
     * @param clazz
     * @return
     */
    public String sort(JSONObject jsonObject, Class clazz){
        //处理按照时间排序
        if(clazz == SHASecuritiesMarket.class){
            String market = jsonObject.getString("market");
            List<SHASecuritiesMarket> list = JSON.parseArray(market, SHASecuritiesMarket.class);
            Collections.sort(list);
            jsonObject.put("market", list);
        }
        if(clazz == SHBSecuritiesMarket.class){
            String market = jsonObject.getString("market");
            List<SHBSecuritiesMarket> list = JSON.parseArray(market, SHBSecuritiesMarket.class);
            Collections.sort(list);
            jsonObject.put("market", list);
        }
        if(clazz == SZASecuritiesMarket.class){
            String market = jsonObject.getString("market");
            List<SZASecuritiesMarket> list = JSON.parseArray(market, SZASecuritiesMarket.class);
            Collections.sort(list);
            jsonObject.put("market", list);
        }
        if(clazz == SZBSecuritiesMarket.class){
            String market = jsonObject.getString("market");
            List<SZBSecuritiesMarket> list = JSON.parseArray(market, SZBSecuritiesMarket.class);
            Collections.sort(list);
            jsonObject.put("market", list);
        }
        return jsonObject.toJSONString();
    }


    /**
     * 初始化数据到缓存中
     * @return
     * @throws Exception
     */
    @Override
    public void initMarketToCache() throws Exception {
        List<Securities> securities = securitiesMapper.querySecuritiesList(null, null);
        int base = 500;//数据分隔基数
        int num = (securities.size() / base) + 1;//计算需要的线程数
        System.err.println("初始化数据到缓存：线程总数--------------" + num);
        for(int n = 0; n < num; n++) {
            int start = n * base;
            int end = (n + 1) * base;//结束坐标
            if (end > securities.size()) {
                end = securities.size();
            }
            List<Securities> list = securities.subList(start, end);
            int finalN = n;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (Securities s : list) {
                        String read = fileUtil.read(s.getSystemCode() + ".json");//读取本地文件中的数据
                        if(StringUtils.hasLength(read)){
                            redisUtil.setStrValue(s.getSystemCode(), read);
                        }
                    }
                    System.err.println("初始化数据到缓存：线程 " + (finalN + 1) + " 任务结束。");
                }
            }).start();
        }


        Set<Integer> ids = new HashSet<>();
        for(Securities s : securities){
            String read = fileUtil.read(s.getSystemCode() + ".json");//读取本地文件中的数据
            if(StringUtils.hasLength(read)){
                ids.add(s.getId());
            }
        }
        redisUtil.setStrValue("securitiesId", JSON.toJSONString(ids));
    }

    /**
     * 检查历史数据
     */
    @Override
    public void checkHistoricalMarketData() {
        System.err.println("检查历史数据任务------------------开始。");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Securities> list = securitiesMapper.querySecuritiesList(null, null);
                    for(Securities s : list){
                        List<SecuritiesMarketVo> securitiesMarketVos = SecuritiesMarketServiceImpl.this.queryHistoricalMatket(s);
                        String read = fileUtil.read(s.getSystemCode() + ".json");
                        JSONObject jsonObject = JSON.parseObject(read);
                        if(null != jsonObject){
                            JSONArray jsonArray = jsonObject.getJSONArray("market");
                            boolean b = false;
                            for(SecuritiesMarketVo sm : securitiesMarketVos){
                                boolean n = true;
                                for(int i = 0; i < jsonArray.size(); i++){
                                    Long tradeDate = jsonArray.getJSONObject(i).getLong("tradeDate");
                                    if(tradeDate == sm.getTradeDate().getTime()){
                                        n = false;
                                        jsonArray.remove(i);//减少对比的数据量
                                        break;
                                    }
                                }
                                if(n){
                                    b = true;
                                    break;
                                }
                            }
                            if(b){//数据不完整，重新保存新数据
                                jsonObject.put("market", securitiesMarketVos);
                                redisUtil.setStrValue(s.getSystemCode(), jsonObject.toJSONString());
                                fileUtil.write(s.getSystemCode() + ".json", jsonObject.toJSONString(), false);//写入
                            }
                        }
                    }
                    SecuritiesMarketServiceImpl.this.checkHistoricalMarketData();//处理完成后重新调用自己继续新一轮的检查处理
                }catch (Exception e){
                    e.printStackTrace();
                }
                System.err.println("检查历史数据任务------------------结束。");
            }
        }).start();
    }


    /**
     * 获取历史数据
     * @param s
     * @return
     * @throws Exception
     */
    public List<SecuritiesMarketVo> queryHistoricalMatket(Securities s) throws Exception{
        List<SecuritiesMarketVo> list = new ArrayList<>();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date marketTime = s.getMarketTime();//上市时间
        Integer year = Integer.valueOf(sdf.format(marketTime));//年份
        while (true) {
            int quarter = 0;//季节
            if (year.intValue() == Integer.valueOf(sdf.format(marketTime)).intValue()) {
                quarter = DateUtil.createDate(marketTime).QUARTER;
            } else {
                quarter = 1;
            }
            boolean b = false;
            while (true) {
                String url = "http://quotes.money.163.com/trade/lsjysj_" + s.getCode() + ".html?year=" + year + "&season=" + quarter;
                HttpResult httpResult = httpClientUtil.pushHttpRequset("GET", url, null, null, "json");
                if (null == httpResult) {
                    System.err.println("数据请求异常");
                    b = true;
                    break;
                }
                if (httpResult.getCode() != 200) {
                    System.err.println(httpResult.getData());
                    b = true;
                    break;
                }
                Document document = Jsoup.parse(httpResult.getData());
                Element element = document.getElementsByClass("table_bg001").get(0);
                Elements tr = element.getElementsByTag("tr");
                if (tr.size() == 1) {//没有数据
                    b = true;
                    break;
                }
                //解析数据
                for (int i = tr.size() - 1; i >= 0; i--) {
                    Elements td = tr.get(i).getElementsByTag("td");
                    if (td.size() == 0) {
                        continue;
                    }
                    String rq = td.get(0).text();//日期
                    String kpj = !NumberUtils.isCreatable(td.get(1).text()) ? "0" : td.get(1).text();//开盘价
                    String zgj = !NumberUtils.isCreatable(td.get(2).text()) ? "0" : td.get(2).text();//最高价
                    String zdj = !NumberUtils.isCreatable(td.get(3).text()) ? "0" : td.get(3).text();//最低价
                    String spj = !NumberUtils.isCreatable(td.get(4).text()) ? "0" : td.get(4).text();//收盘价
                    String zde = !NumberUtils.isCreatable(td.get(5).text()) ? "0" : td.get(5).text();//涨跌额
                    String zdf = !NumberUtils.isCreatable(td.get(6).text()) ? "0" : td.get(6).text();//涨跌幅(%)
                    String cjl = !NumberUtils.isCreatable(td.get(7).text()) ? "0" : td.get(7).text().replaceAll(",", "");//成交量(手)
                    String cjje = !NumberUtils.isCreatable(td.get(8).text()) ? "0" : td.get(8).text().replaceAll(",", "");//成交金额(万元)
                    String zf = !NumberUtils.isCreatable(td.get(9).text()) ? "0" : td.get(9).text();//振幅(%)
                    String hsl = !NumberUtils.isCreatable(td.get(10).text()) ? "0" : td.get(10).text();//换手率(%)
                    String sqspj = new BigDecimal(spj).subtract(new BigDecimal(zde)).setScale(2, RoundingMode.HALF_EVEN).toString();//上期收盘价
                    SecuritiesMarketVo securitiesMarketVo = new SecuritiesMarketVo();
                    securitiesMarketVo.setSecuritiesId(s.getId());
                    securitiesMarketVo.setTradeDate(sdf1.parse(rq));
                    securitiesMarketVo.setLastClosingPrice(sqspj);
                    securitiesMarketVo.setClosingPrice(spj);
                    securitiesMarketVo.setRiseFallPrice(zde);
                    securitiesMarketVo.setRiseFallRatio(zdf);
                    securitiesMarketVo.setOpeningPrice(kpj);
                    securitiesMarketVo.setTopPrice(zgj);
                    securitiesMarketVo.setLowestPrice(zdj);
                    securitiesMarketVo.setAmplitude(zf);
                    securitiesMarketVo.setVolume(String.valueOf(Integer.valueOf(cjl) * 100));
                    securitiesMarketVo.setDealAmount(String.valueOf(Integer.valueOf(cjje) * 10000));
                    securitiesMarketVo.setTurnoverRate(hsl);
                    list.add(securitiesMarketVo);
                }
                quarter++;
                if(quarter == 5){
                    break;
                }
                Thread.sleep(new Random().nextInt(10) * 1000);//暂停10内随机秒，防止因频繁调用被限制IP
            }
            if(b){//没有数据可采集。
                break;
            }
            year++;
        }
        return list;
    }
}
