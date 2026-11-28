package com.pgmmers.radar.service.impl.data;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.pgmmers.radar.service.data.RiskResultService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * author: wangcheng Date: 2020/4/23 Time: 上午10:44 Description: 使用 线程池 发送数据
 * 单体融合后：事件结果写入使用独立的 riskResultExecutor 线程池（见 AsyncConfig），
 * 避免与管理端请求互相争抢。v5.2 ES 8 升级：RestHighLevelClient → ElasticsearchClient。
 */
@Service
public class RiskResultAsyncService implements RiskResultService {

    private static final Logger logger = LoggerFactory.getLogger(RiskResultAsyncService.class);
    private final ElasticsearchClient elasticsearchClient;
    private final Executor riskResultExecutor;

    public RiskResultAsyncService(ElasticsearchClient elasticsearchClient,
            @Qualifier("riskResultExecutor") Executor riskResultExecutor) {
        this.elasticsearchClient = elasticsearchClient;
        this.riskResultExecutor = riskResultExecutor;
        logger.info("RiskResult SaveModel ThreadPool");
    }

    @Override
    public void sendResult(String scenarioGuid, String reqId, String info) {
        String index = scenarioGuid.toLowerCase();
        CompletableFuture.runAsync(() -> {
                    try {
                        var response = elasticsearchClient.index(i -> i
                                .index(index)
                                .id(reqId)
                                .withJson(new java.io.ByteArrayInputStream(
                                        info.getBytes(java.nio.charset.StandardCharsets.UTF_8))));
                        logger.info("es result:{}", response.result());
                    } catch (Exception e) {
                        logger.error("es result error, index={}, reqId={}", index, reqId, e);
                    }
                }, riskResultExecutor
        );
    }
}
