# void_echo_mod — Minecraft 虚空回响模组

## Skill 路由规则

以下场景**必须**先调用对应 Skill 再动手。不要跳过。

### 美术资产
| 场景 | 必须先调 |
|------|----------|
| 做方块模型、物品模型、实体模型 | `blockbench-use` |
| 画贴图、纹理、UV | `blockbench-use` |
| 做生物动画（走/跑/攻击/待机/死亡） | `blockbench-use` |
| 做粒子贴图 | `blockbench-texturing` |
| 像素艺术审美、色板设计、风格参考 | `pixel-art-sprites` |
| 粒子效果视觉设计（形状/节奏/色彩） | `vfx-realtime` |

### 代码
| 场景 | 必须先调 |
|------|----------|
| 添加方块/物品/实体/配方/命令/GUI/维度 | `minecraft-modding` |
| 写 Mixin / 反编译 / Fabric 移植 | `minecraft-fabric-dev` |
| 实现粒子效果（ParticleType 代码） | `minecraft-modding` |
| 写新功能（任何代码改动） | `brainstorming` → `test-driven-development` |

### 世界观
| 场景 | 必须先调 |
|------|----------|
| 虚空维度设定、魔法系统、生态设计 | `worldbuilding` |
| 背景故事、神秘感、叙事结构 | `lore-building` |

### 代码质量
| 场景 | 必须先调 |
|------|----------|
| 遇到 Bug | `systematic-debugging` |
| 开发完成准备提交 | `verification-before-completion` → `code-review` |
| 收到 Review 意见 | `receiving-code-review` |

### 教材/文档
| 场景 | 必须先调 |
|------|----------|
| 写教学 PPT | `slidev` |
| 写练习题 | `quiz-generator` |

### 其他
| 场景 | 必须先调 |
|------|----------|
| 跨 Claude Code 窗口协作 | 用 `mcp__claude-coord-bridge` 工具 |
| 代码架构理解 | `codegraph_explore`（别用 grep） |

---

## 核心原则
- 调 `blockbench-use` 是进 Blockbench 的唯一入口，它会自动分发到 modeling/texturing/animation
- 审美技能（pixel-art-sprites / vfx-realtime）负责"做成什么样好看"
- 工具技能（blockbench-* / minecraft-*）负责"怎么做出来"
- 两个要一起用，不能只用工具技能不管审美

## 自主推进规则

**每完成一个任务后，必须做以下三件事：**
1. 分析当前状态 — 快速回顾刚才做了什么
2. 提出下一步 — 逻辑上的自然后续是什么
3. 自动调用对应 Skill — 按路由表选取，不要等用户说

**逻辑链示例：**
- 做完方块模型 → 自然需要贴图 → `blockbench-texturing`
- 写完粒子代码 → 需要粒子贴图 → `blockbench-texturing` 画贴图
- 贴图画完 → 要注册到游戏 → `minecraft-modding`
- 代码写完 → 该提交 → `verification-before-completion` → `code-review`

**每轮回复结尾必须提议下一步：**
> 下一步：[具体任务] → `[skill-name]`，继续？
