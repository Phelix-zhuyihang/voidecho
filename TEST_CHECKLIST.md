# 🔮 Void Echo 测试清单 + 流程指引

> **JAR 位置:** `.minecraft/versions/1.21.1-Fabric 0.18.4/mods/void_echo-1.0.0.jar`
> **前提:** 开启作弊 (`/gamemode creative` 拿物品, `/gamemode survival` 战斗)

---

## 一、流程图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         VOID ECHO 完整流程                               │
└─────────────────────────────────────────────────────────────────────────┘

  创造模式/新世界
       │
       ▼
  ┌──────────┐    右键阅读    ┌─────────────────┐
  │ 虚空日志  │ ──────────→  │ 10页中英双语指南  │
  │ (自动赠送) │               │ (书+碎片 也可合成) │
  └──────────┘               └─────────────────┘
       │
       ▼
  ┌────────────────┐
  │ 阶段1: 主世界入门 │
  │  寻找遗忘祭坛    │  /locate structure void_echo:forgotten_altar
  │  采集暗晶碎片    │  挖矿/打怪
  │  制作虚空钥匙    │  下界合金碎片+末影珍珠+水晶碎片×3
  └──────┬─────────┘
         │ 右键传送门框架
         ▼
  ┌────────────────┐
  │ 阶段2: 进入虚空  │
  │  终虚渊维度     │  永夜/暗紫迷雾/床无效
  │  探索四群系     │  平原→森林→废土→洞穴
  │  采集+刷怪     │  水晶碎片 + 暗晶莓
  └──────┬─────────┘
         │
         ▼
  ┌────────────────┐
  │ 阶段3: 锻造装备  │
  │  虚空合金锭     │  暗晶块+黑曜石+铁
  │  虚空剑/弓/法杖 │
  │  虚空头盔/胸甲  │
  │  虚空护腿/靴子  │
  └──────┬─────────┘
         │ 全套=免疫虚空伤害+摔落
         ▼
  ┌────────────────┐
  │ 阶段4: Boss 1  │
  │  虚空追猎者    │  虚空平原自然生成 (weight 5)
  │  HP 300×倍率  │  三阶段: 追猎→召唤→狂暴
  │  掉落:虚空之心 │  /summon void_echo:void_stalker ~ ~5 ~
  └──────┬─────────┘
         │ 虚空之心
         ▼
  ┌────────────────┐
  │ 阶段5: 虚空锻炉 │ ──→ 6条升级路线
  │                 │    耐久(4级)│回声→打击/守卫│裂隙→行者/之怒
  └──────┬─────────┘
         │
         ▼
  ┌────────────────┐
  │ 阶段6: Boss 2  │
  │  回声守卫      │  回声圣殿+虚空之心召唤
  │  HP 500×倍率  │  三阶段: 斩击→冲击波→虚空新星
  │  掉落:回声核心 │  /summon void_echo:echo_warden ~ ~5 ~
  └──────┬─────────┘
         │ 回声核心
         ▼
  ┌────────────────┐
  │ 阶段7: 终局装备 │
  │  回声护符      │  虚空维度:永久力量+速度+抗性
  │  回声之书      │  虚空维度:永久夜视
  │  虚空触媒      │  暗晶精灵进化
  └──────┬─────────┘
         │
         ▼
     ★ 通关 ★
