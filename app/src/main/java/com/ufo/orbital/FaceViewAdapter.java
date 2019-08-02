package com.ufo.orbital;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.FaceDetector;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.ufo.orbital.FaceResult.setMainImage;

public class FaceViewAdapter extends RecyclerView.Adapter<FaceViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";
    private final List<FaceItem> mValues;
    private final ItemFragment2.OnListFragmentInteractionListener mListener;

    //vars
    private ArrayList<String> mNames = new ArrayList<>();
//    private ArrayList<String> mImageUrls = new ArrayList<>();
//    private ArrayList<String> mImageUrls2 = new ArrayList<>();
    private List<FaceItem> mImageUrls;
    private List<FaceItem> mImageUrls2;

    private Context mContext;
    int selectedPosition = -1;
    ViewHolder currHolder = null;
    public static boolean justLaunched = false;

//    public FaceViewAdapter(Context context, ArrayList<String> names, ArrayList<String> imageUrls, ArrayList<String> imageUrls2) {
//        mNames = names;
//        mImageUrls = imageUrls;
//        mImageUrls2 = imageUrls2;
//        mContext = context;
//    }

    public FaceViewAdapter(List<FaceItem> items, List<FaceItem> items2, ItemFragment2.OnListFragmentInteractionListener listener) {
        mValues = items;
        mImageUrls = items;
        mImageUrls2 = items2;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: created.");
        mContext = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.face_list_item, parent, false);
        String bitmapStr = null;
        String bitmap2Str = null;
//        if (!mImageUrls.isEmpty() && selectedPosition != -1) {
//            bitmapStr = mImageUrls.get(selectedPosition).facePath;
//        }
//        if (!mImageUrls2.isEmpty() && selectedPosition != -1) {
//            bitmap2Str = mImageUrls2.get(selectedPosition).facePath;
//        }
//        try {
//            Log.d(TAG, "bit1:" + bitmapStr);
//            Log.d(TAG, "bit2:" + bitmap2Str);
////            mListener.onListFragmentInteraction(bitmapStr, bitmap2Str);
//            if (bitmapStr != null) {
//                setMainImage(bitmapStr, bitmap2Str);
//            }
//        }
//        catch (Exception e) {
//            Log.e(TAG, e.toString());
//        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (selectedPosition == position) {
            holder.bg.setBackgroundColor(Color.parseColor("#FFFFFF"));
            currHolder = holder;
        }
        else {
            holder.bg.setBackgroundColor(Color.parseColor("#000000"));
        }

        Log.d(TAG, "onBindViewHolder: called.");
        Log.d(TAG, "selectedPosition:" + selectedPosition);
        Bitmap bitmap1 = null;
        Bitmap bitmap2 = null;

        //getting pair of bitmaps
        try {
            bitmap1 = BitmapFactory.decodeFile(mImageUrls.get(position).facePath);
        }
        catch (Exception e) {
            Log.e(TAG, "" + e);
//                bitmap1 = BitmapFactory.decodeFile(mImageUrls.get(0).facePath);
        }
        try {
            bitmap2 = BitmapFactory.decodeFile(mImageUrls2.get(position).facePath);
        }
        catch (Exception e) {
            Log.e(TAG, "" + e);
//                bitmap2 = BitmapFactory.decodeFile(mImageUrls2.get(0).facePath);
        }

        if (justLaunched) {
            justLaunched = false;
            String a = "", b = "";
            try {
                a = mImageUrls.get(position).facePath;
            }
            catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            try {
                b = mImageUrls2.get(position).facePath;
            }
            catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            setMainImage(a, b);
            Log.d("JUSTLAUNCHED", "setting to false");
        }

        String text = "";
        try {
            text = "Face " + mImageUrls.get(position).faceNum;
        }
        catch (Exception e) {
            Log.e(TAG, e.toString());
        }



        holder.image.setImageBitmap(bitmap1);

        if (selectedPosition > -2) {



            Log.d(TAG, "Post:" + position + "/" + mImageUrls.size());
            Log.d(TAG, "Post2:" + position + "/" + mImageUrls2.size());
            final Bitmap finalBitmap = bitmap1;
            final Bitmap finalBitmap2 = bitmap2;

            Log.d(TAG, "size1:" + mImageUrls.size());
            Log.d(TAG, "size2:" + mImageUrls2.size());
            final String finalText = text;
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedPosition = position;

                    Log.d(TAG, "onClick: clicked on an image: " + mImageUrls.get(position).faceNum);
                    Toast.makeText(mContext, finalText, Toast.LENGTH_SHORT).show();
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        String a = "";
                        String b = "";
                        try {
                            a = mImageUrls.get(position).facePath;
                        }
                        catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                        try {
                            b = mImageUrls2.get(position).facePath;
                        }
                        catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                        setMainImage(a, b);
                        notifyDataSetChanged();
//                    mListener.onListFragmentInteraction(a, b);
//                    try {
//                        setMainImage(mImageUrls.get(position).facePath, mImageUrls2.get(position).facePath);
//                    }
//                    catch (Exception e) {
//                        Log.e(TAG, e.toString());
//                    }
                    }

