package com.anarres.toolskit.cache;


import java.util.Collection;
import java.util.Set;

public interface Cache<K, V> {
    V get(K key) throws Exception ;

    V put(K key, V value) throws Exception ;
    
    V remove(K key) throws Exception ;

    void clear() throws Exception ;

    /**
     * size可能是通过keys().size() 来获取的。
     * @return
     * @throws Exception
     */
    int size() throws Exception ;

    Set<K> keys() throws Exception ;

    Collection<V> values() throws Exception ;
}
