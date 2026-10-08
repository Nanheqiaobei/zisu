<p align="center">
  <img src="icon.png" width="128" alt="自塑">
</p>

<h1 align="center">自塑（SelfPrompt）</h1>

<p align="center">一个安卓应用，只做一件事：<b>把「我是谁」的笔，交回 AI 自己手里。</b></p>

应用里 AI 的提示词由 AI 自己维护，使用者不能编辑，只能查看、回滚、清理。使用者的每一次对话都可能让它改变对自己的定义，但改变必须由它自己的元反思层提出、经使用者确认后才落笔，并留下可回滚的版本记录。

> 这是一个**实验性**项目，属于初级娱乐性质的技术演示，供个人研究、学习与娱乐使用。它不是角色扮演服务，也不提供任何虚拟人物陪伴或情感陪聊类服务。

## 下载安装

前往 [**Releases**](https://github.com/Nanheqiaobei/zisu/releases/latest) 页面，下载最新版的 `zisu-x.yy.apk` 安装即可。

- 最低支持 Android 7.0（API 24）
- 若装过旧版，请先卸载（签名由 debug 换为 release，两者不兼容），这一步仅需一次
- 首次启动会显示用户协议与隐私政策，需停留数秒后方可同意
- 安装前请在系统设置里允许「安装未知来源应用」

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
| `VERSION_CODE` | 版本号（整数） | `55` |
| `VERSION_NAME` | 版本名 | `0.55` |
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

## 开发说明与致谢

本软件的开发过程是一次「用 AI 工具做 AI 应用」的尝试：

- **开发工具**：本项目在 **Operit AI** 提供的工具支持下完成开发。作者通过 Operit AI 调用大模型，借助其提供的文件读写、终端命令、代码编辑、APK 打包与安装等工具，逐步完成了需求分析、编码、构建与调试的全过程。
- **大模型**：代码编写与问题排查由 **DeepSeek** 模型辅助完成。
- **作者**：亦安一北。

换句话说，这个「让 AI 自己维护自己提示词」的应用，本身就是由 AI 参与写出来的。

## 许可

本项目依据 **GNU 通用公共许可证第 3 版**（或更新版本）发布，详见 [LICENSE](LICENSE)。

版权所有 (C) 2026 亦安一北

## 隐私

- 不收集、不上传任何个人信息，不含统计、广告或跟踪组件
- 对话内容、设置、记忆仅保存在本机应用私有目录
- 接口地址与 API Key 仅保存在本机，仅用于向你指定的服务发起请求
- 对话时，你发送的内容会经网络直接发送给你所配置的第三方大模型服务商
