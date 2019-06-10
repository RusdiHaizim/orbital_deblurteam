package com.ufo.orbital;

import android.Manifest;
import android.content.Intent;
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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.jacksonandroidnetworking.JacksonParserFactory;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

public class ResultActivity extends AppCompatActivity {
    private static final String TAG = "ResultActivity";
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 0;
    private ImageView licensePlateView;
    private ImageView progressView;
    private ProgressBar spinner;
    private Button saveButton;

    private Bitmap blurredLicensePlate;
    private Bitmap deblurredLicensePlate;
    private boolean isDeblurred;
    private static final int width = 192;
    private static final int height = 192;
    private Uri uri;
    String Path;

    private File file;

    String opFilePath;
    String response;
    String firstPart = "a34ae04a";
    String FILE_UPLOAD_URL = "http://" + firstPart + ".ngrok.io/";
    String fname;
    String DOWN_URL = "http://" + firstPart + ".ngrok.io/download?filename=";


    private boolean doneSaving = false;

    private void saveBitmaptoFile() {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File myDir = new File(root + "/aaSuperRes" + "/asaved_temp");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        String fname = "pic" + ".png";
        file = new File(myDir, fname);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            Bitmap pictureBitmap = blurredLicensePlate; // obtaining the Bitmap
            pictureBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            opFilePath = file.getAbsolutePath();

            try {
                fOut.flush(); // Not really required
                fOut.close(); // do not forget to close the stream
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, "File not in Gallery");
            }

            UploadFileToServer uploadFileToServer = new UploadFileToServer();
            uploadFileToServer.execute();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "Outside BLOCK");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        isDeblurred = false;

        //DOWN_URL += "\"";

        if (getIntent() != null) {
            try {
                blurredLicensePlate = BitmapFactory.decodeStream(openFileInput("myImage"));
                Log.d(TAG, "Width: " + blurredLicensePlate.getWidth() + " | Height: " + blurredLicensePlate.getHeight());
                //blurredLicensePlate = Bitmap.createScaledBitmap(blurredLicensePlate, width, height, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, "OnCreate");
                Toast.makeText(
                        ResultActivity.this,
                        "Failed to get Intent",
                        Toast.LENGTH_LONG).show();
            }
        }
        saveBitmaptoFile();

        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.setParserFactory(new JacksonParserFactory());

        licensePlateView = findViewById(R.id.deblurredLicensePlate);
        licensePlateView.setImageBitmap(blurredLicensePlate);

        // Spinner.
        spinner = findViewById(R.id.progressBar);
        spinner.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        // Progress view. It also shows the blurred picture on hover.
        progressView = findViewById(R.id.progressView);
        progressView.setColorFilter(Color.BLACK);
        progressView.setAlpha(0f);
        progressView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    progressView.clearColorFilter();
                    progressView.setImageBitmap(blurredLicensePlate);
                    progressView.setAlpha(1f);
                    return true;
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    progressView.setAlpha(0f);
                    progressView.setColorFilter(Color.BLACK);
                    return true;
                }
                return false;
            }
        });

        // Save button.
        saveButton = findViewById(R.id.saveButton);
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
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                // Save to gallery.
                String savedImageURL = MediaStore.Images.Media.insertImage(
                        getContentResolver(),
                        deblurredLicensePlate,
                        fname,
                        ""
                );

                doneSaving = true;

                if (savedImageURL == null) {
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
            final File myDir = new File(root + "/aaSuperRes" + "/adown_temp");
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
            progressView.animate().alpha(1f).setDuration(30000).setListener(null);
        }

        @Override
        protected void onPostExecute(String result) {
//            spinner.setVisibility(View.GONE);
//            licensePlateView.setImageBitmap(deblurredLicensePlate);
//            progressView.animate().alpha(0f).setDuration(100).setListener(null);
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
                            Log.d(TAG, "We have failed...");
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

            boolean deleted = file.delete();
            if (deleted) {
                Log.d(TAG, "deleted");
            }
            else {
                Log.d(TAG, "not deleted");
            }

            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            final File myDir = new File(root + "/adown_temp");
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
                            deblurredLicensePlate = BitmapFactory.decodeFile(fileT.getPath());

                            spinner.setVisibility(View.GONE);
                            licensePlateView.setImageBitmap(deblurredLicensePlate);
                            progressView.animate().alpha(0f).setDuration(100).setListener(null);
                            saveButton.setText("SAVE");
                            saveButton.setEnabled(true);
                        }
                        @Override
                        public void onError(ANError anError) {
                            Log.d(TAG, "We have failed again...");
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



//        private String upload() {
//            String outputStr = null;
//
//            URL url = null;
//            try {
//                url = new URL(FILE_UPLOAD_URL);
//                Log.d(TAG, url.toString());
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//                Log.d(TAG, "invalid URL");
//            }
//            HttpURLConnection con = null;
//            try {
//                con = (HttpURLConnection)url.openConnection();
//                Log.d(TAG, "Opened");
//            } catch (IOException e) {
//                Log.d(TAG,"Nope");
//                e.printStackTrace();
//            }
//            try {
//                //HttpURLConnection con = (HttpURLConnection)url.openConnection();
//                con.setRequestMethod("POST");
//                con.setDoInput(true);
//                con.setDoOutput(true);
//                con.setUseCaches(false);
//                con.setRequestProperty("Content-Type", "pic/png");
//
//                //con.connect();
//                Log.d(TAG, opFilePath);
//                //InputStream in = new FileInputStream(opFilePath);
//
//
//                OutputStream out = con.getOutputStream();
//                blurredLicensePlate.compress(Bitmap.CompressFormat.PNG, 100, out);
//
//                //OutputStream out = con.getOutputStream();
//                //copy(in, con.getOutputStream());
//                //out.flush();
//                out.close();
//                int status = con.getResponseCode();
//                if (status == 200) {
//                    BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
//                    for (String line = r.readLine(); line != null; line = r.readLine()) {
//                        Log.d(TAG, line);
//                        outputStr += line;
//                    }
//                }
//                else {
//                    Log.d(TAG, "gg " + status);
//                }
////                int a = con.getResponseCode();
////                if (outputStr == null) outputStr = "" + a;
////                Log.d(TAG, "tes" + outputStr);
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.d(TAG, "FAIL");
//            }
//
//            return outputStr;
//        }
//        protected long copy(InputStream input, OutputStream output)
//                throws IOException {
//            byte[] buffer = new byte[12288]; // 12K
//            long count = 0L;
//            int n = 0;
//            while (-1 != (n = input.read(buffer))) {
//                output.write(buffer, 0, n);
//                count += n;
//            }
//            return count;
//        }
//
//        private String uploadFile() {
//            String responseString = null;
//            Log.d("Log", "File path" + opFilePath);
//            HttpClient httpclient = new DefaultHttpClient();
//            HttpPost httppost = new HttpPost(FILE_UPLOAD_URL);
//
//            try {
//                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
//                        new AndroidMultiPartEntity.ProgressListener() {
//
//                            @Override
//                            public void transferred(long num) {
//                                //publishProgress((int) ((num / (float) totalSize) * 100));
//                            }
//                        });
//                ExifInterface newIntef = new ExifInterface(opFilePath);
//                newIntef.setAttribute(ExifInterface.TAG_ORIENTATION,String.valueOf(2));
//                File file = new File(opFilePath);
//                entity.addPart("pic", new FileBody(file));
//                //totalSize = entity.getContentLength();
//                httppost.setEntity(entity);
//
//                // Making server call
//                HttpResponse response = httpclient.execute(httppost);
//                HttpEntity r_entity = response.getEntity();
//
//
//                int statusCode = response.getStatusLine().getStatusCode();
//                if (statusCode == 200) {
//                    // Server response
//                    responseString = EntityUtils.toString(r_entity);
//                    Log.d("Log", responseString);
//                } else {
//                    responseString = "Error occurred! Http Status Code: "
//                            + statusCode + " -> " + response.getStatusLine().getReasonPhrase();
//                    Log.d("Log", responseString);
//                }
//
//            } catch (ClientProtocolException e) {
//                responseString = e.toString();
//                Log.d("Catch", "Client prot exception");
//            } catch (IOException e) {
//                responseString = e.toString();
//                Log.d("Catch", "IOException");
//            }
//
//            return responseString;
//        }
}





