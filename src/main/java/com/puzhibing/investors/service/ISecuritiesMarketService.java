package com.puzhibing.investors.service;

import com.puzhibing.investors.util.ResultUtil;

import java.util.List;
import java.util.Map;

public interface ISecuritiesMarketService {


    /**
     * 获取并添加证券日行情数据
     * @throws Exception
     */
    void pullSecuritiesMarket() throws Exception;


    /**
     * 获取指定日期范围内的数据
     * @param code
     * @param date
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> queryAllData(String code, Integer securitiesCategoryId, String date, Integer pageNo, Integer pageSize) throws Exception;



    /**
     * 同步证券历史交易数据
     * @return
     * @throws Exception
     */
    ResultUtil synchronizeHistoricalData(Integer id);
}
