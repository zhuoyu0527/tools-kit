package com.anarres.toolskit.locks;

import java.util.HashMap;
import java.util.Map;

public class FunctionLockKit {
    private static Map<String, Map<Object, Object>> lockMap = new HashMap<>();

    /**
     * 获取指定key 和 function 生成的对象锁。
     * @param key 唯一键 例如：username。（不建议使用复杂对象）
     * @param function 功能描述
     * @return
     */
    public static synchronized Object getLock(String key, Object function) {
        if(!lockMap.containsKey(key)) {
            lockMap.put(key, new HashMap<>());
        }

        Map<Object, Object> funcMap = lockMap.get(key);

        if(!funcMap.containsKey(function)) {
            funcMap.put(function, new Object());
        }

        return funcMap.get(function);
    }
}
