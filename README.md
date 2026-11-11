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

## 许可与致谢

- 上游代码版权归 PORTB 所有，按 GNU LGPL v3 发布；本 fork 保持同一协议并保留原始版权声明。
- 1.21.11 移植在实现上参考了社区 NeoForge 1.21.1 移植
  [Rinorsi/biggerstacks-Unofficial](https://github.com/Rinorsi/biggerstacks-Unofficial) 的技术方案
  （数据组件/编解码器放宽、configuration 阶段规则同步等），特此致谢。
