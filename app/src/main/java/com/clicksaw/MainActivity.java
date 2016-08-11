package com.clicksaw;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private String appDirectoryName;
    private File imageRoot;
    private boolean safeToTakePicture = false;
    private Camera mCamera;
    private CameraPreview mPreview;
    private android.hardware.Camera.PictureCallback mPicture;
    private ImageView btnCapture, btnSwitchCamera;
    private Context myContext;
    private RelativeLayout cameraPreview;
    private boolean cameraFront = false;
    private ImageView btnCancel,btnSave;
    public static byte[] pictureData;
    //private RelativeLayout clickLayout;
    private boolean clicked = false;
    private ImageView btnGallery;
    private int RESULT_LOAD_IMAGE = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;
        initialize();
    }
    private void initialize() {
        btnCapture = (ImageView) findViewById(R.id.click);
        btnSwitchCamera = (ImageView) findViewById(R.id.camera);
        btnCancel = (ImageView) findViewById(R.id.btnCancel);
        btnSave = (ImageView) findViewById(R.id.btnCheck);
        // clickLayout = (RelativeLayout)findViewById(R.id.click_la);
        btnGallery = (ImageView) findViewById(R.id.gallery);

        btnSave.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);

        cameraPreview = (RelativeLayout) findViewById(R.id.camera_preview);

        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.addView(mPreview);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 0);

            }
        });
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimationDrawable frameAnimation = (AnimationDrawable) btnCapture.getDrawable();
                frameAnimation.setCallback(btnCapture);
                frameAnimation.setVisible(true, true);
                frameAnimation.setOneShot(true);
                btnGallery.setVisibility(View.GONE);
                if (!clicked) {
                    frameAnimation.start();
                    mCamera.takePicture(null, mPicture, mPicture);
                    clicked = true;
                } else {
                    Toast.makeText(MainActivity.this, "Image Clicked Choose the options to continue", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnSwitchCamera.setOnClickListener(switchCameraListener);
    }
    View.OnClickListener switchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //get the number of cameras
            int camerasNumber = Camera.getNumberOfCameras();
            if (camerasNumber > 1) {
                //release the old camera instance
                //switch camera, from the front and the back and vice versa

                releaseCamera();
                chooseCamera();
            } else {
                Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("Gallery","Yes");
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                    Uri selectedImageUri = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    Bitmap image;
                    image = BitmapFactory.decodeFile(filePath);
                    int [] gridSize = Constants.getGridSize();
                    System.out.println("GridSize"+gridSize[0]+"wi:"+gridSize[1]);
                    System.out.println("ImageHeight"+image.getHeight()+"wi:"+image.getWidth());
                    if (gridSize[0]<image.getHeight() && gridSize[1]<image.getWidth()){
                        Intent intent = new Intent(MainActivity.this,SelectedImage.class);
                        intent.putExtra("filePath",filePath);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this,"Choose a higher res image",Toast.LENGTH_SHORT).show();
                    }



                }
                break;
            default:
                break;
        }
    }
    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }
    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }
    public void chooseCamera() {
        //if the camera preview is the front
        if (cameraFront) {
            int cameraId = findBackFacingCamera();

            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId);
                setCameraDisplayOrientation(MainActivity.this,cameraId,mCamera);

                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        } else {
            int cameraId = findFrontFacingCamera();

            if (cameraId >= 0) {
                //open the backFacingCamera
                //set a picture callback
                //refresh the preview

                mCamera = Camera.open(cameraId);
                setCameraDisplayOrientation(MainActivity.this,cameraId,mCamera);
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        }
    }
    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }
    private Camera.PictureCallback getPictureCallback() {
        final Camera.PictureCallback picture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                //make a new picture file

                btnCancel.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clicked = false;
                        btnGallery.setVisibility(View.VISIBLE);
                        mPreview.refreshCamera(mCamera);
                        btnCancel.setVisibility(View.GONE);
                        btnSave.setVisibility(View.GONE);
                    }
                });
                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btnGallery.setVisibility(View.VISIBLE);

                        Log.d("Picture Taken","Yes");
                        clicked = false;

                        pictureData = data;

                        Log.d("Picture",""+pictureData.length);

                        Intent intent = new Intent(MainActivity.this,SelectedImage.class);
                        intent.putExtra("front",cameraFront);
                       startActivity(intent);
                        mPreview.refreshCamera(mCamera);
                        btnCancel.setVisibility(View.GONE);
                        btnSave.setVisibility(View.GONE);

                    }
                });

            }
        };

        return picture;
    }
    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }
    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
    public void onResume() {
        super.onResume();
        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            //if the front facing camera does not exist
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                btnSwitchCamera.setVisibility(View.GONE);
            }
            mCamera = Camera.open(findBackFacingCamera());
            setCameraDisplayOrientation(MainActivity.this,findBackFacingCamera(),mCamera);
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
        }
    }
    private boolean hasCamera(Context context) {
        //check if the device has camera
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }
    public static byte[] getPictureData(){
        return pictureData;
    }



    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitByBackKey();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void exitByBackKey() {

        // selectItem(0);
        new AlertDialog.Builder(this)
                .setMessage("Do you want to exit application?")
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {

                            // do something when the button is clicked
                            public void onClick(DialogInterface arg0,
                                                int arg1) {

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    finishAffinity();
                                }

                            }
                        })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {

                            // do something when the button is clicked
                            public void onClick(DialogInterface arg0,
                                                int arg1) {
                            }
                        }).show();


    }
}
