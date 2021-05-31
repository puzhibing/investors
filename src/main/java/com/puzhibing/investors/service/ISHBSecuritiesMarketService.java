package com.puzhibing.investors.service;

import com.puzhibing.investors.pojo.SHBSecuritiesMarket;

import java.util.Date;
import java.util.List;

public interface ISHBSecuritiesMarketService {

    /**
     * 获取指定日期范围内的数据
     * @param securitiesId
     * @param start
     * @param end
     * @return
     * @throws Exception
     */
    List<SHBSecuritiesMarket> queryList(Integer securitiesId, Date start, Date end) throws Exception;
}
