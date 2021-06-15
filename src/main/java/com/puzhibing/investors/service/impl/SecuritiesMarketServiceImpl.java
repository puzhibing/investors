package com.puzhibing.investors.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.puzhibing.investors.dao.*;
import com.puzhibing.investors.pojo.*;
import com.puzhibing.investors.pojo.vo.MarketMovingAverageVo;
import com.puzhibing.investors.pojo.vo.SecuritiesMarketVo;
import com.puzhibing.investors.service.*;
import com.puzhibing.investors.util.CacheUtil;
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
    private RedisUtil redisUtil;

    @Autowired
    private FileUtil fileUtil;

    private Integer pageSize = 5000;

    private int number = 0;

    private int threadNum = 0;


    /**
     * 获取并添加证券日行情数据
     * @throws Exception
     */
    @Override
    public void pullSecuritiesMarket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.err.println(sdf_.format(new Date()) + "------更新上证A股日行情数据任务开始。");
                try {
                    /**
                     * 获取【上海证券交易所A股日行情】数据
                     */
                    String urlSHA = "http://yunhq.sse.com.cn:32041//v1/sh1/list/exchange/ashare?select=code%2Cname%2Copen%2C" +
                            "high%2Clow%2Clast%2Cprev_close%2Cchg_rate%2Cvolume%2Camount%2Ctradephase%2Cchange%2Camp_rate%2C" +
                            "cpxxsubtype%2Ccpxxprodusta&begin=0&end=" + pageSize;
                    Map<String, String> header = new HashMap<>();
                    header.put("Accept", "*/*");
                    header.put("Accept-Encoding", "gzip, deflate");
                    header.put("Accept-Language", "zh-CN,zh;q=0.9");
                    header.put("Cache-Control", "no-cache");
                    header.put("Connection", "keep-alive");
                    header.put("Host", "query.sse.com.cn");
                    header.put("Pragma", "no-cache");
                    header.put("Referer", "http://www.sse.com.cn/");
                    header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36");
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
                        if(null == securities){
                            continue;
                        }
                        SecuritiesMarket securitiesMarket = new SecuritiesMarket();
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
                        //计算换手率（成交量/流通股）
                        String string = "0";
                        if(null != securities.getFlowEquity()){
                            string = new BigDecimal(cjl).divide(new BigDecimal(securities.getFlowEquity()), new MathContext(2, RoundingMode.HALF_EVEN)).toString();
                        }
                        securitiesMarket.setTurnoverRate(string);
                        saveMarketToFile(securities.getSystemCode(), securitiesMarket, securitiesMarket.getTradeDate());//保存数据到文件中
                    }
                    System.err.println(sdf_.format(new Date()) + "------更新上证A股日行情数据任务结束。");

                    calculateMovingAverage("sh_a");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.err.println(sdf_.format(new Date()) + "------更新上证B股日行情数据任务开始。");
                try {
                    /**
                     * 获取【上海证券交易所B股日行情】数据
                     */
                    String urlSHB = "http://yunhq.sse.com.cn:32041//v1/sh1/list/exchange/bshare?select=code%2Cname%2Copen%2Chigh%2C" +
                            "low%2Clast%2Cprev_close%2Cchg_rate%2Cvolume%2Camount%2Ctradephase%2Cchange%2Camp_rate%2Ccpxxsubtype%2C" +
                            "cpxxprodusta&begin=0&end=" + pageSize;
                    Map<String, String> header = new HashMap<>();
                    header.put("Accept", "*/*");
                    header.put("Accept-Encoding", "gzip, deflate");
                    header.put("Accept-Language", "zh-CN,zh;q=0.9");
                    header.put("Cache-Control", "no-cache");
                    header.put("Connection", "keep-alive");
                    header.put("Host", "query.sse.com.cn");
                    header.put("Pragma", "no-cache");
                    header.put("Referer", "http://www.sse.com.cn/");
                    header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36");
                    HttpResult httpResult = httpClientUtil.pushHttpRequset("GET", urlSHB, null, header, null);
                    if(null == httpResult){
                        System.err.println("数据请求异常");
                    }
                    if(httpResult.getCode() != 200){
                        System.err.println(httpResult.getData());
                    }
                    JSONObject jsonObject = JSON.parseObject(httpResult.getData());
                    String date = jsonObject.getString("date");
                    JSONArray list = jsonObject.getJSONArray("list");
                    SecuritiesCategory sh_b = securitiesCategoryService.queryByCode("sh_b");
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

                        Securities securities = securitiesMapper.queryByCodeAndSecuritiesCategory(code, sh_b.getId());
                        if(null == securities){
                            continue;
                        }
                        SecuritiesMarket shbSecuritiesMarket = new SecuritiesMarket();
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
                        String string = "0";
                        if(null != securities.getFlowEquity()){
                            string = new BigDecimal(cjl).divide(new BigDecimal(securities.getFlowEquity()), new MathContext(2, RoundingMode.HALF_EVEN)).toString();
                        }
                        shbSecuritiesMarket.setTurnoverRate(string);
                        saveMarketToFile(securities.getSystemCode(), shbSecuritiesMarket, shbSecuritiesMarket.getTradeDate());//保存数据到文件中
                    }
                    System.err.println(sdf_.format(new Date()) + "------更新上证B股日行情数据任务结束。");
                    calculateMovingAverage("sh_b");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.err.println(sdf_.format(new Date()) + "------更新深证A股日行情数据任务开始。");
                try {
                    /**
                     * 获取【深证证券交易所A股日行情】数据
                     */
                    SecuritiesCategory sz_a = securitiesCategoryService.queryByCode("sz_a");
                    List<Securities> securities1 = securitiesMapper.queryList(null, sz_a.getId(), null, null);
                    for(Securities securities : securities1){
                        String urlSZ = "http://www.szse.cn/api/market/ssjjhq/getTimeData?marketId=1&code=" + securities.getCode();
                        Map<String, String> header = new HashMap<>();
                        header.put("Accept", "application/json, text/javascript, */*; q=0.01");
                        header.put("Accept-Encoding", "gzip, deflate");
                        header.put("Accept-Language", "zh-CN,zh;q=0.9");
                        header.put("Cache-Control", "no-cache");
                        header.put("Connection", "keep-alive");
                        header.put("Content-Type", "application/json");
                        header.put("Host", "www.szse.cn");
                        header.put("Pragma", "no-cache");
                        header.put("Referer", "http://www.szse.cn/market/product/stock/list/index.html");
                        header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36");
                        header.put("X-Request-Type", "ajax");
                        header.put("X-Requested-With", "XMLHttpRequest");
                        HttpResult httpResult = httpClientUtil.pushHttpRequset("GET", urlSZ, null, header, null);
                        if(null == httpResult){
                            System.err.println("数据请求异常");
                            continue;
                        }
                        if(httpResult.getCode() != 200){
                            System.err.println(httpResult.getData());
                            continue;
                        }
                        JSONObject jsonObject1 = JSON.parseObject(httpResult.getData());
                        JSONObject jsonObject = jsonObject1.getJSONObject("data");
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

                        SecuritiesMarket szaSecuritiesMarket = new SecuritiesMarket();
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
                        String string = "0";
                        if(null != securities.getFlowEquity()){
                            string = new BigDecimal(cjl).divide(new BigDecimal(securities.getFlowEquity()), new MathContext(2, RoundingMode.HALF_EVEN)).toString();
                        }
                        szaSecuritiesMarket.setTurnoverRate(string);
                        saveMarketToFile(securities.getSystemCode(), szaSecuritiesMarket, szaSecuritiesMarket.getTradeDate());//保存数据到文件中

                        Thread.sleep(new Random().nextInt(10) * 1000);//暂停10内随机秒，防止因频繁调用被限制IP
                    }
                    System.err.println(sdf_.format(new Date()) + "------更新深证A股日行情数据任务结束。");
                    calculateMovingAverage("sz_a");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();


        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.err.println(sdf_.format(new Date()) + "------更新深证B股日行情数据任务开始。");
                try {
                    /**
                     * 获取【深证证券交易所B股日行情】数据
                     */
                    SecuritiesCategory sz_b = securitiesCategoryService.queryByCode("sz_b");
                    List<Securities> securities2 = securitiesMapper.queryList(null, sz_b.getId(), null, null);
                    for(Securities securities : securities2){
                        String urlSZ = "http://www.szse.cn/api/market/ssjjhq/getTimeData?marketId=1&code=" + securities.getCode();
                        Map<String, String> header = new HashMap<>();
                        header.put("Accept", "application/json, text/javascript, */*; q=0.01");
                        header.put("Accept-Encoding", "gzip, deflate");
                        header.put("Accept-Language", "zh-CN,zh;q=0.9");
                        header.put("Cache-Control", "no-cache");
                        header.put("Connection", "keep-alive");
                        header.put("Content-Type", "application/json");
                        header.put("Host", "www.szse.cn");
                        header.put("Pragma", "no-cache");
                        header.put("Referer", "http://www.szse.cn/market/product/stock/list/index.html");
                        header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36");
                        header.put("X-Request-Type", "ajax");
                        header.put("X-Requested-With", "XMLHttpRequest");
                        HttpResult httpResult = httpClientUtil.pushHttpRequset("GET", urlSZ, null, header, null);
                        if(null == httpResult){
                            System.err.println("数据请求异常");
                            continue;
                        }
                        if(httpResult.getCode() != 200){
                            System.err.println(httpResult.getData());
                            continue;
                        }
                        JSONObject jsonObject1 = JSON.parseObject(httpResult.getData());
                        JSONObject jsonObject = jsonObject1.getJSONObject("data");
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
                        SecuritiesMarket szbSecuritiesMarket = new SecuritiesMarket();
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
                        String string = "0";
                        if(null != securities.getFlowEquity()){
                            string = new BigDecimal(cjl).divide(new BigDecimal(securities.getFlowEquity()), new MathContext(2, RoundingMode.HALF_EVEN)).toString();
                        }
                        szbSecuritiesMarket.setTurnoverRate(string);
                        saveMarketToFile(securities.getSystemCode(), szbSecuritiesMarket, szbSecuritiesMarket.getTradeDate());//保存数据到文件中

                        Thread.sleep(new Random().nextInt(10) * 1000);//暂停10内随机秒，防止因频繁调用被限制IP
                    }
                    System.err.println(sdf_.format(new Date()) + "------更新深证B股日行情数据任务结束。");
                    calculateMovingAverage("sz_b");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * 计算移动平均值并进行存储
     * @param days
     * @throws Exception
     */
    public void movingAverage(List<Integer> days, List<Securities> list) {
        for(int m = 0; m < list.size(); m++){
            Securities s = list.get(m);
            try {
                String value = redisUtil.getValue(s.getSystemCode());
                JSONObject jsonObject = JSON.parseObject(value);
                List<SecuritiesMarketVo> market = jsonObject.getJSONArray("market").toJavaList(SecuritiesMarketVo.class);
                Map<String, Object> map = new HashMap<>();
                for(int i = 0; i < days.size(); i++){
                    List<MarketMovingAverageVo> agr = new ArrayList<>();
                    Integer d = days.get(i);//天数
                    for(int j = 0; j < market.size(); j++){
                        SecuritiesMarketVo object = market.get(j);
                        Double avg = getAvgClosingPrice(market, object.getTradeDate(), d);
                        MarketMovingAverageVo marketMovingAverageVo = new MarketMovingAverageVo();
                        marketMovingAverageVo.setAvg(avg.toString());
                        marketMovingAverageVo.setDay(object.getTradeDate());
                        agr.add(marketMovingAverageVo);
                    }
                    map.put("m_avg_" + d, agr);
                }
                redisUtil.setStrValue("m_avg_" + s.getSystemCode() + ".json", JSON.toJSONString(map));
                fileUtil.write("movingAverage\\" + s.getSystemCode() + ".json", JSON.toJSONString(map));
            }catch (Exception e){
                try {
                    e.printStackTrace();
                    System.out.println(s.getSystemCode() + "：" + e.getMessage());
                    System.out.println(s.getSystemCode() + "：");

                    JSONObject jsonObject = new JSONObject();
                    List<SecuritiesMarketVo> securitiesMarketVos = queryHistoricalMatket(s);
                    jsonObject.put("market", securitiesMarketVos);
                    //处理完数据化保存到文件中
                    redisUtil.setStrValue(s.getSystemCode(), jsonObject.toJSONString());
                    fileUtil.write("market\\" + s.getSystemCode() + ".json", jsonObject.toJSONString());//写入
                    m--;//重新对当前数据进行处理
                    continue;
                }catch (Exception e1){
                    e1.printStackTrace();
                }
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
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
        int y = Integer.valueOf(sdf1.format(new Date())) - 0;
        for(Securities s : securities){
            Map<String, Object> map = new HashMap<>();
            map.put("name", s.getName() + "(" + s.getCode() + ")");
            List<String> d = new ArrayList<>();//日期
            List<Double> day = new ArrayList<>();//1天收盘价
            List<Double> h = new ArrayList<>();//换手率
            List<Double> weeks = new ArrayList<>();//5天移动平均值
            List<Double> halfMonths = new ArrayList<>();//15天移动平均值
            List<Double> months = new ArrayList<>();//30天移动平均值
            List<Double> quarters = new ArrayList<>();//90天移动平均值
            List<Double> years = new ArrayList<>();//365天移动平均值
            List<Double> zf = new ArrayList<>();//振幅
            List<Double> zdl = new ArrayList<>();//涨跌率
            List<Double> cs1 = new ArrayList<>();//测试1
            List<Double> cs2 = new ArrayList<>();//测试2
            String startTime = "";
            String value = redisUtil.getValue(s.getSystemCode());
            JSONObject jsonObject = JSON.parseObject(value);
            List<SecuritiesMarket> securitiesMarkets = queryList(jsonObject, start, end);
            for(int i = 0; i < securitiesMarkets.size(); i++){
                SecuritiesMarket now = securitiesMarkets.get(i);
            }

            int number = 0;
            BigDecimal top = new BigDecimal(0);
            BigDecimal low = new BigDecimal(0);
            int ye = Integer.valueOf(sdf1.format(securitiesMarkets.get(0).getTradeDate())).intValue();
            for(SecuritiesMarket securitiesMarket : securitiesMarkets){
                d.add(sdf.format(securitiesMarket.getTradeDate()));
                day.add(Double.valueOf(securitiesMarket.getClosingPrice()));//收盘价
//                h.add(null == securitiesMarket.getTurnoverRate() ? 0 : Double.valueOf(securitiesMarket.getTurnoverRate()));//换手率
//                zf.add(null == securitiesMarket.getAmplitude() ? 0 : Double.valueOf(securitiesMarket.getAmplitude()));//振幅
//                zdl.add(null == securitiesMarket.getRiseFallRatio() ? 0 : Double.valueOf(securitiesMarket.getRiseFallRatio()));//涨跌率


                int y1 = Integer.valueOf(sdf1.format(securitiesMarket.getTradeDate())).intValue();
                if(!StringUtils.hasLength(startTime) && y == y1){
                    startTime = sdf.format(securitiesMarket.getTradeDate());
                }
//                Double week = getAvgClosingPrice(jsonObject, securitiesMarket.getTradeDate(), 5);
//                weeks.add(week);
//
//                Double halfMonth = getAvgClosingPrice(jsonObject, securitiesMarket.getTradeDate(), 15);
//                halfMonths.add(halfMonth);
//
//                Double month = getAvgClosingPrice(jsonObject, securitiesMarket.getTradeDate(), 30);
//                months.add(month);
//
//                Double quarter = getAvgClosingPrice(jsonObject, securitiesMarket.getTradeDate(), 90);
//                quarters.add(quarter);
//
//                Double yy = getAvgClosingPrice(jsonObject, securitiesMarket.getTradeDate(), 365);
//                years.add(yy);

                if(ye == y1){
                    number++;
                    top = top.add(new BigDecimal(securitiesMarket.getTopPrice()));
                    cs1.add(top.divide(new BigDecimal(number), new MathContext(2, RoundingMode.HALF_EVEN)).doubleValue());
                    low = low.add(new BigDecimal(securitiesMarket.getLowestPrice()));
                    cs2.add(low.divide(new BigDecimal(number), new MathContext(2, RoundingMode.HALF_EVEN)).doubleValue());
                }else{
                    ye = y1;
                    number = 1;
                    top = new BigDecimal(securitiesMarket.getTopPrice());
                    low = new BigDecimal(securitiesMarket.getLowestPrice());
                    cs1.add(top.doubleValue());
                    cs2.add(low.doubleValue());
                }
            }
            d.add(sdf.format(new Date(System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000))));
            map.put("id", s.getId());
            map.put("latitude", d);
            map.put("closingPrice", day);
            map.put("turnoverRate", h);
            map.put("weekAvgClosingPrice", weeks);
            map.put("halfMonthAvgClosingPrice", halfMonths);
            map.put("monthAvgClosingPrice", months);
            map.put("quarterAvgClosingPrice", quarters);
            map.put("yearAvgClosingPrice", years);
            map.put("startTime", startTime);
            map.put("zf", zf);
            map.put("zdl", zdl);
            map.put("cs1", cs1);
            map.put("cs2", cs2);
            list.add(map);
            break;// TODO: 2021/5/19 先查询一条数据
        }
        return list;
    }


    public List<SecuritiesMarket> queryList(JSONObject jsonObject, Date start, Date end) throws Exception {
        List<SecuritiesMarket> list = new ArrayList<>();
        if(null == jsonObject){
            return list;
        }
        List<SecuritiesMarket> market = jsonObject.getJSONArray("market").toJavaList(SecuritiesMarket.class);
        for(int i = 0; i < market.size(); i++){
            if(null != start && null != end){
                long time = market.get(i).getTradeDate().getTime();
                if(time >= start.getTime() && time < end.getTime()){
                    list.add(market.get(i));
                }
            }else{
                list.add(market.get(i));
            }
        }
        return list;
    }



    /**
     * 解析移动平均数据
     * @param date
     * @param day
     * @return
     * @throws Exception
     */
    public Double getAvgClosingPrice(JSONObject jsonObject, Date date, Integer day) throws Exception{
        if(null == jsonObject){
            return 0D;
        }
        JSONArray movingAverage = jsonObject.getJSONArray("movingAverage_" + day);
        for(int i = 0; i < movingAverage.size(); i++){
            JSONObject object = movingAverage.getJSONObject(i);
            Long d = object.getLong("day");
            if(d.compareTo(date.getTime()) == 0){
                return object.getDouble("avg");
            }
        }
        return 0D;
    }



    public Double getAvgClosingPrice(List<SecuritiesMarketVo> market, Date date, Integer days) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - days);
        Date start = sdf.parse((sdf1.format(calendar.getTime()) + " 00:00:00"));
        Date end = sdf.parse((sdf1.format(date) + " 23:59:59"));
        BigDecimal sum = new BigDecimal(0);
        int num = 0;
        for(SecuritiesMarketVo sm : market){
            if(start.getTime() <= sm.getTradeDate().getTime() && end.getTime() >= sm.getTradeDate().getTime()){
                sum = sum.add(new BigDecimal(null == sm.getClosingPrice() ? "0" : sm.getClosingPrice()));
                num++;
            }
        }
        Double avg = 0D;
        if(num > 0){
            avg = sum.divide(new BigDecimal(num), 2, RoundingMode.HALF_EVEN).doubleValue();
        }
        return avg;
    }


    /**
     * 获取自然年的收盘均价
     * @param jsonObject
     * @param date
     * @return
     * @throws Exception
     */
    public Double getYearAvgClosingPrice(JSONObject jsonObject, Date date) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Date start = sdf.parse((sdf1.format(calendar.getTime()) + "-01-01 00:00:00"));
        Date end = sdf.parse((sdf1.format(calendar.getTime()) + "-12-31 23:59:59"));
        BigDecimal sum = new BigDecimal(0);
        List<SecuritiesMarket> shaSecuritiesMarkets = queryList(jsonObject, start, end);
        for (SecuritiesMarket securitiesMarket : shaSecuritiesMarkets) {
            sum = sum.add(new BigDecimal(securitiesMarket.getClosingPrice()));
        }
        Double avg = sum.divide(new BigDecimal(shaSecuritiesMarkets.size()), 2, RoundingMode.HALF_EVEN).doubleValue();
        return avg;
    }





    /**
     * 同步历史交易数据（多线程数据）
     * @return
     * @throws Exception
     */
    @Override
    public ResultUtil synchronizeHistoricalData(Integer base) {
        CacheUtil.threads.clear();
        List<Securities> securities = securitiesMapper.querySecuritiesList(null, null);
        String securitiesId = redisUtil.getValue("securitiesId");
        Set<Integer> integers = new HashSet<>(JSON.parseArray(securitiesId, Integer.class));
        List<Securities> list = new ArrayList<>();
        for(Securities securities1 : securities){
            if(!integers.contains(securities1.getId())){
                list.add(securities1);
            }
        }
        int num = (list.size() / base) + 1;//计算需要的线程数
        SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(sdf_.format(new Date()) + "------同步历史数据：线程总数--------------" + num);
        for(int n = 0; n < num; n++){
            int start = n * base;
            int end = (n + 1) * base;//结束坐标
            if(end > list.size()){
                end = list.size();
            }
            List<Securities> securities1 = list.subList(start, end);
            int finalN = n;
            Thread thread = new Thread(new Runnable() {
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
                            fileUtil.write("market\\" + s.getSystemCode() + ".json", jsonObject.toJSONString());//写入
                            String value = redisUtil.getValue("securitiesId");
                            if(StringUtils.hasLength(value)){
                                Set<Integer> integers1 = new HashSet<>(JSON.parseArray(value, Integer.class));
                                ids.addAll(integers1);
                            }
                            ids.add(s.getId());
                            redisUtil.setStrValue("securitiesId", JSON.toJSONString(ids));
                            Thread.sleep(new Random().nextInt(60) * 1000);//暂停60内随机秒，防止因频繁调用被限制IP
                        }

                        //检测所有数据是否已处理完
                        CacheUtil.threads.remove("historicalMarket_" + finalN);
                        if(CacheUtil.threads.size() == 0){
                            System.err.println(sdf_.format(new Date()) + "------同步历史数据：任务全部结束。");
                            System.err.println(sdf_.format(new Date()) + "------同步历史数据：开始检查是否已全部完成。");
                            int num = securities.size() - fileUtil.findFileCount();
                            if(num != 0){//数据没有处理完
                                System.err.println(sdf_.format(new Date()) + "------同步历史数据：数据还未处理完，继续进行处理。");
                                synchronizeHistoricalData(num < 9 ? num : num / 9);
                            }

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        CacheUtil.threads.remove("historicalMarket_" + finalN);
                        if(CacheUtil.threads.size() == 0){
                            System.err.println(sdf_.format(new Date()) + "------同步历史数据：数据还未处理完，继续进行处理。");
                            int num = securities.size() - fileUtil.findFileCount();
                            synchronizeHistoricalData(num < 9 ? num : num / 9);
                        }
                    }
                    System.err.println(sdf_.format(new Date()) + "------同步历史数据：线程 " + (finalN + 1) + " 任务结束。");
                }
            });
            thread.start();
            CacheUtil.threads.put("historicalMarket_" + n, thread);
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
            List<String> d = new ArrayList<>();
            List<Double> riseFallPrice = new ArrayList<>();
            String startTime = "";
            String value = redisUtil.getValue(s.getSystemCode());
            JSONObject jsonObject = JSON.parseObject(value);
            List<SecuritiesMarket> securitiesMarkets = queryList(jsonObject, start, end);
            BigDecimal sh_a_v = new BigDecimal(0);
            for(int i = 0; i < securitiesMarkets.size(); i++){
                SecuritiesMarket securitiesMarket = securitiesMarkets.get(i);
                if(i == 0){
                    riseFallPrice.add(100D);
                }else{
                    SecuritiesMarket securitiesMarket1 = securitiesMarkets.get(i - 1);
                    if(Long.valueOf(securitiesMarket1.getVolume()) == 0){
                        riseFallPrice.add(0D);
                    }else{
                        riseFallPrice.add(new BigDecimal(Long.valueOf(securitiesMarket.getVolume()) / Long.valueOf(securitiesMarket1.getVolume())).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
                    }

                }


                d.add(sdf.format(securitiesMarket.getTradeDate()));
//                        sh_a_v = sh_a_v.add(new BigDecimal(securitiesMarket.getRiseFallPrice()));
//                        riseFallPrice.add(sh_a_v.setScale(2, RoundingMode.HALF_EVEN).doubleValue());
                int y1 = Integer.valueOf(sdf1.format(securitiesMarket.getTradeDate())).intValue();
                if(!StringUtils.hasLength(startTime) && y == y1){
                    startTime = sdf.format(securitiesMarket.getTradeDate());
                }
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
            fileUtil.write("market\\" + fileName + ".json", jsonObject1.toJSONString());//更新到本地文件中
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
        if(clazz == SecuritiesMarket.class){
            String market = jsonObject.getString("market");
            List<SecuritiesMarket> list = JSON.parseArray(market,SecuritiesMarket.class);
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
        SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(sdf_.format(new Date()) + "------初始化数据到缓存：线程总数--------------" + num);
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
                        String read = fileUtil.read("market\\" + s.getSystemCode() + ".json");//读取本地文件中的数据
                        if(StringUtils.hasLength(read)){
                            redisUtil.setStrValue(s.getSystemCode(), read);
                        }

                        read = fileUtil.read("movingAverage\\" + s.getSystemCode() + ".json");//读取本地文件中的数据
                        if(StringUtils.hasLength(read)){
                            redisUtil.setStrValue("m_avg_" + s.getSystemCode(), read);
                        }
                    }
                    System.err.println(sdf_.format(new Date()) + "------初始化数据到缓存：线程 " + (finalN + 1) + " 任务结束。");
                }
            }).start();
        }


        Set<Integer> ids = new HashSet<>();
        for(Securities s : securities){
            String read = fileUtil.read("market\\" + s.getSystemCode() + ".json");//读取本地文件中的数据
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
    public void checkHistoricalMarketData(List<Securities> list) {
        number = 0;
        threadNum = 10;
        if(null == list){
            list = securitiesMapper.querySecuritiesList(null, null);
        }
        SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(sdf_.format(new Date()) + "------检查历史数据任务开始。");
        int num = list.size() / 9;
        for(int n = 0; n < 10; n++){
            int start = n * num;
            int end = (n + 1) * num;
            if(n == 9){
                end = list.size();
            }
            List<Securities> securities = list.subList(start, end);
            List<Securities> finalList = list;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int index = 0;
                    try {
                        for(int i = 0; i < securities.size(); i++){
                            index = i;
                            Securities s = securities.get(i);
                            List<SecuritiesMarketVo> securitiesMarketVos = SecuritiesMarketServiceImpl.this.queryHistoricalMatket(s);
                            String read = fileUtil.read("market\\" + s.getSystemCode() + ".json");
                            JSONObject jsonObject = JSON.parseObject(read);
                            if(null != jsonObject){
                                JSONArray jsonArray = jsonObject.getJSONArray("market");
                                boolean b = false;
                                for(SecuritiesMarketVo sm : securitiesMarketVos){
                                    boolean n = true;
                                    for(int j = 0; j < jsonArray.size(); j++){
                                        Long tradeDate = jsonArray.getJSONObject(j).getLong("tradeDate");
                                        if(tradeDate == sm.getTradeDate().getTime()){
                                            n = false;
                                            jsonArray.remove(j);//减少对比的数据量
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
                                    fileUtil.write("market\\" + s.getSystemCode() + ".json", jsonObject.toJSONString());//写入
                                }
                            }
                            number++;
                            Thread.sleep(30 * 1000);
                        }
                        threadNum--;
                        if(number == finalList.size() && threadNum == 0){//所有线程处理完成后
                            System.err.println(sdf_.format(new Date()) + "------检查历史数据任务结束，等待12小时后新一轮继续执行。");
                            Thread.sleep(12 * 60 * 60 * 1000);//暂停12小时后重新新一轮执行
                            SecuritiesMarketServiceImpl.this.checkHistoricalMarketData(null);//处理完成后重新调用自己继续新一轮的检查处理
                        }
                        if(number != finalList.size() && threadNum == 0){//所有线程处理完成，但是任务没有处理完
                            SecuritiesMarketServiceImpl.this.checkHistoricalMarketData(CacheUtil.securities);
                        }
                    }catch (Exception e){
                        CacheUtil.securities.addAll(securities.subList(index, securities.size()));
                    }
                }
            }).start();
        }

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
        int year = Integer.valueOf(sdf.format(marketTime)).intValue();//年份
        DateUtil date = DateUtil.createDate(new Date());
        while (true) {
            int quarter = 0;//季节
            if (year == Integer.valueOf(sdf.format(marketTime)).intValue()) {
                quarter = DateUtil.createDate(marketTime).QUARTER;
            } else {
                quarter = 1;
            }
            boolean b = false;
            while (true) {
                String url = "http://quotes.money.163.com/trade/lsjysj_" + s.getCode() + ".html?year=" + year + "&season=" + quarter;
                Map<String, String> header = new HashMap<>();
                header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                header.put("Accept-Encoding", "gzip, deflate");
                header.put("Accept-Language", "zh-CN,zh;q=0.9");
                header.put("Cache-Control", "no-cache");
                header.put("Connection", "keep-alive");
                header.put("Host", "quotes.money.163.com");
                header.put("Pragma", "no-cache");
                header.put("Referer", "http://quotes.money.163.com/trade/lsjysj_" + s.getCode() + ".html");
                header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36");
                header.put("Upgrade-Insecure-Requests", "1");
                HttpResult httpResult = httpClientUtil.pushHttpRequset("GET", url, null, header, "json");
                if (null == httpResult) {
                    throw new Exception("数据请求异常");
                }
                if (httpResult.getCode() != 200) {
                    throw new Exception(httpResult.getData());
                }
                Document document = Jsoup.parse(httpResult.getData());
                Element element = document.getElementsByClass("table_bg001").get(0);
                Elements tr = element.getElementsByTag("tr");
                if (tr.size() == 1 && year >= date.YEAR && quarter >= date.QUARTER) {//查询完毕
                    b = true;
                    break;
                }
                if (tr.size() == 1 && year <= date.YEAR && quarter <= date.QUARTER) {//数据缺失
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
                    String cjl = td.get(7).text().indexOf("-") != -1 ? "0" : td.get(7).text().replaceAll(",", "");//成交量(手)
                    String cjje = td.get(8).text().indexOf("-") != -1 ? "0" : td.get(8).text().replaceAll(",", "");//成交金额(万元)
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
            }
            if(b){//没有数据可采集。
                break;
            }
            year++;
        }
        return list;
    }



    @Override
    public void calculateMovingAverage(String securitiesCategoryCode) {
        List<Securities> list = securitiesMapper.querySecuritiesList(null, securitiesCategoryCode);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(sdf.format(new Date()) + "------计算移动平均数据开始");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SecuritiesMarketServiceImpl.this.movingAverage(Arrays.asList(0, 5, 15, 30, 90, 365), list);
                    System.err.println(sdf.format(new Date()) + "------计算移动平均数据结束");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
