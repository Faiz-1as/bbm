# Bomberman → Swing 框架改造计划

参考项目 `hnsfGameFram(UTF8)`（华南师范教学框架，Swing/AWT，com.tedu 分层）的**目录结构、命名规则、类的抽象与管理写法**，对当前 Bomberman 进行改造。

## 目标
- **完全移植到 Swing/AWT**（丢弃 JavaFX）：`JFrame`/`JPanel`/`ImageIcon`/`Graphics`/`Thread`/`KeyListener`。
- **落地参考框架全部抽象**：`ElementObj` 抽象基类 + 模板方法 `model()`；`ElementManager` 单例（`Map<GameElement,List<ElementObj>>`）；`GameElement` 枚举；`GameLoad`（`.pro`/`.map` + 反射工厂）；`GameThread`（load→run→over）；`GameListener`（委托 PLAY 元素）；`GameJFrame`（setter 注入）；`GameMainJPanel`（Runnable，枚举遍历绘制）；`GameStart`（装配入口）。
- **保留全部现有玩法**：WASD/方向键移动、空格/数字0放炸弹、P/ESC 暂停、炸弹倒计时与链爆、砖块破坏与道具掉落(30%)、3 类道具、无敌与生命、BFS AI（SEEK/FLEE/PLACE）、3 张主题地图、得分/倒计时/胜负判定、单人+双人、菜单/结束/HUD、BGM+SFX。

---

## 目标目录结构

```
src/main/java/com/bomberman/
├── game/
│   └── GameStart.java            # main：实例化 + setter 注入 + start()（取代 Launcher 的装配职责）
├── show/
│   ├── GameJFrame.java           # JFrame 窗体：init/setter 注入(jPanel/keyListener/thead)/start()
│   ├── GameMainJPanel.java       # JPanel implements Runnable：paint(g) 枚举遍历 em→showElement；run() repaint 循环
│   ├── MenuPanel.java            # 取代 MenuView：JButton/JComboBox 选地图/模式
│   ├── EndPanel.java             # 取代 EndView：胜利/失败/对战结果 + 再来一局/返回菜单
│   └── HudPanel.java             # 取代 HudPanel：JPanel 右侧 HUD（用 Timer 刷新）
├── controller/
│   ├── GameThread.java           # extends Thread：run()=gameLoad→gameRun→gameOver；moveAndUpdate；ElementPK 碰撞
│   ├── GameListener.java         # implements KeyListener：按键→PLAY 元素 keyClick()；含防重复 Set
│   ├── CollisionController.java  # 适配 ElementObj/ElementManager（保留 getBombExplosionRange 等）
│   └── AIController.java         # 适配 AIPlayerObj + ElementManager（保留 BFS/dangerMap/escape）
├── manager/
│   ├── ElementManager.java       # 单例：Map<GameElement,List<ElementObj>>；addElement/getElementsByKey/getGameElements（同步）
│   ├── GameElement.java          # 枚举：MAPS,PLAY,ENEMY,BOMB,EXPLOSION,POWERUP（声明更新/绘制顺序）
│   ├── GameLoad.java             # static：loadImg(GameData.pro)、loadObj(obj.pro 反射)、MapLoad(N.map)、loadPlay、getObj
│   └── GameContext.java          # 单例：标量状态 score/timeLeft/gameState/twoPlayer/winner/quitToMenu/mapName
├── element/
│   ├── ElementObj.java           # 抽象基类：x,y,w,h,icon,live；model()=updateImage→move→add；showElement/keyClick/die/pk/createElement
│   ├── PlayerObj.java            # 取代 Player；float fx,fy 内部子像素，同步到 int x,y
│   ├── AIPlayerObj.java          # 取代 AIPlayer；extends PlayerObj；+state/target
│   ├── BombObj.java              # 取代 Bomb；tick(dt)；die() 触发链爆
│   ├── ExplosionObj.java         # 取代 Explosion；duration 衰减；die() 移除
│   ├── PowerUpObj.java           # 取代 PowerUp
│   └── MapTileObj.java           # 取代 Tile；type；showElement 按 type 画地板/墙/砖
├── enums/
│   ├── GameState.java  TileType.java  PowerUpType.java  AIState.java   # 沿用
├── element/MapData.java          # 格阵 + isWalkable/destroyTile；由 GameLoad.MapLoad 填充
└── util/
    ├── Config.java               # 沿用常量
    ├── ResourceManager.java      # 取代 ResourceLoader 的图片部分：ImageIcon 缓存 + 主题帧
    └── AudioManager.java         # javax.sound.sampled.Clip：BGM 循环 + SFX（需 wav）

resources/
├── image/...                     # 沿用现有图片
├── sounds/                       # mp3→wav（见音频）
└── text/                         # 新增，对标 com/tedu/text
    ├── GameData.pro              # 图片 key→classpath 路径（对标 GameData.pro）
    ├── obj.pro                   # 元素 key→全限定类名（反射工厂，对标 obj.pro）
    └── 1.map 2.map 3.map         # 地图结构（WALL 坐标），对标 N.map
```

