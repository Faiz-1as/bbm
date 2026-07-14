package com.bomberman.manager;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.bomberman.element.AIPlayerObj;
import com.bomberman.element.ElementObj;
import com.bomberman.element.MapData;
import com.bomberman.element.MapTileObj;
import com.bomberman.element.PlayerObj;
import com.bomberman.enums.TileType;
import com.bomberman.util.Config;

/**
 * 资源加载器：静态工具类。对标参考框架 com.tedu.manager.GameLoad。
 *
 * 职责：
 *  - loadImg：读 text/GameData.pro -> imgMap<String,ImageIcon>（命名图片注册表）
 *  - loadObj：读 text/obj.pro -> objMap<String,Class<?>>（元素类反射注册表）
 *  - getObj(key)：反射实例化元素，配合子类 createElement(str) 工厂方法（解耦）
 *  - MapLoad(mapId)：读 text/N.map -> 构建 MapData + 生成 MapTileObj 元素入 ElementManager.MAPS
 *  - loadPlay：生成玩家/AI 元素入 ElementManager.PLAY/ENEMY
 */
public class GameLoad {
    private static final ElementManager em = ElementManager.getManager();
    private static final GameContext ctx = GameContext.getInstance();

    /** 命名图片缓存（元素工厂按名取图） */
    public static Map<String, ImageIcon> imgMap = new java.util.HashMap<>();
    /** 元素类反射注册表 */
    private static final Map<String, Class<?>> objMap = new java.util.HashMap<>();
    private static final Properties pro = new Properties();
    private static final Random RANDOM = new Random();

