package com.ufo.orbital;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FaceContent {
    private static String TAG = "FaceContent";
    static final List<FaceItem> ITEMS = new ArrayList<>();
    static final List<FaceItem> ITEMS2 = new ArrayList<>();

    public static void loadSavedImages(File dir) {
        ITEMS.clear();
        ITEMS2.clear();
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                String strFile = file.getAbsolutePath();
                Log.d("face:", strFile);

                String listStr[] = strFile.split("&");
                int a = Integer.parseInt(listStr[1]);
                String b = listStr[2];
                for (String test : listStr) {
                    Log.d("list string", test);
                }
                char c = b.charAt(0);

//                int a = 0;
//                String b = "a";

                Log.d(TAG, "trying to load " + strFile);
                loadImage(file, a, c); // filename, 0, a
            }
        }

    }

    public static void loadImage(File file, int a, char type) {
        FaceItem newItem = new FaceItem();
        Log.d(TAG, "newItem created...");
        newItem.facePath = file.getAbsolutePath();
        newItem.faceNum = a;
        if (type == 'a') {
            addItem(newItem, 0);
            Log.d(TAG, "adding to ITEMS...");
        }
        else {
            addItem(newItem, 1);
            Log.d(TAG, "adding to ITEMS2...");
        }
    }

    private static void addItem(FaceItem item, int type) {
        if (type == 0) {
            ITEMS.add(0, item);
        }
        else {
            ITEMS2.add(0, item);
        }
        Collections.sort(ITEMS, new Comparator<FaceItem>() {
            @Override
            public int compare(FaceItem o1, FaceItem o2) {
                //return o1.name.compareTo(o2.name);
                if (o2.faceNum > o1.faceNum) {
                    return 1;
                }
                else {
                    return -1;
                }
            }
        });
        Collections.sort(ITEMS2, new Comparator<FaceItem>() {
            @Override
            public int compare(FaceItem o1, FaceItem o2) {
                //return o1.name.compareTo(o2.name);
                if (o2.faceNum > o1.faceNum) {
                    return 1;
                }
                else {
                    return -1;
                }
            }
        });
    }
}
