package com.bomberman.element;

import com.bomberman.enums.TileType;
import com.bomberman.util.Config;

import java.util.Random;

/**
 * 地图数据：格阵 + O(1) 查询。由 GameLoad.MapLoad 构建并填充。
 *
 * 同时持有 MapTileObj[][] 引用，便于砖块被炸毁时直接改其类型（无需线性查找）。
 * 静态 {@link #current} 指向当前关卡的地图，供碰撞/AI/绘制共享。
 */
public class MapData {
    /** 各主题的图片索引：{tileImgIdx, wallImgIdx, brickImgIdx} */
    public static final int[][] THEMES = {
            {0, 0, 0},     // 经典草原
            {2, 1, 10},    // 石砖地牢
            {5, 2, 20},    // 沙漠遗迹
    };

    private final TileType[][] grid;
    private final MapTileObj[][] tileRefs;

    private static MapData current;
    private static final Random RANDOM = new Random();

    public MapData() {
        grid = new TileType[Config.MAP_WIDTH][Config.MAP_HEIGHT];
        tileRefs = new MapTileObj[Config.MAP_WIDTH][Config.MAP_HEIGHT];
    }

    /** 由 GameLoad.MapLoad 在构建格子时调用，登记格子元素引用 */
    public void setCell(int x, int y, TileType type, MapTileObj tile) {
        grid[x][y] = type;
        tileRefs[x][y] = tile;
    }

    public TileType getTileType(int x, int y) {
        if (x < 0 || x >= Config.MAP_WIDTH || y < 0 || y >= Config.MAP_HEIGHT) {
            return null;
        }
        return grid[x][y];
    }

    public boolean isWalkable(int x, int y) {
        TileType t = getTileType(x, y);
        return t == TileType.EMPTY;
    }

    /** 砖块被炸毁：格变空地，元素改类型；返回是否掉落道具(30%) */
    public boolean destroyTile(int x, int y) {
        if (getTileType(x, y) != TileType.BRICK) {
            return false;
        }
        grid[x][y] = TileType.EMPTY;
        if (tileRefs[x][y] != null) {
            tileRefs[x][y].setType(TileType.EMPTY);
        }
        return RANDOM.nextFloat() < 0.3f;
    }

    public static MapData getCurrent() { return current; }
    public static void setCurrent(MapData md) { current = md; }
}
