package com.puzhibing.investors.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.puzhibing.investors.dao.SecuritiesMapper;
import com.puzhibing.investors.pojo.SHBSecuritiesMarket;
import com.puzhibing.investors.pojo.Securities;
import com.puzhibing.investors.service.ISHBSecuritiesMarketService;
import com.puzhibing.investors.util.CacheUtil;
import com.puzhibing.investors.util.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class SHBSecuritiesMarketServiceImpl implements ISHBSecuritiesMarketService {

    @Resource
    private SecuritiesMapper securitiesMapper;

    @Autowired
    private RedisUtil redisUtil;


    /**
     * 获取指定日期范围内的数据
     * @param securitiesId
     * @param start
     * @param end
     * @return
     * @throws Exception
     */
    @Override
    public List<SHBSecuritiesMarket> queryList(Integer securitiesId, Date start, Date end) throws Exception {
        List<SHBSecuritiesMarket> list = new ArrayList<>();
        Securities securities = securitiesMapper.selectById(securitiesId);
//        String value = CacheUtil.markets.get(securities.getSystemCode());
        String value = redisUtil.getValue(securities.getSystemCode());
        JSONObject jsonObject = JSON.parseObject(value);
        if(null == jsonObject){
            return list;
        }
        JSONArray market = jsonObject.getJSONArray("market");
        for(int i = 0; i < market.size(); i++){
            SHBSecuritiesMarket object = market.getObject(i, SHBSecuritiesMarket.class);
            if(null != start && null != end){
                long time = object.getTradeDate().getTime();
                if(time >= start.getTime() && time < end.getTime()){
                    list.add(object);
                }
            }else{
                list.add(object);
            }
        }
        return list;
    }
}