**删除**：`core/`（GameLoop/Scene/SceneManager/InputManager 职责被 GameThread/GameJFrame/GameListener 取代）、`Launcher.java`、`util/ResourceLoader.java`（拆为 ResourceManager+AudioManager）、`view/`（JavaFX 视图整体替换为 `show/`）。

---

## 资源与配置

### `text/GameData.pro`（图片注册，key→classpath 路径）
```
player_blue=image/player_blue/26.png
player_blue_run=image/player_blue/28.png
... (按 ResourceManager 实际需要的 key 列出；帧序列用 imgMaps<String,List<ImageIcon>>)
wall=image/barriers/92.png
brick=image/barriers/100.png
tile=image/tiles/212.png
bomb=image/boom/328.png
explode_blue=image/explode/blue/254.png
...
```
对标参考 `GameData.pro`（`paopao=image/...`）。`GameLoad.loadImg()` 读取后填 `imgMap`/`imgMaps`，供 `createElement` 按名取图。

### `text/obj.pro`（元素反射注册，key→类名）
```
play=com.bomberman.element.PlayerObj
enemy=com.bomberman.element.AIPlayerObj
bomb=com.bomberman.element.BombObj
explosion=com.bomberman.element.ExplosionObj
powerup=com.bomberman.element.PowerUpObj
maptile=com.bomberman.element.MapTileObj
```
`GameLoad.loadObj()` 反射 `Class.forName` 存入 `objMap`；`getObj(key)` 用 `getDeclaredConstructor().newInstance()` 造实例，再 `createElement(str)` 解析数据。对标参考 `obj.pro` + `getObj`。

### `text/1.map 2.map 3.map`（地图结构）
格式沿用参考 `TYPE=x,y;x,y;...`，但坐标用**格子**而非像素（Bomberman 用格阵）：
```
WALL=0,0;1,0;...;（边界 + 主题柱子）
```
- 边界 + 主题柱子（确定性）写进 `.map`。
- **砖块程序化填充**保留：`MapLoad` 读 WALL 后，按主题密度+安全区规则随机生成 BRICK（参考的静态文件无法表达随机性，故保留代码生成）。
- 主题图片索引（tile/wall/brick imgIndex）+ 砖块密度由 `mapId` 在 `MapLoad` 内小 switch 决定（或 `.map` 加 `THEME=...` 行）。

### 音频（mp3→wav）
`javax.sound.sampled` 不支持 mp3。改造方案：
- 用 `ffmpeg`/`sox`（实现时探测系统是否有）把 `sounds/*.mp3` 转为 `.wav`，放入 `resources/sounds/`。
- `AudioManager`：SFX 用 `Clip`（预加载）；BGM 用循环 `Clip`（或 `SourceDataLine` 流式）。
- 若无转换工具：`AudioManager` 优雅降级（找不到 wav 则跳过并日志），游戏照常运行；在报告/注释中标注需补 wav。

---

## 逐文件迁移映射

