package com.intellij.manage;

import com.intellij.entity.ProjectEntity;
import com.intellij.entity.config.ApplicationConfigure;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ProjectManager {
    private static ProjectManager INSTANCE;
    private static ApplicationConfigure configure;

    private ProjectManager(){}

    public static void loadProjects() throws IOException, ClassNotFoundException {
        File file = new File("files/config");
        if(!file.exists()) {
            if(file.createNewFile()) {
                configure = new ApplicationConfigure();
                saveConfigure();
            } else {
                throw new RuntimeException("无法创建配置文件！");
            }
        }else {
            ObjectInputStream stream = new ObjectInputStream(Files.newInputStream(file.toPath()));
            configure = (ApplicationConfigure) stream.readObject();
            stream.close();
        }
        INSTANCE = new ProjectManager();
    }

    public static void saveConfigure(){
        try (ObjectOutputStream stream = new ObjectOutputStream(Files.newOutputStream(Paths.get("files/config")))){
            stream.writeObject(configure);
            stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ProjectManager getManager() {
        return INSTANCE;
    }

    public Stream<ProjectEntity> getProjectList(){
        return configure.getList().stream();
    }

    public static void deleteProject(String name){
        configure.removeProjectEntityIf(project -> project.getName().equals(name));
    }

    public static void createProject(String name, String filepath){
        configure.addProjectEntity(new ProjectEntity(name, filepath + "/" + name));
    }
}
