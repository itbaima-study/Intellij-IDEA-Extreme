package com.intellij.window.dialog;

import com.intellij.window.AbstractWindow;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * 目录选择对话框
 */
public class DirectoryChooserDialog extends AbstractDialog {

    private JFileChooser fileChooser;
    public DirectoryChooserDialog(AbstractDialog parent) {
        super(parent, "请选择一个目录", new Dimension(600, 400));
    }

    public DirectoryChooserDialog(AbstractWindow parent) {
        super(parent, "请选择一个目录", new Dimension(600, 400));
    }

    @Override
    protected void initDialogContent() {
        this.setLayout(new BorderLayout());
        this.setResizable(true);
        fileChooser = new JFileChooser();
        this.addComponent(fileChooser, chooser -> {
            //设定文件选择器只能选择目录
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            //添加监听器，当选择完成后，就关闭当前窗口
            chooser.addActionListener(e -> DirectoryChooserDialog.this.closeDialog());
        });
    }

    public File getSelectedFile(){
        return fileChooser.getSelectedFile();
    }
}
