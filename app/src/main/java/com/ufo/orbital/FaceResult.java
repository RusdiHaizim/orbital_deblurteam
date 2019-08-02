package com.ufo.orbital;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

public class FaceResult extends AppCompatActivity implements ItemFragment2.OnListFragmentInteractionListener {
    int status = 0;
    public static final String TAG = "FaceResult";
    public static float bottomCutoff;
    public static int deviceHeight;
    public static int deviceWidth;
    private static int currProg = -1;

    private RecyclerView.Adapter recyclerViewAdapter;
    private RecyclerView recyclerView;

    Button saveBut;
    Button cancelBut;
    private File fileToUpload;

    public static SeekBar faceSeek;
    private ProgressBar spinner;
    public static ImageView upscaledImage;
    public static ImageView upscaledImageResult;
    private static Bitmap afterBitmap;
    private static Bitmap afterBitmapTrans;

    boolean firstDone = false;
    int totalDL_count;
    int currDL_count;

    String first = "35.197.17.49";
    String second = "/mobile_predict";
    String UPLOAD_URL = "http://" + first + second;
    String DOWN_MAIN = "http://" + "first" + "/download?filename=";
    String DOWN_URL = null;

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    private void deleteTransferFiles() {
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scrolling_activity2);
        setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        // Obtain device width to scale the image shown
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;
        Log.d(TAG, "H0: " + deviceHeight + " | " + "W0: " + deviceWidth);

        //Code for scaling bitmap according to device's width and height
        float dp = 275;
        bottomCutoff = dp * getResources().getDisplayMetrics().density;

        upscaledImage = findViewById(R.id.upScaledFace);
        upscaledImageResult = findViewById(R.id.upScaledFaceResult);

