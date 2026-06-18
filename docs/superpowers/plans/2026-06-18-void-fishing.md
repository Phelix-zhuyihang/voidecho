# Void Fishing — 虚空诱捕系统 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:subagent-driven-development or superpowers:executing-plans

**Goal:** 终虚渊虚空诱捕 — 暗晶钓竿向虚空雾气抛浮标 → 等待随机时间收竿 → 群系战利品 + 暗晶蟹偷袭 + T4锻炉升级

**Architecture:** VoidLureItem 右键生成 VoidLureBobberEntity（独立 Entity，非 FishingBobberEntity 子类——避免水逻辑耦合）。浮标悬浮空中，服务端计时器满后收竿 roll 群系 loot table。5% 额外生成 VoidCrabEntity。EnchantmentEffectMixin 扩展 T4。

**Tech Stack:** Fabric 1.21.1 · Yarn build.3 · Fabric API

---

## 文件结构

| 类型 | 文件 | 职责 |
|------|------|------|
| 新建 | `item/VoidLureItem.java` | 钓竿物品，右键抛浮标 / 再右键或潜行右键收竿 |
| 新建 | `entity/VoidLureBobberEntity.java` | 浮标实体（extends Entity），悬浮 + 计时器 + 群系 loot |
| 新建 | `entity/VoidCrabEntity.java` | 暗晶蟹，钓起逃跑型小型敌怪 |
| 新建 | `entity/client/model/VoidCrabModel.java` | 暗晶蟹 EntityModel |
| 新建 | `entity/client/renderer/VoidCrabRenderer.java` | 暗晶蟹 EntityRenderer |
| 修改 | `item/ModItems.java` | 注册 7 个新物品 |
| 修改 | `entity/ModEntities.java` | 注册 2 个新实体 |
| 修改 | `VoidEchoClient.java` | 注册 VoidCrabRenderer |
| 修改 | `block/VoidForgeBlock.java` | T4 共鸣水晶升级 |
| 修改 | `mixin/EnchantmentEffectMixin.java` | T4 水晶谐振效果 |
| 修改 | `item/ModItemGroups.java` | 物品栏加新物品 |
| 新建 | 4× loot_table JSON | 四群系钓鱼战利品 |
| 新建 | 6× recipe JSON | 钓竿合成 + 烹饪 + 兑换 |
| 新建 | 6× model JSON | 物品模型 |
| 修改 | `lang/en_us.json` + `zh_cn.json` | +14 keys 各 |
| 新建 | 6× texture PNG | 16×16 Python 生成 |

---

### Task 1: 注册物品 + 贴图 + 模型 + Lang

**Files:** Modify `ModItems.java`, `ModItemGroups.java`, lang files; Create 6 model JSONs, 6 PNGs

- [ ] **Step 1: ModItems.java 添加 7 个物品**

在 `CRYSTAL_HOE` 注册之后插入：

```java
// ---- Void Fishing ----
public static final Item CRYSTAL_LURE = register("crystal_lure",
        new Item(new Item.Settings().maxDamage(128).rarity(Rarity.UNCOMMON)));

public static final Item VOID_CARP = register("void_carp",
        new Item(new Item.Settings().food(
                new FoodComponent.Builder().nutrition(3).saturationModifier(0.6f).build())));

public static final Item COOKED_VOID_CARP = register("cooked_void_carp",
        new Item(new Item.Settings().food(
                new FoodComponent.Builder().nutrition(5).saturationModifier(1.2f).build())));

public static final Item CRYSTAL_RAY = register("crystal_ray",
        new Item(new Item.Settings().food(
                new FoodComponent.Builder().nutrition(2).saturationModifier(0.4f)
                        .statusEffect(new StatusEffectInstance(
                                net.minecraft.entity.effect.StatusEffects.REGENERATION, 600, 0), 1.0f)
                        .build())));

public static final Item VOID_CRAB_SHELL = register("void_crab_shell",
        new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));

public static final Item RESONANT_CRYSTAL = register("resonant_crystal",
        new Item(new Item.Settings().rarity(Rarity.RARE).fireproof()));

public static final Item AEROLITH_FRAGMENT = register("aerolith_fragment",
        new Item(new Item.Settings().rarity(Rarity.RARE)));
```

