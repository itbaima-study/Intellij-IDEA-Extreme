package com.intellij;

import com.formdev.flatlaf.FlatDarkLaf;
import com.intellij.manage.ProjectManager;
import com.intellij.window.WelcomeWindow;

import javax.swing.*;

/**
 * 整个项目的主启动类，项目的运行就从这里开始
 */
public class Main {
    public static void main(String[] args) throws Exception {
        //加载项目
        ProjectManager.loadProjects();
        //加载皮肤
        UIManager.setLookAndFeel(new FlatDarkLaf());
        //初始化窗口
        WelcomeWindow startWindow = new WelcomeWindow();
        startWindow.openWindow();
    }
}
