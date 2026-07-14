# 实现计划

## 阶段一：项目基础框架与 Util

**目标**: 搭建项目结构，实现配置和工具类

- [ ] 创建 Maven/Gradle 项目结构（或简单目录结构）
- [ ] 实现 `Config.java` - 配置常量
- [ ] 实现 `Vector2.java` - 二维向量（可选，直接用x/y也可以）

## 阶段二：Core 层 - 基础设施

**目标**: 实现游戏循环、输入管理、场景系统

- [ ] 实现 `GameLoop.java` - 封装 AnimationTimer
- [ ] 实现 `InputManager.java` - 单例输入管理
- [ ] 实现 `Scene.java` - 场景抽象基类
- [ ] 实现 `SceneManager.java` - 场景切换管理
- [ ] 实现 `AudioManager.java` - 音效管理（可选，后期再加）

## 阶段三：Model 层 - 数据类

**目标**: 实现所有游戏数据模型

- [ ] 实现 `GameState.java` - 状态枚举
- [ ] 实现 `TileType.java` - 地块类型枚举
- [ ] 实现 `Tile.java` - 地块类
- [ ] 实现 `MapData.java` - 地图数据（含硬编码默认地图）
- [ ] 实现 `PowerUpType.java` - 道具类型枚举
- [ ] 实现 `PowerUp.java` - 道具类
- [ ] 实现 `Bomb.java` - 炸弹类
- [ ] 实现 `Explosion.java` - 爆炸类
- [ ] 实现 `Player.java` - 玩家类
- [ ] 实现 `AIState.java` - AI状态枚举
- [ ] 实现 `AIPlayer.java` - AI玩家类
- [ ] 实现 `GameModel.java` - 游戏总控

## 阶段四：View 层 - 界面

**目标**: 实现菜单、游戏画面、结束界面

- [ ] 实现 `MenuView.java` - 开始菜单
- [ ] 实现 `EndView.java` - 结束界面
- [ ] 实现 `GameCanvas.java` - 游戏画面绘制（先画简单方块）

## 阶段五：Controller 层 - 核心逻辑

**目标**: 实现游戏主逻辑和碰撞检测

- [ ] 实现 `CollisionController.java` - 碰撞检测
- [ ] 实现 `GameController.java` - 游戏主逻辑（不含AI）
- [ ] 集成 Model + View + Controller，实现基本可玩版本

## 阶段六：AI 逻辑

**目标**: 实现简单的AI对手

- [ ] 实现 `AIController.java` - AI行为逻辑
- [ ] 简单寻路（BFS或贪心）
- [ ] 危险检测和躲避
- [ ] 集成到 GameController

## 阶段七：完整功能

**目标**: 添加所有剩余功能

- [ ] 道具系统
- [ ] 分数系统
- [ ] 游戏时间倒计时
- [ ] 暂停功能
- [ ] 胜负判定
- [ ] 音效（可选）

## 阶段八：Polish

**目标**: 优化和完善

- [ ] 代码清理，添加必要注释
- [ ] 测试所有功能
- [ ] 调整数值平衡（速度、炸弹时间等）

---

## 依赖关系

```
阶段一 → 阶段二 → 阶段三 → 阶段四
                          ↓
                       阶段五
                          ↓
                       阶段六
                          ↓
                       阶段七
                          ↓
                       阶段八
```
