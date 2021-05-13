package com.puzhibing.investors.service;

import com.puzhibing.investors.pojo.SecuritiesCategory;

public interface ISecuritiesCategoryService {


    /**
     * 根据编号获取证券类型
     * @param code
     * @return
     * @throws Exception
     */
    SecuritiesCategory queryByCode(String code) throws Exception;


    /**
     * 根据id查询数据
     * @param id
     * @return
     * @throws Exception
     */
    SecuritiesCategory selectById(Integer id) throws Exception;
}
