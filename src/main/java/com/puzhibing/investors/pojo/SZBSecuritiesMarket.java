package com.puzhibing.investors.pojo;

import lombok.Data;

import java.util.Date;

/**
 * 深证B股证券日行情
 */
@Data
public class SZBSecuritiesMarket implements Comparable {
    /**
     * 主键
     */
    private Integer id;
    /**
     * 证券id
     */
    private Integer securitiesId;
    /**
     * 交易日期
     */
    private Date tradeDate;
    /**
     * 上期收盘价
     */
    private String lastClosingPrice;
    /**
     * 本期收盘价
     */
    private String closingPrice;
    /**
     * 涨跌金额
     */
    private String riseFallPrice;
    /**
     * 涨跌率（%）
     */
    private String riseFallRatio;
    /**
     * 开盘价
     */
    private String openingPrice;
    /**
     * 最高价
     */
    private String topPrice;
    /**
     * 最低价
     */
    private String lowestPrice;
    /**
     * 振幅率（%）
     */
    private String amplitude;
    /**
     * 成交量（股）
     */
    private String volume;
    /**
     * 成交金额（元）
     */
    private String dealAmount;
    /**
     * 换手率（%）
     */
    private String turnoverRate;

    @Override
    public int compareTo(Object o) {
        if (o instanceof SZBSecuritiesMarket) {
            SZBSecuritiesMarket szb = (SZBSecuritiesMarket) o;
            if (this.tradeDate == null || szb.tradeDate == null) {
                return -1;
            }
            if (this.tradeDate.getTime() < szb.getTradeDate().getTime()) {
                return -1;
            } else if (this.tradeDate.getTime() == szb.getTradeDate().getTime()) {
                return 0;
            } else {
                return 1;
            }
        }
        return 0;
    }
}
