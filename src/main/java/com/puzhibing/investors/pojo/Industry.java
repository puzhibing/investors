package com.puzhibing.investors.pojo;

import lombok.Data;

/**
 * 行业分类
 */
@Data
public class Industry {
    /**
     * 主键
     */
    private Integer id;
    /**
     * 上级id
     */
    private Integer pid;
    /**
     * 行业名称
     */
    private String name;
    /**
     * 编号
     */
    private String code;
}
