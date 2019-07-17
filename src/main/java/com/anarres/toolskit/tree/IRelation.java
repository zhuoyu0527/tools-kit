package com.anarres.toolskit.tree;

public interface IRelation<K, V> {
        K parentID(V t);
        K myID(V t);
}