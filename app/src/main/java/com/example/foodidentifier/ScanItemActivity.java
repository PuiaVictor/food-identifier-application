package com.example.foodidentifier;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.foodidentifier.ml.MyModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ScanItemActivity extends AppCompatActivity {
    TextView result, confidence, confidence2;
    ImageView imageView;
    Button takePicture, wrongButton, correctButton;
    int imageSize = 224;
    float maxConfidence = 0;
    private Uri imageUri;

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
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference dbReference = firebaseDatabase.getReference().child("Scans");
        StorageReference dbStorageReference = FirebaseStorage.getInstance().getReference().child("Scans");

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
                        showSuggestionForCorrectAnswerDialog();
                    }
                }
        );

        correctButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StorageReference fileStorage = dbStorageReference.child(result.getText().toString() + "." + getFileExtension(imageUri));
                        fileStorage.putFile(imageUri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        DBItem dbItem = new DBItem(
                                                result.getText().toString(),
                                                String.format("%.2f", maxConfidence * 100) + " %",
                                                taskSnapshot.getStorage().getDownloadUrl().toString()
                                        );
                                        dbReference.push().setValue(dbItem);
                                        Toast.makeText(ScanItemActivity.this, "Thank you for your feedback!", Toast.LENGTH_LONG).show();
                                        wrongButton.setEnabled(false);
                                        correctButton.setEnabled(false);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ScanItemActivity.this, "The file could not be uploaded.", Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                }
        );

    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void showSuggestionForCorrectAnswerDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View alertWindowView = inflater.inflate(R.layout.activity_insert_correct_solution_dialog_window, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(alertWindowView)
                .setTitle("Insert the correct answer:");
        builder.setCancelable(false);
        AlertDialog alertWindow = builder.create();
        alertWindow.show();
        EditText correctedItem = (EditText) alertWindowView.findViewById(R.id.correctedItem);
        Button sendButton = (Button) alertWindowView.findViewById(R.id.sendButton);
        Button cancelButton = (Button) alertWindowView.findViewById(R.id.cancelButton);
        sendButton.setEnabled(false);
        correctedItem.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                sendButton.setEnabled(!correctedItem.getText().toString().trim().isEmpty());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                sendButton.setEnabled(!correctedItem.getText().toString().trim().isEmpty());
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled(!correctedItem.getText().toString().trim().isEmpty());
            }
        });

        sendButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertWindow.dismiss();
                        Toast.makeText(ScanItemActivity.this, "Thank you for your feedback!", Toast.LENGTH_LONG).show();
                        wrongButton.setEnabled(false);
                        correctButton.setEnabled(false);
                    }
                }
        );

        cancelButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertWindow.dismiss();
                    }
                }
        );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            imageUri = getImageUri(this, image);
            int dimension = Math.min(image.getWidth(), image.getHeight());
            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
            imageView.setImageBitmap(image);

            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
            classifyFood(image);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Uri getImageUri(Context context, Bitmap bitmapImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmapImage, "Title", null);
        return Uri.parse(path);
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

            maxConfidence = 0;
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
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            String[] classes = {"Banana", "Avocado", "Eggs", "Mozzarella", "Water", "Milk"};

            result.setText(classes[maxPos]);
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
