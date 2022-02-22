package com.github.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置
 *
 * @author luocz
 * @date 2022/2/21
 */
@Setter
@Getter
@ToString
@ConfigurationProperties(prefix = "redis-lock")
public class RedisLockProperties {


    /**
     * 缓存key前缀
     */
    private String prefixKey = "lock:";

}
