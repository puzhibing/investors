package com.puzhibing.investors.util;

import com.puzhibing.investors.pojo.Securities;

import java.util.*;

/**
 * 缓存工具类
 */
public class CacheUtil {

    public static Map<String, String> markets = new HashMap<>();

    public static Set<Integer> securitiesIds = new HashSet<>();

    public static Map<String, Thread> threads = new HashMap<>();

    public static List<Securities> securities = new ArrayList<>();
}
