package com.example.lhub;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {
    private String logData = ""; // GUI indicator
    private TextView logTextView;
    public void addLog(String message) { // add log
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        logData = logData + now.format(formatter) + " -" + message + "\n";
        logTextView.setText(logData);
    }
    public void clearLog() { // delete log
        logData = "";
        logTextView.setText(logData);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) { // android main
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logTextView = findViewById(R.id.logTextView);

        Server s = new Server(this);
    }
}