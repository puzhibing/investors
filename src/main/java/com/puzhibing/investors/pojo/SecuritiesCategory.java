package com.puzhibing.investors.pojo;

import lombok.Data;

/**
 * 证券类型
 */
@Data
public class SecuritiesCategory {
    /**
     * 主键
     */
    private Integer id;
    /**
     * 编号
     */
    private String code;
    /**
     * 名称
     */
    private String name;
    /**
     * 所属国家/地区
     */
    private Integer countryId;

}
