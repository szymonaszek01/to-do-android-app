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

import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Builder
public class FileCopyClass {

    private final Context context;

    public Uri copyAttachmentToExternal(Uri attachmentUri) {
        String path = context.getExternalFilesDir(Environment.DIRECTORY_DCIM).toString() + "/todo";
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
