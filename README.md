## 基于redis的分布式锁

### 使用方式
```
<dependency>
    <groupId>org.github</groupId>
    <artifactId>redislock-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>

配置：
redis-lock:
  enabled: true  #是否启用
  prefix-key: 'lock:' #reids中key前缀


```

```
OneByOne oneByOne = new OneByOne("bizType", "20220128");
String result = oneByOneTemplate.execute(oneByOne, () -> {
   // 业务逻辑
    return "OK";
});
```