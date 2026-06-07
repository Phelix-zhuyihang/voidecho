# 🔮 Void Echo: Ancient Sanctum — 全面代码与设计评审报告

**模组名称：** Void Echo: Ancient Sanctum
**版本：** 1.0.0（Minecraft 1.21.1 / Fabric Loader 0.16.9 / Fabric API 0.102.0）
**代码规模：** 68 个 Java 源文件 + 约 160 个 JSON/资源文件
**评审日期：** 2026-06-06

---

## 一、整体玩法与设计理念

### 1.1 核心玩法分析

Void Echo 的设计核心是一个**以 "虚空" 为主题的全新维度探索 + BOSS 挑战**模组。玩法循环清晰：

1. **主世界阶段** — 寻找 `Forgotten Altar`（遗忘祭坛）结构 → 制作 `Void Key` → 激活虚空传送门 → 进入 `Void's End` 维度
2. **虚空维度探索** — 在 4 个自定义生物群系中收集 `Crystal Shard`（水晶碎片）→ 击杀 `Void Stalker` BOSS 获取 `Void Heart` → 制造 `Void Alloy` 装备
3. **终局挑战** — 在 `Echo Sanctum` 中用 `Void Heart` 召唤 `Echo Warden` 终极 BOSS → 击败获取 `Echo Core` → 制造最强装备

**评价：⭐⭐⭐⭐ (4/5)**
设计意图清晰，流程递进合理。但缺少 "为什么要进入虚空" 的核心驱动力叙事。

### 1.2 与现有游戏机制的契合度

- 使用原版矿物标签体系（`needs_diamond_tool`、`mineable/pickaxe`）
- 使用 Fabric API 标准注册方式
- 虚空维度传送机制仿照下界传送门而设计
- **不足：** 未使用 Fabric 的 `fabric-convention-tags-v2` 来标记虚空装备的矿物层级，可能与其他 MOD 的兼容性不够好

### 1.3 玩家体验流程

| 阶段 | 内容 | 评价 |
|------|------|------|
| 早期 | 寻找祭坛，制作钥匙 | ✅ 探索驱动，良好 |
| 中期 | 收集碎片，打 Void Stalker | ✅ 多阶段 BOSS，有深度 |
| 后期 | 制作虚空合金装备，召唤 Echo Warden | ✅ 终极挑战，奖励丰厚 |
| 引导 | 缺失游戏内引导书 / 进度提示 | ⚠️ 玩家可能不知如何开始 |

---

## 二、实体机制合理性

### 2.1 实体总览

| 实体 | 类型 | HP | 伤害 | 特殊机制 | AI 复杂度 |
|------|------|-----|------|----------|-----------|
| **VoidWorm** | 敌对 | 30 | 6 | 钻地瞬移 | ⭐⭐ |
| **CrystalWraith** | 敌对飞行 | 20 | ❌ 0 | 俯冲（无伤害！） | ⭐⭐ |
| **ShardGuard** | 敌对 | 50 | 10/12 | 冲锋攻击 | ⭐⭐⭐ |
| **VoidStalker** | BOSS | 300×倍率 | 8-14 | 三阶段变身 | ⭐⭐⭐⭐⭐ |
| **EchoWarden** | BOSS | 500×倍率 | 15-20 | 全屏斩/分身/时缓 | ⭐⭐⭐⭐⭐ |
| **CrystalSprite** | 被动飞行 | 10 | 0 | 驯服进化系统 | ⭐⭐⭐ |
| **CrystalGuardian** | 友方宠物 | 30 | 8 | 远程射击/保护 | ⭐⭐⭐ |
| **VoidBolt** | 投射物 | - | 8 | 施加虚空之触 | ⭐ |

### 2.2 🐛 关键 Bug

**🔴 CRITICAL: `CrystalWraithEntity` 无法造成伤害**
- 文件：`entity/mob/CrystalWraithEntity.java`
- 问题：俯冲攻击（Dive Bomb）只设置了速度矢量，在接触目标时**没有调用 `target.damage()` 或覆写碰撞检测**。幽灵会直接穿过玩家而不造成任何伤害。
- 影响：CrystalWraith 完全无害，只是一个视觉威胁。
- 修复：在 `CrystalWraithDiveGoal` 的 `tick()` 中检测与目标的距离 < 1.5 时调用 `target.damage()`。

