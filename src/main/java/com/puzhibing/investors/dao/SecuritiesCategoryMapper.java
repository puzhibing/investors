package com.puzhibing.investors.dao;


import com.puzhibing.investors.dao.mapper.SecuritiesCategorySqlProvider;
import com.puzhibing.investors.pojo.SecuritiesCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

@Mapper
public interface SecuritiesCategoryMapper {


    /**
     * 根据编号获取证券类型
     * @param code
     * @return
     */
    @SelectProvider(type = SecuritiesCategorySqlProvider.class, method = "queryByCode")
    SecuritiesCategory queryByCode(@Param("code") String code);


    /**
     * 根据id查询数据
     * @param id
     * @return
     */
    @SelectProvider(type = SecuritiesCategorySqlProvider.class, method = "selectById")
    SecuritiesCategory selectById(@Param("id") Integer id);


    /**
     * 获取所有数据
     * @return
     */
    @SelectProvider(type = SecuritiesCategorySqlProvider.class, method = "selectList")
    List<SecuritiesCategory> selectList();
}
