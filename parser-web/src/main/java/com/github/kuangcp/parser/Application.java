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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author https://github.com/kuangcp on 2021-12-26 19:57
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final Set<String> svgCache = StoreFileUtil.loadFromConfig();

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> StoreFileUtil.storeToConfig(svgCache)));

        // TODO 文件初始化到内存，退出或宕机 存储到文件
        Blade.of()
                .get("uml", Application::umlHandler)
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

        final String pathMd5 = StoreFileUtil.md5(path);

        try {
            File file = StoreFileUtil.buildCacheFile(path);
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

    private static void writeSvgCache(String path, String content) {
        try {
            FileUtils.write(StoreFileUtil.buildCacheFile(path), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("", e);
        }
    }

    private static void umlHandler(RouteContext ctx) {
        final String path = ctx.query("path");
        final String txt = ctx.query("txt");
        if (Objects.isNull(path) || path.trim().equals("")) {
            indexPage(ctx);
            return;
        }

        if (StoreFileUtil.invalidPath(path)) {
            ctx.json("{\"msg\":\"error path\"}");
            return;
        }

        if (svgCache(ctx)) {
            return;
        }

        log.info("start path={}", path);

        final ParserConfig parserConfig = new ParserConfig();
        parserConfig.addFieldModifier("public", "private");
        parserConfig.addMethodModifier("public", "private");

        parserConfig.setShowSerializableImpl(false);
        parserConfig.setShowConstructors(false);
        parserConfig.setShowMethod(true);
        parserConfig.setOnlyShowRelationClass(true);

        final ParserProgram app = new ParserProgram(parserConfig);
        parserConfig.addFilePath(path);

        // 仅生成 wsd 文件
        if (Objects.nonNull(txt) && !txt.isEmpty()) {
            final Optional<String> wsdOpt = app.buildWsd();
            ctx.text(wsdOpt.orElse("NO Text"));
            return;
        }

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
