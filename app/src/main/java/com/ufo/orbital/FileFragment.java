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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.ufo.orbital.PictureContent.loadSavedImages;

public class FileFragment extends Fragment implements ItemFragment.OnListFragmentInteractionListener {
    private String TAG = "ScrollingActivity";
    private int STORAGE_PERMISSION_CODE = 2;
    private RecyclerView.Adapter recyclerViewAdapter;
    private RecyclerView recyclerView;
    public static Map< String, String> monthlist = new HashMap<String, String>();

    private void fillmap() {
        monthlist.put("Jan", "01");
        monthlist.put("Feb", "02");
        monthlist.put("Mar", "03");
        monthlist.put("Apr", "04");
        monthlist.put("May", "05");
        monthlist.put("Jun", "06");
        monthlist.put("Jul", "07");
        monthlist.put("Aug", "08");
        monthlist.put("Sep", "09");
        monthlist.put("Oct", "10");
        monthlist.put("Nov", "11");
        monthlist.put("Dec", "12");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.scrolling_activity, container, false);

        fillmap();

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission granted... in directory");
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
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                //File myDir = new File(root + "/ListPics");
                File myDir = new File(root + "/aaSuperRes/Downloaded_Files");
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
                    e.printStackTrace();
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