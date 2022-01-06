package com.example.foodidentifier;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.foodidentifier.ml.MyModel;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ScanItemActivity extends AppCompatActivity {
    TextView result, confidence, confidence2;
    ImageView imageView;
    Button takePicture, wrongButton, correctButton;
    int imageSize = 224;
    String scannedItemType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_item);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        result = findViewById(R.id.result);
        confidence = findViewById(R.id.confidence);
        confidence2 = findViewById(R.id.confidence2);
        imageView = findViewById(R.id.imageView);
        takePicture = findViewById(R.id.pictureButton);
        wrongButton = findViewById(R.id.wrongButton);
        correctButton = findViewById(R.id.correctButton);

        takePicture.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                // Launch camera if we have permission
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 1);
                } else {
                    //Request camera permission if we don't have it.
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });
        wrongButton.setEnabled(false);
        correctButton.setEnabled(false);
        wrongButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //display window to insert correct answer
                        Toast.makeText(ScanItemActivity.this, "Thank you for your feedback!", Toast.LENGTH_LONG).show();
                    }
                }
        );

        correctButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //insert to DB
                        Toast.makeText(ScanItemActivity.this, "Thank you for your feedback!", Toast.LENGTH_LONG).show();
                    }
                }
        );

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            int dimension = Math.min(image.getWidth(), image.getHeight());
            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
            imageView.setImageBitmap(image);

            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
            classifyFood(image);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void classifyFood(Bitmap image) {
        try {
            MyModel model = MyModel.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);
            // Runs model inference and gets result.
            MyModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            String[] classes = {"Banana", "Avocado", "Eggs", "Mozzarella", "Water", "Milk"};

            result.setText(classes[maxPos]);
            scannedItemType = classes[maxPos];
            wrongButton.setEnabled(true);
            correctButton.setEnabled(true);

            String confidenceColumn1 = "";
            String confidenceColumn2 = "";

            for (int i = 0; i < 3; i++) {
                confidenceColumn1 += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100);
            }

            for (int i = 3; i < 6; i++) {
                confidenceColumn2 += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100);
            }

            confidence.setText(confidenceColumn1);
            confidence2.setText(confidenceColumn2);
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }

    }
}
