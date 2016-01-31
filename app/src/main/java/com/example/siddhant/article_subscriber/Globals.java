package com.example.siddhant.article_subscriber;

import android.app.Application;
import android.graphics.Typeface;

/**
 * Created by siddhant on 31/1/16.
 */
public class Globals extends Application {

    public static Typeface typeface;

    @Override
    public void onCreate() {
        typeface = Typeface.createFromAsset(getAssets(), "proxima.otf");
        super.onCreate();
    }
}
