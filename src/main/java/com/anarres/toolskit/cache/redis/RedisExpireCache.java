package com.anarres.toolskit.cache.redis;

import com.anarres.toolskit.cache.ExpireCache;
import com.anarres.toolskit.cache.HCache;
import com.anarres.toolskit.support.FuncKit;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class RedisExpireCache<V> implements ExpireCache<V> {

    private final String name;
    private final HCache cached;

    public RedisExpireCache(String name, HCache cached){
        this.name=name;
        this.cached=cached;
    }

    private String prefix() {
        return name + ":";
    }
    /**
     * 获得byte[]型的key
     * @param key
     * @return
     */
    private byte[] getByteKey(String key){
        return (prefix() + key).getBytes();
    }


    @Override
    public V put(String key, V value, long expireMS) throws Exception {
        cached.updateCached(getByteKey(key), FuncKit.serialize(value), Optional.of(expireMS));
        return value;
    }

    @Override
    public V get(String key) throws Exception {
        V value= (V)cached.getCached(getByteKey(key));
        return value;
    }

    @Override
    public V put(String key, V value) throws Exception {
        cached.updateCached(getByteKey(key), FuncKit.serialize(value), Optional.empty());
        return value;
    }

    @Override
    public V remove(String key) throws Exception {
        V previous = get(key);
        cached.deleteCached(getByteKey(key));
        return previous;
    }

    @Override
    public void clear() throws Exception {
        Set<String> keys = keys();
        byte[][] buf = new byte[keys.size()][];
        int i = 0;
        for(String key: keys) {
            buf[i] = key.getBytes();
            i++;
        }
        cached.deleteCached(buf);
    }

    @Override
    public int size() throws Exception {
        return keys().size();
    }

    @Override
    public Set<String> keys() throws Exception {
        return cached.getKeys((prefix() + "*").getBytes());
    }

    @Override
    public Collection<V> values() throws Exception {
        throw new Exception("not support");
    }
}
