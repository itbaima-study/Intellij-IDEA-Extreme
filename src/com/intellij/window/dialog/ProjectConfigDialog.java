package com.intellij.window.dialog;

import com.intellij.entity.config.ProjectConfigure;
import com.intellij.window.MainWindow;
import com.intellij.window.service.MainService;

import javax.swing.*;
import java.awt.*;

/**
 * 项目配置对话框
 */
public class ProjectConfigDialog extends AbstractDialog{

    private final ProjectConfigure configure;
    private final MainService service;
    private JTextField mainClass;
    private JTextField javaCommand;
    public ProjectConfigDialog(MainWindow parent, MainService service, ProjectConfigure configure) {
        super(parent, "项目配置", new Dimension(400, 220));
        this.configure = configure;
        this.service = service;
        this.initComponentContent();
    }

    @Override
    protected void initDialogContent() {
        //首先是所有的名称
        this.addComponent(new JLabel("主类："), label -> label.setBounds(20, 20, 100, 20));
        this.addComponent(new JLabel("Java位置："), label -> label.setBounds(20, 80, 100, 20));
        //接着是两个配置框
        this.addComponent((mainClass = new JTextField()), field -> field.setBounds(100, 20, 280, 20));
        this.addComponent((javaCommand = new JTextField()), field -> field.setBounds(100, 80, 280, 20));
        //然后是对应的描述
        this.addComponent(new JLabel("主类请使用包名.类名，如com.test.Main"),
                label -> label.setBounds(100, 45, 300, 20));
        this.addComponent(new JLabel("此选项用于指定java可执行文件位置，一般情况下"),
                label -> label.setBounds(100, 105, 300, 20));
        this.addComponent(new JLabel("使用系统默认位置，如：/usr/bin/java"),
                label -> label.setBounds(100, 120, 300, 20));
        //最后是确认按钮
        this.addComponent(new JButton("确定"), button -> {
            button.setBounds(160, 155, 80, 25);
            button.addActionListener(e -> {
                this.updateConfigure();
                this.closeDialog();
            });
        });
    }

    private void initComponentContent(){
        mainClass.setText(configure.getMainClass());
        javaCommand.setText(configure.getJavaCommand());
    }

    private void updateConfigure(){
        ProjectConfigure config = new ProjectConfigure(mainClass.getText(), javaCommand.getText());
        service.updateAndSaveConfigure(config);
    }
}
