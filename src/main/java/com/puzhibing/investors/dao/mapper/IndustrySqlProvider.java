package com.puzhibing.investors.dao.mapper;

import org.apache.ibatis.jdbc.SQL;

/**
 * SQL语句构造类
 */
public class IndustrySqlProvider {


    /**
     * 添加数据
     * @return
     */
    public String pullIndustrySql(){
        return new SQL()
                .INSERT_INTO("db_industry")
                .INTO_COLUMNS("pid", "name", "code")
                .INTO_VALUES("#{pid}", "#{name}", "#{code}")
                .toString();
    }


    /**
     * 根据code查询数据
     * @return
     */
    public String queryByCode(){
        return new SQL(){{
            SELECT("id, pid, name, code")
                    .FROM("db_industry")
                    .WHERE("code = #{code}");
        }}.toString();
    }
}
