package com.anarres.toolskit.cache;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 缓存接口，描述了一个Cache在本项目中可能涉及的基本操作。
 * 该 Cache 不能简单的等价于 Map 接口。而应该看做 包含多个Map的Map
 *
 */
public interface HCache {
	/**
	 * 删除 缓存
	 * @param key
	 * @return
	 * @throws Exception
	 */
	String deleteCached(byte[]... key)throws Exception;
	/**
	 * 更新 缓存
	 * @param key
	 * @param value
	 * @return
	 * @throws Exception
	 */
	Object updateCached(byte[] key, byte[] value, Optional<Long> expireMS)throws Exception;
	/**
	 * 获取缓存
	 * @param key
	 * @return
	 * @throws Exception
	 */
	Object getCached(byte[] key)throws Exception;
	/**
	 * 根据 正则表达式key 获取 列表
	 * @param pattern
	 * @return
	 * @throws Exception
	 */
	Set getKeys(byte[] pattern)throws Exception;

	/**
	 * 根据 正则表达式key 获取 列表
	 * @param key
	 * @return
	 * @throws Exception
	 */
	Set getHashKeys(byte[] key)throws Exception;



	/**
	 * 更新 缓存
	 * @param key
	 * @param value
	 * @return
	 * @throws Exception
	 */
	Boolean updateHashCached(byte[] key, byte[] mapkey, byte[] value)throws Exception;


	/**
	 * 获取缓存
	 * @param key
	 * @return
	 * @throws Exception
	 */
	Object getHashCached(byte[] key, byte[] mapkey)throws Exception;


	/**
	 * 删除 缓存
	 * @param key
	 * @param mapkey
	 * @return
	 * @throws Exception
	 */
	Long deleteHashCached(byte[] key, byte[] mapkey)throws Exception;

	/**
	 * 获取 map的长度
	 * @param key
	 * @return
	 * @throws Exception
	 */
	Long getHashSize(byte[] key)throws Exception;
	/**
	 * 获取 map中的所有值
	 * @param key
	 * @return
	 * @throws Exception
	 */
	List getHashValues(byte[] key)throws Exception;


	/**
	 * 获取 map的长度
	 * @return
	 * @throws Exception
	 */
	Long getDBSize()throws Exception;

	/**
	 * 获取 map的长度
	 * @return
	 * @throws Exception
	 */
	void clearDB()throws Exception;
}