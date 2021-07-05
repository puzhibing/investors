package com.puzhibing.investors.controller;


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
    public Object quertTestPage(){
        return "test.html";
    }



    @ResponseBody
    @PostMapping("/queryAllData")
    public ResultUtil queryAllData(String code, Integer securitiesCategoryId, Integer pageNo, Integer pageSize){
        try {
            List<Map<String, Object>> list = securitiesMarketService.queryAllData(code, securitiesCategoryId, pageNo, pageSize);
            return ResultUtil.success(list);
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.runErr();
        }
    }


    @ResponseBody
    @PostMapping("/profitAndLossOfQuantitative")
    public ResultUtil profitAndLossOfQuantitative(String code, Integer securitiesCategoryId, String date, Integer pageNo, Integer pageSize){
        try {
            List<Map<String, Object>> list = securitiesMarketService.profitAndLossOfQuantitative(code, securitiesCategoryId, date, pageNo, pageSize);
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
}
