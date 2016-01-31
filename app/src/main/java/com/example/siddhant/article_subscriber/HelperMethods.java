package com.example.siddhant.article_subscriber;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by siddhant on 31/1/16.
 */
public class HelperMethods {

    public static String makePostRequest(final Context context, String address, Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        HttpURLConnection conn;
        try {
            byte[] dataInBytes = json.getBytes("UTF8");
            URL url = new URL("http://" + Constants.SERVER_IP + address);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("content-Language", "en-US");
            conn.setRequestProperty("content-Length", Integer.toString(dataInBytes.length));
            conn.setRequestProperty("platform", "android");
            conn.setRequestProperty("version", String.valueOf(BuildConfig.VERSION_CODE));
            conn.setConnectTimeout(5000);
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.write(dataInBytes);
            wr.close();

            InputStream is = conn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception ignored) {
            return "";
        }
    }

}
