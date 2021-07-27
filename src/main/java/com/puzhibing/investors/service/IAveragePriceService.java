package com.puzhibing.investors.service;

import java.util.List;
import java.util.Map;

public interface IAveragePriceService {

    /**
     * 保存最新数据
     * @param type
     * @throws Exception
     */
    void saveAveragePrice(String type) throws Exception;


    /**
     * 获取推荐参考证券数据（移动平均成交数据交叉的数据）
     * @param pageNo
     * @param pageSize
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> queryRecommendData(Integer securitiesCategoryId, String code, Integer pageNo, Integer pageSize) throws Exception;
}
