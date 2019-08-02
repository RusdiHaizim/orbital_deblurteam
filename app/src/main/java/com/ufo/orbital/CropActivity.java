package com.ufo.orbital;

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

import java.io.File;
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
        String pictureToCropPath = null;
        pictureToCropPath = intent.getStringExtra(HomeFragment.PICTURE_TO_CROP);
        Log.d(TAG, "" + pictureToCropPath);

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
            pictureToCrop = BitmapFactory.decodeFile(pictureToCropPath);
        }

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
    }
}
