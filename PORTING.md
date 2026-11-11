# PORTING — 版本与移植状态

> 本仓库是 [PORTB/BiggerStacks](https://codeberg.org/PORTB/BiggerStacks) 的 GitHub 镜像 fork，由 `Thatgfsj-minecraft` 维护。
> 规则：只在 `./gradlew build` 通过后才提交/推送；构建不了就记录 BLOCKED 原因，不提交半成品。

## 分支布局

| 分支 | 目标 | 状态 | 说明 |
|---|---|---|---|
| `1.20.1` | Minecraft 1.20.1 × Forge 47.4.20 | 上游镜像 | 与 Codeberg 上游一致，只做同步 |
| `1.21.11` | Minecraft 1.21.11 × NeoForge 21.11.45 | 可构建 + 运行时验证 | 含精妙核心/背包兼容与 XML 变换器引擎 |
| `1.21.11-fabric` | Minecraft 1.21.11 × Fabric Loader 0.20.1 + Fabric API | 开发中 | 本分支，mixin-only 核心功能 |

## Fabric 1.21.11 环境卡

| 项 | 值 |
|---|---|
| Minecraft | 1.21.11 |
| Loader | Fabric Loader 0.20.1 |
| API | Fabric API 0.141.6+1.21.11 |
| 映射 | Mojang official（Loom `officialMojangMappings`） |
| Java | 21 |
| Gradle | 9.5.1 |
| 构建 | Fabric Loom 1.17.21 |
| 协议 | LGPLv3（继承上游） |

构建：

```bash
JAVA_HOME=/path/to/jdk-21 ./gradlew build
# 产物：build/libs/biggerstacks-fabric-1.21.11-<modVersion>.jar
```

运行（开发环境）：

```bash
./gradlew runServer   # 首次运行需要接受 EULA（run/eula.txt）
./gradlew runClient
```

## 与 NeoForge 1.21.11 分支的差异

1. **加载器**：NeoForge → Fabric（Loom / Fabric Loader / Fabric API）。
2. **兼容引擎**：NeoForge 用 FML 10 的 `ClassProcessor` + `FMLModType: LIBRARY` 子包跑 XML 变换器；
   Fabric 没有等价的运行期任意类变换入口，因此本分支是 **mixin-only**，`transformers/*.xml` 与 coremod 不适用。
3. **配置**：`ModConfigSpec` → 普通 JSON 文件（`config/biggerstacks-client.json` / `config/biggerstacks-server.json`）。
4. **网络**：NeoForge payload → Fabric API（`PayloadTypeRegistry` + `ServerPlayNetworking` / `ClientPlayNetworking`）；
   规则集在玩家加入时由服务端直接下发（不再有 configuration 阶段任务）。
5. **事件**：NeoForge 事件 → Fabric API（`ServerLifecycleEvents`、`ServerPlayConnectionEvents`、`CommandRegistrationCallback`、
   `ClientPlayConnectionEvents`、`ItemTooltipCallback`）。
6. **依赖打包**：NeoForge 用 jarJar；Fabric 用 `fabric.mod.json` 的 `jars` + `META-INF/jars/`（ConfigLib / SLW / xstream）。
7. **兼容模块**：1.21.11 的精妙核心/背包、AE2、RS、Modular Routers 都没有 Fabric 构建，因此本分支暂不含兼容模块。

## 已验证

- [x] `./gradlew build` 通过（Loom 1.17.21 / Java 21）
- [x] `./gradlew runServer` 启动到 Done，模组加载，ExtraCodecs mixin 生效（codec 上限放宽日志）
- [x] 规则集 XML 读取（`Loaded BiggerStacks ruleset. Max declared stack size: 1000`）
- [x] `/biggerstacks info` 返回 1000；`summon ... count:2000` 生成后数据为 2000（大数量 codec 通过）
- [x] 服务端命令、RCON、保存/停止正常

## 已知待办

- [ ] 客户端 GUI 目检（`/biggerstacks quicksetup`、tooltip 精确数量、物品角标缩写）——无头环境无法验证
- [x] 重启后大数量持久化复验：count=2000 的实体保存后重启仍为 2000
- [ ] Fabric 兼容模块（等上游发布 Fabric 1.21.11 构建）
- [ ] CI（构建阶段启用，#11）与发布方式（#12）

## 依赖说明

上游 `portb:BiggerStacksConfigLib` / `portb:SimpleLoggerWrapper` 发布在 Codeberg Maven（匿名不可下载），
本仓库从上游发布 jar 提取后放在 `libs/maven`，并在 Fabric 分支以 `META-INF/jars/` 嵌套 jar 形式打包；
xstream 1.4.21 同样作为嵌套 jar 提供。源码仍以上游 Codeberg 仓库为准（LGPLv3）。
