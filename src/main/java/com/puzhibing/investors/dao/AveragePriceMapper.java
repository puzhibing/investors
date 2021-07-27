package com.puzhibing.investors.dao;

import com.puzhibing.investors.dao.mapper.AveragePriceSqlProvider;
import com.puzhibing.investors.pojo.AveragePrice;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface AveragePriceMapper {


    /**
     * 添加数据
     * @param averagePrice
     * @return
     */
    @SelectKey(statement = {"select last_insert_id()"}, keyProperty = "id", before = false, resultType = Integer.class)
    @InsertProvider(type = AveragePriceSqlProvider.class, method = "insert")
    int insert(AveragePrice averagePrice);


    /**
     * 修改数据
     * @param averagePrice
     * @return
     */
    @UpdateProvider(type = AveragePriceSqlProvider.class, method = "updateById")
    int updateById(AveragePrice averagePrice);


    /**
     * 查询数据
     * @param id
     * @return
     */
    @SelectProvider(type = AveragePriceSqlProvider.class, method = "selectById")
    AveragePrice selectById(@Param("id") Integer id);


    /**
     * 根据证券数据id查询
     * @param securitiesId
     * @return
     */
    @SelectProvider(type = AveragePriceSqlProvider.class, method = "selectBySecuritiesId")
    AveragePrice selectBySecuritiesId(@Param("securitiesId") Integer securitiesId);


    /**
     * 获取推荐参考证券数据（移动平均成交数据交叉的数据）
     * @param pageNo
     * @param pageSize
     * @return
     */
//    @Select("select * from (\n" +
//            "select \n" +
//            "b.`code` as `code`,\n" +
//            "b.systemCode as systemCode,\n" +
//            "b.`name` as `name`,\n" +
//            "c.`name` as securitiesCategory,\n" +
//            "a.price as price,\n" +
//            "a.fiveAveragePrice as fiveAveragePrice,\n" +
//            "a.fifteenAveragePrice as fifteenAveragePrice,\n" +
//            "(a.fiveAveragePrice - a.price) as fiveDayDifference,\n" +
//            "(a.fifteenAveragePrice - a.price) as fifteenDayDifference\n" +
//            "from db_average_price a\n" +
//            "left join db_securities b on (a.securitiesId = b.id)\n" +
//            "left join db_securities_category c on (b.securitiesCategoryId = c.id)\n" +
//            ") as aa ORDER BY aa.fiveDayDifference, aa.fifteenDayDifference limit #{pageNo}, #{pageSize}")
    @SelectProvider(type = AveragePriceSqlProvider.class, method = "queryRecommendData")
    List<Map<String, Object>> queryRecommendData(@Param("securitiesCategoryId") Integer securitiesCategoryId, @Param("code") String code,
                                                 @Param("pageNo") Integer pageNo, @Param("pageSize") Integer pageSize);
}
