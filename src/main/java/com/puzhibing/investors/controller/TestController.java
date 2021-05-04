package com.puzhibing.investors.controller;


import com.puzhibing.investors.service.ISecuritiesMarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ISecuritiesMarketService securitiesMarketService;

    @GetMapping("/quertTestPage")
    public Object quertTestPage(){
        return "test.html";
    }
}
