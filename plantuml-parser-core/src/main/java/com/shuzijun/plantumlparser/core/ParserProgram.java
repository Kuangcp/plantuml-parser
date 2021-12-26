package com.shuzijun.plantumlparser.core;

import com.credibledoc.plantuml.svggenerator.SvgGeneratorService;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

/**
 * 解析程序
 *
 * @author shuzijun
 */
public class ParserProgram {

    private final ParserConfig parserConfig;

    public ParserProgram(ParserConfig parserConfig) {
        this.parserConfig = parserConfig;
    }

    public void execute() throws IOException {
        PUmlView pUmlView = readClass();

        // std out
        if (this.parserConfig.getOutFilePath() == null) {
            System.out.println("\nNO OUTPUT FILE\n");
            System.out.println(pUmlView);
            return;
        }


        File outFile = new File(this.parserConfig.getOutFilePath());
        FileUtils.forceMkdirParent(outFile);
        if (!outFile.exists()) {
            final boolean success = outFile.createNewFile();
            if (!success) {
                System.out.println("CREATE FILE FAILED");
                return;
            }
        }
        if (parserConfig.isOutSvg()) {
            String svg = SvgGeneratorService.getInstance().generateSvgFromPlantUml(pUmlView.buildUmlContent());
            FileUtils.write(outFile, svg, StandardCharsets.UTF_8);
        } else {
            FileUtils.write(outFile, pUmlView.buildUmlContent(), StandardCharsets.UTF_8);
        }
    }

    private PUmlView readClass() throws FileNotFoundException {
        StaticJavaParser.getConfiguration().setLanguageLevel(Optional.ofNullable(parserConfig.getLanguageLevel())
                .orElse(ParserConfiguration.LanguageLevel.JAVA_8));
        PUmlView pUmlView = new PUmlView(this.parserConfig);

        Set<File> files = this.parserConfig.getFilePaths();
        for (File file : files) {
            CompilationUnit compilationUnit = StaticJavaParser.parse(file);
            Optional<PackageDeclaration> packageDeclaration = compilationUnit.getPackageDeclaration();
            final String packageName = packageDeclaration.isPresent() ? packageDeclaration.get().getNameAsString() : "";
            VoidVisitor<PUmlView> classNameCollector = new ClassVoidVisitor(packageName, parserConfig);
            classNameCollector.visit(compilationUnit, pUmlView);
        }
        return pUmlView;
    }

    public Optional<String> buildSvg() {
        try {
            PUmlView pUmlView = readClass();
            return Optional.of(SvgGeneratorService.getInstance().generateSvgFromPlantUml(pUmlView.buildUmlContent()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
