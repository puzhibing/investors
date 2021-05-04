package com.puzhibing.investors.dao;


import com.puzhibing.investors.dao.mapper.SecuritiesMarketSqlProvider;
import com.puzhibing.investors.pojo.SecuritiesMarket;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface SecuritiesMarketMapper {


    /**
     * 添加数据
     * @param securitiesMarket
     */
    @SelectKey(statement = {"select last_insert_id()"}, keyProperty = "id", before = false, resultType = Integer.class)
    @InsertProvider(type = SecuritiesMarketSqlProvider.class, method = "insert")
    void insert(SecuritiesMarket securitiesMarket);


    /**
     * 根据证券id和行情日期查询数据
     * @param securitiesId
     * @param date
     * @return
     */
    @SelectProvider(type = SecuritiesMarketSqlProvider.class, method = "queryBySecuritiesIdAndDate")
    SecuritiesMarket queryBySecuritiesIdAndDate(@Param("securitiesId") Integer securitiesId, @Param("date") Date date);


    /**
     * 获取时间范围内的数据
     * @param securitiesId
     * @param start
     * @param end
     * @return
     */
    @SelectProvider(type = SecuritiesMarketSqlProvider.class, method = "queryList")
    List<SecuritiesMarket> queryList(@Param("securitiesId") Integer securitiesId, @Param("start") Date start,
                                     @Param("end") Date end);


    /**
     * 获取平均成交金额
     * @param securitiesId
     * @return
     */
    @Select("select avg(closingPrice) from db_securities_market where securitiesId = #{securitiesId}")
    Double queryClosingPriceAvg(@Param("securitiesId") Integer securitiesId);

}
