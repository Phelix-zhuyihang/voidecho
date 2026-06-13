# Void Echo Mod — 全面 Bug 分析报告

**模组版本：** Fabric 1.21.1
**分析日期：** 2026-06-13
**分析方法：** 完整源码扫描 + 5 并行子 Agent 逐模块深度审计 + 人工交叉验证

---

## 扫描摘要

```
- 物品（特殊行为）：8 个（VoidSwordItem, VoidBowItem, VoidStaffItem, EchoAmuletItem, EchoTomeItem, VoidEchoJournalItem, VoidArmorItem, TooltippedItem）
- 方块（含特殊行为）：17 个 / 7 个有特殊行为（EchoAltarBlock, VoidForgeBlock, VoidPortalFrameBlock, EchoShardBlock, VoidGrassBlock, CrystalBloomBlock, CrystalOreBlock）
- 实体（生物/弹射物）：8 个（VoidWorm, CrystalWraith, ShardGuard, VoidStalker, EchoWarden, CrystalSprite, CrystalGuardian, VoidBoltEntity）
- 配方（JSON）：20 个
- 战利品表：19 个（16 方块 + 3 箱子；实体战利品表目录存在但为空）
- 进度：22 个
- 地物（ConfiguredFeature）：5 个
- 结构（Structure）：3 个（EchoSanctum, ForgottenAltar, VoidFortress）
- 网络包（自定义 Packet）：0 个（ModNetwork 为空骨架）
- 模型 JSON 文件：61 个（16 方块 + 45 物品/工具/生成蛋）
- 纹理 PNG 文件：55 个
- 语言文件条目：约 116 条（en_us.json）
- Mixin 类：2 个（LivingEntityMixin, EnchantmentEffectMixin）
- accessWidener 文件：否
```

验证声明：所有 5 个并行子 Agent 均已扫描完成并产出详细审计报告。本摘要基于源码目录直接检查和 Agent 结果交叉验证。每个类别均已实际检查——0 条目意味着源码和资源中确实不存在（如网络包确认为空、accessWidener 确不存在）。

---

# 功能 Bug 报告

## 一、严重 Bug（导致数据丢失、功能完全失效、物品/进度永久不可达）

### #1 虚空锻造台：副作用在原子 NBT 更新失败时仍触发（材料损失）

| 项目 | 内容 |
|------|------|
| **位置** | `block/VoidForgeBlock.java:46-183` |
| **触发条件** | `ItemStack.apply()` 返回失败（NBT 过大、并发修改等罕见情况），但 lambda 内部已设置 `appliedUpgrade[0]` 数组元素 |
| **实际表现** | `appliedUpgrade[0]` 用于通信 lambda 结果。如果 `apply()` 失败，lambda 对 NBT 的修改被回滚，但 `appliedUpgrade[0]` 已被设置。147-183 行的 switch 语句消费了升级材料（`offHand.decrement(1)` 或 `player.getInventory().remove(...)`），但 NBT 修改未写入。**玩家损失材料，未获得升级** |
| **修复建议** | 将副作用合并到 lambda 内部，仅当返回新 `NbtComponent` 时执行 `decrement`。或使用 `AtomicReference` + 在 lambda 外部仅在 `apply()` 返回新值时执行副作用 |

### #2 实体战利品表 JSON 源文件缺失（gradle clean 后永久丢失）

| 项目 | 内容 |
|------|------|
| **位置** | `src/main/resources/data/void_echo/loot_table/entities/`（目录存在但为空） |
| **触发条件** | `/loot` 命令、其他模组调用 LootTable API 读取实体掉落；或 `gradle clean` 后构建 |
| **实际表现** | 5 个实体有 build 产物中的 JSON（echo_warden, void_stalker, shard_guard, crystal_wraith, void_worm），但源目录为空。2 个实体完全没有战利品表（crystal_sprite, crystal_guardian）。`gradle clean` 后所有文件消失。虽然 `dropEquipment()` 在代码中直接调用 `dropItem()`，但通过 LootTable API 查询将返回空表 |
| **修复建议** | 将 build/resources/main/data/void_echo/loot_table/entities/ 下的文件复制到 src 对应目录；为 crystal_sprite/crystal_guardian 创建战利品表 |

### #3 PortalStorage：返回主世界坐标在传送完成前被删除（数据丢失）

| 项目 | 内容 |
|------|------|
| **位置** | `block/PortalStorage.java:39-46`（删除条目）+ `block/VoidPortalFrameBlock.java:38-41`（使用坐标） |
| **触发条件** | `getReturnPosition()` 在 42-44 行先执行 `returnPositions.remove(playerUuid)` 并 `markDirty()`，然后 VoidPortalFrameBlock 在第 41 行才执行 `player.teleportTo()` |
| **实际表现** | 如果传送失败（目标世界被卸载、玩家断开连接、TeleportTarget 无效等），返回坐标已从 PersistentState 中永久删除。玩家下次使用返回传送门时会被传送到世界出生点而非原始传送门位置 |
| **修复建议** | 将 `remove()` 和 `markDirty()` 移到 `teleportTo()` 成功之后执行；或使用 try-finally 仅在传送成功时删除 |

