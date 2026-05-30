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
        ├── onGameStarted() → 注册 BCLAdditionSettings
        ├── canHasTranslations(lang) → 从 assets/carpet-bcl-addition/lang/ 加载 JSON
        └── 设置注册到 CarpetServer.settingsManager

Mixin（共 4 个，均在 mixins JSON 中注册）:
  EndPortalFrameBlockMixin (@Mixin AbstractBlock)
    └── 注入 getHardness() → 规则启用时返回 50.0F（黑曜石硬度）
  EndPortalFrameBlockStateMixin (@Mixin AbstractBlock.AbstractBlockState)
    └── 注入 getHardness(BlockView, BlockPos) → 同上（挖掘时实际调用的方法）
  EndPortalFrameBlockDropMixin (@Mixin Block)
    └── 注入 afterBreak() TAIL → 规则启用时强制掉落方块物品
  EndPortalFrameMiningSpeedMixin (@Mixin ItemStack)
    └── 注入 getMiningSpeedMultiplier() → 绕过未加载的 PICKAXE_MINEABLE 标签，提供正确的镐挖掘速度
```

### 数据文件

```
data/minecraft/loot_table/blocks/end_portal_frame.json    → 掉落表
data/minecraft/tags/blocks/mineable/pickaxe.json           → 镐为正确工具（⚠️ 未加载）
data/minecraft/tags/blocks/needs_iron_tool.json            → 需要铁镐或以上（⚠️ 未加载）
```

**注意**：标签 JSON 文件存在于源码和构建产物中，但在 Fabric 开发环境运行时未被加载（`isIn(PICKAXE_MINEABLE)` 和 `isIn(NEEDS_IRON_TOOL)` 均返回 false）。目前通过 `EndPortalFrameMiningSpeedMixin` 和 `EndPortalFrameBlockDropMixin` 绕过。

### 关键模式

- **规则注册**：`CarpetServer.settingsManager.parseSettingsClass(BCLAdditionSettings.class)` 在 `onGameStarted()` 中调用。规则字段必须是 `public static` 且非 final，用 `@Rule` 注解。
- **自定义分类**：声明为 `public static final String BCL = "BCL"`，在 `@Rule(categories = {...})` 中引用。
- **Mixin 目标**：`getHardness()` 的 Mixin 必须 target `AbstractBlock.class`（不能用 `Block.class`）——Mixin 注解处理器无法解析 Block 上继承的方法。在注入器内部使用 `(Object) this instanceof EndPortalFrameBlock` 守卫。
- **⚠️ 关键陷阱**：挖掘流程调用的不是 `AbstractBlock.getHardness()`（无参），而是 `AbstractBlock.AbstractBlockState.getHardness(BlockView, BlockPos)`（带参）。后者直接读取 `Block.Settings` 里的 hardness **字段**，完全绕过了对 `getHardness()` 方法的调用。所以必须**两个都注入**才能生效——一个管挖掘裂痕，一个管其他代码路径。
- **挖掘速度修复**：MC 1.21 使用 `ItemStack → Item.getMiningSpeed → ToolComponent.getSpeed` 链条获取挖掘速度，通过检查 `PICKAXE_MINEABLE` 标签确定正确工具。由于数据标签不加载，新增 `EndPortalFrameMiningSpeedMixin` 注入 `ItemStack.getMiningSpeedMultiplier()`，用 `Blocks.STONE` 的默认状态作为代理调用 `ToolComponent.getSpeed()` 获取正确的镐挖掘速度。
- **新 Mixin 必须在 mixins JSON 中注册**：仅放在正确的 package 目录下不够，必须在 `carpet-bcl-addition.mixins.json` 的 `"mixins"` 数组中显式列出。
- **翻译**：通过 `canHasTranslations(String lang)` 提供，读取 `assets/<modid>/lang/<lang>.json` 并返回 `Map<String, String>`。翻译 key 格式为 `carpet.rule.<规则名>.name` / `carpet.rule.<规则名>.desc` / `carpet.rule.<规则名>.extra.<N>` / `carpet.category.<分类名>`。

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

## 已知问题

1. **掉落和挖掘功能已验证通过**（2026-05-30 测试）。末地传送门框架可正常挖掘并掉落方块物品。硬度设为 50.0F（与黑曜石一致）。

2. **中文翻译已正常显示**（2026-05-29 验证通过）。若未来出现编码问题，将中文转为 Unicode 转义序列（`\uXXXX`）是最可靠的跨平台方案。

3. **⚠️ 数据包标签不加载**：`src/main/resources/data/minecraft/tags/` 下的 `pickaxe.json` 和 `needs_iron_tool.json` 在 Fabric 开发环境中**不会**被加载到游戏内。原因尚未查明（可能是 Fabric 资源加载器对 mod 内 `minecraft` 命名空间资源的处理问题）。目前已通过两个 Mixin 绕过：
   - `EndPortalFrameMiningSpeedMixin`：注入 `ItemStack.getMiningSpeedMultiplier()`，用石头的标签作为代理获取镐的正确挖掘速度
   - `EndPortalFrameBlockDropMixin`：注入 `Block.afterBreak()` TAIL，强制掉落方块物品（因为 `needs_iron_tool` 标签未加载，工具等级检查无法正常运作）

   如果未来修复了标签加载问题，可考虑移除这两个 Mixin，恢复为标准数据包+掉落表方案。

## 本地项目

我从github上扒下来了一些mod的源码，具体来说，我（用ai）写的mod和我从github扒下来的mod都在H:\Minecraft\mod文件夹里，你可以随意查。
- 但不是你现在打开的项目你不能修改！！！