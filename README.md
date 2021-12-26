# plantuml-parser ![Gradle Package](https://github.com/shuzijun/plantuml-parser/workflows/Gradle%20Package/badge.svg) ![plugin](https://github.com/shuzijun/plantuml-parser/workflows/plugin/badge.svg)

将Java源代码转换为 plantuml
Convert the Java source code to Plantuml

## plantuml-parser-core

```java
    public static void main(String[]args)throws IOException{
       final ParserConfig parserConfig = new ParserConfig();
       parserConfig.addFilePath("absolute path");
       parserConfig.addFieldModifier("public");
       parserConfig.addMethodModifier("public");

       parserConfig.setOutSvg(true);
       parserConfig.setOutFilePath("./test.svg");
       final ParserProgram app = new ParserProgram(parserConfig);
       app.execute();
    }
```

## parser-web

1. gradle run
2. open http://localhost:9000/uml?path={java project absolute path}&refresh=true
    1. refresh: drop cache

## TODO
1. 重复依赖关系 Relation 去除
1. 以关注模块为起点，追溯父级的方式延展 突出关注模块的 类设计方案
1. 循环依赖检测