需要 import: `net.minecraft.entity.effect.StatusEffects`, `net.minecraft.entity.effect.StatusEffectInstance`

- [ ] **Step 2: ModItemGroups.java 添加物品到创造物品栏**

在 Crystal Tools 段后：
```java
// Void Fishing
entries.add(ModItems.CRYSTAL_LURE);
entries.add(ModItems.VOID_CARP);
entries.add(ModItems.COOKED_VOID_CARP);
entries.add(ModItems.CRYSTAL_RAY);
entries.add(ModItems.VOID_CRAB_SHELL);
entries.add(ModItems.RESONANT_CRYSTAL);
entries.add(ModItems.AEROLITH_FRAGMENT);
```

- [ ] **Step 3: 生成 6 张贴图**

```bash
python -c "
from PIL import Image
import os, math
BASE = r'E:\phelix\void_echo_mod\src\main\resources\assets\void_echo\textures\item'
P = {
    'darkest':(14,2,20,255),'shadow':(26,6,40,255),'mid_dark':(45,15,69,255),
    'main':(69,32,104,255),'light':(92,45,133,255),'highlight':(123,64,168,255),
    'crystal':(155,95,192,255),'core':(184,120,216,255),'energy':(212,144,240,255),
    'glow':(232,192,255,255),'white':(240,224,255,255),
}

def make_lure():
    img=Image.new('RGBA',(16,16),(0,0,0,0));px=img.load()
    for i in range(14): px[i,int(i*0.7+1)]=P['mid_dark']; px[i,int(i*0.7+2)]=P['main']
    for y in range(0,4):
        for x in range(11,15):
            d=abs(x-13)+abs(y-1.5)
            if d<2: px[x,y]=P['glow'] if d<1 else P['crystal']
    px[13,1]=P['white']
    for i in range(4):
        for dy in range(2): px[i,int(i*0.7)+dy]=P['shadow']
    img.save(os.path.join(BASE,'crystal_lure.png')); print('crystal_lure saved')

def make_fish(kind):
    img=Image.new('RGBA',(16,16),(0,0,0,0));px=img.load()
    c1,c2,c3={'void_carp':(P['main'],P['crystal'],P['glow']),
              'crystal_ray':(P['crystal'],P['core'],P['energy'])}[kind]
    for y in range(4,13):
        wy=3-abs(y-8)*0.4
        for x in range(int(8-wy),int(8+wy+1)):
            if 0<=x<16:
                edge=int(abs(x-7.5))>int(wy-1) or y in(4,12)
                px[x,y]=c2 if edge else(c1 if(x+y)%2==0 else c3)
    px[10,7]=P['white']; px[10,8]=P['white']
    for dy in range(3): px[4-dy,8-dy]=c1; px[4-dy,9+dy]=c1
    img.save(os.path.join(BASE,f'{kind}.png')); print(f'{kind} saved')

def make_shell():
    img=Image.new('RGBA',(16,16),(0,0,0,0));px=img.load()
    for y in range(4,14):
        wy=4-abs(y-9)*0.3
        for x in range(int(8-wy),int(8+wy+1)):
            if 0<=x<16:
                if abs(x-7.5)>wy-1: px[x,y]=P['light']
                else: px[x,y]=P['main'] if(x+y)%3 else P['mid_dark']
    for y in range(5,13,2):
        for x in range(5,11): px[x,y]=P['crystal'] if x%2==0 else px[x,y]
    img.save(os.path.join(BASE,'void_crab_shell.png')); print('crab_shell saved')

def make_crystal():
    img=Image.new('RGBA',(16,16),(0,0,0,0));px=img.load()
    for y in range(16):
        for x in range(16):
            d=math.sqrt((x-7.5)**2+(y-7.5)**2)
            swirl=math.sin(d*2.5+math.atan2(y-7.5,x-7.5)*3)
            if d<6:
                if swirl>0.5: px[x,y]=P['energy']
                elif swirl>0: px[x,y]=P['crystal']
                else: px[x,y]=P['core']
    px[7,7]=P['white']; px[8,8]=P['glow']
    for a in range(0,360,45):
        ax=int(7.5+math.cos(math.radians(a))*5)
        ay=int(7.5+math.sin(math.radians(a))*5)
        if 0<=ax<16 and 0<=ay<16: px[ax,ay]=P['glow']
    img.save(os.path.join(BASE,'resonant_crystal.png')); print('resonant_crystal saved')

def make_fragment():
    img=Image.new('RGBA',(16,16),(0,0,0,0));px=img.load()
    for y in range(16):
        for x in range(16):
            d=math.sqrt((x-7.5)**2+(y-6.5)**2)
            angle=math.atan2(y-6.5,x-7.5)
            jagged=math.sin(angle*6)*1.5+5
            if d<jagged:
                lvl=(math.sin(d*2)+1)*0.5
                if lvl>0.7: px[x,y]=P['core']
                elif lvl>0.4: px[x,y]=P['crystal']
                else: px[x,y]=P['main']
    for y in range(16):
        for x in range(16):
            if px[x,y]!=(0,0,0,0):
                for dx,dy in[(-1,0),(1,0),(0,-1),(0,1)]:
                    nx,ny=x+dx,y+dy
                    if 0<=nx<16 and 0<=ny<16 and px[nx,ny]==(0,0,0,0): px[x,y]=P['energy']
    px[7,5]=P['white']; px[8,6]=P['glow']
    img.save(os.path.join(BASE,'aerolith_fragment.png')); print('aerolith_fragment saved')

make_lure(); make_fish('void_carp'); make_fish('crystal_ray')
make_shell(); make_crystal(); make_fragment()
print('Done')
"
```

