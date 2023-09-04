package com.intellij.window;

import com.intellij.manage.ProcessExecuteEngine;
import com.intellij.window.enums.CloseAction;
import com.intellij.window.service.MainService;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class MainWindow extends AbstractWindow <MainService>{

    private final String path;
    private final String name;

    private DefaultMutableTreeNode root;

    public MainWindow(String name, String path) {
        super("项目："+name, new Dimension(1000, 600), true, MainService.class);
        //设定路径和项目名称，然后开始配置窗口内容
        this.path = path;
        this.name = name;
        //窗口关闭不能直接退出程序，因为要回到欢迎界面
        this.setDefaultCloseAction(CloseAction.DISPOSE);
        //为业务层设定当前项目的路径
        service.setPath(path);
        //然后是加载当前项目的配置，项目的配置不同会影响组件的某些显示状态
        service.loadProjectConfig();
        //最后再初始化窗口内容
        this.initWindowContent();
    }

    @Override
    protected void initWindowContent() {
        //我们的代码编辑主界面包括最上面的一排工具栏
        this.addComponent("main.panel.tools", new JPanel(), BorderLayout.NORTH, this::initControlTools);

        //以及左边的文件树区域和中间的代码编辑区域，还有最下面的控制台区域
        this.addComponent("main.panel.content", new JSplitPane(), BorderLayout.CENTER, panel -> {
            //这里我们先分出最下方控制台和中心区域两个部分，所以先纵向分割一下
            panel.setOrientation(JSplitPane.VERTICAL_SPLIT);

            //首先配置最下方的控制台区域
            panel.setBottomComponent(this.createConsole());
            panel.setDividerLocation(380);   //下面的分割条默认在 y = 400 位置上

            //这一块是中心区域，中心区域包含左侧文件树和右侧代码编辑界面
            JSplitPane centerPanel = new JSplitPane();
            centerPanel.setLeftComponent(this.createLeftPanel());
            centerPanel.setRightComponent(this.createRightPanel());
            centerPanel.setDividerLocation(200);   //中间的分割条默认在 x = 200 位置上
            panel.setTopComponent(centerPanel);
        });
    }

    /**
     * 对最上面一排工具栏包括里面的各个按钮进行初始化。
     * @param panel 工具栏面板
     */
    private void initControlTools(JPanel panel){
        //这里采用流式布局，直接让按钮居右按顺序放置
        panel.setPreferredSize(new Dimension(0, 35));
        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.RIGHT);
        panel.setLayout(layout);

        //第一个按钮是运行/停止按钮，这个按钮有两种状态，如果主类已经配置，那么就可以运行，否则就不能运行
        this.addComponent(panel, "main.button.run", new JButton("运行"), button -> {
            button.setPreferredSize(new Dimension(60, 25));
            if(service.getConfigure().getMainClass().isEmpty()) {  //判断主类是否已经配置
                button.setEnabled(false);
                button.setToolTipText("请先完成项目运行配置！");
            } else {
                button.setEnabled(true);
                button.setToolTipText("点击编译运行项目");
            }
            button.addActionListener(e -> service.runButtonAction());
        });
        //第二个按钮是构建按钮，通过它就可以快速对项目进行构建了
        this.addComponent(panel, "main.button.build", new JButton("构建"), button -> {
            button.setPreferredSize(new Dimension(60, 25));
            button.addActionListener(e -> service.buildButtonAction());
        });
        //第三个是设置按钮，这个按钮也比较简单，直接打开对应的配置对话框就可以了
        this.addComponent(panel, "main.button.settings", new JButton("设置"), button -> {
            button.setPreferredSize(new Dimension(60, 25));
            button.addActionListener(e -> service.settingButtonAction());
        });
    }

    /**
     * 创建左侧文件树板块，用于展示整个项目的文件列表
     * @return 文件树板块
     */
    private JScrollPane createLeftPanel(){
        //首先配置文件树
        root = new DefaultMutableTreeNode(new NodeData(path, name));
        buildTreeNode(root);
        JTree fileTree = new JTree(root);
        this.mapComponent("main.tree.files", fileTree);
        fileTree.addTreeSelectionListener(e -> {
            TreePath treePath = e.getPath();
            StringBuilder filePath = new StringBuilder(this.path);
            for (int i = 1; i < treePath.getPathCount(); i++)
                filePath.append("/").append(treePath.getPathComponent(i));
            this.service.switchEditFile(filePath.toString());
        });
        //接着是右键文件树的弹出菜单，对文件进行各种操作，包括创建新的源文件和删除源文件
        JPopupMenu treePopupMenu = new JPopupMenu();
        this.mapComponent("main.popup.tree", treePopupMenu);
        this.add(treePopupMenu);
        JMenuItem createItem = new JMenuItem("创建源文件");
        createItem.addActionListener(e -> service.createNewFile());
        JMenuItem deleteItem = new JMenuItem("删除");
        deleteItem.addActionListener(e -> service.deleteProjectFile());
        treePopupMenu.add(createItem);
        treePopupMenu.add(deleteItem);
        fileTree.addMouseListener(service.fileTreeRightClick());
        //文件树构造完成后，直接放进滚动面板返回就行了
        return new JScrollPane(fileTree);
    }

    /**
     * 创建右侧编辑板块，用于对项目代码进行编辑操作
     * @return 编辑板块
     */
    private JScrollPane createRightPanel(){
        JTextArea editArea = new JTextArea();
        this.mapComponent("main.textarea.edit", editArea);
        //快速配置编辑文本域的各项功能
        this.service.setupEditArea();
        //编辑界面的字体采用FiraCode，好看不止一点半点
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File("files/FiraCode-Medium.ttf"));
            editArea.setFont(font.deriveFont(13.0F));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //默认情况下无法进行编辑，必须选中文件之后才可以
        editArea.setEditable(false);
        return new JScrollPane(editArea);
    }

    /**
     * 创建底部控制台板块，用于展示控制台输出信息
     * @return 底部板块
     */
    private JScrollPane createConsole(){
        JTextArea consoleArea = new JTextArea("控制台中尚未启动任何进程");
        this.mapComponent("main.textarea.console", consoleArea);
        consoleArea.setEditable(false);
        consoleArea.addKeyListener(service.inputRedirect());
        return new JScrollPane(consoleArea);
    }

    /**
     * 快速刷新文件树，构建JTree结点并重新绘制
     */
    public void refreshFileTree(){
        buildTreeNode(root);
        SwingUtilities.updateComponentTreeUI(this);
    }

    /**
     * 构建JTree结点，采用BFS算法完成
     */
    private void buildTreeNode(DefaultMutableTreeNode root){
        root.removeAllChildren();   //先清理掉
        //BFS算法列出所有结点并构建成树
        Queue<DefaultMutableTreeNode> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            DefaultMutableTreeNode node = queue.poll();
            NodeData data = (NodeData) node.getUserObject();
            for (File file : Objects.requireNonNull(data.getFile().listFiles())) {
                if (file.getName().charAt(0) == '.') continue;   //隐藏文件不需要显示出来
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(new NodeData(file.getAbsolutePath(), file.getName()));
                node.add(child);
                if(file.isDirectory()) queue.offer(child);
            }
        }
    }

    @Override
    protected boolean onClose() {
        //关闭之前如果还有运行的项目没有结束，一定要结束掉
        ProcessExecuteEngine.stopProcess();
        //然后回到初始界面
        WelcomeWindow window = new WelcomeWindow();
        window.openWindow();
        return true;
    }

    /**
     * NodeData是JTree的专用结点信息存储介质，包括文件相关信息。
     */
    private static class NodeData{
        private final String filepath;
        private final String nodeName;

        public NodeData(String filepath, String nodeName) {
            this.filepath = filepath;
            this.nodeName = nodeName;
        }

        public File getFile() {
            return new File(filepath);
        }

        @Override
        public String toString() {
            return nodeName;
        }
    }
}
