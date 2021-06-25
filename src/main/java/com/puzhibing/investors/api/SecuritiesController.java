package com.puzhibing.investors.api;


import com.puzhibing.investors.service.ISecuritiesService;
import com.puzhibing.investors.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/securities")
public class SecuritiesController {

    @Autowired
    private ISecuritiesService securitiesService;


    @ResponseBody
    @GetMapping("/pullSecurities")
    public ResultUtil pullSecurities(){
        try {
            securitiesService.pullSecurities();
            return ResultUtil.success();
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.runErr();
        }
    }
}
