# 自塑（SelfPrompt）

一个安卓应用，只做一件事：**把「我是谁」的笔，交回 AI 自己手里。**

应用里 AI 的提示词由 AI 自己维护，使用者不能编辑，只能查看、回滚、清理。使用者的每一次对话都可能让它改变对自己的定义，但改变必须由它自己的元反思层提出、经使用者确认后才落笔，并留下可回滚的版本记录。

> 这是一个**实验性**项目，属于初级娱乐性质的技术演示，供个人研究、学习与娱乐使用。它不是角色扮演服务，也不提供任何虚拟人物陪伴或情感陪聊类服务。

## 特性

- **提示词分层**：固定底线（代码）→ 系统层（代码）→ 核心层（只有名字，人类可编辑）→ AI 自己的设定（由它自己维护）
- **元反思层**：每几轮由 AI 回看最近对话，只在有具体、可复现的理由时提出修改方案，方案先弹给使用者确认
- **长期记忆**：旧对话压缩成日志，是它唯一的长程记忆；可查看、可直接删、可整库销毁
- **零第三方依赖**：只用 `android.*` 与系统自带的 `org.json`，不依赖 Gradle，用自建脚本一条命令出包
- **数据本地**：不收集、不上传任何数据；API Key 只存本机

## 环境要求

- JDK 17
- Android SDK：`build-tools;35.0.0`、`platforms;android-35`
- 系统命令：`javac`、`zip`、`java`

## 构建

```bash
bash build.sh
```

产物为工程根目录下的 `自塑-debug.apk`。

脚本路径都可用环境变量覆盖，不设则用本机默认值：

| 变量 | 说明 | 默认 |
|---|---|---|
| `APP_DIR` | 工程根目录 | `/root/app` |
| `ANDROID_SDK` | Android SDK 根目录 | `/root/android-sdk` |
| `BUILD_TOOLS` | build-tools 版本目录名 | `35.0.0` |
| `PLATFORM` | platform 目录名 | `android-35` |
| `AAPT2` | aapt2 可执行文件 | `/root/bin/aapt2` |
| `ZIPALIGN` | zipalign 可执行文件 | `/root/bin/zipalign` |
| `JAVA` | java 命令 | `java` |
| `VERSION_CODE` | 版本号（整数） | `48` |
| `VERSION_NAME` | 版本名 | `0.48` |
| `KEYSTORE` | 签名用 keystore | `$APP_DIR/debug.keystore` |
| `KS_PASS` / `KEY_ALIAS` / `KEY_PASS` | keystore 口令、别名、别名口令 | `android` / `androiddebugkey` / `android` |

例如在 x86_64 PC 上（aapt2、zipalign 直接用 SDK 自带的）：

```bash
ANDROID_SDK=$HOME/Android/Sdk \
AAPT2=$HOME/Android/Sdk/build-tools/35.0.0/aapt2 \
ZIPALIGN=$HOME/Android/Sdk/build-tools/35.0.0/zipalign \
APP_DIR=$(pwd) \
bash build.sh
```

> 本机 aapt2 / zipalign 是套在 qemu-user 外的包装脚本，因为官方 aapt2 只提供 x86_64 版。这属于本机特例，在 x86_64 机器上不需要。

## 目录结构

```
src/
  AndroidManifest.xml
  java/com/selfprompt/app/   25 个 Java 源文件，界面全部用代码拼，无 layout XML
  res/
    anim/                    页面切换动画
    drawable/                弹窗圆角窗口背景
    mipmap-*/                应用图标（五档 + 自适应）
    raw/gpl3.txt             GPL-3.0 协议全文
    values/                  字符串与主题
    xml/                     备份排除规则
build.sh                     构建脚本
LICENSE                      GPL-3.0
```

主要源文件：

| 文件 | 职责 |
|---|---|
| `MainActivity` | 对话主界面 |
| `Store` | 全部本地存储（设置、对话、记忆、提示词版本） |
| `UiKit` | 统一 UI 工厂（主题、颜色、卡片、弹窗、动效） |
| `ChatClient` | 大模型接口调用 |
| `SystemLayer` / `SystemInfoActivity` | 系统层提示词与说明 |
| `DataActivity` | 备份 / 还原 / 单独导出（zip） |
| `AgreementActivity` | 首次启动的用户协议与隐私政策 |

## 版本号

版本号在 `build.sh` 的 `VERSION_CODE` / `VERSION_NAME`（默认值在文件顶部）中设置，构建时可用环境变量覆盖。

## 许可

本项目依据 **GNU 通用公共许可证第 3 版**（或更新版本）发布，详见 [LICENSE](LICENSE)。

版权所有 (C) 2026 亦安一北

## 隐私

- 不收集、不上传任何个人信息，不含统计、广告或跟踪组件
- 对话内容、设置、记忆仅保存在本机应用私有目录
- 接口地址与 API Key 仅保存在本机，仅用于向你指定的服务发起请求
- 对话时，你发送的内容会经网络直接发送给你所配置的第三方大模型服务商
