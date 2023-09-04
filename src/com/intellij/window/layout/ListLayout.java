package com.intellij.window.layout;

import java.awt.*;

/**
 * 内部的组件会以列表的形式从上往下排列
 * 注意，不能配合滚动条使用，如果需要，必须禁止横向大小修改，否则会出BUG
 */
public class ListLayout implements LayoutManager {

    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return calculateSize(parent);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return calculateSize(parent);
    }

    @Override
    public void layoutContainer(Container parent) {
        int top = 0;
        for (Component component : parent.getComponents()) {
            int height = (int) component.getPreferredSize().getHeight();
            component.setSize(parent.getWidth(), height);
            component.setLocation(0, top);
            top += height;
        }
    }

    /**
     * 计算内容的大小
     * @param parent 容器
     * @return 大小
     */
    private Dimension calculateSize(Container parent){
        int height = 0;
        for (Component component : parent.getComponents())
            height += component.getPreferredSize().getHeight();
        return new Dimension(parent.getWidth(), height);
    }
}
