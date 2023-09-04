package com.intellij.entity.config;

import java.io.Serializable;

/**
 * 项目配置实体类
 */
public class ProjectConfigure implements Serializable {
    private final String mainClass;
    private final String javaCommand;

    public ProjectConfigure(String mainClass, String javaCommand) {
        this.mainClass = mainClass;
        this.javaCommand = javaCommand;
    }

    public String getJavaCommand() {
        return javaCommand;
    }

    public String getMainClass() {
        return mainClass;
    }
}
