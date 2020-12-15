package com.example.afinal;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private static final String TAG = "MainActivity";

    private static final int LAUNCH_CAMERA = 1;
    private static final int REQUEST_CHOOSE_IMAGE = 2;

    private ImageView mimageView;
    private Uri ImageUri;
    private Bitmap resizeBmp;

    boolean isLandScape;
    private boolean isOpen;

    private int imageMaxWidth;
    private int imageMaxHeight;

    Animation fabOpen, fabClose, rotateForward, rotateBackward;

    com.google.android.material.floatingactionbutton.FloatingActionButton addbtn;
    com.google.android.material.floatingactionbutton.FloatingActionButton camerabtn;
    com.google.android.material.floatingactionbutton.FloatingActionButton gallerybtn;
    com.google.android.material.button.MaterialButton textrecbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.side_navi_main);
        Menu menu;
        drawer = findViewById(R.id.drawer_layout);
        mimageView = findViewById(R.id.imageView);

        navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_gallery, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);

        addbtn = findViewById(R.id.fab_add);
        camerabtn = findViewById(R.id.fab_camera);
        gallerybtn = findViewById(R.id.fab_gallery);
        textrecbtn = findViewById(R.id.text_btn);


        isOpen = false;
        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);

        rotateForward = AnimationUtils.loadAnimation(this, R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(this, R.anim.rotate_backward);


        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOpen) {

                    addbtn.setAnimation(rotateBackward);
                    camerabtn.setAnimation(fabClose);
                    gallerybtn.setAnimation(fabClose);

                    camerabtn.setVisibility(View.INVISIBLE);
                    gallerybtn.setVisibility(View.INVISIBLE);

                    isOpen = false;
                } else {
                    addbtn.setAnimation(rotateForward);
                    camerabtn.setAnimation(fabOpen);
                    gallerybtn.setAnimation(fabOpen);

                    camerabtn.setVisibility(View.VISIBLE);
                    gallerybtn.setVisibility(View.VISIBLE);


                    isOpen = true;
                }

            }}
            );


        camerabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent camAct = new Intent(MainActivity.this, CameraActivity.class);
                startActivityForResult(camAct, LAUNCH_CAMERA);

            }

        });


        gallerybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startChooseImageIntentForResult();

            }
        });


        textrecbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(resizeBmp != null) {

                Intent intent = new Intent(MainActivity.this, TextRecognitionResult.class);
                intent.putExtra("camUri", ImageUri);
                startActivity(intent);

            }else{

                Toast.makeText(MainActivity.this, "Please have an image.", Toast.LENGTH_SHORT).show();
            }

            }
        });
        View rootView = findViewById(R.id.root);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                imageMaxWidth = rootView.getWidth();
                imageMaxHeight = rootView.getHeight() - findViewById(R.id.fab_add).getHeight();


            }
        });

        isLandScape =
                (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private Bitmap tryReloadAndDetectImage(Bitmap resizeBmp){
        try {

            Bitmap imgBmp = BitmapUtils.getBitmapFromContentUri(getContentResolver(), ImageUri);

            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

            float scaleFactor =
                    Math.max(
                            (float) imgBmp.getWidth() / (float) targetedSize.first,
                            (float) imgBmp.getHeight() / (float) targetedSize.second);

            resizeBmp =
                    Bitmap.createScaledBitmap(
                            imgBmp,
                            (int) (imgBmp.getWidth() / scaleFactor),
                            (int) (imgBmp.getHeight() / scaleFactor),
                            true);

            mimageView.setImageBitmap
                        (resizeBmp);



        } catch (IOException e) {
           ImageUri = null;
        }

        return resizeBmp;

    }

    private void startChooseImageIntentForResult() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == LAUNCH_CAMERA && resultCode == RESULT_OK){

            textrecbtn.setVisibility(View.VISIBLE);
            ImageUri = (Uri) data.getExtras().get("camUri");
            tryReloadAndDetectImage(resizeBmp);
            faceDetect();

        }else if(requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK){

            textrecbtn.setVisibility(View.VISIBLE);
            ImageUri = data.getData();
            tryReloadAndDetectImage(resizeBmp);
            faceDetect();

        }
    }
    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth;
        int targetHeight;

        targetWidth = imageMaxWidth;
        targetHeight = imageMaxHeight;
        return new Pair<>(targetWidth, targetHeight);
    }

    private void faceDetect() {

        //Create the face detector
        FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false)
                .build();
        if (!faceDetector.isOperational()) {
            new AlertDialog.Builder(mimageView.getContext()).setMessage("Can not setup the face detector").show();
            return;
        }

        //Create a paint Object
        Paint mRectPaint = new Paint();
        mRectPaint.setStrokeWidth(25);
        mRectPaint.setColor(Color.BLACK);
        mRectPaint.setStyle(Paint.Style.STROKE);

        resizeBmp = tryReloadAndDetectImage(resizeBmp);

        if (resizeBmp != null) {

            Bitmap tempBmp = Bitmap.createBitmap(resizeBmp.getWidth(), resizeBmp.getHeight(), Bitmap.Config.RGB_565);

            Canvas tempCanvas = new Canvas(tempBmp);
            tempCanvas.drawBitmap(resizeBmp, 0, 0, null);

            //Detect the face
            Frame frame = new Frame.Builder().setBitmap(resizeBmp).build();
            SparseArray<Face> faces = faceDetector.detect(frame);

            mimageView.setImageDrawable(new BitmapDrawable(getResources(), tempBmp));
            // Draw Rectangles on the faces
            for (int i = 0; i < faces.size(); i++) {
                Face thisFace = faces.valueAt(i);
                float x1 = thisFace.getPosition().x;
                float y1 = thisFace.getPosition().y;
                float x2 = x1 + thisFace.getWidth();
                float y2 = y1 + thisFace.getHeight();

                tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, mRectPaint);
            }
        }else{
            Toast.makeText(this, "Missing image", Toast.LENGTH_SHORT).show();
        }

        }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        Class fragmentClass;
        switch(item.getItemId()){
            default:
                fragmentClass = GalleryFragment.class;
                drawer.closeDrawers();
                startChooseImageIntentForResult();
                break;

        }
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.root, fragment).commit();

        return false;
    }
}



