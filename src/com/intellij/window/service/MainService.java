package com.intellij.window.service;

import com.intellij.entity.ProcessResult;
import com.intellij.entity.config.ProjectConfigure;
import com.intellij.manage.ProcessExecuteEngine;
import com.intellij.manage.FileManager;
import com.intellij.window.MainWindow;
import com.intellij.window.dialog.CompileErrorDialog;
import com.intellij.window.dialog.ProjectConfigDialog;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainService extends AbstractService {
    //当前项目的路径和项目名称
    private String path;
    //当前项目的配置文件，包括主类、java可执行文件位置等。
    private ProjectConfigure configure;
    //用于记录当前正在编辑的文件
    private File currentFile;
    //重做管理器，用于编辑框支持撤销和重做操作的
    private UndoManager undoManager;
    //用于记录当前项目是否处于运行状态
    private boolean isProjectRunning = false;

    /**
     * 设定当前项目的名称和路径
     * @param path 路径
     */
    public void setPath(String path){
        this.path = path.replace("\\", "/");
    }

    /**
     * 获取当前项目的配置
     * @return 项目配置
     */
    public ProjectConfigure getConfigure() {
        return configure;
    }

    /**
     * 加载项目配置文件
     */
    public void loadProjectConfig(){
        File file = new File(path+"/.idea");
        if(file.exists()) {
            try (ObjectInputStream stream = new ObjectInputStream(Files.newInputStream(file.toPath()))){
                configure = (ProjectConfigure) stream.readObject();
            }catch (Exception e){
                e.printStackTrace();
            }
        } else {
            this.updateAndSaveConfigure(new ProjectConfigure("", "java"));
        }
    }

    /**
     * 更新并保存新的设置
     * @param configure 新的设置
     */
    public void updateAndSaveConfigure(ProjectConfigure configure){
        JButton button = this.getComponent("main.button.run");
        this.configure = configure;
        try (ObjectOutputStream stream = new ObjectOutputStream(Files.newOutputStream(Paths.get(path+"/.idea")))){
            stream.writeObject(configure);
            stream.flush();
            if(button != null) {
                if(configure.getMainClass().isEmpty()) {
                    button.setEnabled(false);
                    button.setToolTipText("请先完成项目运行配置！");
                } else {
                    button.setEnabled(true);
                    button.setToolTipText("点击编译运行项目");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 运行按钮的行为，包括以下两种行为：
     * - 如果项目处于运行状态，那么点击就会停止项目。
     * - 如果项目没有处于运行状态，那么就会启动项目。
     */
    public void runButtonAction(){
        MainWindow window = (MainWindow) this.getWindow();
        JButton button = this.getComponent("main.button.run");
        JTextArea consoleArea = this.getComponent("main.textarea.console");
        //判断当前项目是否已经开始运行了，分别进行操作
        if(!this.isProjectRunning) {
            //如果项目没有运行，那么需要先编译项目源代码，如果编译成功，那么就可以开始运行项目了
            button.setEnabled(false);
            consoleArea.setText("正在编译项目源代码...");
            ProcessResult result = ProcessExecuteEngine.buildProject(path);
            if(result.getExitCode() != 0) {
                CompileErrorDialog dialog = new CompileErrorDialog(this.getWindow(), result.getOutput());
                dialog.openDialog();
                button.setEnabled(true);
                return;
            }
            //项目编译完成之后，可能会新增文件，所以需要刷新一下文件树
            window.refreshFileTree();
            //新开一个线程实时对项目的运行进行监控，并实时将项目的输出内容更新到控制台
            new Thread(() -> {
                this.isProjectRunning = true;
                consoleArea.setText("正在编译项目源代码...编译完成，程序已启动：\n");
                button.setText("停止");
                button.setEnabled(true);
                //准备工作完成之后，就可以正式启动进程了，这里最后会返回执行结果
                ProcessResult res = ProcessExecuteEngine.startProcess(
                                path, configure.getJavaCommand(), configure.getMainClass(), consoleArea::append);
                if(res.getExitCode() != 0)
                    consoleArea.append(res.getOutput());
                consoleArea.append("\n进程已结束，退出代码 "+res.getExitCode());
                button.setText("运行");
                this.isProjectRunning = false;
            }).start();
        } else {
            //如果项目正在运行，那么点击按钮就相当于是结束项目运行
            ProcessExecuteEngine.stopProcess();
            this.isProjectRunning = false;
        }
    }

    /**
     * 构建按钮的行为，很明显，直接构建就完事了
     */
    public void buildButtonAction(){
        MainWindow window = (MainWindow) this.getWindow();
        ProcessResult result = ProcessExecuteEngine.buildProject(path);
        if(result.getExitCode() == 0) {
            JOptionPane.showMessageDialog(window, "编译成功！");
        } else {
            CompileErrorDialog dialog = new CompileErrorDialog(window, result.getOutput());
            dialog.openDialog();
        }
        window.refreshFileTree();
    }

    /**
     * 设置按钮的行为，更简单了，直接打开设置面板就完事
     */
    public void settingButtonAction(){
        MainWindow window = (MainWindow) this.getWindow();
        ProjectConfigDialog dialog = new ProjectConfigDialog(window, this, configure);
        dialog.openDialog();
    }

    /**
     * 创建一个新的源代码新的文件并生成默认代码
     */
    public void createNewFile(){
        String newFileName = JOptionPane.showInputDialog(this.getWindow(),
                "请输入你要创建的Java类名称（含包名，如 com.test.Main）", "创建新的Java文件", JOptionPane.PLAIN_MESSAGE);
        this.createFile(newFileName);
    }

    public void deleteProjectFile(){
        String newFileName = JOptionPane.showInputDialog(this.getWindow(),
                "请输入你要删除的Java类名称（含包名，如 com.test.Main）", "删除Java文件", JOptionPane.WARNING_MESSAGE);
        this.deleteFile(newFileName);
    }

    /**
     * 配置文件树的右键弹出窗口
     * @return MouseAdapter
     */
    public MouseAdapter fileTreeRightClick(){
        JTree fileTree = this.getComponent("main.tree.files");
        JPopupMenu treePopupMenu = this.getComponent("main.popup.tree");
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3)
                    treePopupMenu.show(fileTree, e.getX(), e.getY());
            }
        };
    }

    /**
     * 配置编辑框的各项功能
     */
    public void setupEditArea(){
        JTextArea editArea = this.getComponent("main.textarea.edit");
        //当文本内容发生变化时，自动写入到文件中
        editArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                MainService.this.saveFile();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                MainService.this.saveFile();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                MainService.this.saveFile();
            }
        });
        //按下Tab键时，应该输入四个空格，而不是一个Tab缩进（不然太丑）
        editArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == 9) {
                    e.consume();
                    editArea.insert("    ", editArea.getCaretPosition());
                }
            }
        });
        //由于默认的文本区域不支持重做和撤销操作，需要使用UndoManager进行配置，这里添加快捷键
        editArea.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
        editArea.getActionMap().put("Redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(undoManager.canRedo()) undoManager.redo();
            }
        });
        editArea.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
        editArea.getActionMap().put("Undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(undoManager.canUndo()) undoManager.undo();
            }
        });
    }

    /**
     * 让控制台输入重定向到进程的系统输入中
     * @return KeyAdapter
     */
    public KeyAdapter inputRedirect(){
        JTextArea consoleArea = this.getComponent("main.textarea.console");
        return new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(isProjectRunning) {
                    String str = String.valueOf(e.getKeyChar());
                    ProcessExecuteEngine.redirectToProcess(str);
                    consoleArea.append(str);
                }
            }
        };
    }

    /**
     * 切换当前编辑的文件，并更新编辑面板中的内容
     * @param path 文件路径
     */
    public void switchEditFile(String path) {
        JTextArea editArea = this.getComponent("main.textarea.edit");
        currentFile = null;
        File file = new File(path);
        if(file.isDirectory()) return;
        editArea.getDocument().removeUndoableEditListener(undoManager);
        if(file.getName().endsWith(".class")) {
            editArea.setText(ProcessExecuteEngine.decompileCode(file.getAbsolutePath()));
            editArea.setEditable(false);
        } else {
            try(FileReader reader = new FileReader(file)) {
                StringBuilder builder = new StringBuilder();
                int len;
                char[] chars = new char[1024];
                while ((len = reader.read(chars)) > 0)
                    builder.append(chars, 0, len);
                editArea.setText(builder.toString());
                editArea.setEditable(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        editArea.getDocument().addUndoableEditListener((undoManager = new UndoManager()));
        currentFile = file;
    }

    private void deleteFile(String name){
        if(name == null) return;
        String[] split = name.split("\\.");
        String className = split[split.length - 1];
        String packageName = name.substring(0, name.length() - className.length() - 1);

        File file = new File(path+"/src/"+packageName.replace(".", "/")+"/"+className+".java");
        if(file.exists() && file.delete()) {
            JOptionPane.showMessageDialog(this.getWindow(), "文件删除成功！");
        }else {
            JOptionPane.showMessageDialog(this.getWindow(), "文件删除失败，文件不存在？");
        }
        MainWindow window = (MainWindow) this.getWindow();
        window.refreshFileTree();
    }

    /**
     * 创建源文件，并生成默认代码
     * @param name 名称
     */
    private void createFile(String name){
        MainWindow window = (MainWindow) this.getWindow();
        if(name == null) return;
        String[] split = name.split("\\.");
        String className = split[split.length - 1];
        String packageName = name.substring(0, name.length() - className.length() - 1);

        try {
            File dir = new File(path+"/src/"+packageName.replace(".", "/"));
            if(!dir.exists() && !dir.mkdirs()) {
                JOptionPane.showMessageDialog(window, "无法创建文件夹！");
                return;
            }
            File file = new File(path+"/src/"+packageName.replace(".", "/")+"/"+className+".java");
            if(file.exists() || !file.createNewFile()) {
                JOptionPane.showMessageDialog(window, "无法创建，此文件已存在！");
                return;
            }
            FileWriter writer = new FileWriter(file);
            writer.write(FileManager.defaultCode(className, packageName));
            writer.flush();
            window.refreshFileTree();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存当前编辑框中的内容到当前文件中
     */
    private void saveFile(){
        JTextArea editArea = this.getComponent("main.textarea.edit");
        if(currentFile == null) return;
        try (FileWriter writer = new FileWriter(currentFile)){
            writer.write(editArea.getText());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
