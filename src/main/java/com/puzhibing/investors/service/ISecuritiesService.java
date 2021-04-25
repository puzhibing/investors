package com.puzhibing.investors.service;

public interface ISecuritiesService {


    /**
     * 获取证券基础数据并添加到数据库
     * @throws Exception
     */
    void pullSecurities() throws Exception;
}
