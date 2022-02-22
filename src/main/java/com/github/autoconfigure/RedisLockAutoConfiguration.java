package com.github.autoconfigure;

import com.github.onebyone.OneByOneTemplate;
import com.github.onebyone.OneByOneTemplateImpl;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redis lock
 *
 * @author luocz
 * @date 2022/2/21
 */
@Configuration
@EnableConfigurationProperties(RedisLockProperties.class)
@ConditionalOnClass(RedisAutoConfiguration.class)
@ConditionalOnProperty(prefix = "redis-lock", value = "enabled", havingValue = "true")
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class RedisLockAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OneByOneTemplate oneByOneTemplate() {
        return new OneByOneTemplateImpl();
    }
}