```



---

## 二、测试阶段 + 指令

### 🟢 阶段 1：主世界入门（5分钟）

| # | 测试项 | 命令/操作 | ✓ |
|---|--------|-----------|----|
| 1.1 | 获取虚空日志 | 新世界自动获得，或 `/give @s void_echo:void_echo_journal 1` | ☐ |
| 1.2 | 右键阅读日志 | 显示10页中英双语指南 | ☐ |
| 1.3 | 定位遗忘祭坛 | `/locate structure void_echo:forgotten_altar` → 传送到坐标 | ☐ |
| 1.4 | 检查祭坛结构 | 圆形石台+4柱+传送门框架+中心暗晶块 | ☐ |
| 1.5 | 右键碎片读叙事 | 对祭坛上的回声碎片 I 右键 | ☐ |
| 1.6 | 获取材料做钥匙 | 见下方命令 | ☐ |

```bash
# === 阶段1 快速测试命令 ===
/give @s void_echo:void_echo_journal 1
/give @s void_echo:crystal_shard 16
/give @s minecraft:netherite_scrap 4
/give @s minecraft:ender_pearl 4
/give @s minecraft:quartz 32
/locate structure void_echo:forgotten_altar
# → 记下坐标后传送
/tp @s <x> 90 <z>
```

---

### 🟡 阶段 2：传送门 + 维度（5分钟）

| # | 测试项 | 命令/操作 | ✓ |
|---|--------|-----------|----|
| 2.1 | 制作虚空钥匙 | 工作台合成（下界合金配方或石英配方） | ☐ |
| 2.2 | 右键传送门框架 | 手持钥匙右键→音效→传送到终虚渊 | ☐ |
| 2.3 | 确认维度进入 | F3→`dimension: void_echo:voids_end`，天空漆黑 | ☐ |
| 2.4 | 返回主世界 | 右键终虚渊中的传送门框架→回到进入位置 | ☐ |
| 2.5 | 重生锚测试 | 在终虚渊放重生锚+荧石→右键设置重生点→死亡后在锚处重生 | ☐ |
| 2.6 | 床爆炸（无效） | 右键床→爆炸 | ☐ |
| 2.7 | Y<32浓雾 | 飞到Y=32以下→迷雾变浓 | ☐ |

```bash
# === 阶段2 快速测试命令 ===
/give @s void_echo:void_key 1
/locate structure void_echo:forgotten_altar
/tp @s <x> 90 <z>
# → 右键传送门框架 → 进入终虚渊

# 快速进维度（跳过祭坛）
/execute in void_echo:voids_end run tp @s 0 128 0

# 快速往返测试
/give @s void_echo:void_portal_frame 1
# → 在终虚渊放下框架→右键→回主世界

# 重生锚测试
/give @s minecraft:respawn_anchor 1
/give @s minecraft:glowstone 16
```

---

### 🟠 阶段 3：群系探索（10分钟）

| # | 测试项 | 命令/操作 | ✓ |
|---|--------|-----------|----|
| 3.1 | 四群系全部到访 | 依次传送到4个群系 | ☐ |
| 3.2 | F3查看群系名 | F3 中间偏左→biome: `void_echo:xxx` | ☐ |
| 3.3 | 虚空粒子 | 观察暗紫色虚空环境粒子飘浮（0.5%-2%概率） | ☐ |
| 3.4 | 背景音乐 | 在群系中停留→听到`music_voids_end` | ☐ |
| 3.5 | 洞穴氛围音效 | 地下→听到`ambient.cave` | ☐ |
| 3.6 | 水晶矿石生成 | 地下Y=0-64→`void_echo:crystal_ore` | ☐ |
| 3.7 | 虚空草方块 | 群系地表→`void_echo:void_grass_block` | ☐ |
| 3.8 | 暗晶花 | 暗晶森林→`void_echo:crystal_bloom` | ☐ |

```bash
# === 阶段3 快速测试命令 ===
# 虚空平原
/execute in void_echo:voids_end run tp @s 0 128 0
/locate biome void_echo:void_plains

# 暗晶森林
/execute in void_echo:voids_end run tp @s 500 128 0
/locate biome void_echo:crystal_forest

# 虚空废土
/execute in void_echo:voids_end run tp @s 1000 128 0
/locate biome void_echo:void_wastes

# 暗晶洞穴（地下）
/execute in void_echo:voids_end run tp @s 1500 50 0
/locate biome void_echo:crystal_caverns

# 检查矿石
/give @s minecraft:diamond_pickaxe 1
/give @s void_echo:crystal_ore 16
/give @s void_echo:crystal_bloom 8
/give @s void_echo:void_grass_block 8
```

---

### 🔵 阶段 4：怪物 + 掉落（10分钟）

| # | 测试项 | 命令/操作 | ✓ |
|---|--------|-----------|----|
| 4.1 | 虚空蠕虫生成 | 刷怪蛋→遁地→闪现到玩家附近→造成伤害 | ☐ |
| 4.2 | 暗晶幽灵生成 | 刷怪蛋→飞行→俯冲造成5点伤害 | ☐ |
| 4.3 | 裂片守卫生成 | 刷怪蛋→近战+冲锋(12点)→免疫击退 | ☐ |
| 4.4 | 蠕虫掉落 | 击杀→0-2水晶碎片 | ☐ |
| 4.5 | 幽灵掉落 | 击杀→0-3水晶碎片 | ☐ |
| 4.6 | 守卫掉落 | 击杀→1-3水晶碎片 | ☐ |
| 4.7 | 自然生成 | 在虚空平原等待夜晚→怪物自然生成 | ☐ |

```bash
# === 阶段4 快速测试命令 ===
/execute in void_echo:voids_end run tp @s 0 128 0
/gamemode survival
/give @s void_echo:void_sword 1
/give @s void_echo:void_helmet 1
/give @s void_echo:void_chestplate 1
/give @s void_echo:void_leggings 1
/give @s void_echo:void_boots 1

