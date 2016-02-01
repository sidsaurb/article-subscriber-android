package com.example.siddhant.article_subscriber.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.siddhant.article_subscriber.Constants;
import com.example.siddhant.article_subscriber.Globals;
import com.example.siddhant.article_subscriber.HelperMethods;
import com.example.siddhant.article_subscriber.R;
import com.example.siddhant.article_subscriber.networkClasses.AddSubscriptionResponse;
import com.example.siddhant.article_subscriber.networkClasses.Categories;
import com.example.siddhant.article_subscriber.networkClasses.GetArticleContent;
import com.example.siddhant.article_subscriber.networkClasses.GetArticleContentResponse;
import com.example.siddhant.article_subscriber.networkClasses.GetCategoriesResponse;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ContentActivity extends AppCompatActivity {
    private SweetAlertDialog progress;
    SharedPreferences sf;
    TextView contentTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        progress = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progress.setTitleText("Processing");
        progress.getProgressHelper().setBarColor(R.color.light_red);
        progress.setCancelable(false);

        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        TextView categoryTextView = (TextView) findViewById(R.id.categoryTextView);
        TextView detailsTextView = (TextView) findViewById(R.id.detailsTextView);
        TextView emailTextView = (TextView) findViewById(R.id.emailTextView);
        contentTextView = (TextView) findViewById(R.id.contentTextView);

        titleTextView.setTypeface(Globals.typeface);
        categoryTextView.setTypeface(Globals.typeface);
        detailsTextView.setTypeface(Globals.typeface);
        emailTextView.setTypeface(Globals.typeface);
        contentTextView.setTypeface(Globals.typeface);

        Intent i = getIntent();
        String title = i.getStringExtra("title");
        String email = i.getStringExtra("email");
        String name = i.getStringExtra("name");
        Long timestamp = i.getLongExtra("timestamp", 0);
        Integer id = i.getIntExtra("id", 0);
        Integer categoryId = i.getIntExtra("categoryId", 0);

        titleTextView.setText(title);
        String details = "by " + name + ", ";
        DateFormat df1 = new SimpleDateFormat("MMM dd yyyy hh:mm:ss a", Locale.getDefault());

        Date date2 = new Date(timestamp);
        try {
            details += df1.format(date2);
        } catch (Exception ignored) {
        }
        detailsTextView.setText(details);
        emailTextView.setText(email);

        sf = getSharedPreferences(Constants.UserInfoSharedPref, MODE_PRIVATE);
        String categories = sf.getString(Constants.Categories, "");
        if (!categories.isEmpty()) {
            GetCategoriesResponse myCategories = new Gson().fromJson(categories, GetCategoriesResponse.class);
            for (Categories item : myCategories.data) {
                if (item.id == categoryId) {
                    categoryTextView.setText("-- " + item.name);
                    break;
                }
            }
        }
        LoadContent(id);
    }

    private void LoadContent(final int id) {
        progress.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                GetArticleContent myReq = new GetArticleContent(id);
                String response = HelperMethods.makePostRequest(ContentActivity.this, Constants.GET_ARTICLE_CONTENT, myReq);
                if (!response.isEmpty()) {
                    final GetArticleContentResponse myResponse = new Gson().fromJson(response, GetArticleContentResponse.class);
                    if (myResponse.success) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                                contentTextView.setText("\n" + myResponse.data);
                            }
                        });
                    } else {
                        showDialogAndExit();
                    }
                } else {
                    showDialogAndExit();
                }
            }
        }).start();
    }

    private void showDialogAndExit() {
        runOnUiThread(new Runnable() {
            public void run() {
                progress.dismiss();
                new SweetAlertDialog(ContentActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops..")
                        .setContentText("Can't connect to servers")
                        .setConfirmText("Ok")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                finish();
                            }
                        })
                        .show();
            }
        });
    }
}
