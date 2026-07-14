# 泡泡堂游戏设计文档

## 概述

- **项目名称**: Bomberman
- **技术栈**: JDK 8 + JavaFX
- **架构模式**: MVC
- **设计目标**: 实现完整单机版泡泡堂游戏，同时构建可复用的游戏基础设施

---

## 目录结构

```
src/main/java/com/bomberman/
├── Launcher.java                      # 程序入口
├── core/                              # 基础设施（通用）
│   ├── GameLoop.java                  # 游戏循环（AnimationTimer 封装）
│   ├── InputManager.java            # 键盘输入管理
│   ├── Scene.java                    # 场景抽象
│   ├── SceneManager.java              # 场景切换管理
│   └── AudioManager.java              # 音效管理
├── model/                             # Model 层 - 数据
│   ├── GameModel.java                # 游戏状态总控
│   ├── GameState.java                # 游戏状态枚举
│   ├── Player.java                    # 玩家数据
│   ├── AIPlayer.java                # AI对手
│   ├── Bomb.java                      # 炸弹数据
│   ├── Explosion.java                 # 爆炸效果
│   ├── PowerUp.java                   # 道具
│   ├── MapData.java                   # 地图数据
│   └── Tile.java                      # 地图格子
├── view/                              # View 层 - 渲染
│   ├── GameCanvas.java               # 游戏画面绘制
│   ├── MenuView.java                 # 菜单界面
│   └── EndView.java                  # 结束界面
├── controller/                        # Controller 层 - 逻辑
│   ├── GameController.java           # 核心游戏逻辑
│   ├── CollisionController.java      # 碰撞检测
│   └── AIController.java             # AI 逻辑
└── util/                              # 工具类
    ├── Config.java                     # 配置常量
    └── Vector2.java                    # 二维向量

resources/
├── maps/                              # 地图配置文件
└── sounds/                            # 音效文件
```

---

## 核心设计

### 架构模式：MVC

- **Model**: 游戏数据和状态，不依赖 View 和 Controller
- **View**: 负责渲染，只读 Model
- **Controller**: 处理输入和游戏逻辑，更新 Model

### 游戏循环

GameLoop 封装 JavaFX AnimationTimer，每帧执行：

1. **输入处理** - 读取键盘状态
2. **逻辑更新** - 按固定时间步长更新游戏状态
3. **渲染绘制** - 根据当前 Model 绘制画面

目标帧率：60 FPS

---

## Core 层设计

### GameLoop

```java
public abstract class GameLoop extends AnimationTimer {
    private boolean isRunning;
    private long lastTime;

    public void start();
    public void stop();

    // 子类实现
    protected abstract void update(float deltaTime);
    protected abstract void render();
}
```

### InputManager

单例模式，管理键盘输入状态。

```java
public class InputManager {
    private static InputManager instance;
    private boolean[] keys;

    public static InputManager getInstance();
    public boolean isKeyPressed(KeyCode code);
    public void setKeyPressed(KeyCode code, boolean pressed);
}
```

### Scene & SceneManager

场景抽象，用于菜单/游戏/结束画面的切换。

```java
public abstract class Scene {
    public abstract void update(float dt);
    public abstract void render(GraphicsContext gc);
    public abstract void onEnter();
    public abstract void onExit();
}

public class SceneManager {
    private Scene currentScene;
    private Map<String, Scene> scenes;

    public void addScene(String name, Scene scene);
    public void switchTo(String name);
}
```

---

## Model 层设计

### GameModel

游戏状态总控，包含所有游戏数据。

```java
public class GameModel {
    private GameState gameState;
    private Player player;
    private List<AIPlayer> aiPlayers;
    private List<Bomb> bombs;
    private List<Explosion> explosions;
    private List<PowerUp> powerUps;
    private MapData mapData;
    private int score;
    private int timeLeft;

    public void resetGame();
}
```

### GameState (enum)

