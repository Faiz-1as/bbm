package com.bomberman.controller;

import java.util.ArrayList;
import java.util.List;

import com.bomberman.element.BombObj;
import com.bomberman.element.ElementObj;
import com.bomberman.element.ExplosionObj;
import com.bomberman.element.MapData;
import com.bomberman.element.PowerUpObj;
import com.bomberman.element.PlayerObj;
import com.bomberman.enums.TileType;
import com.bomberman.manager.ElementManager;
import com.bomberman.manager.GameElement;
import com.bomberman.util.Config;

/**
 * 碰撞检测控制器。适配新的 ElementObj / ElementManager 结构。
 * 取代原 JavaFX 版 CollisionController，逻辑等价。
 */
public class CollisionController {

    /** 玩家试图位移 (dx,dy) 是否会撞到墙/砖 */
    public static boolean checkPlayerMapCollision(PlayerObj p, float dx, float dy) {
        float newX = p.getFx() + dx;
        float newY = p.getFy() + dy;
        float left = newX + 5;
        float right = newX + Config.TILE_SIZE - 5;
        float top = newY + 5;
        float bottom = newY + Config.TILE_SIZE - 5;

        int tileLeft = (int) (left / Config.TILE_SIZE);
        int tileRight = (int) (right / Config.TILE_SIZE);
        int tileTop = (int) (top / Config.TILE_SIZE);
        int tileBottom = (int) (bottom / Config.TILE_SIZE);

        for (int ty = tileTop; ty <= tileBottom; ty++) {
            for (int tx = tileLeft; tx <= tileRight; tx++) {
                TileType t = MapData.getCurrent().getTileType(tx, ty);
                if (t == null) continue;
                if (t == TileType.WALL || t == TileType.BRICK) return true;
            }
        }
        return false;
    }

    /** 玩家试图位移是否撞到炸弹（自己放的炸弹可穿过） */
    public static boolean checkPlayerBombCollision(PlayerObj p, float dx, float dy) {
        float newX = p.getFx() + dx;
        float newY = p.getFy() + dy;
        float left = newX + 5;
        float right = newX + Config.TILE_SIZE - 5;
        float top = newY + 5;
        float bottom = newY + Config.TILE_SIZE - 5;

        int tileLeft = (int) (left / Config.TILE_SIZE);
        int tileRight = (int) (right / Config.TILE_SIZE);
        int tileTop = (int) (top / Config.TILE_SIZE);
        int tileBottom = (int) (bottom / Config.TILE_SIZE);

        for (ElementObj each : ElementManager.getManager().getElementsByKey(GameElement.BOMB)) {
            BombObj bomb = (BombObj) each;
            if (bomb.getOwner() == p) continue;
            int bx = bomb.getTileX();
            int by = bomb.getTileY();
            if (tileLeft <= bx && bx <= tileRight && tileTop <= by && by <= tileBottom) {
                return true;
            }
        }
        return false;
    }

    /** 玩家是否处于爆炸范围内 */
    public static boolean checkPlayerExplosionCollision(PlayerObj p) {
        if (p.isInvincible()) return false;
        int ptx = Math.round(p.getFx() / Config.TILE_SIZE);
        int pty = Math.round(p.getFy() / Config.TILE_SIZE);
        for (ElementObj each : ElementManager.getManager().getElementsByKey(GameElement.EXPLOSION)) {
            ExplosionObj exp = (ExplosionObj) each;
            if (exp.getTileX() == ptx && exp.getTileY() == pty) return true;
        }
        return false;
    }

    /** 玩家所在格是否有道具，有则返回该道具 */
    public static PowerUpObj checkPlayerPowerUpCollision(PlayerObj p) {
        int ptx = Math.round(p.getFx() / Config.TILE_SIZE);
        int pty = Math.round(p.getFy() / Config.TILE_SIZE);
        for (ElementObj each : ElementManager.getManager().getElementsByKey(GameElement.POWERUP)) {
            PowerUpObj pu = (PowerUpObj) each;
            if (pu.getTileX() == ptx && pu.getTileY() == pty) return pu;
        }
        return null;
    }

    /** 计算炸弹的爆炸覆盖格（含中心格） */
    public static List<Point> getBombExplosionRange(BombObj bomb) {
        List<Point> range = new ArrayList<>();
        int bx = bomb.getTileX();
        int by = bomb.getTileY();
        int power = bomb.getPower();
        range.add(new Point(bx, by, ExplosionObj.Direction.CENTER));

        int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        ExplosionObj.Direction[] expDirs = {
                ExplosionObj.Direction.UP,
                ExplosionObj.Direction.DOWN,
                ExplosionObj.Direction.LEFT,
                ExplosionObj.Direction.RIGHT
        };
        for (int i = 0; i < 4; i++) {
            int dx = dirs[i][0];
            int dy = dirs[i][1];
            ExplosionObj.Direction dir = expDirs[i];
            for (int d = 1; d <= power; d++) {
                int tx = bx + dx * d;
                int ty = by + dy * d;
                TileType t = MapData.getCurrent().getTileType(tx, ty);
                if (t == null) break;
                if (t == TileType.WALL) break;
                range.add(new Point(tx, ty, dir));
                if (t == TileType.BRICK) break;
            }
        }
        return range;
    }

    /** 爆炸覆盖点 */
    public static class Point {
        public final int x;
        public final int y;
        public final ExplosionObj.Direction direction;

        public Point(int x, int y, ExplosionObj.Direction direction) {
            this.x = x;
            this.y = y;
            this.direction = direction;
        }
    }
}
