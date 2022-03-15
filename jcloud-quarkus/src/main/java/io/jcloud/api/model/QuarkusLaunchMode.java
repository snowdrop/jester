package io.jcloud.api.model;

public enum QuarkusLaunchMode {
    LEGACY_JAR("legacy-jar"), NATIVE("native"), JVM("jvm"), DEV("dev"), REMOTE_DEV("remote-dev");

    private final String name;

    QuarkusLaunchMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
