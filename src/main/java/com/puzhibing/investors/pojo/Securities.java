package com.puzhibing.investors.pojo;


import lombok.Data;

import java.util.Date;

/**
 * 证券信息
 */
@Data
public class Securities {
    /**
     * 主键
     */
    private Integer id;
    /**
     * 系统编号
     */
    private String systemCode;
    /**
     * 证券编号
     */
    private String code;
    /**
     * 证券类型id
     */
    private Integer securitiesCategoryId;
    /**
     * 证券名称
     */
    private String name;
    /**
     * 上市时间
     */
    private Date marketTime;
    /**
     * 上市地点
     */
    private String marketAddress;
    /**
     * 流通股本
     */
    private Long flowEquity;
    /**
     * 关注（1=否，2=关注）
     */
    private Integer follow;
}
