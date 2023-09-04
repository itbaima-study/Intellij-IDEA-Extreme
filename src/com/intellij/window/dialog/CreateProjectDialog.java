package com.intellij.window.dialog;

import com.intellij.entity.config.CreateProjectConfigure;
import com.intellij.manage.FileManager;
import com.intellij.window.MainWindow;
import com.intellij.window.WelcomeWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * 项目创建对话框
 */
public class CreateProjectDialog extends AbstractDialog {
    private JTextField nameField;   //项目名称输入框
    private JTextField pathField;   //项目路径输入框
    private JLabel finalPath;     //最终保存位置展示标签
    private JCheckBox defaultCode;   //是否需要生成默认代码
    private JButton createButton;   //创建项目按钮

    private final WelcomeWindow parentWindow; //这里暂时存一下父窗口，后面方便一起关掉

    public CreateProjectDialog(WelcomeWindow parent){
        super(parent, "创建新的项目", new Dimension(500, 300));
        this.parentWindow = parent;
    }

    @Override
    protected void initDialogContent() {
        //首先添加最左侧的标签
        this.addComponent(new JLabel("项目名称："), label -> label.setBounds(20, 20, 100, 20));
        this.addComponent(new JLabel("项目路径："), label -> label.setBounds(20, 60, 100, 20));
        this.addComponent(new JLabel("项目语言："), label -> label.setBounds(20, 120, 100, 20));
        this.addComponent(new JLabel("构建系统："), label -> label.setBounds(20, 160, 100, 20));

        //然后是两个文本框，每个文本框都要添加监听器，当输入时，会实时更新最终路径展示标签
        this.addComponent((nameField = new JTextField()), field -> {
            field.setBounds(100, 20, 380, 25);
            field.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    onKeyPress();
                }
            });
        });
        //路径选择文本框，此文本框还有一个文件选择器打开按钮和最终路径展示的标签
        this.addComponent((pathField = new JTextField()), field -> {
            field.setBounds(100, 60, 330, 25);
            field.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    onKeyPress();
                }
            });
        });
        this.addComponent((finalPath = new JLabel("请填写项目名称和项目保存路径！")),
                label -> label.setBounds(100, 85, 380, 20));
        this.addComponent(new JButton("..."), button -> {
            button.setBounds(435, 60, 45, 25);
            button.addActionListener(e -> selectDirectory());
        });

        //然后是下面的两个选择框（目前只实现了一种，所以说就不扩展了）
        this.addComponent(new JComboBox<String>(), box -> {
            box.setBounds(100, 120, 200, 25);
            box.addItem("Java");
        });
        this.addComponent(new JComboBox<String>(), box -> {
            box.setBounds(100, 160, 200, 25);
            box.addItem("Intellij");
        });

        //最后是是否生成默认代码的勾选框和创建按钮
        this.addComponent((defaultCode = new JCheckBox("是否生成默认代码")),
                box -> box.setBounds(100, 190, 200, 25));
        this.addComponent((createButton = new JButton("创建项目")), button -> {
            button.setBounds(390, 240, 100, 25);
            button.setEnabled(false);
            button.setToolTipText("请先填写上述配置信息！");
            button.addActionListener(e -> {
                boolean hasDefaultCode = defaultCode.isSelected();
                String name = nameField.getText();
                String path = pathField.getText();
                String buildSystem = "Intellij";
                String language = "Java";
                if(!FileManager.createProject(new CreateProjectConfigure(hasDefaultCode, name, path, buildSystem, language))) {
                    JOptionPane.showMessageDialog(this, "未知错误，创建项目失败！");
                    return;
                }
                //项目创建成功，关闭所有窗口，打开项目编辑窗口
                this.closeDialog();
                this.parentWindow.dispose();  //这里别用closeWindow，因为默认是退出程序
                //打开项目编辑窗口
                MainWindow window = new MainWindow(name, path + "/" + name);
                window.openWindow();
            });
        });
    }

    private void onKeyPress(){
        if(nameField.getText() == null || pathField.getText() == null) return;
        if(!pathField.getText().isEmpty() && !nameField.getText().isEmpty()) {
            finalPath.setText("保存位置："+ pathField.getText() + "/" + nameField.getText());
            createButton.setEnabled(true);
            createButton.setToolTipText("点击创建项目");
        } else {
            createButton.setEnabled(false);
            createButton.setToolTipText("请先填写上述配置信息！");
        }
    }

    private void selectDirectory(){
        DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(CreateProjectDialog.this);
        directoryChooserDialog.openDialog();
        File selectedFile = directoryChooserDialog.getSelectedFile();
        if(selectedFile != null) {
            pathField.setText(selectedFile.getAbsolutePath());
            if(nameField.getText() != null && !nameField.getText().isEmpty()) {
                finalPath.setText("保存位置："+ pathField.getText() + "/" + nameField.getText());
                createButton.setEnabled(true);
                createButton.setToolTipText("点击创建项目");
            } else {
                createButton.setEnabled(false);
                createButton.setToolTipText("请先填写上述配置信息！");
            }
        }
    }
}
