package com.example.afinal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_PICTURES;

public class CameraActivity extends MainActivity {

    private static final String KEY_IMAGE_URI = "com.example.afinal.KEY_IMAGE_URI";
    boolean isLandScape;
    private File imageFile;
    private Uri imageUri;
    private String currentImagePath;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int IMAGE_REQUEST_CODE = 102;
    private static final int PERM_CODE = 103;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                {
                    dispatchCaptureImageIntent();

                }else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERM_CODE);


    }}


    private void dispatchCaptureImageIntent(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            File imageFile = null;
            try{

                imageFile = createImageFile();

            }catch(IOException exception){
                Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }

            if(imageFile != null){
                imageUri = FileProvider.getUriForFile(
                        this,
                        "com.example.afinal.fileprovider",
                        imageFile
                );
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, IMAGE_REQUEST_CODE);

            }
        }
    }


    private File createImageFile() throws IOException{
        String fileName = "IMAGE_"
                + new SimpleDateFormat(
                "yyyy_MM_dd_HH_mm_ss", Locale.getDefault()
        ).format(new Date());

        File directory = getExternalFilesDir(DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                fileName,
                ".JPG",
                directory
        );
        currentImagePath = imageFile.getAbsolutePath();
        Toast.makeText(this, "createImgF()", Toast.LENGTH_SHORT).show();

        FileOutputStream outputStream = null;
        outputStream = new FileOutputStream(currentImagePath);
        outputStream.flush();
        outputStream.close();
        galleryAddPic();
        return imageFile;
    }


    private void galleryAddPic(){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(String.valueOf(imageFile));
        Uri contentUri = Uri.fromFile(new File(currentImagePath));
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if(grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    dispatchCaptureImageIntent();
            }else {
                Toast.makeText(this, "permission is required", Toast.LENGTH_SHORT).show();
            }
        }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK){

            try{

                Intent returnPath = new Intent();
                returnPath.putExtra("camUri", imageUri);
                setResult(RESULT_OK, returnPath);

                finish();

            }catch (Exception exception){
                Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


}
