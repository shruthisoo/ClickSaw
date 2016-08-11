package com.clicksaw;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.plattysoft.leonids.ParticleSystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Created by Abhi on 11-08-2016.
 */
public class SelectedImage extends AppCompatActivity implements ViewTreeObserver.OnWindowFocusChangeListener  {
    private FrameLayout selectedImage;
    private SeekBar seekBar;
    private byte[] data;
    private Bitmap selectedImageBit;
    private boolean checkFront;
    private int reqWidth, reqHeight;

    private DraggableGridView gridView;

    private DraggableGridAdapter imageAdapter;
    private TextView tvTime;


    private ArrayList<Bitmap> bitmapsChunk = new ArrayList<>();
    ArrayList<Constants.ImagesClass> chunkedImages = new ArrayList<Constants.ImagesClass>();
    private ArrayList<Constants.ImagesClass> shuffledImages = new ArrayList<>();
    int min = 2;
    private LinearLayout settingsView;
    private ImageView imageHint, imageSeek;

    private boolean timerRunning = false;
    private String filePath;
    private MyCountDownTimer timer;
    private int splitValue = 0;
    private Bitmap imageToBeSplit;

    MediaPlayer mPlayer;
    private int gvHeight,gvWidth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.selected_image);
        // settings = (ImageView)findViewById(R.id.settings);
        gridView = (DraggableGridView) findViewById(R.id.gridview);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        selectedImage = (FrameLayout) findViewById(R.id.frameLayout);

        settingsView = (LinearLayout) findViewById(R.id.settingsView);
        imageHint = (ImageView) findViewById(R.id.imageHint);
        //imageTimer = (ImageView) findViewById(R.id.imageTimer);
        // progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imageSeek = (ImageView)findViewById(R.id.imageSeek);
        tvTime = (TextView)findViewById(R.id.tvTime);

        gridView.setLayout(settingsView);


        timer = new MyCountDownTimer(900000,1000);


        timer.start();
        //  seekBar.setMax((max - min) / step);
        filePath = getIntent().getStringExtra("filePath");
        checkFront = getIntent().getBooleanExtra("front", false);


        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        reqWidth = size.x;
        reqHeight = size.y;




        int seekbarpoints = (reqWidth/4);
        Bitmap bitmap = Bitmap.createBitmap(reqWidth, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);


        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.colorAccent));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);

        Paint textpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textpaint.setColor(Color.rgb(61, 61, 61));
        textpaint.setTextSize(28);
        int point = 0;

        for (int i = 2; i < seekbarpoints; i++  ){
            if (i==2) {
                canvas.drawText(Integer.toString(i), point, 95, textpaint);
            }else if (i>4){
                canvas.drawText(Integer.toString(i), point - 14, 95, textpaint);
            }else {
                canvas.drawText(Integer.toString(i), point - 8, 95, textpaint);
            }
            point = point  + seekbarpoints;
            canvas.drawLine(point, 30, point, 0, paint);


        }

        //Create a new Drawable
        Drawable d = new BitmapDrawable(getResources(),bitmap);
        seekBar.setProgressDrawable(d);

        //get saved gridsize
        int gridSize[] = Constants.getGridSize();
        gvHeight = gridSize[0];
        gvWidth = gridSize[1];

        Log.e("GvHeight",""+gvHeight);
        Log.e("GvWidth",""+gvWidth);

        mPlayer= MediaPlayer.create(SelectedImage.this, R.raw.done);

        imageSeek.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                int cx = (settingsView.getLeft() + settingsView.getRight());
                int cy = (settingsView.getTop());

                // to find  radius when icon is tapped for showing layout
                int startradius = 0;
                int endradius = Math.max(settingsView.getWidth(), settingsView.getHeight());


                // performing circular reveal when icon will be tapped
                Animator animator = ViewAnimationUtils.createCircularReveal(settingsView, cx, cy, startradius, endradius);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(400);



                //  starting radius will be the radius or the extent to which circular reveal animation is to be shown
                int reverse_startradius = Math.max(settingsView.getWidth(), settingsView.getHeight());
                //endradius will be zero
                int reverse_endradius = 0;


                // performing circular reveal for reverse animation
                Animator animate = ViewAnimationUtils.createCircularReveal(settingsView, cx, cy, reverse_startradius, reverse_endradius);


                // to show the layout when icon is tapped
                settingsView.setVisibility(View.VISIBLE);
                animator.start();



            }
        });

        new AsyncBitmap().execute();
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
    public class AsyncBitmap extends AsyncTask<Void, Void, Drawable> {

        CustomImageView customImageView;
        BitmapDrawable drawable;

        public AsyncBitmap() {
            customImageView = new CustomImageView(getApplicationContext());

        }

        @Override
        protected Drawable doInBackground(Void... params) {
            data = MainActivity.getPictureData();
            Bitmap photo = null;
            try {

                if (filePath == null) {
                    photo = BitmapFactory.decodeByteArray(data, 0, data.length);

                    // if (checkFront)
                    //  selectedImageBit = rotate(photo, 270);
                    // else
                    //selectedImageBit = rotate(photo, 90);
                    selectedImageBit = photo;
                    customImageView.imageBitmap = Bitmap.createScaledBitmap(selectedImageBit, gvWidth, gvHeight, true);
                    drawable = new BitmapDrawable(null, customImageView.imageBitmap);
                } else {
                    Log.d("Orientation:", "" + getOrientation(filePath));
                    photo = BitmapFactory.decodeFile(filePath);
                    if (getOrientation(filePath) == 90) {
                        selectedImageBit = rotate(photo, 90);
                    }
                    else {
                        selectedImageBit = photo;
                    }


                    customImageView.imageBitmap = Bitmap.createScaledBitmap(selectedImageBit, gvWidth, gvHeight, true);
                    drawable = new BitmapDrawable(null, customImageView.imageBitmap);
                }


            } catch (Exception e) {
                Log.e("SelectedImage", "saveJPEGBitmapToMediaStore: failed to save image", e);
            }
            return drawable;
        }

        @Override
        protected void onPostExecute(final Drawable result) {
            selectedImage.setBackground(result);
            imageHint.setImageDrawable(result);
            splitValue = min;
            imageToBeSplit = customImageView.imageBitmap;
            seekBar.setMax(4);


            new AsyncSplitImage(2, customImageView.imageBitmap).execute();


            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    int value = 2 + progress;
                    imageToBeSplit = customImageView.imageBitmap;
                    new AsyncSplitImage(value, customImageView.imageBitmap).execute();

                    timer.start();

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            imageHint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(SelectedImage.this);
                    LayoutInflater inflater = LayoutInflater.from(SelectedImage.this);
                    View builderView = inflater.inflate(R.layout.hint_view_layout, null);
                    builder.setView(builderView);
                    ImageView hintImage = (ImageView) builderView.findViewById(R.id.hintImage);
                    ImageView closeHint = (ImageView) builderView.findViewById(R.id.closeHint);

                    hintImage.setImageDrawable(result);
                    builder.create();
                    final AlertDialog ad = builder.show();


                    closeHint.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ad.dismiss();
                        }
                    });


                }
            });


        }
    }

    public static Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();

            m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(
                        b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                throw ex;
            }
        }
        return b;
    }

    public static int getOrientation(String filePath) {
        int rotate = 0;
        try {
            File file = new File(filePath);
            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }


    public class AsyncSplitImage extends AsyncTask<Void, Void, ArrayList<Constants.ImagesClass>> {

        int chunkNumbers;
        Bitmap bitmapImage;

        public AsyncSplitImage(int chunkNumbers, Bitmap bitmapImage) {
            this.chunkNumbers = chunkNumbers;
            this.bitmapImage = bitmapImage;

            bitmapsChunk.clear();
            chunkedImages.clear();
            shuffledImages.clear();


        }

        @Override
        protected void onPreExecute() {
            //  selectedImage.setBackground(drawable);
        }

        @Override
        protected ArrayList<Constants.ImagesClass> doInBackground(Void... params) {

            //For the number of rows and columns of the grid to be displayed
            int rows, cols;

            //For height and width of the small image chunks
            int chunkHeight, chunkWidth;

            //To store all the small image chunks in bitmap format in this list

            rows = cols = chunkNumbers;

            chunkHeight = gvHeight/ rows;
            chunkWidth = gvWidth / cols;
            int yCoord = 0;
            for (int x = 0; x < rows; x++) {
                int xCoord = 0;

                for (int y = 0; y < cols; y++) {

                    bitmapsChunk.add(Bitmap.createBitmap(bitmapImage, xCoord, yCoord, chunkWidth, chunkHeight));
                    xCoord += chunkWidth;
                }

                yCoord += chunkHeight;
            }




            for (int j = 0; j < bitmapsChunk.size(); j++) {
                Constants.ImagesClass imagesClass = new Constants.ImagesClass();
                imagesClass.i = j;
                imagesClass.bitmap = (bitmapsChunk.get(j));
                chunkedImages.add(imagesClass);

            }
            try {
                for (int i = 0; i < chunkedImages.size(); i++) {
                    shuffledImages.add(chunkedImages.get(i));
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                ;
            }


            return shuffledImages;
        }

        @Override
        protected void onPostExecute(final ArrayList<Constants.ImagesClass> result) {

            selectedImage.setBackground(null);
            Collections.shuffle(result);

            imageAdapter = new DraggableGridAdapter(SelectedImage.this, result);
            gridView.setAdapter(imageAdapter);
            gridView.setNumColumns((int) Math.sqrt(result.size()));



            gridView.setListener(new DraggableGridView.Listener() {
                @Override
                public void onItemChanged(ArrayList<Constants.ImagesClass> result) {
                    imageAdapter.notifyDataSetChanged();
                    if (compare(chunkedImages, result)) {

                        if (mPlayer!=null) {
                            mPlayer.setVolume(0.3f, 0.3f);
                            mPlayer.start();
                        }
                        gridView.stopMediaPlayer();

                        ParticleSystem ps = new ParticleSystem(SelectedImage.this, 100, R.drawable.red_spark, 2000);
                        ps.setScaleRange(0.7f, 1.3f);
                        ps.setSpeedRange(0.1f, 0.15f);
                        ps.setRotationSpeedRange(90, 180);
                        ps.setFadeOut(800, new AccelerateInterpolator());
                        ps.oneShot(gridView, 100);

                        ParticleSystem ps2 = new ParticleSystem(SelectedImage.this, 100, R.drawable.yellow_spark, 2000);
                        ps2.setScaleRange(0.2f, 1.3f);
                        ps2.setSpeedRange(0.1f, 0.25f);
                        ps.setRotationSpeedRange(180, 90);
                        ps2.setFadeOut(500, new AccelerateInterpolator());
                        ps2.oneShot(gridView, 100);

                        ParticleSystem ps3 = new ParticleSystem(SelectedImage.this, 100, R.drawable.blue_spark, 2000);
                        ps3.setScaleRange(0.2f, 1.3f);
                        ps3.setSpeedRange(0.1f, 0.25f);
                        ps.setRotationSpeedRange(180, 90);
                        ps3.setFadeOut(500, new AccelerateInterpolator());
                        ps3.oneShot(gridView, 100);

                        AlertDialog.Builder builder = new AlertDialog.Builder(SelectedImage.this);
                        builder.setTitle("Yippe!!");
                        builder.setMessage(""+tvTime.getText());
                        // builder.create();
                        builder.setCancelable(false);

                        builder.setPositiveButton("Start new puzzle", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (mPlayer!=null){
                                    mPlayer.stop();
                                }
                                Intent intent = new Intent(SelectedImage.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                        //Toast.makeText(getApplicationContext(), "DONE", Toast.LENGTH_SHORT).show();

                        timer.cancel();

                    }
                }
            });





        }
    }

    private static boolean compare(ArrayList<Constants.ImagesClass> originalBitmaps, ArrayList<Constants.ImagesClass> result) {

        int matchedRows = 0;
        for (int i = 0; i < originalBitmaps.size(); i++) {
            if (originalBitmaps.get(i).i == result.get(i).i) {
                //    Log.d("Bitmap Matched", "or:" + originalBitmaps.get(i).bitmap.toString() + ":res:" + result.get(i).bitmap.toString());
                matchedRows++;
            }
            //Log.e("Bitmap UnMatched", "or:" + originalBitmaps.get(i).bitmap.toString() + ":res:" + result.get(i).bitmap.toString());

        }

        //Log.d("MatchedRows COunt", "" + matchedRows);
        //Log.d("Original Count", "Or" + originalBitmaps.size());
        if (matchedRows == originalBitmaps.size()) {
            return true;
        } else
            return false;
    }

    class MyCountDownTimer extends CountDownTimer {


        long millisInFuture;
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            this.millisInFuture = millisInFuture;


        }

        @Override
        public void onTick(long millisUntilFinished) {

            long millisFinished = millisUntilFinished;
            long millisLeft = millisInFuture - millisUntilFinished;


            tvTime.setText(""+String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millisLeft) - TimeUnit.HOURS.toMinutes(
                            TimeUnit.MILLISECONDS.toHours(millisLeft)),

                    TimeUnit.MILLISECONDS.toSeconds(millisLeft) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(millisLeft))));

            /*if (millis==60){
                millis = 0;
                sec++;
                String text = String.format("%02d:%02d",mins,sec);
                Log.d("text",text);
                tvTime.setText(text);
            }

           else if (sec==60){
                sec = 0;
                mins+=1;
                String text = String.format("%02d:%02d",mins,sec);
                Log.d("text",text);
                tvTime.setText(text);
            }
            else {
                String text = String.format("%02d:%02d",mins,sec);
                tvTime.setText(text);
            }*/
        }

        @Override
        public void onFinish() {


            // progressBar.setVisibility(View.GONE);

            AlertDialog.Builder builder = new AlertDialog.Builder(SelectedImage.this);
            builder.setTitle("Time Up!!");
            builder.setMessage("Do you want to restart the game??");
            // builder.create();
            builder.setCancelable(false);

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new AsyncSplitImage(splitValue, imageToBeSplit).execute();

                    timer.start();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    Intent intent = new Intent(SelectedImage.this,MainActivity.class);
                    startActivity(intent);
                    finish();

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

}
