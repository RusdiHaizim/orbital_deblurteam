package com.ufo.orbital;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static com.ufo.orbital.HomeFragment.handleSamplingAndRotationBitmap;
//

public class CropActivity extends AppCompatActivity {
    public static final String BLURRED_LICENSE_PLATE = "BLURRED_LICENSE_PLATE";
    private static final String TAG = "CropActivity";
    private CropImageView cropImageView;
    private Button finishedCroppingButton;
    private Bitmap pictureToCrop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Get image taken / uploaded.
        Intent intent = getIntent();
        //String pictureToCropPath = intent.getParcelableExtra(HomeFragment.PICTURE_TO_CROP);
        String pictureToCropPath = intent.getStringExtra(HomeFragment.PICTURE_TO_CROP);
        Log.d(TAG, pictureToCropPath);
        //pictureToCrop = BitmapFactory.decodeFile(pictureToCropPath);

        String receivedAction = null;
        try {
            receivedAction = intent.getAction();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //If got from "Share" Function from other apps
        if (receivedAction != null && receivedAction.equals(Intent.ACTION_SEND)) {
            String receivedType = intent.getType();
            Uri receiveUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (receiveUri != null && receivedType.startsWith("image/")) {
                try {
                    //pictureToCrop = Utils.decodeUri(CropActivity.this,receiveUri, 0);
                    InputStream imageStream = getContentResolver().openInputStream(receiveUri);
                    pictureToCrop = BitmapFactory.decodeStream(imageStream);
                    pictureToCrop = handleSamplingAndRotationBitmap(getApplicationContext(), receiveUri);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //If got from "Main App"
        else {
            //String pictureToCropPath = intent.getParcelableExtra(HomeFragment.PICTURE_TO_CROP);
            //String pictureToCropPath = intent.getStringExtra(HomeFragment.PICTURE_TO_CROP);
            pictureToCrop = BitmapFactory.decodeFile(pictureToCropPath);

            // Now we can remove the temporary file.
//            File tempFile = new File(pictureToCropPath);
//            tempFile.delete();
        }



        /* Now we can remove the temporary file. */
//        File tempFile = new File(pictureToCropPath);
//        tempFile.delete();

        // Put image in the crop view.
        cropImageView = findViewById(R.id.cropImageView);
        cropImageView.setImageBitmap(pictureToCrop);
        cropImageView.setMaxZoom(16);

        finishedCroppingButton = findViewById(R.id.finishedCroppingButton);
        finishedCroppingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap licensePlate = cropImageView.getCroppedImage();

                if (licensePlate.getByteCount() > 5000 * 5000) {
                    // Cropped area cannot exceed 10MB. If it does, stay in the crop view.
                    Toast.makeText(
                            CropActivity.this,
                            "Cropped area is too big.",
                            Toast.LENGTH_LONG).show();
                } else {
//                    String fileName = "myImage";
//                    try {
//                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//                        licensePlate.compress(Bitmap.CompressFormat.PNG, 100, bytes);
//                        FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
//                        fo.write(bytes.toByteArray());
//                        fo.close();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        fileName = null;
//                    }
//                    if (fileName != null) {
//                        //goToResultActivity(licensePlate);
//                        goToResultActivity(fileName);
//                    }
//                    else {
//                        Toast.makeText(
//                                CropActivity.this,
//                                "No such file exists...",
//                                Toast.LENGTH_LONG).show();
//                    }

                    // Save the image temporarily.
                    File dir = new File(Environment.getExternalStorageDirectory() + "/aaSuperRes" + "/atransfer/");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    OutputStream outputStream = null;
                    File file = new File(Environment.getExternalStorageDirectory() + "/aaSuperRes/atransfer/myImage.png");
                    try {
                        outputStream = new FileOutputStream(file);
                        licensePlate.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        outputStream.close();
                        goToResultActivity(file.getAbsolutePath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    private void goToResultActivity(String licensePlate) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(BLURRED_LICENSE_PLATE, licensePlate);
        startActivity(intent);
//        Toast.makeText(
//                CropActivity.this,
//                "Starting",
//                Toast.LENGTH_SHORT).show();
    }
}


//public class CropActivity extends AppCompatActivity {
//    public static final String BLURRED_LICENSE_PLATE = "BLURRED_LICENSE_PLATE";
//
//    private CropImageView cropImageView;
//    private Button finishedCroppingButton;
//    private Bitmap pictureToCrop;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_crop);
//
//        // To prevent the user from disrupting the cropping by turning the phone
//        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//
//        // Get image taken / uploaded.
//        Intent intent = getIntent();
//
//        //String receivedAction = intent.getAction();
//        //String receivedType = intent.getType();
////
////        if (receivedAction.equals(Intent.ACTION_SEND) && receivedType.startsWith("image/")) {
////            Uri receiveUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
////            if (receiveUri != null) {
////                try {
////                    //pictureToCrop = Utils.decodeUri(CropActivity.this,receiveUri, 0);
////                    InputStream imageStream = getContentResolver().openInputStream(receiveUri);
////                    pictureToCrop = BitmapFactory.decodeStream(imageStream);
////                    pictureToCrop = handleSamplingAndRotationBitmap(getApplicationContext(), receiveUri);
////                }
////                catch (Exception e) {
////                    e.printStackTrace();
////                }
////            }
////        }
////        else {
//            //String pictureToCropPath = intent.getParcelableExtra(HomeFragment.PICTURE_TO_CROP);
//            String pictureToCropPath = intent.getStringExtra(HomeFragment.PICTURE_TO_CROP);
//            pictureToCrop = BitmapFactory.decodeFile(pictureToCropPath);
//
//            // Now we can remove the temporary file.
//            File tempFile = new File(pictureToCropPath);
//            tempFile.delete();
//
//        //}
//
//        // Put image in the crop view.
//        cropImageView = findViewById(R.id.cropImageView);
//        cropImageView.setImageBitmap(pictureToCrop);
//        cropImageView.setMaxZoom(16);
//
//        finishedCroppingButton = findViewById(R.id.finishedCroppingButton);
//        finishedCroppingButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.cropping_button_selector));
//        finishedCroppingButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Bitmap licensePlate = cropImageView.getCroppedImage();
//
//                if (licensePlate.getByteCount() > 5000 * 5000) {
//                    // Cropped area cannot exceed 10MB. If it does, stay in the crop view.
//                    Toast.makeText(
//                            CropActivity.this,
//                            "Cropped area is too big.",
//                            Toast.LENGTH_LONG).show();
//                } else {
//                    String fileName = "myImage";
//                    try {
//                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//                        licensePlate.compress(Bitmap.CompressFormat.PNG, 100, bytes);
//                        FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
//                        fo.write(bytes.toByteArray());
//                        fo.close();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        fileName = null;
//                    }
//                    if (fileName != null) {
//                        //goToResultActivity(licensePlate);
//                        goToResultActivity(fileName);
//                    }
//                    else {
//                        Toast.makeText(
//                                CropActivity.this,
//                                "File is null",
//                                Toast.LENGTH_LONG).show();
//                    }
//                }
//            }
//        });
//    //}
//
//    private void goToResultActivity(String licensePlate) {
//        Intent intent = new Intent(this, ResultActivity.class);
//        intent.putExtra(BLURRED_LICENSE_PLATE, licensePlate);
//        startActivity(intent);
//        Toast.makeText(
//                CropActivity.this,
//                "Starting",
//                Toast.LENGTH_LONG).show();
//    }
//}
