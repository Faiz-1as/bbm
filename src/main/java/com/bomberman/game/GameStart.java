package com.bomberman.game;

import com.bomberman.controller.GameListener;
import com.bomberman.controller.GameThread;
import com.bomberman.enums.GameState;
import com.bomberman.manager.GameContext;
import com.bomberman.manager.GameLoad;
import com.bomberman.show.EndPanel;
import com.bomberman.show.GameJFrame;
import com.bomberman.show.GameMainJPanel;
import com.bomberman.show.HudPanel;
import com.bomberman.show.MenuPanel;
import com.bomberman.show.PausePanel;
import com.bomberman.util.ResourceManager;

/**
 * 程序唯一入口。对标参考框架 com.tedu.game.GameStart。
 *
 * 实例化各面板/监听/线程 -> 通过 setter 注入到 GameJFrame -> 装配回调 -> start()。
 */
public class GameStart {

    public static void main(String[] args) {
        // 预加载资源
        ResourceManager.getInstance(); // 触发图片分组加载
        GameLoad.loadImg();            // 命名图片注册表
        GameLoad.loadObj();            // 元素类反射注册表

        GameJFrame gj = new GameJFrame();
        MenuPanel menu = new MenuPanel();
        GameMainJPanel game = new GameMainJPanel();
        HudPanel hud = new HudPanel();
        EndPanel end = new EndPanel();
        PausePanel pause = new PausePanel();
        GameListener listener = new GameListener();
        GameThread th = new GameThread();

        // setter 注入
        gj.setMenuPanel(menu);
        gj.setGameMainPanel(game);
        gj.setHudPanel(hud);
        gj.setEndPanel(end);
        gj.setPausePanel(pause);
        gj.setKeyListener(listener);
        gj.setThread(th);

        GameContext ctx = GameContext.getInstance();

        // 菜单"开始"：设置地图/模式，请求开始一局
        menu.setOnStart(() -> {
            ctx.setMapIndex(menu.getSelectedMapIndex());
            ctx.setTwoPlayerMode(menu.isTwoPlayerMode());
            th.requestStart();
            gj.showGame();
        });

        // 结束"再来一局"：沿用上次设置重开
        end.setOnRestart(() -> {
            th.requestStart();
            gj.showGame();
        });

        // 结束"返回主菜单"
        end.setOnMenu(() -> gj.showMenu());

        // ESC 暂停切换：根据新状态显示/隐藏暂停面板
        listener.setOnPauseToggle(() -> {
            if (ctx.getGameState() == GameState.PAUSED) gj.showPause();
            else if (ctx.getGameState() == GameState.PLAYING) gj.showGame();
        });

        // 暂停面板：继续游戏
        pause.setOnResume(() -> {
            ctx.setGameState(GameState.PLAYING);
            gj.showGame();
        });
        // 暂停面板：重新开始
        pause.setOnRestart(() -> {
            gj.showGame();
            th.requestRestart();
        });
        // 暂停面板：返回主菜单
        pause.setOnMenu(() -> th.requestQuitToMenu());

        // 一局结束回调（GameThread 在 EDT 触发）
        th.setOnGameEnd(() -> {
            if (ctx.isQuitToMenu()) {
                gj.showMenu();
            } else {
                end.showResult(ctx);
                gj.showEnd();
            }
        });

        gj.start();
    }
}
