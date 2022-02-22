
package com.github.onebyone;

import com.github.constants.RedisLockConstants;
import lombok.extern.slf4j.Slf4j;
import com.github.autoconfigure.RedisLockProperties;
import com.github.redis.SedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 一个接一个业务处理模版默认redis实现<br>
 * <p/>
 * 在redis中使用Key-Value的形式记录，在处理前对锁状态进行判断 ，判断逻辑参见{@link #invoke}方法<br>
 * <p/>
 * redis中的Key： Key前缀:业务类型:业务ID:方法<br>
 *
 */
@Slf4j
//@Component("oneByOneTemplate")
public class OneByOneTemplateImpl implements OneByOneTemplate {

//    @Autowired
//    private RedisClient redisClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisLockProperties redisLockProperties;
//    /**
//     * 序列缓存key前缀
//     */
//    public static final String REDIS_ONEBYONE_PREFIX_KEY = "pbs:obo:";//TODO:

    /**
     * 默认排队时间长度，10秒
     */
    private static final int DEFAULT_TIME_OUT_MSECS = 10000;
    /**
     * 默认锁时间长度，30秒
     */
    private static final int DEFAULT_EXPIRE_MSECS = 30000;

    public <T> T execute(OneByOne oneByOne, CallBack<T> callBack) {
        return execute(oneByOne, Boolean.TRUE, callBack);
    }

    public <T> T execute(OneByOne oneByOne, boolean waitInQueue, CallBack<T> callBack) {
        return execute(oneByOne, waitInQueue, DEFAULT_TIME_OUT_MSECS, DEFAULT_EXPIRE_MSECS, callBack);
    }

    public <T> T execute(OneByOne oneByOne, boolean waitInQueue, int timeoutMsecs, int expireMsecs,
            CallBack<T> callBack) {
        int timeoutMsecsTemp = DEFAULT_TIME_OUT_MSECS;
        int expireMsecsTemp = DEFAULT_EXPIRE_MSECS;
        // 需要排队
        if (waitInQueue) {
            // 当参数timeoutMsecs取值小于等于零时，则使用默认的排队10秒
            if (timeoutMsecs <= 0) {
                timeoutMsecsTemp = DEFAULT_TIME_OUT_MSECS;
            } else {
                timeoutMsecsTemp = timeoutMsecs;
            }

            // 不需要排队
        } else {
            timeoutMsecsTemp = 0;
        }

        // 当参数expireMsecs取值小于等于零时，则使用默认的有效期30秒
        if (expireMsecs <= 0) {
            expireMsecsTemp = DEFAULT_EXPIRE_MSECS;
        } else {
            expireMsecsTemp = expireMsecs;
        }

        return invoke(oneByOne, timeoutMsecsTemp, expireMsecsTemp, callBack);
    }

    /**
     * 处理业务
     *
     * @param <T>
     * @param oneByOne 一个接一个处理记录
     */
    private <T> T invoke(final OneByOne oneByOne, final int timeoutMsecs, final int expireMsecs, CallBack<T> callBack) {
        final String key = redisLockProperties.getPrefixKey() + oneByOne.getBizType() + RedisLockConstants.SYMBOL_COLON
                + oneByOne.getBizId();
        SedisLock sedisLock = new SedisLock(redisTemplate, key, timeoutMsecs, expireMsecs);
        long begin = System.currentTimeMillis();
        try {
            if (sedisLock.acquire()) { // 启用锁
                begin = System.currentTimeMillis();
                return callBack.invoke();
            } else {
                log.info("OneByOne waiting timeOut,key[{}] timeoutMsecs[{}ms] expireMsecs[{}ms].", new Object[] {
                        key, timeoutMsecs, expireMsecs });
                throw new RuntimeException( "key[" + key + "] OneByOne业务等待处理超时.");
            }
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException("key[" + key + "] OneByOne业务处理失败.");
//            throw new BizzException("OneByOne.process.error", "key[" + key + "] OneByOne业务处理失败.");
        } finally {
            long cost = System.currentTimeMillis() - begin;
            // 判断执行时间是否超过了锁的过期时间,超过锁有效期的不释放锁
            log.debug("OneByOne key[{}] process cost[{}].", key, cost);
            sedisLock.release();

            if (cost > expireMsecs) {
                log.info("OneByOne key[{}] process timeout, cost[{}].", key, cost);
            }

        }
    }

}
