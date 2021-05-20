package com.puzhibing.investors.util;

import com.puzhibing.investors.service.IIndustryService;
import com.puzhibing.investors.service.ISecuritiesMarketService;
import com.puzhibing.investors.service.ISecuritiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * 定时任务工具类
 */
@Component
public class TaskUtil {

    @Autowired
    private IIndustryService industryService;

    @Autowired
    private ISecuritiesService securitiesService;

    @Autowired
    private ISecuritiesMarketService securitiesMarketService;


    /**
     * 每隔一分钟去处理的定时任务
     */
    @Scheduled(fixedRate = 1000 * 60)
    public void taskMinute(){
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 每天的20点执行的任务
     */
    @Scheduled(cron = "0 0 17 * * *")
    public void taskDay(){
        try {
            securitiesService.pullSecurities();
            Thread.sleep(60 * 1000);
            securitiesMarketService.pullSecuritiesMarket();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 18 * * *")
    public void taskDay1(){
        try {
            securitiesMarketService.synchronizeHistoricalData();
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    /**
     * 每月第一天的20点执行的任务
     */
    @Scheduled(cron = "0 0 20 1 * *")
    public void taskMonth(){
        try {
            industryService.pullIndustry();//行业分类
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
