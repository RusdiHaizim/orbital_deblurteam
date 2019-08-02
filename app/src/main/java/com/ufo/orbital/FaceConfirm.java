package com.ufo.orbital;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class FaceConfirm extends AppCompatActivity {
    public static final String CONFIRM_CODE = "CONFIRM_CODE";
    private static final String TAG = "FaceConfirmActivity";
    private ImageView confirmedFace;
    private Button btnCfm;
    private Bitmap confirmed_bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Get image taken / uploaded.
        Intent intent = getIntent();
        String pictureToCfmPath = null;
        pictureToCfmPath = intent.getStringExtra(HomeFragment.PICTURE_TO_CROP);
        Log.d(TAG, "" + pictureToCfmPath);

        confirmed_bitmap = BitmapFactory.decodeFile(pictureToCfmPath);

        // Put image in the crop view.
        confirmedFace = findViewById(R.id.confirmView);
        confirmedFace.setImageBitmap(confirmed_bitmap);

        btnCfm = findViewById(R.id.finishedConfirm);
        btnCfm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //delete cache
                String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                final File myDir = new File(root + "/aaSuperRes/faceTransfer");
                if (!myDir.exists()) {
                    myDir.mkdirs();
                }
                File[] clearing = myDir.listFiles();
                if (myDir.exists()) {
                    for (File fly : clearing) {
                        fly.delete();
                    }
                }

                // Save the image temporarily.
                File dir = new File(Environment.getExternalStorageDirectory() + "/aaSuperRes" + "/atransfer/");
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                OutputStream outputStream = null;
                File file = new File(Environment.getExternalStorageDirectory() + "/aaSuperRes/atransfer/myImage.png");
                try {
                    outputStream = new FileOutputStream(file);
                    confirmed_bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();
                    goToResultActivity(file.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void goToResultActivity(String bitmapPath) {
        Intent intent = new Intent(this, FaceResult.class);
        intent.putExtra(CONFIRM_CODE, bitmapPath);
        startActivity(intent);
    }
}
