package com.github.kuangcp.parser;

import com.blade.Blade;
import com.blade.mvc.RouteContext;
import com.shuzijun.plantumlparser.core.ParserConfig;
import com.shuzijun.plantumlparser.core.ParserProgram;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author https://github.com/kuangcp on 2021-12-26 19:57
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final Set<String> svgCache = new HashSet<>();

    public static void main(String[] args) {

        Blade.of()
                .get("uml", Application::svgHandler)
                .get("/", Application::indexPage)
                .start(Application.class, args);
    }

    private static void indexPage(RouteContext ctx) {
        final StringBuilder idxBuilder = new StringBuilder("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Plant UML Reader</title>\n" +
                "</head>\n" +
                "<body>\n");

        idxBuilder.append("<h2>Recent:</h2>");
        if (!svgCache.isEmpty()) {
            idxBuilder.append("<ol>");
            svgCache.stream().sorted()
                    .forEach(key -> idxBuilder.append("<li><a href=\"/uml?path=")
                            .append(key).append("\" > ")
                            .append(key).append("</a> </li>"));
            idxBuilder.append("</ol>");
        }
        idxBuilder.append("</body></html>");

        ctx.html(idxBuilder.toString());
    }

    /**
     * @return 是否命中缓存
     */
    private static boolean svgCache(RouteContext ctx) {
        final String refresh = ctx.query("refresh");
        final String path = ctx.query("path");

        if (Objects.nonNull(refresh)) {
            svgCache.remove(path);
            return false;
        }

        final String pathMd5 = md5(path);

        try {
            File file = buildCacheFile(path);
            if (!file.exists()) {
                return false;
            }
            final String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            if (Objects.nonNull(fileContent) && !fileContent.equals("")) {
                log.info("hint cache: path={} md5={}", path, pathMd5);
                svgCache.add(path);
                ctx.response().contentType("image/svg+xml");
                ctx.text(fileContent);
                return true;
            }
        } catch (IOException e) {
            log.error("", e);
        }

        return false;
    }

    private static File buildCacheFile(String path) {
        final String osName = System.getProperty("os.name");
        final String homeDir = System.getProperty("user.home");
        final String pathMd5 = md5(path);

        File file;
        if (Objects.equals(osName, "Linux")) {
            file = new File(homeDir + "/.plantuml-parser/" + pathMd5 + ".svg");
        } else {
            file = new File(homeDir + "\\.plantuml-parser\\" + pathMd5 + ".svg");
        }
        try {
            FileUtils.forceMkdirParent(file);
        } catch (IOException e) {
            log.error("", e);
        }
        return file;
    }

    private static void writeSvgCache(String path, String content) {
        try {
            FileUtils.write(buildCacheFile(path), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("", e);
        }
    }

    static String md5(String str) {
        try {
            final MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(str.getBytes());
            StringBuilder result = new StringBuilder();
            final byte[] s = md5.digest();
            for (byte b : s) {
                result.append(Integer.toHexString((0x000000ff & b) | 0xffffff00).substring(6));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("", e);
        }
        return "";
    }


    private static void svgHandler(RouteContext ctx) {
        final String path = ctx.query("path");
        if (Objects.isNull(path) || path.trim().equals("")) {
            indexPage(ctx);
            return;
        }

        if (svgCache(ctx)) {
            return;
        }

        log.info("start path={}", path);
        final ParserConfig parserConfig = new ParserConfig();
        parserConfig.addFieldModifier("public", "private");
        parserConfig.addMethodModifier("public", "private");

        final ParserProgram app = new ParserProgram(parserConfig);
        parserConfig.addFilePath(path);

        final Optional<String> svgOpt = app.buildSvg();
        log.info("finish path={}", path);
        if (svgOpt.isPresent()) {
            ctx.response().contentType("image/svg+xml");
            ctx.text(svgOpt.get());
            writeSvgCache(path, svgOpt.get());
            svgCache.add(path);
        } else {
            ctx.json("{\"msg\":\"generate error\"}");
        }
    }
}
