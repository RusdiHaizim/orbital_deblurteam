package com.ufo.orbital;

import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class PictureContent {
    private static String TAG = "PictureContent";
    static final List<PictureItem> ITEMS = new ArrayList<>();

    private static void addItem(PictureItem item) {
        ITEMS.add(0, item);
    }

    public static void loadSavedImages(File dir) {
        ITEMS.clear();
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                String absolutePath = file.getAbsolutePath();
                String extension = absolutePath.substring(absolutePath.lastIndexOf("."));
                Log.d(TAG, absolutePath);
                Log.d(TAG, extension);
                if (extension.equals(".jpg") || extension.equals(".png")) {
                    Log.d(TAG, "trying...");
                    loadImage(file);
                    Log.d(TAG, "adding...");
                }
            }
        }
    }

    public static void loadImage(File file) {
        PictureItem newItem = new PictureItem();
        Log.d(TAG, "newItem created...");
        newItem.uri = Uri.fromFile(file);
        Log.d(TAG, "uri added...");
        newItem.date = getDateFromUri(newItem.uri);
        //newItem.date = "Unknown";
        Log.d(TAG, "date added...");
        Log.d(TAG, "trying to addItem...");
        addItem(newItem);
    }

    private static String getDateFromUri(Uri uri){
//        String[] split = uri.getPath().split("/");
//        String fileName = split[split.length - 1];
//        String fileNameNoExt = fileName.split("\\.")[0];
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String dateString = format.format(new Date(Long.parseLong(fileNameNoExt)));
//        return dateString;
        File file = new File(uri.getPath());
        ExifInterface intf = null;
        String dateString = null;
        try {
            intf = new ExifInterface(file.getAbsolutePath());
            if (intf != null) {
                dateString = intf.getAttribute(ExifInterface.TAG_DATETIME);
                Log.d(TAG, "Date Created: " + dateString);
            }
        }
        catch (Exception e) {
            Log.d(TAG, "failed Date");
        }
        if (dateString == null) {
            Date lastModDate = new Date(file.lastModified());
            dateString = lastModDate.toString();
            dateString = ap(dateString);
            Log.d(TAG, "Date Last Modified: " + dateString);
        }
        return dateString;
    }
    private static String ap(String str) {
        String rt = "";
        StringTokenizer stringTokenizer = new StringTokenizer(str, " ");
        for (int i = 0; i < 3; ++i) {
            rt += stringTokenizer.nextElement();
            rt += " ";
        }
        rt += "\n";
        rt += stringTokenizer.nextElement(); rt += " ";
        rt += "\n";
        while (stringTokenizer.hasMoreElements()) {
            rt += stringTokenizer.nextElement();
            rt += " ";
        }
        Log.d(TAG, rt);
        return rt;
    }
}
