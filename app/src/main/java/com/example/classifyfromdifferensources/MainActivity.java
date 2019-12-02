package com.example.classifyfromdifferensources;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import org.tensorflow.lite.examples.classification.tflite.Classifier;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.example.classifyfromdifferensources.R.string.test_unsuccessful;

public class MainActivity extends AppCompatActivity {
    public static Classifier classifier;
    final int REQUEST_GET_SINGLE_FILE = 303;
    TextView recTag0, recConf0;
    ImageView img;
    Button btnGallery, btnStream, btnCamera, btnLiveStream;
    Bitmap bitmap, resizedBitmap, resizedBitmap2;
    //Logger LOGGER= Logger.getGlobal();
    int resultSize = 0;
    private Classifier.Model model = Classifier.Model.QUANTIZED;
    private Classifier.Device device = Classifier.Device.CPU;
    private Context myContext;

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myContext = getApplicationContext();
        setContentView(R.layout.activity_main);
        recTag0 = findViewById(R.id.recTag0);
        recConf0 = findViewById(R.id.recConf0);
        img = findViewById(R.id.img1);
        btnGallery = findViewById(R.id.openFromGallery);
        btnStream = findViewById(R.id.liveBtn);
        btnCamera = findViewById(R.id.cameraButton);
        btnLiveStream = findViewById(R.id.btnLiveStream);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stp);
        final String mImageURLString = "https://homepages.cae.wisc.edu/~ece533/images/airplane.png";
        rereateClassifier();
        processImage(bitmap, img, recTag0, recConf0);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GET_SINGLE_FILE);
            }
        });
        btnStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue requestQueue = Volley.newRequestQueue(myContext);
                /* String url ="http://www.google.com";
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                recTag0.setText("Response is: "+ response.substring(0,500));
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("error", "onErrorResponse: "+error.toString());
                    }
                });
                requestQueue.add(stringRequest);*/
                ImageRequest imageRequest = new ImageRequest(mImageURLString, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        Log.d("succesfull", "onResponse: success ");
                        img.setImageBitmap(response);
                        bitmap = response;
                        processImage(bitmap, img, recTag0, recConf0);
                    }
                }, 0, 0, null, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        recTag0.setText(test_unsuccessful);
                    }
                });
                requestQueue.add(imageRequest);
            }
        });
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, org.tensorflow.lite.examples
                        .classification.ClassifierActivity.class);
                startActivity(intent);

            }
        });
        btnLiveStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, com.example.classifyfromdifferensources.StreamingActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_GET_SINGLE_FILE) {
                    Uri selectedImageUri = data.getData();
                    // Get the path from the Uri
                    final String path = getPathFromURI(selectedImageUri);
                    if (path != null) {
                        File f = new File(path);
                        selectedImageUri = Uri.fromFile(f);
                    }
                    // Set the image in ImageView
                    Uri imageUri = data.getData();
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    img.setImageBitmap(bitmap);
                    processImage(bitmap, img, recTag0, recConf0);
                }
            }
        } catch (Exception e) {
            Log.e("FileSelectorActivity", "File select error", e);
        }
    }

    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        assert cursor != null;
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    public void processImage(Bitmap bitmap, ImageView img, TextView recTag0, TextView recConf0) {
        resizedBitmap = getResizedBitmap(bitmap, classifier.getImageSizeX(), classifier.getImageSizeY());
        resizedBitmap2 = resizedBitmap;
        final List<Classifier.Recognition> results = classifier.recognizeImage(resizedBitmap);
        resultSize = Math.min(results.size(), 3);
        for (int i = 0; i < resultSize; i++) {
            Classifier.Recognition rec = results.get(i);
            String willBePrinted = "id=" + rec.getTitle() + "confidence" + rec.getConfidence();
            Log.d("results", willBePrinted);
        }
        img.setImageBitmap(resizedBitmap2);
        recTag0.setText(results.get(0).getTitle());
        recConf0.setText(String.format(Locale.ENGLISH,"%f",results.get(0).getConfidence())) ;
    }

    public void rereateClassifier() {
        if (device == Classifier.Device.GPU && model == Classifier.Model.QUANTIZED) {
            Log.d("device err:", "Not creating classifier: GPU doesn't support quantized models.");
            return;
        }
        try {
            Log.d("running:",
                    "Creating classifier (model=%s, device=%s, numThreads=%d)");
            int numThreads = -1;
            classifier = Classifier.create(MainActivity.this, model, device, numThreads);
        } catch (IOException e) {
            Log.e("exception", "Failed to create classifier.");
        }
    }
}
