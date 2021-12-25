package com.shuzijun.plantumlparser.core;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * class之间的关系
 *
 * @author 关系
 */
public class PUmlRelation {

    /**
     * 符号端
     */
    private String parent;

    private String parentBean;

    /**
     * 直线端
     */
    private String child;
    private String childBean;

    private String relation;

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setChild(String child) {
        this.child = child;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    @Override
    public String toString() {
        return parent + " " + relation + " " + child;
    }

    public String getParent() {
        return parent;
    }

    public String getChild() {
        return child;
    }

    public String getRelation() {
        return relation;
    }

    public String buildKey() {
        return Stream.of(parent, relation, child).sorted().collect(Collectors.joining(""));
    }
}
