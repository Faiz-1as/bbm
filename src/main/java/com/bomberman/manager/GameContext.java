package com.bomberman.manager;

import com.bomberman.element.PlayerObj;
import com.bomberman.enums.GameState;

/**
 * 游戏标量状态管理器：单例。存放不属于某个元素的全局状态。
 * 与 ElementManager 同为「管理器」风格，供 GameThread / HUD / 结束面板共享。
 *
 * 对标参考框架中由 GameThread 持有的零散状态，这里集中收口。
 */
public class GameContext {
    private GameState gameState;
    private boolean twoPlayerMode;
    private int score;
    private int timeLeft;
    private String winner;      // 双人对战胜者
    private boolean quitToMenu; // ESC 退出标记
    private int mapIndex;
    private String mapName;

    /** 玩家引用（HUD/结束判定/AI 需要直接读取玩家状态） */
    private PlayerObj player1;
    private PlayerObj player2;

    private static GameContext instance;

    public static synchronized GameContext getInstance() {
        if (instance == null) {
            instance = new GameContext();
        }
        return instance;
    }

    private GameContext() {
        reset();
    }

    public void reset() {
        gameState = GameState.MENU;
        twoPlayerMode = false;
        score = 0;
        timeLeft = 0;
        winner = null;
        quitToMenu = false;
        mapIndex = 0;
        mapName = "";
        player1 = null;
        player2 = null;
    }

    public GameState getGameState() { return gameState; }
    public void setGameState(GameState gameState) { this.gameState = gameState; }

    public boolean isTwoPlayerMode() { return twoPlayerMode; }
    public void setTwoPlayerMode(boolean twoPlayerMode) { this.twoPlayerMode = twoPlayerMode; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public void addScore(int points) { this.score += points; }

    public int getTimeLeft() { return timeLeft; }
    public void setTimeLeft(int timeLeft) { this.timeLeft = timeLeft; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }

    public boolean isQuitToMenu() { return quitToMenu; }
    public void setQuitToMenu(boolean quitToMenu) { this.quitToMenu = quitToMenu; }

    public int getMapIndex() { return mapIndex; }
    public void setMapIndex(int mapIndex) { this.mapIndex = mapIndex; }

    public String getMapName() { return mapName; }
    public void setMapName(String mapName) { this.mapName = mapName; }

    public PlayerObj getPlayer1() { return player1; }
    public void setPlayer1(PlayerObj player1) { this.player1 = player1; }
    public PlayerObj getPlayer2() { return player2; }
    public void setPlayer2(PlayerObj player2) { this.player2 = player2; }
}
