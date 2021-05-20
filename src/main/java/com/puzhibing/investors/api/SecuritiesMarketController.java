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


}