---

## 二、功能性缺陷（功能部分失效、行为错误、AI 冲突、体验异常）

### #4 ShardGuard 冲锋 / VoidWorm 潜地 Goal 与 MeleeAttackGoal 冲突

| 项目 | 内容 |
|------|------|
| **位置** | `entity/mob/ShardGuardEntity.java:138-140`（`shouldContinue()` 返回 `false`）；`entity/mob/VoidWormEntity.java:155`（同样模式） |
| **触发条件** | Goal 的 `start()` 设置实体状态（`isCharging=true`/`isBurrowing=true`），`shouldContinue()` 立即返回 `false` 报告 Goal 已结束 |
| **实际表现** | Goal 选择器认为冲锋/潜地 Goal 已完成，立即运行 MeleeAttackGoal（优先级 3），后者调用 `startMovingTo(target, 0.8)` 与冲锋状态中实体 `tick()` 调用的 `startMovingTo(target, 1.5)` 冲突——导航路径在两个速度间来回切换，实体移动抖动 |
| **修复建议** | `shouldContinue()` 应返回 `guard.isCharging` / `worm.isBurrowing`，或改为在 Goal 自身的 `tick()` 中管理所有持续状态逻辑 |

### #5 虚空维度地表特征使用错误的 Y 定位方式（植物浮空/埋地）

| 项目 | 内容 |
|------|------|
| **位置** | `worldgen/placed_feature/crystal_bloom_placed.json:12-22`；`crystal_cluster_placed.json:12-18` |
| **触发条件** | 使用 `minecraft:height_range`（uniform 0→128）而非 `minecraft:heightmap` |
| **实际表现** | Crystal Bloom（地表植物，`noCollision` + `breakInstantly`）可能在 Y=37 或 Y=112 等随机高度尝试放置，而非地表。`vegetation_patch` 会尝试寻找有效地面，但起始点偏离地表会导致部分放置失败或视觉效果异常。对比 `void_grass_patch_placed.json` 正确使用了 `WORLD_SURFACE_WG` heightmap |
| **修复建议** | 将两个 placed_feature 的 `height_range` 替换为 `{"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"}` |

### #6 VoidSwordItem / VoidStaffItem 客户端无手臂摆动动画

| 项目 | 内容 |
|------|------|
| **位置** | `item/VoidSwordItem.java:34-36`；`item/VoidStaffItem.java:30-32` |
| **触发条件** | `use()` 方法在 `world.isClient` 时返回 `TypedActionResult.pass(stack)` |
| **实际表现** | 客户端认为物品使用"未通过"，不播放手臂摆动动画。玩家按右键感觉"什么都没发生"，尽管服务端正确执行了传送/发射虚空箭 |
| **修复建议** | 客户端也返回 `TypedActionResult.success(stack)`（或 `consume`）。仅将实际游戏逻辑（坐标检查、实体生成）留在 `!world.isClient` 中 |

### #7 虚空锻造台 `player.getInventory().remove()` 第三参数传 null（脆弱的 API 依赖）

| 项目 | 内容 |
|------|------|
| **位置** | `block/VoidForgeBlock.java:159, 172, 177` |
| **触发条件** | `player.getInventory().remove(predicate, count, null)` — 第三个参数传 `null` |
| **实际表现** | 原版 `Inventory` 接口仅有 `remove(Predicate, int)` 2 参数版本。3 参数版本依赖 Fabric API 的 `PlayerInventory` mixin。当前 Fabric API 0.102+ 支持 `@Nullable PlayerEntity` 第三参数，但若未来版本变更或与其他修改 Inventory 接口的模组冲突，可能抛出 NPE |
| **修复建议** | 改回使用 2 参数版本 `player.getInventory().remove(predicate, count)` 避免不必要的 API 依赖 |

### #8 CrystalWraith 俯冲攻击有小范围盲区（1.5~2.0 格距离）

| 项目 | 内容 |
|------|------|
| **位置** | `entity/mob/CrystalWraithEntity.java:187-192`（`canStart()`）+ 212-217（命中检测） |
| **触发条件** | `canStart()` 要求 `squaredDistanceTo >= 4.0`（欧几里德距离 ≥ 2.0），但 `tick()` 在 `dist < 2.25`（距离 < 1.5）时命中并结束俯冲。距离在 [1.5, 2.0) 区间时既不能命中也不能开始新俯冲 |
| **实际表现** | 在极少数情况下（目标恰好在临界距离），CrystalWraith 暂时无法攻击。但浮空 AI 维持高度优势（通常高于目标 3~5 格），实际游玩中很少触发此边界情况 |
| **修复建议** | 可将 `canStart()` 的距离下限从 4.0 降至 2.25 或直接移除下限（因为命中检测已有上限） |

