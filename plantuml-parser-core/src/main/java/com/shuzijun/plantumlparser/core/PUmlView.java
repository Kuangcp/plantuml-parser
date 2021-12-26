package com.shuzijun.plantumlparser.core;

import com.shuzijun.plantumlparser.core.component.PUmlClass;
import com.shuzijun.plantumlparser.core.constant.RelationType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * PUml视图
 *
 * @author shuzijun
 */
public class PUmlView {

    private final ParserConfig parserConfig;
    private final List<PUmlClass> classes = new ArrayList<>();
    private final List<PUmlRelation> relations = new ArrayList<>();

    public PUmlView(ParserConfig parserConfig) {
        this.parserConfig = parserConfig;
    }

    public void addPUmlClass(PUmlClass pUmlClass) {
        classes.add(pUmlClass);
    }

    public void addPUmlRelation(PUmlRelation pUmlRelation) {
        if (!parserConfig.isNeededRelation(pUmlRelation.getParent(), pUmlRelation.getChild())) {
            return;
        }
        relations.add(pUmlRelation);
    }

    public String buildUmlContent() {
        // TODO 支持 扫描所有类，但是关系只以关注的包 作为第一级 依次往上 得到整个树

        if (parserConfig.isShowCircularDepends()) {
            // FIXME 校验 循环依赖错误
            final Map<String, String> extendsMap = relations.stream()
                    .filter(v -> Objects.equals(v.getRelation(), RelationType.EXTENDS) || Objects.equals(v.getRelation(), RelationType.IMPLEMENT))
                    .collect(Collectors.toMap(PUmlRelation::getChild, PUmlRelation::getParent, (front, current) -> current));

            final Map<String, String> relationMap = relations.stream()
                    .filter(v -> Objects.equals(v.getRelation(), RelationType.COMPOSITION))
//                    .collect(Collectors.toMap(PUmlRelation::getParent, PUmlRelation::getChild, (front, current) -> current));
                    .collect(Collectors.toMap(PUmlRelation::getParent, v -> extendsMap.getOrDefault(v.getChild(), v.getChild()), (front, current) -> current));

            System.out.println();
            for (Map.Entry<String, String> entry : relationMap.entrySet()) {
                Set<String> cache = new HashSet<>();
                String next = relationMap.get(entry.getValue());

                cache.add(next);
                while (next != null) {
                    next = relationMap.get(next);
                    if (cache.contains(next)) {
                        break;
                    }
                    if (next == null) {
                        break;
                    }
                    cache.add(next);
                }
                if (cache.size() > 1) {
                    System.out.println(entry.getKey() + " <> " + entry.getValue());
                }
            }
            System.out.println();
        }

        if (parserConfig.isOnlyShowRelationClass()) {
            final Set<String> classSet = relations.stream()
                    .flatMap(v -> Stream.of(v.getParent(), v.getChild())).collect(Collectors.toSet());

            return "@startuml\n" +
                    (classes.stream()
                            .filter(v -> classSet.contains(v.getFinalName()))
                            .map(PUmlClass::toString).collect(Collectors.joining("\n")) + "\n") +
                    ("\n\n" + relations.stream().map(PUmlRelation::toString).collect(Collectors.joining("\n")) + "\n") +
                    "@enduml"
                    ;
        }

        return "@startuml\n" +
                (classes.stream().map(PUmlClass::toString).collect(Collectors.joining("\n")) + "\n") +
                ("\n\n" + relations.stream().map(PUmlRelation::toString).collect(Collectors.joining("\n")) + "\n") +
                "@enduml"
                ;
    }
}
