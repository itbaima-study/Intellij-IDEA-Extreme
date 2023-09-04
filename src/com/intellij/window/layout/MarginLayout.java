package com.intellij.window.layout;

import java.awt.*;

/**
 * 自定义的外边距布局
 * 唯一作用：为组件设定一个外边距然后再展示出来
 * 此布局要求内部有且只能有一个组件
 */
public class MarginLayout implements LayoutManager {
    //四个边距需要存储一下
    private final int left;
    private final int right;
    private final int top;
    private final int bottom;

    /**
     * 一次性设定四个边距，如果需要可以自行扩展
     * @param margin 边距
     */
    public MarginLayout(int margin){
        this.left = this.bottom = this.right = this.top = margin;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return parent.getSize();
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return parent.getMinimumSize();
    }

    @Override
    public void layoutContainer(Container parent) {
        if (parent.getComponents().length != 1)
            throw new IllegalStateException("此布局的容器中必须有且仅有一个组件！");
        Component component = parent.getComponent(0);
        //计算组件的大小
        component.setSize(this.calculateComponentSize(parent.getSize()));
        //计算组件的位置
        component.setLocation(this.calculateComponentLocation());
    }

    /**
     * 组件大小计算
     * @param parent 外部容器大小
     * @return 最终大小
     */
    private Dimension calculateComponentSize(Dimension parent){
        //宽度就是外部容器宽度 减去 左右两个边距
        int width = (int) (parent.getWidth() - this.left - this.right);
        //高度就是外部容器高度 减去 上下两个边距
        int height = (int) (parent.getHeight() - this.top - this.bottom);
        return new Dimension(width, height);
    }

    /**
     * 计算组件的最终位置
     * @return 最终位置
     */
    private Point calculateComponentLocation(){
        return new Point(left, top);    //这个就简单了，实际上就是 (左边距,上边距)
    }
}