### 2.3 平衡性分析

| 项目 | 评价 |
|------|------|
| BOSS 数值 | EchoWarden 500 HP / 15 近战 / 20 全屏斩，配置合理，有 `bossHealthMultiplier` 可调 |
| VoidStalker 三阶段 | 设计精良，阶段转换有明确阈值（60%/30%）和不同 AI 策略 |
| ShardGuard 冲锋 | 12 点冲锋伤害 + 免疫击退，较为强大但冷却时间（5-8秒）合理 |
| 虚空维度难度 | `voidDimensionDifficulty` 可配置，默认 1.0 无额外缩放 |

### 2.4 生成规则

- **所有实体均缺少自然生成规则注册！** `SpawnRestriction.register()` 调用未在任何文件中发现。虽然生物群系 JSON 中定义了 spawn 数据，但原版 `SpawnRestriction` 控制生成条件（光照、高度限制等）。
- 只有 `VoidFortress` 结构 JSON 中定义了 `shard_guard` 的结构内部生成。

---

## 三、物品与纹理模型美观度

### 3.1 纹理质量

**评价：⭐⭐⭐ (3/5)** — 纹理统一使用了深紫色/紫蓝色调，契合虚空主题。但存在不一致：

| 纹理 | 状态 |
|------|------|
| 方块纹理（16 个方块） | ✅ 全部存在，风格一致 |
| 实体纹理（7 个实体） | ✅ 全部存在 |
| 物品纹理（16 个物品） | ✅ 全部存在 |
| 盔甲纹理（2 层） | ✅ 存在 |
| 粒子纹理（3 个） | ⚠️ `void_ambient` 引用了不存在的纹理 |

### 3.2 🐛 纹理/模型问题

**🔴 CRITICAL: 粒子纹理缺失**
- 文件：`particles/void_ambient.json`
- 问题：引用 `void_echo:particle/void_ambient`，实际文件名为 `textures/particle/void_particle.png`
- 影响：虚空环境粒子将显示为紫黑缺失纹理方块
- 修复：将纹理路径改为 `void_echo:particle/void_particle` 或将 PNG 重命名为 `void_ambient.png`

### 3.3 模型技术问题

- 所有 17 个 blockstate JSON 语法正确，模型引用有效
- 所有 block 模型使用正确的父模型（`cube_all`、`cube_bottom_top`、`cross`）
- `void_bow.json` 的覆写顺序正确（pull 0.0 / 0.65 / 0.9），匹配原版弓的行为
- 实体模型均使用正确尺寸的纹理（64×64 或 32×32），UV 映射基本合理
- 实体模型（ShardGuard、VoidStalker、VoidWorm）的肢体摆动不随 `limbDistance` 缩放，静立时也在 "空气行走"

**评价：⭐⭐⭐⭐ (4/5)** — 模型技术方面质量较高，仅一个致命纹理路径问题。

---

## 四、维度设计完善度

### 4.1 维度定义

`dimension_type/voids_end.json` 的配置：

| 属性 | 值 | 评价 |
|------|-----|------|
| 时间固定 | 18000（午夜） | ✅ 永夜虚空 |
| 环境光 | 0.1 | ✅ 极其昏暗 |
| 天光 | 启用 | ⚠️ 与永夜组合，天空永远黑暗 |
| 床 | 禁用 | ✅ 需用重生锚 |
| 重生锚 | 启用 | ✅ 替代重生机制 |
| 坐标缩放 | 1.0 | ✅ 默认 |
| 高度范围 | 0-256 | ✅ |
| 迷雾 | 自定义暗紫色 | ✅ 氛围感强 |

### 4.2 自定义维度效果

`ModDimensionEffects.java` 正确实现了：

- 浓雾（Y<32 时启用 `useThickFog`）
- 暗紫色天空 (`rgb(0.02, 0.0, 0.05)`)
- 暗紫色雾 (`rgb(0.05, 0.0, 0.1)`)
- 禁用云层（`getCloudsHeight()` 返回 `NaN`）、End 类型天空、永久昏暗

