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

### 1. 准备Android设备

- 需要一台已Root的Android设备
- Android版本建议7.0或以上
- 确保设备与Web控制端在同一局域网

### 2. 安装Android客户端

1. 在Android Studio中打开`android-client`项目
2. 编译并安装APK到Android设备
3. 启动应用，授予截屏和root权限
4. 记录显示的设备IP地址和端口号

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
- 在Android Studio中选择 Build > Build Bundle(s) / APK(s) > Build APK(s)

## 许可证

MIT License

## 贡献

欢迎提交Issue和Pull Request！

## 免责声明

本工具仅用于合法的设备管理和技术学习目的。使用者需自行承担使用本工具产生的所有责任。请勿用于任何非法用途。
