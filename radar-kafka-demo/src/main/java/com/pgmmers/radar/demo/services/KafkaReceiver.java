package com.pgmmers.radar.demo.services;


import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Optional;

/**
 * kafka 消息来源， 调用 radar engine 事例。
 *
 * @author feihu.wang
 */
@Service
public class KafkaReceiver {

    private static Logger logger = LoggerFactory.getLogger(KafkaReceiver.class);

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${sys.conf.engine-url}")
    private String engineUrl;

    @KafkaListener(topics = {"trans-event"})
    public void listen(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            logger.info("recv data:{}", message);
            invokeEngine(message.toString());
        }

    }

    /**
     * send to radar engine
     *
     * @param jsonInfo
     */
    private void invokeEngine(String jsonInfo) {
        String modelGuid = "DB8A078F-97FE-4A7F-AAC0-5AF1A6C36CE8";
        String reqId = System.currentTimeMillis() + "";
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("modelGuid", modelGuid);
        params.add("reqId", reqId);
        params.add("jsonInfo", jsonInfo);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<JSONObject> resp = restTemplate.exchange(engineUrl, HttpMethod.POST, entity, JSONObject.class);
        logger.info("risk result:status={},body={}", resp.getStatusCode(), resp.getBody());
        // now, you can save result to redis or send to kafka etc
    }

    /**
     * generator demo data.
     *
     * @author feihu.wang
     */
    @PostConstruct
    public void dataGenerator() {
        new Thread(() -> {
            int eventId;
            String jsonInfoTemp = "{\n" +
                    "        \"eventId\":\"#eventId#\", \"mobile\":\"18516249909\", \"userId\":\"18516249909\",\n" +
                    "        \"eventTime\":#evnetTime#, \"userIP\":\"180.175.166.148\", \n" +
                    "        \"deviceId\": \"SDKSKDSLD-ASDFA-32348235\", \"os\":\"ios\",\"amount\":500.0,\n" +
                    "        \"channel\":\"alipay\"\n" +
                    "        } ";
            for (int i = 1; i <= 10; i++) {
                eventId = 1000 + i;
                Long eventTime = System.currentTimeMillis() - 1000000;
                String jsonInfo = jsonInfoTemp.replace("#eventId#", String.valueOf(eventId)).replace("#evnetTime#", eventTime.toString());
                logger.info("send demo data:{}", jsonInfo);
                kafkaTemplate.send("trans-event", jsonInfo);
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
