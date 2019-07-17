package com.anarres.toolskit.tree;

import java.util.List;

public interface Node<V extends java.io.Serializable> extends java.io.Serializable {

        void addChild(Node<V> node);

        List<Node<V>> getChilds();

        V getSource();

        Node<V> parent();

        boolean hasChild();

        boolean isRoot();

        boolean isLeaf();

}