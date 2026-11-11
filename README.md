# BiggerStacks (GitHub fork)

Allows increasing stack sizes past 64 up to MAXINT/2 (about 1 billion).

本仓库是 [PORTB/BiggerStacks](https://codeberg.org/PORTB/BiggerStacks)（Codeberg）的 GitHub 镜像 fork，
用于维护 **Minecraft 1.21.11** 版本。

## 分支

| 分支 | 版本 | 说明 |
|---|---|---|
| `1.21.11` | Minecraft 1.21.11 / NeoForge 21.11.x | 本 fork 的维护目标（WIP） |
| `1.20.1` | Minecraft 1.20.1 / Forge 47.4.20 | 上游镜像分支，与 Codeberg 上游保持一致 |

上游其余历史分支与标签已镜像，便于对照。移植进度与差异说明见 [PORTING.md](./PORTING.md)。

## 构建（1.21.11）

需要 JDK 21：

```bash
git checkout 1.21.11
JAVA_HOME=/path/to/jdk-21 ./gradlew build
# 产物：build/libs/biggerstacks-1.21.11-<version>.jar
```

## 使用

1. 将 jar 同时放进客户端和服务端的 `mods` 目录。
2. 在游戏内运行 `/biggerstacks quicksetup` 调整三种堆叠档位，或直接编辑
   `config/biggerstacks-rules.xml`。


## 兼容模块（1.21.11 / NeoForge）

| 模组 | 状态 |
|---|---|
| Sophisticated Core / Backpacks | ✅ 已支持（mixin + XML 变换） |
| Applied Energistics 2 | ⛔ 阻塞（上游无 1.21.11 构建，见 #5） |
| Refined Storage | ⛔ 阻塞（上游无 1.21.11 构建，见 #6） |
| Modular Routers | ⛔ 阻塞（上游无 1.21.11 构建，见 #7） |
| Better Bundles | ➖ 不适用（原版 bundle 已由本模组处理，见 #8） |
| NeoForge transfer API（通用） | ✅ 已支持（槽位容量上限已放宽） |

详细兼容矩阵与移植说明见 [PORTING.md](./PORTING.md)；兼容相关 issue 见 [milestone 1.21.11 兼容与首发](https://github.com/Thatgfsj-minecraft/BiggerStacks/milestone/1)。

## 仓库与流程

- 仓库由 `Thatgfsj-minecraft` 组织维护；上游同步走 `upstream`（Codeberg）remote。
- 改动遵循 issue + PR 流程；CI 在构建阶段启用（#11）。
## 许可与致谢

- 上游代码版权归 PORTB 所有，按 GNU LGPL v3 发布；本 fork 保持同一协议并保留原始版权声明。
- 1.21.11 移植在实现上参考了社区 NeoForge 1.21.1 移植
  [Rinorsi/biggerstacks-Unofficial](https://github.com/Rinorsi/biggerstacks-Unofficial) 的技术方案
  （数据组件/编解码器放宽、configuration 阶段规则同步等），特此致谢。
