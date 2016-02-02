package com.example.siddhant.article_subscriber.activities;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.example.siddhant.article_subscriber.Constants;
import com.example.siddhant.article_subscriber.Globals;
import com.example.siddhant.article_subscriber.HelperMethods;
import com.example.siddhant.article_subscriber.R;
import com.example.siddhant.article_subscriber.adapters.SubscriptionAdapter;
import com.example.siddhant.article_subscriber.TypefaceSpan;
import com.example.siddhant.article_subscriber.networkClasses.AddSubscription;
import com.example.siddhant.article_subscriber.networkClasses.AddSubscriptionResponse;
import com.example.siddhant.article_subscriber.networkClasses.GetCategoriesResponse;
import com.example.siddhant.article_subscriber.networkClasses.GetSubscriptions;
import com.example.siddhant.article_subscriber.networkClasses.GetSubscriptionsResponse;
import com.google.gson.Gson;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SelectSubscriptionActivity extends AppCompatActivity {

    private SweetAlertDialog progress;
    SharedPreferences sf;
    ListView categoriesListView;
    SubscriptionAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_subscription);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            SpannableString s = new SpannableString("Select subscription categories..");
            s.setSpan(new TypefaceSpan(Globals.typeface), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            actionBar.setTitle(s);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            upArrow.setColorFilter(ContextCompat.getColor(this, R.color.red), PorterDuff.Mode.SRC_ATOP);
            actionBar.setHomeAsUpIndicator(upArrow);
        }

        categoriesListView = (ListView) findViewById(R.id.categoriesListView);

        progress = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progress.setTitleText("Processing");
        progress.getProgressHelper().setBarColor(R.color.light_red);
        progress.setCancelable(false);

        sf = getSharedPreferences(Constants.UserInfoSharedPref, MODE_PRIVATE);

        LoadPreviousSubscriptions();
    }

    GetSubscriptionsResponse mySubscriptions;
    GetCategoriesResponse myCategories;

    private void LoadPreviousSubscriptions() {
        progress.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                GetSubscriptions myReq = new GetSubscriptions(sf.getInt(Constants.UserId, -1));
                String response = HelperMethods.makePostRequest(SelectSubscriptionActivity.this, Constants.GET_SUBSCRIPTIONS, myReq);
                if (!response.isEmpty()) {
                    mySubscriptions = new Gson().fromJson(response, GetSubscriptionsResponse.class);
                    if (mySubscriptions.success) {
                        String categories = sf.getString(Constants.Categories, "");
                        if (categories.isEmpty()) {
                            boolean result = HelperMethods.getAndSaveCategories(SelectSubscriptionActivity.this);
                            if (result) {
                                categories = sf.getString(Constants.Categories, "");
                                myCategories = new Gson().fromJson(categories, GetCategoriesResponse.class);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LoadListView();
                                    }
                                });
                            } else {
                                showDialogAndExit();
                            }
                        } else {
                            myCategories = new Gson().fromJson(categories, GetCategoriesResponse.class);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    LoadListView();
                                }
                            });
                        }
                    } else {
                        showDialogAndExit();
                    }
                } else {
                    showDialogAndExit();
                }
            }
        }).start();
    }

    private void LoadListView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.dismiss();
                myAdapter = new SubscriptionAdapter(SelectSubscriptionActivity.this, myCategories.data, mySubscriptions);
                categoriesListView.setAdapter(myAdapter);
            }
        });
    }

    private void showDialogAndExit() {
        runOnUiThread(new Runnable() {
            public void run() {
                progress.dismiss();
                new SweetAlertDialog(SelectSubscriptionActivity.this, SweetAlertDialog.ERROR_TYPE)
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_select_subscription, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_done:
                sendSubscriptionsToServer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendSubscriptionsToServer() {
        progress.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                AddSubscription myReq = new AddSubscription(sf.getInt(Constants.UserId, -1), myAdapter.mySubscriptions.data);
                String response = HelperMethods.makePostRequest(SelectSubscriptionActivity.this, Constants.ADD_SUBSCRIPTIONS, myReq);
                if (!response.isEmpty()) {
                    AddSubscriptionResponse myResponse = new Gson().fromJson(response, AddSubscriptionResponse.class);
                    if (myResponse.success) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                                SharedPreferences.Editor editor = sf.edit();
                                editor.putBoolean(Constants.ReloadFeeds, true);
                                editor.commit();
                                Toast.makeText(SelectSubscriptionActivity.this, "Subscriptions updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                                Toast.makeText(SelectSubscriptionActivity.this, "Can't update subscriptions", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.dismiss();
                            Toast.makeText(SelectSubscriptionActivity.this, "Can't update subscriptions", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}
