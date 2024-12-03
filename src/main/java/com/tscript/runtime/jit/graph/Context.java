package com.tscript.runtime.jit.graph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class Context {

    private Node next;

    public final Deque<Node> checkpointTrace = new ArrayDeque<>();

    public Context(Node start){
        setNext(start);
    }

    public Node getNext(){
        return next;
    }

    public void setNext(Node next) {
        this.next =next;
    }

}
