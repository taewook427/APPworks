package com.example.kutil6_metrics;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class NoteActivity {
    public String password = "0000";
    public String[] titles = {"Empty note"};
    public String[] contents = {"Empty note"};
    private Context context;

    public NoteActivity(boolean loadContent, Context c) { // load password
        this.context = c;
        try {
            File file = new File(this.context.getFilesDir(), "data.bin");
            FileInputStream f = new FileInputStream(file);
            byte[] buffer = new byte[4];
            f.read(buffer);
            buffer = new byte[decode(buffer)];
            f.read(buffer);
            this.password = new String(buffer, StandardCharsets.UTF_8);

            if (loadContent) {
                buffer = new byte[4];
                f.read(buffer);
                int num = decode(buffer);
                this.titles = new String[num];
                this.contents = new String[num];

                for (int i = 0; i < num; i++) {
                    buffer = new byte[4];
                    f.read(buffer);
                    buffer = new byte[decode(buffer)];
                    f.read(buffer);
                    this.titles[i] = new String(buffer, StandardCharsets.UTF_8);

                    buffer = new byte[4];
                    f.read(buffer);
                    buffer = new byte[decode(buffer)];
                    f.read(buffer);
                    this.contents[i] = new String(buffer, StandardCharsets.UTF_8);
                }
            }
            f.close();

        } catch (Exception e) {
            this.password = "0000";
            this.titles = new String[]{"New note"};
            this.contents = new String[]{e.toString()};
            saveNote();
        }
    }

    public void saveNote() { // save content
        try {
            File file = new File(this.context.getFilesDir(), "data.bin");
            FileOutputStream f = new FileOutputStream(file);
            byte[] buffer = this.password.getBytes(StandardCharsets.UTF_8);
            f.write(encode(buffer.length));
            f.write(buffer);

            f.write(encode(this.titles.length));
            for (int i = 0; i < this.titles.length; i++) {
                buffer = this.titles[i].getBytes(StandardCharsets.UTF_8);
                f.write(encode(buffer.length));
                f.write(buffer);
                buffer = this.contents[i].getBytes(StandardCharsets.UTF_8);
                f.write(encode(buffer.length));
                f.write(buffer);
            }

            f.close();
            Toast.makeText(this.context, "Note saved", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this.context, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] encode(int num) {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            result[i] = (byte) (num % 256);
            num = num / 256;
        }
        return result;
    }

    private int decode(byte[] data) {
        int result = 0;
        int mul = 1;
        for (int i = 0; i < 4; i++) {
            result = result + mul * (data[i] & 0xFF);
            mul = mul * 256;
        }
        return result;
    }
}