# 批量生成测试
/give @s void_echo:void_worm_spawn_egg 5
/give @s void_echo:crystal_wraith_spawn_egg 5
/give @s void_echo:shard_guard_spawn_egg 5
```

---

### 🟣 阶段 5：武器 + 盔甲（5分钟）

| # | 测试项 | 命令/操作 | ✓ |
|---|--------|-----------|----|
| 5.1 | 虚空剑右键瞬移 | 右键→向前传送5格→冷却200tick | ☐ |
| 5.2 | 瞬移阻挡提示 | 对墙右键→"Cannot teleport"→短冷却40tick | ☐ |
| 5.3 | 虚空弓额外伤害 | 射击→+4虚空伤害 | ☐ |
| 5.4 | 虚空法杖发射 | 右键→虚空弹射→40tick冷却 | ☐ |
| 5.5 | 盔甲速度效果 | 左上角→速度I(1-3件)→速度II(4件) | ☐ |
| 5.6 | 全套免疫摔落 | 全套虚空合金→高处跳下→0伤害 | ☐ |
| 5.7 | 全套免疫虚空 | 全套→掉出世界→传送回地表+缓降+抗性 | ☐ |

```bash
# === 阶段5 快速测试命令 ===
/gamemode survival
/give @s void_echo:void_sword 1
/give @s void_echo:void_bow 1
/give @s void_echo:void_staff 1
/give @s void_echo:void_helmet 1
/give @s void_echo:void_chestplate 1
/give @s void_echo:void_leggings 1
/give @s void_echo:void_boots 1
/give @s minecraft:arrow 64

# 虚空免疫测试（全套穿着后）
/execute in void_echo:voids_end run tp @s 0 0 0
# → 应自动传送回地表而不死亡
```

---

### 🔴 阶段 6：Boss 1 — 虚空追猎者（5分钟）

| # | 测试项 | 关键观察 | ✓ |
|---|--------|----------|----|
| 6.1 | 召唤Boss | 紫色Boss血条出现 | ☐ |
| 6.2 | 阶段1(100%-60%) | 传送背后攻击 | ☐ |
| 6.3 | 阶段2(60%-30%) | 召唤蠕虫+护盾+虚空光束 | ☐ |
| 6.4 | 阶段3(<30%) | 高速连击+AOE脉冲 | ☐ |
| 6.5 | 击杀掉落 | 虚空之心×1+碎片+合金锭 | ☐ |
| 6.6 | 击杀成就 | 获得"Stalker's End"进度 | ☐ |

```bash
# === 阶段6 快速测试命令 ===
/execute in void_echo:voids_end run tp @s 0 128 0
/gamemode survival
/give @s void_echo:void_sword 1
/give @s void_echo:void_bow 1
/give @s void_echo:void_staff 1
/give @s void_echo:void_helmet 1
/give @s void_echo:void_chestplate 1
/give @s void_echo:void_leggings 1
/give @s void_echo:void_boots 1
/give @s minecraft:arrow 64
/summon void_echo:void_stalker ~ ~5 ~

