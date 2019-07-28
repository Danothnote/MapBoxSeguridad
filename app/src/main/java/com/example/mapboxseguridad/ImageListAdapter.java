package com.example.mapboxseguridad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ImageListAdapter extends BaseAdapter {

    private Context context;
    private int layaout;
    private ArrayList<Image> imageList;

    public ImageListAdapter() {
    }

    public ImageListAdapter(Context context, int layaout, ArrayList<Image> imageList) {
        this.context = context;
        this.layaout = layaout;
        this.imageList = imageList;
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object getItem(int positon) {
        return imageList.get(positon);
    }

    @Override
    public long getItemId(int positon) {

        return positon;
    }

    private class ViewHolder{
        ImageView imageView;
        TextView txtName;
    }

    @Override
    public View getView(int positon, View view, ViewGroup viewGroup) {
        View row = view;
        ViewHolder holder = new ViewHolder();

        if (row == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layaout, null);

            holder.txtName = (TextView) row.findViewById(R.id.txtImage);
            holder.imageView = (ImageView) row.findViewById(R.id.imageSelect);

            row.setTag(holder);
        }
        else
            holder = (ViewHolder) row.getTag();
        Image image = imageList.get(positon);

        holder.txtName.setText(image.getName());

        byte[] imageBytes = image.getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes,0 ,imageBytes.length);
        holder.imageView.setImageBitmap(bitmap);

        return row;
    }
}
