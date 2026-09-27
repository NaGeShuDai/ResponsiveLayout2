# Practical 2 — Implementing Alternative Responsive Layouts

作者：NaGeShuDai · 1586329608@qq.com

基于 Practical 1 的 **Part 1：LinearLayout with Weights** 完成。所有布局均手写 XML。
选择 Practical 2 Part 2 的 **Option A：Smallest Width（sw600dp）**。

## 任务与实现

| 配置 | Android 自动选择的资源 | 设计 |
| --- | --- | --- |
| 手机竖屏 | `layout/activity_main.xml` | 沿用纵向权重布局：标题、四个彩色单词、应用标题和按钮 |
| 手机横屏 | `layout-land/activity_main.xml` | 左侧标题，右侧单词、应用标题和按钮；左右权重 2:3 |
| 平板（smallestWidth ≥ 600dp） | `layout-sw600dp/activity_main.xml` | 左右权重 1:2，24dp 外边距与栏间距，24sp 文本 |

表中路径均相对于 `app/src/main/res/`。平板横竖屏均使用 sw600dp；它的匹配优先于 land。
MainActivity 只调用同一个 `setContentView(R.layout.activity_main)`，不使用 Java 判断方向，也不锁定方向或接管 configChanges。
三份布局保持完全相同的 View ID。大小使用 match_parent、wrap_content、0dp + layout_weight，以及 dp/sp。
边到边显示的系统栏/刘海 Insets 会与 XML 内边距相加，避免覆盖平板留白。
Change/Cancel 沿用 Practical 1 的展示控件；本实验关注布局，未新增业务行为。

## 在 Android Studio 运行

1. 打开本目录，使用 JDK 21，同步 Gradle。
2. 安装项目要求的 Android SDK 36.1，并设置本机 SDK 路径（local.properties 不提交）。
3. 选择 app 运行到手机虚拟设备；旋转屏幕，确认纵向排列变为左右双栏。
4. 创建平板虚拟设备（如 Pixel C，smallestWidth ≥ 600dp），横竖屏检查较大字体、留白和双栏。

构建与测试命令（Windows 使用 gradlew.bat，其他系统使用 ./gradlew）：

~~~text
gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest :app:lintDebug
gradlew.bat :app:connectedDebugAndroidTest
python tools/verify_layouts.py
~~~

调试 APK：app/build/outputs/apk/debug/app-debug.apk。
仪器化测试覆盖手机横竖屏、599dp/600dp 边界、平板横屏优先级，以及实际窗口内所有控件的可见性与边界。
XML 合约检查不需要 Android Studio 或第三方 Python 库。

## 本机 Java 临时目录问题

若 Windows 构建出现 `Unable to establish loopback connection`，可在当前 PowerShell 会话使用较短的临时目录后重试：

~~~powershell
New-Item -ItemType Directory -Force .gradle/tmp | Out-Null
$tempPath = (Resolve-Path .gradle/tmp).Path.Replace('\', '/')
$env:JAVA_TOOL_OPTIONS = "-Djdk.net.unixdomain.tmpdir=$tempPath -Djava.io.tmpdir=$tempPath"
.\gradlew.bat :app:assembleDebug --no-daemon
~~~

此设置仅用于本机环境排错，不改变项目 UI 或全局 Java 设置。

## 提交步骤

每个完成的步骤都单独提交并立即推送至 GitHub：

1. 建立 Practical 2 的竖屏基础工程。
2. 增加手机横屏双栏资源。
3. 增加 sw600dp 平板资源与 Insets 留白处理。
4. 增加测试、运行说明与验证记录。

详细结果见 docs/TESTING.md。
