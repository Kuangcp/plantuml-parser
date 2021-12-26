package com.shuzijun.plantumlparser.core;

import com.github.javaparser.ParserConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;

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

    private final Set<String> fieldModifier = new HashSet<>();

    private final Set<String> methodModifier = new HashSet<>();

    /**
     * 输出文件路径
     */
    private String outFilePath;
    private boolean outSvg = false;

    /**
     * 展示类 时包含包名
     */
    private boolean showPackage = true;

    /**
     * 展示构造器
     */
    private boolean showConstructors = true;

    /**
     * 展示实现 Serializable 接口关系
     */
    private boolean showSerializableImpl = true;

    /**
     * 展示 方法
     */
    private boolean showMethod = true;
    /**
     * 只展示 复杂业务层 Service Adapter Handler
     */
    private boolean onlyShowLogicLayer = false;
    private Predicate<String> logicPredicate = null;

    /**
     * 只展示具有关联关系的类
     */
    private boolean onlyShowRelationClass = false;

    /**
     * 展示 实体循环依赖
     */
    private boolean showCircularDepends = false;


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

    public void addFieldModifier(String... modifier) {
        if (Objects.isNull(modifier)) {
            return;
        }
        fieldModifier.addAll(Arrays.asList(modifier));
    }

    public boolean isNeedShowModifier(String modifier) {
        return fieldModifier.contains(modifier);
    }

    public void addMethodModifier(String... modifier) {
        if (Objects.isNull(modifier)) {
            return;
        }
        methodModifier.addAll(Arrays.asList(modifier));
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

    public boolean isNeededRelation(String parent, String child) {
        if (!this.onlyShowLogicLayer) {
            return true;
        }
        if (Objects.isNull(logicPredicate)) {
            return true;
        }
        return logicPredicate.test(parent) && logicPredicate.test(child);
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

    public void setLogicPredicate(Predicate<String> logicPredicate) {
        this.logicPredicate = logicPredicate;
    }

    public boolean isOutSvg() {
        return outSvg;
    }

    public void setOutSvg(boolean outSvg) {
        this.outSvg = outSvg;
    }
}
