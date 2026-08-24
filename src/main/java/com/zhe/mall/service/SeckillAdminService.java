package com.zhe.mall.service;

import com.zhe.mall.common.Result;
import com.zhe.mall.dto.SeckillCreateDTO;
import com.zhe.mall.entity.SeckillActivity;

import java.util.List;

public interface SeckillAdminService {
    int batchCreate(SeckillCreateDTO dto);
    Result<List<SeckillActivity>> showAll();
    Result<List<SeckillActivity>> showAvailable();
}