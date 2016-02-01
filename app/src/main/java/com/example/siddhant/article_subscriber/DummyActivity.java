package com.example.siddhant.article_subscriber;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.siddhant.article_subscriber.activities.FeedActivity;
import com.example.siddhant.article_subscriber.activities.RegistrationActivity;

/**
 * Created by siddhant on 1/2/16.
 */
public class DummyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sf = getSharedPreferences(Constants.UserInfoSharedPref, Context.MODE_PRIVATE);
        boolean a = sf.getBoolean(Constants.SignInDone, false);
        if (a) {
            startActivity(new Intent(this, FeedActivity.class));
        } else {
            startActivity(new Intent(this, RegistrationActivity.class));
        }
        finish();
    }
}