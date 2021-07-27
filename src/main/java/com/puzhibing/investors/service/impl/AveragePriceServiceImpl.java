package com.puzhibing.investors.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.puzhibing.investors.dao.AveragePriceMapper;
import com.puzhibing.investors.dao.SecuritiesMapper;
import com.puzhibing.investors.pojo.AveragePrice;
import com.puzhibing.investors.pojo.Securities;
import com.puzhibing.investors.pojo.SecuritiesCategory;
import com.puzhibing.investors.pojo.vo.MarketMovingAverageVo;
import com.puzhibing.investors.service.IAveragePriceService;
import com.puzhibing.investors.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class AveragePriceServiceImpl implements IAveragePriceService {

    @Resource
    private SecuritiesMapper securitiesMapper;

    @Resource
    private AveragePriceMapper averagePriceMapper;

    @Autowired
    private FileUtil fileUtil;




    /**
     * 保存数据
     * @param type
     * @throws Exception
     */
    @Override
    public void saveAveragePrice(String type) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.err.println(sdf.format(new Date()) + "------保存移动平均成交数据开始");
                List<Securities> securities = securitiesMapper.querySecuritiesList(null, type);
                for(Securities s : securities){
                    String value = fileUtil.read("movingAverage\\" + s.getSystemCode() + ".json");
                    JSONObject jsonObject = JSON.parseObject(value);
                    AveragePrice averagePrice = averagePriceMapper.selectBySecuritiesId(s.getId());
                    if(averagePrice == null){
                        averagePrice = new AveragePrice();
                        averagePrice.setSecuritiesId(s.getId());
                        JSONArray m_avg_0 = jsonObject.getJSONArray("m_avg_0");
                        averagePrice.setPrice(m_avg_0.getDouble(m_avg_0.size() - 1));
                        JSONArray m_avg_5 = jsonObject.getJSONArray("m_avg_5");
                        averagePrice.setFiveAveragePrice(m_avg_5.getDouble(m_avg_5.size() - 1));
                        JSONArray m_avg_15 = jsonObject.getJSONArray("m_avg_15");
                        averagePrice.setFifteenAveragePrice(m_avg_15.getDouble(m_avg_15.size() - 1));
                        averagePriceMapper.insert(averagePrice);
                    }else{
                        JSONArray m_avg_0 = jsonObject.getJSONArray("m_avg_0");
                        averagePrice.setPrice(m_avg_0.getDouble(m_avg_0.size() - 1));
                        JSONArray m_avg_5 = jsonObject.getJSONArray("m_avg_5");
                        averagePrice.setFiveAveragePrice(m_avg_5.getDouble(m_avg_5.size() - 1));
                        JSONArray m_avg_15 = jsonObject.getJSONArray("m_avg_15");
                        averagePrice.setFifteenAveragePrice(m_avg_15.getDouble(m_avg_15.size() - 1));
                        averagePriceMapper.updateById(averagePrice);
                    }
                }

                System.err.println(sdf.format(new Date()) + "------保存移动平均成交数据结束");
            }
        }).start();
    }

    /**
     * 获取推荐参考证券数据（移动平均成交数据交叉的数据）
     * @param pageNo
     * @param pageSize
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, Object>> queryRecommendData(Integer securitiesCategoryId, String code, Integer pageNo, Integer pageSize) throws Exception {
        return averagePriceMapper.queryRecommendData(securitiesCategoryId, code, pageNo, pageSize);
    }
}
