package com.example.kutil6_lhub;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView logTextView;
    private KUtil6LHubService service;
    private boolean bound;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private Runnable poller = new Runnable() {
        @Override
        public void run() {
            if (bound && service != null) {
                String res = service.logger.poll();
                while (res != null) {
                    logTextView.append(res);
                    res = service.logger.poll();
                }
            }
            uiHandler.postDelayed(this, 500);
        }
    };
    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder ibinder) {
            KUtil6LHubService.LocalBinder lb = (KUtil6LHubService.LocalBinder) ibinder;
            service = lb.getService();
            bound = true;
            uiHandler.post(poller);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
            service = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) { // android main
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logTextView = findViewById(R.id.logTextView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
        Intent svc = new Intent(this, KUtil6LHubService.class);
        startService(svc);
        bindService(svc, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        if (bound) {
            unbindService(conn);
            bound = false;
        }
        uiHandler.removeCallbacks(poller);
        stopService(new Intent(this, KUtil6LHubService.class));
        super.onDestroy();
    }
}