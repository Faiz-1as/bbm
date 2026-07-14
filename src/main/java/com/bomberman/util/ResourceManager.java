package com.bomberman.util;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图片资源管理器：单例。预加载并缓存所有游戏图片（ImageIcon）。
 *
 * 取代原 JavaFX 版 ResourceLoader 的图片部分。图片按分组缓存：
 * 瓦片/墙壁/砖块/销毁动画/炸弹帧/爆炸帧(按色)/玩家精灵(按色)/道具/按钮/背景。
 *
 * 音频见 {@link AudioManager}。
 */
public class ResourceManager {
    private static ResourceManager instance;

    private final Map<String, ImageIcon> imageCache = new HashMap<>();

    private final List<ImageIcon> tileImages = new ArrayList<>();
    private final List<ImageIcon> wallImages = new ArrayList<>();
    private final List<ImageIcon> brickImages = new ArrayList<>();
    private final List<ImageIcon> barrierDestroyImages = new ArrayList<>();
    private final List<ImageIcon> bombFrames = new ArrayList<>();
    private final Map<String, List<ImageIcon>> explosionFrames = new HashMap<>();
    private final Map<String, List<ImageIcon>> playerSprites = new HashMap<>();
    private final List<ImageIcon> siteImages = new ArrayList<>();

    private ImageIcon btnUp, btnOver, btnDown;
    private ImageIcon backgroundImage;
    private ImageIcon shadowImage;

    private ResourceManager() {
        loadAll();
    }

