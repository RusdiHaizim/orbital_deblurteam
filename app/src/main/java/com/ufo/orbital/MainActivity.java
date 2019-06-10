package com.ufo.orbital;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String PICTURE_TO_CROP = "PICTURE_TO_CROP";
    private int UPLOAD_PICTURE_REQUEST_CODE = 1;
    private int STORAGE_PERMISSION_CODE = 2;
    private int TAKE_PICTURE_REQUEST_CODE = 3;

    private ImageButton takePictureButton;
    private ImageButton uploadPictureButton;
    private ImageButton btnF;
    private int EMAIL_REQUEST_CODE = 100;

    String currentPhotoPath;
    File destination;
    Uri photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(getApplicationContext(),
//                    "Permission already granted",
//                    Toast.LENGTH_SHORT).show();
        }
        else {
            Log.d(TAG, "Permission not granted, going to request permission");
            requestStoragePermission();
        }

        //button for feedback
        btnF = findViewById(R.id.btn_feedback);
        btnF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedback();
            }
        });

        //button to take picture from gallery
        uploadPictureButton = findViewById(R.id.uploadButton);
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
        takePictureButton = findViewById(R.id.cameraButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                    Intent cameraIntent = new Intent();
//                    cameraIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
//                    startActivityForResult(cameraIntent, TAKE_PICTURE_REQUEST_CODE);
                    dispatchTakePictureIntent();
                }
                catch (ActivityNotFoundException e) {
                    Log.e(TAG, "Camera not found inside device.");
                }
            }
        });

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
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
                photoURI = FileProvider.getUriForFile(this,
                        "com.ufo.orbital.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.d(TAG, "before starting activity...");
                startActivityForResult(takePictureIntent, TAKE_PICTURE_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /* Feedback functions */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

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

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        Bitmap picture = null;
        if (reqCode == UPLOAD_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                // Case 'upload picture'.
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                picture = BitmapFactory.decodeStream(imageStream);
                picture = handleSamplingAndRotationBitmap(getApplicationContext(), imageUri);
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
                picture = handleSamplingAndRotationBitmap(getApplicationContext(), photoURI);
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
            Toast.makeText(getApplicationContext(),
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

    private static int calculateInSampleSize(BitmapFactory.Options options,
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
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

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

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private void goToCropActivity(Bitmap picture) {
        // Save the image temporarily.
        File dir = new File(Environment.getExternalStorageDirectory() + "/aaSuperRes" + "/atransfer/");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        OutputStream outputStream = null;
        File file = new File(Environment.getExternalStorageDirectory() + "/aaSuperRes/atransfer/picture.jpg");
        try {
            outputStream = new FileOutputStream(file);
            picture.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, CropActivity.class);
        intent.putExtra(PICTURE_TO_CROP, file.getAbsolutePath());
        startActivity(intent);
    }

    /* Permission Functions */
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This Permission is needed cos of this and that")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
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
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }




}

//public class MainActivity extends AppCompatActivity {
//
//    private static final String TAG = "AICamera";
//    private static final int REQUEST_CAMERA_PERMISSION = 200;
//
//    private TextureView textureView;
//    private String cameraId;
//    protected CameraDevice cameraDevice;
//    protected CameraCaptureSession cameraCaptureSessions;
//    protected CaptureRequest.Builder captureRequestBuilder;
//    private Size imageDimension;
//    private Handler mBackgroundHandler;
//    private HandlerThread mBackgroundThread;
//    private TextView tv;
//    private String predictedClass = "none";
//    private AssetManager mgr;
//    private boolean processing = false;
//    private Image image = null;
//    private int mSensorOrientation;
//
//    static {
//        System.loadLibrary("native-lib");
//    }
//
//
//    public native String predFromCaffe2(byte[] Y, byte[] U, byte[] V, int width, int height, int y_row_stride,
//                                        int uv_row_stride, int uv_pixel_stride, int scale_width, int scale_height, int degree);
//
//    public native void initCaffe2(AssetManager mgr);
//
//    private class SetUpNeuralNetwork extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void[] v) {
//            try {
//                initCaffe2(mgr);
//                predictedClass = "Neural net loaded! Inferring...";
//            } catch (Exception e) {
//                Log.d(TAG, "Couldn't load neural network.");
//            }
//            return null;
//        }
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//
//        mgr = getResources().getAssets();
//
//        new SetUpNeuralNetwork().execute();
//
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
//
//        setContentView(R.layout.activity_main);
//
//        textureView = (TextureView) findViewById(R.id.textureView);
//        textureView.setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE);
//        final GestureDetector gestureDetector = new GestureDetector(this.getApplicationContext(),
//                new GestureDetector.SimpleOnGestureListener() {
//                    @Override
//                    public boolean onDoubleTap(MotionEvent e) {
//                        return true;
//                    }
//
//                    @Override
//                    public void onLongPress(MotionEvent e) {
//                        super.onLongPress(e);
//
//                    }
//
//                    @Override
//                    public boolean onDoubleTapEvent(MotionEvent e) {
//                        return true;
//                    }
//
//                    @Override
//                    public boolean onDown(MotionEvent e) {
//                        return true;
//                    }
//                });
//
//        textureView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return gestureDetector.onTouchEvent(event);
//            }
//        });
//
//        assert textureView != null;
//        textureView.setSurfaceTextureListener(textureListener);
//        tv = (TextView) findViewById(R.id.sample_text);
//
//    }
//
//    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
//        @Override
//        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//            //open your camera here
//            openCamera();
//        }
//
//        @Override
//        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//            // Transform you image captured size according to the surface width and height
//        }
//
//        @Override
//        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//            return false;
//        }
//
//        @Override
//        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//        }
//    };
//    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
//        @Override
//        public void onOpened(CameraDevice camera) {
//            cameraDevice = camera;
//            createCameraPreview();
//        }
//
//        @Override
//        public void onDisconnected(CameraDevice camera) {
//            cameraDevice.close();
//        }
//
//        @Override
//        public void onError(CameraDevice camera, int error) {
//            cameraDevice.close();
//            cameraDevice = null;
//        }
//    };
//
//    protected void startBackgroundThread() {
//        mBackgroundThread = new HandlerThread("Camera Background");
//        mBackgroundThread.start();
//        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
//    }
//
//    protected void stopBackgroundThread() {
//        mBackgroundThread.quitSafely();
//        try {
//            mBackgroundThread.join();
//            mBackgroundThread = null;
//            mBackgroundHandler = null;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    protected void createCameraPreview() {
//        try {
//            SurfaceTexture texture = textureView.getSurfaceTexture();
//            assert texture != null;
//            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
//            Surface surface = new Surface(texture);
//            final int width = 227;
//            final int height = 227;
//            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 4);
//            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
//                @Override
//                public void onImageAvailable(ImageReader reader) {
//                    try {
//
//                        image = reader.acquireNextImage();
//                        if (processing) {
//                            image.close();
//                            return;
//                        }
//                        processing = true;
//                        int w = image.getWidth();
//                        int h = image.getHeight();
//                        ByteBuffer Ybuffer = image.getPlanes()[0].getBuffer();
//                        ByteBuffer Ubuffer = image.getPlanes()[1].getBuffer();
//                        ByteBuffer Vbuffer = image.getPlanes()[2].getBuffer();
//                        // TODO: use these for proper image processing on different formats.
//
//                        final int yRowStride = image.getPlanes()[0].getRowStride();
//                        final int uvRowStride = image.getPlanes()[1].getRowStride();
//
//                        final int uvPixelStride = image.getPlanes()[1].getPixelStride();
//
//
//                        byte[] Y = new byte[Ybuffer.capacity()];
//                        byte[] U = new byte[Ubuffer.capacity()];
//                        byte[] V = new byte[Vbuffer.capacity()];
//                        Ybuffer.get(Y);
//                        Ubuffer.get(U);
//                        Vbuffer.get(V);
//
//                        predictedClass = predFromCaffe2(Y, U, V, w, h, yRowStride, uvRowStride,
//                                uvPixelStride, width, height, mSensorOrientation);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                tv.setText(predictedClass);
//                                processing = false;
//                            }
//                        });
//
//                    } finally {
//                        if (image != null) {
//                            image.close();
//                        }
//                    }
//                }
//            };
//            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
//            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            captureRequestBuilder.addTarget(surface);
//            captureRequestBuilder.addTarget(reader.getSurface());
//
//            cameraDevice.createCaptureSession(Arrays.asList(surface, reader.getSurface()), new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    if (null == cameraDevice) {
//                        return;
//                    }
//                    cameraCaptureSessions = cameraCaptureSession;
//                    updatePreview();
//                }
//
//                @Override
//                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    Toast.makeText(MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
//                }
//            }, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void openCamera() {
//        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            cameraId = manager.getCameraIdList()[0];
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
//            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            assert map != null;
//            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
//            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
//                return;
//            }
//            manager.openCamera(cameraId, stateCallback, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    protected void updatePreview() {
//        if (null == cameraDevice) {
//            return;
//        }
//        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//        try {
//            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void closeCamera() {
//        if (null != cameraDevice) {
//            cameraDevice.close();
//            cameraDevice = null;
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                Toast.makeText(MainActivity.this, "You can't use this app without granting permission", Toast.LENGTH_LONG).show();
//                finish();
//            }
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        startBackgroundThread();
//        if (textureView.isAvailable()) {
//            openCamera();
//        } else {
//            textureView.setSurfaceTextureListener(textureListener);
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        closeCamera();
//        stopBackgroundThread();
//        super.onPause();
//    }
//
//
//}
