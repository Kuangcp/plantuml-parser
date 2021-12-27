package com.github.kuangcp.parser;

import com.blade.kit.JsonKit;
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
import java.util.Set;

/**
 * @author https://github.com/kuangcp on 2021-12-27 00:22
 */
public class StoreFileUtil {

    private static final Logger log = LoggerFactory.getLogger(StoreFileUtil.class);

    public static Set<String> loadFromConfig() {

        final File configFile = getConfigFile();
        try {
            final String json = FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);
            return JsonKit.formJson(json, HashSet.class);
        } catch (IOException e) {
            log.error("", e);
        }
        return new HashSet<>();
    }

    static boolean invalidPath(String path) {
        final String homeDir = System.getProperty("user.home");
        if (!path.startsWith(homeDir)) {
            return true;
        }
        if (path.contains("..")) {
            return true;
        }
        String allowList = System.getenv("parseAllowList");
        if (Objects.nonNull(allowList)) {
            return !path.startsWith(allowList);
        }

        return false;
    }

    public static void storeToConfig(Set<String> list) {
        final String s = JsonKit.toString(list);
        log.info("cache: {}", s);

        File file = getConfigFile();
        try {
            FileUtils.write(file, s, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("", e);
        }
    }

    private static File getConfigFile() {
        final String osName = System.getProperty("os.name");
        final String homeDir = System.getProperty("user.home");
        File file;
        if (Objects.equals(osName, "Linux")) {
            file = new File(homeDir + "/.plantuml-parser/cache.json");
        } else {
            file = new File(homeDir + "\\.plantuml-parser\\cache.json");
        }
        return file;
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

    static File buildCacheFile(String path) {
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
}
