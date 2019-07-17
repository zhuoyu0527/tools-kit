package com.anarres.toolskit.cache.redis;

import com.anarres.toolskit.cache.Cache;
import com.anarres.toolskit.cache.HCache;
import com.anarres.toolskit.support.FuncKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;

public class RedisCache<K, V> implements Cache<K, V> {
    private static Logger logger = LoggerFactory.getLogger(RedisCache.class);
    private final String name;
    private final HCache cached;

    public RedisCache(String name, HCache cached){
        this.name=name;
        this.cached=cached;
    }

    /**
     * 获得byte[]型的key
     * @param key
     * @return
     */
    private byte[] getByteKey(K key){
        if(key instanceof String){
            String preKey = key.toString();
            return preKey.getBytes();
        }else{
            return FuncKit.serialize(key);
        }
    }


    private byte[] getByteName(){
        return name.getBytes();

    }

    @Override
    public V get(K key) throws Exception {
        logger.debug("根据key从Redis中获取对象 key [" + key + "]");
        if (key == null) {
            return null;
        }else{
            V value= (V) cached.getHashCached(getByteName(),getByteKey(key));
            return value;
        }

    }

    @Override
    public V put(K key, V value) throws Exception {
        logger.debug("根据key存储 key [" + key + "]");

        cached.updateHashCached(getByteName(),getByteKey(key), FuncKit.serialize(value));
        return value;

    }

    @Override
    public V remove(K key) throws Exception {
        logger.debug("从redis中删除 key [" + key + "]");

        V previous = get(key);
        cached.deleteHashCached(getByteName(),getByteKey(key));
        return previous;

    }

    @Override
    public void clear() throws Exception {
        logger.debug("从redis中删除所有元素");

        cached.deleteCached(getByteName());

    }

    @Override
    public int size() throws Exception  {
        Long longSize = new Long(cached.getHashSize(getByteName()));
        return longSize.intValue();

    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keys() throws Exception  {
        Set<K> keys = cached.getHashKeys(getByteName());
        return keys;

    }

    @Override
    public Collection<V> values() throws Exception  {

        Collection<V> values = cached.getHashValues(getByteName());
        return values;

    }
}
