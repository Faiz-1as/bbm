package com.bomberman.manager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.bomberman.element.ElementObj;

/**
 * 元素管理器：单例。集中存储所有游戏元素，供视图(绘制)与控制器(逻辑)统一访问。
 *
 * 数据结构：Map<GameElement, List<ElementObj>>，以枚举分类存储。
 * 使用 CopyOnWriteArrayList 保证「逻辑线程写、EDT 读」的并发安全。
 *
 * 对标参考框架 com.tedu.manager.ElementManager
 */
public class ElementManager {
    private Map<GameElement, List<ElementObj>> gameElements;

    /** 返回全量元素集合（视图据此按枚举顺序绘制） */
    public Map<GameElement, List<ElementObj>> getGameElements() {
        return gameElements;
    }

    /** 添加元素到指定分类（多由 GameLoad / 元素自身 add 调用） */
    public void addElement(ElementObj obj, GameElement ge) {
        gameElements.get(ge).add(obj);
    }

    /** 依据分类取出该类元素列表 */
    public List<ElementObj> getElementsByKey(GameElement ge) {
        return gameElements.get(ge);
    }

    /** 清空全部元素（关卡切换/重开时调用） */
    public synchronized void clearAll() {
        for (GameElement ge : GameElement.values()) {
            gameElements.get(ge).clear();
        }
    }

    // ============== 单例 ==============
    private static ElementManager EM = null;

    public static synchronized ElementManager getManager() {
        if (EM == null) {
            EM = new ElementManager();
        }
        return EM;
    }

    private ElementManager() {
        init();
    }

    /** 实例化：为每个枚举分类创建一个并发安全的列表 */
    public void init() {
        gameElements = new ConcurrentHashMap<GameElement, List<ElementObj>>();
        for (GameElement ge : GameElement.values()) {
            gameElements.put(ge, new CopyOnWriteArrayList<ElementObj>());
        }
    }
}
