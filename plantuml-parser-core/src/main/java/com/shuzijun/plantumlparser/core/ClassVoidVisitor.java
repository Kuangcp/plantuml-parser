package com.shuzijun.plantumlparser.core;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.nodeTypes.NodeWithType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.shuzijun.plantumlparser.core.component.PUmlClass;
import com.shuzijun.plantumlparser.core.component.PUmlField;
import com.shuzijun.plantumlparser.core.component.PUmlMethod;
import com.shuzijun.plantumlparser.core.constant.ClassType;
import com.shuzijun.plantumlparser.core.constant.RelationType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 类
 *
 * @author shuzijun
 */
public class ClassVoidVisitor extends VoidVisitorAdapter<PUmlView> {

    private final String packageName;

    private final ParserConfig parserConfig;

    private final List<String> IGNORE_COMPOSITION = Arrays.asList("short", "Short", "int", "Integer", "long", "Long",
            "String", "char", "Char", "boolean", "Boolean", "byte", "Byte", "BigDecimal", "Object", "T", "Charset",
            "Date", "LocalDate", "LocalDateTime");
    private final List<String> IGNORE_COMPOSITION_PREFIX = Arrays.asList("ArrayList", "List", "Set", "Map", "Collection", "Function");

    public ClassVoidVisitor(String packageName, ParserConfig parserConfig) {
        this.packageName = packageName;
        this.parserConfig = parserConfig;
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, PUmlView pUmlView) {
        PUmlClass pUmlClass = new PUmlClass();
        pUmlClass.setPackageName(parserConfig.isShowPackage() ? packageName : "");
        pUmlClass.setClassName(declaration.getNameAsString());
        if (declaration.isInterface()) {
            pUmlClass.setClassType(ClassType.INTERFACE_CLASS);
        } else {
            pUmlClass.setClassType(ClassType.NORMAL_CLASS);
            for (Modifier modifier : declaration.getModifiers()) {
                if (modifier.toString().trim().contains("abstract")) {
                    pUmlClass.setClassType(ClassType.ABSTRACT_CLASS);
                    break;
                }
            }
        }
        pUmlView.addPUmlClass(pUmlClass);

        fillFields(declaration, pUmlClass);
        fillConstructors(declaration, pUmlClass);
        fillMethods(declaration, pUmlClass);

        final Optional<Node> parentNode = declaration.getParentNode();
        if (!parentNode.isPresent()) {
            System.out.println("WARN no parent" + declaration);
            return;
        }

        Node node = parentNode.get();
        NodeList<ImportDeclaration> importDeclarations = parseImport(node, pUmlClass, pUmlView);

        Map<String, String> importMap = new HashMap<>();
        if (importDeclarations != null) {
            for (ImportDeclaration importDeclaration : importDeclarations) {
                importMap.put(importDeclaration.getName().getIdentifier(), importDeclaration.getName().toString());
            }
        }

        fillExtendOrImplRelation(pUmlView, pUmlClass, importMap, declaration.getImplementedTypes(), RelationType.IMPLEMENT);
        fillExtendOrImplRelation(pUmlView, pUmlClass, importMap, declaration.getExtendedTypes(), RelationType.EXTENDS);
        fillComposition(declaration, pUmlView, pUmlClass, importMap);

        super.visit(declaration, pUmlView);
    }

    private void fillFields(ClassOrInterfaceDeclaration declaration, PUmlClass pUmlClass) {
        for (FieldDeclaration field : declaration.getFields()) {
            PUmlField pUmlField = new PUmlField();
            if (field.getModifiers().size() != 0) {
                field.getModifiers().stream()
                        .map(v -> v.toString().trim())
                        .filter(VisibilityUtils::isVisibility)
                        .findFirst()
                        .ifPresent(pUmlField::setVisibility);
            }
            if (parserConfig.isNeedShowModifier(pUmlField.getVisibility())) {
                pUmlField.setStatic(field.isStatic());
                final Optional<VariableDeclarator> firstNode = field.getVariables().getFirst();
                if (!firstNode.isPresent()) {
                    System.out.println("WARN " + field);
                    continue;
                }
                pUmlField.setType(firstNode.get().getTypeAsString());
                pUmlField.setName(firstNode.get().getNameAsString());
                pUmlClass.addPUmlFieldList(pUmlField);
            }
        }
    }

    private void fillConstructors(ClassOrInterfaceDeclaration declaration, PUmlClass pUmlClass) {
        if (!parserConfig.isShowConstructors()) {
            return;
        }
        for (ConstructorDeclaration constructor : declaration.getConstructors()) {
            PUmlMethod pUmlMethod = new PUmlMethod();
            if (constructor.getModifiers().size() != 0) {
                constructor.getModifiers().stream()
                        .map(v -> v.toString().trim())
                        .filter(VisibilityUtils::isVisibility)
                        .findFirst()
                        .ifPresent(pUmlMethod::setVisibility);
            }
            if (parserConfig.isMethodModifier(pUmlMethod.getVisibility())) {
                pUmlMethod.setStatic(constructor.isStatic());
                pUmlMethod.setAbstract(constructor.isAbstract());
                pUmlMethod.setReturnType("<<Create>>");
                pUmlMethod.setName(constructor.getNameAsString());
                for (Parameter parameter : constructor.getParameters()) {
                    pUmlMethod.addParam(parameter.getTypeAsString());
                }
                pUmlClass.addPUmlMethodList(pUmlMethod);
            }
        }
    }

