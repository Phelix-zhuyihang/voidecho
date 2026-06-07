# Void Echo 代码审查 — 问题清单

> 完整评审报告见 [REVIEW_REPORT.md](./REVIEW_REPORT.md)

## 🔴 CRITICAL（必须立即修复）

| # | 文件 | 行号 | 问题 |
|---|------|------|------|
| 1 | `lang/en_us.json` `lang/zh_cn.json` | 161 | JSON 语法错误：缺失逗号，导致所有翻译字符串失效 |
| 2 | `particles/void_ambient.json` | - | 纹理路径引用不存在的 `void_ambient.png`（实际为 `void_particle.png`） |
| 3 | `entity/mob/CrystalWraithEntity.java` | dive goal | 俯冲攻击无伤害——未调用 `target.damage()` |
| 4 | `noise_settings/voids_end.json` | depth | `depth` 硬编码为 0，导致 `crystal_caverns` 群系无法生成 |

## 🔴 HIGH（严重影响功能）

| # | 文件 | 行号 | 问题 |
|---|------|------|------|
| 5 | `block/VoidPortalFrameBlock.java` `block/PortalStorage.java` | 75 | 跨世界存储 Bug：存入虚空维度、从主世界读取，传送门返回位置永远丢失 |
| 6 | `tags/worldgen/biome/has_structure/forgotten_altar.json` | - | 引用主世界群系而非虚空群系 |
| 7 | `item/VoidStaffItem.java` | 16 | `super(settings.maxDamage(250))` 丢弃了 rarity 和 fireproof 设置 |

## 🟡 MODERATE

| # | 文件 | 问题 |
|---|------|------|
| 8 | `configured_feature/crystal_ore_vein.json` | 使用旧版 `type` 字段格式（应为 `predicate_type`） |
| 9 | `mixin/EnchantmentEffectMixin.java` `mixin/LivingEntityMixin.java` | 两个 Mixin 同时 @ModifyVariable 注入 damage()，执行顺序不确定 |
| 10 | `item/VoidSwordItem.java` | 瞬移安全检测不检查完整碰撞箱，可能窒息 |
| 11 | 3 个 Structure 类 | Y 坐标在 getStructurePosition 和 generate() 中不一致 |

## 🟢 LOW

| # | 文件 | 问题 |
|---|------|------|
| 12 | `item/VoidArmorItem.java` | 使用 `==` 比较 ItemStack（应使用引用安全的比较） |
| 13 | `EchoAmuletItem.java` `EchoTomeItem.java` `VoidArmorItem.java` | 每 tick 无条件刷新状态效果 |
| 14 | `CrystalBloomBlock.java` | 使用已弃用的 `isTransparent()` |
| 15 | 多个文件 | 未使用的 import 语句 |
| 16 | 实体模型（ShardGuard, VoidStalker, VoidWorm） | 肢体摆动不随 limbDistance 缩放（"空气行走"） |
| 17 | `ModStructurePieceTypes.java` | NBT read 读取未写入的 `length` 字段 |
| 18 | `EchoWardenCloneGoal` | "Clone" 实际是快速瞬移而非真正分身 |
| 19 | `PortalStorage.java` | 无旧 UUID 清理机制 |
| 20 | `VoidRiftManager.java` | 无全局裂隙数量上限 |

## 📊 评分汇总

| 维度 | 评分 |
|------|------|
| 整体玩法与设计理念 | ⭐⭐⭐⭐ |
| 实体机制合理性 | ⭐⭐⭐ |
| 物品与纹理模型美观度 | ⭐⭐⭐⭐ |
| 维度设计完善度 | ⭐⭐⭐ |
| 地形生成分析 | ⭐⭐⭐ |
| 结构制作完成度 | ⭐⭐⭐⭐ |
| 代码质量与 Bug | ⭐⭐⭐ |
| 可玩性评估 | ⭐⭐⭐ |
| 可扩展性与可维护性 | ⭐⭐⭐ |
| **综合** | **⭐⭐⭐½ (3.5/5)** |
