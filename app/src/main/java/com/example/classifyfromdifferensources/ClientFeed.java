package com.example.classifyfromdifferensources;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.examples.classification.tflite.Classifier;

import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static com.example.classifyfromdifferensources.MainActivity.getResizedBitmap;

public class ClientFeed extends AppCompatActivity {
    public static FrameNumberListener frameNumberListener = new FrameNumberListener();
    public static boolean LOOP_FOR_FRAME = true;
    static List<Classifier.Recognition> results;
    static boolean threadFree = true;
    static boolean isProcessFinished = true;
    WebView clientView;
    ImageView screenShot;
    Button btnCapture;
    TextView feedRecTag0;
    TextView feedRecConf0;
    TextView time;
    Classifier classifier = MainActivity.classifier;
    Bitmap bmp;
    Bitmap resizedBitmap;
    private boolean continueFrame = false;

    public static Bitmap screenshot(WebView webView) {
        try {
            float scale = webView.getScale();
            // int height = (int) (webView.getContentHeight()/scale+ 0.5);
            //Bitmap bitmap = Bitmap.createBitmap((int) (webView.getWidth()/scale), height, Bitmap.Config.ARGB_8888);
            Bitmap bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            webView.draw(canvas);
            bitmap = Bitmap.createBitmap(bitmap, 25, 25, 775, 480);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView.enableSlowWholeDocumentDraw();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.client_feed);
        clientView = findViewById(R.id.web_view_feed);
        screenShot = findViewById(R.id.screenShot);
        btnCapture = findViewById(R.id.capture);
        time = findViewById(R.id.feed_time);
        feedRecConf0 = findViewById(R.id.feed_recConf0);
        feedRecTag0 = findViewById(R.id.feed_recTag0);
        frameNumberListener.setValueChangeListener(new FrameNumberListener.onValueChangeListener() {
            @Override
            public void onChange() {
                if (continueFrame) {
                    screenShot.post(new Runnable() {
                        @Override
                        public void run() {
                            bmp = screenshot(clientView);
                            screenShot.setImageBitmap(bmp);
                            long i = System.currentTimeMillis();
                            time.setText(String.format(Locale.ENGLISH, "%f", results.get(0).getConfidence()));
                            if (threadFree) {
                                isProcessFinished = false;
                                Bitmap tmpBmp = bmp.copy(bmp.getConfig(), true);
                                resizedBitmap = getResizedBitmap(tmpBmp, classifier.getImageSizeX(), classifier.getImageSizeY());
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        long i = System.currentTimeMillis();
                                        threadFree = false;
                                        results = classifier.recognizeImage(resizedBitmap);
                                        feedRecTag0.setText(results.get(0).getTitle());
                                        feedRecConf0.setText(String.format(Locale.ENGLISH, "%f", results.get(0).getConfidence()));
                                        isProcessFinished = true;
                                        Log.d(TAG, "run:" + (i - System.currentTimeMillis()));
                                        threadFree = true;
                                    }
                                }).start();
                            }
                        }
                    });
                }
            }
        });
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* screenShot.setImageBitmap(screenshot(clientView));
                tvClientFeed.setText(String.format("time is:%s",System.currentTimeMillis()));*/
                continueFrame = !continueFrame;
                if (!continueFrame) screenShot.setVisibility(View.INVISIBLE);
                else screenShot.setVisibility(View.VISIBLE);

            }
        });
        String ip = getIntent().getStringExtra("ip");
        Integer port = Integer.valueOf(getIntent().getStringExtra("port"));
        clientView.setWebViewClient(new MyClient());
        clientView.loadUrl("http://" + ip + ":" + port);
        clientView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        new VideoFromWeb().execute();

    }

    private static class VideoFromWeb extends AsyncTask<Integer, Integer, Long> {
        @Override
        protected Long doInBackground(Integer... integers) {
            long i = System.currentTimeMillis();
            do {
                if (System.currentTimeMillis() - i > 40) {
                    frameNumberListener.setFrameNumber(frameNumberListener.getFrameNumber() + 1);
                    publishProgress();
                    i = System.currentTimeMillis();
                }
            } while (!isCancelled());
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

    private class MyClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Log.d("test", "onLoadResource: onLoadResource");
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Log.d("test", "onLoadResource:shouldInterceptRequest ");
            return super.shouldInterceptRequest(view, request);
        }
    }
}