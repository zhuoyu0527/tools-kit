package com.anarres.toolskit.support;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class Futures {
    public static <T> CompletableFuture<T> error(Throwable throwable) {
        CompletableFuture f = new CompletableFuture();
        f.completeExceptionally(throwable);
        return f;
    }

    public static <V> CompletableFuture<V> margin(BiFunction<V, V, V> function, Collection<CompletableFuture<V>> margins) {
        CompletableFuture<V>[] array = margins.toArray(new CompletableFuture[0]);
        return CompletableFuture.allOf(array).thenApply(aVoid -> {
                V rv = null;
                for(CompletableFuture<V> cf: margins) {
                    V value = cf.getNow(null);
                    rv = function.apply(rv, value);
                }
                return rv;
            });
    }
}
