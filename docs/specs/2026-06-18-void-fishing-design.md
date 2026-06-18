# Void Fishing — 虚空诱捕系统规格

**模组:** Void Echo: Ancient Sanctum (虚空回响) · Fabric 1.21.1
**日期:** 2026-06-18
**状态:** 已确认

---

## 一、问题

终虚渊维度无水无岩浆，原版钓鱼竿浮标落入虚空无法使用。玩家在维度中缺少非战斗资源获取方式。需要一种主题一致、与现有玩法循环互补的资源获取机制。

## 二、核心机制

**虚空诱捕 (Void Luring):** 暗晶钓竿向虚空能量饱和的雾气中抛出诱饵浮标，浮标悬浮于空中。等待随机间隔后浮标下沉（VOID_AMBIENT 粒子信号），玩家收竿获得该群系对应的战利品。

- 浮标在所有 4 个虚空群系中均可使用（维度检查：`world.getRegistryKey()` in ModDimensions.VOIDS_END）
- 无水面需求 — 浮标实体悬浮在半空中
- 等待时间 5-30 秒（随机），与原版钓鱼一致
- 收竿时根据群系 roll 不同战利品表

## 三、物品

| ID | 名称(en/zh) | 类型 | 稀有度 | 说明 |
|----|------------|------|--------|------|
| `crystal_lure` | Crystal Lure / 暗晶钓竿 | VoidLureItem (新类) | UNCOMMON | 右键抛浮标，耐久 128 |
| `void_carp` | Void Carp / 虚空鲤 | Item | COMMON | 食物 3饥饿/0.6饱和度，可烤(5饥饿/1.2) |
| `crystal_ray` | Crystal Ray / 水晶鳐 | Item | UNCOMMON | 食物 2饥饿/0.4饱和度 + 30秒生命恢复 I |
| `void_crab_shell` | Void Crab Shell / 暗晶蟹壳 | Item | UNCOMMON | 材料，可合成虚空合金碎片 |
| `resonant_crystal` | Resonant Crystal / 共鸣水晶 | Item | RARE | T4 锻炉升级材料 |
| `aerolith_fragment` | Aerolith Fragment / 艾洛斯残片 | Item | RARE | 收藏品，含 1 句 Aerolith 文明短文本 |

## 四、实体

| ID | 类型 | 说明 |
|----|------|------|
| `void_lure_bobber` | VoidLureBobberEntity | 钓竿浮标，悬浮空中，VOID_AMBIENT 粒子 |
| `void_crab` | VoidCrabEntity | 5%概率钓起时生成，逃跑型小型敌对生物，HP 20 伤害 4，击杀掉落暗晶蟹壳×1-2 |

## 五、群系战利品表

钓鱼产出按群系分配：

| 群系 | 常见(60%) | 不常见(25%) | 稀有(10%) | 极稀有(5%) |
|------|----------|------------|----------|-----------|
| 虚空平原 | 虚空鲤 | 暗晶碎片×2 | 水晶鳐 | 艾洛斯残片 |
| 水晶森林 | 虚空鲤 | 水晶鳐 | 暗晶碎片×3 | 共鸣水晶 |
| 虚空荒原 | 暗晶蟹壳 | 虚空鲤 | 共鸣水晶 | 虚空合金碎片 |
| 水晶洞窟 | 虚空鲤 | 暗晶碎片×3 | 水晶鳐 | 艾洛斯残片 |

暗晶蟹生成概率：钓起时 5%（所有群系），独立于战利品表。

## 六、鱼群气泡

虚空中随机生成可见的暗晶粒子云团（VOID_AMBIENT，浮空 3×3 粒子区域），距地面 2-8 格。在气泡附近 8 格范围内钓鱼，上钩时间减半，稀有概率 ×2。

实现：每个区块 2% 概率生成 1 个气泡点。纯客户端粒子 + 服务端检测范围。

## 七、合成配方

```
暗晶钓竿:
  CS  -  CS    (CS=crystal_shard, -=empty, S=stick)
  CS  S  CS
  -   S  -

虚空鲤烹饪: void_carp → furnace → cooked_void_carp
水晶鳐烹饪: crystal_ray → furnace → cooked_crystal_ray

暗晶蟹壳×3 → crafting → 虚空合金碎片×1
```

## 八、共鸣水晶 — T4 锻炉升级

在虚空锻炉上，主手武器 + 副手共鸣水晶 → "水晶谐振"升级：

- **水晶谐振 (Crystal Resonance, T4):** 击中时 20% 概率连锁 5 格内另一个目标，造成 6 伤害（虚空脉冲效果）
- 前提：武器需已有任意 T1 回声或裂隙升级

## 九、实现文件清单

### Java
- `item/VoidLureItem.java` — 钓竿物品类
- `entity/VoidLureBobberEntity.java` — 浮标实体
- `entity/VoidCrabEntity.java` — 暗晶蟹实体
- `item/ModItems.java` — 注册 6 个新物品
- `entity/ModEntities.java` — 注册 2 个新实体
- `entity/client/renderer/VoidCrabRenderer.java` — 渲染
- `entity/client/model/VoidCrabModel.java` — 模型
- `VoidEchoClient.java` — 注册渲染器
- `mixin/EnchantmentEffectMixin.java` — T4 水晶谐振效果

### JSON
- `data/void_echo/recipes/crystal_lure.json`
- `data/void_echo/recipes/cooked_void_carp.json`
- `data/void_echo/recipes/cooked_crystal_ray.json`
- `data/void_echo/recipes/void_alloy_fragment_from_crab.json`
- `data/void_echo/loot_table/gameplay/fishing/void_plains.json`
- `data/void_echo/loot_table/gameplay/fishing/crystal_forest.json`
- `data/void_echo/loot_table/gameplay/fishing/void_wastes.json`
- `data/void_echo/loot_table/gameplay/fishing/crystal_caverns.json`
- `data/void_echo/tags/worldgen/biome/void_fishing.json` (biome tag)
- 物品模型 ×6（handheld for lure, generated for others）

### Assets
- `textures/item/crystal_lure.png` — 16×16
- `textures/item/void_carp.png` — 16×16
- `textures/item/crystal_ray.png` — 16×16
- `textures/item/void_crab_shell.png` — 16×16
- `textures/item/resonant_crystal.png` — 16×16
- `textures/item/aerolith_fragment.png` — 16×16
- `textures/entity/void_crab.png` — 16×16

### Lang
- `en_us.json` +14 keys
- `zh_cn.json` +14 keys

## 十、依赖与不实现

**依赖:** ModDimensions (维度检测)、ModItems/ModBlocks (现有注册模式)、ModParticleTypes.VOID_AMBIENT、EnchantmentEffectMixin (T4 扩展)

**不实现:**
- 鱼塘/鱼缸方块（装饰性，YAGNI）
- 鱼骨工具（水晶工具已填补齿轮空档）
- 三级钓竿分级（一根通用钓竿，简单即美）
- 虚空河（纯视觉效果，与气泡功能重叠）
