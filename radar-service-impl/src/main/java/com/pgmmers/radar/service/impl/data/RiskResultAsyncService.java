package com.pgmmers.radar.service.impl.data;

import com.pgmmers.radar.service.data.RiskResultService;
import java.util.concurrent.CompletableFuture;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * author: wangcheng Date: 2020/4/23 Time: 上午10:44 Description: 使用 线程池 发送数据
 */
@ConditionalOnProperty(prefix = "sys.conf", name = "app", havingValue = "engine")
@ConditionalOnExpression("'${sys.conf.riskResult:ThreadPool}'.equals('ThreadPool')")
@Service
public class RiskResultAsyncService implements RiskResultService {

    private static final Logger logger = LoggerFactory.getLogger(RiskResultAsyncService.class);
    private final RestHighLevelClient restHighLevelClient;

    public RiskResultAsyncService(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
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
                }
        );
    }
}
