package com.puzhibing.investors.controller;


import com.puzhibing.investors.service.ISecuritiesMarketService;
import com.puzhibing.investors.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ISecuritiesMarketService securitiesMarketService;

    @GetMapping("/quertTestPage")
    public Object quertTestPage(){
        return "test.html";
    }



    @ResponseBody
    @PostMapping("/queryAllData")
    public ResultUtil queryAllData(String code, Integer securitiesCategoryId, String date, Integer pageNo, Integer pageSize){
        try {
            List<Map<String, Object>> list = securitiesMarketService.queryAllData(code, securitiesCategoryId, date, pageNo, pageSize);
            return ResultUtil.success(list);
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.runErr();
        }
    }
}
