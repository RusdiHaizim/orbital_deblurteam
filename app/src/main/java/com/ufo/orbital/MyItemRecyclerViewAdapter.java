package com.ufo.orbital;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.FileNotFoundException;
import java.util.List;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {
    private String TAG = "RecycleView";
    private final List<PictureItem> mValues;
    private final ItemFragment.OnListFragmentInteractionListener mListener;
    private Context context;
    private int reqSize = 150;
    TextView tv;

    public MyItemRecyclerViewAdapter(List<PictureItem> items, ItemFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        //holder.mImageView.setImageURI(mValues.get(position).uri);

        //Two copies of bitmap, one big(tmpO), one small(tmpD)
        Bitmap tmpD = null;
        Bitmap tmpO = null;

        WindowManager mWinMgr = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        final int displayWidth = mWinMgr.getDefaultDisplay().getWidth();

        try {
            tmpD = decodeUri(context, mValues.get(position).uri, reqSize);
            tmpO = decodeUri(context, mValues.get(position).uri, displayWidth);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Date of Item
        holder.mDateView.setText(mValues.get(position).date);

        //Image of Item
        if (holder.mItem.touched == false) {
            holder.mImageView.setImageBitmap(tmpD);
            holder.mDateView.setVisibility(View.VISIBLE);
            Log.d(TAG, "Width: " + reqSize);
        }
        else {
            holder.mImageView.setImageBitmap(tmpO);
            holder.mDateView.setVisibility(View.GONE);
            Log.d(TAG, "Width: " + displayWidth);
        }
        final Bitmap finalTmpO = tmpO;
        final Bitmap finalTmpD = tmpD;
        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.mItem.touched == false) {
                    holder.mImageView.setImageBitmap(finalTmpO);
                    holder.mItem.touched = true;
                    Toast.makeText(context, "Zooming", Toast.LENGTH_SHORT).show();
                    holder.mDateView.setVisibility(View.GONE);
                    Log.d(TAG, "Width: " + finalTmpO.getWidth());
                }
                else {
                    holder.mImageView.setImageBitmap(finalTmpD);
                    holder.mItem.touched = false;
                    holder.mDateView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Width: " + finalTmpD.getWidth());
                }
            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }
    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize)
    throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;

        if (requiredSize != 0) {
            while (true) {
                if (width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }
        }
        else {
            scale = 1;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImageView;
        public final TextView mDateView;
        public PictureItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = view.findViewById(R.id.item_image_view);
            mDateView = view.findViewById(R.id.item_date_tv);
        }
    }
}
