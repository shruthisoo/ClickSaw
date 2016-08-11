package com.clicksaw;

/**
 * Created by Abhi on 11-08-2016.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by Abhi on 03-08-2016.
 */
public class DraggableGridAdapter extends BaseAdapter {

    private Context context;
    private int imageWidth,imageHeight;
    private ArrayList<Constants.ImagesClass> itemList;
    public DraggableGridAdapter(Context context, ArrayList<Constants.ImagesClass> itemList ) {
        this.context = context;
        this.itemList = itemList;
    }
    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView image;
        TextView tv;
        Bitmap bmp = itemList.get(position).bitmap;

        if(convertView == null){
            image = new ImageView(context);

            imageWidth = bmp.getWidth()-20;
            imageHeight = bmp.getHeight();
        }else{
            image = (ImageView) convertView;
//            tv = (TextView)convertView;
        }

        image.setImageBitmap(bmp);
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        // tv.setText(bmp.toString());


        return image;

    }
    public void swapItems(int positionOne, int positionTwo) {
        Log.d("SwapItems Called","Yes");
        Constants.ImagesClass temp = (Constants.ImagesClass) getItem(positionOne);
        set(positionOne, (Constants.ImagesClass) getItem(positionTwo));
        set(positionTwo, temp);
    }
    public void set(int position, Constants.ImagesClass item) {
        itemList.set(position, item);
        notifyDataSetChanged();
    }
    public ArrayList<Constants.ImagesClass> getItemList(){
        return itemList;
    }


}
