package com.intellij.window.component;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class JListItem extends JComponent {
    private final String name;
    private final String filepath;
    private boolean mouseOver = false;
    private Runnable clickAction = () -> {};

    private final JPopupMenu popupMenu = new JPopupMenu();

    public JListItem(String name, String filepath) {
        this.name = name;
        this.filepath = filepath;
        this.setUI(new JListItemUI());
        this.setPreferredSize(new Dimension(0, 50));
        this.add(popupMenu);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseOver = true;
                JListItem.this.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseOver = false;
                JListItem.this.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1)
                    clickAction.run();
                else if (e.getButton() == MouseEvent.BUTTON3)
                    popupMenu.show(JListItem.this, e.getX(), e.getY());
            }
        });
    }

    public void setClickAction(Runnable clickAction){
        this.clickAction = clickAction;
    }

    public void configurePopupMenu(Consumer<JPopupMenu> consumer){
        consumer.accept(this.popupMenu);
    }

    private class JListItemUI extends ComponentUI {
        @Override
        public void paint(Graphics g, JComponent c) {
            if(mouseOver) {
                g.setColor(new Color(255, 255, 255, 128));
                g.fillRoundRect(5, 5, c.getWidth() - 10, c.getHeight() - 5, 10, 10);
            }
            g.setColor(hashColor(name.hashCode()));
            g.fillRoundRect(10, 10, 35, 35, 10, 10);
            g.setColor(Color.WHITE);
            g.drawString(filepath, 50, 42);
            Font font = g.getFont();
            g.setFont(new Font(font.getName(), Font.PLAIN, 15));
            g.drawString(name, 50, 22);
            g.setColor(Color.WHITE);
            g.setFont(new Font(font.getName(), Font.PLAIN, 20));
            g.drawString(name.substring(0, 1), 20, 35);
        }
    }

    /**
     * 根据哈希值随机选择一个颜色
     * @return 颜色
     */
    private Color hashColor(int hashCode){
        Color[] colors = {Color.PINK, Color.ORANGE, new Color(0, 201, 87), new Color(160, 102, 211),
                        new Color(227, 207, 87), new Color(221, 160, 221), new Color(51, 161, 210),
                        new Color(46, 139, 87), new Color(252, 230, 201), new Color(128, 138, 135)};
        int index = Math.abs(hashCode) % colors.length;
        return colors[index];
    }
}
