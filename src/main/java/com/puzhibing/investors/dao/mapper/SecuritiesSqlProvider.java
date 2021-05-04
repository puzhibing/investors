package com.puzhibing.investors.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;

public class SecuritiesSqlProvider {

    private final String COLUMNS = "systemCode, code, securitiesCategoryId, name, marketTime, marketAddress";


    /**
     * 添加数据
     * @return
     */
    public String insert(){
        return new SQL(){{
            INSERT_INTO("db_securities")
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
                    .FROM("db_securities")
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
                    .FROM("db_securities")
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


}