### 4.3 缺失元素

| 缺失项 | 重要性 |
|--------|--------|
| 虚空维度无特殊矿物变种（无 void 版煤矿/铁矿等） | 低 |
| 无虚空特有流体 | 低 |
| 无地下洞穴系统定制（carver 引用原版） | 中 |
| 维度入口传送门不能激活（ForgottenAltar 的传送门框架是装饰性的） | ⚠️ 高 |

**🐛 问题：** `ForgottenAltarStructure` 中生成的 `VOID_PORTAL_FRAME` 方块仅用于装饰，无法像下界传送门那样被激活。实际传送门激活逻辑在 `VoidPortalFrameBlock.onUse()` 中依赖手持 `VOID_KEY` 右键。

---

## 五、地形生成分析

### 5.1 噪声配置

`noise_settings/voids_end.json` 使用了 `end/island` 噪声生成器，产生**类似末地的浮空岛屿地形**。

### 5.2 🐛 重大 Bug

**🔴 CRITICAL: `crystal_caverns` 生物群系可能永远不会生成**
- 文件：`noise_settings/voids_end.json` + `multi_noise_biome_source_parameter_list/voids_end_biomes.json`
- 问题：噪声路由器中 `depth` 参数被硬编码为 `0`，但 multi_noise 参数列表中 `crystal_caverns` 需要 `depth: 0.5`
- 由于 `depth` 恒为 0，`crystal_caverns` 永远不会被选中
- 修复：修改噪声路由器，使 depth 参数能够达到 0.5

### 5.3 特征生成

| 特征 | 类型 | 生成条件 | 评价 |
|------|------|----------|------|
| `crystal_ore_vein` | 矿石 | Void Stone 替换, 0-64 高度 | ⚠️ 使用了旧版 `type` 字段格式 |
| `crystal_ore_overworld` | 矿石 | 主世界 -64 ~ 64 高度 | ✅ |
| `crystal_bloom_patch` | 植被 | 地表相对高度 -1 ~ +1 | ⚠️ 名称是 "patch" 但用 `simple_block` |
| `crystal_cluster` | 装饰 | 0-128 均匀分布 | ⚠️ 同上，"cluster" 但只放置单个方块 |
| `void_grass_patch` | 地表 | 地表相对 | ⚠️ 同上 |

### 5.4 生物群系分布

| 生物群系 | 温度/湿度 | 视觉色调 | 特色 |
|----------|-----------|------|------|
| `void_plains` | 0.5 / -0.5 | 蓝色调 | 基础虚空平原 |
| `crystal_forest` | -0.5 / 0.5 | 蓝绿色调 | 水晶植被 |
| `void_wastes` | 0.8 / -0.8 | 红棕色调 | 荒芜废土 |
| `crystal_caverns` | -0.7 / 0.7 | 暗紫色调 | ⚠️ 可能因 depth=0 无法生成 |

所有四个生物群系共享相同的配乐 (`music_voids_end`)、氛围音效 (`ambient.cave`) 和水/雾颜色范围。色调渐变形成连贯的视觉体验：蓝（平原）→ 蓝绿（森林）→ 红棕（废土）→ 暗紫（洞穴）。

---

## 六、结构制作完成度与复杂度

### 6.1 三个结构逐个评估

#### Echo Sanctum（回声圣所）
- **完成度：⭐⭐⭐⭐ (4/5)**
- 圆形石砖平台（半径 4）、4 根柱子上浮空水晶+末地烛、拱门连接、中央祭坛、2 个隐藏战利品箱（70%概率）
- 使用环形判断 (`dist <= 20`) 制作圆台，裂缝石砖 30% 概率出现
- **问题：** 回声碎片只放了类型 2，其他 4 块（1、3、4、5）没有散布

#### Forgotten Altar（遗忘祭坛）
- **完成度：⭐⭐⭐ (3/5)**
- 7x7 石砖圆台、4 根柱子、虚空传送门框架环、中心水晶
- **问题：** 内部/外部区域都使用 `VOID_STONE_BRICKS`（代码有 `if (dist <= 4)` / `else` 分支但两个分支放置相同方块，暗示作者本意用不同方块）
- **🐛 高严重度：** 传送门框架是装饰性的，不可激活