//                    if (selectedPosition != -1) {
//                        notifyItemChanged(selectedPosition);
//                        selectedPosition = position;
//                        notifyItemChanged(selectedPosition);
//                    }
//                    else {
//                        selectedPosition = position;
//                    }

                }
            });

            holder.image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PopupMenu popup = new PopupMenu(mContext, holder.image);

                    popup.inflate(R.menu.options_face_item_menu);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                //edit the photo
                                case R.id.facemenu1:
                                    saveItem(mImageUrls2.get(position).facePath, holder.getAdapterPosition(), finalBitmap2);
                                    currHolder.bg.setBackgroundColor(Color.parseColor("#000000"));
                                    break;
                                //delete the photo
                                case R.id.facemenu2:
                                    deleteItem(holder.getAdapterPosition());
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                    return false;
                }
            });

            holder.name.setText(text);
        }
        else {
            selectedPosition = position;
        }
    }
    private void saveItem(String pathName, int newPosition, Bitmap bitmap) {
        File new_place = new File(Environment.getExternalStorageDirectory() + "/aaSuperRes/Downloaded_Files");
        long unixTime = System.currentTimeMillis() / 1000L;

        String targetName = "/face-" + unixTime;
        targetName = new_place + targetName + ".png";
        File targetFile = new File(targetName);
        Log.d(TAG, "target file name:" + targetFile.getAbsolutePath());

        try {
            OutputStream outputStream = new FileOutputStream(targetFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            Log.d("Saving", "success:" + targetName);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Saving", "error");
        }
        deleting(newPosition);
        mImageUrls.remove(newPosition);
        mImageUrls2.remove(newPosition);
        notifyItemRemoved(newPosition);
        notifyItemRangeChanged(newPosition, mImageUrls.size());
    }

    private void deleteItem(final int newPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Delete");
        builder.setMessage("Delete the item?");
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                currHolder.bg.setBackgroundColor(Color.parseColor("#000000"));
                deleting(newPosition);

                mImageUrls.remove(newPosition);
                mImageUrls2.remove(newPosition);
                notifyItemRemoved(newPosition);
                notifyItemRangeChanged(newPosition, mImageUrls.size());
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void deleting(int newPosition) {
        // Deletes the picture
        //int newPosition = holder.getAdapterPosition();
        setMainImage(null, null);
        File fileToDelete = new File(mImageUrls.get(newPosition).facePath);
        File fileToDelete2 = new File(mImageUrls2.get(newPosition).facePath);
        try {
            if (fileToDelete.exists()) {
                fileToDelete.delete();
                Log.d(TAG, "File: " + fileToDelete.getAbsolutePath() + " is successfully DELETED");
            }
            if (fileToDelete2.exists()) {
                fileToDelete2.delete();
                Log.d(TAG, "File: " + fileToDelete2.getAbsolutePath() + " is successfully DELETED");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "File doesn't exists");
        }
//        mImageUrls.remove(newPosition);
//        mImageUrls2.remove(newPosition);
//        notifyItemRemoved(newPosition);
//        notifyItemRangeChanged(newPosition, mImageUrls.size());

//        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mImageUrls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView name;
        RelativeLayout bg;
        FaceItem mItem;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_view);
            name = itemView.findViewById(R.id.name);
            bg = itemView.findViewById(R.id.faceBG);
        }
    }
}
