package com.pgmmers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.pgmmers.radar.mapper")
public class AdminApplication
{
    public static void main( String[] args ){
        SpringApplication.run(AdminApplication.class, args);
    }
}
