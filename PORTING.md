# PORTING — 版本与移植状态

> 本仓库是 [PORTB/BiggerStacks](https://codeberg.org/PORTB/BiggerStacks) 的 GitHub 镜像 fork，用于在自己的维护下提供 **Minecraft 1.21.11** 版本。
> 规则：只在 `./gradlew build` 通过后才提交/推送；构建不了就记录 BLOCKED 原因，不提交半成品。

## 分支布局

| 分支 | 目标 | 状态 | 说明 |
|---|---|---|---|
| `1.20.1` | Minecraft 1.20.1 × Forge 47.4.20 | 上游镜像 | 与 Codeberg 上游 `1.20.1` 分支一致，只做同步，不改动 |
| `1.21.11` | Minecraft 1.21.11 × NeoForge 21.11.x | WIP | 本 fork 的维护目标，见下 |

上游其余历史分支（`1.16` / `1.18` / `1.19` 等）与标签均已镜像到本仓库，方便对照。

## 1.21.11 环境卡

| 项 | 值 |
|---|---|
| Minecraft | 1.21.11 |
| Loader | NeoForge 21.11.45 |
| 映射 | Mojmap（official） |
| Java | 21（Temurin） |
| Gradle | 9.5.1（wrapper） |
| 构建 | ModDevGradle 2.0.147 |
| 协议 | LGPLv3（继承上游） |

构建：

```bash
JAVA_HOME=/path/to/jdk-21 ./gradlew build
# 产物：build/libs/biggerstacks-1.21.11-<modVersion>.jar
```

运行（开发环境）：

```bash
./gradlew runServer   # 首次运行需要接受 EULA
./gradlew runClient
```

## 与上游 1.20.1 的差异（1.21.11 分支）

1. **Loader**：Forge 47 → NeoForge 21.11（ModDevGradle）。上游的 ForgeGradle 5.1 / Java 17 不再适用于 1.21.11。
2. **数据模型**：1.20.5+ 物品数量在 `ItemStack` 中是 int，序列化走 data component / codec；因此
   - 删除上游的 `BigCount` NBT 兼容补丁与 `FriendlyByteBuf` 字节计数补丁；
   - 通过 `DataComponentsMixin` 放宽 `MAX_STACK_SIZE` 组件的持久化范围（1..MAX）并把网络同步改为 VAR_INT；
   - 通过 `ExtraCodecsMixin` 放宽 `ItemStack` 数量 codec 的 1..99 限制。
3. **网络**：`SimpleChannel` → `RegisterPayloadHandlersEvent` + `CustomPacketPayload`/`StreamCodec`；
   规则集握手改走 NeoForge configuration 阶段（`RegisterConfigurationTasksEvent` + `RulesetSyncTask`）。
4. **配置**：`ForgeConfigSpec` → `ModConfigSpec`（`net.neoforged.neoforge.common`）。
5. **事件**：`MinecraftForge.EVENT_BUS` → `NeoForge.EVENT_BUS`，`@Mod.EventBusSubscriber` → `@EventBusSubscriber`。
6. **权限**：1.21.11 引入 `PermissionCheck`/`PermissionSet`；`/biggerstacks` 使用 `Commands.LEVEL_OWNERS`。
7. **UI**：1.21.9+ 的 GUI 渲染改为 `Matrix3x2fStack` / `ActiveTextCollector`；物品数量渲染注入点改为 `GuiGraphics#renderItemCount`。
8. **兼容模块**：AE2 / Refined Storage / 精妙核心 / 模块化路由器 / BetterBundles 的 1.21.11 构建尚不可用，
   因此本分支暂不包含 `mixin/compat/**` 与 `TransformerLib` 的 XML 运行时变换器（上游 1.20.1 的
   `TransformerEngine` 依赖 ModLauncher 内部字段，见下）。`TransformerEngine` 保留 mixin 插件骨架，
   以后恢复兼容模块时按当前 ModLauncher API 重新实现。

## 已验证

- [x] `./gradlew build` 通过（Java 21 / Gradle 9.5.1）
- [x] 开发环境 `runServer` 启动到 Done，无 mixin 报错
- [x] 生产环境：NeoForge 21.11.45 独立服务器 + jar-in-jar 打包后的 jar 启动成功，内嵌库正常加载
- [x] 大数量验证：`/summon ... {Item:{id:"minecraft:stone",count:2000}}` 生成、保存、重启后数量仍为 2000
- [x] 规则集 XML 读取（1000）与 `/biggerstacks info` 生效

## 已知待办

- [ ] 客户端 GUI 目检（`/biggerstacks quicksetup` 界面）
- [ ] 容器/槽位极限的完整 E2E（需要玩家客户端）
- [ ] 兼容模块恢复（等待目标模组发布 1.21.11 构建）
- [ ] `TransformerLib` / ModLauncher launch plugin 的 1.21.11 实现
- [ ] CI（构建阶段启用，组织约定不在每个 PR 跑 CI）

## 依赖说明

上游的 `portb:BiggerStacksConfigLib` / `portb:BiggerStacksTransformerLib` / `portb:SimpleLoggerWrapper`
发布在 Codeberg Maven，但该仓库不允许匿名下载；本仓库从上游 1.20.1 发布 jar 的
`META-INF/jarjar/` 中提取了这些库，放入 `libs/maven` 作为本地 Maven 仓库使用。源码仍以上游
Codeberg 仓库为准（LGPLv3）。