```java
public enum GameState {
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER,
    VICTORY
}
```

### Player

玩家数据。

```java
public class Player {
    private float x, y;
    private int lives;
    private float speed;
    private int bombPower;
    private int maxBombs;
    private int currentBombs;
    private boolean canWalkThroughWalls;
    private boolean invincible;
    private float invincibleTimer;

    public void move(float dx, float dy, float dt, MapData map);
    public Bomb placeBomb();
    public void takeDamage();
    public void collectPowerUp(PowerUp powerUp);
}
```

### AIPlayer extends Player

AI 对手数据。

```java
public class AIPlayer extends Player {
    private int targetX, targetY;
    private float moveTimer;
    private AIState state;
}

public enum AIState {
    IDLE,
    SEEKING,
    FLEEING,
    PLACING_BOMB
}
```

### Bomb

炸弹数据。

```java
public class Bomb {
    private int x, y;           // 格子坐标
    private float timer;         // 爆炸倒计时
    private int power;          // 爆炸范围
    private Player owner;

    public boolean tick(float dt);  // 返回是否爆炸
}
```

### Explosion

爆炸效果数据。

```java
public class Explosion {
    private int x, y;
    private Direction direction;  // CENTER, UP, DOWN, LEFT, RIGHT
    private float duration;
    private boolean isCenter;
}
```

### PowerUp

道具数据。

```java
public class PowerUp {
    private int x, y;
    private PowerUpType type;
}

public enum PowerUpType {
    SPEED,           // 速度提升
    BOMB_COUNT,       // 炸弹数+1
    BOMB_POWER,      // 威力提升
    WALL_PASS        // 穿墙
}
```

### MapData & Tile

地图数据。

```java
public class MapData {
    public static final int WIDTH = 15;
    public static final int HEIGHT = 13;
    private Tile[][] tiles;

    public Tile getTile(int x, int y);
    public boolean isWalkable(int x, int y);
    public boolean destroyTile(int x, int y);  // 返回是否生成道具
}

public class Tile {
    private int x, y;
    private TileType type;
}

public enum TileType {
    EMPTY,    // 空地
    WALL,     // 石墙（不可破坏）
    BRICK      // 砖块（可破坏）
}
```

---

## View 层设计

### GameCanvas

游戏主画面绘制。

```java
public class GameCanvas {
    private Canvas canvas;
    private GraphicsContext gc;
    private static final int TILE_SIZE = 40;

    public void render(GameModel model);
    private void drawMap(MapData mapData);
    private void drawPlayer(Player player, Color color);
    private void drawBomb(Bomb bomb);
    private void drawExplosion(Explosion explosion);
    private void drawPowerUp(PowerUp powerUp);
    private void drawUI(GameModel model);
}
```

### MenuView

开始菜单界面。

```java
public class MenuView {
    private VBox root;
    private Text titleText;
    private Button startButton;
    private ComboBox<String> mapSelector;

    public Parent getRoot();
    public void setOnStartHandler(EventHandler<ActionEvent> handler);
}
```

### EndView

结束/胜利界面。

```java
public class EndView {
    private VBox root;
    private Text resultText;
    private Text scoreText;
    private Button restartButton;
    private Button menuButton;

    public void showVictory(int score);
    public void showGameOver(int score);
}
```

---

## Controller 层设计

### GameController

核心游戏逻辑。

```java
public class GameController {
    private GameModel model;
    private CollisionController collisionCtrl;
    private AIController aiCtrl;

    public GameController(GameModel model);

    public void initGame(String mapName);
    public void update(float deltaTime);
    private void handleInput();
    private void updatePlayerMovement(float dt);
    private void updateBombs(float dt);
    private void updateExplosions(float dt);
    private void processExplosions();
    private void checkCollisions();
    private void checkWinCondition();
}
```

### CollisionController

碰撞检测。