    private void fillMethods(ClassOrInterfaceDeclaration declaration, PUmlClass pUmlClass) {
        if (!parserConfig.isShowMethod()) {
            return;
        }
        for (MethodDeclaration method : declaration.getMethods()) {
            PUmlMethod pUmlMethod = new PUmlMethod();

            if (method.getModifiers().size() != 0) {
                method.getModifiers().stream()
                        .map(v -> v.toString().trim())
                        .filter(VisibilityUtils::isVisibility)
                        .findFirst()
                        .ifPresent(pUmlMethod::setVisibility);
            }
            if (parserConfig.isMethodModifier(pUmlMethod.getVisibility())) {
                pUmlMethod.setStatic(method.isStatic());
                pUmlMethod.setAbstract(method.isAbstract());
                pUmlMethod.setReturnType(method.getTypeAsString());
                pUmlMethod.setName(method.getNameAsString());
                for (Parameter parameter : method.getParameters()) {
                    pUmlMethod.addParam(parameter.getTypeAsString());
                }
                pUmlClass.addPUmlMethodList(pUmlMethod);
            }
        }
    }

    private String deGeneric(String typeStr) {
        final int idx = typeStr.indexOf("<");
        if (idx == -1) {
            return typeStr;
        }
        return typeStr.substring(0, idx);
    }

    private String deArray(String typeStr) {
        final int idx = typeStr.indexOf("[");
        if (idx == -1) {
            return typeStr;
        }
        return typeStr.substring(0, idx);
    }


    private void fillComposition(ClassOrInterfaceDeclaration declaration,
                                 PUmlView pUmlView,
                                 PUmlClass pUmlClass,
                                 Map<String, String> importMap) {
        final List<FieldDeclaration> fields = declaration.getFields();
        if (fields.isEmpty()) {
            return;
        }
        fields.stream()
                .map(v -> v.getVariables().getFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(NodeWithType::getTypeAsString)
                .map(this::deGeneric)
                .map(this::deArray)
                .forEach(v -> {
                    if (IGNORE_COMPOSITION.contains(v)) {
                        return;
                    }
                    if (IGNORE_COMPOSITION_PREFIX.stream().anyMatch(v::startsWith)) {
                        return;
                    }
                    String longType = importMap.getOrDefault(v, getPackageNamePrefix(pUmlClass.getPackageName()) + v);
//                    if (longType.startsWith("java")){
//                        return;
//                    }

                    PUmlRelation pUmlRelation = new PUmlRelation();
                    pUmlRelation.setChild(getPackageNamePrefix(pUmlClass.getPackageName()) + pUmlClass.getClassName());
                    pUmlRelation.setParent(longType);
                    pUmlRelation.setRelation(RelationType.COMPOSITION);
                    pUmlView.addPUmlRelation(pUmlRelation);
                });
    }

    private void fillExtendOrImplRelation(PUmlView pUmlView,
                                          PUmlClass pUmlClass,
                                          Map<String, String> importMap,
                                          NodeList<ClassOrInterfaceType> implementedTypes,
                                          String relationSignal) {
        if (implementedTypes.size() == 0) {
            return;
        }
        for (ClassOrInterfaceType implementedType : implementedTypes) {
            final String nameAsString = implementedType.getNameAsString();
            if (Objects.equals(nameAsString, "Serializable") && !parserConfig.isShowSerializableImpl()) {
                return;
            }
            PUmlRelation pUmlRelation = new PUmlRelation();
            pUmlRelation.setChild(getPackageNamePrefix(pUmlClass.getPackageName()) + pUmlClass.getClassName());
            if (importMap.containsKey(nameAsString)) {
                if (parserConfig.isShowPackage()) {
                    pUmlRelation.setParent(importMap.get(nameAsString));
                } else {
                    pUmlRelation.setParent(nameAsString);
                }
            } else {
                // 同包，没有import语句的情况
                pUmlRelation.setParent(getPackageNamePrefix(pUmlClass.getPackageName()) + nameAsString);
            }
            pUmlRelation.setRelation(relationSignal);
            pUmlView.addPUmlRelation(pUmlRelation);
        }
    }

    private NodeList<ImportDeclaration> parseImport(Node node, PUmlClass pUmlClass, PUmlView pUmlView) {
        if (node instanceof CompilationUnit) {
            return ((CompilationUnit) node).getImports();
        } else if (node instanceof ClassOrInterfaceDeclaration) {
            pUmlClass.setClassName(((ClassOrInterfaceDeclaration) node).getNameAsString() + "." + pUmlClass.getClassName());

            Node parentNode = node.getParentNode().get();
            if (parentNode instanceof CompilationUnit) {
                PUmlRelation pUmlRelation = new PUmlRelation();
                pUmlRelation.setChild(getPackageNamePrefix(pUmlClass.getPackageName()) + pUmlClass.getClassName());
                pUmlRelation.setParent(getPackageNamePrefix(pUmlClass.getPackageName()) + pUmlClass.getClassName().substring(0, pUmlClass.getClassName().lastIndexOf(".")));
                pUmlRelation.setRelation(RelationType.IMPORT);
                pUmlView.addPUmlRelation(pUmlRelation);
            }
            parseImport(parentNode, pUmlClass, pUmlView);
        }
        return null;
    }

    private String getPackageNamePrefix(String packageName) {
        if (packageName == null || packageName.trim().equals("")) {
            return "";
        } else {
            return packageName + ".";
        }
    }
}
