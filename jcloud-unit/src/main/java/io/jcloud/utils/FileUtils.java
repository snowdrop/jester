package io.jcloud.utils;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;

public final class FileUtils {

    private FileUtils() {

    }

    public static Path copyContentTo(String content, Path target) {
        try {
            Files.writeString(target, content);
        } catch (IOException e) {
            fail("Failed when writing file " + target + ". Caused by " + e.getMessage());
        }

        return target;
    }

    public static String loadFile(File file) {
        try {
            return org.apache.commons.io.FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            fail("Could not load file " + file + " . Caused by " + e.getMessage());
        }

        return null;
    }

    public static String loadFile(String file) {
        try {
            return IOUtils.toString(
                    FileUtils.class.getResourceAsStream(file),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            fail("Could not load file " + file + " . Caused by " + e.getMessage());
        }

        return EMPTY;
    }

    public static void recreateDirectory(Path folder) {
        deletePath(folder);
        createDirectory(folder);
    }

    public static void createDirectory(Path folder) {
        try {
            org.apache.commons.io.FileUtils.forceMkdir(folder.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deletePath(Path folder) {
        deleteFile(folder.toFile());
    }

    public static void deleteFile(File file) {
        if (file.exists()) {
            try {
                org.apache.commons.io.FileUtils.forceDelete(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
