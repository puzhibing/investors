package com.puzhibing.investors;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling//开启定时任务
@SpringBootApplication
@MapperScan("com.puzhibing.investors.dao")//扫描mapper
public class InvestorsApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvestorsApplication.class, args);
    }

}
