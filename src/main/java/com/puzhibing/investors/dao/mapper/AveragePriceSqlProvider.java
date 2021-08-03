package com.puzhibing.investors.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;



public class AveragePriceSqlProvider {

    private String tableName = "db_average_price";

    private String columns = "securitiesId, price, fiveAveragePrice, fifteenAveragePrice";


    /**
     * 添加数据
     * @return
     */
    public String insert(){
        return new SQL(){{
            INSERT_INTO(tableName)
                    .INTO_COLUMNS(columns)
                    .INTO_VALUES("#{securitiesId}, #{price}, #{fiveAveragePrice}, #{fifteenAveragePrice}");
        }}.toString();
    }


    /**
     * 修改数据
     * @return
     */
    public String updateById(){
        return new SQL(){{
            UPDATE(tableName)
                    .SET("securitiesId = #{securitiesId}, price = #{price}, fiveAveragePrice = #{fiveAveragePrice}, fifteenAveragePrice = #{fifteenAveragePrice}")
                    .WHERE("id = #{id}");
        }}.toString();
    }


    /**
     * 查询单个数据
     * @return
     */
    public String selectById(){
        return new SQL(){{
            SELECT("id, " + columns)
                    .FROM(tableName)
                    .WHERE("id = #{id}");
        }}.toString();
    }


    /**
     * 根据证券id查询
     * @return
     */
    public String selectBySecuritiesId(){
        return new SQL(){{
            SELECT("id, " + columns)
                    .FROM(tableName)
                    .WHERE("securitiesId = #{securitiesId}");
        }}.toString();
    }


    /**
     * 获取推荐参考证券数据（移动平均成交数据交叉的数据）
     * @param securitiesCategoryId
     * @param code
     * @return
     */
    public String queryRecommendData(@Param("securitiesCategoryId") Integer securitiesCategoryId, @Param("code") String code){
        SQL sql_ = new SQL();
        sql_.SELECT("b.`code` as `code`,\n" +
                "            b.systemCode as systemCode,\n" +
                "            b.`name` as `name`,\n" +
                "            b.follow as follow,\n" +
                "            c.`name` as securitiesCategory,\n" +
                "            a.price as price,\n" +
                "            a.fiveAveragePrice as fiveAveragePrice,\n" +
                "            a.fifteenAveragePrice as fifteenAveragePrice,\n" +
                "            (a.fiveAveragePrice - a.price) as fiveDayDifference,\n" +
                "            (a.fifteenAveragePrice - a.price) as fifteenDayDifference")
                .FROM("db_average_price a")
                .LEFT_OUTER_JOIN("db_securities b on (a.securitiesId = b.id)")
                .LEFT_OUTER_JOIN("db_securities_category c on (b.securitiesCategoryId = c.id)")
                .WHERE("1 = 1");
        if(null != securitiesCategoryId){
            sql_.WHERE("b.securitiesCategoryId = #{securitiesCategoryId}");
        }
        if(null != code){
            sql_.WHERE("b.`code` like '%" + code + "%'");
        }

        SQL sql = new SQL();
        sql.SELECT("*")
                .FROM("(" + sql_.toString() + ") as aa")
                .ORDER_BY("aa.fiveDayDifference desc, aa.fifteenDayDifference desc")
                .LIMIT("#{pageNo}, #{pageSize}");
        return sql.toString();
    }
}
