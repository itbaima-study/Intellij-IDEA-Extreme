package com.intellij.entity.config;

import com.intellij.entity.ProjectEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 当前应用的配置实体类
 */
public class ApplicationConfigure implements Serializable {
    private final List<ProjectEntity> list = new ArrayList<>();

    public void addProjectEntity(ProjectEntity entity){
        list.add(entity);
    }

    public void removeProjectEntityIf(Predicate<ProjectEntity> predicate){
        list.removeIf(predicate);
    }

    public List<ProjectEntity> getList(){
        return list;
    }
}
