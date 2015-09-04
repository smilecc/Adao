package com.smilec.adao;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sxcui on 2015/9/3.
 */
public class CookieManager {
    public static String Get(Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences("cookie", Context.MODE_PRIVATE);
        return sharedPreferences.getString("cookie","");
    }

    public static boolean Set(Context context,String cookie)
    {
        SharedPreferences.Editor editor = context.getSharedPreferences("cookie", Context.MODE_PRIVATE).edit();
        editor.putString("cookie",cookie);
        return editor.commit();
    }

    public static void initCookie(MainActivity Actvitity)
    {
        initCookie(Actvitity,false);
    }

    public static void initCookie(MainActivity Actvitity,Boolean isShowTips)
    {
        Context mContext = (Context)Actvitity;
        try
        {
            URL url = new URL("http://h.nimingban.com/Api/getCookie");
            HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
            urlConn.setRequestProperty("User-Agent", "HavfunClient-Android");

            BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            String responseCookie = urlConn.getHeaderField("Set-Cookie");

            String ResponseContent= br.readLine();
            Log.d("adao", ResponseContent);
            if(ResponseContent.equals("\"ok\""))
            {
                Pattern pattern=Pattern.compile("userhash=.*?;");
                Matcher m=pattern.matcher(responseCookie);
                if(m.find()) {
                    String cookieFromResponse = m.group();
                    Log.d("adao", "cookie from server " + cookieFromResponse);

                    CookieManager.Set(mContext,cookieFromResponse);
                if(isShowTips)
                {
                    Message message = new Message();
                    message.what = MainActivity.GET_COOKIE_OVER;
                    Actvitity.myhandler.sendMessage(message);
                }
                }else{
                    Message message = new Message();
                    message.what = MainActivity.GET_COOKIE_FAIL;
                    Actvitity.myhandler.sendMessage(message);
                }
            }else{
                Message message = new Message();
                message.what = MainActivity.GET_COOKIE_REFUSE;
                Actvitity.myhandler.sendMessage(message);
            }


        }catch (Exception err)
        {
            Log.d("adao", err.toString());
            Message message = new Message();
            message.what = MainActivity.GET_COOKIE_FAIL;
            Actvitity.myhandler.sendMessage(message);
        }

    }
}
