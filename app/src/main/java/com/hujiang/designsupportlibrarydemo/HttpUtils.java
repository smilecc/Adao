package com.hujiang.designsupportlibrarydemo;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {

    public String Get(String url) {
        return Get(url, "");
    }

    public String Get(String urlStr, String cookies) {
        try {
            URL url = new URL(urlStr);
            Log.d("adao", urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String result = "";

            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(in);
            String readLine = null;
            while ((readLine = bufferedReader.readLine()) != null) {
                result += readLine;
            }
            Log.d("adao", "Get result: " + result);
            return result;
        } catch (Exception err) {
            Log.e("adao", "error: " + err.toString());
            return err.toString();
        }
    }


}
