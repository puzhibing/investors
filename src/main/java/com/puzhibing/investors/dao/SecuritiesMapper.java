package com.puzhibing.investors.dao;


import com.puzhibing.investors.dao.mapper.SecuritiesSqlProvider;
import com.puzhibing.investors.pojo.Securities;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SecuritiesMapper {


    /**
     * 添加数据
     * @param securities
     */
    @SelectKey(statement = {"select last_insert_id()"}, keyProperty = "id", before = false, resultType = Integer.class)
    @InsertProvider(type = SecuritiesSqlProvider.class, method = "insert")
    void insert(Securities securities);


    /**
     * 根据证券编号和类型id获取数据
     * @param code
     * @param securitiesCategoryId
     * @return
     */
    @SelectProvider(type = SecuritiesSqlProvider.class, method = "queryByCodeAndSecuritiesCategory")
    Securities queryByCodeAndSecuritiesCategory(@Param("code") String code, @Param("securitiesCategoryId") Integer securitiesCategoryId);


    /**
     * 根据证券编号和类型id获取数据
     * @param code
     * @param securitiesCategoryId
     * @return
     */
    @SelectProvider(type = SecuritiesSqlProvider.class, method = "queryList")
    List<Securities> queryList(@Param("code") String code, @Param("securitiesCategoryId") Integer securitiesCategoryId,
                               @Param("pageNo") Integer pageNo, @Param("pageSize") Integer pageSize);


    /**
     * 查询数据列表
     * @param code
     * @param securitiesCategoryCode
     * @return
     */
    @SelectProvider(type = SecuritiesSqlProvider.class, method = "querySecuritiesList")
    List<Securities> querySecuritiesList(@Param("code") String code, @Param("securitiesCategoryCode") String securitiesCategoryCode);


    /**
     * 根据id获取数据
     * @param id
     * @return
     */
    @SelectProvider(type = SecuritiesSqlProvider.class, method = "selectById")
    Securities selectById(@Param("id") Integer id);


    /**
     * 修改数据
     * @param securities
     * @return
     */
    @UpdateProvider(type = SecuritiesSqlProvider.class, method = "updateById")
    void updateById(Securities securities);


    /**
     * 根据系统编号获取数据
     * @param systemCode
     * @return
     */
    @SelectProvider(type = SecuritiesSqlProvider.class, method = "querySystemCode")
    Securities querySystemCode(String systemCode);
}
