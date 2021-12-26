package com.github.kuangcp.parser;

import com.blade.Blade;
import com.blade.mvc.RouteContext;
import com.shuzijun.plantumlparser.core.ParserConfig;
import com.shuzijun.plantumlparser.core.ParserProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author https://github.com/kuangcp on 2021-12-26 19:57
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final Map<String, String> svgCache = new ConcurrentHashMap<>();

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
        if (!svgCache.isEmpty()) {
            idxBuilder.append("<h2>Recent:</h2><ol>");
            svgCache.keySet().stream().sorted()
                    .forEach(key -> idxBuilder.append("<li><a href=\"/uml?path=")
                            .append(key).append("\" > ")
                            .append(key).append("</a> </li>"));
            idxBuilder.append("</ol>");
        }
        idxBuilder.append("</body></html>");

        ctx.html(idxBuilder.toString());
    }

    private static void svgHandler(RouteContext ctx) {
        final String path = ctx.query("path");
        final String refresh = ctx.query("refresh");
        if (Objects.isNull(path)) {
            ctx.json("{\"msg\":\"no path\"}");
            return;
        }

        if (Objects.nonNull(refresh)) {
            svgCache.remove(path);
        } else if (svgCache.containsKey(path)) {
            ctx.response().contentType("image/svg+xml");
            ctx.text(svgCache.get(path));
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
            svgCache.put(path, svgOpt.get());
        } else {
            ctx.json("{\"msg\":\"generate error\"}");
        }
    }
}
