package com.ufo.orbital;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    public static final String PICTURE_TO_CROP = "PICTURE_TO_CROP";
    private int UPLOAD_PICTURE_REQUEST_CODE = 1;
    private int STORAGE_PERMISSION_CODE = 2;
    private int TAKE_PICTURE_REQUEST_CODE = 3;

    private ImageButton takePictureButton;
    private ImageButton uploadPictureButton;
    ImageButton btnF;
    ImageButton btnM;
    ImageButton btnD;
    private int EMAIL_REQUEST_CODE = 100;

    String currentPhotoPath;
    File destination;
    Uri photoURI;

    public static File image = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File tt1 = new File(root + "/aaSuperRes/asaved_temp");
        File tt2 = new File(root + "/aaSuperRes/atransfer");
        File[] target1 = tt1.listFiles();
        File[] target2 = tt2.listFiles();
        if (tt1.exists() && tt2.exists()) {
            for (File file : target1) {
                file.delete();
            }
            for (File file : target2) {
                file.delete();
            }
        }


        //Permissions initializer
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission already granted");
        }
        else {
            Log.d(TAG, "Permission not granted, going to request permission");
            requestStoragePermission();
        }


        //button for feedback
        btnF = view.findViewById(R.id.btn_feedback);
        btnF.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_button));
        btnF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();
            }
        });

        //button to take picture from gallery
        uploadPictureButton = view.findViewById(R.id.uploadButton);
        uploadPictureButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.list_selector_background));
        uploadPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the gallery to choose an image.
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, UPLOAD_PICTURE_REQUEST_CODE);
            }
        });

        //button to take picture from camera
        destination = new File(Environment.getExternalStorageDirectory(), "picture.jpg");
        takePictureButton = view.findViewById(R.id.cameraButton);
        takePictureButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.list_selector_background));
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dispatchTakePictureIntent();
                }
                catch (ActivityNotFoundException e) {
                    Log.e(TAG, "Camera not found inside device.");
                }
            }
        });
        return view;
    }

    //function to start taking photo from camera
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                Log.d(TAG, "trying...");
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d(TAG, "Error when creating file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                //Uri
                photoURI = FileProvider.getUriForFile(getActivity(),
                        "com.ufo.orbital.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.d(TAG, "before starting activity...");
                startActivityForResult(takePictureIntent, TAKE_PICTURE_REQUEST_CODE);
            }
        }
    }

    //save a temp pic from camera
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File myDir = new File(root + "/aaSuperRes" + "/asaved_temp");
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                myDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "createImageFile: " + currentPhotoPath);
        return image;
    }

    /* Feedback functions */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

    protected void sendFeedback() {
        Intent _Intent = new Intent(Intent.ACTION_SENDTO);
        //_Intent.setType("text/html");
        _Intent.setType("message/rfc822");
        _Intent.setData(Uri.parse("mailto:"));
        _Intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ getString(R.string.mail_feedback_email) });
        _Intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_feedback_subject));
        _Intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.mail_feedback_message));
        //startActivity(Intent.createChooser(_Intent, "Send Feedback"));
        startActivityForResult(Intent.createChooser(_Intent, "Send Feedback"), EMAIL_REQUEST_CODE);
    }

    public Bitmap loadImageFromFile(){
        BitmapFactory.decodeFile(currentPhotoPath);
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
        return bitmap;
    }

    /* Activity requests:
     * - Upload Pic
     * - Take Pic
     * - Email Feedback
      * */
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        Bitmap picture = null;
        if (reqCode == UPLOAD_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                // Case 'upload picture'.
                Uri imageUri = data.getData();
                InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                picture = BitmapFactory.decodeStream(imageStream);
                picture = handleSamplingAndRotationBitmap(getActivity().getApplicationContext(), imageUri);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "error when Uploading image");
            }
            if (picture != null) {
                Log.d(TAG, "Going to Crop(U)");
                goToCropActivity(picture);
            } else {
                Log.d(TAG, "Image upload cancelled!");
            }
        }

        else if (reqCode == TAKE_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                picture = loadImageFromFile();
                picture = handleSamplingAndRotationBitmap(getActivity().getApplicationContext(), photoURI);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "error when Capturing image");
            }
            if (picture != null) {
                Log.d(TAG, "Going to Crop(C)");
                goToCropActivity(picture);
            } else {
                Log.d(TAG, "Image Capture cancelled!");
            }
        }
        else if (reqCode == EMAIL_REQUEST_CODE) {
            Log.d(TAG, "requestCode: " + reqCode + " | resultCode: " + resultCode);
            Toast.makeText(getActivity().getApplicationContext(),
                    "Feedback Sent!",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            Log.d(TAG, "Requestcode Invalid: " + reqCode);
        }
    }

    /* Rotating Image Functions */
    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }

    // Calculate scaling factor
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                ++inSampleSize;
            }
        }
        return inSampleSize;
    }

    /* Rotation Functions
     * - Main
     */
    public static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    /* Rotation Functions
     * - Rotate the image
     */
    public static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    /* Intent to go to CropActivity Class */
    private void goToCropActivity(Bitmap picture) {
        // Save the image temporarily.
        File dir = new File(Environment.getExternalStorageDirectory() + "/aaSuperRes" + "/atransfer/");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        OutputStream outputStream = null;
        File file = new File(Environment.getExternalStorageDirectory() + "/aaSuperRes/atransfer/picture.png");
        try {
            outputStream = new FileOutputStream(file);
            picture.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(getActivity(), CropActivity.class);
        intent.putExtra(PICTURE_TO_CROP, file.getAbsolutePath());
        startActivity(intent);
    }

    /* Permission Functions
     * - Request
     */
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Permission needed")
                    .setMessage("This Permission is needed cos of this and that")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }
        else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    /* Permission Functions
     * - onRequest
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Permission GRANTED", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getActivity(), "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
