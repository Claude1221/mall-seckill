package com.zhe.mall.controller;

import com.zhe.mall.dto.SeckillCreateDTO;
import com.zhe.mall.service.SeckillAdminService;
import com.zhe.mall.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/seckill")
public class SeckillAdminController {

    @Autowired
    private SeckillAdminService seckillAdminService;

    @PostMapping("/create")
    public Result createSeckill(@RequestBody SeckillCreateDTO dto) {
        int count = seckillAdminService.batchCreate(dto);
        return Result.ok("成功创建 " + count + " 个秒杀活动");
    }
}
