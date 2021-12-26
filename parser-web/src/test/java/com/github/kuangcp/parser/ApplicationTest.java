package com.github.kuangcp.parser;

import org.junit.Test;


/**
 * @author https://github.com/kuangcp on 2021-12-26 23:33
 */
public class ApplicationTest {

    @Test
    public void testMd5() throws Exception {
        final String str = CacheFileList.md5("/home/kcp/Code/Java/plantuml-parser");
        System.out.println(str);
    }

}