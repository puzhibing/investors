package com.puzhibing.investors.dao;


import com.puzhibing.investors.dao.mapper.SHBSecuritiesMarketSqlProvider;
import com.puzhibing.investors.pojo.SHBSecuritiesMarket;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface SHBSecuritiesMarketMapper {


    /**
     * 添加数据
     * @param shbSecuritiesMarket
     */
    @SelectKey(statement = {"select last_insert_id()"}, keyProperty = "id", before = false, resultType = Integer.class)
    @InsertProvider(type = SHBSecuritiesMarketSqlProvider.class, method = "insert")
    void insert(SHBSecuritiesMarket shbSecuritiesMarket);


    /**
     * 根据证券id和行情日期查询数据
     * @param securitiesId
     * @param date
     * @return
     */
    @SelectProvider(type = SHBSecuritiesMarketSqlProvider.class, method = "queryBySecuritiesIdAndDate")
    SHBSecuritiesMarket queryBySecuritiesIdAndDate(@Param("securitiesId") Integer securitiesId, @Param("date") Date date);


    /**
     * 获取时间范围内的数据
     * @param securitiesId
     * @param start
     * @param end
     * @return
     */
    @SelectProvider(type = SHBSecuritiesMarketSqlProvider.class, method = "queryList")
    List<SHBSecuritiesMarket> queryList(@Param("securitiesId") Integer securitiesId, @Param("start") Date start,
                                     @Param("end") Date end);


    /**
     * 获取平均成交金额
     * @param securitiesId
     * @return
     */
    @Select("select avg(closingPrice) from db_sh_b_securities_market where securitiesId = #{securitiesId}")
    Double queryClosingPriceAvg(@Param("securitiesId") Integer securitiesId);



    /**
     * 修改数据
     * @param shbSecuritiesMarket
     */
    @UpdateProvider(type = SHBSecuritiesMarketSqlProvider.class, method = "update")
    void update(SHBSecuritiesMarket shbSecuritiesMarket);

}
