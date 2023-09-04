package com.intellij.window.service;

import com.intellij.window.AbstractWindow;

import java.awt.*;
import java.util.function.Function;

/**
 * AbstractService是所有窗口业务层实现的顶层抽象。
 * 这个类只进行各项实际业务处理，不负责UI相关内容，特别要求：
 * - 不能编写任何构造方法
 * 开始享用吧！
 */
public abstract class AbstractService {

    private AbstractWindow<? extends AbstractService> window;
    private Function<String, Component> componentGetter;

    /**
     * 方便AbstractWindows进行服务配置，快速指定当前Service所属的窗口。
     * @param window 当前业务层所属窗口
     */
    public final void setWindow(AbstractWindow<? extends AbstractService> window, Function<String, Component> componentGetter) {
        this.window = window;
        this.componentGetter = componentGetter;
    }

    /**
     * 通过组件名称，快速得到对应组件对象
     * @param componentName 组件名称
     * @return 组件对象
     */
    @SuppressWarnings("unchecked")
    protected final <T extends Component> T getComponent(String componentName){
        return (T) this.componentGetter.apply(componentName);
    }

    /**
     * 获取当前业务实现的所属窗口
     * @return 窗口
     */
    protected final AbstractWindow<? extends AbstractService> getWindow(){
        return this.window;
    }
}
