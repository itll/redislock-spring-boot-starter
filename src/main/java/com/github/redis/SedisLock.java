
package com.github.redis;


import org.springframework.data.redis.core.RedisTemplate;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Redis distributed lock implementation
 */
public class SedisLock {

    private static Random random = new Random();
    /**
     * Lock key path.
     */
    private final String lockKey;
    /**
     * 锁拥有者标识
     */
    private final String owner = "";//TODO:
//    private final String owner = Identities.uuid();

//    private RedisClient redisClient;

    private RedisTemplate redisTemplate;

    /**
     * Lock expiration in miliseconds.
     */
    private int expireMsecs = 60 * 1000; // 锁超时，防止线程在入锁以后，无限的执行等待
    /**
     * Acquire timeout in miliseconds.
     */
    private int timeoutMsecs = 10 * 1000; // 锁等待，防止线程饥饿
    private boolean locked = false;

    /**
     * Detailed constructor with default acquire timeout 10000 msecs and lock expiration of 60000 msecs.
     *
     * @param redisTemplate
     * @param lockKey     lock key (ex. account:1, ...)
     */
    public SedisLock(RedisTemplate redisTemplate, String lockKey) {
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey;
    }

    /**
     * Detailed constructor with default lock expiration of 60000 msecs.
     *
     * @param redisClient
     * @param lockKey      lock key (ex. account:1, ...)
     * @param timeoutMsecs acquire timeout in miliseconds (default: 10000 msecs)
     */
    public SedisLock(RedisTemplate redisTemplate, String lockKey, int timeoutMsecs) {
        this(redisTemplate, lockKey);
        this.timeoutMsecs = timeoutMsecs;
    }

    /**
     * Detailed constructor.
     *
     * @param redisTemplate
     * @param lockKey      lock key (ex. account:1, ...)
     * @param timeoutMsecs acquire timeout in miliseconds (default: 10000 msecs)
     * @param expireMsecs  lock expiration in miliseconds (default: 60000 msecs)
     */
    public SedisLock(RedisTemplate redisTemplate, String lockKey, int timeoutMsecs, int expireMsecs) {
        this(redisTemplate, lockKey, timeoutMsecs);
        this.expireMsecs = expireMsecs;
    }

    /**
     * Detailed constructor with default acquire timeout 10000 msecs and lock expiration of 60000 msecs.
     *
     * @param lockKey lock key (ex. account:1, ...)
     */
    public SedisLock(String lockKey) {
        this(null, lockKey);
    }

    /**
     * Detailed constructor with default lock expiration of 60000 msecs.
     *
     * @param lockKey      lock key (ex. account:1, ...)
     * @param timeoutMsecs acquire timeout in miliseconds (default: 10000 msecs)
     */
    public SedisLock(String lockKey, int timeoutMsecs) {
        this(null, lockKey, timeoutMsecs);
    }

    /**
     * Detailed constructor.
     *
     * @param lockKey      lock key (ex. account:1, ...)
     * @param timeoutMsecs acquire timeout in miliseconds (default: 10000 msecs)
     * @param expireMsecs  lock expiration in miliseconds (default: 60000 msecs)
     */
    public SedisLock(String lockKey, int timeoutMsecs, int expireMsecs) {
        this(null, lockKey, timeoutMsecs, expireMsecs);
    }

    /**
     * @return lock key
     */
    public String getLockKey() {
        return lockKey;
    }

    /**
     * Acquire lock.
     *
     * @return true if lock is acquired, false acquire timeouted
     * @throws InterruptedException in case of thread interruption
     */
    public synchronized boolean acquire() throws InterruptedException {
        return acquire(redisTemplate);
    }

    /**
     * Acquire lock.
     *
     * @param redisTemplate
     * @return true if lock is acquired, false acquire timeouted
     * @throws InterruptedException in case of thread interruption
     */
    public synchronized boolean acquire(RedisTemplate redisTemplate) throws InterruptedException {
        int timeout = timeoutMsecs;
        while (timeout >= 0) {
            // 记录redislock所有者

            boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, owner, expireMsecs, TimeUnit.MICROSECONDS);

//            StatefulRedisConnection<String, String> connection = redisClient.connect();
//            RedisCommands<String, String> redisAsyncCommands = connection.sync();
//            String redisFuture = redisAsyncCommands.set(lockKey, owner, SetArgs.Builder.nx().px(expireMsecs));
            if (result) {
                locked = true;
                return true;
            }
//            if ("OK".equals(redisClient.execute(new ShardedJedisAction<String>() {
//                @Override
//                public String doAction(ShardedJedis jedis) {
//                    Jedis j = jedis.getShard(lockKey);
//                    // redis中不存在就设置lockKey对应的值，同时设置毫秒级过期时间
//                    return j.set(lockKey, owner, "NX", "PX", expireMsecs);
//                }
//            }))) {
//                // lock acquired
//                locked = true;
//                return true;
//            }
            int spinWatingTime = random.nextInt(200);
            timeout -= spinWatingTime;
            Thread.sleep(spinWatingTime);// NOSONAR
        }
        return false;
    }

    /**
     * Acqurired lock release.
     */
    public void release() {
        release(redisTemplate);
    }

    /**
     * Acqurired lock release.
     */
    public void release(RedisTemplate redisTemplate) {
//        StatefulRedisConnection<String, String> connection = redisClient.connect();
//        RedisCommands<String, String> redisAsyncCommands = connection.sync();

        String value = (String) redisTemplate.opsForValue().get(lockKey);

        if (locked && owner.equals(value)) {
            redisTemplate.delete(lockKey);
//            redisAsyncCommands.del(lockKey);
            locked = false;
        }

//        if (locked && owner.equals(redisClient.execute(new ShardedJedisAction<String>() {
//            @Override
//            public String doAction(ShardedJedis jedis) {
//                return jedis.get(lockKey);
//            }
//        }))) {
//            redisClient.execute(new ShardedJedisAction<Long>() {
//                @Override
//                public Long doAction(ShardedJedis jedis) {
//                    return jedis.del(lockKey);
//                }
//            });
//            locked = false;
//        }
    }

}
