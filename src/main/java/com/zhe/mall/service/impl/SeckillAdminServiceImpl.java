package com.zhe.mall.service.impl;

import com.zhe.mall.common.Result;
import com.zhe.mall.dto.SeckillCreateDTO;
import com.zhe.mall.entity.SeckillActivity;
import com.zhe.mall.mapper.SeckillActivityMapper;
import com.zhe.mall.service.SeckillAdminService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SeckillAdminServiceImpl implements SeckillAdminService {

    @Autowired
    private SeckillActivityMapper seckillActivityMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public int batchCreate(SeckillCreateDTO dto) {

        // 清空 seckill_activity 表的所有数据
        seckillActivityMapper.delete(null);
        
        // 同时清理 Redis 中的所有秒杀缓存
        java.util.Set<String> keys = redisTemplate.keys("seckill:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        //  插入秒杀活动表，预热 Redis
        List<SeckillActivity> activities = new ArrayList<>();
        for (Long roomId : dto.getRoomIds()) {
            SeckillActivity activity = new SeckillActivity();
            activity.setRoomId(roomId);
            activity.setSeckillPrice(dto.getSeckillPrice());
            activity.setStartTime(dto.getStartTime());
            activity.setEndTime(dto.getEndTime());
            activity.setStatus(1); // 1-进行中
            activities.add(activity);
        }
        for (SeckillActivity activity : activities) {
            seckillActivityMapper.insert(activity);
        }
        for (SeckillActivity activity : activities) {
            String key = "seckill:room:" + activity.getRoomId();
            long seconds = Duration.between(LocalDateTime.now(), activity.getEndTime()).getSeconds();
            if(seconds > 0){
                redisTemplate.opsForValue().set(key, "available", Duration.ofSeconds(seconds));
            }
            String infoKey = "seckill:info:" + activity.getRoomId();
            redisTemplate.opsForHash().put(infoKey, "endTime",String.valueOf(activity.getEndTime().toEpochSecond(java.time.ZoneOffset.UTC)));
        }
        return activities.size();
    }

    @Override
    public Result<List<SeckillActivity>> showAll() {
        LambdaQueryWrapper<SeckillActivity> queryWrapper = new LambdaQueryWrapper<>();

        return Result.ok(seckillActivityMapper.selectList(queryWrapper));
    }
    @Override
    public Result<List<SeckillActivity>> showAvailable() {
        LambdaQueryWrapper<SeckillActivity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SeckillActivity::getStatus, 1);

        return Result.ok(seckillActivityMapper.selectList(queryWrapper));
    }
}