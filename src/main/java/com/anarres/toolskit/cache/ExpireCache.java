package com.anarres.toolskit.cache;

public interface ExpireCache<V> extends Cache<String, V> {

    V put(String key, V value, long expireMS) throws Exception ;
}
