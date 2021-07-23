package com.puzhibing.investors.service;

import com.puzhibing.investors.pojo.Securities;
import com.puzhibing.investors.util.ResultUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.util.List;
import java.util.Map;

public interface ISecuritiesMarketService {


    /**
     * 获取并添加证券日行情数据
     * @throws Exception
     */
    void pullSecuritiesMarket() ;


    /**
     * 获取指定日期范围内的数据
     * @param code
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> queryAllData(String code, Integer pageNo, Integer pageSize) throws Exception;



    /**
     * 获取行情数据
     * @param code
     * @return
     * @throws Exception
     */
    Map<String, Object> queryMarkt(String code) throws Exception;



    /**
     * 同步证券历史交易数据
     * @return
     * @throws Exception
     */
    ResultUtil synchronizeHistoricalData(Integer base);


    /**
     *
     * @param code
     * @throws Exception
     */
    List<Map<String, Object>> queryPotentialEnergy(String code) throws Exception;


    /**
     * 初始化数据到缓存中
     * @return
     * @throws Exception
     */
    void initMarketToCache() throws Exception;


    /**
     * 检查历史行情数据
     */
    void checkHistoricalMarketData(List<Securities> list);


    /**
     * 计算移动平均成交数据
     * @throws Exception
     */
    void calculateMovingAverage(String securitiesCategoryCode) throws Exception;


    /**
     * 计算移动平均势能数据
     * @param securitiesCategoryCode
     * @throws Exception
     */
    void potentialEnergyMovingAverage(String securitiesCategoryCode) throws Exception;


    /**
     * 导出数据到excel
     * @param systemCode
     * @return
     * @throws Exception
     */
    HSSFWorkbook exportMarket(String systemCode) throws Exception;
}
