package com.puzhibing.investors.pojo.vo;

import lombok.Data;

import java.util.Date;

/**
 * 移动平均成交价
 */
@Data
public class MarketMovingAverageVo {
    /**
     * 证券编号
     */
    private String code;
    /**
     * 系统编号
     */
    private String systemCode;
    /**
     * 证券名称
     */
    private String name;
    /**
     * 关注（1=否，2=关注）
     */
    private Integer follow;
    /**
     * 上市证券交易所
     */
    private String securitiesCategory;
    /**
     * 当前成交价
     */
    private Double price;
    /**
     * 五日移动平均成交价
     */
    private Double fiveAveragePrice;
    /**
     * 十五日移动平均成交价
     */
    private Double fifteenAveragePrice;
    /**
     * 五日差额
     */
    private Double fiveDayDifference;
    /**
     * 十五日差额
     */
    private Double fifteenDayDifference;

}
