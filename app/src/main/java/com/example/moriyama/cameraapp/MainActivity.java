package com.example.moriyama.cameraapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Button;
import android.view.View;
import android.content.Intent;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.util.Log;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CHOOSER = 1000;
    private final static int RESULT_CAMERA = 1001;
    private final static int REQUEST_PERMISSION = 1002;

    private final int TARGET_WIDTH = 520;
    private final int TARGET_HEIGHT = 400;

    private ImageView imageView;
    private Uri cameraUri;
    private Uri m_uri, s_uri;
    private String filePath;

    private Bitmap picture;

    private DownloadTask task;


    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image_view);
        Button cameraButton = findViewById(R.id.camera_button);
        Button httpButton = findViewById(R.id.http_button);
        Button downloadButton = findViewById(R.id.download_button);
        Button downloadButton2 = findViewById(R.id.download_button2);
        Button panelbutton = findViewById(R.id.buttonPanel);
        Button oributton = findViewById(R.id.oributton);
        Button heatButton = findViewById(R.id.download_button3);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Android 6, API 23以上でパーミッシンの確認
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermission();
                } else {
                    cameraIntent();
                }
            }
        });

        httpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraUri != null) {
                    try {
                        InputStream is = getContentResolver().openInputStream(cameraUri);
                        picture = BitmapFactory.decodeStream(is);
                        picture = resize(picture, 1200, 1000, -90);
                        picture = trim(picture, 840, 680);
                        final Handler handler = new Handler();
                        new PostBmpAsyncHttpRequest(MainActivity.this, handler).execute(new Param("http://35.247.52.9/get_img.php", picture));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if (m_uri != null){
                    try {
                        InputStream is = getContentResolver().openInputStream(m_uri);
                        picture = BitmapFactory.decodeStream(is);
                        picture = resize(picture, 840, 680, 0);
                        final Handler handler = new Handler();
                        new PostBmpAsyncHttpRequest(MainActivity.this, handler).execute(new Param("http://35.247.52.9/get_img.php", picture));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    Log.d("debug", "nophoto");
                }
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // ボタンをタップして非同期処理を開始
                task = new DownloadTask();
                // Listenerを設定
                task.setListener(createListener());
                task.execute("http://35.247.52.9/cal_v3/data.jpg");
            }
        });

        downloadButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // ボタンをタップして非同期処理を開始
                task = new DownloadTask();
                // Listenerを設定
                task.setListener(createListener());
                task.execute("http://35.247.52.9/cal_v3/result.jpg");
            }
        });

        heatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // ボタンをタップして非同期処理を開始
                task = new DownloadTask();
                // Listenerを設定
                task.setListener(createListener());
                task.execute("http://35.247.52.9/cal_v3/grad.jpg");
            }
        });

        panelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGallery();
            }
        });

        oributton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraUri != null){
                    try {
                        InputStream is = getContentResolver().openInputStream(cameraUri);
                        picture = BitmapFactory.decodeStream(is);
                        picture = resize(picture, 1200, 1000, -90);
                        picture = trim(picture, 840, 680);
                        imageView.setImageBitmap(picture);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if(m_uri != null){
                    try {
                        InputStream is = getContentResolver().openInputStream(m_uri);
                        picture = BitmapFactory.decodeStream(is);
                        picture = resize(picture, 840, 680, 0);
                        imageView.setImageBitmap(picture);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }



    @Override
    protected void onDestroy() {
        task.setListener(null);
        super.onDestroy();
    }

    private DownloadTask.Listener createListener() {
        return new DownloadTask.Listener() {
            @Override
            public void onSuccess(Bitmap bmp) {
                imageView.setImageBitmap(bmp);
            }
        };
    }


    private void cameraIntent() {

        // 保存先のフォルダーをカメラに指定した場合
        File cameraFolder = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM), "Camera");


        // 保存ファイル名
        String fileName = new SimpleDateFormat(
                "ddHHmmss", Locale.US).format(new Date());
        filePath = String.format("%s/%s.jpg", cameraFolder.getPath(), fileName);
        Log.d("debug", "filePath:" + filePath);

        // capture画像のファイルパス
        File cameraFile = new File(filePath);
        cameraUri = FileProvider.getUriForFile(
                MainActivity.this,
                getApplicationContext().getPackageName() + ".fileprovider",
                cameraFile);
        m_uri = null;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        startActivityForResult(intent, RESULT_CAMERA);

        Log.d("debug", "startActivityForResult()");
    }

    private void showGallery() {

        //カメラの起動Intentの用意
        String photoName = System.currentTimeMillis() + ".jpg";
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, photoName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        m_uri = getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, m_uri);

        // ギャラリー用のIntent作成
        Intent intentGallery;
        if (Build.VERSION.SDK_INT < 19) {
            intentGallery = new Intent(Intent.ACTION_GET_CONTENT);
            intentGallery.setType("image/*");
        } else {
            intentGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intentGallery.addCategory(Intent.CATEGORY_OPENABLE);
            intentGallery.setType("image/jpeg");
        }
        Intent intent = Intent.createChooser(intentCamera, "画像の選択");
        intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {intentGallery});
        startActivityForResult(intent, REQUEST_CHOOSER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CAMERA) {

            if(cameraUri != null){
                try {
                    InputStream is = getContentResolver().openInputStream(cameraUri);
                    picture = BitmapFactory.decodeStream(is);
                    picture = resize(picture, 900, 600, -90);
                    picture = trim(picture, TARGET_WIDTH, TARGET_HEIGHT);
                    imageView.setImageBitmap(picture);
                } catch (IOException e) {
                    return;
                }

                registerDatabase(filePath);
            }
            else{
                Log.d("debug","cameraUri == null");
            }
        }

        if(requestCode == REQUEST_CHOOSER) {

            if(resultCode != RESULT_OK) {
                // キャンセル時
                return ;
            }

            Uri resultUri = (data != null ? data.getData() : m_uri);

            if(resultUri == null) {
                // 取得失敗
                return;
            }

            // ギャラリーへスキャンを促す
            MediaScannerConnection.scanFile(
                    this,
                    new String[]{resultUri.getPath()},
                    new String[]{"image/jpeg"},
                    null
            );
            m_uri = resultUri;
            cameraUri = null;
            // 画像を設定
            imageView.setImageURI(resultUri);
        }
    }

    // アンドロイドのデータベースへ登録する
    private void registerDatabase(String file) {
        ContentValues contentValues = new ContentValues();
        ContentResolver contentResolver = MainActivity.this.getContentResolver();
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put("_data", file);
        contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    }

    // Runtime Permission check
    private void checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            cameraIntent();
        }
        // 拒否していた場合
        else {
            requestPermission();
        }
    }

    // 許可を求める
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);

        } else {
            Toast toast = Toast.makeText(this,
                    "許可されないとアプリが実行できません",
                    Toast.LENGTH_SHORT);
            toast.show();

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,},
                    REQUEST_PERMISSION);

        }
    }

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        Log.d("debug", "onRequestPermissionsResult()");

        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraIntent();

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this,
                        "これ以上なにもできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    private static Bitmap resize(Bitmap picture, int targetWidth, int targetHeight, int angle) {
        if (picture == null || targetHeight < 0 || targetWidth < 0) {
            return null;
        }

        int pictureWidth = picture.getWidth();
        int pictureHeight = picture.getHeight();
        float scale = Math.min((float) targetWidth / pictureWidth, (float) targetHeight / pictureHeight);

        Matrix matrix = new Matrix();
        matrix.setRotate(angle, pictureWidth/2, pictureHeight/2);
        matrix.postScale(scale, scale);

        return Bitmap.createBitmap(picture, 0, 0, pictureWidth, pictureHeight, matrix, true);
    }

    private static Bitmap trim(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();

            //トリミングする幅、高さ、座標の設定
            int startX = (width - maxWidth) /2;
            int startY = (height - maxHeight)/2;

            Bitmap result = Bitmap.createBitmap(image, startX, startY, maxWidth, maxHeight, null, true);

            return result;
        } else {
            return image;
        }
    }
}
