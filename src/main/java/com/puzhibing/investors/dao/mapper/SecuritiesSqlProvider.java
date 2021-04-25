package com.puzhibing.investors.dao.mapper;

import org.apache.ibatis.jdbc.SQL;

public class SecuritiesSqlProvider {

    private final String COLUMNS = "code, securitiesCategoryId, name, marketTime, marketAddress";


    /**
     * 添加数据
     * @return
     */
    public String insert(){
        return new SQL(){{
            INSERT_INTO("db_securities")
                    .INTO_COLUMNS(COLUMNS)
                    .INTO_VALUES("#{code}, #{securitiesCategoryId}, #{name}, #{marketTime}, #{marketAddress}");
        }}.toString();
    }


    /**
     * 根据证券编号和类型id获取数据
     * @return
     */
    public String queryByCodeAndSecuritiesCategory(){
        return new SQL(){{
            SELECT("id, " + COLUMNS)
                    .FROM("db_securities")
                    .WHERE("code = #{code} and securitiesCategoryId = #{securitiesCategoryId}");
        }}.toString();
    }
}