//    private void uploadImage() {
//        Log.d(TAG, "starting the upload");
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_SERVER,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            JSONObject jsonObject = new JSONObject(response);
//                            String Response = jsonObject.getString("response");
//                            Toast.makeText(ResultActivity.this, Response, Toast.LENGTH_LONG).show();
//                            Log.e(TAG, Response);
//                        }
//                        catch(JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e(TAG, "no response");
//            }
//        })
//        {
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> params = new HashMap<>();
//
//                params.put("image", imageToString(blurredLicensePlate));
//                return super.getParams();
//            }
//        };
//        MySingleton.getInstance(ResultActivity.this).addtoRequestQue(stringRequest);
//        Log.e(TAG, "ending the upload");
//    }
//    private String imageToString(Bitmap bitmap) {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
//        byte[] imgBytes = byteArrayOutputStream.toByteArray();
//        return Base64.encodeToString(imgBytes, Base64.DEFAULT);
//    }



//        mgr = getResources().getAssets();
//        new SetUpNeuralNetwork().execute();


//                jniFreeBitmapData(handler);
//                handler=null;
//            }
//            handler = predFromCaffe2(blurredLicensePlate);
//            deblurredLicensePlate = jniGetBitmapFromStoredBitmapData(handler);

//private AssetManager mgr;
//
//    static {
//        System.loadLibrary("native-lib");
//    }

//
//    //initialise assetmanager
//    public native void initCaffe2(AssetManager mgr);
//    //set bitmap in native
//    public native ByteBuffer predFromCaffe2(Bitmap bitmap);
//    //free bitmap in native
//    private native void jniFreeBitmapData(ByteBuffer handler);
//    //get bitmap from native
//    private native Bitmap jniGetBitmapFromStoredBitmapData(ByteBuffer handler);
//
//    private class SetUpNeuralNetwork extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void[] v) {
//            try {
//                initCaffe2(mgr);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//    }
