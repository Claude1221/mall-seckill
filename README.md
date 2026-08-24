# seckill — 秒杀微服务

基于 Spring Boot 3.5 的高并发秒杀微服务，通过 Redis + Lua 实现原子秒杀，独立于主服务运行。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.5.15 | 基础框架 |
| JDK | 17 | 运行环境 |
| MyBatis-Plus | 3.5.5 | ORM |
| MySQL | 8.x | 持久化存储 |
| Redis | 6.x+ | 秒杀库存 & 原子操作 |
| Knife4j | 4.5.0 | API 文档 |
| Lombok | - | 样板代码简化 |

## 项目结构

```
seckill/
├── pom.xml
└── src/
    ├── main/java/com/atafl/seckill/
    │   ├── SeckillApplication.java          # 启动类，启用 @Async，暴露 RestTemplate
    │   ├── common/
    │   │   ├── Result.java                   # 统一响应体
    │   │   └── ResultCodeEnum.java           # 状态码枚举
    │   ├── config/
    │   │   ├── Knife4jConfig.java            # API 文档配置
    │   │   └── RedisConfig.java              # Lua 脚本 Bean 注册
    │   ├── controller/
    │   │   ├── SeckillAdminController.java   # 管理员接口（创建秒杀）
    │   │   └── SeckillController.java        # 用户秒杀接口
    │   ├── dto/
    │   │   └── SeckillCreateDTO.java         # 秒杀创建请求体
    │   ├── entity/
    │   │   └── SeckillActivity.java          # 秒杀活动实体
    │   ├── mapper/
    │   │   └── SeckillActivityMapper.java    # 数据访问层
    │   └── service/
    │       ├── SeckillAdminService.java      # 管理服务接口
    │       ├── SeckillService.java           # 秒杀服务接口
    │       ├── AsyncSeckillService.java      # 异步回调服务
    │       └── impl/
    │           ├── SeckillAdminServiceImpl.java
    │           └── SeckillServiceImpl.java
    └── resources/
        ├── application.yml                   # 配置
        └── seckill.lua                       # Redis Lua 秒杀脚本
```

## 核心架构

```
用户请求 → Controller → Service → Redis Lua 脚本（原子秒杀）
                                      ↓ 成功
                              @Async 回调主服务
```

### 秒杀流程

1. **管理员创建秒杀活动** → 清空历史数据 → 写入 DB → Redis 预热
2. **用户发起秒杀** `POST /seckill/{roomId}?userId=X`
3. **Redis Lua 原子操作**：
   - 检查活动是否过期 → 返回 3
   - 检查库存 `available/sold` → `sold` 返回 0
   - 原子翻转 `available → sold` → 返回 1（成功）
4. **异步回调** → `RestTemplate` 向主服务创建预约

### 库存模型

**单库存秒杀**：每个 roomId 有且仅有一个库存，使用二值 `available/sold` 翻转，非递减计数器模式。

## API 接口

| 方法 | 端点 | 说明 |
|------|------|------|
| `POST` | `/admin/seckill/create` | 批量创建秒杀活动（会清空旧数据） |
| `POST` | `/seckill/{roomId}?userId={userId}` | 用户秒杀抢购 |

### 创建秒杀 - 请求体

```json
{
  "roomIds": [101, 102, 103],
  "seckillPrice": 99.00,
  "startTime": "2026-08-10T10:00:00",
  "endTime": "2026-08-10T12:00:00"
}
```

### 秒杀抢购 - 返回值

| 状态码 | 含义 |
|--------|------|
| 200 | 秒杀成功 |
| 701 | 库存不足 |
| 703 | 活动无效/已过期 |

## 数据库

数据库名称：`lease`（与主租赁系统共享）

### seckill_activity 表

| 列名 | 类型 | 说明 |
|------|------|------|
| id | bigint | 自增主键 |
| room_id | bigint | 房源 ID |
| seckill_price | decimal | 秒杀价 |
| start_time | datetime | 秒杀开始时间 |
| end_time | datetime | 秒杀结束时间 |
| status | int | 0-未开始 / 1-进行中 / 2-已结束 |
| create_time | datetime | 创建时间（自动填充） |

## Redis 键设计

| 键模式 | 类型 | 说明 | TTL |
|--------|------|------|-----|
| `seckill:room:{roomId}` | String | 库存状态：`available` 或 `sold` | 活动结束时间 |
| `seckill:info:{roomId}` | Hash | 活动信息（endTime 等） | 无显式 TTL |

## 快速启动

### 前置条件

- JDK 17+
- Maven 3.9+
- MySQL 8.x（创建 `lease` 数据库）
- Redis

### 配置

修改 `application.yml` 中的数据库和 Redis 连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lease
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

### 运行

```bash
mvn spring-boot:run
```

服务运行在 **8082** 端口，API 文档：http://localhost:8082/doc.html

## 在线演示

线上地址：`http://139.129.51.230:8082/doc.html`

1. 调用 `POST /admin/seckill/create` 创建秒杀活动（结束时间需晚于当前时间）：

```json
{
  "roomIds": [2],
  "seckillPrice": 99.00,
  "startTime": "2026-08-10T00:00:00",
  "endTime": "2026-08-11T23:59:59"
}
```

2. 调用 `POST /seckill/2?userId=1` 抢购，返回「秒杀成功」
3. 秒杀成功后异步回调主服务（`http://139.129.51.230:8081`）自动创建租约，可在 [Lease 管理后台](https://github.com/anfemglan/apartment) 的租约查询接口中查看

## 注意事项

- `POST /admin/seckill/create` 会**清空全部旧数据**再写入新活动
- 回调主服务地址：`http://139.129.51.230:8081`
- 异步回调无重试机制，若主服务宕机会丢单