- [ ] **Step 4: 创建 6 个物品模型 JSON**

`crystal_lure.json` → `"parent": "minecraft:item/handheld"`，其余 `"parent": "minecraft:item/generated"`，全部 texture layer0 指向 `void_echo:item/<name>`

- [ ] **Step 5: 添加 lang 条目**

en_us.json 在 crystal_hoe 后：
```json
"item.void_echo.crystal_lure": "Crystal Lure",
"item.void_echo.void_carp": "Void Carp",
"item.void_echo.cooked_void_carp": "Cooked Void Carp",
"item.void_echo.crystal_ray": "Crystal Ray",
"item.void_echo.void_crab_shell": "Void Crab Shell",
"item.void_echo.resonant_crystal": "Resonant Crystal",
"item.void_echo.aerolith_fragment": "Aerolith Fragment",
```

zh_cn.json 同上位置：
```json
"item.void_echo.crystal_lure": "暗晶钓竿",
"item.void_echo.void_carp": "虚空鲤",
"item.void_echo.cooked_void_carp": "熟虚空鲤",
"item.void_echo.crystal_ray": "水晶鳐",
"item.void_echo.void_crab_shell": "暗晶蟹壳",
"item.void_echo.resonant_crystal": "共鸣水晶",
"item.void_echo.aerolith_fragment": "艾洛斯残片",
```

- [ ] **Step 6: 编译 + 提交**

```bash
gradle build && git add <all> && git commit -m "feat(fishing): 7 new items — lure, fish, crab shell, resonant crystal, aerolith fragment"
```

---

### Task 2: VoidLureItem + VoidLureBobberEntity

**Files:** Create `VoidLureItem.java`, `VoidLureBobberEntity.java`; Modify `ModItems.java`, `ModEntities.java`

- [ ] **Step 1: VoidLureItem.java**

```java
package com.example.voidecho.item;

import com.example.voidecho.entity.VoidLureBobberEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class VoidLureItem extends Item {
    public VoidLureItem(Settings settings) { super(settings); }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        // Check if player already has an active bobber (tracked via vehicle as marker)
        if (user.getVehicle() instanceof VoidLureBobberEntity bobber) {
            if (!world.isClient) {
                bobber.tryReelIn();
                stack.damage(1, user, hand == Hand.MAIN_HAND
                        ? net.minecraft.entity.EquipmentSlot.MAINHAND
                        : net.minecraft.entity.EquipmentSlot.OFFHAND);
            }
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.PLAYERS, 0.5f, 0.6f);
            return TypedActionResult.success(stack);
        }
        // Cast
        if (!world.isClient) {
            VoidLureBobberEntity bobber = new VoidLureBobberEntity(world, user);
            world.spawnEntity(bobber);
            user.startRiding(bobber);
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_FISHING_BOBBER_THROW, SoundCategory.PLAYERS, 0.5f, 0.6f);
        }
        return TypedActionResult.success(stack);
    }
}
```

