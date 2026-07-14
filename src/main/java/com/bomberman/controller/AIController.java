package com.bomberman.controller;

import com.bomberman.element.AIPlayerObj;
import com.bomberman.element.BombObj;
import com.bomberman.element.ElementObj;
import com.bomberman.element.ExplosionObj;
import com.bomberman.element.MapData;
import com.bomberman.element.PlayerObj;
import com.bomberman.enums.AIState;
import com.bomberman.enums.TileType;
import com.bomberman.manager.ElementManager;
import com.bomberman.manager.GameContext;
import com.bomberman.manager.GameElement;
import com.bomberman.util.Config;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * AI 行为控制器：单例。适配新结构（MapData.current / ElementManager / CollisionController 静态方法）。
 *
 * 移动采用"逐格对齐"：AI 始终朝某相邻格左上角(tile*TILE)移动，抵达后再选下一格，
 * 避免跨格(straddle)被柱子卡住。
 *  - SEEKING：只选可走且非危险的相邻格
 *  - FLEEING：BFS 找最近安全格，允许途经危险格
 *  - PLACING_BOMB：放弹后立刻进入逃跑
 */
public class AIController {
    private static final AIController INSTANCE = new AIController();
    private final Random random = new Random();

    private AIController() {}

    public static AIController getInstance() { return INSTANCE; }

    public void updateAI(AIPlayerObj ai, float dt) {
        int aiTileX = Math.round(ai.getFx() / Config.TILE_SIZE);
        int aiTileY = Math.round(ai.getFy() / Config.TILE_SIZE);

        AIState prevState = ai.getState();
        AIState newState;
        if (isPositionDangerous(aiTileX, aiTileY)) {
            newState = AIState.FLEEING;
        } else if (shouldPlaceBomb(ai)) {
            newState = AIState.PLACING_BOMB;
        } else {
            newState = AIState.SEEKING;
        }
        ai.setState(newState);
        if (newState != prevState) {
            ai.clearTarget();
        }

        switch (newState) {
            case FLEEING:       flee(ai, dt); break;
            case PLACING_BOMB:  placeBomb(ai); break;
            case SEEKING:       seek(ai, dt); break;
            default: break;
        }
    }

    private void seek(AIPlayerObj ai, float dt) {
        if (!ai.hasTarget()) chooseSeekTarget(ai);
        moveTowardTarget(ai, dt);
    }

    private void flee(AIPlayerObj ai, float dt) {
        if (!ai.hasTarget()) {
            boolean[][] danger = computeDangerMap();
            int[] step = findNearestSafeStep(ai, danger);
            if (step != null) ai.setTarget(step[0], step[1]);
        }
        moveTowardTarget(ai, dt);
    }

    private void placeBomb(AIPlayerObj ai) {
        if (ai.getCurrentBombs() < ai.getMaxBombs()) {
            int tx = Math.round(ai.getFx() / Config.TILE_SIZE);
            int ty = Math.round(ai.getFy() / Config.TILE_SIZE);
            ElementObj proto = com.bomberman.manager.GameLoad.getObj("bomb");
            if (proto instanceof BombObj) {
                BombObj bomb = (BombObj) proto.createElement(tx + "," + ty + "," + ai.getBombPower());
                bomb.setOwner(ai);
                ElementManager.getManager().addElement(bomb, GameElement.BOMB);
                ai.onBombPlaced();
            }
        }
        ai.clearTarget();
        ai.setState(AIState.FLEEING);
    }

    private void chooseSeekTarget(AIPlayerObj ai) {
        int curX = Math.round(ai.getFx() / Config.TILE_SIZE);
        int curY = Math.round(ai.getFy() / Config.TILE_SIZE);
        boolean[][] danger = computeDangerMap();
        int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        List<int[]> valid = new ArrayList<>();
        for (int[] d : dirs) {
            int nx = curX + d[0];
            int ny = curY + d[1];
            if (isWalkable(nx, ny) && !danger[nx][ny]) valid.add(new int[]{nx, ny});
        }
        if (!valid.isEmpty()) {
            int[] t = valid.get(random.nextInt(valid.size()));
            ai.setTarget(t[0], t[1]);
        } else {
            ai.clearTarget();
        }
    }

