package com.pgmmers.radar.config;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class EsConfig {

    private static final Logger logger = LoggerFactory.getLogger(EsConfig.class);

    @Value("${elasticsearch.username}")
    private String username;
    @Value("${elasticsearch.password}")
    private String password;
    @Value("${elasticsearch.url}")
    private String url;


    @Bean
    public RestHighLevelClient esClient(){
        if(StringUtils.isEmpty(this.username)){
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(HttpHost.create(this.url)));
            return client;
        }

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));  //es账号密码（默认用户名为elastic）
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(HttpHost.create(this.url))
                        .setHttpClientConfigCallback(httpAsyncClientBuilder -> {
                            httpAsyncClientBuilder.disableAuthCaching();
                            return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                        })
        );
        return client;
    }

}