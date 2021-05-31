package com.puzhibing.investors.api;

import com.puzhibing.investors.service.ISecuritiesMarketService;
import com.puzhibing.investors.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 交易数据
 */
@RestController
@CrossOrigin
@RequestMapping("/securitiesMarket")
public class SecuritiesMarketController {

    @Autowired
    private ISecuritiesMarketService securitiesMarketService;


    /**
     * 同步历史交易数据
     * @return
     */
    @ResponseBody
    @PostMapping("/synchronizeHistoricalData")
    public ResultUtil synchronizeHistoricalData(){
        try {
            return securitiesMarketService.synchronizeHistoricalData();
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.runErr();
        }
    }
}
