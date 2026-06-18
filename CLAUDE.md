# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 构建命令

```bash
./gradlew build          # 编译并打包 JAR
./gradlew runClient      # 启动 Minecraft 客户端进行测试
./gradlew runServer      # 启动专用服务器进行测试
```

输出 JAR：`build/libs/carpet-bcl-addition-1.0.0.jar`

暂无测试。需要 Java 21。

## 架构

这是一个 Minecraft 1.21 Fabric 的 **Carpet mod 扩展**。Carpet 扩展通过 `CarpetExtension` 接口注册 `@Rule` 注解的设置项，玩家使用 `/carpet <规则名> true/false` 切换。

```
BCLAdditionMod (ModInitializer)
  └── BCLAdditionExtension (CarpetExtension)
        ├── onGameStarted() → 注册 BCLAdditionSettings + BCLAdditionOrgSettings
        ├── canHasTranslations(lang) → 从 assets/carpet-bcl-addition/lang/ 加载 JSON
        └── 设置注册到 CarpetServer.settingsManager

Mixin（共 10 个，均在 mixins JSON 中注册）:
  末地传送门框架（4 个）:
    EndPortalFrameBlockMixin (@Mixin AbstractBlock)
      └── 注入 getHardness() → 规则启用时返回硬度
    EndPortalFrameBlockStateMixin (@Mixin AbstractBlock.AbstractBlockState)
      └── 注入 getHardness(BlockView, BlockPos) → 挖掘时实际调用的方法，同时处理 softNetherite
    EndPortalFrameBlockDropMixin (@Mixin Block)
      └── 注入 afterBreak() TAIL → 规则启用时强制掉落方块物品
    EndPortalFrameMiningSpeedMixin (@Mixin ItemStack)
      └── 注入 getMiningSpeedMultiplier() → 提供正确的镐挖掘速度
  二期新增（6 个）:
    SoftNetheriteMixin (@Mixin AbstractBlock)
      └── 注入 getHardness() → 下界合金/远古残骸硬度降为 1/18
    DisableWindChargeEffectMixin (@Mixin WindChargeEntity)
      └── 注入 createExplosion() HEAD 取消 → 阻止风弹改方块
    ChannelingIgnoreConditionsMixin (@Mixin TridentEntity)
      └── 注入 onBlockHit+onEntityHit TAIL → 无天气也可引雷
    RiptideIgnoreConditionsMixin (@Mixin TridentItem)
      └── @WrapOperation 拦截 isTouchingWaterOrRain() → 跳过激流的水/雨检查
    ForceOpenContainerMixin (@Mixin ShulkerBoxBlock)
      └── 注入 canOpen() HEAD 取消 → 强制打开被阻潜影盒
    OpenPlayerInventoryMixin (@Mixin PlayerEntity)
      └── 注入 interact() HEAD → 潜行右键打开玩家背包

Condition: CarpetOrgRequired implements Rule.Condition
  └── shouldRegister() → FabricLoader.isModLoaded("carpet-org-addition") && version >= 1.41.5
```

### Settings 类拆分

- `BCLAdditionSettings` — 7 个独立规则（mineableEndPortalFrame + 6 个二期规则），始终注册
- `BCLAdditionOrgSettings` — 4 个依赖规则，带 `conditions = {CarpetOrgRequired.class}`，仅在 Carpet-Org >= v1.41.5 时可用

### 数据文件

```
data/minecraft/loot_table/blocks/end_portal_frame.json    → 掉落表
data/minecraft/tags/blocks/mineable/pickaxe.json           → 镐为正确工具（⚠️ 未加载）
data/minecraft/tags/blocks/needs_iron_tool.json            → 需要铁镐或以上（⚠️ 未加载）
```

### 关键模式

