package com.ufo.orbital;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class CropActivity extends AppCompatActivity {
    public static final String BLURRED_LICENSE_PLATE = "BLURRED_LICENSE_PLATE";

    private CropImageView cropImageView;
    private Button finishedCroppingButton;
    private Bitmap pictureToCrop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        // Get image taken / uploaded.
        Intent intent = getIntent();
        //String pictureToCropPath = intent.getParcelableExtra(MainActivity.PICTURE_TO_CROP);
        String pictureToCropPath = intent.getStringExtra(MainActivity.PICTURE_TO_CROP);
        pictureToCrop = BitmapFactory.decodeFile(pictureToCropPath);

        // Now we can remove the temporary file.
        File tempFile = new File(pictureToCropPath);
        tempFile.delete();

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
                    String fileName = "myImage";
                    try {
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        licensePlate.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                        FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
                        fo.write(bytes.toByteArray());
                        fo.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        fileName = null;
                    }
                    if (fileName != null) {
                        //goToResultActivity(licensePlate);
                        goToResultActivity(fileName);
                    }
                    else {
                        Toast.makeText(
                                CropActivity.this,
                                "File is null",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void goToResultActivity(String licensePlate) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(BLURRED_LICENSE_PLATE, licensePlate);
        startActivity(intent);
        Toast.makeText(
                CropActivity.this,
                "Starting",
                Toast.LENGTH_LONG).show();
    }
}
