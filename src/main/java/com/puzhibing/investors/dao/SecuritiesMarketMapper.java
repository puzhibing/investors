package com.puzhibing.investors.dao;


import com.puzhibing.investors.dao.mapper.SecuritiesMarketSqlProvider;
import com.puzhibing.investors.pojo.SecuritiesMarket;
import org.apache.ibatis.annotations.*;

import java.util.Date;

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
}
