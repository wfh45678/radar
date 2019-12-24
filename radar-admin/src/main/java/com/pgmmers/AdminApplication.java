package com.pgmmers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class, RestClientAutoConfiguration.class})
@MapperScan("com.pgmmers.radar.mapper")
public class AdminApplication
{
    public static void main( String[] args ){
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        SpringApplication.run(AdminApplication.class, args);
    }
}
