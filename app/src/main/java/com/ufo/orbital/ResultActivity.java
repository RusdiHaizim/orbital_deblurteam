package com.ufo.orbital;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import com.jacksonandroidnetworking.JacksonParserFactory;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

public class ResultActivity extends AppCompatActivity {
    private static final String TAG = "ResultActivity";
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 0;

    //XML features
    private Button saveButton;
    private Button cancelButton;
    private ImageView afterconversion;
    private ImageView beforeconversion;
    private ProgressBar spinner;
    private SeekBar seekBar;

    // Device dimensions
    int deviceHeight;
    int deviceWidth;

    //Bitmaps to store original, result, and temporary result images respectively
    Bitmap before_conversion;
    Bitmap after_conversion;
    Bitmap tempAfter;

    //Options to hide image
    private int imageStatus; //0 when not pressed, 1 when pressed

    //To check if conversion process is done
    private boolean isDeblurred;

    //File containing the original image
    private File file;

    public static float bottomCutoff;

//    String firstPart = "51630a90.ngrok.io";
    //String firstPart = getString(R.string.firstPart);
    // 35.197.17.49

    // Server IP code
    String firstPart = "35.197.17.49";
    String secondPart = "/mobile_predict";
    String FILE_UPLOAD_URL = "http://" + firstPart + secondPart;
//    String DOWN_URL = "http://" + firstPart + "/download?filename=";
    String DOWN_URL = "";
    String fname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        isDeblurred = false;
        imageStatus = 0;

