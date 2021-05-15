package com.puzhibing.investors.dao.mapper;

import com.puzhibing.investors.pojo.SHBSecuritiesMarket;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;

import java.util.Date;


public class SHBSecuritiesMarketSqlProvider {

    private final String COLUMNS = "securitiesId, tradeDate, lastClosingPrice, closingPrice, riseFallPrice, riseFallRatio, " +
            "openingPrice, topPrice, lowestPrice, amplitude, volume, dealAmount, turnoverRate";

    private final String TABLE = "db_sh_b_securities_market";

    /**
     * 添加数据
     * @return
     */
    public String insert(){
        return new SQL(){{
            INSERT_INTO(TABLE)
                    .INTO_COLUMNS(COLUMNS)
                    .INTO_VALUES("#{securitiesId}, #{tradeDate}, #{lastClosingPrice}, #{closingPrice}, #{riseFallPrice}, #{riseFallRatio}, " +
                            "#{openingPrice}, #{topPrice}, #{lowestPrice}, #{amplitude}, #{volume}, #{dealAmount}, #{turnoverRate}");
        }}.toString();
    }


    /**
     * 根据证券id和行情日期获取数据
     * @return
     */
    public String queryBySecuritiesIdAndDate(){
        return new SQL(){{
            SELECT("id, " + COLUMNS)
                    .FROM(TABLE)
                    .WHERE("securitiesId = #{securitiesId} and DATE_FORMAT(tradeDate, '%Y-%m-%d') = DATE_FORMAT(#{date}, '%Y-%m-%d')");
        }}.toString();
    }


    /**
     * 获取时间范围内的数据
     * @param securitiesId
     * @param start
     * @param end
     * @return
     */
    public String queryList(@Param("securitiesId") Integer securitiesId, @Param("start") Date start,
                            @Param("end") Date end){
        SQL sql = new SQL() {{
            SELECT("id, " + COLUMNS)
                    .FROM(TABLE)
                    .WHERE("securitiesId = #{securitiesId}");
        }};
        if(null != start && null != end){
            sql.WHERE("tradeDate BETWEEN #{start} AND #{end}");
        }
        sql.ORDER_BY("tradeDate");
        return sql.toString();
    }



    /**
     * 修改数据
     * @return
     */
    public String update(){
        return new SQL(){{
            UPDATE(TABLE)
                    .SET("securitiesId = #{securitiesId}, tradeDate = #{tradeDate}, lastClosingPrice = #{lastClosingPrice}, closingPrice = #{closingPrice}, " +
                            "riseFallPrice = #{riseFallPrice}, riseFallRatio = #{riseFallRatio}, openingPrice = #{openingPrice}, topPrice = #{topPrice}, lowestPrice = #{lowestPrice}, " +
                            "amplitude = #{amplitude}, volume = #{volume}, dealAmount = #{dealAmount}, turnoverRate = #{turnoverRate}")
                    .WHERE("id = #{id}");
        }}.toString();
    }
}
