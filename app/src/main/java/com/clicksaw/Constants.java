package com.clicksaw;

import android.graphics.Bitmap;

/**
 * Created by Abhi on 30-07-2016.
 */
public class Constants {

   static int gvHeight,gvWidth;
    public static class ImagesClass{
        public int i;
        public  Bitmap bitmap;
    }

    public static void saveGridSize(int width,int height){
        gvWidth = width;
        gvHeight = height;
    }
    public static int[] getGridSize(){
        int [] gridSize = new int[2];
        gridSize[0] = gvHeight;
        gridSize[1] = gvWidth;

        return gridSize;

    }
}