        Intent intent = getIntent();
        if (intent != null) {
            try {
                String cropToResult = intent.getStringExtra(CropActivity.BLURRED_LICENSE_PLATE);
                Log.d(TAG, cropToResult);
                before_conversion = BitmapFactory.decodeFile(cropToResult);

                Log.d(TAG, "Width: " + before_conversion.getWidth() + " | Height: " + before_conversion.getHeight());
                Log.d(TAG, "AFTER||Width: " + before_conversion.getWidth() + " | Height: " + before_conversion.getHeight());
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "OnCreate");
                Toast.makeText(
                        ResultActivity.this,
                        "Failed to get Intent",
                        Toast.LENGTH_LONG).show();
            }
        }

        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.setParserFactory(new JacksonParserFactory());

        beforeconversion = findViewById(R.id.beforeConversion); //original image
        afterconversion = findViewById(R.id.afterConversion); //result image

        // Spinner/progressBar
        spinner = findViewById(R.id.progressBar);
        spinner.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);


        // Obtain device width to scale the image shown
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;

        //Scaling the bitmap acc to device width and height
        float dp = 100;
        bottomCutoff = dp * getResources().getDisplayMetrics().density;

        //CRUCIAL BUFFER Function
        saveBitmaptoFile();

        // Slider to adjust between ORIGINAL and RESULT images
        seekBar = findViewById(R.id.seekBar);
        seekBar.setEnabled(false);
        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Write code to perform some action when progress is changed.
                    if (progress != 0) {
                        tempAfter = Bitmap.createBitmap(after_conversion, 0, 0, progress, after_conversion.getHeight());
                        afterconversion.setImageBitmap(tempAfter);
                        //Log.d(TAG, "progress: " + progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is started.
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is stopped.
                    //Toast.makeText(ResultActivity.this, "", Toast.LENGTH_SHORT).show();
                    if (seekBar.getProgress() == 0) {
                        Toast.makeText(ResultActivity.this, "Original", Toast.LENGTH_SHORT).show();
                    }
                    else if (seekBar.getProgress() == seekBar.getMax()){
                        Toast.makeText(ResultActivity.this, "Result", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Log.d(TAG, "Percentage: " + seekBar.getProgress()*100/seekBar.getMax() + "%");
//                        Toast.makeText(ResultActivity.this, "Percentage: " + seekBar.getProgress()*100/seekBar.getMax() + "%" , Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setEnabled(false);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); //Closes this activity, reverts to last activity "CropActivity"
            }
        });

        // Save button.
        saveButton = findViewById(R.id.saveButton);
        saveButton.setWidth(deviceWidth);
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check and request permissions.
                int permissionCheck = ContextCompat.checkSelfPermission(
                        ResultActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            ResultActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_EXTERNAL_STORAGE_PERMISSION_CODE);
                }

                boolean deleted = file.delete();
                if (deleted) {
                    Log.d(TAG, "pic 'file' is deleted");
                }
                else {
                    Log.d(TAG, "pic 'file' is not deleted");
                }

                File dl_file = new File(Environment.getExternalStorageDirectory() + "/aaSuperRes/Downloaded_Files/" + fname);
                try {
                    OutputStream outputStream = new FileOutputStream(dl_file);
                    after_conversion.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Code to delete cache files in storage directories
                String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                File tt1 = new File(root + "/aaSuperRes/asaved_temp");
                File tt2 = new File(root + "/aaSuperRes/atransfer");
                File[] target1 = tt1.listFiles();
                File[] target2 = tt2.listFiles();
                if (tt1.exists() && tt2.exists()) {
                    for (File fly : target1) {
                        fly.delete();
                    }
                    for (File fly : target2) {
                        fly.delete();
                    }
                }

                Log.d(TAG, fname);

                // Deletes the original image
                if (HomeFragment.image != null) {
                    HomeFragment.image.delete();
                }

                if (!dl_file.exists()) {
                    // There was an error saving.
                    Toast.makeText(
                            ResultActivity.this,
                            "Could not save the image to the gallery.",
                            Toast.LENGTH_LONG).show();
                } else {
                    // Success.
                    Toast.makeText(
                            ResultActivity.this,
                            "Image saved!",
                            Toast.LENGTH_LONG).show();

                    goToMainActivity();
                }
            }
        });
    }

    private void saveBitmaptoFile() {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File myDir = new File(root + "/aaSuperRes" + "/asaved_temp");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        String fname = "pic.png";
        file = new File(myDir, fname);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            Bitmap pictureBitmap = before_conversion; // obtaining the Bitmap
            pictureBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);

            try {
                fOut.flush(); // Not really required
                fOut.close(); // do not forget to close the stream
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Execute async task in bg to run the uploading image function
            UploadFileToServer uploadFileToServer = new UploadFileToServer();
            uploadFileToServer.execute();

            // ImageView features
            before_conversion = resize(before_conversion, deviceWidth, 10000); //resize original to scale with device
            float b_x = before_conversion.getWidth();
            float b_y = before_conversion.getHeight();
            if (b_y > deviceHeight - bottomCutoff) {
                int diff = deviceHeight - (int)bottomCutoff;
                before_conversion = resize(before_conversion, 10000, diff);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "Outside BLOCK");
        }
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_CODE) {
            // Disable the save button if permission request is denied.
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                saveButton.setEnabled(false);
            }
        }
    }
    @Override
    public void onBackPressed() {
        if (beforeconversion != null) {
            beforeconversion.setImageBitmap(null);

        }
        if (afterconversion != null) {
            afterconversion.setImageBitmap(null);
        }
        if (before_conversion != null) {
            before_conversion = null;
        }
        if (after_conversion != null) {
            after_conversion = null;
        }
        super.onBackPressed();
    }


    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private class UploadFileToServer extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spinner.setVisibility(View.VISIBLE);
            if (afterconversion != null) {
                afterconversion.setImageBitmap(null);
            }
            beforeconversion.setImageBitmap(before_conversion);
            beforeconversion.animate().alpha(1f).setDuration(5000).setListener(null);

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
            if (before_conversion != null) {
                try {
//                    before_conversion = resize(before_conversion, deviceWidth, 10000);
                    beforeconversion.setImageBitmap(before_conversion);
                    Log.d(TAG, "SUCCESSFULLY resized before_conversion");
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "null conversion");
                }
            }
            else {
                Log.d(TAG, "BEFORE CONVERSION IS NULL");
            }
            getget();
            Log.d(TAG, "After | " + temp);
            return temp;
        }

        private void getget() {
            AndroidNetworking.upload(FILE_UPLOAD_URL)
                    .addMultipartFile("file", file)
                    .addMultipartParameter("landmark_detection", "false")
                    .setTag("")
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d(TAG, "Success?");
                            Log.d(TAG, "string is " + response.toString());
                            StringTokenizer st = new StringTokenizer(response.toString(), "%");
                            st.nextToken(); fname = st.nextToken();
                            fname.replace("\\", "\\\\");
                            String stringlist[] = fname.split("\\\\");
                            fname = "";
                            for (String str : stringlist) {
                                fname += str;
                            }
                            Log.d(TAG, "fname AFT:" + fname);

                            DOWN_URL = fname;
                            //DOWN_URL += "\"";
                            Log.d(TAG, DOWN_URL);
                            try {
                                downdown();
                            }
                            catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }


                        }
                        @Override
                        public void onError(ANError anError) {
                            Log.d(TAG, "We have failed the UPLOAD...");
                            saveButton.setText(R.string.error_text);
                            spinner.setVisibility(View.GONE);
                            afterconversion.setImageBitmap(before_conversion);
                            Toast.makeText(ResultActivity.this, "Failed to Upload to Server", Toast.LENGTH_SHORT).show();
                            if (anError.getErrorCode() != 0) {
                                Log.d(TAG, "onError errorCode : " + anError.getErrorCode());
                                Log.d(TAG, "onError errorBody : " + anError.getErrorBody());
                                Log.d(TAG, "onError errorDetail : " + anError.getErrorDetail());
                            }
                            else {
                                Log.d(TAG, "onError errorDetail : " + anError.getErrorDetail());
                            }
                        }
                    });
        }

        private void downdown() {
            beforeconversion.clearColorFilter();

            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            final File myDir = new File(root + "/aaSuperRes/atransfer");
            if (!myDir.exists()) {
                myDir.mkdirs();
            }
            long unixTime = System.currentTimeMillis() / 1000L;
            fname = "hi-res" + unixTime + ".png";

            //File fileT = new File(myDir, fname);
            AndroidNetworking.download(DOWN_URL, myDir.toString(), fname)
                    .setTag("")
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .startDownload(new DownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            Log.d(TAG, "success...");
                            File fileT = new File(myDir, fname);
                            after_conversion = BitmapFactory.decodeFile(fileT.getPath());

                            //set spinner to disappear
                            spinner.setVisibility(View.GONE);

                            //set seekbar to appear
                            seekBar.setVisibility(View.VISIBLE);

                            //Set buttons
                            saveButton.setText("SAVE");
                            saveButton.setEnabled(true);
                            saveButton.setWidth(deviceWidth/2);
                            cancelButton.setVisibility(View.VISIBLE);
                            cancelButton.setEnabled(true);
                            cancelButton.setWidth(deviceWidth/2);

                            // ImageView features
                            before_conversion = resize(before_conversion, deviceWidth, 10000); //resize original to scale with device
                            after_conversion = resize(after_conversion, deviceWidth, 10000);
                            float b_x = before_conversion.getWidth();
                            float b_y = before_conversion.getHeight();
                            float dp = 200;
                            bottomCutoff = dp * getResources().getDisplayMetrics().density;

                            if (b_y > deviceHeight - bottomCutoff) {
                                int diff = deviceHeight - (int)bottomCutoff;
                                before_conversion = resize(before_conversion, 10000, diff);
                                after_conversion = resize(after_conversion, 10000, diff);
                            }

                            beforeconversion.setImageBitmap(before_conversion);
                            afterconversion.setImageBitmap(after_conversion);

                            //setting seekbars
                            Log.d(TAG, "max is: " + deviceWidth);
                            seekBar.setMax(before_conversion.getWidth());
                            seekBar.setProgress(before_conversion.getWidth()/2);
                            tempAfter = after_conversion.copy(Bitmap.Config.ARGB_8888,true);
                            tempAfter = Bitmap.createBitmap(after_conversion, 0, 0, before_conversion.getWidth()/2, after_conversion.getHeight());
                            afterconversion.setImageBitmap(tempAfter);

                            seekBar.setEnabled(true);
                            Toast.makeText(ResultActivity.this, "Image Successfully Downloaded!", Toast.LENGTH_SHORT).show();

                            beforeconversion.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //-> Hide others
                                    if (imageStatus == 0) {
                                        imageStatus = 1;
                                        seekBar.setVisibility(View.GONE);
                                        saveButton.setVisibility(View.GONE);
                                        cancelButton.setVisibility(View.GONE);
                                    }
                                    //-> Back to visible
                                    else {
                                        imageStatus = 0;
                                        seekBar.setVisibility(View.VISIBLE);
                                        saveButton.setVisibility(View.VISIBLE);
                                        cancelButton.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                            afterconversion.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //-> Hide others
                                    if (imageStatus == 0) {
                                        imageStatus = 1;
                                        seekBar.setVisibility(View.GONE);
                                        saveButton.setVisibility(View.GONE);
                                        cancelButton.setVisibility(View.GONE);
                                    }
                                    //-> Back to visible
                                    else {
                                        imageStatus = 0;
                                        seekBar.setVisibility(View.VISIBLE);
                                        saveButton.setVisibility(View.VISIBLE);
                                        cancelButton.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                        @Override
                        public void onError(ANError anError) {
                            spinner.setVisibility(View.GONE);
                            afterconversion.setImageBitmap(before_conversion);
                            saveButton.setText(R.string.error_text);
                            Toast.makeText(ResultActivity.this, "Failed to Download from Server", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "We have failed the DOWNLOAD...");
                            if (anError.getErrorCode() != 0) {
                                Log.d(TAG, "onError errorCode : " + anError.getErrorCode());
                                Log.d(TAG, "onError errorBody : " + anError.getErrorBody());
                                Log.d(TAG, "onError errorDetail : " + anError.getErrorDetail());
                            }
                            else {
                                Log.d(TAG, "onError errorDetail : " + anError.getErrorDetail());
                            }

                        }
                    });
        }
    }
}