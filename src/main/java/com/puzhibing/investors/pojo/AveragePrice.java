package com.puzhibing.investors.pojo;

import lombok.Data;

/**
 * 存储当前的移动平均成交数据
 */
@Data
public class AveragePrice {
    /**
     * 主键
     */
    private Integer id;
    /**
     * 证券id
     */
    private Integer securitiesId;
    /**
     * 成交金额
     */
    private Double price;
    /**
     * 五日平均成交金额
     */
    private Double fiveAveragePrice;
    /**
     * 十五日平均成交金额
     */
    private Double fifteenAveragePrice;
}