---

## 三、资源与配置不一致问题

### #9 声音字幕键名不匹配：sounds.json 与 en_us.json 不一致

| 项目 | 内容 |
|------|------|
| **位置** | `assets/void_echo/sounds.json:84, 88` vs `assets/void_echo/lang/en_us.json:105-106` |
| **具体差异** | sounds.json 引用 `subtitles.void_echo.block.portal.activate`；en_us.json 定义 `subtitles.void_echo.block.void_portal.activate`（多了一个 `void_` 前缀）。环境音同理 |
| **实际表现** | 激活/使用虚空传送门时，字幕显示原始未翻译键名而非 "Portal activates" / "Portal hums" |
| **修复建议** | 修改 sounds.json 的 subtitle 字段匹配 en_us.json 的键名（推荐，更语义化） |

### #10 zh_cn.json 创造模式标签页键名与其他语言文件不一致

| 项目 | 内容 |
|------|------|
| **位置** | `assets/void_echo/lang/zh_cn.json:78` vs `item/ModItemGroups.java:15` |
| **具体差异** | zh_cn.json 使用 `"itemGroup.void_echo.void_echo_tab"`；Java 代码和 en_us.json 使用 `"itemGroup.void_echo_tab"` |
| **实际表现** | 中文客户端创造模式标签页标签显示原始键名而非 "虚空回响" |
| **修复建议** | 将 zh_cn.json:78 的键改为 `"itemGroup.void_echo_tab"` |

### #11 噪声设置 `final_density` 梯度超出维度实际高度

| 项目 | 内容 |
|------|------|
| **位置** | `worldgen/noise_settings/voids_end.json:114-119` |
| **具体差异** | `y_clamped_gradient` 设置 `to_y: 192`，但维度 `min_y=0, height=128`（有效 Y 范围 0~127） |
| **实际表现** | 128 以上被截断，但 y=16~128 范围内的梯度形状仍受 192 设计参数影响。地形侵蚀在有效范围内的权重分布可能与原始意图不同——暗示噪声参数最初为 height=256 设计，未针对 height=128 重新调整 |
| **修复建议** | 将 `to_y` 改为 `128`，并重新评估 `from_value` 和 `to_value` 以匹配实际世界高度 |

### #12 find_all_biomes.json 使用非标准 entity_properties 数组格式

| 项目 | 内容 |
|------|------|
| **位置** | `data/void_echo/advancements/find_all_biomes.json:13-36` |
| **具体差异** | `player` 字段使用数组包装：`[{"condition": "minecraft:entity_properties", "entity": "this", "predicate": ...}]`。同类型的 `void_explorer.json` 使用标准对象格式 |
| **实际表现** | 如果 1.21.1 的 `minecraft:location` trigger 解析器不识别数组形式的 EntityPredicate，该进度可能永远不会触发 |
| **修复建议** | 参考 `void_explorer.json` 使用标准格式：`"player": {"location": {"biomes": "void_echo:void_plains"}}` |

---

## 四、性能与兼容性风险

### #13 LivingEntityMixin 每 tick 重新应用护甲效果

| 项目 | 内容 |
|------|------|
| **位置** | `mixin/LivingEntityMixin.java:68-91` |
| **判断依据** | `@Inject(method = "tick", at = @At("HEAD"))` 对每个 LivingEntity 每 tick 执行。即使已穿戴全套虚空护甲，SLOW_FALLING（60 tick 持续）和 FIRE_RESISTANCE（100 tick 持续）仍每 tick 重新 apply。效果图标闪烁、不必要的 StatusEffectInstance 创建 |
| **修复建议** | 参照 EchoAmuletItem 的 `applyIfNeeded` 模式——当剩余时间 > 30 tick 时跳过重新 apply |

### #14 每次伤害事件复制武器 NBT（无必要的内存分配）

| 项目 | 内容 |
|------|------|
| **位置** | `mixin/EnchantmentEffectMixin.java:153`；`mixin/LivingEntityMixin.java:188` |
| **判断依据** | 每次任意实体受到伤害，`damage()` 的 `@ModifyVariable` 都执行 `weapon.getOrDefault(CUSTOM_DATA, ...).copyNbt()`，即使武器没有任何虚空锻造升级。创建了大量临时 NbtCompound |
| **修复建议** | 先用 `weapon.contains(DataComponentTypes.CUSTOM_DATA)` 检查是否存在自定义数据，仅当存在时才 copyNbt |

