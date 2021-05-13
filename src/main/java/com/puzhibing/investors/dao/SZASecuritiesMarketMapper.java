package com.puzhibing.investors.dao;


import com.puzhibing.investors.dao.mapper.SZASecuritiesMarketSqlProvider;
import com.puzhibing.investors.pojo.SZASecuritiesMarket;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface SZASecuritiesMarketMapper {


    /**
     * 添加数据
     * @param szaSecuritiesMarket
     */
    @SelectKey(statement = {"select last_insert_id()"}, keyProperty = "id", before = false, resultType = Integer.class)
    @InsertProvider(type = SZASecuritiesMarketSqlProvider.class, method = "insert")
    void insert(SZASecuritiesMarket szaSecuritiesMarket);


    /**
     * 根据证券id和行情日期查询数据
     * @param securitiesId
     * @param date
     * @return
     */
    @SelectProvider(type = SZASecuritiesMarketSqlProvider.class, method = "queryBySecuritiesIdAndDate")
    SZASecuritiesMarket queryBySecuritiesIdAndDate(@Param("securitiesId") Integer securitiesId, @Param("date") Date date);


    /**
     * 获取时间范围内的数据
     * @param securitiesId
     * @param start
     * @param end
     * @return
     */
    @SelectProvider(type = SZASecuritiesMarketSqlProvider.class, method = "queryList")
    List<SZASecuritiesMarket> queryList(@Param("securitiesId") Integer securitiesId, @Param("start") Date start,
                                     @Param("end") Date end);


    /**
     * 获取平均成交金额
     * @param securitiesId
     * @return
     */
    @Select("select avg(closingPrice) from db_sz_a_securities_market where securitiesId = #{securitiesId}")
    Double queryClosingPriceAvg(@Param("securitiesId") Integer securitiesId);

}
