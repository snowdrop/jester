package io.jcloud.utils;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

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
            org.apache.commons.io.FileUtils.createParentDirectories(target.toFile());
            Files.writeString(target, content, CREATE, APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed when writing file " + target, e);
        }

        return target;
    }

    public static String loadFile(File file) {
        try {
            return org.apache.commons.io.FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not load file " + file, e);
        }
    }

    public static String loadFile(String file) {
        try {
            return IOUtils.toString(
                    FileUtils.class.getResourceAsStream(file),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not load file " + file, e);
        }
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
