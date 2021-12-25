package com.shuzijun.plantumlparser.core;

/**
 * class之间的关系
 *
 * @author 关系
 */
public class PUmlRelation {

    /**
     * 符号端 被依赖一方
     */
    private String parent;

    private String parentBean;

    /**
     * 直线端 需要依赖外部的一方
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

    public String getParentBean() {
        return parentBean;
    }

    public void setParentBean(String parentBean) {
        this.parentBean = parentBean;
    }

    public String getChildBean() {
        return childBean;
    }

    public void setChildBean(String childBean) {
        this.childBean = childBean;
    }
}
