package com.bomberman.element;

import java.awt.Graphics;

import javax.swing.ImageIcon;

import com.bomberman.enums.TileType;
import com.bomberman.util.Config;
import com.bomberman.util.ResourceManager;

/**
 * 地图格子元素：地板/墙壁/砖块。作为 ElementManager.MAPS 中的元素被统一绘制。
 *
 * createElement 格式："tileX,tileY,typeCode,themeId"
 *   typeCode: 0=EMPTY, 1=WALL, 2=BRICK
 *   themeId: 地图主题(0/1/2)，决定使用的瓦片/墙壁/砖块图片索引
 */
public class MapTileObj extends ElementObj {
    private int tileX;
    private int tileY;
    private TileType type;
    private ImageIcon floorImg;
    private ImageIcon overlayImg; // 墙壁或砖块图片；空地为 null

    public MapTileObj() {
        super();
    }

    @Override
    public void showElement(Graphics g) {
        int px = tileX * Config.TILE_SIZE;
        int py = tileY * Config.TILE_SIZE;
        int size = Config.TILE_SIZE;
        if (floorImg != null) {
            g.drawImage(floorImg.getImage(), px, py, size, size, null);
        }
        if (overlayImg != null) {
            g.drawImage(overlayImg.getImage(), px, py, size, size, null);
        }
    }

    /** 砖块被炸毁时切换为空地（保留地板） */
    public void setType(TileType type) {
        this.type = type;
        this.overlayImg = overlayFor(type);
    }

    private ImageIcon overlayFor(TileType t) {
        switch (t) {
            case WALL: return wallImg;
            case BRICK: return brickImg;
            default: return null;
        }
    }

    public TileType getType() { return type; }
    public int getTileX() { return tileX; }
    public int getTileY() { return tileY; }

    // 主题图片缓存（createElement 时按主题取一次）
    private ImageIcon wallImg;
    private ImageIcon brickImg;

    @Override
    public ElementObj createElement(String str) {
        String[] a = str.split(",");
        this.tileX = Integer.parseInt(a[0]);
        this.tileY = Integer.parseInt(a[1]);
        int typeCode = Integer.parseInt(a[2]);
        int themeId = Integer.parseInt(a[3]);
        this.type = TileType.values()[typeCode];

        int[] theme = MapData.THEMES[themeId];
        this.floorImg = ResourceManager.getInstance().getTileImageByIndex(theme[0]);
        this.wallImg = ResourceManager.getInstance().getWallImageByIndex(theme[1]);
        this.brickImg = ResourceManager.getInstance().getBrickImageByIndex(theme[2]);
        this.overlayImg = overlayFor(type);

        setX(tileX * Config.TILE_SIZE);
        setY(tileY * Config.TILE_SIZE);
        setW(Config.TILE_SIZE);
        setH(Config.TILE_SIZE);
        return this;
    }
}
