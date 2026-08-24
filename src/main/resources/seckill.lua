-- KEYS[1] = seckill:room:123   （库存状态 key）
-- KEYS[2] = seckill:info:123   （活动结束时间 key）
-- ARGV[1] = 当前时间戳（秒）

local stockKey = KEYS[1]
local infoKey = KEYS[2]
local now = tonumber(ARGV[1])

-- 读取活动结束时间
local endTime = tonumber(redis.call('hget', infoKey, 'endTime'))
if not endTime or now > endTime then
    return 3  -- 活动已结束
end

-- 读取库存状态
local status = redis.call('get', stockKey)
if status == 'available' then
    redis.call('set', stockKey, 'sold')
    return 1  -- 抢购成功
elseif status == 'sold' then
    return 0  -- 已售罄
else
    return 2  -- 活动未开始（状态异常）
end