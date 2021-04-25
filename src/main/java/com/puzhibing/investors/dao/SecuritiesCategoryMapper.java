package com.puzhibing.investors.dao;


import com.puzhibing.investors.dao.mapper.SecuritiesCategorySqlProvider;
import com.puzhibing.investors.pojo.SecuritiesCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

@Mapper
public interface SecuritiesCategoryMapper {


    /**
     * 根据编号获取证券类型
     * @param code
     * @return
     */
    @SelectProvider(type = SecuritiesCategorySqlProvider.class, method = "queryByCode")
    SecuritiesCategory queryByCode(@Param("code") String code);
}
