package io.jcloud.utils;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

public final class FileUtils {

    private static final int NO_RECURSIVE = 1;

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

    public static void copyFileTo(File file, Path target) {
        try {
            org.apache.commons.io.FileUtils.copyFileToDirectory(file, target.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Could not copy project.", e);
        }
    }

    public static void copyDirectoryTo(Path source, Path target) {
        try {
            org.apache.commons.io.FileUtils.copyDirectory(source.toFile(), target.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Could not copy project.", e);
        }
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
            return IOUtils.toString(FileUtils.class.getResourceAsStream(file), StandardCharsets.UTF_8);
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

    public static void createDirectoryIfDoesNotExist(Path folder) {
        if (!Files.exists(folder)) {
            folder.toFile().mkdirs();
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

    public static Optional<String> findFile(Path basePath, String endsWith) {
        try (Stream<Path> binariesFound = Files.find(basePath, NO_RECURSIVE,
                (path, basicFileAttributes) -> path.toFile().getName().endsWith(endsWith))) {
            return binariesFound.map(path -> path.normalize().toString()).findFirst();
        } catch (IOException ex) {
            // ignored
        }

        return Optional.empty();
    }

}
