# 双色球缩水过滤器

可安装的 Android 应用（WebView + 完整过滤界面 V3.4）。

> **声明**：只做组合筛选与注数缩减，**不预测下期，不提高单注中奖概率**。

---

## 一、下载安装 APK（最简单）

### 方法 A：Releases（推荐）

1. 打开：https://github.com/blancewang/lottery-filter/releases  
2. 点最新版本里的 **app-debug.apk** 下载  
3. 手机打开该文件安装  
   - 若提示未知来源：设置 → 应用 → 特殊权限 → 安装未知应用 → 允许你用的浏览器/文件管理器  
4. 桌面出现 **「双色球缩水」**

### 方法 B：Actions 产物

1. 打开：https://github.com/blancewang/lottery-filter/actions  
2. 点最新一次绿色成功的 **Build APK**  
3. 页面底部 **Artifacts** → 下载 `ssq-filter-apk`  
4. 解压 zip，得到 `app-debug.apk` 后安装  

> Artifacts 默认约保留 90 天；Releases 可长期保存。

### 方法 C：手动触发编译

1. 打开 Actions → **Build APK** → **Run workflow** → Run  
2. 等 2～5 分钟出现绿色勾  
3. 按方法 A 或 B 下载  

---

## 二、若构建失败：补全界面文件

应用界面在：

`app/src/main/assets/index.html`

请把完整的 **双色球缩水_V3.4_专业手机版.html** 上传覆盖该路径：

1. 打开仓库 → `app/src/main/assets/`  
2. 右上角 **Add file → Upload files**  
3. 上传后把文件名改为 `index.html`（或先删除旧的再上传）  
4. Commit 到 `main`  
5. 等待 Actions 自动重新构建 APK  

也可用 `parts/p00.txt`… 分段文件，CI 会自动 `cat` 合并为 `index.html`。

---

## 三、用 Android Studio 本地出包

1. 安装 Android Studio  
2. Open 本仓库根目录  
3. 确认 `app/src/main/assets/index.html` 是完整 V3.4 页面  
4. `Build → Build APK(s)`  
5. 输出：`app/build/outputs/apk/debug/app-debug.apk`  

```bash
./gradlew assembleDebug
```

---

## 功能摘要

- 杀号 / 胆码 / 候选  
- 结构、三区/四区大小、012/0123、边缘、五行、行列、江恩  
- 分板块独立容错（区间）  
- 多轮：**本轮投注集 → 下轮**  

技术：Kotlin WebView，minSdk 24。
