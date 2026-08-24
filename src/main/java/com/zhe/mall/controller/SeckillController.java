package com.zhe.mall.controller;

import com.zhe.mall.common.Result;
import com.zhe.mall.common.ResultCodeEnum;
import com.zhe.mall.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @PostMapping("/{roomId}")
    public Result seckill(@PathVariable Long roomId, Long userId) {
        // 调用 SeckillService 处理秒杀
        int result = seckillService.grab(roomId, userId);
        if(result == 1){
            return Result.ok("秒杀成功");
        }
        if(result == 2||result == 3){
            return Result.fail(703, ResultCodeEnum.SECKILL_ACTIVITY_INVALID.getMessage());
        }
        return Result.fail(701, ResultCodeEnum.SECKILL_STOCK_NOT_ENOUGH.getMessage());
    }
}