    private void moveTowardTarget(AIPlayerObj ai, float dt) {
        if (!ai.hasTarget()) return;
        float targetPxX = ai.getTargetTileX() * Config.TILE_SIZE;
        float targetPxY = ai.getTargetTileY() * Config.TILE_SIZE;
        float step = ai.getSpeed() * dt;
        boolean blocked = false;

        float diffX = targetPxX - ai.getFx();
        if (Math.abs(diffX) > 0.001f) {
            float dirX = Math.signum(diffX);
            float amt = Math.min(step, Math.abs(diffX));
            if (!CollisionController.checkPlayerMapCollision(ai, dirX * amt, 0)
                    && !CollisionController.checkPlayerBombCollision(ai, dirX * amt, 0)) {
                ai.setFx(ai.getFx() + dirX * amt);
            } else {
                blocked = true;
            }
        }
        float diffY = targetPxY - ai.getFy();
        if (Math.abs(diffY) > 0.001f) {
            float dirY = Math.signum(diffY);
            float amt = Math.min(step, Math.abs(diffY));
            if (!CollisionController.checkPlayerMapCollision(ai, 0, dirY * amt)
                    && !CollisionController.checkPlayerBombCollision(ai, 0, dirY * amt)) {
                ai.setFy(ai.getFy() + dirY * amt);
            } else {
                blocked = true;
            }
        }
        if (blocked) { ai.clearTarget(); return; }
        if (Math.abs(ai.getFx() - targetPxX) <= 0.001f && Math.abs(ai.getFy() - targetPxY) <= 0.001f) {
            ai.clearTarget();
        }
    }

    private boolean[][] computeDangerMap() {
        int w = Config.MAP_WIDTH, h = Config.MAP_HEIGHT;
        boolean[][] danger = new boolean[w][h];
        for (ElementObj each : ElementManager.getManager().getElementsByKey(GameElement.BOMB)) {
            BombObj bomb = (BombObj) each;
            for (CollisionController.Point p : CollisionController.getBombExplosionRange(bomb)) {
                danger[p.x][p.y] = true;
            }
        }
        for (ElementObj each : ElementManager.getManager().getElementsByKey(GameElement.EXPLOSION)) {
            ExplosionObj exp = (ExplosionObj) each;
            danger[exp.getTileX()][exp.getTileY()] = true;
        }
        return danger;
    }

