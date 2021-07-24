package com.puzhibing.investors.pojo.vo;

import lombok.Data;

import java.util.Date;

/**
 * 移动平均成交价
 */
@Data
public class MarketMovingAverageVo implements Comparable {
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
     * 差额
     */
    private Double difference;


    @Override
    public int compareTo(Object o) {
        if (o instanceof MarketMovingAverageVo) {
            MarketMovingAverageVo sm = (MarketMovingAverageVo) o;
            if (this.difference == null || sm.difference == null) {
                return -1;
            }
            if (this.difference < sm.getDifference()) {
                return -1;
            } else if (this.difference == sm.getDifference()) {
                return 0;
            } else {
                return 1;
            }
        }
        return 0;
    }
}
