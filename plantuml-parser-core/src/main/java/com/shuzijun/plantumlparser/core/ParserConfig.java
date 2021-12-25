package com.shuzijun.plantumlparser.core;

import com.github.javaparser.ParserConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;

/**
 * 解析配置
 *
 * @author shuzijun
 */
public class ParserConfig {
    /**
     * 解析的文件路径
     */
    private final Map<String, File> fileMap = new HashMap<>();

    /**
     * 输出文件路径
     */
    private String outFilePath;

    private final Set<String> fieldModifier = new HashSet<>();

    private final Set<String> methodModifier = new HashSet<>();

    /**
     * 展示类 时包含包名
     */
    private boolean showPackage = true;

    /**
     * 展示构造器
     */
    private boolean showConstructors = false;

    /**
     * 展示实现 Serializable 接口关系
     */
    private boolean showSerializableImpl = false;

    /**
     * 展示 方法
     */
    private boolean showMethod = false;
    /**
     * 只展示 复杂业务层 Service Adapter Handler
     */
    private boolean onlyShowLogicLayer = false;

    /**
     * 只展示具有关联关系的类
     */
    private boolean onlyShowRelationClass = true;

    /**
     * 展示 实体循环依赖
     */
    private boolean showCircularDepends = true;


    private ParserConfiguration.LanguageLevel languageLevel = ParserConfiguration.LanguageLevel.JAVA_8;

    public String getOutFilePath() {
        return outFilePath;
    }

    public void setOutFilePath(String outFilePath) {
        this.outFilePath = outFilePath;
    }

    public Set<File> getFilePaths() {
        return new HashSet<>(fileMap.values());
    }

    public void addFilePath(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            Collection<File> files = FileUtils.listFiles(file, new String[]{"java"}, Boolean.TRUE);
            files.forEach(fileTemp -> fileMap.put(fileTemp.getPath(), fileTemp));
        } else if (filePath.endsWith("java")) {
            fileMap.put(file.getPath(), file);
        }
    }

    public void addFieldModifier(String modifier) {
        fieldModifier.add(modifier);
    }

    public boolean isFieldModifier(String modifier) {
        return fieldModifier.contains(modifier);
    }

    public void addMethodModifier(String modifier) {
        methodModifier.add(modifier);
    }

    public boolean isMethodModifier(String modifier) {
        return methodModifier.contains(modifier);
    }

    public boolean isShowPackage() {
        return showPackage;
    }

    public void setShowPackage(boolean showPackage) {
        this.showPackage = showPackage;
    }

    public boolean isShowConstructors() {
        return showConstructors;
    }

    public void setShowConstructors(boolean showConstructors) {
        this.showConstructors = showConstructors;
    }

    public ParserConfiguration.LanguageLevel getLanguageLevel() {
        return languageLevel;
    }

    public void setLanguageLevel(ParserConfiguration.LanguageLevel languageLevel) {
        this.languageLevel = languageLevel;
    }

    public boolean isShowSerializableImpl() {
        return showSerializableImpl;
    }

    public void setShowSerializableImpl(boolean showSerializableImpl) {
        this.showSerializableImpl = showSerializableImpl;
    }

    public boolean isOnlyShowLogicLayer() {
        return onlyShowLogicLayer;
    }

    public void setOnlyShowLogicLayer(boolean onlyShowLogicLayer) {
        this.onlyShowLogicLayer = onlyShowLogicLayer;
    }

    public boolean isLogicLayer(String className) {
        return className.contains("Impl")
                || className.contains("Handler")
                || className.contains("Adapter")
                || className.contains("Aspect")
                || className.contains("Facade")
                || className.contains("Schedule")
                ;
    }

    public boolean isShowMethod() {
        return showMethod;
    }

    public void setShowMethod(boolean showMethod) {
        this.showMethod = showMethod;
    }

    public boolean isOnlyShowRelationClass() {
        return onlyShowRelationClass;
    }

    public void setOnlyShowRelationClass(boolean onlyShowRelationClass) {
        this.onlyShowRelationClass = onlyShowRelationClass;
    }

    public boolean isShowCircularDepends() {
        return showCircularDepends;
    }

    public void setShowCircularDepends(boolean showCircularDepends) {
        this.showCircularDepends = showCircularDepends;
    }
}
