package com.clicksaw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;

/**
 * Created by Abhi on 28-07-2016.
 */
public class CustomImageView extends View {

    public Bitmap imageBitmap;

    public CustomImageView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        if(imageBitmap != null) {
            Log.d("ImageBitmap","Not Null");
            canvas.drawBitmap(imageBitmap, 0, 0, null);
        }
        else
        {
            Log.d("ImageBitmap","Null");
        }
    }
}