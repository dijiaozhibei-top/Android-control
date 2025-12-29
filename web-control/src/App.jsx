import { useState, useEffect, useRef } from 'react';
import './App.css';

function App() {
  const [connected, setConnected] = useState(false);
  const [ipAddress, setIpAddress] = useState('');
  const [port, setPort] = useState('8080');
  const [socket, setSocket] = useState(null);
  const canvasRef = useRef(null);
  const imageRef = useRef(null);

  useEffect(() => {
    return () => {
      if (socket) {
        socket.close();
      }
    };
  }, [socket]);

  const connectToDevice = () => {
    if (!ipAddress || !port) {
      alert('请输入IP地址和端口号');
      return;
    }

    try {
      const ws = new WebSocket(`ws://${ipAddress}:${port}`);
      
      ws.onopen = () => {
        setConnected(true);
        setSocket(ws);
        console.log('已连接到设备');
      };

      ws.onmessage = (event) => {
        // 接收屏幕画面数据
        const img = new Image();
        img.onload = () => {
          const canvas = canvasRef.current;
          const ctx = canvas.getContext('2d');
          ctx.clearRect(0, 0, canvas.width, canvas.height);
          ctx.drawImage(img, 0, 0);
        };
        img.src = `data:image/jpeg;base64,${event.data}`;
      };

      ws.onerror = (error) => {
        console.error('WebSocket错误:', error);
        setConnected(false);
      };

      ws.onclose = () => {
        setConnected(false);
        console.log('连接已关闭');
      };
    } catch (error) {
      console.error('连接失败:', error);
      alert('连接失败，请检查IP地址和端口号');
    }
  };

  const disconnect = () => {
    if (socket) {
      socket.close();
      setSocket(null);
      setConnected(false);
    }
  };

  const sendClick = (x, y) => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({
        type: 'click',
        x: x,
        y: y
      }));
    }
  };

  const sendSwipe = (startX, startY, endX, endY, duration) => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({
        type: 'swipe',
        startX: startX,
        startY: startY,
        endX: endX,
        endY: endY,
        duration: duration
      }));
    }
  };

  const sendKey = (keyCode) => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({
        type: 'key',
        keyCode: keyCode
      }));
    }
  };

  const handleCanvasClick = (e) => {
    const canvas = canvasRef.current;
    const rect = canvas.getBoundingClientRect();
    const scaleX = canvas.width / rect.width;
    const scaleY = canvas.height / rect.height;
    const x = (e.clientX - rect.left) * scaleX;
    const y = (e.clientY - rect.top) * scaleY;
    sendClick(Math.round(x), Math.round(y));
  };

  return (
    <div className="app">
      <div className="header">
        <h1>📱 Android远程控制</h1>
        <div className="connection-panel">
          {!connected ? (
            <>
              <input
                type="text"
                placeholder="设备IP地址"
                value={ipAddress}
                onChange={(e) => setIpAddress(e.target.value)}
                className="input"
              />
              <input
                type="text"
                placeholder="端口"
                value={port}
                onChange={(e) => setPort(e.target.value)}
                className="input short"
              />
              <button onClick={connectToDevice} className="button primary">
                连接
              </button>
            </>
          ) : (
            <>
              <span className="status connected">● 已连接</span>
              <button onClick={disconnect} className="button danger">
                断开
              </button>
            </>
          )}
        </div>
      </div>

      <div className="main-content">
        <div className="screen-container">
          <canvas
            ref={canvasRef}
            width={1080}
            height={1920}
            onClick={handleCanvasClick}
            className={`screen-canvas ${connected ? 'active' : ''}`}
          />
          {!connected && (
            <div className="screen-placeholder">
              <p>请连接设备以查看和控制屏幕</p>
            </div>
          )}
        </div>

        {connected && (
          <div className="control-panel">
            <div className="panel-section">
              <h3>快捷操作</h3>
              <div className="button-grid">
                <button onClick={() => sendKey(3)} className="control-btn">
                  Home
                </button>
                <button onClick={() => sendKey(4)} className="control-btn">
                  Back
                </button>
                <button onClick={() => sendKey(24)} className="control-btn">
                  音量+
                </button>
                <button onClick={() => sendKey(25)} className="control-btn">
                  音量-
                </button>
              </div>
            </div>

            <div className="panel-section">
              <h3>滑动操作</h3>
              <div className="button-grid">
                <button onClick={() => sendSwipe(540, 1500, 540, 500, 500)} className="control-btn">
                  向上滑动
                </button>
                <button onClick={() => sendSwipe(540, 500, 540, 1500, 500)} className="control-btn">
                  向下滑动
                </button>
                <button onClick={() => sendSwipe(200, 960, 880, 960, 500)} className="control-btn">
                  向右滑动
                </button>
                <button onClick={() => sendSwipe(880, 960, 200, 960, 500)} className="control-btn">
                  向左滑动
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
