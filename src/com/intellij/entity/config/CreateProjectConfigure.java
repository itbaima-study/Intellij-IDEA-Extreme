package com.intellij.entity.config;

/**
 * 创建项目配置实体类
 */
public class CreateProjectConfigure {

    private final boolean hasDefaultCode;
    private final String name;
    private final String path;
    private final String buildSystem;
    private final String language;

    public CreateProjectConfigure(boolean hasDefaultCode, String name, String path, String buildSystem, String language) {
        this.hasDefaultCode = hasDefaultCode;
        this.name = name;
        this.path = path;
        this.buildSystem = buildSystem;
        this.language = language;
    }

    public boolean hasDefaultCode() {
        return hasDefaultCode;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "CreateProjectConfigure{" +
                "hasDefaultCode=" + hasDefaultCode +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", buildSystem='" + buildSystem + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}
