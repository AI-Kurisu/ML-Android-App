package com.example.afinal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.IOException;

public class TextRecognitionResult extends AppCompatActivity {


    private TextView editTextView;
    private ImageView mimageView;
    private Uri ImaUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_recognition_result);
        mimageView = findViewById(R.id.textImageView);

        Bundle bundle = getIntent().getExtras();
        ImaUri = (Uri) bundle.get("camUri");

        mimageView.setImageURI(ImaUri);

        try {
            InputImage image = InputImage.fromFilePath(this, ImaUri);

            TextRecognizer recognizer = TextRecognition.getClient();
            Task<Text> result =
                    recognizer.process(image)
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text visionText) {
                                    // Task completed successfully
                                    // ...
                                    String s = visionText.getText();
                                    if (s.length() == 0){
                                        editTextView = findViewById(R.id.editTextTextMultiLine);
                                        editTextView.setVisibility(View.INVISIBLE);

                                        Toast.makeText(TextRecognitionResult.this, "No Text Found", Toast.LENGTH_SHORT).show();

                                    }else {

                                        editTextView.setText(s);

                                    }

                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                            Toast.makeText(TextRecognitionResult.this, "Can not setup text detector", Toast.LENGTH_SHORT).show();
                                        }
                                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }

}

