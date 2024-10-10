package com.example.appmusic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener {

    // Kiểm tra trạng thái chạy nhạc
    private static final int LEVEL_PAUSE = 0;
    private static final int LEVEL_PLAY = 1;
    private static final MediaPlayer player = new MediaPlayer();
    private static final int STATE_IDE = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_PAUSED = 3;

    // Danh sách bài hát
    private final ArrayList<SongEntity> listSong = new ArrayList<>();
    private TextView tvName, tvAlbum, tvTime;
    private SeekBar seekBar;
    private ImageView ivPlay;
    private int index;
    private SongEntity songEntity;
    private Thread thread;
    private int state = STATE_IDE;
    private String totalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    // Khởi tạo các view và kiểm tra quyền truy cập
    private void initViews() {
        ivPlay = findViewById(R.id.iv_play);
        ivPlay.setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.iv_next).setOnClickListener(this);
        tvName = findViewById(R.id.tv_name);
        tvAlbum = findViewById(R.id.tv_album);
        tvTime = findViewById(R.id.tv_time);
        seekBar = findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(this);

        // Kiểm tra quyền truy cập dựa trên phiên bản Android
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ cần thêm quyền READ_MEDIA_AUDIO để truy cập file âm thanh
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 101);
                return;
            }
        } else {
            // Các phiên bản Android cũ hơn cần quyền READ_EXTERNAL_STORAGE
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
                return;
            }
        }
        // Nếu quyền đã được cấp, tiếp tục tải danh sách bài hát
        loadingListSongOffline();
    }

    // Xử lý yêu cầu quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadingListSongOffline();
                } else {
                    Toast.makeText(this, R.string.txt_alert, Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadingListSongOffline();
                } else {
                    Toast.makeText(this, R.string.txt_alert, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    // Tải ds bài hát từ bộ nhớ ngoài
    @SuppressLint("Range")
    private void loadingListSongOffline() {
        Cursor c = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null);
        if (c != null) {
            c.moveToFirst();
            listSong.clear();
            while (!c.isAfterLast()) {
                String name = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String path = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
                String album = "N/A";
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    album = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ARTIST));
                }
                listSong.add(new SongEntity(name, path, album));
                c.moveToNext();
            }
            c.close();
        }

        // Hiển thị ds bài hát trong RecyclerView
        RecyclerView rv = findViewById(R.id.rv_song);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new MusicAdapter(listSong, this));

        // Kiểm tra nếu ds bài hát
        if (!listSong.isEmpty()) {
            play();
            playPause();
        } else {
            Toast.makeText(this, "Không tìm thấy bài hát nào!", Toast.LENGTH_SHORT).show();
        }
    }


    // Bắt đầu phát bài hát
    public void playSong(SongEntity songEntity) {
        index = listSong.indexOf(songEntity);
        this.songEntity = songEntity;
        play();
    }

    // Xử lý các sự kiện nút Play/Pause, Next và Back
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_play) {
            playPause();
        } else if (v.getId() == R.id.iv_next) {
            next();
        } else if (v.getId() == R.id.iv_back) {
            back();
        }
    }

    // Chuyển về bài hát trước
    private void back() {
        if (index == 0) {
            index = listSong.size() - 1;
        } else {
            index--;
        }
        play();
    }

    // Chuyển tới bài hát tiếp theo
    private void next() {
        if (index >= listSong.size()) {
            index = 0;
        } else {
            index++;
        }
        play();
    }

    // Tạm dừng và tiếp tục bài hát
    private void playPause() {
        if (state == STATE_PLAYING && player.isPlaying()) {
            player.pause();
            ivPlay.setImageLevel(LEVEL_PAUSE);
            state = STATE_PAUSED;
        } else if (state == STATE_PAUSED) {
            player.start();
            state = STATE_PLAYING;
            ivPlay.setImageLevel(LEVEL_PLAY);
        } else {
            play();
        }
    }

    // Nếu ds rỗng thì quay lại
    private void play() {
        if (listSong.isEmpty()) {
            return;
        }

        songEntity = listSong.get(index);
        tvName.setText(songEntity.getName());
        tvAlbum.setText(songEntity.getAlbum());
        player.reset();
        try {
            player.setDataSource(songEntity.getPath());
            player.prepare();
            player.start();
            ivPlay.setImageLevel(LEVEL_PLAY);
            state = STATE_PLAYING;
            totalTime = getTime(player.getDuration());
            seekBar.setMax(player.getDuration());
            if (thread == null) {
                startLooping();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Liên tục cập nhật thời gian bài hát
    private void startLooping() {thread = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                    return;
                }
                runOnUiThread(() -> updateTime());
            }
        }
    };
        thread.start();
    }

    // Cập nhật thời gian của bài hát trên giao diện
    private void updateTime() {
        if (state == STATE_PLAYING || state == STATE_PAUSED) {
            int time = player.getCurrentPosition();
            tvTime.setText(String.format("%s/%s", getTime(time), totalTime));
            seekBar.setProgress(time);
        }
    }

    // Chuyển đổi thời gian từ milliseconds thành mm:ss
    @SuppressLint("SimpleDateFormat")
    private String getTime(int time) {
        return new SimpleDateFormat("mm:ss").format(new Date(time));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        thread.interrupt();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (state == STATE_PLAYING || state == STATE_PAUSED) {
            player.seekTo(seekBar.getProgress());
        }
    }
}