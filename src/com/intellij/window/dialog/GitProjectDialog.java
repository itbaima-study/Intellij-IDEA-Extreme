package com.intellij.window.dialog;

import com.intellij.entity.ProcessResult;
import com.intellij.manage.ProcessExecuteEngine;
import com.intellij.manage.ProjectManager;
import com.intellij.window.MainWindow;
import com.intellij.window.WelcomeWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Git项目拉取对话框
 */
public class GitProjectDialog extends AbstractDialog{
    private JTextField location;
    private JTextField branch;
    private JTextField dir;
    private JButton startFetch;
    private final WelcomeWindow parent;

    public GitProjectDialog(WelcomeWindow parent) {
        super(parent, "从Git获取项目", new Dimension(400, 250));
        this.parent = parent;
    }

    @Override
    protected void initDialogContent() {
        //首先是所有的名称
        this.addComponent(new JLabel("远程地址："), label -> label.setBounds(20, 20, 100, 20));
        this.addComponent(new JLabel("远程分支："), label -> label.setBounds(20, 80, 100, 20));
        this.addComponent(new JLabel("项目路径："), label -> label.setBounds(20, 125, 100, 20));
        //接着是三个配置框
        this.addComponent((location = new JTextField()), field -> {
            field.setBounds(100, 20, 280, 20);
            field.addKeyListener(this.inputObserver());
        });
        this.addComponent((branch = new JTextField()), field -> {
            field.setBounds(100, 80, 280, 20);
            field.addKeyListener(this.inputObserver());
        });
        this.addComponent((dir = new JTextField()), field -> {
            field.setBounds(100, 125, 250, 20);
            field.addKeyListener(this.inputObserver());
        });
        //然后是对应的描述
        this.addComponent(new JLabel("Git远程仓库地址，注意，此功能需要您的电脑安"),
                label -> label.setBounds(100, 40, 300, 20));
        this.addComponent(new JLabel("装git命令行工具并且配置SSH之后才能使用！"),
                label -> label.setBounds(100, 55, 300, 20));
        this.addComponent(new JLabel("对应的远程仓库分支，如：main"),
                label -> label.setBounds(100, 100, 300, 20));
        this.addComponent(new JLabel("项目目录必须是一个已存在的空目录"),
                label -> label.setBounds(100, 145, 300, 20));
        //最后是按钮
        this.addComponent(new JButton("..."), button -> {
            button.setBounds(355, 125, 30, 20);
            button.addActionListener(e -> selectDirectory());
        });
        this.addComponent((startFetch = new JButton("开始获取")), button -> {
            button.setBounds(160, 180, 100, 25);
            button.setEnabled(false);
            button.addActionListener(e -> {
                startFetch.setEnabled(false);
                ProcessResult result = ProcessExecuteEngine.fetchFromGit(location.getText(), branch.getText(), dir.getText());
                if (result.getExitCode() == 0) {
                    this.closeDialog();
                    this.parent.dispose();
                    String[] split = dir.getText().split("/");
                    String name = split[split.length - 1];
                    ProjectManager.createProject(name, dir.getText().substring(0, dir.getText().length() - name.length()));
                    MainWindow window = new MainWindow(name, dir.getText());
                    window.openWindow();
                } else {
                    JOptionPane.showMessageDialog(this, result.getOutput(), "错误", JOptionPane.ERROR_MESSAGE);
                    startFetch.setEnabled(true);
                }
            });
        });
    }

    private void selectDirectory(){
        DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(this);
        directoryChooserDialog.openDialog();
        File selectedFile = directoryChooserDialog.getSelectedFile();
        if(selectedFile != null) {
            dir.setText(selectedFile.getAbsolutePath());
            startFetch.setEnabled(canStart());
        }
    }

    private KeyAdapter inputObserver(){
        return new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                startFetch.setEnabled(canStart());
            }
        };
    }

    private boolean canStart(){
        return !dir.getText().isEmpty() && !branch.getText().isEmpty() && !location.getText().isEmpty();
    }
}
