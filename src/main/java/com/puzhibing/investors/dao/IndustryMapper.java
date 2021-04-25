package com.puzhibing.investors.dao;

import com.puzhibing.investors.dao.mapper.IndustrySqlProvider;
import com.puzhibing.investors.pojo.Industry;
import org.apache.ibatis.annotations.*;


@Mapper
public interface IndustryMapper {


    /**
     * 添加数据
     */
    @SelectKey(statement = {"select last_insert_id()"}, keyProperty = "id", before = false, resultType = Integer.class)
    @InsertProvider(type = IndustrySqlProvider.class, method = "pullIndustrySql")
    Integer pullIndustry(Industry industry);


    /**
     * 使用code查询数据
     * @param code
     * @return
     */
    @SelectProvider(type = IndustrySqlProvider.class, method = "queryByCode")
    Industry queryByCode(String code);
}
