package com.anarres.toolskit.cache.shiro;

import com.anarres.toolskit.cache.redis.RedisCache;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

import java.util.Collection;
import java.util.Set;

/**
 * Shiro 实现的缓存
 *
 * @param <K>
 * @param <V>
 */
public class ShiroRedisCache<K,V> implements Cache<K,V> {
	private com.anarres.toolskit.cache.Cache<K, V> impl;

	public ShiroRedisCache(RedisCache<K, V> redisImpl){
		this.impl = redisImpl;
	}

	@Override
	public V get(K key) throws CacheException {
		try {
			return impl.get(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public V put(K key, V value) throws CacheException {
		try {
			return impl.put(key, value);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public V remove(K key) throws CacheException {
		try {
			return impl.remove(key);
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public void clear() throws CacheException {
		try {
			impl.clear();
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public int size() {
		try {
			return impl.size();
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public Set<K> keys() {
		try {
			return impl.keys();
		} catch (Exception e) {
			throw new CacheException(e);
		}

	}

	@Override
	public Collection<V> values() {
		try {
			return impl.values();
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

}