# 自然遭遇测试（虚空平原等待）
/execute in void_echo:voids_end run tp @s 0 128 0
# → 等待/刷怪，看Boss是否自然生成（weight=5）
```

---

### 🟤 阶段 7：Boss 2 — 回声守卫（10分钟）

| # | 测试项 | 关键观察 | ✓ |
|---|--------|----------|----|
| 7.1 | 召唤Boss | 红色Boss血条+天空变暗+浓雾 | ☐ |
| 7.2 | 全屏斩击 | 蓄力→20格半径无视视线→20伤害+击退 | ☐ |
| 7.3 | 时间减速 | "Time bends..."→缓慢V+挖掘疲劳III | ☐ |
| 7.4 | 闪烁突袭 | "blinks through the void"→连续3次传送攻击 | ☐ |
| 7.5 | 阶段2(50%) | "form destabilizes"→满屏粒子→回声冲击波 | ☐ |
| 7.6 | 回声冲击波 | 扇形击退15伤害(阶段2新增) | ☐ |
| 7.7 | 阶段3(25%) | "frenzied rage"→爆炸粒子+移速提升 | ☐ |
| 7.8 | 虚空吸取 | "drains your life force"→吸生命+Boss回血 | ☐ |
| 7.9 | 虚空新星 | 3秒蓄力→"VOID NOVA!"→12格25伤害 | ☐ |
| 7.10 | 击杀掉落 | 回声核心×1+合金锭2-5+碎片5-12 | ☐ |
| 7.11 | 击杀成就 | 获得"Silence Falls"进度 | ☐ |
| 7.12 | 死亡特效 | 满屏粒子+多重音效 | ☐ |

```bash
# === 阶段7 快速测试命令 ===
/execute in void_echo:voids_end run tp @s 0 128 0
/gamemode survival
/give @s void_echo:void_sword 1
/give @s void_echo:void_bow 1
/give @s void_echo:void_staff 1
/give @s void_echo:void_helmet 1
/give @s void_echo:void_chestplate 1
/give @s void_echo:void_leggings 1
/give @s void_echo:void_boots 1
/give @s minecraft:arrow 64
/give @s minecraft:respawn_anchor 1
/give @s minecraft:glowstone 16
/summon void_echo:echo_warden ~ ~5 ~

