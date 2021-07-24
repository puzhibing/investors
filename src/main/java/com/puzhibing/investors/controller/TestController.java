package com.puzhibing.investors.controller;


import com.puzhibing.investors.pojo.vo.MarketMovingAverageVo;
import com.puzhibing.investors.service.ISecuritiesMarketService;
import com.puzhibing.investors.util.ResultUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ISecuritiesMarketService securitiesMarketService;

    @GetMapping("")
    public Object queryTestPage(){
        return "test.html";
    }


    @GetMapping("/info")
    public Object queryInfoPage(){
        return "info.html";
    }



    @ResponseBody
    @PostMapping("/queryMarkt")
    public ResultUtil queryMarkt(String code){
        try {
            Map<String, Object> map = securitiesMarketService.queryMarkt(code);
            return ResultUtil.success(map);
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.runErr();
        }
    }


    @ResponseBody
    @PostMapping("/queryPotentialEnergy")
    public ResultUtil queryPotentialEnergy(String code){
        try {
            List<Map<String, Object>> list = securitiesMarketService.queryPotentialEnergy(code);
            return ResultUtil.success(list);
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.runErr();
        }
    }


    /**
     * 下载数据
     * @param systemCode
     * @param response
     */
    @ResponseBody
    @GetMapping("/exportMarket")
    public void exportMarket(String systemCode, HttpServletResponse response){
        try {
            HSSFWorkbook hssfWorkbook = securitiesMarketService.exportMarket(systemCode);
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(systemCode + ".xls", "utf-8"));
            response.setContentType("application/vnd.ms-excel");
            ServletOutputStream out = response.getOutputStream();
            hssfWorkbook.write(out);
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取推荐证券数据
     * @return
     */
    @ResponseBody
    @GetMapping("/queryRecommendData")
    public ResultUtil queryRecommendData(){
        try {
            List<MarketMovingAverageVo> list = securitiesMarketService.queryRecommendData(1, 10);
            return ResultUtil.success(list);
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.runErr();
        }
    }
}
