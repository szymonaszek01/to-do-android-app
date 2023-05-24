package com.app.toDo.util;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileCopyClass {

    private Context context;

    public FileCopyClass(Context context) {
        this.context = context;
    }

//    public Uri copyAttachmentToAppStorage(Uri uri) throws IOException {
//
//        File dst = new File(context.getFilesDir(), uri.getLastPathSegment());
//
//        InputStream src = context.getContentResolver().openInputStream(uri);
//        FileOutputStream fOut = context.openFileOutput(uri.getLastPathSegment(), Context.MODE_PRIVATE);
//        try (InputStream in = src) {
//            try (OutputStream out = fOut) {
//                // Transfer bytes from in to out
//                byte[] buf = new byte[1024];
//                int len;
//                while ((len = in.read(buf)) > 0) {
//                    out.write(buf, 0, len);
//                }
//            }
//        }
//
//        return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", dst);
//    }

    public Uri copyAttachmenToExternal(Uri attachmentUri) {
        String path;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            path = context.getExternalFilesDir(Environment.DIRECTORY_DCIM).toString() + "/todo";
        } else {
            path = Environment.getExternalStorageDirectory().toString() + "/todo";
        }
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdir();
        }
        File file = new File(path + "/" + attachmentUri.getLastPathSegment());
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(attachmentUri);
            createNewFile(inputStream, file);
        } catch (IOException e) {
            Log.e("file", e.getMessage());
        }
        return Uri.fromFile(file);
    }

    private void createNewFile(InputStream ins, File destination) throws IOException {
        try (FileOutputStream out = new FileOutputStream(destination)) {
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = ins.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
        }
    }

}