    public static synchronized ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    // ==================== 加载 ====================
    private void loadAll() {
        try {
            loadTileImages();
            loadWallImages();
            loadBrickImages();
            loadBarrierDestroyImages();
            loadBombFrames();
            loadExplosionFrames();
            loadPlayerSprites();
            loadSiteImages();
            loadButtonImages();
            loadBackgrounds();
            System.out.println("[ResourceManager] 图片资源加载完成");
        } catch (Exception e) {
            System.err.println("[ResourceManager] 加载失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ImageIcon load(String path) {
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }
        try {
            URL url = getClass().getClassLoader().getResource(path);
            if (url == null) {
                System.err.println("[ResourceManager] 找不到资源: " + path);
                return null;
            }
            BufferedImage bi = ImageIO.read(url);
            if (bi == null) {
                return null;
            }
            ImageIcon icon = new ImageIcon(bi);
            imageCache.put(path, icon);
            return icon;
        } catch (Exception e) {
            System.err.println("[ResourceManager] 加载图片失败: " + path + " - " + e.getMessage());
            return null;
        }
    }

    private void loadTileImages() {
        int[] ids = {212, 214, 216, 218, 220, 224, 226, 228, 230};
        for (int id : ids) {
            ImageIcon img = load("image/tiles/" + id + ".png");
            if (img != null) tileImages.add(img);
        }
        ImageIcon jpg = load("image/tiles/222.jpg");
        if (jpg != null) tileImages.add(jpg);
    }

    private void loadWallImages() {
        int[] ids = {92, 94, 96, 98};
        for (int id : ids) {
            ImageIcon img = load("image/barriers/" + id + ".png");
            if (img != null) wallImages.add(img);
        }
    }

    private void loadBrickImages() {
        int[] ids = {100, 102, 104, 106, 108, 110, 112, 114, 116, 118, 120,
                122, 124, 126, 128, 130, 132, 134, 136, 138, 140, 142,
                144, 146, 148, 150, 152, 154, 156, 158, 160, 162, 164,
                166, 168, 170, 172, 174, 176, 178, 180, 182, 184};
        for (int id : ids) {
            ImageIcon img = load("image/barriers/" + id + ".png");
            if (img != null) brickImages.add(img);
        }
    }

    private void loadBarrierDestroyImages() {
        int[] ids = {239, 241, 243, 245, 247};
        for (int id : ids) {
            ImageIcon img = load("image/barrier_destroy/" + id + ".png");
            if (img != null) barrierDestroyImages.add(img);
        }
    }

    private void loadBombFrames() {
        for (int id = 328; id <= 370; id += 2) {
            ImageIcon img = load("image/boom/" + id + ".png");
            if (img != null) bombFrames.add(img);
        }
    }

    private void loadExplosionFrames() {
        String[] colors = {"blue", "green", "pink", "yellow"};
        int[][] frameIds = {
                {254, 262, 270},
                {256, 264, 272},
                {250, 258, 266},
                {252, 260, 268},
        };
        for (int i = 0; i < colors.length; i++) {
            List<ImageIcon> frames = new ArrayList<>();
            for (int id : frameIds[i]) {
                ImageIcon img = load("image/explode/" + colors[i] + "/" + id + ".png");
                if (img != null) frames.add(img);
            }
            explosionFrames.put(colors[i], frames);
        }
    }

    private void loadPlayerSprites() {
        String[] colors = {"blue", "green", "pink", "yellow"};
        int[][] frameIds = {
                {26, 28, 30, 32, 35, 37, 39, 41, 44},
                {4, 6, 8, 10, 13, 15, 17, 19, 22},
                {70, 72, 74, 76, 79, 81, 83, 85, 88},
                {48, 50, 52, 54, 57, 59, 61, 63, 66},
        };
        for (int i = 0; i < colors.length; i++) {
            List<ImageIcon> frames = new ArrayList<>();
            String folder = "image/player_" + colors[i] + "/";
            for (int id : frameIds[i]) {
                ImageIcon img = load(folder + id + ".png");
                if (img != null) frames.add(img);
            }
            playerSprites.put(colors[i], frames);
        }
    }

    private void loadSiteImages() {
        int[] ids = {186, 188, 190, 192, 194};
        for (int id : ids) {
            ImageIcon img = load("image/site/" + id + ".png");
            if (img != null) siteImages.add(img);
        }
    }

    private void loadButtonImages() {
        btnUp = load("image/Button/1_up.png");
        btnOver = load("image/Button/2_over.png");
        btnDown = load("image/Button/3_down.png");
    }

    private void loadBackgrounds() {
        backgroundImage = load("image/baiyun.png");
        shadowImage = load("image/shadow.png");
    }

    // ==================== 取图接口 ====================

    public ImageIcon getTileImageByIndex(int index) {
        if (tileImages.isEmpty()) return null;
        return tileImages.get(index % tileImages.size());
    }

    public ImageIcon getWallImageByIndex(int index) {
        if (wallImages.isEmpty()) return null;
        return wallImages.get(index % wallImages.size());
    }

    public ImageIcon getBrickImageByIndex(int index) {
        if (brickImages.isEmpty()) return null;
        return brickImages.get(index % brickImages.size());
    }

    public ImageIcon getBarrierDestroyFrame(int frameIndex) {
        if (barrierDestroyImages.isEmpty()) return null;
        return barrierDestroyImages.get(frameIndex % barrierDestroyImages.size());
    }

    public int getBarrierDestroyFrameCount() {
        return barrierDestroyImages.size();
    }

    public ImageIcon getBombFrame(float timerPercent) {
        if (bombFrames.isEmpty()) return null;
        int idx = (int) ((1.0f - timerPercent) * (bombFrames.size() - 1));
        return bombFrames.get(Math.min(idx, bombFrames.size() - 1));
    }

    public List<ImageIcon> getExplosionFrames(String color) {
        return explosionFrames.getOrDefault(color, Collections.emptyList());
    }

    public List<ImageIcon> getPlayerSprites(String color) {
        return playerSprites.getOrDefault(color, Collections.emptyList());
    }

    /** @param color 颜色; @param direction 0=下,1=左,2=右,3=上; @param animFrame 0-2 */
    public ImageIcon getPlayerFrame(String color, int direction, int animFrame) {
        List<ImageIcon> sprites = playerSprites.get(color);
        if (sprites == null || sprites.isEmpty()) return null;
        int idx = Math.min(direction * 2 + (animFrame % 2), sprites.size() - 1);
        return sprites.get(idx);
    }

    public ImageIcon getSiteImage(int index) {
        if (siteImages.isEmpty()) return null;
        return siteImages.get(index % siteImages.size());
    }

    public ImageIcon getBtnUp() { return btnUp; }
    public ImageIcon getBtnOver() { return btnOver; }
    public ImageIcon getBtnDown() { return btnDown; }
    public ImageIcon getBackgroundImage() { return backgroundImage; }
    public ImageIcon getShadowImage() { return shadowImage; }
}
