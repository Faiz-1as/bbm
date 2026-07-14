package com.bomberman.element;

import com.bomberman.controller.AIController;
import com.bomberman.controller.GameThread;
import com.bomberman.enums.AIState;
import com.bomberman.util.Config;

/**
 * AI 对手元素。继承 PlayerObj，重写 move() 委托 AIController 做决策与逐格移动；
 * 重写 takeDamage() 在死亡时置 live=false（由 moveAndUpdate 移除并计分）。
 *
 * createElement 格式："tileX,tileY"（颜色固定 green，role=0 不响应键盘）
 */
public class AIPlayerObj extends PlayerObj {
    private AIState state = AIState.IDLE;
    /** 当前前往的目标格；targetTileX<0 表示无目标，需重选 */
    private int targetTileX = -1;
    private int targetTileY = -1;

    public AIPlayerObj() {
        super();
    }

    @Override
    protected void move() {
        float dt = GameThread.dt;
        if (invincible) {
            invincibleTimer -= dt;
            if (invincibleTimer <= 0) invincible = false;
        }
        if (!isAlive()) return;
        AIController.getInstance().updateAI(this, dt);
    }

    @Override
    public void takeDamage() {
        super.takeDamage();
        if (!isAlive()) {
            setLive(false); // 死亡：标记移除（moveAndUpdate 调 die 并从 ENEMY 移除）
        }
    }

    @Override
    public ElementObj createElement(String str) {
        String[] a = str.split(",");
        int tx = Integer.parseInt(a[0]);
        int ty = Integer.parseInt(a[1]);
        super.createElement(tx + "," + ty + ",green,0");
        this.state = AIState.IDLE;
        this.targetTileX = -1;
        this.targetTileY = -1;
        return this;
    }

    public AIState getState() { return state; }
    public void setState(AIState state) { this.state = state; }

    public boolean hasTarget() { return targetTileX >= 0; }
    public int getTargetTileX() { return targetTileX; }
    public int getTargetTileY() { return targetTileY; }
    public void setTarget(int x, int y) { this.targetTileX = x; this.targetTileY = y; }
    public void clearTarget() { this.targetTileX = -1; this.targetTileY = -1; }
}
