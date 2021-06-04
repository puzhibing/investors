package com.puzhibing.investors.util;

import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtil {

    public int YEAR;//年

    public int QUARTER;//季度

    public int MONTH;//月

    public int WEEK;//周

    public int WEEK_OF_MONTH;//月中的几周

    public int DAY;//天

    public int HOUR;//小时

    public int MINUTE;//分钟

    public int SECOND;//秒


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
