package com.bomberman.show;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 开始菜单面板（Swing）。选地图、选模式、开始游戏。取代原 JavaFX 版 MenuView。
 */
public class MenuPanel extends JPanel {
    private final JComboBox<String> mapSelector = new JComboBox<>();
    private final JComboBox<String> modeSelector = new JComboBox<>();
    private Runnable onStart;

    public MenuPanel() {
        setBackground(Color.decode("#16213e"));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        JLabel title = new JLabel("Q版炸弹堂");
        title.setAlignmentX(CENTER_ALIGNMENT);
        title.setForeground(Color.decode("#ffd700"));
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 48));
        add(title);
        add(Box.createVerticalStrut(8));

        JLabel bomb = new JLabel("BOMB MAN");
        bomb.setAlignmentX(CENTER_ALIGNMENT);
        bomb.setFont(new Font("Microsoft YaHei", Font.PLAIN, 32));
        add(bomb);
        add(Box.createVerticalStrut(24));

        JLabel mapLabel = sectionLabel("选择地图");
        add(mapLabel);
        mapSelector.addItem("经典草原");
        mapSelector.addItem("石砖地牢");
        mapSelector.addItem("沙漠遗迹");
        styleSelector(mapSelector);
        add(mapSelector);
        add(Box.createVerticalStrut(16));

        JLabel modeLabel = sectionLabel("游戏模式");
        add(modeLabel);
        modeSelector.addItem("单人模式");
        modeSelector.addItem("双人模式");
        styleSelector(modeSelector);
        add(modeSelector);
        add(Box.createVerticalStrut(24));

        JButton startButton = new JButton("开始游戏");
        startButton.setAlignmentX(CENTER_ALIGNMENT);
        startButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        startButton.setBackground(Color.decode("#ffd700"));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onStart != null) onStart.run();
            }
        });
        add(startButton);
        add(Box.createVerticalStrut(24));

        JLabel instr = new JLabel("<html><div style='text-align:center;'>"
                + "玩家1: WASD + 空格　|　玩家2: 方向键 + 数字0<br>"
                + "空格 — 放置炸弹　|　P — 暂停　|　ESC — 暂停/退出"
                + "</div></html>");
        instr.setAlignmentX(CENTER_ALIGNMENT);
        instr.setForeground(Color.decode("#ffd700"));
        instr.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        add(instr);
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setAlignmentX(CENTER_ALIGNMENT);
        l.setForeground(Color.decode("#ffd700"));
        l.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        return l;
    }

    private void styleSelector(JComboBox<String> cb) {
        cb.setAlignmentX(CENTER_ALIGNMENT);
        cb.setMaximumSize(new java.awt.Dimension(260, 30));
        cb.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
    }

    public void setOnStart(Runnable r) { this.onStart = r; }

    public int getSelectedMapIndex() {
        int idx = mapSelector.getSelectedIndex();
        return idx >= 0 ? idx : 0;
    }

    public boolean isTwoPlayerMode() {
        return modeSelector.getSelectedIndex() == 1;
    }
}
