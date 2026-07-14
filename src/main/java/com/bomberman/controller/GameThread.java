package com.bomberman.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bomberman.element.AIPlayerObj;
import com.bomberman.element.BombObj;
import com.bomberman.element.ElementObj;
import com.bomberman.element.ExplosionObj;
import com.bomberman.element.PowerUpObj;
import com.bomberman.element.PlayerObj;
import com.bomberman.enums.GameState;
import com.bomberman.enums.PowerUpType;
import com.bomberman.enums.TileType;
import com.bomberman.manager.ElementManager;
import com.bomberman.manager.GameContext;
import com.bomberman.manager.GameElement;
import com.bomberman.manager.GameLoad;
import com.bomberman.util.AudioManager;
import com.bomberman.util.Config;

import javax.swing.SwingUtilities;

/**
 * 游戏主线程。对标参考框架 com.tedu.controller.GameThread。
 *
 * run() = gameLoad -> gameRun -> gameOver 三阶段循环。
 *  - gameLoad：清空元素、加载地图、生成玩家/AI、复位计分/计时
 *  - gameRun：每帧 moveAndUpdate(模板 model) + 炸弹/爆炸处理 + 碰撞 + 计时 + 胜负
 *  - gameOver：通知 UI 切到结束/菜单
 *
 * dt 为实时帧间隔（秒），供时基逻辑使用；gameTime 为帧计数，传入 model() 做动画。
 */
public class GameThread extends Thread {
    private final ElementManager em = ElementManager.getManager();
    private final GameContext ctx = GameContext.getInstance();

    /** 实时帧间隔（秒），供元素时基逻辑读取 */
    public static volatile float dt = 0f;
    /** 帧计数，传入 model(gameTime) 做动画 */
    public static long gameTime = 0L;

    private volatile boolean startRequested = false;
    /** 暂停时"重新开始"标记：为 true 时本次 gameRun 退出后不弹结束面板，直接开新局 */
    private volatile boolean restartRequested = false;
    private float timeAccumulator = 0f;

    /** 结束回调（由 GameStart 注入，在 EDT 切面板） */
    private Runnable onGameEnd;

    public void setOnGameEnd(Runnable r) { this.onGameEnd = r; }

    /** 菜单"开始/再来一局"触发：请求开始一局 */
    public void requestStart() { this.startRequested = true; }

    /** 暂停面板"重新开始"：退出当前 gameRun 并开新局（不弹结束面板） */
    public void requestRestart() {
        restartRequested = true;
        startRequested = true;
        ctx.setGameState(GameState.GAME_OVER); // 退出当前 gameRun
    }

    /** 暂停面板"返回主菜单"：退出当前 gameRun，由 gameOver 回调切到菜单 */
    public void requestQuitToMenu() {
        ctx.setQuitToMenu(true);
        ctx.setGameState(GameState.GAME_OVER); // 退出当前 gameRun
        AudioManager.getInstance().stopBGM();
    }

