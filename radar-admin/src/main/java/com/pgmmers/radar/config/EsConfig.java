package com.pgmmers.radar.config;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

@Configuration
public class EsConfig {

    private static final Logger logger = LoggerFactory.getLogger(EsConfig.class);
    @Value("${elasticsearch.ip}")
    private String hostName;
    @Value("${elasticsearch.port}")
    private String port;
    @Value("${elasticsearch.cluster.name}")
    private String clusterName;
    @Value("${elasticsearch.pool-size}")
    private String poolSize;

    @Bean(name = "transportClient")
    public TransportClient transportClient() {
        logger.info("Elasticsearch初始化开始。。。。。");
        TransportClient transportClient = null;
        try {
            // 配置信息
            Settings esSetting = Settings.builder()
                    .put("cluster.name", clusterName) //集群名字
                    .put("client.transport.sniff", true)//增加嗅探机制，找到ES集群
                    .put("thread_pool.search.size", Integer.parseInt(poolSize))//增加线程池个数，暂时设为5
                    .build();
            //配置信息Settings自定义
            transportClient = new PreBuiltTransportClient(esSetting);
            TransportAddress transportAddress = new TransportAddress(InetAddress.getByName(hostName), Integer.valueOf(port));
            transportClient.addTransportAddresses(transportAddress);
        } catch (Exception e) {
            logger.error("elasticsearch TransportClient create error!!", e);
        }
        return transportClient;
    }
}