- [ ] **Step 2: 更新 CRYSTAL_LURE 使用 VoidLureItem**

```java
public static final Item CRYSTAL_LURE = register("crystal_lure",
        new VoidLureItem(new Item.Settings().maxDamage(128).rarity(Rarity.UNCOMMON)));
```

- [ ] **Step 3: VoidLureBobberEntity.java**

```java
package com.example.voidecho.entity;

import com.example.voidecho.ModParticleTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.List;

public class VoidLureBobberEntity extends Entity {
    private static final TrackedData<Integer> OWNER_ID = DataTracker.registerData(
            VoidLureBobberEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private static final int MIN_WAIT = 100;
    private int ticksAlive = 0;

    public VoidLureBobberEntity(EntityType<?> type, World world) { super(type, world); }

    public VoidLureBobberEntity(World world, PlayerEntity owner) {
        super(ModEntities.VOID_LURE_BOBBER, world);
        this.dataTracker.set(OWNER_ID, owner.getId());
        Vec3d lookPos = owner.getCameraPosVec(1.0f).add(owner.getRotationVec(1.0f).multiply(4.0));
        this.setPosition(lookPos.x, owner.getEyeY() + 0.5, lookPos.z);
        this.setVelocity(owner.getRotationVec(1.0f).multiply(0.6));
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) { builder.add(OWNER_ID, -1); }

    @Override
    public void tick() {
        super.tick();
        PlayerEntity owner = getOwner();
        if (owner == null) { this.discard(); return; }
        if (ticksAlive > 10) this.setVelocity(Vec3d.ZERO);
        // Hover oscillation
        if (ticksAlive > 10 && !this.getWorld().isClient) {
            this.setPosition(this.getX(), this.getY() + Math.sin(ticksAlive * 0.05) * 0.03, this.getZ());
        }
        // VOID_AMBIENT particles
        if (this.getWorld().isClient && ticksAlive > 10 && this.random.nextFloat() < 0.3f) {
            this.getWorld().addParticle(ModParticleTypes.VOID_AMBIENT,
                    this.getX() + this.random.nextGaussian() * 0.2,
                    this.getY() + 0.2, this.getZ() + this.random.nextGaussian() * 0.2, 0, 0.03, 0);
        }
        if (!this.getWorld().isClient) {
            if (owner.getWorld() != this.getWorld() || this.squaredDistanceTo(owner) > 100.0) {
                this.discard();
            }
        }
        ticksAlive++;
    }

    public void tryReelIn() {
        if (this.getWorld().isClient || ticksAlive < MIN_WAIT) { this.discard(); return; }
        PlayerEntity owner = getOwner();
        if (owner == null) { this.discard(); return; }
        ServerWorld sw = (ServerWorld) this.getWorld();
        Identifier biomeId = sw.getBiome(this.getBlockPos()).getKey().orElse(null);
        String lootPath = getLootTableForBiome(biomeId);
        RegistryKey<net.minecraft.loot.LootTable> lootKey = RegistryKey.of(
                RegistryKeys.LOOT_TABLE, Identifier.of("void_echo", lootPath));
        net.minecraft.loot.context.LootContextParameterSet params =
                new net.minecraft.loot.context.LootContextParameterSet.Builder(sw)
                        .add(net.minecraft.loot.context.LootContextParameters.ORIGIN, this.getPos())
                        .add(net.minecraft.loot.context.LootContextParameters.THIS_ENTITY, owner)
                        .luck(owner.getLuck())
                        .build(net.minecraft.loot.context.LootContextTypes.FISHING);
        net.minecraft.loot.LootTable table = sw.getServer()
                .getReloadableRegistries().getLootTable(lootKey);
        List<ItemStack> loot = table.generateLoot(params);
        for (ItemStack drop : loot) {
            if (!owner.getInventory().insertStack(drop)) owner.dropItem(drop, false);
        }
        // 5% Void Crab ambush
        if (owner.getRandom().nextFloat() < 0.05f) {
            VoidCrabEntity crab = new VoidCrabEntity(ModEntities.VOID_CRAB, this.getWorld());
            crab.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(),
                    this.random.nextFloat() * 360f, 0);
            this.getWorld().spawnEntity(crab);
        }
        this.discard();
    }

    private String getLootTableForBiome(Identifier biomeId) {
        if (biomeId == null) return "gameplay/fishing/void_plains";
        String p = biomeId.getPath();
        if (p.contains("crystal_forest")) return "gameplay/fishing/crystal_forest";
        if (p.contains("void_wastes")) return "gameplay/fishing/void_wastes";
        if (p.contains("crystal_caverns")) return "gameplay/fishing/crystal_caverns";
        return "gameplay/fishing/void_plains";
    }

    private PlayerEntity getOwner() {
        int id = this.dataTracker.get(OWNER_ID);
        return id < 0 ? null : (PlayerEntity) this.getWorld().getEntityById(id);
    }

    @Override protected void readCustomDataFromNbt(NbtCompound nbt) { ticksAlive = nbt.getInt("ticks_alive"); }
    @Override protected void writeCustomDataToNbt(NbtCompound nbt) { nbt.putInt("ticks_alive", ticksAlive); }
}
```

