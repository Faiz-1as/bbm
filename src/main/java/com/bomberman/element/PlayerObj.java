package com.bomberman.element;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.ImageIcon;

import com.bomberman.controller.CollisionController;
import com.bomberman.controller.GameThread;
import com.bomberman.manager.ElementManager;
import com.bomberman.manager.GameElement;
import com.bomberman.manager.GameLoad;
import com.bomberman.util.AudioManager;
import com.bomberman.util.Config;
import com.bomberman.util.ResourceManager;

/**
 * 玩家元素。继承 ElementObj，重写 model 各步骤：
 *  - keyClick：按角色(role)记录移动键状态 / 触发放弹请求（由 GameListener 转发）
 *  - move：根据按键位移并做碰撞（墙/砖/炸弹）
 *  - add：消费放弹请求，生成 BombObj 加入 ElementManager.BOMB
 *  - showElement：绘制阴影 + 方向精灵 + 无敌闪烁
 *
 * createElement 格式："tileX,tileY,color,role"  role: 1=WASD+空格, 2=方向键+数字0
 */
public class PlayerObj extends ElementObj {
    // 子像素坐标（ElementObj.x/y 为 int，供碰撞/绘制；此处保留 float 做平滑移动）
    protected float fx;
    protected float fy;

    protected int direction;       // 0=下 1=左 2=右 3=上
    protected int lives;
    protected float speed;
    protected int bombPower;
    protected int maxBombs;
    protected int currentBombs;
    protected boolean invincible;
    protected float invincibleTimer;

    protected String color;
    protected int role;

    private final Set<Integer> held = ConcurrentHashMap.newKeySet();
    private volatile boolean placeBombRequest;

    public PlayerObj() {
        super();
    }

    @Override
    public void keyClick(boolean pressed, int key) {
        if (isMyMoveKey(key)) {
            if (pressed) held.add(key); else held.remove(key);
        } else if (pressed && isMyBombKey(key)) {
            placeBombRequest = true;
        }
    }

    private boolean isMyMoveKey(int key) {
        if (role == 1) {
            return key == KeyEvent.VK_W || key == KeyEvent.VK_S || key == KeyEvent.VK_A || key == KeyEvent.VK_D;
        }
        return key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN || key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT;
    }

    private boolean isMyBombKey(int key) {
        if (role == 1) return key == KeyEvent.VK_SPACE;
        return key == KeyEvent.VK_NUMPAD0 || key == KeyEvent.VK_0;
    }

    @Override
    protected void move() {
        float dt = GameThread.dt;
        if (invincible) {
            invincibleTimer -= dt;
            if (invincibleTimer <= 0) invincible = false;
        }
        if (!isAlive()) return;

        float dx = 0, dy = 0;
        if (role == 1) {
            if (held.contains(KeyEvent.VK_W)) { dy = -1; direction = 3; }
            if (held.contains(KeyEvent.VK_S)) { dy = 1;  direction = 0; }
            if (held.contains(KeyEvent.VK_A)) { dx = -1; direction = 1; }
            if (held.contains(KeyEvent.VK_D)) { dx = 1;  direction = 2; }
        } else {
            if (held.contains(KeyEvent.VK_UP))    { dy = -1; direction = 3; }
            if (held.contains(KeyEvent.VK_DOWN))  { dy = 1;  direction = 0; }
            if (held.contains(KeyEvent.VK_LEFT))  { dx = -1; direction = 1; }
            if (held.contains(KeyEvent.VK_RIGHT)) { dx = 1;  direction = 2; }
        }
        if (dx != 0 && dy != 0) { dx *= 0.707f; dy *= 0.707f; }

        float step = speed * dt;
        if (dx != 0) {
            float amt = dx * step;
            if (!CollisionController.checkPlayerMapCollision(this, amt, 0)
                    && !CollisionController.checkPlayerBombCollision(this, amt, 0)) {
                fx += amt;
            }
        }
        if (dy != 0) {
            float amt = dy * step;
            if (!CollisionController.checkPlayerMapCollision(this, 0, amt)
                    && !CollisionController.checkPlayerBombCollision(this, 0, amt)) {
                fy += amt;
            }
        }
        setX(Math.round(fx));
        setY(Math.round(fy));
    }

    /** 模板 add 步骤：消费放弹请求，生成炸弹 */
    @Override
    protected void add(long gameTime) {
        if (!placeBombRequest) return;
        placeBombRequest = false;
        if (!isAlive() || currentBombs >= maxBombs) return;

        int tx = Math.round(fx / Config.TILE_SIZE);
        int ty = Math.round(fy / Config.TILE_SIZE);
        ElementObj proto = GameLoad.getObj("bomb");
        if (proto instanceof BombObj) {
            BombObj bomb = (BombObj) proto.createElement(tx + "," + ty + "," + bombPower);
            bomb.setOwner(this);
            ElementManager.getManager().addElement(bomb, GameElement.BOMB);
            currentBombs++;
            AudioManager.getInstance().playSound("bomb");
        }
    }