```java
public class CollisionController {
    public boolean checkPlayerMapCollision(Player player, float dx, float dy, MapData map);
    public boolean checkPlayerExplosionCollision(Player player, List<Explosion> explosions);
    public PowerUp checkPlayerPowerUpCollision(Player player, List<PowerUp> powerUps);
    public List<Point> getBombExplosionRange(Bomb bomb, MapData map);
}
```

### AIController

AI 行为控制。

```java
public class AIController {
    public void updateAI(AIPlayer ai, GameModel model, float dt);
    private List<Point> findPath(Point start, Point target, MapData map);
    private boolean isPositionDangerous(Point pos, GameModel model);
    private Point findSafeTile(AIPlayer ai, GameModel model);
    private boolean shouldPlaceBomb(AIPlayer ai, GameModel model);
}
```

AI 简单寻路使用贪心算法或 BFS。

---

## 游戏流程

### 一帧的处理顺序

```
1. handleInput()
   - 读取 InputManager
   - 更新玩家位置
   - 检测放炸弹

2. updateAI()
   - AI 决策
   - AI 移动

3. updateBombs()
   - 炸弹倒计时
   - 爆炸时创建 Explosion

4. processExplosions()
   - 计算爆炸范围
   - 破坏砖块
   - 生成道具
   - 伤害检测

5. updateExplosions()
   - 更新爆炸效果时间

6. checkCollisions()
   - 玩家 & 爆炸 → 受伤
   - 玩家 & 道具 → 拾取

7. checkWinCondition()
   - 检查是否胜利或失败
```

### 场景切换流程

```
Launcher 启动
    ↓
初始化 SceneManager，添加 MenuScene
    ↓
[用户点击开始]
    ↓
GameController.initGame()
GameModel.resetGame()
SceneManager.switchTo("game")
    ↓
游戏循环运行
    ↓
[游戏结束/胜利]
    ↓
GameModel.gameState = GAME_OVER/VICTORY
EndView.showVictory/GameOver()
SceneManager.switchTo("end")
```

---

## 游戏设计

### 地图规格

- 大小：15 × 13 格
- 每格：40 × 40 像素
- 画布尺寸：600 × 520 像素

### 玩家初始属性

- 生命：3
- 速度：1 格/秒
- 最大炸弹数：1
- 炸弹威力：2 格

### 道具说明

| 道具 | 效果 | 图标 |
|-----|------|-----|
| SPEED | 移动速度 +25% | ⚡ |
| BOMB_COUNT | 可放置炸弹数 +1 | 💣 |
| BOMB_POWER | 爆炸范围 +1 格 | 💪 |
| WALL_PASS | 可穿过砖块（10秒） | 👻 |

### 分数规则

- 破坏砖块：10 分
- 击败 AI：100 分
- 拾取道具：20 分
- 胜利奖励：500 分

### 操作按键

| 按键 | 功能 |
|-----|------|
| ↑ ↓ ← → / W A S D | 移动 |
| 空格 | 放炸弹 |
| P / ESC | 暂停/继续 |

---

## 配置常量

```java
public class Config {
    public static final int TILE_SIZE = 40;
    public static final int MAP_WIDTH = 15;
    public static final int MAP_HEIGHT = 13;

    public static final float PLAYER_SPEED = 3.0f;
    public static final float BOMB_TIMER = 3.0f;
    public static final float EXPLOSION_DURATION = 0.5f;
    public static final float INVINCIBLE_TIME = 2.0f;

    public static final int GAME_TIME = 180; // seconds

    public static final int INITIAL_LIVES = 3;
    public static final int INITIAL_BOMBS = 1;
    public static final int INITIAL_POWER = 2;
}
```

---

## 实现阶段

按以下顺序实现：

1. **Core 层** - GameLoop, InputManager, Scene 基础
2. **Model 层** - 所有数据类
3. **View 层** - 菜单、游戏画面
4. **Controller 层** - 游戏逻辑、碰撞
5. **AI 逻辑** - 简单 AI 行为
6. **音效与 polish**
