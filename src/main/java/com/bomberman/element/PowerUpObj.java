package com.bomberman.element;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.ImageIcon;

import com.bomberman.enums.PowerUpType;
import com.bomberman.util.Config;
import com.bomberman.util.ResourceManager;

/**
 * 道具元素。砖块被炸毁时有概率掉落，玩家走过即拾取。
 *
 * createElement 格式："tileX,tileY,typeCode"
 *   typeCode: 0=SPEED, 1=BOMB_COUNT, 2=BOMB_POWER
 */
public class PowerUpObj extends ElementObj {
    private int tileX;
    private int tileY;
    private PowerUpType type;

    public PowerUpObj() {
        super();
    }

    @Override
    public void showElement(Graphics g) {
        int x = tileX * Config.TILE_SIZE;
        int y = tileY * Config.TILE_SIZE;
        int size = Config.TILE_SIZE;

        int idx = type.ordinal() % 5;
        ImageIcon icon = ResourceManager.getInstance().getSiteImage(idx);
        if (icon != null) {
            double imgSize = size * 0.7;
            double off = (size - imgSize) / 2.0;
            g.drawImage(icon.getImage(), (int) (x + off), (int) (y + off), (int) imgSize, (int) imgSize, null);
        } else {
            double pSize = size * 0.6;
            double off = (size - pSize) / 2.0;
            switch (type) {
                case SPEED:       g.setColor(Color.decode("#ffeb3b")); g.fillRect((int)(x+off),(int)(y+off),(int)pSize,(int)pSize); break;
                case BOMB_COUNT:  g.setColor(Color.decode("#ff5722")); g.fillOval((int)(x+off),(int)(y+off),(int)pSize,(int)pSize); break;
                case BOMB_POWER:  g.setColor(Color.decode("#ff9800")); g.fillRect((int)(x+off),(int)(y+off),(int)pSize,(int)pSize); break;
            }
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] a = str.split(",");
        this.tileX = Integer.parseInt(a[0]);
        this.tileY = Integer.parseInt(a[1]);
        this.type = PowerUpType.values()[Integer.parseInt(a[2])];
        setX(tileX * Config.TILE_SIZE);
        setY(tileY * Config.TILE_SIZE);
        setW(Config.TILE_SIZE);
        setH(Config.TILE_SIZE);
        return this;
    }

    public int getTileX() { return tileX; }
    public int getTileY() { return tileY; }
    public PowerUpType getType() { return type; }
}