#### Void Fortress（虚空堡垒）
- **完成度：⭐⭐⭐⭐ (4/5)**
- 最复杂的结构：入口塔楼（废墟圆柱体）、螺旋楼梯（下降 10 格）、4 方向主走廊、4 个专业房间（BOSS 房/兵营/金库/陷阱房）
- BOSS 房为半径 6 的圆形，含棋盘格地板和水晶平台
- 陷阱房含灵魂沙+灵魂火（50%概率）
- **问题：** 楼梯只是空心竖井而非实际楼梯方块

### 6.2 结构生成条件

| 结构 | 间距/间隔 | 生物群系 | 地形适应 | 评价 |
|------|-----------|----------|----------|------|
| Echo Sanctum | 50/30 | crystal_forest | beard_thin | ✅ |
| Forgotten Altar | 40/25 | ⚠️ 主世界群系(plains, desert, sunflower_plains) | beard_thin | ❌ 应该在虚空维度 |
| Void Fortress | 30/20 | void_plains + crystal_forest | bury | ✅ |

### 6.3 处理器列表

三个 processor_list 文件（`echo_sanctum_pristine`、`forgotten_altar_weathering`、`void_fortress_aged`）定义了风化/老化规则，但**没有被任何结构的 JSON 文件引用**（结构 JSON 缺失 `"processors"` 字段）。它们是孤立资源，除非 Java 代码中直接引用。

---

## 七、代码质量与 Bug 审查

### 7.1 架构与注册规范

| 方面 | 评价 |
|------|------|
| 注册顺序 | ✅ `VoidEcho.onInitialize()` 中顺序正确 |
| 服务端/客户端分发 | ✅ 使用 `world.isClient` 守卫正确 |
| 配置持久化 | ✅ `PortalStorage extends PersistentState` |
| 属性注册 | ✅ `FabricDefaultAttributeRegistry.register()` |
| 网络同步 | ✅ 使用 `ServerBossBar` 自动同步，无自定义包 |
| 燃料注册 | ✅ VoidStone 注册为燃料（800 ticks） |

### 7.2 🐛 Bug 完整清单

以下按严重程度排列：

