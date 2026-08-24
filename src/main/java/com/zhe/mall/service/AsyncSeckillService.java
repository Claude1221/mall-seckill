package com.zhe.mall.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AsyncSeckillService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.master.url}")
    private String masterUrl;

    @Async
    public void createAppointment(Long roomId, Long userId) {
        log.info("秒杀成功，开始异步创建预约，roomId={}, userId={}", roomId, userId);
        try {
            String url = masterUrl + "/api/seckill/callback?roomId=" + roomId + "&userId=" + userId;
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("预约创建成功，响应：{}", response.getBody());
            } else {
                log.error("预约创建失败，状态码：{}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("异步回调主服务失败", e);
        }
    }
}