### #15 CrystalBloomBlock.canPlaceAt 每次调用创建新 TagKey 对象

| 项目 | 内容 |
|------|------|
| **位置** | `block/CrystalBloomBlock.java:35-38` |
| **判断依据** | `TagKey.of(RegistryKeys.BLOCK, Identifier.of(...))` 在每次方块放置检查时创建新对象。TagKey 是不可变的，应提取为静态常量 |
| **修复建议** | 提取为 `private static final TagKey<Block> PLANTABLE_ON = TagKey.of(RegistryKeys.BLOCK, Identifier.of("void_echo", "crystal_bloom_plantable_on"))` |

### #16 VoidFortress 结构包围盒超出维度高度上限

| 项目 | 内容 |
|------|------|
| **位置** | `world/structure/VoidFortressStructure.java:66-68` |
| **判断依据** | `startPos.y = 60`，`maxY = 60 + 80 = 140`，超过维度限高 128。虽不导致崩溃，但包围盒跨越额外竖直区块，增加区块生成过程中需评估此结构部件的次数 |
| **修复建议** | 将 maxY 限制为 `Math.min(startPos.getY() + 80, 127)` |

---

## 五、已验证为误报 / 夸大的发现

以下条目来自子 Agent 报告，经人工交叉验证后确认为非 Bug 或严重程度被夸大：

| 原 Agent 报告 | 实际验证结果 | 结论 |
|---------------|-------------|------|
| "VoidBoltEntity 没有渲染器—弹射物不可见" | `VoidEchoClient.java:22` 已通过 `EntityRendererRegistry.register(ModEntities.VOID_BOLT, FlyingItemEntityRenderer::new)` 注册 | **误报** |
| "所有 3 个结构箱子战利品表缺失—箱子永久为空" | 3 个 JSON 文件均在 `loot_table/chests/` 下确认为存在、格式正确 | **误报** |
| "CrystalWraith 完全无法攻击 2 格内目标" | 有 1.5-2.0 格盲区但浮空 AI 维持高度差，极罕见。已在 #8 降级为低严重度 | **夸大** |
| "void_master 进度使用 impossible trigger—永远无法完成" | `VoidMasterHandler.java` 通过 `tracker.grantCriterion()` 手动授予—这是代码授予的标准模式 | **误报** |
| "VoidForgeBlock remove() null 第三参数必定 NPE" | Fabric API `PlayerInventory.remove(Predicate, int, @Nullable PlayerEntity)` 官方支持 null。已在 #7 降级为 API 依赖风险 | **夸大** |
| "CrystalWraith 没有 MeleeAttackGoal—完全无法战斗" | 通过 `CrystalWraithDiveGoal` 俯冲战斗，设计如此。已在 #8 降级为盲区 | **夸大** |

---

## 六、综合评价

### 模组核心功能完整度：**部分可用**

核心游玩路径可基本走通：挖矿 → 合成虚空钥匙 → 传送虚空维度 → 探索 → Boss 战 → 终局装备。但有 3 个严重 Bug 影响核心功能可靠性。

### 各维度评估：

| 维度 | 评分 | 说明 |
|------|------|------|
| 架构与注册流 | ★★★★☆ | Fabric API 1.21.1 规范使用正确，注册顺序合理，无过时 API |
| Mixin 设计 | ★★★★★ | 两个 Mixin 优先级交互经过深思熟虑并有注释文档；无 @Overwrite |
| 客户端/服务端隔离 | ★★★★☆ | @Environment 注解正确，入口点分离清晰 |
| 资源完整性 | ★★★★☆ | 纹理、模型、语言键基本完整，仅 2 个键名不一致 |
| 世界生成 | ★★★☆☆ | 功能可用但有参数未调整（噪声高度、特征放置方式） |
| 测试覆盖 | ★☆☆☆☆ | 无测试代码 |
| 代码质量 | ★★★★☆ | 整体清晰、良好注释的关键 Mixin 交互 |

### 修复优先级：

| 优先级 | 数量 | 条目 |
|--------|------|------|
| 🔴 高（须立即修复） | 3 个 | #1 VoidForge 副作用、#3 PortalStorage 数据丢失、#4 AI Goal 冲突 |
| 🟡 中（影响体验） | 5 个 | #5 特征 Y 定位、#6 客户端动画、#7 API 依赖、#9 声音字幕、#10 zh_cn 键名 |
| 🟢 低（改进建议） | 4 个 | #11 噪声梯度、#12 进度格式、#13~#16 性能 |

---

*报告生成方式：完整源码扫描（70+ Java 文件 + 190+ JSON 资源文件 + 55+ 纹理）→ 5 并行子 Agent 逐模块深度审计 → 人工交叉验证所有发现并分类汇总*
