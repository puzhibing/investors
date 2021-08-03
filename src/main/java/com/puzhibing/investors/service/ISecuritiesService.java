package com.puzhibing.investors.service;

import com.puzhibing.investors.pojo.Securities;

public interface ISecuritiesService {


    /**
     * 获取证券基础数据并添加到数据库
     * @throws Exception
     */
    void pullSecurities() throws Exception;


    /**
     * 根据系统编号查询数据
     * @param systemCode
     * @return
     * @throws Exception
     */
    Securities querySystemCode(String systemCode) throws Exception;


    /**
     * 修改数据
     * @param securities
     * @throws Exception
     */
    void updateById(Securities securities) throws Exception;
}
