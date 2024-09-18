package com.example.bt2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private TextView textView1, textView2, textView3;
    private Button startStopButton1, startStopButton2, startStopButton3;
    private boolean isRunning1 = false, isRunning2 = false, isRunning3 = false;
    private Handler handler;
    private Thread thread1, thread2, thread3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        startStopButton1 = findViewById(R.id.startStopButton1);
        startStopButton2 = findViewById(R.id.startStopButton2);
        startStopButton3 = findViewById(R.id.startStopButton3);

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        textView1.setText(String.valueOf(msg.arg1));
                        break;
                    case 2:
                        textView2.setText(String.valueOf(msg.arg1));
                        break;
                    case 3:
                        textView3.setText(String.valueOf(msg.arg1));
                        break;
                }
            }
        };

        startStopButton1.setOnClickListener(view -> {
            if (isRunning1) {
                stopThread1();
            } else {
                startThread1();
            }
            isRunning1 = !isRunning1;
            startStopButton1.setText(isRunning1 ? "Stop" : "Start");
        });

        startStopButton2.setOnClickListener(view -> {
            if (isRunning2) {
                stopThread2();
            } else {
                startThread2();
            }
            isRunning2 = !isRunning2;
            startStopButton2.setText(isRunning2 ? "Stop" : "Start");
        });

        startStopButton3.setOnClickListener(view -> {
            if (isRunning3) {
                stopThread3();
            } else {
                startThread3();
            }
            isRunning3 = !isRunning3;
            startStopButton3.setText(isRunning3 ? "Stop" : "Start");
        });
    }

    private void startThread1() {
        thread1 = new Thread(() -> {
            Random random = new Random();
            while (!Thread.currentThread().isInterrupted()) { // Kiểm tra gián đoạn của luồng
                int number = random.nextInt(51) + 50;
                Message msg = handler.obtainMessage(1, number, 0);
                handler.sendMessage(msg);
                try {
                    Thread.sleep(1000); // Tạm dừng để cập nhật giá trị sau mỗi giây
                } catch (InterruptedException e) {
                    // Nếu luồng bị gián đoạn trong lúc ngủ, thoát vòng lặp
                    Thread.currentThread().interrupt();
                }
            }
        });
        thread1.start();
    }

    private void startThread2() {
        thread2 = new Thread(() -> {
            int number = 1;
            while (!Thread.currentThread().isInterrupted()) { // Kiểm tra gián đoạn của luồng
                Message msg = handler.obtainMessage(2, number, 0);
                handler.sendMessage(msg);
                number += 2;
                try {
                    Thread.sleep(2500); // Tạm dừng để cập nhật giá trị sau mỗi 2.5 giây
                } catch (InterruptedException e) {
                    // Nếu luồng bị gián đoạn trong lúc ngủ, thoát vòng lặp
                    Thread.currentThread().interrupt();
                }
            }
        });
        thread2.start();
    }

    private void startThread3() {
        thread3 = new Thread(() -> {
            int number = 0;
            while (!Thread.currentThread().isInterrupted()) { // Kiểm tra gián đoạn của luồng
                Message msg = handler.obtainMessage(3, number, 0);
                handler.sendMessage(msg);
                number += 2;
                try {
                    Thread.sleep(2000); // Tạm dừng để cập nhật giá trị sau mỗi 2 giây
                } catch (InterruptedException e) {
                    // Nếu luồng bị gián đoạn trong lúc ngủ, thoát vòng lặp
                    Thread.currentThread().interrupt();
                }
            }
        });
        thread3.start();
    }


    private void stopThread1() {
        if (thread1 != null && thread1.isAlive()) {
            thread1.interrupt(); // Gửi tín hiệu gián đoạn cho luồng
        }
    }

    private void stopThread2() {
        if (thread2 != null && thread2.isAlive()) {
            thread2.interrupt(); // Gửi tín hiệu gián đoạn cho luồng
        }
    }

    private void stopThread3() {
        if (thread3 != null && thread3.isAlive()) {
            thread3.interrupt(); // Gửi tín hiệu gián đoạn cho luồng
        }
    }
}