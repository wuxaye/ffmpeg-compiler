package com.xaye.compiler;

import static com.xaye.compiler.FilePickerUtils.getRealPathFromUri;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_VIDEO = 1001;
    private ImageView ivThumbnail;
    private TextView tvVideoInfo,tvFFVersion;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivThumbnail = findViewById(R.id.iv_thumbnail);
        tvVideoInfo = findViewById(R.id.tv_video_info);
        tvFFVersion = findViewById(R.id.tv_ffmpeg_version);
        progressBar = findViewById(R.id.progress_bar);

        findViewById(R.id.btn_pick_video).setOnClickListener(v -> {
            // 检查权限
            if (!XXPermissions.isGranted(this, Permission.READ_MEDIA_VIDEO)) {
                requestPermissions();
            } else {
                FilePickerUtils.pickVideo(this);
            }
        });

        tvFFVersion.setText(String.format("FFmpeg version : %s", FFmpegHelper.getFFmpegVersion()));
        Log.i("MainActivity", " FFmpeg version : " + FFmpegHelper.getFFmpegVersion());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_VIDEO && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            showLoading(true);

            // 获取缩略图（快速显示）
            loadThumbnail(uri);

            // 解析视频信息（后台线程）
            parseVideoInfo(uri);
        }
    }

    private void parseVideoInfo(Uri uri) {
        new Thread(() -> {
            String realPath = getRealPathFromUri(this,uri);
            String info = realPath != null ?
                    FFmpegHelper.getMediaInfo(realPath) :
                    "Error: Could not get file path";

            runOnUiThread(() -> {
                tvVideoInfo.setText(info);
                showLoading(false);
            });
        }).start();
    }



    private void loadThumbnail(Uri uri) {
        new Thread(() -> {
            Bitmap thumbnail = createThumbnail(uri);
            runOnUiThread(() -> ivThumbnail.setImageBitmap(thumbnail));
        }).start();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        ivThumbnail.setAlpha(show ? 0.3f : 1.0f);
    }

    private Bitmap createThumbnail(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, uri);
            return retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void requestPermissions() {
        XXPermissions.with(this)
                .permission(Permission.READ_MEDIA_VIDEO)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean allGranted) {
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean doNotAskAgain) {
                        OnPermissionCallback.super.onDenied(permissions, doNotAskAgain);
                        Toast.makeText(MainActivity.this, "给我读取权限啊！", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}