| # | 严重度 | 文件/位置 | 问题描述 |
|---|--------|-----------|----------|
| 1 | 🔴 CRITICAL | `lang/en_us.json:161` `lang/zh_cn.json:161` | **JSON 语法错误**：最后一个 advancement 条目后缺少逗号，导致整个语言文件解析失败，所有翻译字符串失效——游戏内将显示原始翻译键而非中文/英文 |
| 2 | 🔴 CRITICAL | `particles/void_ambient.json` | 纹理路径引用不存在的 `void_ambient.png`（实际文件是 `textures/particle/void_particle.png`） |
| 3 | 🔴 CRITICAL | `entity/mob/CrystalWraithEntity.java` | 俯冲攻击不造成伤害——缺少碰撞检测或 `target.damage()` 调用，CrystalWraith 是完全无害的 |
| 4 | 🔴 CRITICAL | `noise_settings/voids_end.json` + `multi_noise_biome_source_parameter_list/voids_end_biomes.json` | `depth` 硬编码为 0，导致 `crystal_caverns` 生物群系（需要 depth=0.5）可能永不会生成 |
| 5 | 🔴 HIGH | `block/VoidPortalFrameBlock.java:75` `block/PortalStorage.java` | **跨世界存储 Bug**：传送门激活时将返回位置存入**虚空维度**的 PersistentState，但返回时从**主世界**的 PersistentState 读取。传回点永远找不到，玩家总是被传到主世界出生点 |
| 6 | 🔴 HIGH | `tags/worldgen/biome/has_structure/forgotten_altar.json` | 引用了主世界群系（plains, desert, sunflower_plains）而非虚空群系——遗忘祭坛永远不会在虚空维度生成 |
| 7 | 🔴 HIGH | `item/VoidStaffItem.java:16` | `super(settings.maxDamage(250))` 创建全新 Settings 对象，**丢弃了调用方传入的 `rarity(Rarity.EPIC)` 和 `fireproof()` 设置**——该法杖不是史诗稀有度，也不防火 |
| 8 | 🟡 MODERATE | `worldgen/configured_feature/crystal_ore_vein.json` | 使用旧版 `"type": "minecraft:block_match"` 格式而非新版 `"predicate_type"`，与同目录的 `crystal_ore_overworld.json` 不一致，在 1.21+ 中可能加载失败 |
| 9 | 🟡 MODERATE | `mixin/EnchantmentEffectMixin.java` `mixin/LivingEntityMixin.java` | 两个 Mixin 同时 `@ModifyVariable` 注入 `LivingEntity.damage()` HEAD，执行顺序不确定，可能导致复合伤害计算不一致 |
| 10 | 🟡 MODERATE | `item/VoidSwordItem.java` | 瞬移安全检查只验证目标方块和上方 1 格，不检查玩家完整碰撞箱（1.8 格高），上方第二格若为固体方块将导致玩家窒息 |
| 11 | 🟡 MODERATE | 3 个结构 Java 类（EchoSanctumStructure, ForgottenAltarStructure, VoidFortressStructure） | `getStructurePosition` 中的 Y 坐标（固定值 90/60）与 `generate()` 中重新计算的 `surfaceY` 不一致，可能导致结构方块在 boundingBox 之外——结构被截断或不生成 |
| 12 | 🟢 LOW | `item/VoidArmorItem.java` | `player.getEquippedStack(wornSlot) == stack` 使用引用相等（`==`）而非 `ItemStack.areEqual()`，在某些边缘情况下（物品栏序列化/克隆）可能失败 |
| 13 | 🟢 LOW | `item/EchoAmuletItem.java` `EchoTomeItem.java` `VoidArmorItem.java` | 每 tick 无条件刷新状态效果——应检查玩家是否已有效果且剩余持续时间充足，再决定是否重新施加 |
| 14 | 🟢 LOW | `block/CrystalBloomBlock.java` | 使用已弃用的 `isTransparent()` 方法 |
| 15 | 🟢 LOW | `ModBlocks.java` `VoidPortalFrameBlock.java` `PortalStorage.java` `VoidFortressStructure.java` `ForgottenAltarStructure.java` 等 | 多个文件存在未使用的 import 语句，代码清理不彻底 |
| 16 | 🟢 LOW | 实体模型（ShardGuard, VoidStalker, VoidWorm） | 肢体摆动不随 `limbDistance` 缩放——静立时也在 "空气行走"。对比 EchoWarden 正确使用了 `Math.min(limbDistance * 0.5, 0.6)` |
| 17 | 🟢 LOW | `ModStructurePieceTypes.java` | 从 NBT 读取 `"length"` 字段但 `writeNbt` 方法从未写入该字段 |
| 18 | 🟢 LOW | `entity/boss/EchoWardenEntity.java` 的 `EchoWardenCloneGoal` | "Clone" 分身技能实际只是快速瞬移 3 次（同一 tick 内循环 `for (int i = 0; i < 3; i++)` 依次传送到目标周围 3 个位置），并非真正的分身/幻影——名称具有误导性 |
| 19 | 🟢 LOW | `PortalStorage.java` | 无旧 UUID 清理机制——大型服务器上长期运行后 map 无限增长 |
| 20 | 🟢 LOW | `VoidRiftManager.java` | 使用 `static HashMap<ServerWorld, List<ActiveRift>>` 且无全局裂隙数量上限 |

### 7.3 代码风格评价

| 方面 | 评价 |
|------|------|
| 命名规范 | ✅ 遵循 Java/Mojang 命名惯例 |
| 注释 | ⚠️ 较少，仅关键方法有 JavaDoc |
| 包结构 | ✅ 合理分层（block/entity/item/world/mixin/network/config） |
| Mixin 使用 | ⚠️ 两个 Mixin 存在执行顺序依赖问题 |
| 数据驱动 | ✅ 大部分配置通过 JSON 数据包实现 |
| NBT 访问模式 | ⚠️ 在多处使用 `copyNbt()` 创建防御性副本而非直接访问，效率略低但无功能影响 |

---

