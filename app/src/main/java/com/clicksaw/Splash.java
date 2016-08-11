package com.clicksaw;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.clicksaw.DraggableGridView;
import com.crittercism.app.Crittercism;

/**
 * Created by Abhi on 26-07-2016.
 */
public class Splash extends AppCompatActivity {


    DraggableGridView draggableGridView;
    int gvHeight,gvWidth;
    private ImageView icon;
    int SPLASH_TIME_OUT = 5000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        icon = (ImageView)findViewById(R.id.icon);
        draggableGridView = (DraggableGridView)findViewById(R.id.gridview);

        Crittercism.initialize(getApplicationContext(), "dd9fcd33873d4805b2620dfbc6cf3f0e00444503");

        icon.setAlpha(1.0F);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.translate_top_to_center);
        icon.startAnimation(anim);

        final ViewTreeObserver vto = draggableGridView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                gvHeight = draggableGridView.getMeasuredHeight();
                gvWidth = draggableGridView.getMeasuredWidth();
                if (gvHeight>0 && gvWidth>0) {


                    Constants.saveGridSize(gvWidth,gvHeight);
                }
                else{

                }}
        });

        if (!isFirstTime()) {
            if (Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, 1);
            }
            else
            {
                callSplashout();
            }

        } else {
            callSplashout();
        }
    }

    private boolean isFirstTime()
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        System.out.println("preferece:"+preferences.getAll());
        boolean ranBefore = preferences.getBoolean("RanBefore", false);
        if (!ranBefore) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RanBefore", true);
            editor.commit();
        }
        return ranBefore;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        switch (requestCode) {
            case 1: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callSplashout();

                } else {
                    Toast.makeText(Splash.this, "Permission deny to read your Phone State", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                return;
            }

        }
    }
    void callSplashout()
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(Splash.this,MainActivity.class);
                startActivity(intent);

            }
        }, SPLASH_TIME_OUT);
    }
}
