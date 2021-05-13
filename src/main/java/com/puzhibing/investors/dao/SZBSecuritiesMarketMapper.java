package com.puzhibing.investors.dao;


import com.puzhibing.investors.dao.mapper.SZBSecuritiesMarketSqlProvider;
import com.puzhibing.investors.pojo.SZBSecuritiesMarket;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface SZBSecuritiesMarketMapper {


    /**
     * 添加数据
     * @param szbSecuritiesMarket
     */
    @SelectKey(statement = {"select last_insert_id()"}, keyProperty = "id", before = false, resultType = Integer.class)
    @InsertProvider(type = SZBSecuritiesMarketSqlProvider.class, method = "insert")
    void insert(SZBSecuritiesMarket szbSecuritiesMarket);


    /**
     * 根据证券id和行情日期查询数据
     * @param securitiesId
     * @param date
     * @return
     */
    @SelectProvider(type = SZBSecuritiesMarketSqlProvider.class, method = "queryBySecuritiesIdAndDate")
    SZBSecuritiesMarket queryBySecuritiesIdAndDate(@Param("securitiesId") Integer securitiesId, @Param("date") Date date);


    /**
     * 获取时间范围内的数据
     * @param securitiesId
     * @param start
     * @param end
     * @return
     */
    @SelectProvider(type = SZBSecuritiesMarketSqlProvider.class, method = "queryList")
    List<SZBSecuritiesMarket> queryList(@Param("securitiesId") Integer securitiesId, @Param("start") Date start,
                                     @Param("end") Date end);


    /**
     * 获取平均成交金额
     * @param securitiesId
     * @return
     */
    @Select("select avg(closingPrice) from db_sz_b_securities_market where securitiesId = #{securitiesId}")
    Double queryClosingPriceAvg(@Param("securitiesId") Integer securitiesId);

}
