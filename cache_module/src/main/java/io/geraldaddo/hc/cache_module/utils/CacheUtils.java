package io.geraldaddo.hc.cache_module.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CacheUtils {
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    public void evictFromCacheByKeyMatch(String cacheName, String baseKey) {
        String pattern = String.format("%s::%s_*", cacheName, baseKey);
        redisTemplate.delete(redisTemplate.keys(pattern));
    }
}