| 现有 (JavaFX/MVC) | 新 (Swing/element-manager) | 关键变化 |
|---|---|---|
| `Launcher` | `game/GameStart` + `show/GameJFrame` | 装配职责拆到 GameStart（实例化+setter 注入+start）；窗口到 GameJFrame；场景切换改 GameJFrame 内 `CardLayout` 切 menu/game/end 面板 |
| `core/GameLoop` (AnimationTimer) | `controller/GameThread` (extends Thread) | run()=gameLoad→gameRun→gameOver；moveAndUpdate 枚举遍历调 model()；ElementPK 碰撞 |
| `core/Scene`+`SceneManager`+`InputManager` | 删除 | 场景→CardLayout；输入→GameListener |
| `controller/GameController` (上帝类) | 拆分：`GameThread.gameRun` + 各 `ElementObj.model/move/die` + `CollisionController` + `AIController` | 输入移 GameListener；逐元素更新移 ElementObj；碰撞移 CollisionController；标量状态移 GameContext |
| `controller/CollisionController` | `controller/CollisionController`（适配） | 操作 ElementManager 列表；保留 getBombExplosionRange/Point |
| `controller/AIController` | `controller/AIController`（适配） | 操作 AIPlayerObj + ElementManager；保留 BFS/dangerMap/escape |
| `model/Player` | `element/PlayerObj extends ElementObj` | showElement/move/keyClick/die/placeBomb(add→BOMB)；float fx,fy |
| `model/AIPlayer` | `element/AIPlayerObj extends PlayerObj` | +state/target |
| `model/Bomb` | `element/BombObj extends ElementObj` | tick(dt)；die 触发链爆与范围生成 |
| `model/Explosion` | `element/ExplosionObj extends ElementObj` | duration 衰减；die 移除 |
| `model/PowerUp` | `element/PowerUpObj extends ElementObj` | |
| `model/Tile` | `element/MapTileObj extends ElementObj` | type；showElement 按 type 画 |
| `model/MapData` | `element/MapData` + `GameLoad.MapLoad` | MapLoad 读 .map 填 MAPS；MapData 保留 isWalkable/destroyTile |
| `model/GameModel` (状态容器) | `ElementManager`(元素) + `GameContext`(标量) | 元素进 ElementManager；score/time/state/twoPlayer/winner 进 GameContext |
| `model/enums/*` | `enums/*` | 沿用；新增 `manager/GameElement` |
| `view/GameCanvas` | `show/GameMainJPanel.paint(g)` + 各 `showElement` | 绘制下沉到元素；面板只枚举遍历 |
| `view/MenuView` | `show/MenuPanel` | Swing 控件 |
| `view/EndView` | `show/EndPanel` | Swing 控件 |
| `view/HudPanel` | `show/HudPanel` | Swing + Timer 刷新 |
| `util/Config` | `util/Config` | 沿用 |
| `util/ResourceLoader` | `util/ResourceManager`(图) + `util/AudioManager`(声) | ImageIcon 缓存；Clip 音频 |
| — | `manager/ElementManager`/`GameElement`/`GameLoad`、`element/ElementObj` | 新增核心抽象 |

---

## 关键设计决策

1. **坐标**：`ElementObj` 用 `int x,y,w,h`（像素，对标参考）。`PlayerObj` 内部保留 `float fx,fy` 做子像素移动，每帧同步 `x=(int)fx`；`BombObj/ExplosionObj/PowerUpObj/MapTileObj` 用格坐标，`x=tileX*TILE_SIZE`。碰撞用 `java.awt.Rectangle`（纯数据类，无 Swing 窗体依赖），`pk()` 调 `intersects`，完全对标参考。

2. **双线程**：`GameThread`(逻辑 Thread) + `GameMainJPanel`(渲染 Runnable) + EDT 绘制，对标参考。`ElementManager` 方法 `synchronized`，元素列表用 `Collections.synchronizedList` 或遍历加锁，避免逻辑线程改、EDT 读的竞态（参考教学代码未同步，这里补齐）。

3. **标量状态**：新增 `manager/GameContext` 单例（与 ElementManager 同风格），存 score/timeLeft/gameState/twoPlayer/winner/quitToMenu/mapName，供 GameThread/HUD/EndPanel 共享。

4. **时间**：`GameThread` 用 `System.nanoTime()` 算 `dt`（炸弹倒计时/爆炸时长/无敌/游戏计时/AI 需要）；同时维护 `long gameTime` 帧计数器传入 `model(gameTime)` 做动画（对标参考）。即 `model()` 模板管动画+移动，`gameRun` 额外显式调时基逻辑（如 `bombObj.tick(dt)`）。