- [ ] **Step 4: ModEntities.java 注册浮标实体**

```java
public static final EntityType<VoidLureBobberEntity> VOID_LURE_BOBBER = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(VoidEcho.MOD_ID, "void_lure_bobber"),
        EntityType.Builder.<VoidLureBobberEntity>create(
                (type, world) -> new VoidLureBobberEntity(type, world),
                net.minecraft.entity.SpawnGroup.MISC)
                .dimensions(0.25f, 0.25f).maxTrackingRange(4).trackingTickInterval(5)
                .build("void_lure_bobber"));
```

- [ ] **Step 5: 编译 + 提交**

---

### Task 3: VoidCrabEntity + 模型 + 渲染器

**Files:** Create `VoidCrabEntity.java`, `VoidCrabModel.java`, `VoidCrabRenderer.java`; Modify `ModEntities.java`, `VoidEchoClient.java`

- [ ] **Step 1: VoidCrabEntity.java**

```java
package com.example.voidecho.entity;

import com.example.voidecho.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class VoidCrabEntity extends HostileEntity {
    private int fleeTimer = 200;

    public VoidCrabEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world); this.experiencePoints = 5;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new FleeGoal<>(this, PlayerEntity.class, 8.0f, 1.4, 1.6));
        this.goalSelector.add(2, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(3, new LookAroundGoal(this));
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            PlayerEntity target = this.getWorld().getClosestPlayer(this, 12.0);
            if (target != null && target.isAlive()) fleeTimer = 200;
            if (fleeTimer <= 0) this.discard();
            fleeTimer--;
        }
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (!this.getWorld().isClient) {
            this.dropStack(new net.minecraft.item.ItemStack(ModItems.VOID_CRAB_SHELL,
                    1 + this.random.nextInt(2)));
        }
    }
}
```

- [ ] **Step 2: VoidCrabModel.java** — 简单 EntityModel，用 16×16×8 主体 box + 4 条小支柱腿

- [ ] **Step 3: VoidCrabRenderer.java** — extends MobEntityRenderer<VoidCrabEntity, VoidCrabModel>

- [ ] **Step 4: ModEntities.java 注册**

```java
public static final EntityType<VoidCrabEntity> VOID_CRAB = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(VoidEcho.MOD_ID, "void_crab"),
        EntityType.Builder.create(VoidCrabEntity::new, net.minecraft.entity.SpawnGroup.MONSTER)
                .dimensions(0.6f, 0.4f).maxTrackingRange(4).trackingTickInterval(20)
                .build("void_crab"));
```

- [ ] **Step 5: VoidEchoClient.java 注册渲染器**
- [ ] **Step 6: 生成 void_crab.png 贴图 (16×16)**
- [ ] **Step 7: 编译 + 提交**

