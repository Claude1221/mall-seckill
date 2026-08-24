package com.zhe.mall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_activity")
public class SeckillActivity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roomId;           // 关联 room_info 表
    private BigDecimal seckillPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;        // 0-未开始, 1-进行中, 2-已结束
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}