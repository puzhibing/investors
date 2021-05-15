package com.puzhibing.investors.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.puzhibing.investors.dao.IndustryMapper;
import com.puzhibing.investors.pojo.Industry;
import com.puzhibing.investors.service.IIndustryService;
import com.puzhibing.investors.util.http.HttpClientUtil;
import com.puzhibing.investors.util.http.HttpResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 行业分类
 */
@Service
public class IndustryServiceImpl implements IIndustryService {

    @Resource
    private IndustryMapper industryMapper;

    @Autowired
    private HttpClientUtil httpClientUtil;


    @Override
    public void pullIndustry() throws Exception {
        String url = "http://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodes";
        HttpResult httpResult = httpClientUtil.pushHttpRequset("GET", url, null, null, "json");
        if(null == httpResult){
            System.err.println("数据请求异常");
        }
        if(httpResult.getCode() != 200){
            System.err.println(httpResult.getData());
        }
        Map<String, String> map = new HashMap<>();
        map.put("str", httpResult.getData());
        map.put("pid", String.valueOf(0));
        saveIndustry(Arrays.asList(map));
    }


    public void saveIndustry(List<Map<String, String>> s) throws Exception{
        List<Map<String, String>> ss = new ArrayList<>();
        if(s.size() == 0){
            return;
        }
        for(Map<String, String> m : s){
            String str = m.get("str");
            Integer pid = Integer.valueOf(m.get("pid"));
            JSONArray a = JSON.parseArray(str);
            String name = a.getString(0);
            String code = "";
            String b = "";
            if(a.getString(1).indexOf("[") >= 0){
                code = a.getString(3);
                b = a.getString(1);
            }
            if(a.getString(1).indexOf("[") < 0){
                code = a.getString(2);
            }
            Industry industry = industryMapper.queryByCode(code);
            if(null == industry){
                industry = new Industry();
                industry.setCode(code);
                industry.setName(name);
                industry.setPid(pid);
                industryMapper.pullIndustry(industry);
            }
            if(!"".equals(b) && b.indexOf("[") >= 0){
                JSONArray ar = JSON.parseArray(b);
                for(int i = 0; i < ar.size(); i++){
                    Map<String, String> map = new HashMap<>();
                    map.put("str", ar.getString(i));
                    map.put("pid", industry.getId().toString());
                    ss.add(map);
                }
            }
        }
        saveIndustry(ss);
    }
}
