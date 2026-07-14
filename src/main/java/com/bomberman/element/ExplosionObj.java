package com.bomberman.element;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.ImageIcon;

import com.bomberman.util.Config;
import com.bomberman.util.ResourceManager;

/**
 * 爆炸效果元素。由炸弹爆炸时创建，持续 EXPLOSION_DURATION 后消失。
 *
 * createElement 格式："tileX,tileY,dirCode,fromPlayerFlag"
 *   dirCode: 0=CENTER,1=UP,2=DOWN,3=LEFT,4=RIGHT
 *   fromPlayerFlag: 1=玩家1炸弹(蓝色), 0=其它(粉色)
 */
public class ExplosionObj extends ElementObj {
    public enum Direction { CENTER, UP, DOWN, LEFT, RIGHT }

    private int tileX;
    private int tileY;
    private Direction direction;
    private boolean isCenter;
    private boolean fromPlayer;
    private float duration;

    public ExplosionObj() {
        super();
    }

    @Override
    public void showElement(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int x = tileX * Config.TILE_SIZE;
        int y = tileY * Config.TILE_SIZE;
        int size = Config.TILE_SIZE;

        String color = fromPlayer ? "blue" : "pink";
        List<ImageIcon> frames = ResourceManager.getInstance().getExplosionFrames(color);
        if (frames.isEmpty()) return;

        int frameIdx;
        double angle = 0;
        switch (direction) {
            case CENTER: frameIdx = 0; break;
            case LEFT:
            case RIGHT:  frameIdx = 1; break;
            case UP:
            case DOWN:   frameIdx = 1; angle = 90; break;
            default:     frameIdx = 0; break;
        }
        frameIdx = Math.min(frameIdx, frames.size() - 1);

        float alpha = Math.min(1.0f, duration / Config.EXPLOSION_DURATION);
        g2.setComposite(awtAlpha(alpha));

        ImageIcon icon = frames.get(frameIdx);
        if (angle != 0) {
            double cx = x + size / 2.0;
            double cy = y + size / 2.0;
            g2.rotate(Math.toRadians(angle), cx, cy);
            g2.drawImage(icon.getImage(), x, y, size, size, null);
            g2.rotate(-Math.toRadians(angle), cx, cy);
        } else {
            g2.drawImage(icon.getImage(), x, y, size, size, null);
        }
        g2.setComposite(awtAlpha(1.0f));
    }

    private static java.awt.Composite awtAlpha(float a) {
        return java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, a)));
    }

    /** 倒计时；返回 true 表示已结束 */
    public boolean tick(float dt) {
        duration -= dt;
        return duration <= 0;
    }

    @Override
    public ElementObj createElement(String str) {
        String[] a = str.split(",");
        this.tileX = Integer.parseInt(a[0]);
        this.tileY = Integer.parseInt(a[1]);
        this.direction = Direction.values()[Integer.parseInt(a[2])];
        this.isCenter = (direction == Direction.CENTER);
        this.fromPlayer = "1".equals(a[3]);
        this.duration = Config.EXPLOSION_DURATION;

        setX(tileX * Config.TILE_SIZE);
        setY(tileY * Config.TILE_SIZE);
        setW(Config.TILE_SIZE);
        setH(Config.TILE_SIZE);
        return this;
    }

    public int getTileX() { return tileX; }
    public int getTileY() { return tileY; }
    public Direction getDirection() { return direction; }
    public boolean isCenter() { return isCenter; }
    public boolean isFromPlayer() { return fromPlayer; }
}