5. **模板方法 `model()`**：`final void model(long gameTime){ updateImage(gameTime); move(); add(gameTime); }`，子类按需重写三者。对标参考 `ElementObj.model`。

6. **反射工厂**：`GameLoad.getObj(key)` → `objMap.get(key).getDeclaredConstructor().newInstance()` → `createElement(str)` 返回 this。`loadPlay()` 用 `getObj("play").createElement("tileX,tileY,color")`。对标参考（用现代 `newInstance`，参考的 `class.newInstance()` 已废弃）。

7. **场景切换**：`GameJFrame` 用 `CardLayout` 在 `menu/game/end` 三面板间切；`GameStart` 注入三面板与 GameThread/GameListener。菜单"开始"→ `GameThread` 重置 + `GameLoad.MapLoad` + `loadPlay` → 切 game 面板。

8. **图片加载**：`GameLoad.loadImg()` 读 `GameData.pro`，值作 classpath 路径，`ImageIcon(getClass().getClassLoader().getResource(url))`（比参考的文件系统路径更稳，且与现有 ResourceLoader 一致）。

9. **精灵翻转/动画**：`Graphics.drawImage(img, x+w, y, -w, h, null)` 水平镜像；动画帧用 `updateImage(gameTime)` 切换 `imgx`，对标参考 `PaoPao.updateImage`。

---

## 实施阶段（每阶段后 `mvn -q compile` 验证）

1. **构建骨架**：改 `pom.xml`（删 JavaFX 依赖/插件，加 exec-maven-plugin，mainClass=`com.bomberman.game.GameStart`）；写 `GameStart`/`GameJFrame`/`GameMainJPanel`/`GameThread`/`GameListener`/`ElementManager`/`GameElement`/`ElementObj`/`GameContext` 最小可运行版（空窗体）。删 `core/`、`Launcher`。
2. **资源层**：`GameLoad`(loadImg/loadObj/getObj) + `text/GameData.pro`/`obj.pro` + `ResourceManager`；图片能按 key 取到 ImageIcon。
3. **地图**：`MapTileObj` + `MapData` + `GameLoad.MapLoad` + `text/1.map 2.map 3.map`；`GameMainJPanel.paint` 枚举遍历画出地图与主题。
4. **玩家与输入**：`PlayerObj`(showElement/move/keyClick/die/placeBomb) + `GameListener` 委托 + `CollisionController.checkPlayerMapCollision`；WASD 移动+碰撞。
5. **炸弹与爆炸**：`BombObj`(tick/die) + `ExplosionObj` + `CollisionController.getBombExplosionRange` + 链爆 + 砖块破坏/道具掉落；`GameThread.ElementPK` 处理玩家↔爆炸。
6. **道具**：`PowerUpObj` + 拾取 + 三类效果 + `GameThread.ElementPK` 玩家↔道具。
7. **AI**：`AIPlayerObj` + `AIController`(BFS/dangerMap/escape) + ENEMY 更新。
8. **双人+胜负+计时**：Player2（方向键/数字0）+ `GameContext` 计分/倒计时/胜负（单/双人）+ ESC/P。
9. **界面**：`MenuPanel`(选图/选模式) + `EndPanel`(胜利/失败/对战) + `HudPanel`(实时属性) + `CardLayout` 切换。
10. **音频**：mp3→wav + `AudioManager`(BGM/SFX) + 各触发点接入。
11. **冒烟回归**：编译→运行→逐功能手测（移动/放弹/链爆/道具/AI/双人/暂停/胜负/菜单/音效）。

---

## 验证方式
- `mvn -q compile` 每阶段通过。
- `mvn exec:java`（或 IDE 运行 `GameStart.main`）启动，冒烟测试上述全部玩法。
- 保留 `docs/实作报告.md` 等文档不动；视情况在末尾追加一节说明新架构。

## 风险与权衡
- **音频转换**：依赖系统 ffmpeg/sox；不可用则降级跳过（游戏仍可玩），并标注需补 wav。
- **外观差异**：Swing 菜单/HUD 无法 1:1 复刻 JavaFX CSS 渐变/圆角；保证功能等价、视觉合理。
- **双线程同步**：补 `synchronized`，规避参考教学代码的竞态。
- **改动量大**：分 11 阶段、每阶段编译，确保可回退定位。
