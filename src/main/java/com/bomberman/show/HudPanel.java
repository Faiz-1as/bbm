package com.bomberman.show;

import com.bomberman.element.PlayerObj;
import com.bomberman.manager.GameContext;
import com.bomberman.util.Config;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * 游戏内 HUD 侧边栏（Swing）。右侧深色面板显示实时属性，Swing Timer 周期刷新。
 *
 * 布局采用 BoxLayout(Y_AXIS)，各分区(全局/玩家1/玩家2)按自然高度排列，
 * 避免 GridLayout 等高压缩导致双人模式时 P2 区被挤压遮挡。
 */
public class HudPanel extends JPanel {
    private static final Color BG = Color.decode("#14142a");
    private static final Color ROW_BG = Color.decode("#1e1e3c");
    private static final Color GOLD = Color.decode("#ffd700");
    private static final Color GRAY = Color.decode("#9a9ac0");

    private final JLabel scoreVal = new JLabel("--");
    private final JLabel timeVal = new JLabel("--");
    private final JLabel livesVal = new JLabel("--");
    private final JLabel bombsVal = new JLabel("--");
    private final JLabel powerVal = new JLabel("--");
    private final JLabel lives2Val = new JLabel("--");
    private final JLabel bombs2Val = new JLabel("--");
    private final JLabel power2Val = new JLabel("--");
    private final JLabel mapLabel = new JLabel(" ");

    private final JPanel p2Block = new JPanel();
    private final Timer timer;

    public HudPanel() {
        setPreferredSize(new Dimension(Config.HUD_WIDTH, Config.WINDOW_HEIGHT));
        setBackground(BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(14, 12, 14, 12));

        add(centeredLabel("游戏状态", GOLD, 15, true));
        add(Box.createVerticalStrut(10));

        // 全局：得分 / 时间
        add(sectionHeader("状态", GRAY));
        add(row("得分", scoreVal));
        add(row("时间", timeVal));
        add(Box.createVerticalStrut(10));

        // 玩家1
        add(sectionHeader("玩家 1", Color.decode("#4fc3f7")));
        add(row("血量", livesVal));
        add(row("炸弹", bombsVal));
        add(row("威力", powerVal));
        add(Box.createVerticalStrut(10));

        // 玩家2（仅双人模式显示）
        p2Block.setLayout(new BoxLayout(p2Block, BoxLayout.Y_AXIS));
        p2Block.setOpaque(false);
        p2Block.setAlignmentX(LEFT_ALIGNMENT);
        add(p2Block);
        add(Box.createVerticalGlue());

        add(mapLabel);
        mapLabel.setForeground(GOLD);
        mapLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        mapLabel.setAlignmentX(LEFT_ALIGNMENT);

        timer = new Timer(250, e -> refresh());
    }

    private JLabel centeredLabel(String text, Color c, int size, boolean bold) {
        JLabel l = new JLabel(text);
        l.setForeground(c);
        l.setFont(new Font("Microsoft YaHei", bold ? Font.BOLD : Font.PLAIN, size));
        l.setAlignmentX(CENTER_ALIGNMENT);
        return l;
    }

    private JLabel sectionHeader(String text, Color c) {
        JLabel l = new JLabel(text);
        l.setForeground(c);
        l.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(2, 4, 4, 4));
        return l;
    }

    private JPanel row(String name, JLabel val) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBackground(ROW_BG);
        p.setOpaque(true);
        p.setMaximumSize(new Dimension(Config.HUD_WIDTH - 24, 28));
        p.setPreferredSize(new Dimension(Config.HUD_WIDTH - 24, 28));
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JLabel n = new JLabel(name);
        n.setForeground(GRAY);
        n.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        val.setForeground(GOLD);
        val.setFont(new Font("Microsoft YaHei", Font.BOLD, 15));
        p.add(n);
        p.add(Box.createHorizontalGlue());
        p.add(val);
        return p;
    }

    public void start() { timer.start(); }
    public void stop() { timer.stop(); }

    private void refresh() {
        GameContext ctx = GameContext.getInstance();
        PlayerObj p1 = ctx.getPlayer1();
        if (p1 != null) {
            livesVal.setText(p1.getLives() + " / " + Config.INITIAL_LIVES);
            scoreVal.setText(String.valueOf(ctx.getScore()));
            int m = ctx.getTimeLeft() / 60, s = ctx.getTimeLeft() % 60;
            timeVal.setText(String.format("%d:%02d", m, s));
            timeVal.setForeground(ctx.getTimeLeft() <= 30 ? Color.decode("#ff4444") : GOLD);
            bombsVal.setText(p1.getMaxBombs() + "x");
            powerVal.setText(p1.getBombPower() + "x");
        }

        boolean twoP = ctx.isTwoPlayerMode();
        p2Block.setVisible(twoP);
        p2Block.removeAll();
        if (twoP) {
            p2Block.add(sectionHeader("玩家 2", Color.decode("#f06292")));
            PlayerObj p2 = ctx.getPlayer2();
            if (p2 != null) {
                lives2Val.setText(p2.getLives() + " / " + Config.INITIAL_LIVES);
                lives2Val.setForeground(p2.isAlive() ? GOLD : Color.decode("#ff4444"));
                bombs2Val.setText(p2.getMaxBombs() + "x");
                power2Val.setText(p2.getBombPower() + "x");
            }
            p2Block.add(row("血量", lives2Val));
            p2Block.add(row("炸弹", bombs2Val));
            p2Block.add(row("威力", power2Val));
        }

        mapLabel.setText("地图: " + (ctx.getMapName() == null ? "" : ctx.getMapName()));
        revalidate();
        repaint();
    }
}
