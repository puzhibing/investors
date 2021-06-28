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
@RequestMapping("/securitiesMarket")
public class SecuritiesMarketController {

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
     * 计算移动平均数据
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
