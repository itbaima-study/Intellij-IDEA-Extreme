package com.intellij.window;

import com.intellij.manage.ProjectManager;
import com.intellij.window.component.JLinePanel;
import com.intellij.window.component.JListItem;
import com.intellij.window.enums.CloseAction;
import com.intellij.window.layout.ListLayout;
import com.intellij.window.layout.MarginLayout;
import com.intellij.window.service.WelcomeService;

import javax.swing.*;
import java.awt.*;

/**
 * WelcomeWindow 是初始欢迎窗口，也是程序启动之后的窗口。
 * 本窗口包含的功能有：
 * 1. 创建新的项目（包括文件夹创建、项目初始文件创建）
 * 2. 打开系统已有项目。
 * 3. 从版本控制系统获取项目。
 * 4. 管理项目列表（展示列表、项目右键可删除）
 * 本窗口的特性：
 * 1. 关闭窗口等于直接结束程序。
 * 2. 在关闭窗口时需要保存当前运行时的配置。
 * 3. 关闭项目后会回到此窗口。
 * 整个窗口布局为：
 * - 最顶层为三个按钮（新建项目、打开项目、从VCS获取）
 * - 按钮下方有一个搜索框，可以快速搜索项目。
 * - 最下方的区域全部作为项目列表，展示所有项目。
 */
public class WelcomeWindow extends AbstractWindow <WelcomeService>{
    public WelcomeWindow() {
        super("欢迎访问 Intellij IDEA Extreme", new Dimension(320, 500), false, WelcomeService.class);
        //设定窗口关闭行为为直接退出程序
        this.setDefaultCloseAction(CloseAction.EXIT);
        //初始化窗口组件
        this.initWindowContent();
    }

    @Override
    protected boolean onClose() {
        //退出程序时，配置文件记得保存一下
        ProjectManager.saveConfigure();
        return true;  //欢迎窗口点击直接关闭就可以了
    }

    @Override
    protected void initWindowContent() {
        //上半部分就是工具栏，包括创建项目、打开项目之类的按钮，按钮下方就是一个搜索框，可以搜索当前的项目列表中的项目
        //下半部分就是列表，所以说这里我们直接分为上下两个部分来写
        //要写这样的界面的话，布局采用用BorderLayout就很合适，默认就是，所以说无需修改布局

        //首先是上半部分，也就是按钮和搜索框的区域：
        this.addComponent("welcome.panel.top", new JPanel(), BorderLayout.NORTH, panel -> {
            //顶部区域需要一个最外层的面板来装载，依然采用边界布局，因为还得继续分两半
            panel.setPreferredSize(new Dimension(0, 65));
            panel.setLayout(new BorderLayout());
            //上半部分是按钮区域，这里使用带分割线的面板组件
            this.addComponent(panel, "welcome.panel.top.top",
                    new JLinePanel(true), BorderLayout.NORTH, this::initContentTop);
            //下半部分是搜索框，这个就简单，价格搜索框就完事了
            this.addComponent(panel, "welcome.panel.top.bottom",
                    new JPanel(), BorderLayout.CENTER, this::initContentBottom);
        });

        //接着是下半部分，直接一个方法就封装好了，因为可能需要反复使用
        this.initProjectList();
    }

    /**
     * 对上半部分面板的上半部分进行配置
     * @param top 下半部分面板
     */
    private void initContentTop(JLinePanel top){
        top.setPreferredSize(new Dimension(100, 35));
        FlowLayout flowLayout = new FlowLayout();
        top.setLayout(flowLayout);
        //创建项目按钮
        this.addComponent(top, "welcome.button.create", new JButton("创建项目"), button -> {
            button.setPreferredSize(new Dimension(90, 25));
            button.addActionListener(e -> service.createNewProject());
        });
        //打开项目按钮
        this.addComponent(top, "welcome.button.open", new JButton("打开项目"), button -> {
            button.setPreferredSize(new Dimension(90, 25));
            button.addActionListener(e -> service.openProject());
        });
        //从VCS获取项目
        this.addComponent(top, "welcome.button.vcs", new JButton("从VCS获取"), button -> {
            button.setPreferredSize(new Dimension(100, 25));
            button.addActionListener(e -> service.openVcsProject());
        });
    }

    /**
     * 对上半部分面板的下半部分进行配置
     * @param bottom 下半部分面板
     */
    private void initContentBottom(JPanel bottom){
        bottom.setLayout(new MarginLayout(5));
        this.addComponent(bottom, "welcome.field.search", new JTextField(),
                field -> field.addKeyListener(service.refreshListAdapter()));
    }

    /**
     * 对下半部分的项目列表进行配置，这里采用的机制是将整个下半部分的组件全部替换成一个全新的
     * 滚动列表，这样可以避免很多奇奇怪怪的问题，虽然有点浪费性能
     */
    public void initProjectList(){
        JTextField searchField = this.getComponent("welcome.field.search");
        String search = searchField.getText();
        //装载所有项目Item的面板，这里使用ListLayout来展示为列表形式
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new ListLayout());
        //提供项目管理器获取所有已经读取的项目列表，开始装载
        ProjectManager.getManager().getProjectList()
                .filter(proj -> proj.getName().contains(search))
                .forEach(proj -> this.addComponent(listPanel,   //JListItem使我们自己定义列表Item组件，很好看的
                        "welcome.item."+proj.getName(), new JListItem(proj.getName(), proj.getFilePath()), item -> {
                    item.setClickAction(() -> service.enterProject(proj));
                    item.configurePopupMenu(menu -> {
                        JMenuItem deleteProject = new JMenuItem("删除当前项目");
                        deleteProject.addActionListener(e -> service.deleteProject(proj));
                        menu.add(deleteProject);
                    });
                }));
        //如果原本就有此组件，那么就移除
        JScrollPane projectList = this.getComponent("welcome.scroll.project");
        if(projectList != null) this.remove(projectList);
        //替换成一个全新的项目列表组件
        projectList = new JScrollPane(listPanel);
        this.addComponent("welcome.scroll.project", projectList, BorderLayout.CENTER, null);
        //动态更新组件之后，一定要调用updateComponentTreeUI方法重新绘制整个界面
        SwingUtilities.updateComponentTreeUI(this);
    }
}
