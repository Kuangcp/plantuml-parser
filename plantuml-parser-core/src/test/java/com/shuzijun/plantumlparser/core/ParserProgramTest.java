package com.shuzijun.plantumlparser.core;

import org.junit.Test;

/**
 * @author https://github.com/kuangcp on 2021-12-25 22:00
 */
public class ParserProgramTest {
    @Test
    public void testSSS() throws Exception {
        final ParserConfig parserConfig = new ParserConfig();
        String dir = "/home/kcp/Code/Java/plantuml-parser-no/plantuml-parser-core/src/test/java/com/shuzijun/plantumlparser/app1";
        final String dir1 = System.getenv("dir");
        if (dir1 != null) {
            dir = dir1;
        }
        parserConfig.addFilePath(dir);
        parserConfig.addFieldModifier("public");
        parserConfig.addFieldModifier("private");
        parserConfig.addMethodModifier("public");

        parserConfig.setOutFilePath("./test.wsd");
        final ParserProgram app = new ParserProgram(parserConfig);
        app.execute();
    }
}