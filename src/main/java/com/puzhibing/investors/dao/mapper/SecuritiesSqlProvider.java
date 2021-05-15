package com.puzhibing.investors.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;

public class SecuritiesSqlProvider {

    private final String COLUMNS = "systemCode, code, securitiesCategoryId, name, marketTime, marketAddress";

    private final String TABLE = "db_securities";


    /**
     * 添加数据
     * @return
     */
    public String insert(){
        return new SQL(){{
            INSERT_INTO(TABLE)
                    .INTO_COLUMNS(COLUMNS)
                    .INTO_VALUES("#{systemCode}, #{code}, #{securitiesCategoryId}, #{name}, #{marketTime}, #{marketAddress}");
        }}.toString();
    }


    /**
     * 根据证券编号和类型id获取数据
     * @return
     */
    public String queryByCodeAndSecuritiesCategory(){
        return new SQL(){{
            SELECT("id, " + COLUMNS)
                    .FROM(TABLE)
                    .WHERE("code = #{code} and securitiesCategoryId = #{securitiesCategoryId}");
        }}.toString();
    }


    /**
     * 根据证券编号和类型id获取数据
     * @param code
     * @param securitiesCategoryId
     * @return
     */
    public String queryList(@Param("code") String code, @Param("securitiesCategoryId") Integer securitiesCategoryId,
                            @Param("pageNo") Integer pageNo, @Param("pageSize") Integer pageSize){
        return new SQL(){{
            SQL sql = SELECT("id, " + COLUMNS)
                    .FROM(TABLE)
                    .WHERE("1 = 1");
            if(null != code){
                sql.WHERE("code like CONCAT('%', #{code}, '%')").OR().WHERE("systemCode like CONCAT('%', #{code}, '%')");
            }
            if(null != securitiesCategoryId){
                sql.WHERE("securitiesCategoryId = #{securitiesCategoryId}");
            }
            if(null != pageNo && null != pageSize){
                sql.LIMIT("#{pageNo}, #{pageSize}");
            }
        }}.toString();
    }


    /**
     * 查询数据列表
     * @param code
     * @param securitiesCategoryCode
     * @return
     */
    public String querySecuritiesList(@Param("code") String code, @Param("securitiesCategoryCode") String securitiesCategoryCode){
        SQL sql = new SQL();
        sql.SELECT("id, " + COLUMNS)
                .FROM(TABLE)
                .WHERE("1 = 1");
        if(null != code && !"".equals(code)){
            sql.WHERE("code like CONCAT('%', #{code}, '%')").OR().WHERE("systemCode like CONCAT('%', #{code}, '%')");
        }
        if(null != securitiesCategoryCode && !"".equals(securitiesCategoryCode)){
            sql.WHERE("securitiesCategoryId in (select id from db_securities_category where code like CONCAT('%', #{securitiesCategoryCode}, '%'))");
        }
        return sql.toString();
    }
}
