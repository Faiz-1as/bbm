# 元素类注册表 (key -> 全限定类名)
# GameLoad.getObj(key) 通过反射实例化，配合 createElement(str) 工厂方法（对标参考 obj.pro）
play=com.bomberman.element.PlayerObj
enemy=com.bomberman.element.AIPlayerObj
bomb=com.bomberman.element.BombObj
explosion=com.bomberman.element.ExplosionObj
powerup=com.bomberman.element.PowerUpObj
maptile=com.bomberman.element.MapTileObj
