package com.puzhibing.investors.util;

import com.puzhibing.investors.service.ISecuritiesMarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * 系统监听器
 */
@WebListener
@Component
public class ServerStartListener implements ServletContextListener {

    @Autowired
    private ISecuritiesMarketService securitiesMarketService;

    /**
     * 系统初始化后执行
     * @param sce
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            /**
             * 初始化数据到缓存中
             */
            securitiesMarketService.initMarketToCache();
            securitiesMarketService.checkHistoricalMarketData(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
