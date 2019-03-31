package com.example.moriyama.cameraapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostBmpAsyncHttpRequest extends AsyncTask<Param, Void, String> {
    private Activity mActivity;
    String[] words;
    private final int TARGET_WIDTH = 520;
    private final int TARGET_HEIGHT = 400;
    Bitmap bmp = null;
    private Handler mhandler;

    public PostBmpAsyncHttpRequest(Activity activity, Handler handler) {
        mActivity = activity;
        mhandler = handler;
    }

    @Override
    protected String doInBackground(Param... params) {
        Param param = params[0];
        HttpURLConnection connection = null;
        StringBuilder sb = new StringBuilder();
        try {
            // 画像をjpeg形式でstreamに保存
            ByteArrayOutputStream jpg = new ByteArrayOutputStream();
            param.bmp.compress(Bitmap.CompressFormat.JPEG, 100, jpg);


            URL url = new URL(param.uri);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(100000);//接続タイムアウトを設定する。
            connection.setReadTimeout(100000);//レスポンスデータ読み取りタイムアウトを設定する。
            connection.setRequestMethod("POST");//HTTPのメソッドをPOSTに設定する。
            //ヘッダーを設定する
            connection.setRequestProperty("User-Agent", "Android");
            connection.setRequestProperty("Content-Type","application/octet-stream");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setDoInput(true);//リクエストのボディ送信を許可する
            connection.setDoOutput(true);//レスポンスのボディ受信を許可する
            connection.setUseCaches(false);//キャッシュを使用しない
            connection.connect();

            // データを投げる
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            out.write(jpg.toByteArray());
            out.flush();
            publishProgress();
            // データを受け取る
            InputStream is = connection.getInputStream();
            //bmp = BitmapFactory.decodeStream(is);
            //setView(bmp, mhandler);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line = "";
            while ((line = reader.readLine()) != null)
                words = line.split(">>>", 0);
            for (int i = 0 ; i < words.length ; i++){
                sb.append(words[i]);
                sb.append("\n");
            }
            sb.append("\n");
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            connection.disconnect();
        }
        return sb.toString();
    }
    @Override
    protected void onProgressUpdate(Void... progress) {
        TextView textView = mActivity.findViewById(R.id.textView);
        textView.setText("通信中");
    }
    public void onPostExecute(String string) {
        // 戻り値をViewにセット
        TextView textView = mActivity.findViewById(R.id.textView);
        textView.setText(string);
    }
    public void SetView(final Bitmap bm, Handler handler) {
        // 戻り値をViewにセット
        handler.post(new Runnable() {
            @Override
            public void run() {
                Bitmap bm2;
                ImageView View = mActivity.findViewById(R.id.image_view);
                bm2 = resize(bm, TARGET_WIDTH, TARGET_HEIGHT);
                //bm2 = bm;
                View.setImageBitmap(bm2);
                TextView textView = mActivity.findViewById(R.id.textView);
                textView.setText("通信終了");
            }
        });
    }

    private static Bitmap resize(Bitmap picture, int targetWidth, int targetHeight) {
        if (picture == null || targetHeight < 0 || targetWidth < 0) {
            return null;
        }

        int pictureWidth = picture.getWidth();
        int pictureHeight = picture.getHeight();
        float scale = (float) targetHeight / pictureHeight;

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        return Bitmap.createBitmap(picture, 0, 0, pictureWidth, pictureHeight, matrix, true);
    }


}