        saveBut = findViewById(R.id.faceSav);
        cancelBut = findViewById(R.id.faceCan);
        saveBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File old_place = new File(Environment.getExternalStorageDirectory() + "/aaSuperRes/faceTransfer/");
                File[] targetTemp = old_place.listFiles();
                File new_place = new File(Environment.getExternalStorageDirectory() + "/aaSuperRes/Downloaded_Files");
                long unixTime = System.currentTimeMillis() / 1000L;
                long count = 0;
                for (File fly : targetTemp) {
                    count++;
                    Log.d(TAG, "fileInside:" + fly.getAbsolutePath());

                    String targetName = "/face-" + unixTime + count;
                    targetName = new_place + targetName + ".png";
                    File targetFile = new File(targetName);
                    Log.d(TAG, "target file name:" + targetFile.getAbsolutePath());

                    String conditional = fly.getAbsolutePath();
                    String stringlist[] = conditional.split("&");
                    conditional = stringlist[stringlist.length - 1];
                    char check = conditional.charAt(0);
                    Log.d(TAG, "conditional:" + conditional);

                    if (check == 'b') {
                        Log.d(TAG, "trying to write:" + fly.getAbsolutePath());
                        Bitmap tempBitmap = null;
                        try {
                            tempBitmap = BitmapFactory.decodeFile(fly.getAbsolutePath());
                        } catch (Exception e) {
                            Log.e(TAG + "decode", e.toString());
                        }

                        try {
                            OutputStream outputStream = new FileOutputStream(targetFile);
                            tempBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                            outputStream.close();
                            Log.d("Saving", "success:" + targetName);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("Saving", "error");
                        }
                    }
                }
                deleteTransferFiles();
                goToMainActivity();

            }
        });
        cancelBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTransferFiles();
                finish();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            try {
                String filepath = intent.getStringExtra(FaceConfirm.CONFIRM_CODE);
                fileToUpload = new File(filepath);
                Log.d(TAG, "Got the file:" + filepath);
            }
            catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }

        // Spinner/progressBar
        spinner = findViewById(R.id.faceProgressBar);
        spinner.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        initRecycler();

        faceSeek = findViewById(R.id.faceSeekBar);
        faceSeek.setEnabled(false);

        faceSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress != 0) {
                    try {
                        if (afterBitmap != null && afterBitmapTrans != null) {
                            afterBitmapTrans = Bitmap.createBitmap(afterBitmap, 0, 0, progress, afterBitmap.getHeight());
                            upscaledImageResult.setImageBitmap(afterBitmapTrans);
                            currProg = progress;
                        }
                    } catch (Exception e) {
                        Log.e(TAG + "seek", e.toString());
                        if (afterBitmap == null) {
                            Log.d(TAG, "AFTBITMAP IS NULL!!!!");
                        }
                        if (afterBitmapTrans == null) {
                            Log.d(TAG, "TRANS BITMAP IS NULL!!!!");
                        }
                    }
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Write code to perform some action when touch is started.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "progress" + faceSeek.getMax() + "devicewidth" + deviceWidth);
                if (seekBar.getProgress() == 0) {
                    Toast.makeText(FaceResult.this, "Original", Toast.LENGTH_SHORT).show();
                } else if (seekBar.getProgress() == seekBar.getMax()) {
                    Toast.makeText(FaceResult.this, "Result", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Percentage: " + seekBar.getProgress() * 100 / seekBar.getMax() + "%");
                }
            }
        });

        // Execute async task in bg to run the uploading image function
        UploadFileToServer uploadFileToServer = new UploadFileToServer();
        uploadFileToServer.execute();
    }

    private void initRecycler() {
        if (recyclerViewAdapter == null) {
            recyclerView = findViewById(R.id.main_fragment2);
            recyclerViewAdapter = recyclerView.getAdapter();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                File myDir = new File(root + "/aaSuperRes/faceTransfer");
                try {
                    FaceContent.loadSavedImages(myDir);
                    recyclerViewAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Changing data set");
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "Failed to log Recycler");
                }
            }
        });
    }

    private static Bitmap autoScaleBitmap(Bitmap bitmap) {
        Bitmap tempBitmap = null;
        try {
            tempBitmap = resize(bitmap, deviceWidth, 10000);
        }
        catch (Exception e) {
            Log.e(TAG + "auto", e.toString());
        }
        float b_x = 0, b_y = 0;
        if (tempBitmap != null) {
            b_x = tempBitmap.getWidth();
            b_y = tempBitmap.getHeight();
        }
        Log.d(TAG, "H: " + b_x + " | " + "W: " + b_y);
        if (tempBitmap != null && b_y > deviceHeight - bottomCutoff) {
            int diff = deviceHeight - (int)bottomCutoff;
            Log.d(TAG, "Diff: " + diff);
            try {
                tempBitmap = resize(bitmap, 10000, diff);
            }
            catch (Exception e) {
                Log.e(TAG + "auto", e.toString());
            }
            Log.d(TAG, "NH: " + tempBitmap.getHeight() + " | " + "NW: " + tempBitmap.getWidth());
        }
        return tempBitmap;
    }
    @Override
    public void onBackPressed() {
        if (upscaledImage != null) {
            upscaledImage.setImageBitmap(null);

        }
        if (upscaledImageResult != null) {
            upscaledImageResult.setImageBitmap(null);
        }
        if (afterBitmap != null) {
            afterBitmap = null;
//            afterBitmap.recycle();
        }
        if (afterBitmapTrans != null) {
//            afterBitmapTrans.recycle();
            afterBitmapTrans = null;
        }
        super.onBackPressed();
    }


    //recycler -> here, to display main image
    public static void setMainImage(String bitmapStr1, String bitmapStr2) {
        faceSeek.setVisibility(View.VISIBLE);
        Bitmap bitmap1 = null;
        Bitmap bitmap2 = null;

        if (upscaledImage != null) {
            upscaledImage.setImageBitmap(null);
        }
        if (upscaledImageResult != null) {
            upscaledImageResult.setImageBitmap(null);
        }

        if (bitmapStr1 == null) {
            assert upscaledImage != null;
            upscaledImage.setImageBitmap(null);
            upscaledImageResult.setImageBitmap(null);
            Log.d("setMainImage:", "null path filename");
        }
        else {
            try {
                bitmap1 = BitmapFactory.decodeFile(bitmapStr1);
                bitmap1 = autoScaleBitmap(bitmap1);
                if (bitmap1 != null) {
                    Log.d(TAG, "BITMAP1 IS NOT NULL");
                    upscaledImage.setImageBitmap(bitmap1);
                }


            } catch (Exception e) {
                Log.e(TAG + "decode", e.toString());
            }

            try {
                bitmap2 = BitmapFactory.decodeFile(bitmapStr2);
                bitmap2 = autoScaleBitmap(bitmap2);
                if (bitmap2 != null) {
                    Log.d(TAG, "BITMAP2 IS NOT NULL");
                    upscaledImageResult.setImageBitmap(bitmap2);
                }


            } catch (Exception e) {
                Log.e(TAG + "decode", e.toString());
            }

            try {
                afterBitmap = bitmap2.copy(Bitmap.Config.ARGB_8888, true);
            } catch (Exception e) {
                Log.e(TAG, "Failed to copy afterBitmap");
            }
            try {
                afterBitmapTrans = bitmap2.copy(Bitmap.Config.ARGB_8888, true);
            } catch (Exception e) {
                Log.e(TAG, "Failed to copy afterBitmapTrans");
            }
        }
        try {
            if (afterBitmap != null && afterBitmapTrans != null) {
                afterBitmapTrans = Bitmap.createBitmap(afterBitmap, 0, 0, currProg, afterBitmap.getHeight());
                upscaledImageResult.setImageBitmap(afterBitmapTrans);
                Log.d("SETTING", "MAIN IMAGE " + currProg);
            }
        } catch (Exception e) {
            Log.e(TAG + "seek", e.toString());
            if (afterBitmap == null) {
                Log.d(TAG, "AFTBITMAP IS NULL!!!!");
            }
            if (afterBitmapTrans == null) {
                Log.d(TAG, "TRANS BITMAP IS NULL!!!!");
            }
        }
    }

    @Override
    public void onListFragmentInteraction(String bitmapStr1, String bitmapStr2) {
        Log.d(TAG + "OLF", bitmapStr1 + " | " + bitmapStr2);
        setMainImage(bitmapStr1, bitmapStr2);
    }

    private class UploadFileToServer extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String result) {
            //
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.d(TAG, "Before");
            //String temp = uploadFile();
            String temp = null;

            getget();
            Log.d(TAG, "After | " + temp);
            Log.d(TAG, "Loading images");
            return temp;
        }
    }

    private void getget() {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        final File myDir = new File(root + "/aaSuperRes/faceTransfer");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        deleteTransferFiles();
        Log.d(TAG, "trying to upload...");
        AndroidNetworking.upload(UPLOAD_URL)
                .addMultipartFile("file", fileToUpload) //file here
                .addMultipartParameter("landmark_detection", "true")
                .setTag("")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Upload_Success");
                        Log.d(TAG, "String is " + response.toString());

                        //settling '\'
                        String nextStr = response.toString();
                        StringTokenizer st = new StringTokenizer(response.toString(), "%");
                        st.nextToken();
                        nextStr = st.nextToken();
                        Log.d(TAG, "nextStr:" + nextStr);
                        nextStr.replace("\\", "\\\\");
                        String stringlist[] = nextStr.split("\\\\");
                        nextStr = "";
                        for (String str : stringlist) {
                            nextStr += str;
                        }
                        Log.d(TAG, "nextStr AFT:" + nextStr);

                        //splitting files
                        StringTokenizer stringTokenizer = new StringTokenizer(nextStr, ",");
                        totalDL_count = 0;
                        int count = 0;
                        int hasFailed = 1;
                        String tempstr = "";
                        while (stringTokenizer.hasMoreElements()) {
                             tempstr = stringTokenizer.nextToken();
                            DOWN_URL = tempstr;
                            Log.d(TAG, "DL_Link: " + tempstr);
                            try {
                                hasFailed = downdown(count);
                                Log.d(TAG, "code:" + hasFailed);
                            }
                            catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                            count++;
                            totalDL_count++;
                        }
                        Log.d(TAG+"getget", "after while loop");

                        Log.d(TAG, "Done download");
                        //downdown();
                        //
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, "We have failed the UPLOAD...");
                        //
                        spinner.setVisibility(View.GONE);
                        Toast.makeText(FaceResult.this, "Failed to upload to server", Toast.LENGTH_SHORT).show();
                        if (anError.getErrorCode() != 0) {
                            Log.d(TAG, "onError errorCode : " + anError.getErrorCode());
                            Log.d(TAG, "onError errorBody : " + anError.getErrorBody());
                            Log.d(TAG, "onError errorDetail : " + anError.getErrorDetail());
                        } else {
                            Log.d(TAG, "onError errorDetail : " + anError.getErrorDetail());
                        }
                    }
                });
    }

    private int downdown(int count) {
        status = 0;
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        final File myDir = new File(root + "/aaSuperRes/faceTransfer");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        String filename = "face&" + count/2 + "&";
        if (count % 2 == 0) {
            filename += "a";
        }
        else {
            filename += "b";
        }
        final String finalFilename = filename;
        AndroidNetworking.download(DOWN_URL, myDir.toString(), filename + ".png")
                .setTag("")
                .setPriority(Priority.MEDIUM)
                .build()
                .startDownload(new DownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        Log.d(TAG, "Download_Success");
                        Log.d(TAG, "success...");

                        firstDone = true;

                        String pathName = myDir.getAbsolutePath() + "/" + finalFilename + ".png";

                        upscaledImage.setImageBitmap(null);
                        upscaledImageResult.setImageBitmap(null);

                        //result
                        spinner.setVisibility(View.GONE);

                        saveBut.setVisibility(View.VISIBLE);
                        cancelBut.setVisibility(View.VISIBLE);

                        faceSeek.setEnabled(true);
                        faceSeek.setMax(deviceWidth);
                        currProg = deviceWidth/2;
                        faceSeek.setProgress(currProg);
                        currDL_count++;
                        if (currDL_count == totalDL_count) {
                            FaceViewAdapter.justLaunched = true;
                        }

                        Log.d(TAG, "Configuring setttings");

                        try {
                            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                            File Dir = new File(root + "/aaSuperRes/faceTransfer");
                            FaceContent.loadSavedImages(Dir);
                            recyclerViewAdapter.notifyDataSetChanged();
                            Log.d(TAG, "Changing");
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            Log.d(TAG, "Failed to log Recycler");
                        }

                        status = 1;
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, "We have failed the DOWNLOAD...");
                        spinner.setVisibility(View.GONE);
                        if (anError.getErrorCode() != 0) {
                            Log.d(TAG, "onError errorCode : " + anError.getErrorCode());
                            Log.d(TAG, "onError errorBody : " + anError.getErrorBody());
                            Log.d(TAG, "onError errorDetail : " + anError.getErrorDetail());
                        } else {
                            Log.d(TAG, "onError errorDetail : " + anError.getErrorDetail());
                        }


                    }
                });
        return status;
    }

    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;
            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }
}