    // ==================== 图片 ====================
    public static void loadImg() {
        String path = "text/GameData.pro";
        try (InputStream is = GameLoad.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("[GameLoad] 缺少 " + path);
                return;
            }
            pro.clear();
            pro.load(is);
            for (Object o : pro.keySet()) {
                String key = o.toString();
                String imgPath = pro.getProperty(key);
                ImageIcon icon = loadIcon(imgPath);
                if (icon != null) imgMap.put(key, icon);
            }
            System.out.println("[GameLoad] 图片加载: " + imgMap.size() + " 项");
        } catch (IOException e) {
            System.err.println("[GameLoad] loadImg 失败: " + e.getMessage());
        }
    }

    private static ImageIcon loadIcon(String path) {
        try {
            URL url = GameLoad.class.getClassLoader().getResource(path);
            if (url == null) {
                System.err.println("[GameLoad] 找不到图片: " + path);
                return null;
            }
            BufferedImage bi = ImageIO.read(url);
            return bi == null ? null : new ImageIcon(bi);
        } catch (Exception e) {
            System.err.println("[GameLoad] 加载图片失败 " + path + ": " + e.getMessage());
            return null;
        }
    }

    // ==================== 反射工厂 ====================
    public static void loadObj() {
        String path = "text/obj.pro";
        try (InputStream is = GameLoad.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("[GameLoad] 缺少 " + path);
                return;
            }
            pro.clear();
            pro.load(is);
            for (Object o : pro.keySet()) {
                String key = o.toString();
                String classUrl = pro.getProperty(key);
                try {
                    objMap.put(key, Class.forName(classUrl));
                } catch (ClassNotFoundException e) {
                    System.err.println("[GameLoad] 类未找到: " + classUrl);
                }
            }
            System.out.println("[GameLoad] 元素类注册: " + objMap.size() + " 项");
        } catch (IOException e) {
            System.err.println("[GameLoad] loadObj 失败: " + e.getMessage());
        }
    }

    /** 反射实例化元素（约定无参构造）；配合 createElement(str) 填充数据 */
    public static ElementObj getObj(String key) {
        Class<?> cls = objMap.get(key);
        if (cls == null) {
            System.err.println("[GameLoad] 未知元素 key: " + key);
            return null;
        }
        try {
            Object obj = cls.getDeclaredConstructor().newInstance();
            if (obj instanceof ElementObj) return (ElementObj) obj;
        } catch (Exception e) {
            System.err.println("[GameLoad] 实例化失败 " + key + ": " + e.getMessage());
        }
        return null;
    }

    // ==================== 地图 ====================
    public static void MapLoad(int mapId) {
        if (mapId < 0 || mapId >= Config.MAP_NAMES.length) mapId = 0;
        String fileName = "text/" + (mapId + 1) + ".map";
        try (InputStream is = GameLoad.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                System.err.println("[GameLoad] 缺少地图: " + fileName);
                return;
            }
            pro.clear();
            pro.load(is);
        } catch (IOException e) {
            System.err.println("[GameLoad] 读取地图失败 " + fileName + ": " + e.getMessage());
            return;
        }

        // 解析主题(图片索引+砖块密度)
        int tileIdx = MapData.THEMES[mapId][0];
        int wallIdx = MapData.THEMES[mapId][1];
        int brickIdx = MapData.THEMES[mapId][2];
        float density = 0.6f;
        String theme = pro.getProperty("THEME");
        if (theme != null) {
            String[] t = theme.split(",");
            if (t.length >= 4) {
                tileIdx = Integer.parseInt(t[0].trim());
                wallIdx = Integer.parseInt(t[1].trim());
                brickIdx = Integer.parseInt(t[2].trim());
                density = Float.parseFloat(t[3].trim());
            }
        }
        // 解析内部墙坐标
        List<int[]> walls = new ArrayList<>();
        String wallStr = pro.getProperty("WALL");
        if (wallStr != null) {
            for (String pair : wallStr.split(";")) {
                String[] xy = pair.split(",");
                if (xy.length == 2) walls.add(new int[]{Integer.parseInt(xy[0].trim()), Integer.parseInt(xy[1].trim())});
            }
        }

        MapData md = new MapData();
        for (int y = 0; y < Config.MAP_HEIGHT; y++) {
            for (int x = 0; x < Config.MAP_WIDTH; x++) {
                TileType type = TileType.EMPTY;
                boolean boundary = (x == 0 || x == Config.MAP_WIDTH - 1 || y == 0 || y == Config.MAP_HEIGHT - 1);
                if (boundary) {
                    type = TileType.WALL;
                } else if (contains(walls, x, y)) {
                    type = TileType.WALL;
                } else {
                    boolean safe = (x < 3 && y < 3) || (x > Config.MAP_WIDTH - 4 && y > Config.MAP_HEIGHT - 4);
                    if (!safe && RANDOM.nextFloat() < density) {
                        type = TileType.BRICK;
                    }
                }
                MapTileObj tile = (MapTileObj) getObj("maptile");
                tile.createElement(x + "," + y + "," + type.ordinal() + "," + mapId);
                md.setCell(x, y, type, tile);
                em.addElement(tile, GameElement.MAPS);
            }
        }
        MapData.setCurrent(md);
        ctx.setMapIndex(mapId);
        ctx.setMapName(Config.MAP_NAMES[mapId]);
        System.out.println("[GameLoad] 地图加载完成: " + Config.MAP_NAMES[mapId]);
    }

    private static boolean contains(List<int[]> walls, int x, int y) {
        for (int[] w : walls) if (w[0] == x && w[1] == y) return true;
        return false;
    }

    // ==================== 玩家/AI ====================
    public static void loadPlay(boolean twoPlayer) {
        em.getElementsByKey(GameElement.PLAY).clear();
        em.getElementsByKey(GameElement.ENEMY).clear();
        em.getElementsByKey(GameElement.BOMB).clear();
        em.getElementsByKey(GameElement.EXPLOSION).clear();
        em.getElementsByKey(GameElement.POWERUP).clear();

        // 玩家1
        PlayerObj p1 = (PlayerObj) getObj("play");
        p1.createElement("1,1,blue,1");
        em.addElement(p1, GameElement.PLAY);
        ctx.setPlayer1(p1);

        if (twoPlayer) {
            PlayerObj p2 = (PlayerObj) getObj("play");
            p2.createElement((Config.MAP_WIDTH - 2) + "," + (Config.MAP_HEIGHT - 2) + ",pink,2");
            em.addElement(p2, GameElement.PLAY);
            ctx.setPlayer2(p2);
        } else {
            ctx.setPlayer2(null);
            AIPlayerObj ai = (AIPlayerObj) getObj("enemy");
            ai.createElement((Config.MAP_WIDTH - 2) + "," + (Config.MAP_HEIGHT - 2));
            em.addElement(ai, GameElement.ENEMY);
        }
    }
}
