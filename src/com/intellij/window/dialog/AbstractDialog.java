package com.intellij.window.dialog;

import com.intellij.window.AbstractWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

/**
 * 当前项目中所有对话框的顶层抽象类
 */
public abstract class AbstractDialog extends JDialog {

    public AbstractDialog(AbstractWindow parent, String title, Dimension size){
        super(parent, title, true);   //对话框默认情况下都采用这种模式
        this.setSize(size);                 //对话框的大小默认情况下无法进行修改
        this.setResizable(false);
        this.setLocation(this.calculateCenter());  //对话框也要相对于当前窗口进行居中
        this.setLayout(null);               //因为对话框默认大小不可变，所以对话框默认布局为空，方便布置组件
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                AbstractDialog.this.closeDialog();
            }
        });
        initDialogContent();   //初始化对话框组件
    }

    public AbstractDialog(AbstractDialog parent, String title, Dimension size){
        super(parent, title, true);   //对话框默认情况下都采用这种模式
        this.setSize(size);                 //对话框的大小默认情况下无法进行修改
        this.setResizable(false);
        this.setLocation(this.calculateCenter());  //对话框也要相对于当前窗口进行居中
        this.setLayout(null);               //因为对话框默认大小不可变，所以对话框默认布局为空，方便布置组件
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                AbstractDialog.this.closeDialog();
            }
        });
        initDialogContent();   //初始化对话框组件
    }

    /**
     * 整个对话框的初始化方法，由子类实现，默认在当前类的构造方法中完成调用
     */
    protected abstract void initDialogContent();

    public void closeDialog() {
        this.dispose();
    }

    public void openDialog() {
        super.setVisible(true);
    }

    /**
     * 一律只能用我们自己的openDialog()方法展示对话框，原本的可见性设置直接封掉
     */
    @Override
    public void setVisible(boolean b) {
        throw new UnsupportedOperationException("请使用openDialog()方法展示对话框！");
    }

    /**
     * 计算当前对话框的居中位置，对话框的居中是相对于窗口的，所以说不能向像窗口那样计算
     * 我们要拿到窗口的位置，然后计算窗口的中心位置再减去对话框的尺寸，就是居中位置了
     * @return 对话框居中位置
     */
    private Point calculateCenter(){
        Point windowPoint = this.getParent().getLocation();
        Dimension windowSize = this.getParent().getSize();
        int x = (int) (windowPoint.getX() + windowSize.getWidth() / 2 - this.getWidth() / 2);
        int y = (int) (windowPoint.getY() + windowSize.getHeight() / 2 - this.getHeight() / 2);
        return new Point(x, y);
    }

    /**
     * 原版的组件添加方法太不方便了，要写很多行代码，这里调整一下
     * @param component 待添加的组件
     * @param consumer 组件配置在这里写
     */
    protected <T extends Container> void addComponent(T component, Consumer<T> consumer){
        if(consumer != null)
            consumer.accept(component);
        this.add(component);
    }
}
