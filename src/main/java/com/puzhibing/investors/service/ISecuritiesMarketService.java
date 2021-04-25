package com.puzhibing.investors.service;

public interface ISecuritiesMarketService {


    /**
     * 获取并添加证券日行情数据
     * @throws Exception
     */
    void pullSecuritiesMarket() throws Exception;
}
