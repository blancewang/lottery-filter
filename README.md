# 双色球缩水过滤器 (lottery-filter)

Android 可安装应用：**双色球组合过滤 / 多轮投注集缩水**（V3.4）。

> 声明：本工具只做结构筛选与注数缩减，**不预测下期号码，不提高单注中奖概率**。

## 下载安装（推荐）

1. 打开本仓库 **[Releases](https://github.com/blancewang/lottery-filter/releases)**  
2. 下载最新的 **`app-debug.apk`**  
3. 手机端：设置 → 安全 → 允许「安装未知应用」（针对浏览器/文件管理器）  
4. 打开 APK 安装 → 桌面出现「双色球缩水」

若 Releases 里还没有 APK：

1. 打开 **[Actions](https://github.com/blancewang/lottery-filter/actions)**  
2. 点最新一次 **Build APK** 成功的工作流  
3. 底部 **Artifacts** 下载 `ssq-filter-apk` 并解压得到 `app-debug.apk`

## 本地用 Android Studio 编译

1. 安装 [Android Studio](https://developer.android.com/studio)  
2. `File → Open` 打开本仓库根目录  
3. 等待 Gradle 同步  
4. 菜单 `Build → Build Bundle(s) / APK(s) → Build APK(s)`  
5. 生成路径：`app/build/outputs/apk/debug/app-debug.apk`

或命令行：

```bash
./gradlew assembleDebug
```

## 功能概要

- 红球杀号 / 胆码 / 候选点选  
- 结构、三区/四区大小、012/0123、边缘、五行、行列、江恩  
- **分板块独立容错区间**  
- **多轮过滤：本轮投注集 → 下轮继续收紧**  
- 条件说明（?）含号码列表  

核心界面为 `app/src/main/assets/index.html`，由 `MainActivity` WebView 加载。

## 技术说明

- minSdk 24 / targetSdk 34  
- Kotlin + AndroidX WebView  
- GitHub Actions 自动构建 Debug APK 并上传 Artifact / Release  
