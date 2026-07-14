package com.bomberman.show;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.bomberman.util.Config;

/**
 * 游戏窗体。对标参考框架 com.tedu.show.GameJFrame。
 *
 * 采用 setter 注入(set注入)各面板与监听/线程；start() 组装界面、绑定监听、启动线程。
 * 用 CardLayout 在 菜单/游戏/结束 三个面板间切换。
 */
public class GameJFrame extends JFrame {
    public static int GameX = Config.WINDOW_WIDTH;
    public static int GameY = Config.WINDOW_HEIGHT;

    private JPanel root;
    private CardLayout cards;

    private MenuPanel menuPanel;
    private GameMainJPanel gameMainPanel;
    private HudPanel hudPanel;
    private EndPanel endPanel;
    private PausePanel pausePanel;
    private KeyListener keyListener;
    private Thread thread;

    public GameJFrame() {
        init();
    }

    public void init() {
        setSize(GameX, GameY);
        setTitle("Q版炸弹堂");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        root = new JPanel();
        cards = new CardLayout();
        root.setLayout(cards);
        setContentPane(root);
    }

    /** 启动：组装面板、绑定监听、启动线程、显示窗体 */
    public void start() {
        // 游戏面板 = 画布(中) + HUD(东)
        JPanel gameCard = new JPanel(new BorderLayout());
        if (gameMainPanel != null) gameCard.add(gameMainPanel, BorderLayout.CENTER);
        if (hudPanel != null) gameCard.add(hudPanel, BorderLayout.EAST);

        if (menuPanel != null) root.add(menuPanel, "menu");
        root.add(gameCard, "game");
        if (endPanel != null) root.add(endPanel, "end");
        if (pausePanel != null) root.add(pausePanel, "pause");

        if (keyListener != null) addKeyListener(keyListener);

        if (thread != null) thread.start();

        // 画面刷新线程（GameMainJPanel implements Runnable）
        if (gameMainPanel != null) new Thread(gameMainPanel).start();
        if (hudPanel != null) hudPanel.start();

        setVisible(true);
        requestFocus();
    }

    public void showMenu() {
        cards.show(root, "menu");
        requestFocus();
    }

    public void showGame() {
        cards.show(root, "game");
        requestFocus();
    }

    public void showEnd() {
        cards.show(root, "end");
        requestFocus();
    }

    public void showPause() {
        cards.show(root, "pause");
        requestFocus();
    }

    // ========== setter 注入 ==========
    public void setMenuPanel(MenuPanel menuPanel) { this.menuPanel = menuPanel; }
    public void setGameMainPanel(GameMainJPanel gameMainPanel) { this.gameMainPanel = gameMainPanel; }
    public void setHudPanel(HudPanel hudPanel) { this.hudPanel = hudPanel; }
    public void setEndPanel(EndPanel endPanel) { this.endPanel = endPanel; }
    public void setPausePanel(PausePanel pausePanel) { this.pausePanel = pausePanel; }
    public void setKeyListener(KeyListener keyListener) { this.keyListener = keyListener; }
    public void setThread(Thread thread) { this.thread = thread; }
}