- **规则注册**：`CarpetServer.settingsManager.parseSettingsClass(BCLAdditionSettings.class)` 在 `onGameStarted()` 中调用。规则字段必须是 `public static` 且非 final，用 `@Rule` 注解。
- **自定义分类**：声明为 `public static final String BCL = "BCL"`，在 `@Rule(categories = {...})` 中引用。
- **Mixin 目标**：`getHardness()` 的 Mixin 必须 target `AbstractBlock.class`（不能用 `Block.class`）——Mixin 注解处理器无法解析 Block 上继承的方法。
- **⚠️ 关键陷阱**：挖掘流程调用的不是 `AbstractBlock.getHardness()`（无参），而是 `AbstractBlock.AbstractBlockState.getHardness(BlockView, BlockPos)`（带参）。后者直接读取 `Block.Settings` 里的 hardness **字段**，完全绕过无参方法。所以修改方块硬度的功能必须**两个都注入**，否则挖掘时不会生效。
- **Condition 机制**：实现 `Rule.Condition` 接口的 `shouldRegister()` 方法，在 `@Rule(conditions = {...})` 中引用。返回 false 则该规则不会被注册。
- **@WrapOperation 注入**：MixinExtras 的 `@WrapOperation` 用于拦截**调用点**（而非目标方法本身），通过 intermediary 名称指定 target：`target = "Lnet/minecraft/...;methodName()Z"`。当目标方法在 Yarn 映射中不可见时（如 `isTouchingWaterOrRain`），这是唯一可行的注入方式。普通的 `@Inject(method = "...")` 需要 Mixin AP 在编译时解析方法，遇到 Yarn 未映射的方法会失败。
- **新 Mixin 必须在 mixins JSON 中注册**：仅放在正确的 package 目录下不够，必须在 `carpet-bcl-addition.mixins.json` 的 `"mixins"` 数组中显式列出。
- **翻译**：通过 `canHasTranslations(String lang)` 提供，读取 `assets/<modid>/lang/<lang>.json`。翻译 key 格式为 `carpet.rule.<规则名>.name` / `carpet.rule.<规则名>.desc` / `carpet.rule.<规则名>.extra.<N>` / `carpet.category.<分类名>`。

## Carpet API 版本

本项目使用 **Carpet 1.4.147**（CurseForge 文件 ID `5425253`，通过 Curse Maven 获取）。这是**旧版 API**：
- 使用 `carpet.settings.ParsedRule`（非 `carpet.api.settings.CarpetRule`）
- `@Rule` 注解**没有** `desc`/`extra` 属性——描述完全由 `canHasTranslations()` 提供
- **必须**实现 `CarpetExtension.canHasTranslations()`——否则 `parseSettingsClass` 会抛出 NPE

升级 Carpet 版本时，需确认 API 是否已变更（新版 API 有 `CarpetRule<T>`、带标识符的 `SettingsManager`、以及 `@Rule` 上的 `desc`/`extra` 属性）。

## 依赖来源

Carpet 通过 **Curse Maven**（`curse.maven:carpet-349239:<文件ID>`）获取，因为标准坐标 `carpet:fabric-carpet` 不在 Fabric Maven 上。修改 `gradle.properties` 中的 `carpet_file_id` 来更换 Carpet 版本。

## Mod 版本约束

`fabric.mod.json` 中 Minecraft 锁定为 `=1.21`（不含小版本）。Carpet 最低版本 `>=1.4.147`。

## 未来计划

升级 MC 1.21.2+ 后应添加的规则：
- `playerCommandSummonMannequin` — 召唤玩家模型。依赖 1.21.2+ 新增的 `Mannequin` 实体，参考 Carpet-Org 26.1 的 `PlayerCommandExtension.summonMannequin()` 实现。

## 已知问题

1. **⚠️ 数据包标签不加载**：`pickaxe.json` 和 `needs_iron_tool.json` 在 Fabric 开发环境中不加载。通过 Mixin 绕过。
2. **ChannelingIgnoreConditionsMixin 有未检查的类型转换**（编译器警告，不影响运行）。

## 本地参考项目

| 项目 | 用途 | 分支 |
|------|------|------|
| `Carpet-Org-Addition` | 规则名称和翻译的主要参考源 | `26.1`（最新）、`1.21` |
| `Carpet-TIS-Addition` | Mixin 代码风格和架构参考 | `master` |

**注意**：这些是只读参考项目，不要修改它们。
