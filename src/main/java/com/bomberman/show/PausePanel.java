package com.bomberman.show;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 暂停面板：游戏暂停时显示，提供「继续游戏 / 重新开始 / 返回主菜单」三个选项。
 * 按 ESC 也可继续游戏（由 GameListener 处理）。
 */
public class PausePanel extends JPanel {
    private Runnable onResume;
    private Runnable onRestart;
    private Runnable onMenu;

    public PausePanel() {
        setBackground(new Color(26, 26, 46));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(80, 80, 80, 80));

        add(Box.createVerticalGlue());

        JLabel title = new JLabel("游戏暂停");
        title.setForeground(Color.decode("#ffd700"));
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 44));
        title.setAlignmentX(CENTER_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(40));

        add(button("继续游戏", new Color(76, 175, 80), e -> { if (onResume != null) onResume.run(); }));
        add(Box.createVerticalStrut(16));
        add(button("重新开始", new Color(255, 152, 0), e -> { if (onRestart != null) onRestart.run(); }));
        add(Box.createVerticalStrut(16));
        add(button("返回主菜单", new Color(233, 69, 96), e -> { if (onMenu != null) onMenu.run(); }));

        add(Box.createVerticalStrut(36));
        JLabel hint = new JLabel("按 ESC 继续游戏");
        hint.setForeground(Color.decode("#9a9ac0"));
        hint.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        hint.setAlignmentX(CENTER_ALIGNMENT);
        add(hint);

        add(Box.createVerticalGlue());
    }

    private JButton button(String text, Color bg, ActionListener listener) {
        JButton b = new JButton(text);
        b.setAlignmentX(CENTER_ALIGNMENT);
        b.setMaximumSize(new java.awt.Dimension(240, 44));
        b.setPreferredSize(new java.awt.Dimension(240, 44));
        b.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.addActionListener(listener);
        return b;
    }

    public void setOnResume(Runnable r) { this.onResume = r; }
    public void setOnRestart(Runnable r) { this.onRestart = r; }
    public void setOnMenu(Runnable r) { this.onMenu = r; }
}