    @Override
    public void run() {
        while (true) {
            if (startRequested) {
                startRequested = false;
                gameLoad();
                ctx.setGameState(GameState.PLAYING);
                gameRun();
                if (restartRequested) {
                    restartRequested = false; // 重新开始：跳过结束面板
                } else {
                    gameOver();
                }
            }
            try {
                sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // ==================== 阶段1：加载 ====================
    private void gameLoad() {
        em.clearAll();
        GameLoad.MapLoad(ctx.getMapIndex());
        GameLoad.loadPlay(ctx.isTwoPlayerMode());
        ctx.setScore(0);
        ctx.setTimeLeft(Config.GAME_TIME);
        ctx.setWinner(null);
        ctx.setQuitToMenu(false);
        timeAccumulator = 0f;
        gameTime = 0L;
        AudioManager.getInstance().playBGM();
    }

    // ==================== 阶段2：运行 ====================
    private void gameRun() {
        long lastTime = System.nanoTime();
        while (ctx.getGameState() == GameState.PLAYING || ctx.getGameState() == GameState.PAUSED) {
            long now = System.nanoTime();
            float d = (now - lastTime) / 1_000_000_000.0f;
            lastTime = now;
            if (d > 0.05f) d = 0.05f; // 钳制大跳变
            dt = d;
            gameTime++;

            if (ctx.getGameState() == GameState.PLAYING) {
                moveAndUpdate(em.getGameElements(), gameTime);
                tickBombs(d);
                tickExplosions(d);
                checkCollisions();
                updateGameTimer(d);
                checkWinCondition();
            }
            try {
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /** 元素自动化：遍历枚举分类，死亡则 die+移除，否则调模板 model() */
    private void moveAndUpdate(Map<GameElement, List<ElementObj>> all, long gt) {
        for (GameElement ge : GameElement.values()) {
            List<ElementObj> list = all.get(ge);
            for (int i = list.size() - 1; i >= 0; i--) {
                ElementObj obj = list.get(i);
                if (!obj.isLive()) {
                    obj.die();
                    list.remove(i);
                    continue;
                }
                obj.model(gt);
            }
        }
    }

    private void tickBombs(float d) {
        List<BombObj> toExplode = new ArrayList<>();
        for (ElementObj each : em.getElementsByKey(GameElement.BOMB)) {
            BombObj bomb = (BombObj) each;
            if (bomb.tick(d)) toExplode.add(bomb);
        }
        for (BombObj b : toExplode) {
            em.getElementsByKey(GameElement.BOMB).remove(b);
            if (b.getOwner() != null) b.getOwner().onBombExploded();
        }
        for (BombObj b : toExplode) explodeBomb(b);
    }

    private void explodeBomb(BombObj bomb) {
        List<CollisionController.Point> range = CollisionController.getBombExplosionRange(bomb);
        AudioManager.getInstance().playSound("explode");

        boolean fromPlayer = bomb.getOwner() != null && bomb.getOwner().getRole() == 1;
        for (CollisionController.Point p : range) {
            ElementObj proto = GameLoad.getObj("explosion");
            if (proto instanceof ExplosionObj) {
                ExplosionObj exp = (ExplosionObj) proto.createElement(
                        p.x + "," + p.y + "," + p.direction.ordinal() + "," + (fromPlayer ? 1 : 0));
                em.addElement(exp, GameElement.EXPLOSION);
            }
            TileType t = com.bomberman.element.MapData.getCurrent().getTileType(p.x, p.y);
            if (t == TileType.BRICK) {
                boolean spawn = com.bomberman.element.MapData.getCurrent().destroyTile(p.x, p.y);
                ctx.addScore(Config.SCORE_BRICK);
                if (spawn) spawnPowerUp(p.x, p.y);
            }
        }
        triggerChainReactions(range);
    }

    private void triggerChainReactions(List<CollisionController.Point> range) {
        List<BombObj> toExplode = new ArrayList<>();
        for (ElementObj each : em.getElementsByKey(GameElement.BOMB)) {
            BombObj bomb = (BombObj) each;
            for (CollisionController.Point p : range) {
                if (p.x == bomb.getTileX() && p.y == bomb.getTileY()) {
                    toExplode.add(bomb);
                    break;
                }
            }
        }
        for (BombObj b : toExplode) {
            em.getElementsByKey(GameElement.BOMB).remove(b);
            if (b.getOwner() != null) b.getOwner().onBombExploded();
        }
        for (BombObj b : toExplode) explodeBomb(b);
    }

    private void tickExplosions(float d) {
        List<ExplosionObj> done = new ArrayList<>();
        for (ElementObj each : em.getElementsByKey(GameElement.EXPLOSION)) {
            ExplosionObj exp = (ExplosionObj) each;
            if (exp.tick(d)) done.add(exp);
        }
        for (ExplosionObj e : done) em.getElementsByKey(GameElement.EXPLOSION).remove(e);
    }

    private void spawnPowerUp(int x, int y) {
        PowerUpType[] types = PowerUpType.values();
        PowerUpType type = types[(int) (Math.random() * types.length)];
        ElementObj proto = GameLoad.getObj("powerup");
        if (proto instanceof PowerUpObj) {
            PowerUpObj pu = (PowerUpObj) proto.createElement(x + "," + y + "," + type.ordinal());
            em.addElement(pu, GameElement.POWERUP);
        }
    }

    private void checkCollisions() {
        // 玩家
        for (ElementObj each : em.getElementsByKey(GameElement.PLAY)) {
            PlayerObj p = (PlayerObj) each;
            if (!p.isAlive()) continue;
            if (CollisionController.checkPlayerExplosionCollision(p)) p.takeDamage();
            PowerUpObj pu = CollisionController.checkPlayerPowerUpCollision(p);
            if (pu != null) {
                p.collectPowerUp(pu);
                em.getElementsByKey(GameElement.POWERUP).remove(pu);
                ctx.addScore(Config.SCORE_POWERUP);
                AudioManager.getInstance().playSound("prop");
            }
        }
        // AI
        for (ElementObj each : em.getElementsByKey(GameElement.ENEMY)) {
            AIPlayerObj ai = (AIPlayerObj) each;
            if (!ai.isAlive()) continue;
            if (CollisionController.checkPlayerExplosionCollision(ai)) {
                ai.takeDamage();
                if (!ai.isAlive()) ctx.addScore(Config.SCORE_AI);
            }
            PowerUpObj pu = CollisionController.checkPlayerPowerUpCollision(ai);
            if (pu != null) {
                ai.collectPowerUp(pu);
                em.getElementsByKey(GameElement.POWERUP).remove(pu);
            }
        }
    }

    private void updateGameTimer(float d) {
        timeAccumulator += d;
        if (timeAccumulator >= 1.0f) {
            timeAccumulator -= 1.0f;
            ctx.setTimeLeft(ctx.getTimeLeft() - 1);
            if (ctx.getTimeLeft() <= 0) {
                if (ctx.isTwoPlayerMode() && ctx.getWinner() == null) ctx.setWinner("平局！");
                endGame(GameState.GAME_OVER);
            }
        }
    }

    private void checkWinCondition() {
        PlayerObj p1 = ctx.getPlayer1();
        PlayerObj p2 = ctx.getPlayer2();
        boolean p1Alive = p1 != null && p1.isAlive();
        boolean p2Alive = !ctx.isTwoPlayerMode() || (p2 != null && p2.isAlive());
        boolean allAIDead = em.getElementsByKey(GameElement.ENEMY).stream()
                .noneMatch(e -> ((AIPlayerObj) e).isAlive());

        if (ctx.isTwoPlayerMode()) {
            if (!p1Alive && !p2Alive) { ctx.setWinner("平局！"); endGame(GameState.GAME_OVER); }
            else if (!p1Alive) { ctx.setWinner("玩家2 胜利！"); endGame(GameState.GAME_OVER); }
            else if (!p2Alive) { ctx.setWinner("玩家1 胜利！"); endGame(GameState.GAME_OVER); }
            return;
        }
        if (allAIDead && p1Alive) { ctx.addScore(Config.SCORE_VICTORY); endGame(GameState.VICTORY); }
        if (!p1Alive) { endGame(GameState.GAME_OVER); }
    }

    private void endGame(GameState state) {
        ctx.setGameState(state);
        AudioManager.getInstance().stopBGM();
        AudioManager.getInstance().playSound(state == GameState.VICTORY ? "level_complete" : "mission_failed");
    }

    // ==================== 阶段3：结束 ====================
    private void gameOver() {
        if (onGameEnd != null) {
            SwingUtilities.invokeLater(onGameEnd);
        }
    }
}
