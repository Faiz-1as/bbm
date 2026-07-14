package com.bomberman.manager;

/**
 * 元素分类枚举，同时声明元素的更新与绘制顺序。
 * 顺序即绘制层级：先声明的先绘制（位于底层），后声明的覆盖在其上。
 *
 * 对标参考框架 com.tedu.manager.GameElement
 */
public enum GameElement {
    /** 地图：地板/墙壁/砖块（最底层） */
    MAPS,
    /** 道具 */
    POWERUP,
    /** 炸弹 */
    BOMB,
    /** 爆炸效果 */
    EXPLOSION,
    /** 玩家 */
    PLAY,
    /** AI 敌人（最顶层） */
    ENEMY
}
