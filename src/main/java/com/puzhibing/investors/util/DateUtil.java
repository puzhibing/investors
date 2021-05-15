package com.puzhibing.investors.util;

import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtil {

    public Integer YEAR;//年

    public Integer QUARTER;//季度

    public Integer MONTH;//月

    public Integer WEEK;//周

    public Integer WEEK_OF_MONTH;//月中的几周

    public Integer DAY;//天

    public Integer HOUR;//小时

    public Integer MINUTE;//分钟

    public Integer SECOND;//秒


    /**
     * 初始化日期
     * @param date
     */
    public static DateUtil createDate(Date date){
        if(null == date){
            date = new Date();
        }
        DateUtil dateUtil = new DateUtil();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        dateUtil.YEAR = calendar.get(calendar.YEAR);
        dateUtil.MONTH = calendar.get(calendar.MONTH) + 1;
        dateUtil.QUARTER = Double.valueOf((dateUtil.MONTH + 2) / 3).intValue();
        dateUtil.WEEK = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        dateUtil.DAY = calendar.get(Calendar.DAY_OF_MONTH);
        dateUtil.HOUR = calendar.get(Calendar.HOUR_OF_DAY);
        dateUtil.MINUTE = calendar.get(Calendar.MINUTE);
        dateUtil.SECOND = calendar.get(Calendar.SECOND);
        dateUtil.WEEK_OF_MONTH = calendar.get(Calendar.WEEK_OF_MONTH);
        return dateUtil;
    }

}
