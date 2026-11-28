/*
 * Copyright (c) 2019 WangFeiHu
 *  Radar is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *  http://license.coscl.org.cn/MulanPSL2
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 */

package com.pgmmers.radar.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * ES 8.x 客户端装配（v5.2 ES 8 升级）：低层 {@link RestClient}（连接/认证/超时）+
 * {@link ElasticsearchClient}（elasticsearch-java 类型安全 DSL）。
 * 原则：仅事件检索存储接入 ES，服务端 8.x 默认开启安全（TLS+认证），自建环境
 * 显式关闭 security 后本配置无需凭证即可工作；生产带密码场景走 username/password 分支。
 */
@Configuration
public class EsConfig {

    private static final Logger logger = LoggerFactory.getLogger(EsConfig.class);

    @Value("${elasticsearch.username}")
    private String username;
    @Value("${elasticsearch.password}")
    private String password;
    @Value("${elasticsearch.url}")
    private String url;

    /** 低层 REST 客户端（连接/认证/超时），供 ElasticsearchClient 传输层复用。 */
    @Bean
    public RestClient esRestClient() {
        RestClientBuilder builder = RestClient.builder(HttpHost.create(this.url))
                // 原配置缺失超时，v5.2 顺手补齐（连接 5s / 请求 30s）
                .setRequestConfigCallback(requestConfig -> requestConfig
                        .setConnectTimeout(5_000)
                        .setSocketTimeout(30_000));
        if (StringUtils.isNotEmpty(this.username)) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password));
            builder.setHttpClientConfigCallback(httpAsyncClientBuilder -> {
                httpAsyncClientBuilder.disableAuthCaching();
                return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            });
        }
        logger.info("es rest client initialized: {}", this.url);
        return builder.build();
    }

    /** 高层客户端：Jackson 序列化（复用 Spring 已有 jackson-databind）。 */
    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient esRestClient) {
        return new ElasticsearchClient(new RestClientTransport(esRestClient, new JacksonJsonpMapper()));
    }
}
