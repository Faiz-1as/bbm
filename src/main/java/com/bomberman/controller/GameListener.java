package com.bomberman.controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bomberman.element.ElementObj;
import com.bomberman.enums.GameState;
import com.bomberman.manager.ElementManager;
import com.bomberman.manager.GameContext;
import com.bomberman.manager.GameElement;

/**
 * 键盘监听器。对标参考框架 com.tedu.controller.GameListener。
 *
 * 按键去重(Set)：按住不重复触发。
 *  - ESC：在 PLAYING/PAUSED 间切换暂停，并触发回调显示/隐藏暂停面板。
 *  - 其余键：转发给所有 PLAY 元素的 keyClick，由各玩家元素按自身角色(role)决定是否响应。
 */
public class GameListener implements KeyListener {
    private final ElementManager em = ElementManager.getManager();
    private final GameContext ctx = GameContext.getInstance();
    private final Set<Integer> set = new HashSet<>();

    /** 暂停状态切换回调（由 GameStart 注入，用于显示/隐藏暂停面板） */
    private Runnable onPauseToggle;

    public void setOnPauseToggle(Runnable r) { this.onPauseToggle = r; }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (set.contains(key)) return; // 去重
        set.add(key);

        if (key == KeyEvent.VK_ESCAPE) {
            if (ctx.getGameState() == GameState.PLAYING) {
                ctx.setGameState(GameState.PAUSED);
            } else if (ctx.getGameState() == GameState.PAUSED) {
                ctx.setGameState(GameState.PLAYING);
            } else {
                return; // 菜单/结束界面不响应 ESC
            }
            if (onPauseToggle != null) onPauseToggle.run();
            return;
        }

        List<ElementObj> play = em.getElementsByKey(GameElement.PLAY);
        for (ElementObj obj : play) obj.keyClick(true, key);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (!set.contains(key)) return;
        set.remove(key);
        List<ElementObj> play = em.getElementsByKey(GameElement.PLAY);
        for (ElementObj obj : play) obj.keyClick(false, key);
    }
}
