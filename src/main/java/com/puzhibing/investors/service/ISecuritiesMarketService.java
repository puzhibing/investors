package com.puzhibing.investors.service;

import com.puzhibing.investors.pojo.Securities;
import com.puzhibing.investors.pojo.vo.MarketMovingAverageVo;
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
    Map<String, Object> queryMarkt(Integer type, String code) throws Exception;



    /**
     * 同步证券历史交易数据
     * @return
     * @throws Exception
     */
    ResultUtil synchronizeHistoricalData(Integer base);


    /**
     * 获取移动平均势能数据
     * @param code
     * @throws Exception
     */
    List<Map<String, Object>> queryPotentialEnergy(String code) throws Exception;



    /**
     * 获取移动平均势能数据
     * @param code
     * @throws Exception
     */
    List<Map<String, Object>> queryPotentialEnergy_(String code) throws Exception;


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
     * 计算周纬度日行情均价数据
     * @param securitiesCategoryCode
     * @throws Exception
     */
    void weekMovingAverage(String securitiesCategoryCode) throws Exception;

    /**
     * 计算月纬度日行情均价数据
     * @param securitiesCategoryCode
     * @throws Exception
     */
    void monthMovingAverage(String securitiesCategoryCode) throws Exception;

    /**
     * 计算季纬度日行情均价数据
     * @param securitiesCategoryCode
     * @throws Exception
     */
    void quarterMovingAverage(String securitiesCategoryCode) throws Exception;

    /**
     * 计算年纬度日行情均价数据
     * @param securitiesCategoryCode
     * @throws Exception
     */
    void yearMovingAverage(String securitiesCategoryCode) throws Exception;


    /**
     * 导出数据到excel
     * @param systemCode
     * @return
     * @throws Exception
     */
    HSSFWorkbook exportMarket(String systemCode) throws Exception;


    /**
     * 获取推荐参考证券数据（移动平均成交数据交叉的数据）
     * @return
     * @throws Exception
     */
    List<MarketMovingAverageVo> queryRecommendData(Integer securitiesCategoryId, String code, Integer pageNo, Integer pageSize) throws Exception;
}