    @Override
    public void showElement(Graphics g) {
        if (!isAlive()) return;
        Graphics2D g2 = (Graphics2D) g;
        int x = getX();
        int y = getY();
        int size = Config.TILE_SIZE;
        float imgSize = size * 0.9f;
        float off = (size - imgSize) / 2f;

        ImageIcon shadow = ResourceManager.getInstance().getShadowImage();
        if (shadow != null) {
            g2.drawImage(shadow.getImage(), (int) (x + off), (int) (y + off + imgSize * 0.75f),
                    (int) imgSize, (int) (imgSize * 0.25f), null);
        }

        List<ImageIcon> sprites = ResourceManager.getInstance().getPlayerSprites(color);
        int faceIdx;
        boolean flip = false;
        switch (direction) {
            case 3: faceIdx = 5; break;
            case 1: faceIdx = 1; flip = true; break;
            case 2: faceIdx = 1; break;
            default: faceIdx = 8; break;
        }
        ImageIcon sprite = (sprites != null && sprites.size() > faceIdx) ? sprites.get(faceIdx) : null;

        Composite old = g2.getComposite();
        if (invincible && (System.currentTimeMillis() / 100) % 2 == 0) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        }
        if (sprite != null) {
            if (flip) {
                g2.drawImage(sprite.getImage(), (int) (x + off + imgSize), (int) (y + off), (int) -imgSize, (int) imgSize, null);
            } else {
                g2.drawImage(sprite.getImage(), (int) (x + off), (int) (y + off), (int) imgSize, (int) imgSize, null);
            }
        } else {
            Color c = "blue".equals(color) ? Color.decode("#4fc3f7") : Color.decode("#f44336");
            g2.setColor(c);
            g2.fillOval((int) (x + off), (int) (y + off), (int) imgSize, (int) imgSize);
            g2.setColor(Color.BLACK);
            g2.fillOval((int) (x + size * 0.35f), (int) (y + size * 0.35f), 5, 5);
            g2.fillOval((int) (x + size * 0.55f), (int) (y + size * 0.35f), 5, 5);
        }
        g2.setComposite(old);
    }

    public void takeDamage() {
        if (invincible) return;
        lives--;
        if (lives > 0) {
            invincible = true;
            invincibleTimer = Config.INVINCIBLE_TIME;
        }
    }

    public void collectPowerUp(PowerUpObj pu) {
        switch (pu.getType()) {
            case SPEED:       speed *= 1.25f; break;
            case BOMB_COUNT:  maxBombs++; break;
            case BOMB_POWER:  bombPower++; break;
        }
    }

    public void onBombExploded() {
        if (currentBombs > 0) currentBombs--;
    }

    /** AI 放置炸弹时计数+1 */
    public void onBombPlaced() { currentBombs++; }
    public int getCurrentBombs() { return currentBombs; }

    public boolean isAlive() { return lives > 0; }

    @Override
    public ElementObj createElement(String str) {
        String[] a = str.split(",");
        int tx = Integer.parseInt(a[0]);
        int ty = Integer.parseInt(a[1]);
        this.color = a[2];
        this.role = Integer.parseInt(a[3]);
        this.fx = tx * Config.TILE_SIZE;
        this.fy = ty * Config.TILE_SIZE;
        setX(tx * Config.TILE_SIZE);
        setY(ty * Config.TILE_SIZE);
        setW(Config.TILE_SIZE);
        setH(Config.TILE_SIZE);

        this.direction = 0;
        this.lives = Config.INITIAL_LIVES;
        this.speed = Config.PLAYER_SPEED;
        this.bombPower = Config.INITIAL_POWER;
        this.maxBombs = Config.INITIAL_BOMBS;
        this.currentBombs = 0;
        this.invincible = false;
        this.invincibleTimer = 0;
        this.held.clear();
        this.placeBombRequest = false;
        return this;
    }

    public float getFx() { return fx; }
    public float getFy() { return fy; }
    public void setFx(float fx) { this.fx = fx; setX(Math.round(fx)); }
    public void setFy(float fy) { this.fy = fy; setY(Math.round(fy)); }
    public int getLives() { return lives; }
    public float getSpeed() { return speed; }
    public int getBombPower() { return bombPower; }
    public int getMaxBombs() { return maxBombs; }
    public int getDirection() { return direction; }
    public void setDirection(int d) { this.direction = d; }
    public int getRole() { return role; }
    public String getColor() { return color; }
    public boolean isInvincible() { return invincible; }
}
