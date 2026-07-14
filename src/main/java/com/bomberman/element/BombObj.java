package com.bomberman.element;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.ImageIcon;

import com.bomberman.util.Config;
import com.bomberman.util.ResourceManager;

/**
 * 炸弹元素。由玩家/AI 放置，倒计时结束后由 GameThread 触发爆炸。
 *
 * createElement 格式："tileX,tileY,power"
 * owner 通过 setOwner 在创建后注入（字符串无法携带对象引用）。
 */
public class BombObj extends ElementObj {
    private int tileX;
    private int tileY;
    private float timer;
    private int power;
    private PlayerObj owner;

    public BombObj() {
        super();
    }

    @Override
    public void showElement(Graphics g) {
        int x = tileX * Config.TILE_SIZE;
        int y = tileY * Config.TILE_SIZE;
        int size = Config.TILE_SIZE;

        float timerPercent = timer / Config.BOMB_TIMER;
        ImageIcon icon = ResourceManager.getInstance().getBombFrame(timerPercent);
        if (icon != null) {
            g.drawImage(icon.getImage(), x, y, size, size, null);
        } else {
            float pulse = 1.0f + (float) Math.sin(System.currentTimeMillis() / 100.0) * 0.1f;
            float bSize = size * 0.7f * pulse;
            float off = (size - bSize) / 2f;
            g.setColor(Color.decode("#ff5722"));
            g.fillOval((int) (x + off), (int) (y + off), (int) bSize, (int) bSize);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        int t = (int) Math.ceil(timer);
        g.drawString(String.valueOf(t), x + Config.TILE_SIZE / 2 - 4, y + Config.TILE_SIZE / 2 + 5);
    }

    /** 倒计时；返回 true 表示到时爆炸 */
    public boolean tick(float dt) {
        timer -= dt;
        return timer <= 0;
    }

    @Override
    public ElementObj createElement(String str) {
        String[] a = str.split(",");
        this.tileX = Integer.parseInt(a[0]);
        this.tileY = Integer.parseInt(a[1]);
        this.power = Integer.parseInt(a[2]);
        this.timer = Config.BOMB_TIMER;

        setX(tileX * Config.TILE_SIZE);
        setY(tileY * Config.TILE_SIZE);
        setW(Config.TILE_SIZE);
        setH(Config.TILE_SIZE);
        return this;
    }

    public int getTileX() { return tileX; }
    public int getTileY() { return tileY; }
    public float getTimer() { return timer; }
    public int getPower() { return power; }
    public PlayerObj getOwner() { return owner; }
    public void setOwner(PlayerObj owner) { this.owner = owner; }
}
