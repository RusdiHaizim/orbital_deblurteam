package com.ufo.orbital;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
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
    private ImageView afterconversion;
    private ImageView beforeconversion;
    private ProgressBar spinner;
    private Button saveButton;
    private Button cancelButton;

    SeekBar seekBar;
    Bitmap tempBefore;
    Bitmap tempAfter;
    int deviceHeight;
    int deviceWidth;


    Bitmap before_conversion;
    Bitmap after_conversion;
    private boolean isDeblurred;
//    private static final int width = 192;
//    private static final int height = 192;

    private File file;

    //String firstPart = "c5d0c996.ngrok.io";
//    String FILE_UPLOAD_URL = "http://" + firstPart + ".ngrok.io/";
//    String DOWN_URL = "http://" + firstPart + ".ngrok.io/download?filename=";

    //String firstPart = getString(R.string.firstPart);
    // 35.197.17.49
    String firstPart = "35.197.17.49";
    String FILE_UPLOAD_URL = "http://" + firstPart;
    String DOWN_URL = "http://" + firstPart + "/download?filename=";
    String fname;

    private int prevProg;



    private boolean doneSaving = false;

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
            pictureBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate

            try {
                fOut.flush(); // Not really required
                fOut.close(); // do not forget to close the stream
            } catch (IOException e) {
                e.printStackTrace();
            }

            //useless function that just caches the public Pictures directory
