package com.pgmmers.radar.service.impl.data;

import com.pgmmers.radar.service.data.RiskResultService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * author: wangcheng Date: 2020/4/23 Time: 上午10:44 Description: 使用 线程池 发送数据
 * 单体融合后：事件结果写入使用独立的 riskResultExecutor 线程池（见 AsyncConfig），
 * 避免与管理端请求互相争抢。
 */
@Service
public class RiskResultAsyncService implements RiskResultService {

    private static final Logger logger = LoggerFactory.getLogger(RiskResultAsyncService.class);
    private final RestHighLevelClient restHighLevelClient;
    private final Executor riskResultExecutor;

    public RiskResultAsyncService(RestHighLevelClient restHighLevelClient,
            @Qualifier("riskResultExecutor") Executor riskResultExecutor) {
        this.restHighLevelClient = restHighLevelClient;
        this.riskResultExecutor = riskResultExecutor;
        logger.info("RiskResult SaveModel ThreadPool");
    }

    @Override
    public void sendResult(String modelGuid, String reqId, String info) {
        CompletableFuture.runAsync(() -> {
                    try {
                        IndexRequest request = new IndexRequest(modelGuid.toLowerCase());
                        request.id(reqId);
                        request.source(info, XContentType.JSON);
                        IndexResponse result = restHighLevelClient.index(request, RequestOptions.DEFAULT);
                        logger.info("es result:{}", result.toString());
                    } catch (Exception e) {
                        logger.error("es result error");
                    }
                }, riskResultExecutor
        );
    }
}
