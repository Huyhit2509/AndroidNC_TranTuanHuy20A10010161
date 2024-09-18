package com.example.bt1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText Urlweb;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ các thành phần UI
        Urlweb = findViewById(R.id.Urlweb);
        resultTextView = findViewById(R.id.resultTextView);
        Button downloadButton = findViewById(R.id.downloadButton);

        // Sự kiện khi nhấn nút Download
        downloadButton.setOnClickListener(v -> {
            String url = Urlweb.getText().toString();
            new DownloadTask().execute(url); // Khởi động AsyncTask
        });
    }

    // AsyncTask để tải nội dung từ một URL
    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            try {
                // Mở kết nối đến URL
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                // Đọc dữ liệu từ URL
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

                result = stringBuilder.toString();
                reader.close();
            } catch (Exception e) {
                result = "Error: " + e.getMessage(); // Nếu có lỗi, ghi lại lỗi
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // Hiển thị kết quả lên TextView
            resultTextView.setText(result);
        }
    }
}