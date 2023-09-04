package com.intellij.entity;

import java.io.Serializable;

/**
 * 项目实体类（项目列表、保存项目用到）
 */
public class ProjectEntity implements Serializable {
    private final String name;  //项目名称
    private final String filepath;   //项目位置

    public ProjectEntity(String name, String filepath) {
        this.name = name;
        this.filepath = filepath;
    }

    public String getName() {
        return name;
    }

    public String getFilePath() {
        return filepath;
    }
}
