package com.puzhibing.investors.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.puzhibing.investors.dao.*;
import com.puzhibing.investors.pojo.*;
import com.puzhibing.investors.pojo.vo.MarketMovingAverageVo;
import com.puzhibing.investors.pojo.vo.SecuritiesMarketVo;
import com.puzhibing.investors.service.*;
import com.puzhibing.investors.util.*;
import com.puzhibing.investors.util.http.HttpClientUtil;
import com.puzhibing.investors.util.http.HttpResult;
import com.puzhibing.investors.util.redis.RedisUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.reflect.Array;
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
    private IAveragePriceService averagePriceService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private ExcelUtil excelUtil;

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
                try {
                    sendSHA();
                }catch (Exception e){
                    e.printStackTrace();
                    try {
                        Thread.sleep(10 * 60 * 1000);
                        sendSHA();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendSHB();
                }catch (Exception e){
                    e.printStackTrace();
                    try {
                        Thread.sleep(10 * 60 * 1000);
                        sendSHB();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendSZA();
                }catch (Exception e){
                    e.printStackTrace();
                    try {
                        Thread.sleep(10 * 60 * 1000);
                        sendSZA();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }).start();


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendSZB();
                }catch (Exception e){
                    e.printStackTrace();
                    try {
                        Thread.sleep(10 * 60 * 1000);
                        sendSZB();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }).start();
    }


    public void sendSHA() throws Exception{
        SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(sdf_.format(new Date()) + "------更新上证A股日行情数据任务开始。");
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
            if(null != securities.getFlowEquity() && securities.getFlowEquity() != 0){
                string = new BigDecimal(cjl).divide(new BigDecimal(securities.getFlowEquity()), new MathContext(2, RoundingMode.HALF_EVEN))
                        .multiply(new BigDecimal(100)).toString();
            }
            securitiesMarket.setTurnoverRate(string);
            saveMarketToFile(securities.getSystemCode(), securitiesMarket, securitiesMarket.getTradeDate());//保存数据到文件中
        }
        System.err.println(sdf_.format(new Date()) + "------更新上证A股日行情数据任务结束。");
        weekMovingAverage("sh_a");
        monthMovingAverage("sh_a");
        quarterMovingAverage("sh_a");
        yearMovingAverage("sh_a");
        calculateMovingAverage("sh_a");
        potentialEnergyMovingAverage("sh_a");
    }


    public void sendSHB() throws Exception{
        SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(sdf_.format(new Date()) + "------更新上证B股日行情数据任务开始。");
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
            if(null != securities.getFlowEquity() && securities.getFlowEquity() != 0){
                string = new BigDecimal(cjl).divide(new BigDecimal(securities.getFlowEquity()), new MathContext(2, RoundingMode.HALF_EVEN))
                        .multiply(new BigDecimal(100)).toString();
            }
            shbSecuritiesMarket.setTurnoverRate(string);
            saveMarketToFile(securities.getSystemCode(), shbSecuritiesMarket, shbSecuritiesMarket.getTradeDate());//保存数据到文件中
        }
        System.err.println(sdf_.format(new Date()) + "------更新上证B股日行情数据任务结束。");
        weekMovingAverage("sh_b");
        monthMovingAverage("sh_b");
        quarterMovingAverage("sh_b");
        yearMovingAverage("sh_b");
        calculateMovingAverage("sh_b");
        potentialEnergyMovingAverage("sh_b");
    }



    public void sendSZA() throws Exception{
        SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(sdf_.format(new Date()) + "------更新深证A股日行情数据任务开始。");
        /**
         * 获取【深证证券交易所A股日行情】数据
         */
        SecuritiesCategory sz_a = securitiesCategoryService.queryByCode("sz_a");
        List<Securities> securities1 = securitiesMapper.queryList(null, sz_a.getId(), null, null);
        for(Securities securities : securities1){
            double random = Math.random();
            String urlSZ = "http://www.szse.cn/api/market/ssjjhq/getTimeData?random=" + random + "&marketId=1&code=" + securities.getCode();
            Map<String, String> header = new HashMap<>();
            header.put("Accept", "application/json, text/javascript, */*; q=0.01");
            header.put("Accept-Encoding", "gzip, deflate");
            header.put("Accept-Language", "zh-CN,zh;q=0.9");
            header.put("Cache-Control", "no-cache");
            header.put("Connection", "keep-alive");
            header.put("Content-Type", "application/json");
            header.put("Host", "www.szse.cn");
            header.put("Pragma", "no-cache");
            header.put("Referer", "http://www.szse.cn/market/trend/index.html");
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
            if(null != securities.getFlowEquity() && securities.getFlowEquity() != 0){
                string = new BigDecimal(cjl).divide(new BigDecimal(securities.getFlowEquity()), new MathContext(2, RoundingMode.HALF_EVEN))
                        .multiply(new BigDecimal(100)).toString();
            }
            szaSecuritiesMarket.setTurnoverRate(string);
            saveMarketToFile(securities.getSystemCode(), szaSecuritiesMarket, szaSecuritiesMarket.getTradeDate());//保存数据到文件中

            Thread.sleep(new Random().nextInt(10) * 1000);//暂停10内随机秒，防止因频繁调用被限制IP
        }
        System.err.println(sdf_.format(new Date()) + "------更新深证A股日行情数据任务结束。");
        weekMovingAverage("sz_a");
        monthMovingAverage("sz_a");
        quarterMovingAverage("sz_a");
        yearMovingAverage("sz_a");
        calculateMovingAverage("sz_a");
        potentialEnergyMovingAverage("sz_a");
    }



    public void sendSZB() throws Exception{
        SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(sdf_.format(new Date()) + "------更新深证B股日行情数据任务开始。");
        /**
         * 获取【深证证券交易所B股日行情】数据
         */
        SecuritiesCategory sz_b = securitiesCategoryService.queryByCode("sz_b");
        List<Securities> securities2 = securitiesMapper.queryList(null, sz_b.getId(), null, null);
        for(Securities securities : securities2){
            double random = Math.random();
            String urlSZ = "http://www.szse.cn/api/market/ssjjhq/getTimeData?random=" + random + "&marketId=1&code=" + securities.getCode();
            Map<String, String> header = new HashMap<>();
            header.put("Accept", "application/json, text/javascript, */*; q=0.01");
            header.put("Accept-Encoding", "gzip, deflate");
            header.put("Accept-Language", "zh-CN,zh;q=0.9");
            header.put("Cache-Control", "no-cache");
            header.put("Connection", "keep-alive");
            header.put("Content-Type", "application/json");
            header.put("Host", "www.szse.cn");
            header.put("Pragma", "no-cache");
            header.put("Referer", "http://www.szse.cn/market/trend/index.html");
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
            if(null != securities.getFlowEquity() && securities.getFlowEquity() != 0){
                string = new BigDecimal(cjl).divide(new BigDecimal(securities.getFlowEquity()), new MathContext(2, RoundingMode.HALF_EVEN))
                        .multiply(new BigDecimal(100)).toString();
            }
            szbSecuritiesMarket.setTurnoverRate(string);
            saveMarketToFile(securities.getSystemCode(), szbSecuritiesMarket, szbSecuritiesMarket.getTradeDate());//保存数据到文件中

            Thread.sleep(new Random().nextInt(10) * 1000);//暂停10内随机秒，防止因频繁调用被限制IP
        }
        System.err.println(sdf_.format(new Date()) + "------更新深证B股日行情数据任务结束。");
        weekMovingAverage("sz_b");
        monthMovingAverage("sz_b");
        quarterMovingAverage("sz_b");
        yearMovingAverage("sz_b");
        calculateMovingAverage("sz_b");
        potentialEnergyMovingAverage("sz_b");
    }



    /**
     * 计算移动平均成交值并进行存储
     * @param days
     * @throws Exception
     */
    public void movingAverage(List<Integer> days, List<Securities> list) {
        for(int m = 0; m < list.size(); m++){
            Securities s = list.get(m);
            try {
                String value = fileUtil.read("market\\" + s.getSystemCode() + ".json");
                if(!StringUtils.hasLength(value)){
                    continue;
                }
                JSONObject jsonObject = JSON.parseObject(value);
                List<SecuritiesMarketVo> market = jsonObject.getJSONArray("market").toJavaList(SecuritiesMarketVo.class);
                Map<String, Object> map = new HashMap<>();
                for(int i = 0; i < days.size(); i++){
                    List<String> agr = new ArrayList<>();
                    Integer d = days.get(i);//天数
                    for(int j = 0; j < market.size(); j++){
                        SecuritiesMarketVo object = market.get(j);
                        Double avg = getAvgClosingPrice(market, object.getTradeDate(), d);
                        agr.add(avg.toString());
                    }
                    map.put("m_avg_" + d, agr);
                }

                List<String> date = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                for(int j = 0; j < market.size(); j++){
                    SecuritiesMarketVo object = market.get(j);
                    date.add(sdf.format(object.getTradeDate()));
                }
                map.put("date", date);

                fileUtil.write("movingAverage\\" + s.getSystemCode() + ".json", JSON.toJSONString(map));
            }catch (Exception e){
                try {
                    e.printStackTrace();
                    System.out.println(s.getSystemCode() + "：" + e.getMessage());
                    System.out.println(s.getSystemCode() + "：");

                    JSONObject jsonObject = new JSONObject();
                    List<SecuritiesMarketVo> securitiesMarketVos = queryHistoricalMatket(s);
                    jsonObject.put("market", securitiesMarketVos);
                    //处理完数据后保存到文件中
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
     * 计算移动平均势能值并进行存储
     * @param days
     * @throws Exception
     */
    public void potentialEnergy(List<Integer> days, List<Securities> list) {
        for(int m = 0; m < list.size(); m++){
            Securities s = list.get(m);
            try {
                String value = fileUtil.read("market\\" + s.getSystemCode() + ".json");
                if(!StringUtils.hasLength(value)){
                    continue;
                }
                JSONObject jsonObject = JSON.parseObject(value);
                List<SecuritiesMarketVo> market = jsonObject.getJSONArray("market").toJavaList(SecuritiesMarketVo.class);
                Map<String, Object> map = new HashMap<>();
                Map<String, Object> map1 = new HashMap<>();
                for(int i = 0; i < days.size(); i++){
                    List<String> agr = new ArrayList<>();
                    Integer d = days.get(i);//天数
                    for(int j = 0; j < market.size(); j++){
                        SecuritiesMarketVo object = market.get(j);
                        Double avg = 0D;
                        if(j > 0){
                            avg = getAvgPotentialEnergy(market, object.getTradeDate(), d);
                        }
                        agr.add(avg.toString());
                    }
                    map1.put("p_avg_" + d, agr);
                }
                map.put("last", map1);

                Map<String, Object> map2 = new HashMap<>();
                for(int i = 0; i < days.size(); i++){
                    List<String> agr = new ArrayList<>();
                    Integer d = days.get(i);//天数
                    for(int j = 0; j < market.size(); j++){
                        SecuritiesMarketVo object = market.get(j);
                        Double avg = 0D;
                        if(j > 0){
                            avg = getAvgPotentialEnergy_(market, object.getTradeDate(), d);
                        }
                        agr.add(avg.toString());
                    }
                    map2.put("p_avg_" + d, agr);
                }
                map.put("today", map2);

                List<String> date = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                for(int j = 0; j < market.size(); j++){
                    SecuritiesMarketVo object = market.get(j);
                    date.add(sdf.format(object.getTradeDate()));
                }
                map.put("date", date);

                fileUtil.write("potentialEnergy\\" + s.getSystemCode() + ".json", JSON.toJSONString(map));
            }catch (Exception e){
                try {
                    e.printStackTrace();
                    System.out.println(s.getSystemCode() + "：" + e.getMessage());
                    System.out.println(s.getSystemCode() + "：");

                    JSONObject jsonObject = new JSONObject();
                    List<SecuritiesMarketVo> securitiesMarketVos = queryHistoricalMatket(s);
                    jsonObject.put("market", securitiesMarketVos);
                    //处理完数据化保存到文件中
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
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> queryAllData(String code, Integer pageNo, Integer pageSize) throws Exception {
        pageNo = (pageNo - 1) * pageSize;
        List<Securities> securities = securitiesMapper.queryList(code, null, pageNo, pageSize);
        List<Map<String, Object>> list = new ArrayList<>();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
        String y = sdf1.format(new Date());
        for(Securities s : securities){
            Map<String, Object> map = new HashMap<>();
            map.put("name", s.getName() + "(" + s.getCode() + ")");
            String startTime = "";
            String value = fileUtil.read("movingAverage\\" + s.getSystemCode() + ".json");
            JSONObject jsonObject = JSON.parseObject(value);
            JSONArray date = jsonObject.getJSONArray("date");
            for (int i = date.size() - 1; i >= 0; i--){
                String string = date.getString(i);
                if(!y.equals(string.substring(0, string.indexOf("-")))){
                    startTime = date.getString(i + 1);
                    break;
                }
            }

            map.put("id", s.getId());
            map.put("systemCode", s.getSystemCode());
            map.put("latitude", jsonObject.getJSONArray("date"));
            map.put("closingPrice", jsonObject.getJSONArray("m_avg_1"));
            map.put("weekAvgClosingPrice", jsonObject.getJSONArray("m_avg_5"));
            map.put("halfMonthAvgClosingPrice", jsonObject.getJSONArray("m_avg_15"));
            map.put("monthAvgClosingPrice", jsonObject.getJSONArray("m_avg_30"));
            map.put("quarterAvgClosingPrice", jsonObject.getJSONArray("m_avg_90"));
            map.put("yearAvgClosingPrice", jsonObject.getJSONArray("m_avg_365"));
            map.put("startTime", startTime);
            list.add(map);
        }
        return list;
    }

    @Override
    public Map<String, Object> queryMarkt(Integer type, String code) throws Exception {
        List<Securities> securities = securitiesMapper.queryList(code, null, null, null);
        Map<String, Object> map = new HashMap<>();
        if(securities.size() == 0){
            return map;
        }
        Securities s = securities.get(0);
        if(null != s){
            map.put("name", s.getName() + "(" + s.getCode() + ")");
            String value = fileUtil.read("movingAverage\\" + s.getSystemCode() + ".json");
            JSONObject jsonObject = JSON.parseObject(value);
            if(type == 1){//日
                JSONArray date = jsonObject.getJSONArray("date");
                String startTime = date.get(date.size() > 90 ? date.size() - 90 : 0).toString();
                map.put("id", s.getId());
                map.put("systemCode", s.getSystemCode());
                map.put("latitude", jsonObject.getJSONArray("date"));
                map.put("closingPrice", jsonObject.getJSONArray("m_avg_1"));
                map.put("weekAvgClosingPrice", jsonObject.getJSONArray("m_avg_5"));
                map.put("halfMonthAvgClosingPrice", jsonObject.getJSONArray("m_avg_10"));
                map.put("monthAvgClosingPrice", jsonObject.getJSONArray("m_avg_20"));
                map.put("quarterAvgClosingPrice", jsonObject.getJSONArray("m_avg_60"));
                map.put("yearAvgClosingPrice", jsonObject.getJSONArray("m_avg_240"));
                map.put("startTime", startTime);
            }
            if(type == 2){//周
                String value1 = fileUtil.read("weekMovingAverage\\" + s.getSystemCode() + ".json");
                JSONObject jsonObject1 = JSON.parseObject(value1);
                JSONArray date = jsonObject1.getJSONArray("date");
                String startTime = date.get(date.size() > 50 ? date.size() - 50 : 0).toString();
                map.put("id", s.getId());
                map.put("systemCode", s.getSystemCode());
                map.put("latitude", jsonObject1.getJSONArray("date"));
                map.put("closingPrice", jsonObject1.getJSONArray("m_avg_5"));
                map.put("halfMonthAvgClosingPrice", jsonObject1.getJSONArray("m_avg_10"));
                map.put("monthAvgClosingPrice", jsonObject1.getJSONArray("m_avg_20"));
                map.put("quarterAvgClosingPrice", jsonObject1.getJSONArray("m_avg_60"));
                map.put("yearAvgClosingPrice", jsonObject1.getJSONArray("m_avg_240"));
                map.put("startTime", startTime);
            }
            if(type == 3){//月
                String value1 = fileUtil.read("monthMovingAverage\\" + s.getSystemCode() + ".json");
                JSONObject jsonObject1 = JSON.parseObject(value1);
                JSONArray date = jsonObject1.getJSONArray("date");
                String startTime = date.get(date.size() > 24 ? date.size() - 24 : 0).toString();
                map.put("id", s.getId());
                map.put("systemCode", s.getSystemCode());
                map.put("latitude", jsonObject1.getJSONArray("date"));
                map.put("closingPrice", jsonObject1.getJSONArray("m_avg_20"));
                map.put("quarterAvgClosingPrice", jsonObject1.getJSONArray("m_avg_60"));
                map.put("yearAvgClosingPrice", jsonObject1.getJSONArray("m_avg_240"));
                map.put("startTime", startTime);
            }
            if(type == 4){//季
                String value1 = fileUtil.read("quarterMovingAverage\\" + s.getSystemCode() + ".json");
                JSONObject jsonObject1 = JSON.parseObject(value1);
                JSONArray date = jsonObject1.getJSONArray("date");
                String startTime = date.get(date.size() > 12 ? date.size() - 12 : 0).toString();
                map.put("id", s.getId());
                map.put("systemCode", s.getSystemCode());
                map.put("latitude", jsonObject1.getJSONArray("date"));
                map.put("closingPrice", jsonObject1.getJSONArray("m_avg_60"));
                map.put("yearAvgClosingPrice", jsonObject1.getJSONArray("m_avg_240"));
                map.put("startTime", startTime);
            }
            if(type == 5){//年
                String value1 = fileUtil.read("yearMovingAverage\\" + s.getSystemCode() + ".json");
                JSONObject jsonObject1 = JSON.parseObject(value1);
                JSONArray date = jsonObject1.getJSONArray("date");
                String startTime = date.get(0).toString();
                map.put("id", s.getId());
                map.put("systemCode", s.getSystemCode());
                map.put("latitude", jsonObject1.getJSONArray("date"));
                map.put("closingPrice", jsonObject1.getJSONArray("m_avg_240"));
                map.put("startTime", startTime);
            }
        }
        return map;
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
        JSONArray movingAverage = jsonObject.getJSONArray("m_avg_" + day);
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
        Date start = sdf.parse((sdf1.format(date) + " 00:00:00"));
        Date end = sdf.parse((sdf1.format(date) + " 23:59:59"));
        BigDecimal sum = new BigDecimal(0);
        Double avg = 0D;
        int index = days;
        for(int i = 0; i < market.size(); i++){
            SecuritiesMarketVo sm = market.get(i);
            if(start.getTime() <= sm.getTradeDate().getTime() && end.getTime() >= sm.getTradeDate().getTime()){
                if(i < days){
                    index = i + 1;
                }
                if(i != 0){
                    sum = sum.add(new BigDecimal(null == sm.getClosingPrice() ? "0" : sm.getClosingPrice()));
                    for(int j = 1; j < index; j++){
                        sm = market.get(i - j);
                        sum = sum.add(new BigDecimal(null == sm.getClosingPrice() ? "0" : sm.getClosingPrice()));
                    }
                    avg = sum.divide(new BigDecimal(index), 2, RoundingMode.HALF_EVEN).doubleValue();
                }
                break;
            }
        }
        return avg;





//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - days);
//        Date start = sdf.parse((sdf1.format(calendar.getTime()) + " 00:00:00"));
//        Date end = sdf.parse((sdf1.format(date) + " 23:59:59"));
//        BigDecimal sum = new BigDecimal(0);
//        int num = 0;
//        for(SecuritiesMarketVo sm : market){
//            if(start.getTime() <= sm.getTradeDate().getTime() && end.getTime() >= sm.getTradeDate().getTime()){
//                sum = sum.add(new BigDecimal(null == sm.getClosingPrice() ? "0" : sm.getClosingPrice()));
//                num++;
//            }
//        }
//        Double avg = 0D;
//        if(num > 0){
//            avg = sum.divide(new BigDecimal(num), 2, RoundingMode.HALF_EVEN).doubleValue();
//        }
//        return avg;
    }


    public Double getAvgPotentialEnergy(List<SecuritiesMarketVo> market, Date date, Integer days) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - days);
        Date start = sdf.parse((sdf1.format(calendar.getTime()) + " 00:00:00"));
        Date end = sdf.parse((sdf1.format(date) + " 23:59:59"));
        BigDecimal last = new BigDecimal(0);//上期收盘价总和
        BigDecimal vs = new BigDecimal(0);//差价总和
        int num = 0;//天数
        for(SecuritiesMarketVo sm : market){
            if(start.getTime() <= sm.getTradeDate().getTime() && end.getTime() >= sm.getTradeDate().getTime()){
                BigDecimal lastClosingPrice = new BigDecimal(null == sm.getLastClosingPrice() ? "0" : sm.getLastClosingPrice());//上期收盘价
                BigDecimal topPrice = new BigDecimal(null == sm.getTopPrice() ? "0" : sm.getTopPrice());//本日最高
                BigDecimal lowestPrice = new BigDecimal(null == sm.getLowestPrice() ? "0" : sm.getLowestPrice());//本日最低
                BigDecimal multiply = null;
                if(topPrice.subtract(lowestPrice).doubleValue() == 0){
                    multiply = new BigDecimal(null == sm.getRiseFallPrice() ? "0" : sm.getRiseFallPrice());
                }else{
                    multiply = topPrice.subtract(lastClosingPrice).subtract(lastClosingPrice.subtract(lowestPrice));//v = (to - la) - (la - lo)
                }
                last = last.add(lastClosingPrice);
                vs = vs.add(multiply);
                num++;
            }
        }
        Double avg = 0D;
        if(num > 0){
            BigDecimal nu = new BigDecimal(num);
            BigDecimal divide = vs.divide(nu, new MathContext(4, RoundingMode.HALF_EVEN));
            BigDecimal divide1 = last.divide(nu, new MathContext(4, RoundingMode.HALF_EVEN));
            if(divide.doubleValue() != 0 && divide1.doubleValue() != 0){
                avg = divide.divide(divide1, new MathContext(4, RoundingMode.HALF_EVEN)).multiply(new BigDecimal(100)).doubleValue();
            }
        }
        return avg;
    }

    public Double getAvgPotentialEnergy_(List<SecuritiesMarketVo> market, Date date, Integer days) throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - days);
        Date start = sdf.parse((sdf1.format(calendar.getTime()) + " 00:00:00"));
        Date end = sdf.parse((sdf1.format(date) + " 23:59:59"));
        BigDecimal today = new BigDecimal(0);//本期收盘价总和
        BigDecimal vs = new BigDecimal(0);//差价总和
        int num = 0;//天数
        for(SecuritiesMarketVo sm : market){
            if(start.getTime() <= sm.getTradeDate().getTime() && end.getTime() >= sm.getTradeDate().getTime()){
                BigDecimal closingPrice = new BigDecimal(null == sm.getClosingPrice() ? "0" : sm.getClosingPrice());//本期收盘价
                BigDecimal topPrice = new BigDecimal(null == sm.getTopPrice() ? "0" : sm.getTopPrice());//本日最高
                BigDecimal lowestPrice = new BigDecimal(null == sm.getLowestPrice() ? "0" : sm.getLowestPrice());//本日最低
                BigDecimal multiply = null;
                if(topPrice.subtract(lowestPrice).doubleValue() == 0){
                    multiply = new BigDecimal(null == sm.getRiseFallPrice() ? "0" : sm.getRiseFallPrice());
                }else{
                    multiply = closingPrice.subtract(topPrice).subtract(lowestPrice.subtract(closingPrice));
                }
                today = today.add(closingPrice);
                vs = vs.add(multiply);
                num++;
            }
        }
        Double avg = 0D;
        if(num > 0){
            BigDecimal nu = new BigDecimal(num);
            BigDecimal divide = vs.divide(nu, new MathContext(4, RoundingMode.HALF_EVEN));
            BigDecimal divide1 = today.divide(nu, new MathContext(4, RoundingMode.HALF_EVEN));
            if(divide.doubleValue() != 0 && divide1.doubleValue() != 0){
                avg = divide.divide(divide1, new MathContext(4, RoundingMode.HALF_EVEN)).multiply(new BigDecimal(100)).doubleValue();
            }
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
                            fileUtil.write("market\\" + s.getSystemCode() + ".json", jsonObject.toJSONString());//写入
                            String value = redisUtil.getValue("securitiesId");
                            if(StringUtils.hasLength(value)){
                                Set<Integer> integers1 = new HashSet<>(JSON.parseArray(value, Integer.class));
                                ids.addAll(integers1);
                            }
                            ids.add(s.getId());
                            redisUtil.setStrValue("securitiesId", JSON.toJSONString(ids));
//                            Thread.sleep(new Random().nextInt(10) * 1000);//暂停10内随机秒，防止因频繁调用被限制IP
                        }

                        //检测所有数据是否已处理完
                        CacheUtil.threads.remove("historicalMarket_" + finalN);
                        if(CacheUtil.threads.size() == 0){
                            System.err.println(sdf_.format(new Date()) + "------同步历史数据：任务全部结束。");
                            System.err.println(sdf_.format(new Date()) + "------同步历史数据：开始检查是否已全部完成。");
                            int num = securities.size() - fileUtil.findFileCount("market\\");
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
                            int num = securities.size() - fileUtil.findFileCount("market\\");
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
     * 获取移动平均势能数据
     * @param code
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> queryPotentialEnergy(String code) throws Exception {
        List<Securities> securities = securitiesMapper.queryList(code, null, null, null);
        List<Map<String, Object>> list = new ArrayList<>();
        if(securities.size() == 0){
            return list;
        }
        Securities s = securities.get(0);
        if(null != s){
            int l = 1;
            for(int i = 0; i < 10; i++){
                if(i == 0){
                    l = 0;
                }
                if(l == 1){
                    l++;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("name", "");
                String value = fileUtil.read("potentialEnergy\\" + s.getSystemCode() + ".json");
                JSONObject jsonObject = JSON.parseObject(value);
                JSONArray date = jsonObject.getJSONArray("date");
                String startTime = date.get(date.size() - 90).toString();
                map.put("id", l);
                map.put("latitude", jsonObject.getJSONArray("date"));
                JSONObject last = jsonObject.getJSONObject("last");
                map.put("value", last.getJSONArray("p_avg_" + l));
                map.put("startTime", startTime);
                list.add(map);
                l++;
            }
        }
        return list;
    }



    /**
     * 获取移动平均势能数据
     * @param code
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> queryPotentialEnergy_(String code) throws Exception {
        List<Securities> securities = securitiesMapper.queryList(code, null, null, null);
        List<Map<String, Object>> list = new ArrayList<>();
        if(securities.size() == 0){
            return list;
        }
        Securities s = securities.get(0);
        if(null != s){
            int l = 1;
            for(int i = 0; i < 10; i++){
                if(i == 0){
                    l = 0;
                }
                if(l == 1){
                    l++;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("name", "");
                String value = fileUtil.read("potentialEnergy\\" + s.getSystemCode() + ".json");
                JSONObject jsonObject = JSON.parseObject(value);
                JSONArray date = jsonObject.getJSONArray("date");
                String startTime = date.get(date.size() - 90).toString();
                map.put("id", l);
                map.put("latitude", jsonObject.getJSONArray("date"));
                JSONObject last = jsonObject.getJSONObject("today");
                map.put("value", last.getJSONArray("p_avg_" + l));
                map.put("startTime", startTime);
                list.add(map);
                l++;
            }
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
            String value = fileUtil.read("market\\" + fileName + ".json");
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
                jsonObject1.put("market", Arrays.asList(o));
            }
            fileUtil.write("market\\" + fileName + ".json", jsonObject1.toJSONString());//更新到本地文件中
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
//        int base = 500;//数据分隔基数
//        int num = (securities.size() / base) + 1;//计算需要的线程数
//        SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        System.err.println(sdf_.format(new Date()) + "------初始化数据到缓存：线程总数--------------" + num);
//        for(int n = 0; n < num; n++) {
//            int start = n * base;
//            int end = (n + 1) * base;//结束坐标
//            if (end > securities.size()) {
//                end = securities.size();
//            }
//            List<Securities> list = securities.subList(start, end);
//            int finalN = n;
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    for (Securities s : list) {
//                        String read = fileUtil.read("market\\" + s.getSystemCode() + ".json");//读取本地文件中的数据
//                        if(StringUtils.hasLength(read)){
//                            redisUtil.setStrValue(s.getSystemCode(), read);
//                        }
//
//                        read = fileUtil.read("movingAverage\\" + s.getSystemCode() + ".json");//读取本地文件中的数据
//                        if(StringUtils.hasLength(read)){
//                            redisUtil.setStrValue("m_avg_" + s.getSystemCode(), read);
//                        }
//                    }
//                    System.err.println(sdf_.format(new Date()) + "------初始化数据到缓存：线程 " + (finalN + 1) + " 任务结束。");
//                }
//            }).start();
//        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(sdf.format(new Date()) + "------初始化数据到缓存任务开始");
        Set<Integer> ids = new HashSet<>();
        for(Securities s : securities){
            String read = fileUtil.read("market\\" + s.getSystemCode() + ".json");//读取本地文件中的数据
            if(StringUtils.hasLength(read)){
                ids.add(s.getId());
            }
        }
        redisUtil.setStrValue("securitiesId", JSON.toJSONString(ids));
        System.err.println(sdf.format(new Date()) + "------初始化数据到缓存任务结束。");
    }

    /**
     * 检查历史数据
     */
    @Override
    public void checkHistoricalMarketData(List<Securities> list) {
        List<Securities> list1 = list;
        CacheUtil.securities = new ArrayList<>();
        number = 0;
        if(null == list1){
            list1 = securitiesMapper.querySecuritiesList(null, null);
        }
        threadNum = list1.size() % 50 != 0 ? (list1.size() / 50) + 1 : list1.size() / 50;
        SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(sdf_.format(new Date()) + "------检查历史数据任务开始。线程数--" + threadNum);
        for(int n = 0; n < threadNum; n++){
            int start = n * 50;
            int end = (n + 1) * 50;
            if(n == (threadNum - 1)){
                end = list1.size();
            }
            List<Securities> securities = list1.subList(start, end);
            List<Securities> finalList = list1;
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
                                    fileUtil.write("market\\" + s.getSystemCode() + ".json", jsonObject.toJSONString());//写入
                                }
                            }
                            number++;
//                            Thread.sleep(new Random().nextInt(10) * 1000);//暂停10内随机秒，防止因频繁调用被限制IP
                        }
                        threadNum--;
                        if(number != finalList.size() && threadNum == 0){//所有线程处理完成，但是任务没有处理完
                            SecuritiesMarketServiceImpl.this.checkHistoricalMarketData(CacheUtil.securities);
                        }
                        if(number == finalList.size() && threadNum == 0){
                            System.err.println(sdf_.format(new Date()) + "------检查历史数据任务结束。");
                        }
                    }catch (Exception e){
                        e.printStackTrace();
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


    /**
     * 计算移动平均成交数据
     * @param securitiesCategoryCode
     */
    @Override
    public void calculateMovingAverage(String securitiesCategoryCode) {
        List<Securities> list = securitiesMapper.querySecuritiesList(null, securitiesCategoryCode);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(sdf.format(new Date()) + "------计算移动平均成交数据开始");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SecuritiesMarketServiceImpl.this.movingAverage(Arrays.asList(1, 5, 10, 20, 60, 240), list);
                    System.err.println(sdf.format(new Date()) + "------计算移动平均成交数据结束");
                    averagePriceService.saveAveragePrice(securitiesCategoryCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * 计算移动平均势能数据
     * @param securitiesCategoryCode
     * @throws Exception
     */
    @Override
    public void potentialEnergyMovingAverage(String securitiesCategoryCode) throws Exception {
        List<Securities> list = securitiesMapper.querySecuritiesList(null, securitiesCategoryCode);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(sdf.format(new Date()) + "------计算移动平均势能数据开始");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SecuritiesMarketServiceImpl.this.potentialEnergy(Arrays.asList(0, 2, 3, 4, 5, 6, 7, 8, 9, 10), list);
                    System.err.println(sdf.format(new Date()) + "------计算移动平均势能数据结束");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * 计算周纬度日行情均价数据
     * @param securitiesCategoryCode
     * @throws Exception
     */
    @Override
    public void weekMovingAverage(String securitiesCategoryCode) throws Exception {
        List<Securities> list = securitiesMapper.querySecuritiesList(null, securitiesCategoryCode);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(sdf.format(new Date()) + "------计算周纬度日行情均价数据开始");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for(Securities s : list){
                        List<String> date = new ArrayList<>();
                        List<String> price = new ArrayList<>();
                        String value = fileUtil.read("market\\" + s.getSystemCode() + ".json");
                        if(!StringUtils.hasLength(value)){
                            continue;
                        }
                        JSONObject jsonObject = JSON.parseObject(value);
                        List<SecuritiesMarketVo> market = jsonObject.getJSONArray("market").toJavaList(SecuritiesMarketVo.class);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        BigDecimal sum = new BigDecimal(0);
                        int num = 0;
                        for(int i = 0; i < market.size(); i++){
                            SecuritiesMarketVo sm = market.get(i);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(sm.getTradeDate());
                            int week = calendar.get(Calendar.DAY_OF_WEEK);
                            week = week > 1 ? week - 1 : week + 6;
                            if(week == 1){
                                if(num != 0){
                                    //计算上轮的数值
                                    BigDecimal avg = sum.divide(new BigDecimal(num), new MathContext(2, RoundingMode.HALF_EVEN));
                                    price.add(avg.toString());
                                    date.add(sdf.format(market.get(i - 1).getTradeDate()));
                                }

                                //初始化本轮的数值
                                sum = new BigDecimal(0);
                                num = 0;
                            }

                            sum = sum.add(new BigDecimal(sm.getClosingPrice()));
                            num++;

                            if(i == market.size() - 1){//最后一轮的处理
                                if(num != 0){
                                    BigDecimal avg = sum.divide(new BigDecimal(num), new MathContext(2, RoundingMode.HALF_EVEN));
                                    price.add(avg.toString());
                                    date.add(sdf.format(sm.getTradeDate()));
                                }
                            }
                        }
                        Map<String, Object> map = new HashMap<>();
                        map.put("m_avg_5", price);
                        map.put("date", date);
                        List<Integer> days = Arrays.asList(10, 20, 60, 240);
                        List<SecuritiesMarketVo> market_ = new ArrayList<>();
                        for(int i = 0; i < price.size(); i++){
                            SecuritiesMarketVo sm = new SecuritiesMarketVo();
                            sm.setClosingPrice(price.get(i));
                            sm.setTradeDate(sdf.parse(date.get(i)));
                            market_.add(sm);
                        }
                        for(int i = 0; i < days.size(); i++){
                            List<String> agr = new ArrayList<>();
                            Integer d = days.get(i);//天数
                            for(int j = 0; j < market_.size(); j++){
                                SecuritiesMarketVo object = market_.get(j);
                                Double avg = getAvgClosingPrice(market_, object.getTradeDate(), d);
                                agr.add(avg.toString());
                            }
                            map.put("m_avg_" + d, agr);
                        }
                        fileUtil.write("weekMovingAverage\\" + s.getSystemCode() + ".json", JSON.toJSONString(map));
                    }

                    System.err.println(sdf.format(new Date()) + "------计算周纬度日行情均价数据结束");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }



    /**
     * 计算月纬度日行情均价数据
     * @param securitiesCategoryCode
     * @throws Exception
     */
    @Override
    public void monthMovingAverage(String securitiesCategoryCode) throws Exception {
        List<Securities> list = securitiesMapper.querySecuritiesList(null, securitiesCategoryCode);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(sdf.format(new Date()) + "------计算月纬度日行情均价数据开始");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for(Securities s : list){
                        List<String> date = new ArrayList<>();
                        List<String> price = new ArrayList<>();
                        String value = fileUtil.read("market\\" + s.getSystemCode() + ".json");
                        if(!StringUtils.hasLength(value)){
                            continue;
                        }
                        JSONObject jsonObject = JSON.parseObject(value);
                        List<SecuritiesMarketVo> market = jsonObject.getJSONArray("market").toJavaList(SecuritiesMarketVo.class);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        BigDecimal sum = new BigDecimal(0);
                        int num = 0;
                        int m = 0;
                        for(int i = 0; i < market.size(); i++){
                            SecuritiesMarketVo sm = market.get(i);
                            int month = DateUtil.createDate(sm.getTradeDate()).MONTH;
                            if(i == 0){
                                m = month;
                            }
                            if(m != month){//跨月
                                //计算上轮的数值
                                if(num != 0){
                                    BigDecimal avg = sum.divide(new BigDecimal(num), new MathContext(2, RoundingMode.HALF_EVEN));
                                    price.add(avg.toString());
                                    date.add(sdf.format(market.get(i - 1).getTradeDate()));
                                }

                                //初始化本轮的数值
                                sum = new BigDecimal(0);
                                num = 0;
                                m = month;
                            }

                            sum = sum.add(new BigDecimal(sm.getClosingPrice()));
                            num++;

                            if(i == market.size() - 1){//最后一轮的处理
                                if(num != 0){
                                    BigDecimal avg = sum.divide(new BigDecimal(num), new MathContext(2, RoundingMode.HALF_EVEN));
                                    price.add(avg.toString());
                                    date.add(sdf.format(sm.getTradeDate()));
                                }
                            }

                        }
                        Map<String, Object> map = new HashMap<>();
                        map.put("m_avg_20", price);
                        map.put("date", date);
                        List<Integer> days = Arrays.asList(60, 240);
                        List<SecuritiesMarketVo> market_ = new ArrayList<>();
                        for(int i = 0; i < price.size(); i++){
                            SecuritiesMarketVo sm = new SecuritiesMarketVo();
                            sm.setClosingPrice(price.get(i));
                            sm.setTradeDate(sdf.parse(date.get(i)));
                            market_.add(sm);
                        }
                        for(int i = 0; i < days.size(); i++){
                            List<String> agr = new ArrayList<>();
                            Integer d = days.get(i);//天数
                            for(int j = 0; j < market_.size(); j++){
                                SecuritiesMarketVo object = market_.get(j);
                                Double avg = getAvgClosingPrice(market_, object.getTradeDate(), d);
                                agr.add(avg.toString());
                            }
                            map.put("m_avg_" + d, agr);
                        }
                        fileUtil.write("monthMovingAverage\\" + s.getSystemCode() + ".json", JSON.toJSONString(map));
                    }

                    System.err.println(sdf.format(new Date()) + "------计算月纬度日行情均价数据结束");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }




    /**
     * 计算季纬度日行情均价数据
     * @param securitiesCategoryCode
     * @throws Exception
     */
    @Override
    public void quarterMovingAverage(String securitiesCategoryCode) throws Exception {
        List<Securities> list = securitiesMapper.querySecuritiesList(null, securitiesCategoryCode);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(sdf.format(new Date()) + "------计算季纬度日行情均价数据开始");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for(Securities s : list){
                        List<String> date = new ArrayList<>();
                        List<String> price = new ArrayList<>();
                        String value = fileUtil.read("market\\" + s.getSystemCode() + ".json");
                        if(!StringUtils.hasLength(value)){
                            continue;
                        }
                        JSONObject jsonObject = JSON.parseObject(value);
                        List<SecuritiesMarketVo> market = jsonObject.getJSONArray("market").toJavaList(SecuritiesMarketVo.class);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        BigDecimal sum = new BigDecimal(0);
                        int num = 0;
                        int m = 0;
                        for(int i = 0; i < market.size(); i++){
                            SecuritiesMarketVo sm = market.get(i);
                            int quarter = DateUtil.createDate(sm.getTradeDate()).QUARTER;
                            if(i == 0){
                                m = quarter;
                            }
                            if(m != quarter){//跨季
                                //计算上轮的数值
                                if(num != 0){
                                    BigDecimal avg = sum.divide(new BigDecimal(num), new MathContext(2, RoundingMode.HALF_EVEN));
                                    price.add(avg.toString());
                                    date.add(sdf.format(market.get(i - 1).getTradeDate()));
                                }

                                //初始化本轮的数值
                                sum = new BigDecimal(0);
                                num = 0;
                                m = quarter;
                            }

                            sum = sum.add(new BigDecimal(sm.getClosingPrice()));
                            num++;

                            if(i == market.size() - 1){//最后一轮的处理
                                if(num != 0){
                                    BigDecimal avg = sum.divide(new BigDecimal(num), new MathContext(2, RoundingMode.HALF_EVEN));
                                    price.add(avg.toString());
                                    date.add(sdf.format(sm.getTradeDate()));
                                }
                            }
                        }
                        Map<String, Object> map = new HashMap<>();
                        map.put("m_avg_60", price);
                        map.put("date", date);
                        List<Integer> days = Arrays.asList(240);
                        List<SecuritiesMarketVo> market_ = new ArrayList<>();
                        for(int i = 0; i < price.size(); i++){
                            SecuritiesMarketVo sm = new SecuritiesMarketVo();
                            sm.setClosingPrice(price.get(i));
                            sm.setTradeDate(sdf.parse(date.get(i)));
                            market_.add(sm);
                        }
                        for(int i = 0; i < days.size(); i++){
                            List<String> agr = new ArrayList<>();
                            Integer d = days.get(i);//天数
                            for(int j = 0; j < market_.size(); j++){
                                SecuritiesMarketVo object = market_.get(j);
                                Double avg = getAvgClosingPrice(market_, object.getTradeDate(), d);
                                agr.add(avg.toString());
                            }
                            map.put("m_avg_" + d, agr);
                        }
                        fileUtil.write("quarterMovingAverage\\" + s.getSystemCode() + ".json", JSON.toJSONString(map));
                    }

                    System.err.println(sdf.format(new Date()) + "------计算季纬度日行情均价数据结束");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }





    /**
     * 计算年纬度日行情均价数据
     * @param securitiesCategoryCode
     * @throws Exception
     */
    @Override
    public void yearMovingAverage(String securitiesCategoryCode) throws Exception {
        List<Securities> list = securitiesMapper.querySecuritiesList(null, securitiesCategoryCode);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println(sdf.format(new Date()) + "------计算年纬度日行情均价数据开始");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for(Securities s : list){
                        List<String> date = new ArrayList<>();
                        List<String> price = new ArrayList<>();
                        String value = fileUtil.read("market\\" + s.getSystemCode() + ".json");
                        if(!StringUtils.hasLength(value)){
                            continue;
                        }
                        JSONObject jsonObject = JSON.parseObject(value);
                        List<SecuritiesMarketVo> market = jsonObject.getJSONArray("market").toJavaList(SecuritiesMarketVo.class);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        BigDecimal sum = new BigDecimal(0);
                        int num = 0;
                        int m = 0;
                        for(int i = 0; i < market.size(); i++){
                            SecuritiesMarketVo sm = market.get(i);
                            int year = DateUtil.createDate(sm.getTradeDate()).YEAR;
                            if(i == 0){
                                m = year;
                            }
                            if(m != year){//跨年
                                //计算上轮的数值
                                if(num != 0){
                                    BigDecimal avg = sum.divide(new BigDecimal(num), new MathContext(2, RoundingMode.HALF_EVEN));
                                    price.add(avg.toString());
                                    date.add(sdf.format(market.get(i - 1).getTradeDate()));
                                }

                                //初始化本轮的数值
                                sum = new BigDecimal(0);
                                num = 0;
                                m = year;
                            }

                            sum = sum.add(new BigDecimal(sm.getClosingPrice()));
                            num++;

                            if(i == market.size() - 1){//最后一轮的处理
                                if(num != 0){
                                    BigDecimal avg = sum.divide(new BigDecimal(num), new MathContext(2, RoundingMode.HALF_EVEN));
                                    price.add(avg.toString());
                                    date.add(sdf.format(sm.getTradeDate()));
                                }
                            }
                        }
                        Map<String, Object> map = new HashMap<>();
                        map.put("m_avg_240", price);
                        map.put("date", date);
                        fileUtil.write("yearMovingAverage\\" + s.getSystemCode() + ".json", JSON.toJSONString(map));
                    }

                    System.err.println(sdf.format(new Date()) + "------计算年纬度日行情均价数据结束");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }




    /**
     * 导出数据到excel
     * @param systemCode
     * @return
     * @throws Exception
     */
    @Override
    public HSSFWorkbook exportMarket(String systemCode) throws Exception {
        Securities securities = securitiesMapper.querySystemCode(systemCode);
        String value = fileUtil.read("market\\" + securities.getSystemCode() + ".json");
        List<List<List<String>>> lists = new ArrayList<>();
        List<List<String>> data = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if(StringUtils.hasLength(value)){
            JSONObject jsonObject = JSON.parseObject(value);
            List<SecuritiesMarketVo> market = jsonObject.getJSONArray("market").toJavaList(SecuritiesMarketVo.class);
            for(SecuritiesMarketVo sm : market){
                List<String> list = new ArrayList<>();
                list.add(sdf.format(sm.getTradeDate()));
                list.add(sm.getLastClosingPrice());
                list.add(sm.getClosingPrice());
                list.add(sm.getRiseFallPrice());
                list.add(sm.getRiseFallRatio());
                list.add(sm.getOpeningPrice());
                list.add(sm.getTopPrice());
                list.add(sm.getLowestPrice());
                list.add(sm.getAmplitude());
                list.add(sm.getVolume());
                list.add(sm.getDealAmount());
                list.add(sm.getTurnoverRate());
                data.add(list);
            }
        }
        lists.add(data);
        List<List<String>> titles = new ArrayList<>();
        List<String> title = new ArrayList<>();
        title.add("交易日期");
        title.add("上期收盘价");
        title.add("本期收盘价");
        title.add("涨跌金额");
        title.add("涨跌率（%）");
        title.add("开盘价");
        title.add("最高价");
        title.add("最低价");
        title.add("振幅率（%）");
        title.add("成交量（股）");
        title.add("成交金额（元）");
        title.add("换手率（%）");
        titles.add(title);
        return excelUtil.writeDataToExcel(titles, lists);
    }


    /**
     * 获取推荐参考证券数据（移动平均成交数据交叉的数据）
     * @param pageNo
     * @param pageSize
     * @return
     * @throws Exception
     */
    @Override
    public List<MarketMovingAverageVo> queryRecommendData(Integer securitiesCategoryId, String code, Integer pageNo, Integer pageSize) throws Exception {
        pageNo = (pageNo - 1) * pageSize;
        List<Map<String, Object>> maps = averagePriceService.queryRecommendData(securitiesCategoryId, code, pageNo, pageSize);
        List<MarketMovingAverageVo> list = new ArrayList<>();
        for (Map<String, Object> m : maps){
            MarketMovingAverageVo marketMovingAverageVo = new MarketMovingAverageVo();
            marketMovingAverageVo.setCode(m.get("code").toString());
            marketMovingAverageVo.setSystemCode(m.get("systemCode").toString());
            marketMovingAverageVo.setName(m.get("name").toString());
            marketMovingAverageVo.setFollow(Integer.valueOf(m.get("follow").toString()));
            marketMovingAverageVo.setSecuritiesCategory(m.get("securitiesCategory").toString());
            marketMovingAverageVo.setPrice(Double.valueOf(m.get("price").toString()));
            marketMovingAverageVo.setFiveAveragePrice(Double.valueOf(m.get("fiveAveragePrice").toString()));
            marketMovingAverageVo.setFifteenAveragePrice(Double.valueOf(m.get("fifteenAveragePrice").toString()));
            marketMovingAverageVo.setFiveDayDifference(Double.valueOf(m.get("fiveDayDifference").toString()));
            marketMovingAverageVo.setFifteenDayDifference(Double.valueOf(m.get("fifteenDayDifference").toString()));
            list.add(marketMovingAverageVo);
        }
        return list;
    }
}
