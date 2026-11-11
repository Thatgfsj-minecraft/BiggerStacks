# BiggerStacks (GitHub fork)

Allows increasing stack sizes past 64 up to MAXINT/2 (about 1 billion).

本仓库是 [PORTB/BiggerStacks](https://codeberg.org/PORTB/BiggerStacks)（Codeberg）的 GitHub 镜像 fork，
由 `Thatgfsj-minecraft` 组织维护，同时提供 **NeoForge** 与 **Fabric** 两条 Minecraft 1.21.11 版本线。

## 分支

| 分支 | 加载器 / 版本 | 说明 |
|---|---|---|
| `1.21.11` | NeoForge 21.11.x · Minecraft 1.21.11 | 主维护线，含第三方模组兼容（精妙核心/背包、XML 变换器引擎） |
| `1.21.11-fabric`（当前） | Fabric Loader 0.20.1 · Minecraft 1.21.11 | Fabric 版本线（核心功能，mixin-only） |
| `1.20.1` | Forge 47.4.20 | 上游镜像分支 |

上游其余历史分支与标签已镜像。各版本差异与限制见 [PORTING.md](./PORTING.md)。

## 构建

需要 JDK 21。

```bash
# NeoForge 版
git checkout 1.21.11
JAVA_HOME=/path/to/jdk-21 ./gradlew build
# -> build/libs/biggerstacks-1.21.11-<version>.jar

# Fabric 版
git checkout 1.21.11-fabric
JAVA_HOME=/path/to/jdk-21 ./gradlew build
# -> build/libs/biggerstacks-fabric-1.21.11-<version>.jar
```

## 使用

1. 将 jar 放进对应加载器（NeoForge 或 Fabric + Fabric API）的 `mods` 目录。
2. 运行 `/biggerstacks quicksetup` 调整堆叠档位，或直接编辑 `config/biggerstacks-rules.xml`。

## 兼容模块（1.21.11）

| 模组 | NeoForge | Fabric |
|---|---|---|
| Sophisticated Core / Backpacks | ✅ 已支持 | ⛔ 上游无 Fabric 1.21.11 构建 |
| Applied Energistics 2 | ⛔ 上游无 1.21.11 构建（见 #5） | ⛔ 同上 |
| Refined Storage | ⛔ 上游无 1.21.11 构建（见 #6） | ⛔ 同上 |
| Modular Routers | ⛔ 上游无 1.21.11 构建（见 #7） | ⛔ 同上 |
| Better Bundles | ➖ 不适用（见 #8） | ➖ 不适用 |

Fabric 版没有 NeoForge 的 `ClassProcessor` 运行期类变换入口，因此上游的 XML 变换器引擎无法等价移植，
Fabric 版是 mixin-only（详见 #16）。

## 仓库与流程

- 仓库由 `Thatgfsj-minecraft` 组织维护；上游同步走 `upstream`（Codeberg）remote。
- 改动遵循 issue + PR 流程；CI 在构建阶段启用（#11）。

## 许可与致谢

- 上游代码版权归 PORTB 所有，按 GNU LGPL v3 发布；本 fork 保持同一协议并保留原始版权声明。
- 1.21.11 NeoForge 移植在实现上参考了社区 1.21.1 移植
  [Rinorsi/biggerstacks-Unofficial](https://github.com/Rinorsi/biggerstacks-Unofficial) 的技术方案，特此致谢。
