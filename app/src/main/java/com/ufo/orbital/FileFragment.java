package com.ufo.orbital;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;

import static com.ufo.orbital.PictureContent.loadSavedImages;

public class FileFragment extends Fragment implements ItemFragment.OnListFragmentInteractionListener {
    private String TAG = "ScrollingActivity";
    private int STORAGE_PERMISSION_CODE = 2;
    private RecyclerView.Adapter recyclerViewAdapter;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "here");
        View view =  inflater.inflate(R.layout.scrolling_activity, container, false);
        Log.d(TAG, "here");

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(getApplicationContext(),
//                    "Permission already granted",
//                    Toast.LENGTH_SHORT).show();
        }
        else {
            Log.d(TAG, "Permission not granted, going to request permission");
            requestStoragePermission();
        }

        if (recyclerViewAdapter == null) {
            //Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.main_fragment);
            recyclerView = view.findViewById(R.id.main_fragment);
            recyclerViewAdapter = recyclerView.getAdapter();
        }
        Log.d(TAG, "here");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                File myDir = new File(root + "/ListPics");
                if (!myDir.exists()) {
                    myDir.mkdirs();
                }
                try {
                    loadSavedImages(myDir);
                    //loadSavedImages(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS));
                    //Log.d(TAG, "" + context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS));
                    recyclerViewAdapter.notifyDataSetChanged();
                }
                catch (Exception e) {
                    Log.d(TAG, "failed");
                }
            }
        });
    }

    @Override
    public void onListFragmentInteraction(PictureItem item) {

    }

    /* Permission Functions */
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