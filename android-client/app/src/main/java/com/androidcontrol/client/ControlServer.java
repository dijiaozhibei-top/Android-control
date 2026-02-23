package com.androidcontrol.client;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Base64;

public class ControlServer extends WebSocketServer {
    private Context context;
    private int resultCode;
    private Intent data;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private Handler handler;
    private HandlerThread handlerThread;
    private boolean isRunning = false;
    private WebSocket client;
    
    public ControlServer(Context context, int port, int resultCode, Intent data) {
        super(new InetSocketAddress(port));
        this.context = context;
        this.resultCode = resultCode;
        this.data = data;
        this.handlerThread = new HandlerThread("ScreenCapture");
        this.handlerThread.start();
        this.handler = new Handler(handlerThread.getLooper());
    }
    
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        this.client = conn;
        ((MainActivity) context).log("客户端已连接: " + conn.getRemoteSocketAddress());
        startScreenCapture();
    }
    
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        this.client = null;
        ((MainActivity) context).log("客户端已断开连接");
        stopScreenCapture();
    }
    
    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");
            
            switch (type) {
                case "click":
                    handleClick(json.getInt("x"), json.getInt("y"));
                    break;
                case "swipe":
                    handleSwipe(
                        json.getInt("startX"),
                        json.getInt("startY"),
                        json.getInt("endX"),
                        json.getInt("endY"),
                        json.getInt("duration")
                    );
                    break;
                case "key":
                    handleKey(json.getInt("keyCode"));
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onError(WebSocket conn, Exception ex) {
        ((MainActivity) context).log("错误: " + ex.getMessage());
    }
    
    @Override
    public void onStart() {
        ((MainActivity) context).log("服务器正在监听端口: " + getPort());
        isRunning = true;
    }
    
    private void startScreenCapture() {
        MediaProjectionManager mpm = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mediaProjection = mpm.getMediaProjection(resultCode, data);
        
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        int density = metrics.densityDpi;
        
        imageReader = ImageReader.newInstance(width, height, android.graphics.PixelFormat.RGBA_8888, 2);
        
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenCapture",
            width, height, density,
            android.view.Display.FLAGS,
            imageReader.getSurface(),
            null,
            handler
        );
        
        startCaptureLoop();
    }
    
    private void startCaptureLoop() {
        handler.postDelayed(() -> {
            if (!isRunning || client == null || client.isClosed()) {
                return;
            }
            
            try {
                Image image = imageReader.acquireLatestImage();
                if (image != null) {
                    Bitmap bitmap = imageToBitmap(image);
                    image.close();
                    
                    if (bitmap != null) {
                        String base64 = bitmapToBase64(bitmap);
                        bitmap.recycle();
                        
                        if (base64 != null && client != null && client.isOpen()) {
                            client.send(base64);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            startCaptureLoop();
        }, 100); // 每100毫秒发送一次
    }
    
    private Bitmap imageToBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * image.getWidth();
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        Bitmap bitmap = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height,
            Bitmap.Config.ARGB_8888
        );
        bitmap.copyPixelsFromBuffer(buffer);
        
        if (rowPadding != 0) {
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        }
        
        return bitmap;
    }
    
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // 压缩质量50%
        byte[] bytes = baos.toByteArray();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getEncoder().encodeToString(bytes);
        }
        return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);
    }
    
    private void stopScreenCapture() {
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
    }
    
    private void handleClick(int x, int y) {
        handler.post(() -> {
            try {
                Runtime runtime = Runtime.getRuntime();
                String command = String.format("input tap %d %d", x, y);
                Process process = runtime.exec("su");
                java.io.DataOutputStream os = new java.io.DataOutputStream(process.getOutputStream());
                os.writeBytes(command + "\n");
                os.writeBytes("exit\n");
                os.flush();
                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private void handleSwipe(int startX, int startY, int endX, int endY, int duration) {
        handler.post(() -> {
            try {
                Runtime runtime = Runtime.getRuntime();
                String command = String.format("input swipe %d %d %d %d %d", startX, startY, endX, endY, duration);
                Process process = runtime.exec("su");
                java.io.DataOutputStream os = new java.io.DataOutputStream(process.getOutputStream());
                os.writeBytes(command + "\n");
                os.writeBytes("exit\n");
                os.flush();
                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private void handleKey(int keyCode) {
        handler.post(() -> {
            try {
                Runtime runtime = Runtime.getRuntime();
                String command = String.format("input keyevent %d", keyCode);
                Process process = runtime.exec("su");
                java.io.DataOutputStream os = new java.io.DataOutputStream(process.getOutputStream());
                os.writeBytes(command + "\n");
                os.writeBytes("exit\n");
                os.flush();
                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    @Override
    public void stop() throws IOException {
        isRunning = false;
        stopScreenCapture();
        if (handlerThread != null) {
            handlerThread.quitSafely();
        }
        super.stop();
    }
}