## 八、可玩性评估

### 8.1 优势

1. **清晰的进度体系** — 通过 23 个 Advancement（成就）形成完整的进度引导
2. **多阶段 BOSS 战** — Void Stalker 的三阶段机制（潜行者→召唤者→狂暴）是最大亮点，有明确的转阶段触发（60%/30% HP）和策略变化（瞬移背刺→召唤蠕虫+吸收盾+光束→高速连击+AOE脉冲）
3. **装备进阶** — Void Alloy 提供全套武器/工具/盔甲体系（剑/弓/法杖/头盔/胸甲/护腿/靴子），含附魔系统（5 种自定义附魔）和虚空锻炉升级系统（3 种 NBT 升级）
4. **维度氛围** — 暗紫色天空、浓雾、自定义音乐（`voids_end.ogg`）和 19 个自定义音效共同营造了强烈的虚空氛围
5. **Risk/Reward 平衡** — Echo Amulet 在虚空维度中永久提供力量+速度+抗性，但在主世界无效，鼓励玩家留在虚空探索

### 8.2 不足

1. **引导缺失** — 新玩家不知道如何找到 Forgotten Altar 来开始进程；无游戏内手册或线索物品
2. **虚空维度缺乏目的** — 除了收集 Crystal Shard 和打 BOSS 外，缺少非战斗内容（如虚空村庄、NPC、交易、谜题等）
3. **重复劳动** — Crystal Shard 的获取过于依赖刷怪（Crystal Wraith 0-3 个、Void Worm 0-2 个）和采矿（水晶矿 1-6 个含时运）
4. **终局内容单薄** — Echo Warden 没有多阶段机制，战斗模式单一（全屏斩→传送→时缓→近战循环），且 CrystalWraith 不造成伤害使得虚空维度缺少空中威胁
5. **死亡惩罚模糊** — 虚空维度禁用床但可使用重生锚，然而并没有说明重生锚的合成配方需要什么

### 8.3 目标受众

适合**硬核探索型玩家**和**MOD 整合包开发者**。模组可以通过 `ModConfig` 调整 BOSS 血量和维度难度。难度曲线初段较陡——玩家需先在主世界探险找到遗忘祭坛，制作 Void Key（需要下界合金碎片和末影珍珠），才能进入虚空维度。

---

## 九、可扩展性与可维护性

### 9.1 扩展性

| 方面 | 评价 |
|------|------|
| API 暴露 | ❌ 无公共 API——所有类都是 `public` 但未设计给外部 mod 调用 |
| 数据驱动 | ✅ 附魔、战利品表、配方、结构均通过 JSON 配置 |
| 自定义注册项 | ⚠️ 未提供可扩展的注册表接口（如自定义虚空生物群系） |
| 标签系统 | ✅ 定义了 `void_alloy_items`、`void_armor`、`void_weapons`、`void_stone_blocks` 等标签 |

### 9.2 配置

`ModConfig.java` 目前仅 2 个可配置参数：

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `bossHealthMultiplier` | 1.0 | BOSS 血量倍率 |
| `voidDimensionDifficulty` | 1.0 | 虚空维度伤害倍率（传入伤害乘以此值） |

**评价：⭐⭐ (2/5)** — 配置项太少。建议增加：

- `crystalOreSpawnRate` — 水晶矿生成率
- `riftSpawnChance` — 裂隙事件概率
- `wardenSlashDamage` — Warden 全屏斩基础伤害
- `stalkerPhaseThresholds` — Stalker 转阶段血量阈值
- `enableVoidWormBurrow` — 蠕虫钻地开关（对低配客户端友好）

### 9.3 兼容性设计

| 方面 | 评价 |
|------|------|
| 矿物标签 | ✅ 使用 `minecraft:needs_diamond_tool`、`minecraft:mineable/pickaxe` |
| 挖掘等级 | ✅ 钻石等级（虚空传送门框架和回声祭坛需要钻石工具） |
| 物品标签 | ✅ 定义了武器/盔甲/合金物品标签 |
| 流体 | ❌ 未注册自定义流体 |
| 能量系统 | ❌ 未集成任何能量 API |
| Mod Menu | ⚠️ 在 `fabric.mod.json` 中建议但未集成（`build.gradle` 中依赖被注释掉） |
| 矿物辞典兼容 | ⚠️ 未使用 `c` 标签（Fabric convention tags）进行跨 MOD 矿物统一 |