//            try {
//                MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                Log.d(TAG, "File not in Gallery");
//            }

            UploadFileToServer uploadFileToServer = new UploadFileToServer();
            uploadFileToServer.execute();


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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        isDeblurred = false;

        //get device width to scale image shown
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;

        Intent intent = getIntent();
        if (intent != null) {
            try {
                //before_conversion = BitmapFactory.decodeStream(openFileInput("myImage"));
                String cropToResult = intent.getStringExtra(CropActivity.BLURRED_LICENSE_PLATE);
                Log.d(TAG, cropToResult);
                before_conversion = BitmapFactory.decodeFile(cropToResult);

                Log.d(TAG, "Width: " + before_conversion.getWidth() + " | Height: " + before_conversion.getHeight());
                //before_conversion = Bitmap.createScaledBitmap(before_conversion, width, height, true);
                //before_conversion = resize(before_conversion, 128, 128);
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
        //saveBitmaptoFile();

        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.setParserFactory(new JacksonParserFactory());

        saveBitmaptoFile();

        afterconversion = findViewById(R.id.afterConversion);
        before_conversion = resize(before_conversion, deviceWidth, 100000);
        afterconversion.setImageBitmap(before_conversion);

        // Spinner.
        spinner = findViewById(R.id.progressBar);
        spinner.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        // Progress view. It also shows the blurred picture on hover.
        beforeconversion = findViewById(R.id.beforeConversion);

//        beforeconversion.setColorFilter(Color.BLACK);
//        beforeconversion.setAlpha(0f);
//        beforeconversion.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
//                    beforeconversion.clearColorFilter();
//                    beforeconversion.setImageBitmap(before_conversion);
//                    beforeconversion.setAlpha(1f);
//                    return true;
//                }
//                else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
//                    beforeconversion.setAlpha(0f);
//                    beforeconversion.setColorFilter(Color.BLACK);
//                    return true;
//                }
//                return false;
//            }
//        });

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
                        Log.d(TAG, "progress: " + progress);
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
                finish();

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
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
//                // Save to gallery.
//                String savedImageURL = MediaStore.Images.Media.insertImage(
//                        getContentResolver(),
//                        after_conversion,
//                        fname,
//                        ""
//                );

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

                String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                File tt = new File(root + "/aaSuperRes/atransfer");
                File[] target = tt.listFiles();
                for (File fly : target) {
                    fly.delete();
                }


                Log.d(TAG, fname);

                // Deletes the original image
                if (HomeFragment.image != null) {
                    HomeFragment.image.delete();
                }

                doneSaving = true;

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

    @Override
    protected void onResume() {
        super.onResume();

        if (!isDeblurred) {
            isDeblurred = true;
            new BackgroundDeblurring().execute();
            //new UploadFileToServer().execute();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SHUTTING DOWN");
        if (!doneSaving) {
            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            final File myDir = new File(root + "/aaSuperRes/atransfer");
            File ft = new File(myDir, fname);
            Log.d(TAG, ft.toString());
            if (ft.exists()) {
                ft.delete();
            }
        }

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

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Asynchronous task used to deblur the license plate in the background.
    private class BackgroundDeblurring extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            spinner.setVisibility(View.VISIBLE);
            beforeconversion.animate().alpha(1f).setDuration(30000).setListener(null);
        }

        @Override
        protected void onPostExecute(String result) {
//            spinner.setVisibility(View.GONE);
//            afterconversion.setImageBitmap(afterConversion);
//            beforeConversion.animate().alpha(0f).setDuration(100).setListener(null);
//            saveButton.setText("SAVE");
//            saveButton.setEnabled(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = null;
            //uploadImage();

            //uploadFile();
            return result;
        }

    }
    private class UploadFileToServer extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(String result) {
//            boolean deleted = file.delete();
//            if (deleted) {
//                Log.d(TAG, "deleted");
//            }
//            else {
//                Log.d(TAG, "not deleted");
//            }


        }
        @Override
        protected String doInBackground(Void... params) {
            Log.d(TAG, "Before");
            //String temp = uploadFile();
            String temp = null;
            if (before_conversion != null) {
                try {
                    before_conversion = resize(before_conversion, deviceWidth, 100000);
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
            //downdown();

            Log.d(TAG, "After | " + temp);
            return temp;
        }
        private void getget() {

            AndroidNetworking.upload(FILE_UPLOAD_URL)
                    .addMultipartFile("file", file)
                    .setTag("")
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "Success?");
                            Log.d(TAG, response.toString());
                            //String tt;
                            StringTokenizer st = new StringTokenizer(response.toString(), "%");
                            st.nextToken(); fname = st.nextToken();
                            DOWN_URL += fname;
                            //DOWN_URL += "\"";
                            Log.d(TAG, DOWN_URL);
                            downdown();

                        }
                        @Override
                        public void onError(ANError anError) {
                            Log.d(TAG, "We have failed the UPLOAD...");
                            if (anError.getErrorCode() != 0) {
                                Log.d(TAG, "onError errorCode : " + anError.getErrorCode());
                                Log.d(TAG, "onError errorBody : " + anError.getErrorBody());
                                Log.d(TAG, "onError errorDetail : " + anError.getErrorDetail());

                                spinner.setVisibility(View.GONE);
                                afterconversion.setImageBitmap(before_conversion);
                                Toast.makeText(ResultActivity.this, "Failed to Upload to Server", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Log.d(TAG, "onError errorDetail : " + anError.getErrorDetail());
                            }
                        }
                    });
        }
        private void downdown() {
            beforeconversion.clearColorFilter();
            beforeconversion.setImageBitmap(before_conversion);
            //beforeconversion.setAlpha(1f);



            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            //final File myDir = new File(root + "/aaSuperRes/Downloaded_Files");
            final File myDir = new File(root + "/aaSuperRes/atransfer");
            if (!myDir.exists()) {
                myDir.mkdirs();
            }

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

//                            beforeconversion.setVisibility(View.GONE);

                            //beforeconversion.setImageAlpha(0);

                            before_conversion = resize(before_conversion, deviceWidth, 100000);
                            beforeconversion.setImageBitmap(before_conversion);
                            after_conversion = resize(after_conversion, deviceWidth, 100000);
                            //afterconversion.setImageBitmap(after_conversion);


                            //TESTING
                            seekBar.setMax(deviceWidth);
                            Log.d(TAG, "max is: " + deviceWidth);
                            seekBar.setProgress(deviceWidth);
                            tempAfter = after_conversion.copy(Bitmap.Config.ARGB_8888,true);
                            afterconversion.setImageBitmap(tempAfter);


//                            beforeconversion.animate().alpha(0f).setDuration(100).setListener(null);

                            seekBar.setEnabled(true);
                            Toast.makeText(ResultActivity.this, "Image Successfully Downloaded!", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onError(ANError anError) {
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
