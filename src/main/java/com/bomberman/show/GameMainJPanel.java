package com.bomberman.show;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import com.bomberman.element.ElementObj;
import com.bomberman.manager.ElementManager;
import com.bomberman.manager.GameElement;
import com.bomberman.util.Config;

/**
 * 游戏主画面面板。对标参考框架 com.tedu.show.GameMainJPanel。
 *
 * 实现 Runnable：独立线程按固定间隔 repaint 刷新画面。
 * paintComponent 按枚举顺序遍历 ElementManager 中所有元素，调用各自的 showElement 绘制。
 */
public class GameMainJPanel extends JPanel implements Runnable {
    private final ElementManager em = ElementManager.getManager();

    public GameMainJPanel() {
        setPreferredSize(new Dimension(Config.CANVAS_WIDTH, Config.CANVAS_HEIGHT));
        setDoubleBuffered(true);
        setFocusable(false); // 不抢占焦点，按键由 GameJFrame 上的 GameListener 接收
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 背景
        g.setColor(Color.decode("#2d5a27"));
        g.fillRect(0, 0, Config.CANVAS_WIDTH, Config.CANVAS_HEIGHT);

        // 按枚举顺序绘制所有元素
        Map<GameElement, List<ElementObj>> all = em.getGameElements();
        for (GameElement ge : GameElement.values()) {
            for (ElementObj obj : all.get(ge)) {
                obj.showElement(g);
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
