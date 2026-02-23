package com.androidcontrol.client;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final int SCREEN_CAPTURE_REQUEST_CODE = 100;
    private static final int REQUEST_OVERLAY_PERMISSION = 101;
    private static final int PERMISSION_REQUEST_CODE = 102;
    
    private MediaProjectionManager projectionManager;
    private ControlServer server;
    private TextView logTextView;
    private ScrollView scrollView;
    private EditText portEditText;
    private Button startButton;
    private Button stopButton;
    private TextView ipTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        
        initViews();
        checkPermissions();
    }
    
    private void initViews() {
        ipTextView = findViewById(R.id.ipTextView);
        portEditText = findViewById(R.id.portEditText);
        portEditText.setText("8080");
        
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        
        logTextView = findViewById(R.id.logTextView);
        scrollView = findViewById(R.id.scrollView);
        
        ipTextView.setText("设备IP: " + getLocalIpAddress());
        
        startButton.setOnClickListener(v -> requestScreenCapture());
        stopButton.setOnClickListener(v -> stopServer());
    }
    
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    PERMISSION_REQUEST_CODE);
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(intent);
            }
        }
    }
    
    private void requestScreenCapture() {
        startActivityForResult(
            projectionManager.createScreenCaptureIntent(),
            SCREEN_CAPTURE_REQUEST_CODE
        );
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == SCREEN_CAPTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startServer(resultCode, data);
            } else {
                log("截屏权限被拒绝");
                Toast.makeText(this, "需要截屏权限才能使用远程控制", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void startServer(int resultCode, Intent data) {
        try {
            int port = Integer.parseInt(portEditText.getText().toString());
            server = new ControlServer(this, port, resultCode, data);
            server.start();
            
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            portEditText.setEnabled(false);
            
            log("服务器已启动，端口: " + port);
            log("等待Web控制端连接...");
            Toast.makeText(this, "服务器已启动", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的端口号", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
        
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        portEditText.setEnabled(true);
        
        log("服务器已停止");
        Toast.makeText(this, "服务器已停止", Toast.LENGTH_SHORT).show();
    }
    
    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();
            return String.format("%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));
        }
        return "无法获取IP";
    }
    
    public void log(String message) {
        runOnUiThread(() -> {
            String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            logTextView.append("[" + timestamp + "] " + message + "\n");
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
        }
    }
    
    public static Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        return size;
    }
}
