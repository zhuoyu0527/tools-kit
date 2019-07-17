package com.anarres.toolskit.support;

import java.util.HashMap;
import java.util.Map;

public class DoubleKeyMap<K1, K2, V> {

    Map<K1, Map<K2, V>> map = new HashMap<>();

    public V get(K1 k, K2 k2) {
      Map<K2, V> keyMap = map.get(k);

      if(null == keyMap) {
          return null;
      }

      return keyMap.get(k2);
    }

    public boolean contains(K1 k, K2 k2) {
        Map<K2, V> keyMap = map.get(k);

        if(null == keyMap) {
            return false;
        }

        return keyMap.containsKey(k2);
    }

    public V put(K1 k, K2 k2, V v) {
        if(!map.containsKey(k)) {
            map.put(k, new HashMap<>());

        }
        return map.get(k).put(k2, v);
    }

    public Map<K2, V> get(K1 k) {
        return map.get(k);
    }
}
