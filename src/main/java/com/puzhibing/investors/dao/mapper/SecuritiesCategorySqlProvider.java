package com.puzhibing.investors.dao.mapper;

import org.apache.ibatis.jdbc.SQL;

public class SecuritiesCategorySqlProvider {


    /**
     * 根据编号获取类型
     * @return
     */
    public String queryByCode(){
        return new SQL(){{
            SELECT("id, code, name, countryId")
                    .FROM("db_securities_category")
                    .WHERE("code = #{code}");
        }}.toString();
    }


    /**
     * 根据id查询数据
     * @return
     */
    public String selectById(){
        return new SQL(){{
            SELECT("id, code, name, countryId")
                    .FROM("db_securities_category")
                    .WHERE("id = #{id}");
        }}.toString();
    }

}
