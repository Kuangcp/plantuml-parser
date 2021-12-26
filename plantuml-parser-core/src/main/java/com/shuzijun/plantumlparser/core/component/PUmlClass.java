package com.shuzijun.plantumlparser.core.component;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * plantUml类
 *
 * @author shuzijun
 */
public class PUmlClass {
    private String packageName;

    private String className;

    private String classType;

    private List<PUmlField> pUmlFieldList = new LinkedList<>();

    private List<PUmlMethod> pUmlMethodList = new LinkedList<>();

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFinalName() {
        return this.packageName == null ? "" : this.packageName + "." + this.className;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public void addPUmlFieldList(PUmlField pUmlField) {
        this.pUmlFieldList.add(pUmlField);
    }

    public void addPUmlMethodList(PUmlMethod pUmlMethod) {
        this.pUmlMethodList.add(pUmlMethod);
    }

    @Override
    public String toString() {
        return classType + " " + ((packageName == null || packageName.trim().equals("")) ? "" : (packageName + ".")) + className + " {\n" +
                (pUmlFieldList.stream().map(PUmlField::toString).collect(Collectors.joining("\n")) + "\n") +
                (pUmlMethodList.stream().map(PUmlMethod::toString).collect(Collectors.joining("\n")) + "\n") +
                "}";
    }
}
