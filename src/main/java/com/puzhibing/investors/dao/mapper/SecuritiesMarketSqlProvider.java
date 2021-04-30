package com.puzhibing.investors.dao.mapper;

import org.apache.ibatis.jdbc.SQL;

public class SecuritiesMarketSqlProvider {

    private final String COLUMNS = "securitiesId, tradeDate, lastClosingPrice, closingPrice, riseFallPrice, riseFallRatio, " +
            "openingPrice, topPrice, lowestPrice, amplitude, volume, dealAmount, inout";

    private final String TABLE = "db_securities_market";

    /**
     * 添加数据
     * @return
     */
    public String insert(){
        return new SQL(){{
            INSERT_INTO(TABLE)
                    .INTO_COLUMNS(COLUMNS)
                    .INTO_VALUES("#{securitiesId}, #{tradeDate}, #{lastClosingPrice}, #{closingPrice}, #{riseFallPrice}, #{riseFallRatio}, " +
                            "#{openingPrice}, #{topPrice}, #{lowestPrice}, #{amplitude}, #{volume}, #{dealAmount}, #{inout}");
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
                    .WHERE("securitiesId = #{securitiesId} and tradeDate = #{date}");
        }}.toString();
    }
}
