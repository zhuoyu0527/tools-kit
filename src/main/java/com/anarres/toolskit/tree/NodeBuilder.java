package com.anarres.toolskit.tree;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ath on 2016/3/14.
 */
public class NodeBuilder<K, V extends Serializable> {

    private NodeBuilder() {}

    public static  <K, V extends Serializable>  NodeBuilder<K, V> newBuidler() {
        return new NodeBuilder();
    }

    public NodeBuilder index(Iterable<V> resources) {
        for(V obj : resources) {
            sourceIndex.put(relation.myID(obj), obj);
        }
        return this;
    }

    public NodeBuilder relation(IRelation<K, V> relation){
        this.relation = relation;
        return this;
    }


    public synchronized List<Node<V>> toNode(Iterable<V> iter) {
        rootNode = new NodeImpl<>();

        indexMap.clear();
        indexMap.putAll(sourceIndex);
        for(V obj : iter) {
            indexMap.put(relation.myID(obj), obj);
        }

        for(V obj : iter) {
            createNode(relation.myID(obj), null);
        }

        return rootNode.getChilds();
    }

    private final Map<K, Node<V>> cache = new HashMap<>();
    private final Map<K, V> indexMap = new HashMap<>();

    private final Map<K, V> sourceIndex = new HashMap();
    private IRelation<K, V> relation;
    private Node<V> rootNode;


    private Node<V> getRoot(Node<V> node){
        if(node.isRoot()) {
            return node;
        } else {
            return getRoot(node.parent());
        }
    }

    private Node<V> createNode(K k, Node<V> child) {
        if(!cache.containsKey(k)) {
            NodeImpl<V> node = new NodeImpl();
            node.source = indexMap.get(k);
            node.childs = new ArrayList<>();
            cache.put(k, node);
        }
        NodeImpl<V> node = (NodeImpl)cache.get(k);
        if(null != child) {
            node.childs.add(child);
        }

        K parentID = relation.parentID(node.source);
        if(parentID == null || indexMap.get(parentID) == null) {
            rootNode.addChild(node);
            return node;
        }
        Node<V> parentNode = cache.get(parentID);
        if(null == parentNode) {
            createNode(parentID, node);
        } else {
            //确立关系
            parentNode.addChild(node);
            node.parent = parentNode;
        }


        return node;
    }

//    private void createNode(T obj) {
//        K myID = relation.myID(obj);
//        if(cache.containsKey(myID)) {
//            Node<T> node = cache.get(myID);
//            node.source = obj;
//        } else {
//            Node<T> node = new Node<>();
//            node.childs = new ArrayList<>();
//            node.source = obj;
//
//            createParent(node);
//        }
//
//
//    }
//
//    private void createParent(Node<T> node) {
//        K parentID = relation.parentID(node.source);
//        if(!cache.containsKey(parentID)) {
//            Node<T> parentNode = new Node<>();
//            cache.put(parentID, parentNode);
//        }
//
//        Node<T> parentNode = cache.get(parentID);
//        parentNode.addChild(node);
//    }



    public static class NodeImpl<V extends Serializable> implements Node<V> {
        private V source;
        private List<Node<V>> childs = new ArrayList<>();

        private Node<V> parent;

        public void addChild(Node<V> node) {
            childs.add(node);
        }

        public List<Node<V>> getChilds() {
            return this.childs;
        }

        public V getSource() {
            return source;
        }

        public Node<V> parent() {
            return parent;
        }

        public boolean hasChild() {
            return null != childs && !childs.isEmpty();
        }

        public boolean isRoot(){
            return null == parent;
        }

        public boolean isLeaf(){
            return !hasChild();
        }

    }


}
