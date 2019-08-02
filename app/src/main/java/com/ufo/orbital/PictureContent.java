package com.ufo.orbital;

import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    Map< String, String> monthlist = new HashMap<String, String>();

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
                if (extension.equals(".jpg") || extension.equals(".png") || extension.equals(".jfif")) {
                    Log.d(TAG, "trying...");
                    loadImage(file);
                    Log.d(TAG, "adding...");
                }
            }
        }
        Collections.sort(ITEMS, new Comparator<PictureItem>() {
            @Override
            public int compare(PictureItem o1, PictureItem o2) {
                //return o1.name.compareTo(o2.name);
                return o2.priority.compareTo(o1.priority);
            }
        });

    }

    public static void loadImage(File file) {
        PictureItem newItem = new PictureItem();
        Log.d(TAG, "newItem created...");
        newItem.uri = Uri.fromFile(file);
        Log.d(TAG, "uri added...");
        newItem.date = getDateFromUri(newItem.uri, newItem);
        //newItem.date = "Unknown";
        Log.d(TAG, "date added...");
        Log.d(TAG, "trying to addItem...");
        addItem(newItem);
    }

    private static String getDateFromUri(Uri uri, PictureItem currItem){
//        String[] split = uri.getPath().split("/");
//        String fileName = split[split.length - 1];
//        String fileNameNoExt = fileName.split("\\.")[0];
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String dateString = format.format(new Date(Long.parseLong(fileNameNoExt)));
//        return dateString;
        File file = new File(uri.getPath());
        ExifInterface intf = null;
        String dateString = null;

        Date lastModDate = new Date(file.lastModified());
        dateString = lastModDate.toString();
        dateString = appending(dateString, currItem);
        Log.d(TAG, "Date Last Modified: " + dateString);

        return dateString;
    }
    private static String appending(String str, PictureItem currItem) {
        String rt = "";
        String tempstr = "";
        currItem.priority = "";
        StringTokenizer stringTokenizer = new StringTokenizer(str, " ");
        for (int i = 0; i < 3; ++i) {
            tempstr = stringTokenizer.nextElement().toString();
            rt += tempstr;
            if (i == 1) {
                String shortMonth = tempstr.substring(0, 3);
                currItem.priority += FileFragment.monthlist.get(shortMonth);
                Log.d("Months", "M: " + tempstr + " | Val: " + FileFragment.monthlist.get(shortMonth));
            }
            if (i == 2) {
                currItem.priority += tempstr;
            }
            rt += " ";
        }
        rt += "\n";
        String tempstr2 = stringTokenizer.nextElement().toString();
        currItem.priority += tempstr2;
        rt += tempstr2; rt += " ";
        rt += "\n";
        while (stringTokenizer.hasMoreElements()) {
            tempstr = stringTokenizer.nextElement().toString();
            rt += tempstr;
            rt += " ";
        }
        currItem.priority = tempstr + currItem.priority;
        Log.d("PRIORITY", currItem.priority);
        Log.d(TAG, rt);
        return rt;
    }
}