---

## 十、综合建议

### 🔥 优先修复清单

| 优先级 | 问题 | 预计工作量 |
|--------|------|-----------|
| **P0** | 修复 `lang/en_us.json` 和 `zh_cn.json` 第 161 行缺失逗号 | 1 分钟 |
| **P0** | 修复 `void_ambient` 粒子纹理路径（`void_ambient` → `void_particle` 或重命名文件） | 1 分钟 |
| **P0** | 修复 CrystalWraith 俯冲攻击无伤害——在 dive goal 中添加接触伤害判定 | 15 分钟 |
| **P0** | 修复 PortalStorage 跨世界存储 Bug——将 set 和 get 调用统一为同一世界的 PersistentState | 30 分钟 |
| **P0** | 修复 noise_settings 中 `depth=0` 导致 crystal_caverns 无法生成 | 10 分钟 |
| **P1** | 修复 `forgotten_altar` 生物群系标签——改为引用虚空群系 | 5 分钟 |
| **P1** | 修复 VoidStaffItem Settings 覆盖 Bug——在构造时合并而非替换 settings | 5 分钟 |
| **P1** | 修复 `crystal_ore_vein.json` 的 `"type"` → `"predicate_type"` 字段格式 | 5 分钟 |
| **P1** | 修复 VoidSwordItem 瞬移窒息风险——检查完整玩家碰撞箱（2 格高） | 20 分钟 |
| **P2** | 将孤立的 processor_list 连接到对应结构 JSON 的 `"processors"` 字段 | 15 分钟 |
| **P2** | 修复 3 个结构类中 Y 坐标不一致——使用统一的 surfaceY 计算方式 | 1 小时 |
| **P2** | 修复 ShardGuard/VoidStalker/VoidWorm 模型肢体缩放——添加 `limbDistance` 缩放 | 30 分钟 |
| **P2** | 为所有实体添加 `SpawnRestriction.register()` 生成规则 | 30 分钟 |
| **P2** | 清理所有未使用的 import 语句 | 15 分钟 |

### 🗺️ 开发路线图建议

#### 第一阶段：修复与稳定（1-2 周）
- 修复上述所有 P0 和 P1 问题
- 添加基础 `SpawnRestriction` 注册（光照/高度条件）
- 确保 4 个生物群系都能正常生成
- 运行一遍完整的游戏流程测试（进入虚空→探索 4 个群系→击败 Stalker→召唤 Warden→击败 Warden）

#### 第二阶段：内容完善（2-4 周）
- 为 CrystalWraith、VoidWorm、ShardGuard 增加更多 AI 行为变体
- 添加虚空维度特有的洞穴生成器（替换原版 cave carver）
- 实现 Echo Warden 的多阶段战斗（50% HP 进入狂暴、25% HP 召唤 Void Worm 增援等）
- 添加虚空维度的简单 NPC 或可交互方块（如虚空旅人、废弃研究站）
- 给 VoidStalker 的 "召唤者" 阶段增加虚空光束的视觉预警粒子

#### 第三阶段：丰富玩法（1-2 月）
- 实现以下 3-5 个玩法扩展点子
- 添加 Mod Menu 集成配置屏幕（可视化编辑 `void_echo.json` 配置）
- 添加更多虚空结构和地牢（每个生物群系至少一个独特结构）
- 为虚空维度添加天气/环境事件系统

### 💡 玩法扩展点子

#### 点子 1：虚空裂隙事件深化
**核心机制：** 当前 `VoidRiftManager` 仅生成敌对生物。可改为动态事件系统——裂隙随时间 "生长"，阶段越大越危险，奖励越丰厚。
**所需改动：**
- 在 `ActiveRift` 中添加 "阶段" 枚举（小→中→大→灾难）
- 大裂隙生成迷你 BOSS（强化版 CrystalWraith）
- 关闭裂隙时根据阶段掉落递增数量的 Rift Fragment
- 添加裂隙生长时的视觉特效（粒子增多、方块范围扩大）

