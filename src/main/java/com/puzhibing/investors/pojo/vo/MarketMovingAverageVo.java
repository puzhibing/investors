package com.puzhibing.investors.pojo.vo;

import lombok.Data;

import java.util.Date;

/**
 * 移动平均
 */
@Data
public class MarketMovingAverageVo {
    /**
     * 日期
     */
    private Date day;
    /**
     * 平均值
     */
    private String avg;

}
