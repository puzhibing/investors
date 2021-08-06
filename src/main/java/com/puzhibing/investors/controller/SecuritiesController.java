package com.puzhibing.investors.controller;


import com.puzhibing.investors.pojo.Securities;
import com.puzhibing.investors.pojo.SecuritiesCategory;
import com.puzhibing.investors.pojo.vo.MarketMovingAverageVo;
import com.puzhibing.investors.service.ISecuritiesCategoryService;
import com.puzhibing.investors.service.ISecuritiesMarketService;
import com.puzhibing.investors.service.ISecuritiesService;
import com.puzhibing.investors.util.ResultUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
@RequestMapping("/securities")
public class SecuritiesController {

    @Autowired
    private ISecuritiesMarketService securitiesMarketService;

    @Autowired
    private ISecuritiesCategoryService securitiesCategoryService;

    @Autowired
    private ISecuritiesService securitiesService;




    /**
     * 跳转到列表页
     * @return
     */
    @GetMapping("")
    public Object showSecuritiesList(){
        return "securities.html";
    }


    /**
     * 跳转到行情分析页
     * @param code
     * @return
     */
    @GetMapping("/showSecuritiesMarket/{code}")
    public Object showSecuritiesMarket(@PathVariable("code") String code){
        return "securitiesMarket.html";
    }


    /**
     * 获取行情数据
     * @param code
     * @return
     */
    @ResponseBody
    @PostMapping("/queryMarket")
    public ResultUtil queryMarket(Integer type, String code){
        try {
            Map<String, Object> map = securitiesMarketService.queryMarkt(type, code);
            return ResultUtil.success(map);
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.runErr();
        }
    }




    /**
     * 获取势能数据
     * @param code
     * @return
     */
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
     * 获取势能数据
     * @param code
     * @return
     */
    @ResponseBody
    @PostMapping("/queryPotentialEnergy_")
    public ResultUtil queryPotentialEnergy_(String code){
        try {
            List<Map<String, Object>> list = securitiesMarketService.queryPotentialEnergy_(code);
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
    @PostMapping("/queryRecommendData")
    public ResultUtil queryRecommendData(Integer securitiesCategoryId, String code, Integer pageNo, Integer pageSize){
        try {
            List<MarketMovingAverageVo> list = securitiesMarketService.queryRecommendData(securitiesCategoryId, code, pageNo, pageSize);
            return ResultUtil.success(list);
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.runErr();
        }
    }

    /**
     * 获取所有证券分类数据
     * @return
     */
    @ResponseBody
    @PostMapping("/querySecuritiesCategory")
    public ResultUtil querySecuritiesCategory(){
        try {
            List<SecuritiesCategory> securitiesCategories = securitiesCategoryService.selectList();
            return ResultUtil.success(securitiesCategories);
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.tokenErr();
        }
    }


    /**
     * 修改关注状态
     * @param systemCode
     * @param follow
     * @return
     */
    @ResponseBody
    @PostMapping("/updateFollow")
    public ResultUtil updateFollow(String systemCode, Integer follow){
        try {
            Securities securities = securitiesService.querySystemCode(systemCode);
            if(null == securities){
                return ResultUtil.error("无效的数据");
            }
            securities.setFollow(follow);
            securitiesService.updateById(securities);
            return ResultUtil.success();
        }catch (Exception e){
            e.printStackTrace();
            return ResultUtil.runErr();
        }
    }
}
