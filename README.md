# Carpet-BCL-Addition

[![License](https://img.shields.io/github/license/cbtc-59/carpet-bcl-addition)](https://choosealicense.com/licenses/mit/)
[![Github](https://img.shields.io/github/downloads/cbtc-59/carpet-bcl-addition/total?color=161616&label=Github%20downloads&logo=github)](https://github.com/cbtc-59/Carpet-BCL-Addition/releases)

一个 [fabric-carpet](https://github.com/gnembon/fabric-carpet) 扩展模组，添加了一些有趣的规则。

## 前置模组

| 名称 | 类型 | 链接 |
|------|------|------|
| Carpet | 必须 | [MC百科](https://www.mcmod.cn/class/2361.html) |
| Fabric API | 必须 | [MC百科](https://www.mcmod.cn/class/3124.html) |

## 版本支持

仅支持 Minecraft **1.21**。

## 规则

| 规则 | 类型 | 说明 |
|------|------|------|
| `mineableEndPortalFrame` | boolean | 使末地传送门框架方块可挖掘 |
| `softNetherite` | boolean | 远古残骸和下界合金块硬度降为 1/18 |
| `disableWindChargeEffect` | boolean | 阻止风弹改变方块状态 |
| `channelingIgnoreConditions` | enum | 允许忽略三叉戟引雷的条件 |
| `riptideIgnoreConditions` | boolean | 允许忽略三叉戟激流的条件 |
| `forceOpenContainer` | enum | 允许打开被阻挡的容器 |
| `truePeacefulMode` | boolean | 生物不会与玩家敌对 |
| `playerCommandCloseScreen` | boolean | 为 /player 添加 esc 子命令 |
| `fakePlayerAutoRestock` | boolean | 假玩家自动从背包补货 |

所有规则分类为 `BCL`。使用 `/carpet <规则名> <值>` 切换。

## 下载

- [GitHub Releases](https://github.com/cbtc-59/Carpet-BCL-Addition/releases)

## 致谢

部分规则设计和翻译参考了 [Carpet-Org-Addition](https://github.com/fcsailboat/Carpet-Org-Addition)。
