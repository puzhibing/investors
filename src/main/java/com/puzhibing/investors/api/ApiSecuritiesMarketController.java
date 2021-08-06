package com.puzhibing.investors.api;

import com.puzhibing.investors.service.ISecuritiesMarketService;
import com.puzhibing.investors.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 交易数据
 */
@CrossOrigin
@RestController
@RequestMapping("/apiSecuritiesMarket")
public class ApiSecuritiesMarketController {

    @Autowired
    private ISecuritiesMarketService securitiesMarketService;


    /**
     * 同步历史交易数据
     * @return
     */
    @ResponseBody
    @GetMapping("/synchronizeHistoricalData")
    public ResultUtil synchronizeHistoricalData(Integer base){
        try {
            return securitiesMarketService.synchronizeHistoricalData(base);
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.runErr();
        }
    }


    /**
     * 计算移动平均成交数据
     * @return
     */
    @ResponseBody
    @GetMapping("/calculateMovingAverage")
    public ResultUtil calculateMovingAverage(){
        try {
            securitiesMarketService.calculateMovingAverage("sh_a");
            securitiesMarketService.calculateMovingAverage("sh_b");
            securitiesMarketService.calculateMovingAverage("sz_a");
            securitiesMarketService.calculateMovingAverage("sz_b");
            return ResultUtil.success();
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.runErr();
        }
    }

    /**
     * 计算移动平均势能数据
     * @return
     */
    @ResponseBody
    @GetMapping("/potentialEnergyMovingAverage")
    public ResultUtil potentialEnergyMovingAverage(){
        try {
            securitiesMarketService.potentialEnergyMovingAverage("sh_a");
            securitiesMarketService.potentialEnergyMovingAverage("sh_b");
            securitiesMarketService.potentialEnergyMovingAverage("sz_a");
            securitiesMarketService.potentialEnergyMovingAverage("sz_b");
            return ResultUtil.success();
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.runErr();
        }
    }


    /**
     * 计算周日纬度行情均价数据
     * @return
     */
    @ResponseBody
    @GetMapping("/weekMovingAverage")
    public ResultUtil weekMovingAverage(){
        try {
            securitiesMarketService.weekMovingAverage("sh_a");
            securitiesMarketService.weekMovingAverage("sh_b");
            securitiesMarketService.weekMovingAverage("sz_a");
            securitiesMarketService.weekMovingAverage("sz_b");

            securitiesMarketService.monthMovingAverage("sh_a");
            securitiesMarketService.monthMovingAverage("sh_b");
            securitiesMarketService.monthMovingAverage("sz_a");
            securitiesMarketService.monthMovingAverage("sz_b");

            securitiesMarketService.quarterMovingAverage("sh_a");
            securitiesMarketService.quarterMovingAverage("sh_b");
            securitiesMarketService.quarterMovingAverage("sz_a");
            securitiesMarketService.quarterMovingAverage("sz_b");

            securitiesMarketService.yearMovingAverage("sh_a");
            securitiesMarketService.yearMovingAverage("sh_b");
            securitiesMarketService.yearMovingAverage("sz_a");
            securitiesMarketService.yearMovingAverage("sz_b");
            return ResultUtil.success();
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.runErr();
        }
    }


    /**
     * 获取当日行情数据
     * @return
     */
    @ResponseBody
    @GetMapping("/pullSecuritiesMarket")
    public ResultUtil pullSecuritiesMarket(){
        try {
            securitiesMarketService.pullSecuritiesMarket();
            return ResultUtil.success();
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.runErr();
        }
    }
}
