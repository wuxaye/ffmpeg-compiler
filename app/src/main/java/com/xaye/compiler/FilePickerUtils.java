package com.xaye.compiler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class FilePickerUtils {
    private static final int REQUEST_PICK_VIDEO = 1001;

    public static void pickVideo(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        activity.startActivityForResult(intent, REQUEST_PICK_VIDEO);
    }

    public static String handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_VIDEO && resultCode == Activity.RESULT_OK) {
            return data.getData().getPath(); // 注意：实际需要处理Uri转真实路径
        }
        return null;
    }

    @SuppressLint("Range")
    public static String getRealPathFromUri(Context context, Uri uri) {
        String path = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 处理Document Uri
            String docId = DocumentsContract.getDocumentId(uri);
            String[] split = docId.split(":");
            String type = split[0];
            if ("primary".equalsIgnoreCase(type)) {
                path = Environment.getExternalStorageDirectory() + "/" + split[1];
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 处理Content Uri
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.Video.Media.DATA}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                cursor.close();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            path = uri.getPath();
        }
        return path;
    }
}
