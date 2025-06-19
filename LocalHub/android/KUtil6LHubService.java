package com.example.kutil6_lhub;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.ConcurrentLinkedQueue;

// background service (cannot use name LHubService with NanoHTTPD)
public class KUtil6LHubService extends Service {
    private String CHANNEL_ID = "Kutil6LocalHubServiceChannel";
    public ConcurrentLinkedQueue<String> logger = new ConcurrentLinkedQueue<>();
    private IBinder binder = new LocalBinder();
    public class LocalBinder extends Binder {
        public KUtil6LHubService getService() {
            return KUtil6LHubService.this;
        }
    }

    private Thread svrThread;
    private Server server;

    @Override
    public void onCreate() {
        super.onCreate();
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "service channel", importance);
        channel.setDescription("kutil6_lhub service channel");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) { notificationManager.createNotificationChannel(channel); }
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("LocalHub")
                .setContentText("LocalHub Server Working")
                .setSmallIcon(R.drawable.alerticon) // mini icon
                .build();
        startForeground(1, notification);

        server = new Server(this, logger);
        svrThread = new Thread(new Runnable() {
            @Override
            public void run() { server.startServer(); }
        });
        svrThread.start(); // independent with UI thread
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (svrThread != null) {
            server.stopServer();
            svrThread.interrupt();
        } // stop the server
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}