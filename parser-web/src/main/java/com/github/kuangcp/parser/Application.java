package com.github.kuangcp.parser;

import com.blade.Blade;
import com.blade.mvc.RouteContext;
import com.blade.mvc.http.Response;
import com.shuzijun.plantumlparser.core.ParserConfig;
import com.shuzijun.plantumlparser.core.ParserProgram;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author https://github.com/kuangcp on 2021-12-26 19:57
 */
public class Application {

    private static final Map<String, String> svgCache = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        Blade.of()
                .get("uml", Application::svgHandler)
                .get("/basic-routes-example", ctx -> ctx.text("GET called"))
                .post("/basic-routes-example", ctx -> ctx.text("POST called"))
                .put("/basic-routes-example", ctx -> ctx.text("PUT called"))
                .delete("/basic-routes-example", ctx -> ctx.text("DELETE called"))
                .start(Application.class, args);
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
            ctx.response().header("Content-Type", "image/svg+xml");
            ctx.text(svgCache.get(path));
            return;
        }

        final ParserConfig parserConfig = new ParserConfig();
        parserConfig.addFieldModifier("public", "private");
        parserConfig.addMethodModifier("public", "private");

        final ParserProgram app = new ParserProgram(parserConfig);
        parserConfig.addFilePath(path);

        final Optional<String> svgOpt = app.buildSvg();
        if (svgOpt.isPresent()) {
            final Response response = ctx.response();
            response.header("Content-Type", "image/svg+xml");
            ctx.text(svgOpt.get());
            svgCache.put(path, svgOpt.get());
        } else {
            ctx.json("{\"msg\":\"generate error\"}");
        }
    }
}
