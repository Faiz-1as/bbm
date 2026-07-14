package com.bomberman.util;

/**
 * 游戏配置常量
 */
public class Config {
    public static final int TILE_SIZE = 40;
    public static final int MAP_WIDTH = 15;
    public static final int MAP_HEIGHT = 13;

    public static final float PLAYER_SPEED = 120.0f;
    public static final float BOMB_TIMER = 3.0f;
    public static final float EXPLOSION_DURATION = 0.5f;
    public static final float INVINCIBLE_TIME = 1.0f;

    public static final int GAME_TIME = 180;

    public static final int INITIAL_LIVES = 3;
    public static final int INITIAL_BOMBS = 1;
    public static final int INITIAL_POWER = 2;

    public static final int SCORE_BRICK = 10;
    public static final int SCORE_AI = 100;
    public static final int SCORE_POWERUP = 20;
    public static final int SCORE_VICTORY = 500;

    public static final int CANVAS_WIDTH = MAP_WIDTH * TILE_SIZE;
    public static final int CANVAS_HEIGHT = MAP_HEIGHT * TILE_SIZE;

    /** 画布 + 右侧 HUD 宽度 */
    public static final int HUD_WIDTH = 200;
    public static final int WINDOW_WIDTH = CANVAS_WIDTH + HUD_WIDTH;
    public static final int WINDOW_HEIGHT = CANVAS_HEIGHT;

    public static final String[] MAP_NAMES = {"经典草原", "石砖地牢", "沙漠遗迹"};
}