# 正常召唤流程（通过祭坛）
/execute in void_echo:voids_end run locate structure void_echo:echo_sanctum
# → 传送到圣殿 → 在祭坛放暗晶块+虚空之心
/give @s void_echo:echo_altar 1
/give @s void_echo:crystal_block 16
/give @s void_echo:void_heart 1
```

---

### ⚫ 阶段 8：虚空锻炉升级（5分钟）

| # | 测试项 | 材料 | NBT键 | ✓ |
|---|--------|------|-------|----|
| 8.1 | 放置锻炉 | — | — | ☐ |
| 8.2 | 无效物品 | 任意非虚空装备→提示"cannot upgrade" | — | ☐ |
| 8.3 | 耐久+1 | 右装备+左手碎片×4 | `durability_upgrade:1` | ☐ |
| 8.4 | 耐久+4(封顶) | 升到Lv4→再试→"max durability" | `durability_upgrade:4` | ☐ |
| 8.5 | 回声T1 | 右手装备+左手回声核心 | `echo_upgrade:true` | ☐ |
| 8.6 | 回声打击T2 | 有回声+副手触媒+背包有核心 | `echo_strike:true` | ☐ |
| 8.7 | 回声守卫T2 | 有回声+副手碎片×3 | `echo_guard:true` | ☐ |
| 8.8 | 裂隙T1 | 右手装备+左手碎片×2 | `rift_upgrade:true` | ☐ |
| 8.9 | 裂隙行者T2 | 有裂隙+副手触媒+背包有暗晶块 | `rift_walker:true` | ☐ |
| 8.10 | 裂隙之怒T2 | 有裂隙+副手合金锭+背包有触媒 | `rift_fury:true` | ☐ |
| 8.11 | 路径互斥 | 有回声→升裂隙→回声被清除 | — | ☐ |

```bash
# === 阶段8 快速测试命令 ===
/gamemode survival
/give @s void_echo:void_forge 1
/give @s void_echo:void_sword 2
/give @s void_echo:crystal_shard 64
/give @s void_echo:echo_core 4
/give @s void_echo:void_catalyst 4
/give @s void_echo:rift_fragment 32
/give @s void_echo:crystal_block 16
/give @s void_echo:void_alloy_ingot 16
# → 放锻炉 → 右手剑+左手材料 → 右键
# → F3+H 开启高级提示框查看NBT
```

---

### 💚 阶段 9：宠物系统（3分钟）

| # | 测试项 | 命令/操作 | ✓ |
|---|--------|-----------|----|
| 9.1 | 暗晶精灵 | 刷怪蛋→小型飞行生物 | ☐ |
| 9.2 | 暗晶莓驯服 | 手持莓右键→跟随30秒 | ☐ |
| 9.3 | 虚空触媒进化 | 手持触媒右键→替换为守卫 | ☐ |
| 9.4 | 守卫远程射击 | 对敌对生物发射虚空弹射 | ☐ |
| 9.5 | 守卫护主 | 主人受伤→获得伤害吸收I | ☐ |

```bash
/give @s void_echo:crystal_sprite_spawn_egg 2
/give @s void_echo:crystal_berry 16
/give @s void_echo:void_catalyst 1
# → 生成精灵 → 喂莓 → 喂触媒 → 进化
```

---

### 💛 阶段 10：裂隙事件 + 饰品（5分钟）

| # | 测试项 | 命令/操作 | ✓ |
|---|--------|-----------|----|
| 10.1 | 裂隙生成 | 终虚渊等待→crying obsidian+传送门粒子 | ☐ |
| 10.2 | 裂隙关闭 | 站在裂隙3格内3秒→碎片掉落 | ☐ |
| 10.3 | 裂隙成长 | 粒子范围随时间增大（阶段1→2→3） | ☐ |
| 10.4 | 回声护符效果 | 携带护符在虚空→力量+速度+抗性 | ☐ |
| 10.5 | 护符主世界无效 | 回主世界→效果消失 | ☐ |
| 10.6 | 回声之书效果 | 携带书在虚空→永久夜视 | ☐ |

```bash
/execute in void_echo:voids_end run tp @s 0 128 0
/gamemode survival
/give @s void_echo:echo_amulet 1
/give @s void_echo:echo_tome 1
# → 等裂隙（0.1%/10秒）或直接看护符/书的效果
```

---

### 💜 阶段 11：结构生成（5分钟）

| # | 测试项 | 命令 | ✓ |
|---|--------|------|----|
| 11.1 | 遗忘祭坛(主世界) | `/locate structure void_echo:forgotten_altar` | ☐ |
| 11.2 | 虚空堡垒(地下) | `/execute in void_echo:voids_end run locate structure void_echo:void_fortress` | ☐ |
| 11.3 | 回声圣殿(森林) | `/execute in void_echo:voids_end run locate structure void_echo:echo_sanctum` | ☐ |
| 11.4 | 堡垒内部房间 | 入口塔→楼梯→走廊→Boss房→兵营→金库→陷阱房 | ☐ |
| 11.5 | 回声碎片收集 | 三个结构中各找到回声碎片(I-V) | ☐ |

```bash
# 忘记祭坛
/locate structure void_echo:forgotten_altar

# 虚空堡垒
/execute in void_echo:voids_end run locate structure void_echo:void_fortress

# 回声圣殿
/execute in void_echo:voids_end run locate structure void_echo:echo_sanctum
```

---

## 三、快速通关测试（3分钟验证核心流程）

```bash
# === 一键跑完核心流程 ===
/gamemode creative
/give @s void_echo:void_echo_journal 1
/give @s void_echo:void_key 1
/give @s minecraft:diamond_pickaxe 1
# → 进终虚渊
/execute in void_echo:voids_end run tp @s 0 128 0
# → 召唤Boss 1
/gamemode survival
/give @s void_echo:void_sword 1
/give @s void_echo:void_helmet 1
/give @s void_echo:void_chestplate 1
/give @s void_echo:void_leggings 1
/give @s void_echo:void_boots 1
/summon void_echo:void_stalker ~ ~5 ~
# → 击杀 → 召唤Boss 2
/summon void_echo:echo_warden ~ ~5 ~
# → 击杀 → 锻造
/give @s void_echo:void_forge 1
/give @s void_echo:echo_core 1
/give @s void_echo:void_catalyst 1
/give @s void_echo:rift_fragment 8
# → 升级 → 终局饰品
/give @s void_echo:echo_amulet 1
/give @s void_echo:echo_tome 1
```

---

## 四、配置调节（可选）

编辑 `config/void_echo.json`：

```json
{
  "bossHealthMultiplier": 1.0,    // 改大=更难, 改小=更简单
  "voidDimensionDifficulty": 1.0   // 虚空维度所有伤害倍率
}
```
