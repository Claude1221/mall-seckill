package com.zhe.mall.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SeckillCreateDTO {
    private List<Long> roomIds;
    private BigDecimal seckillPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}