#### 点子 2：虚空共鸣系统（风险/收益）
**核心机制：** 在虚空维度中待得越久，`ECHO_RESONANCE` 效果层数越深（玩家受到更多伤害），但 `CRYSTAL_SHARD` 掉落率同步提高。风险与收益并存，鼓励玩家做 "深度探索 vs 安全返回" 的抉择。
**所需改动：**
- 新增一个追踪 "虚空暴露时间" 的玩家 attachment（使用 Fabric Data Attachment API）
- 在 `LivingEntityMixin` 中动态调整伤害倍率和掉落倍率
- 离开虚空维度时重置层数

#### 点子 3：水晶驯兽进化树
**核心机制：** 扩展 CrystalSprite → CrystalGuardian 进化链，增加更多进化路线。例如：喂养不同物品使 CrystalSprite 进化为不同形态的守护者（水晶弓箭手/水晶治疗者/水晶坦克）。
**所需改动：**
- 新建 2-3 个 CrystalGuardian 变体实体类
- 在 `CrystalSpriteEntity` 的 `onUse` 中添加进化分支判断（检查玩家手持物品类型）
- 为每种进化路线设计不同的属性、AI 和纹理

#### 点子 4：虚空传送门网络
**核心机制：** 允许玩家在虚空维度建立多个传送门连接点，形成快速旅行网络。`PortalStorage` 中不再只存储单一返回点，而是维护一个命名的传送门网络。
**所需改动：**
- 扩展 `PortalStorage` 数据结构——从 `HashMap<UUID, BlockPos>` 改为 `HashMap<UUID, HashMap<String, BlockPos>>`
- 新建 "传送门命名/选择" 的交互方式（可通过右键传送门框架打开简单 GUI 或使用 `/trigger` 命令）
- 限制每个玩家的最大传送门数量

#### 点子 5：虚空锻炉升级分支树
**核心机制：** 将 `VoidForgeBlock` 从当前的线性升级（3 种 NBT 升级：耐久增强/回声升级/裂隙升级）扩展为分支技能树。每种武器/盔甲有不同的升级路线。例如：虚空剑可升级为 "生命偷取剑"（吸血增强）或 "爆发剑"（暴击伤害）。
**所需改动：**
- 重构 `VoidForgeBlock.onUse()` 支持多分支路径选择
- 在 `CUSTOM_DATA` NBT 中存储升级路径标识（`void_echo:upgrade_path`）
- 在 `EnchantmentEffectMixin` 中根据升级路径实现不同效果
- 每种升级路径需要不同的材料消耗

---

## 📊 总体评分

| 维度 | 评分 | 权值 |
|------|------|------|
| 整体玩法与设计理念 | ⭐⭐⭐⭐ | 15% |
| 实体机制合理性 | ⭐⭐⭐ | 15% |
| 物品与纹理模型美观度 | ⭐⭐⭐⭐ | 10% |
| 维度设计完善度 | ⭐⭐⭐ | 10% |
| 地形生成分析 | ⭐⭐⭐ | 10% |
| 结构制作完成度 | ⭐⭐⭐⭐ | 10% |
| 代码质量与 Bug | ⭐⭐⭐ | 15% |
| 可玩性评估 | ⭐⭐⭐ | 10% |
| 可扩展性与可维护性 | ⭐⭐⭐ | 5% |

**综合评分：⭐⭐⭐½ (3.5/5)**

---

> **结论：** Void Echo 是一个有清晰愿景和扎实基础的虚空主题大型模组。三阶段 VoidStalker BOSS、完整的维度设计、丰富的结构和音效资产展现了作者不俗的设计和开发能力。当前最需要解决的是 7 个 CRITICAL/HIGH 级别的 Bug（特别是语言文件 JSON 语法错误导致所有翻译失效、跨世界存储 Bug 导致传送门无法正确返回、CrystalWraith 无伤害和 crystal_caverns 无法生成），修复后模组可达到可发布质量。在此基础上，增加引导系统（游戏内手册/提示）、丰富终局内容（Echo Warden 多阶段）和添加更多配置选项将使模组达到优秀水平。
