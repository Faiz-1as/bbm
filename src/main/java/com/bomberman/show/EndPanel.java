package com.bomberman.show;

import com.bomberman.manager.GameContext;

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
 * 结束/胜利面板（Swing）。取代原 JavaFX 版 EndView。
 */
public class EndPanel extends JPanel {
    private final JLabel resultLabel = new JLabel(" ", JLabel.CENTER);
    private final JLabel scoreLabel = new JLabel(" ", JLabel.CENTER);
    private Runnable onRestart;
    private Runnable onMenu;

    public EndPanel() {
        setBackground(Color.decode("#2a2a4a"));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalGlue());

        resultLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 44));
        resultLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(resultLabel);
        add(Box.createVerticalStrut(16));

        scoreLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 24));
        scoreLabel.setForeground(Color.decode("#ffd700"));
        scoreLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(scoreLabel);
        add(Box.createVerticalStrut(30));

        JButton restart = new JButton("再来一局");
        restart.setAlignmentX(CENTER_ALIGNMENT);
        restart.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        restart.setBackground(Color.decode("#e94560"));
        restart.setForeground(Color.WHITE);
        restart.setFocusPainted(false);
        restart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onRestart != null) onRestart.run();
            }
        });
        add(restart);
        add(Box.createVerticalStrut(12));

        JButton menu = new JButton("返回主菜单");
        menu.setAlignmentX(CENTER_ALIGNMENT);
        menu.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        menu.setBackground(Color.decode("#533483"));
        menu.setForeground(Color.WHITE);
        menu.setFocusPainted(false);
        menu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onMenu != null) onMenu.run();
            }
        });
        add(menu);
        add(Box.createVerticalGlue());
    }

    /** 依据 GameContext 当前结果填充内容 */
    public void showResult(GameContext ctx) {
        if (ctx.isQuitToMenu()) {
            return; // 退回菜单，不显示结束面板
        }
        if (ctx.getGameState() != null && ctx.getGameState().name().equals("VICTORY")) {
            resultLabel.setText("胜利！");
            resultLabel.setForeground(Color.decode("#4caf50"));
            scoreLabel.setText("得分：" + ctx.getScore());
        } else {
            if (ctx.isTwoPlayerMode() && ctx.getWinner() != null) {
                resultLabel.setText(ctx.getWinner());
                resultLabel.setForeground(Color.decode("#4caf50"));
                scoreLabel.setText(" ");
            } else {
                resultLabel.setText("游戏结束");
                resultLabel.setForeground(Color.decode("#f44336"));
                scoreLabel.setText("得分：" + ctx.getScore());
            }
        }
    }

    public void setOnRestart(Runnable r) { this.onRestart = r; }
    public void setOnMenu(Runnable r) { this.onMenu = r; }
}