---

### Task 4: 钓鱼战利品表 (4 JSON)

**Files:** Create `data/void_echo/loot_table/gameplay/fishing/void_plains.json` 等 4 个文件

- [ ] **Step 1: void_plains.json**
```json
{
  "type": "minecraft:fishing",
  "pools": [{
    "rolls": 1,
    "entries": [
      {"type": "minecraft:item", "name": "void_echo:void_carp", "weight": 60},
      {"type": "minecraft:item", "name": "void_echo:crystal_shard", "weight": 25,
       "functions": [{"function": "minecraft:set_count", "count": 2}]},
      {"type": "minecraft:item", "name": "void_echo:crystal_ray", "weight": 10},
      {"type": "minecraft:item", "name": "void_echo:aerolith_fragment", "weight": 5}
    ]
  }]
}
```

- [ ] **Step 2-4: crystal_forest / void_wastes / crystal_caverns** — 同样结构，按规格替换条目+权重
- [ ] **Step 5: 编译 + 提交**

---

### Task 5: 合成配方 (6 JSON)

**Files:** Create 6 recipe JSONs

- [ ] crystal_lure.json (crafting_shaped — 暗晶碎片 + 木棍三角排列)
- [ ] cooked_void_carp.json (smelting — void_carp → cooked_void_carp)
- [ ] cooked_crystal_ray.json (smelting — crystal_ray → 熟食)
- [ ] void_alloy_fragment_from_crab.json (crafting_shapeless — 蟹壳×3 → 虚空合金碎片×1)
- [ ] void_carp_from_crystal_ray.json (可选)
- [ ] 提交

---

### Task 6: T4 锻炉升级 — 水晶谐振

**Files:** Modify `VoidForgeBlock.java`, `EnchantmentEffectMixin.java`

- [ ] **Step 1: VoidForgeBlock — 添加 T4 升级分支**

在 use() 方法中 T3 升级之后插入：

```java
// T4: Crystal Resonance (weapon only, requires prior echo or rift T1)
if (offHand.isOf(ModItems.RESONANT_CRYSTAL)
        && (nbt.contains("void_echo:echo_upgrade", NbtElement.BYTE_TYPE)
            || nbt.contains("void_echo:rift_upgrade", NbtElement.BYTE_TYPE))
        && !nbt.contains("void_echo:crystal_resonance", NbtElement.BYTE_TYPE)) {
    nbt.putBoolean("void_echo:crystal_resonance", true);
    appliedUpgrade[0] = "crystal_resonance";
    return NbtComponent.of(nbt);
}
```

- [ ] **Step 2: EnchantmentEffectMixin — 20% 链式打击**

在 modifyDamage 方法中，TAIL inject 后添加：

```java
NbtCompound nbt = stack.get(DataComponentTypes.CUSTOM_DATA).copyNbt();
if (nbt != null && nbt.getBoolean("void_echo:crystal_resonance")
        && self.getRandom().nextFloat() < 0.2f) {
    LivingEntity chain = null;
    double nearest = 25.0;
    for (LivingEntity e : target.getWorld().getEntitiesByClass(LivingEntity.class,
            target.getBoundingBox().expand(5.0), e -> e != target && e.isAlive() && e != self)) {
        double d = target.squaredDistanceTo(e);
        if (d < nearest) { nearest = d; chain = e; }
    }
    if (chain != null) {
        chain.damage(target.getDamageSources().magic(), 6.0f);
    }
}
```

- [ ] **Step 3: 编译 + 提交**

---

### Task 7: 整理 + 最终验证

- [ ] Step 1: 确认所有 14 个 lang key 在 en_us.json 和 zh_cn.json 中存在
- [ ] Step 2: 确认 6 张贴图存在、尺寸 16×16
- [ ] Step 3: 确认 6 个模型 JSON valid
- [ ] Step 4: 确认 4 个战利品表 valid
- [ ] Step 5: 确认 6 个配方 valid
- [ ] Step 6: `gradle build` → BUILD SUCCESSFUL
- [ ] Step 7: `git status` → clean staged, commit
