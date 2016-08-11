package com.clicksaw;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clicksaw.Constants;

import java.util.ArrayList;

/**
 * Created by Abhi on 03-08-2016.
 */

public class DraggableGridView extends GridView implements View.OnTouchListener{

    float dX,dY;
    int positionFirst,positionNext;
    private Listener mListener;
    private View view1,view2;

    private MediaPlayer mPlayer;
    private LinearLayout layout;

    private Context context;
    private int[] gridSize;

    public DraggableGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setOnTouchListener(this);
        gridSize = Constants.getGridSize();

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mPlayer= MediaPlayer.create(context, R.raw.push_a);

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (layout!=null)
                    layout.setVisibility(INVISIBLE);

                float currentXPosition = event.getX();
                float currentYPosition = event.getY();

                positionFirst = pointToPosition((int) currentXPosition, (int) currentYPosition);
                view1 = getChildAt(positionFirst);
                final ClipData data = ClipData.newPlainText("position",positionFirst + "");
                DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view1);
                v.startDrag(data,shadowBuilder, view1, 0);
                if (view1!=null)
                    view1.setVisibility(INVISIBLE);



                //final ImageView imageView = (ImageView)view1;

                v.setOnDragListener(new OnDragListener() {
                    @Override
                    public boolean onDrag(View v, DragEvent dragEvent) {
                        final int action = dragEvent.getAction();
                        switch(action) {

                            case DragEvent.ACTION_DRAG_STARTED:

                                Log.d("Drag Stared", "Yes");
                                return true;
                            case DragEvent.ACTION_DRAG_ENTERED:
                                Log.d("Drag Entered", "Yes");
                                return true;
                            case DragEvent.ACTION_DRAG_LOCATION:
                                Log.d("Drag Location", "Yes");

                                return true;
                            case DragEvent.ACTION_DROP:

                                float draggedXPosition = dragEvent.getX();
                                float draggedYPosition = dragEvent.getY();

                                // if (dragEvent.getResult()){

                                positionNext = pointToPosition((int) draggedXPosition, (int) draggedYPosition);
                               if (mPlayer!=null)
                                mPlayer.start();
                                //   positionNext = getPositionForView(view1);
                                Log.d("Position Next", "" + positionNext);
                                if (positionNext != -1) {
                                    ((DraggableGridAdapter) getAdapter()).swapItems(positionFirst, positionNext);
                                    mListener.onItemChanged(((DraggableGridAdapter) getAdapter()).getItemList());
                                }

                                Log.e("yes View1 is not null","Yes");
                                try {
                                    view1.setVisibility(VISIBLE);
                                }
                                catch(NullPointerException e){
                                    e.printStackTrace();
                                }
                                Log.d("Drag DROP","Yes");
                                return true;
                            case DragEvent.ACTION_DRAG_EXITED:
                                Log.d("Drag Exited","Yes"+dragEvent.getResult());
                                try {
                                    view1.setVisibility(VISIBLE);
                                }
                                catch(NullPointerException e){
                                    e.printStackTrace();
                                }
                                return true;
                            case DragEvent.ACTION_DRAG_ENDED:
                                if (!dragEvent.getResult()){
                                    Log.d("Drag Exited","Yes"+dragEvent.getResult());
                                    try {
                                        view1.setVisibility(VISIBLE);
                                    }
                                    catch(NullPointerException e){
                                        e.printStackTrace();
                                    }
                                }



                                Log.d("Drag End","yes");
                                return true;
                            default:
                                Log.e("DragDrop Example","Unknown action type received by OnDragListener.");
                                break;
                        }
                        return true;
                    }
                });

                break;
            case MotionEvent.ACTION_UP:

                try {
                    view1.setVisibility(VISIBLE);
                }
                catch(NullPointerException e){
                    e.printStackTrace();
                }
                break;

            default:
                return true;




        }
        return true;
    }

    public void setListener(Listener l){
        mListener = l;
    }

    public interface Listener{
        void onItemChanged(ArrayList<Constants.ImagesClass> result);
    }
    //to hide settings layout on touch
    public void setLayout(LinearLayout layout){
        this.layout = layout;
    }
    public void stopMediaPlayer(){
        if (mPlayer!=null){
            mPlayer.stop();
            mPlayer.release();
        }}
    public void setMediaPlayer(){
        mPlayer= MediaPlayer.create(context, R.raw.push_a);
    }
}
