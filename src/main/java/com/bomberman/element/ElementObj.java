package com.bomberman.element;

import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.ImageIcon;

/**
 * 所有游戏元素的抽象基类。
 *
 * 设计要点（对标参考框架 com.tedu.element.ElementObj）：
 * 1. 持有元素的通用属性：坐标 x,y、宽高 w,h、图片 icon、生存状态 live。
 * 2. 模板方法 model(gameTime)：定义元素每帧执行顺序——换装 -> 移动 -> 生成(发射)。
 *    子类按需重写 updateImage/move/add，由 model 统一调度（模板模式）。
 * 3. showElement(g)：每个元素自绘，视图层只需按枚举顺序遍历调用。
 * 4. keyClick：约定需要响应键盘的子类重写，由 GameListener 统一转发。
 * 5. die：死亡钩子，可做死亡动画/掉装备。
 * 6. getRectangle/pk：基于矩形相交的碰撞判定。
 * 7. createElement：工厂方法，由子类解析字符串数据并返回 this，配合 GameLoad 反射工厂使用。
 */
public abstract class ElementObj {

    private int x;
    private int y;
    private int w;
    private int h;
    private ImageIcon icon;
    /** 生存状态：true 存在，false 死亡（将被管理器移除） */
    private boolean live = true;

    public ElementObj() {
        // 供反射工厂newInstance()使用，子类继承不报错
    }

    public ElementObj(int x, int y, int w, int h, ImageIcon icon) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.icon = icon;
    }

    /** 显示元素：每个子类实现自己的绘制 */
    public abstract void showElement(Graphics g);

    /**
     * 键盘事件：bl=true 按下，false 松开；key 为 KeyEvent code。
     * 需要响应键盘的子类重写本方法。
     */
    public void keyClick(boolean bl, int key) {
    }

    /** 移动方法：需要移动的子类重写 */
    protected void move() {
    }

    /** 换装/动画：需要动画的子类重写 */
    protected void updateImage(long time) {
    }

    /** 生成新元素（如玩家放炸弹、敌人发射）：需要生成的子类重写 */
    protected void add(long gameTime) {
    }

    /**
     * 模板方法：定义元素每帧执行顺序。final 锁定流程，子类只能重写各步骤。
     */
    public final void model(long gameTime) {
        updateImage(gameTime);
        move();
        add(gameTime);
    }

    /** 死亡钩子 */
    public void die() {
    }

    /**
     * 工厂方法：子类解析字符串(如 "x,y,color")填充自身属性并返回 this。
     * 配合 GameLoad.getObj(key) 反射创建实例后调用。
     */
    public ElementObj createElement(String str) {
        return null;
    }

    /** 实时碰撞矩形 */
    public Rectangle getRectangle() {
        return new Rectangle(x, y, w, h);
    }

    /** 矩形相交碰撞：true 表示发生碰撞 */
    public boolean pk(ElementObj obj) {
        return this.getRectangle().intersects(obj.getRectangle());
    }

    // ========== getter/setter ==========
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getW() { return w; }
    public void setW(int w) { this.w = w; }
    public int getH() { return h; }
    public void setH(int h) { this.h = h; }
    public ImageIcon getIcon() { return icon; }
    public void setIcon(ImageIcon icon) { this.icon = icon; }
    public boolean isLive() { return live; }
    public void setLive(boolean live) { this.live = live; }
}
