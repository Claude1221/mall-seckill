package com.zhe.mall.service.impl;

import com.zhe.mall.service.AsyncSeckillService;
import com.zhe.mall.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AsyncSeckillService asyncSeckillService;

    @Autowired
    private DefaultRedisScript<Long> seckillScript;


    @Override
    public int grab(Long roomId, Long userId) {
        String stockKey = "seckill:room:" + roomId;
        String infoKey = "seckill:info:" + roomId;
        long now = System.currentTimeMillis()/1000;
        log.info("成功抓取");
        Long result = redisTemplate.execute(
                seckillScript,
                Arrays.asList(stockKey, infoKey),
                String.valueOf(now)
        );

        if (result == null) {
            return 0;
        }
        if (result == 1) {
            log.info("用户：{} 秒杀成功，房间号：{}", userId, roomId);
            asyncSeckillService.createAppointment(roomId, userId);
            return 1;
        }
        log.info("Lua脚本返回结果: {}", result);
        return result.intValue();
    }
}
