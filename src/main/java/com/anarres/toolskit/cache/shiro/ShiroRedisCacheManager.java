package com.anarres.toolskit.cache.shiro;

import com.anarres.toolskit.cache.HCache;
import com.anarres.toolskit.cache.redis.RedisCache;
import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

/**
 * redis的缓存管理器
 * @author
 *
 */
public class ShiroRedisCacheManager extends AbstractCacheManager {
	private final HCache cached;

	public ShiroRedisCacheManager(HCache cached) {
		this.cached = cached;
	}

	@Override
	protected Cache createCache(String cacheName) throws CacheException {
		return new ShiroRedisCache(new RedisCache(cacheName, cached));
	}

}