    private int[] findNearestSafeStep(AIPlayerObj ai, boolean[][] danger) {
        int startX = Math.round(ai.getFx() / Config.TILE_SIZE);
        int startY = Math.round(ai.getFy() / Config.TILE_SIZE);
        int w = Config.MAP_WIDTH, h = Config.MAP_HEIGHT;
        boolean[][] visited = new boolean[w][h];
        Queue<int[]> queue = new LinkedList<>();
        int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        visited[startX][startY] = true;
        for (int[] d : dirs) {
            int nx = startX + d[0], ny = startY + d[1];
            if (isWalkable(nx, ny)) {
                visited[nx][ny] = true;
                queue.add(new int[]{nx, ny, nx, ny});
            }
        }
        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int cx = cur[0], cy = cur[1], fsx = cur[2], fsy = cur[3];
            if (!danger[cx][cy]) return new int[]{fsx, fsy};
            for (int[] d : dirs) {
                int nx = cx + d[0], ny = cy + d[1];
                if (nx >= 0 && nx < w && ny >= 0 && ny < h && !visited[nx][ny] && isWalkable(nx, ny)) {
                    visited[nx][ny] = true;
                    queue.add(new int[]{nx, ny, fsx, fsy});
                }
            }
        }
        return null;
    }

    private boolean isWalkable(int x, int y) {
        TileType t = MapData.getCurrent().getTileType(x, y);
        if (t != TileType.EMPTY) return false;
        for (ElementObj each : ElementManager.getManager().getElementsByKey(GameElement.BOMB)) {
            BombObj bomb = (BombObj) each;
            if (bomb.getTileX() == x && bomb.getTileY() == y) return false;
        }
        for (ElementObj each : ElementManager.getManager().getElementsByKey(GameElement.EXPLOSION)) {
            ExplosionObj exp = (ExplosionObj) each;
            if (exp.getTileX() == x && exp.getTileY() == y) return false;
        }
        return true;
    }

    private boolean isPositionDangerous(int x, int y) {
        for (ElementObj each : ElementManager.getManager().getElementsByKey(GameElement.EXPLOSION)) {
            ExplosionObj exp = (ExplosionObj) each;
            if (exp.getTileX() == x && exp.getTileY() == y) return true;
        }
        for (ElementObj each : ElementManager.getManager().getElementsByKey(GameElement.BOMB)) {
            BombObj bomb = (BombObj) each;
            if (bomb.getTileX() == x && bomb.getTileY() == y) return true;
            if (bomb.getX() == x) {
                int minY = Math.min(bomb.getTileY(), y);
                int maxY = Math.max(bomb.getTileY(), y);
                boolean blocked = false;
                for (int ty = minY + 1; ty < maxY; ty++) {
                    TileType t = MapData.getCurrent().getTileType(x, ty);
                    if (t != TileType.EMPTY) { blocked = true; break; }
                }
                if (!blocked && Math.abs(bomb.getTileY() - y) <= bomb.getPower()) return true;
            }
            if (bomb.getTileY() == y) {
                int minX = Math.min(bomb.getTileX(), x);
                int maxX = Math.max(bomb.getTileX(), x);
                boolean blocked = false;
                for (int tx = minX + 1; tx < maxX; tx++) {
                    TileType t = MapData.getCurrent().getTileType(tx, y);
                    if (t != TileType.EMPTY) { blocked = true; break; }
                }
                if (!blocked && Math.abs(bomb.getTileX() - x) <= bomb.getPower()) return true;
            }
        }
        return false;
    }

    private boolean shouldPlaceBomb(AIPlayerObj ai) {
        int aiTileX = Math.round(ai.getFx() / Config.TILE_SIZE);
        int aiTileY = Math.round(ai.getFy() / Config.TILE_SIZE);
        boolean nearBrick = false;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if ((dx == 0 && dy == 0) || (dx != 0 && dy != 0)) continue;
                TileType t = MapData.getCurrent().getTileType(aiTileX + dx, aiTileY + dy);
                if (t == TileType.BRICK) nearBrick = true;
            }
        }
        boolean nearPlayer = false;
        if (isPlayerNear(aiTileX, aiTileY, 3)) nearPlayer = true;
        if (!nearBrick && !nearPlayer) return false;
        if (!hasEscapeAfterBomb(ai)) return false;
        float chance = nearPlayer ? 0.1f : 0.05f;
        return random.nextFloat() < chance;
    }

    private boolean isPlayerNear(int aiTileX, int aiTileY, int dist) {
        GameContext ctx = GameContext.getInstance();
        if (nearPlayer(ctx.getPlayer1(), aiTileX, aiTileY, dist)) return true;
        if (ctx.isTwoPlayerMode() && nearPlayer(ctx.getPlayer2(), aiTileX, aiTileY, dist)) return true;
        return false;
    }

    private boolean nearPlayer(PlayerObj p, int aiTileX, int aiTileY, int dist) {
        if (p == null || !p.isAlive()) return false;
        int ptx = Math.round(p.getFx() / Config.TILE_SIZE);
        int pty = Math.round(p.getFy() / Config.TILE_SIZE);
        return Math.abs(aiTileX - ptx) + Math.abs(aiTileY - pty) <= dist;
    }

    private boolean hasEscapeAfterBomb(AIPlayerObj ai) {
        int bx = Math.round(ai.getFx() / Config.TILE_SIZE);
        int by = Math.round(ai.getFy() / Config.TILE_SIZE);
        boolean[][] danger = computeDangerMap();
        BombObj hypo = (BombObj) com.bomberman.manager.GameLoad.getObj("bomb");
        if (hypo == null) return true;
        hypo.createElement(bx + "," + by + "," + ai.getBombPower());
        for (CollisionController.Point p : CollisionController.getBombExplosionRange(hypo)) {
            danger[p.x][p.y] = true;
        }
        return findNearestSafeStep(ai, danger) != null;
    }
}
