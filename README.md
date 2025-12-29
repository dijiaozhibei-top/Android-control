# Android远程控制系统

通过Web浏览器远程控制Android设备的完整解决方案。

## 项目结构

```
Android-control/
├── web-control/          # Web控制端（React + Vite）
└── android-client/      # Android客户端（WebSocket服务器）
```

## 功能特性

- 📱 **实时屏幕共享**: 通过WebSocket实时传输Android设备屏幕画面
- 🖱️ **远程点击控制**: 在Web端点击即可控制Android设备
- 👆 **滑动操作**: 支持上下左右滑动操作
- ⌨️ **按键模拟**: 支持Home、Back、音量键等常用按键
- 🎨 **现代化UI**: 基于React和Vite构建的现代化Web界面
- 🔌 **WebSocket通信**: 基于WebSocket的高效实时通信

## 使用说明

### 方法一：使用GitHub Actions自动构建APK（推荐）

1. **触发构建**
   - 代码推送到 `main` 或 `develop` 分支会自动触发构建
   - 在GitHub仓库的 "Actions" 标签页手动触发工作流
   - 访问：https://github.com/dijiaozhibei-top/Android-control/actions

2. **下载APK**
   - 构建完成后，在Actions页面找到工作流运行记录
   - 点击进入，在 "Artifacts" 区域下载APK
   - `android-control-debug`: 调试版本APK
   - `android-control-release`: 发布版本APK（未签名）

3. **Releases下载**
   - 推送到 `main` 分支会自动创建GitHub Release
   - 访问：https://github.com/dijiaozhibei-top/Android-control/releases
   - 下载最新版本的APK

### 方法二：本地Android Studio构建

1. **准备Android设备**
   - 需要一台已Root的Android设备
   - Android版本建议7.0或以上
   - 确保设备与Web控制端在同一局域网

2. **在Android Studio中打开项目**
   ```bash
   打开 android-client 文件夹
   ```

3. **编译和安装**
   - 点击 "Run" 按钮或使用快捷键 Shift+F10
   - 或者使用命令行构建：
     ```bash
     cd android-client
     ./gradlew assembleDebug  # 构建调试版本
     ./gradlew assembleRelease  # 构建发布版本
     ```

4. **启动应用**
   - 安装APK到Android设备
   - 授予截屏和root权限
   - 记录显示的设备IP地址和端口号（默认8080）

### 3. 启动Web控制端

#### 方式一：使用EdgeOne Pages（推荐）

1. 访问已部署的Web控制端URL
2. 输入Android设备的IP地址和端口号
3. 点击"连接"按钮
4. 开始远程控制

#### 方式二：本地运行

```bash
cd web-control
npm install
npm run dev
```

访问 `http://localhost:3000` 并输入设备IP和端口进行连接。

## 部署到EdgeOne Pages

Web控制端已集成EdgeOne Pages部署功能，可以一键部署：

1. 确保已安装依赖：
```bash
cd web-control
npm install
```

2. 构建项目：
```bash
npm run build
```

3. 使用EdgeOne Pages部署 `dist` 目录

## 技术栈

### Web控制端
- **React 18**: 用户界面框架
- **Vite**: 现代化构建工具
- **WebSocket API**: 实时通信

### Android客户端
- **Java**: 开发语言
- **MediaProjection API**: 屏幕截取
- **WebSocket Server**: 实时通信服务
- **Java-WebSocket**: WebSocket库

## 通信协议

### 从Android发送到Web端
```json
"data:image/jpeg;base64,<base64编码的屏幕画面>"
```

### 从Web端发送到Android端

**点击事件：**
```json
{
  "type": "click",
  "x": 100,
  "y": 200
}
```

**滑动事件：**
```json
{
  "type": "swipe",
  "startX": 100,
  "startY": 200,
  "endX": 300,
  "endY": 400,
  "duration": 500
}
```

**按键事件：**
```json
{
  "type": "key",
  "keyCode": 3
}
```

## 权限说明

Android应用需要以下权限：
- `INTERNET`: 网络通信
- `FOREGROUND_SERVICE`: 前台服务
- `MEDIA_PROJECTION`: 屏幕截取（需要用户授权）
- `SYSTEM_ALERT_WINDOW`: 悬浮窗权限

## 注意事项

⚠️ **重要提示：**
- Android设备需要Root权限才能执行点击和滑动操作
- 建议在安全的局域网环境使用
- 请勿在公共网络环境下暴露Android设备的IP和端口
- 使用完毕后及时断开连接

## 开发

### 本地开发

**Web控制端开发：**
```bash
cd web-control
npm install
npm run dev
```

**Android客户端开发：**
- 在Android Studio中打开 `android-client` 项目
- 连接Android设备或使用模拟器
- 运行应用

### 构建

**Web控制端生产构建：**
```bash
cd web-control
npm run build
```

**Android客户端APK构建：**

方式一：使用GitHub Actions自动构建
```bash
git push origin main  # 自动触发构建
```
然后访问GitHub Actions页面下载构建产物。

方式二：本地构建
```bash
cd android-client
./gradlew assembleDebug    # 构建调试版本
./gradlew assembleRelease  # 构建发布版本
```

**手动触发GitHub Actions构建：**
1. 访问 https://github.com/dijiaozhibei-top/Android-control/actions
2. 选择 "Build Android APK" 工作流
3. 点击 "Run workflow"
4. 选择分支并运行

**构建产物位置：**
- **Debug APK**: `android-client/app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `android-client/app/build/outputs/apk/release/app-release-unsigned.apk`

**GitHub Actions输出：**
- 构建成功后会自动创建Release
- Release包含调试版和发布版APK
- 构建产物保留30天

## 许可证

MIT License

## 贡献

欢迎提交Issue和Pull Request！

## 免责声明

本工具仅用于合法的设备管理和技术学习目的。使用者需自行承担使用本工具产生的所有责任。请勿用于任何非法用途。

## GitHub Actions CI/CD

项目已配置GitHub Actions自动构建APK，支持以下触发方式：

### 触发条件
- ✅ 推送代码到 `main` 或 `develop` 分支
- ✅ 针对这些分支的Pull Request
- ✅ 手动触发（在Actions页面）

### 工作流功能
1. **自动构建**: 检测到Android客户端代码变更时自动构建
2. **多版本输出**: 同时构建Debug和Release版本
3. **自动发布**: 推送到main分支时创建GitHub Release
4. **构建产物**: 上传APK到GitHub Actions Artifacts

### 构建产物说明
- `android-control-debug`: 调试版本，使用debug签名
- `android-control-release`: 发布版本，未签名（需自行签名）

### 查看构建状态
- Actions页面: https://github.com/dijiaozhibei-top/Android-control/actions
- README徽章: 显示最新的构建状态

### 自定义构建
如需修改构建配置，编辑 `.github/workflows/build-apk.yml`

**注意**: Release版本APK未签名，如需发布到应用商店，需要使用自己的密钥进行签名。
