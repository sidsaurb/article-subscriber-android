package com.example.siddhant.article_subscriber.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.siddhant.article_subscriber.Constants;
import com.example.siddhant.article_subscriber.adapters.FeedAdapter;
import com.example.siddhant.article_subscriber.Globals;
import com.example.siddhant.article_subscriber.HelperMethods;
import com.example.siddhant.article_subscriber.R;
import com.example.siddhant.article_subscriber.TypefaceSpan;
import com.example.siddhant.article_subscriber.networkClasses.Article;
import com.example.siddhant.article_subscriber.networkClasses.GetArticles;
import com.example.siddhant.article_subscriber.networkClasses.GetArticlesResponse;
import com.example.siddhant.article_subscriber.networkClasses.UpdateRegistrationId;
import com.example.siddhant.article_subscriber.networkClasses.UpdateRegistrationIdResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FeedActivity extends AppCompatActivity {

    //    private SweetAlertDialog progress;
    private GoogleCloudMessaging gcm;
    private String regid;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    SharedPreferences sf;
    ListView feedListView;
    RelativeLayout refreshingFeedRelativeLayout, problemRelativeLayout;
    SwipeRefreshLayout myRefreshLayout;
    ArrayList<Article> currentArticles;
    FeedAdapter myFeedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            SpannableString s = new SpannableString("Feeds");
            s.setSpan(new TypefaceSpan(Globals.typeface), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            actionBar.setTitle(s);
        }

//        progress = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
//        progress.setTitleText("Processing");
//        progress.getProgressHelper().setBarColor(R.color.light_red);
//        progress.setCancelable(false);

        ((TextView) findViewById(R.id.textView)).setTypeface(Globals.typeface);
        ((TextView) findViewById(R.id.tryAgainTextView)).setTypeface(Globals.typeface);

        feedListView = (ListView) findViewById(R.id.feedListView);
        refreshingFeedRelativeLayout = (RelativeLayout) findViewById(R.id.refreshingFeedRelativeLayout);
        problemRelativeLayout = (RelativeLayout) findViewById(R.id.problemRelativeLayout);


        sf = getSharedPreferences(Constants.UserInfoSharedPref, MODE_PRIVATE);
        if (sf.getBoolean(Constants.IsReLogin, false)) {
            updateRegistrationId();
        }

        if (sf.getString(Constants.Categories, "").equals("")) {
            saveCategories();
        }

        LoadListView();

        problemRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadListView();
            }
        });

        myRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        myRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
        myRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoadListView();
            }
        });
    }

    private void LoadListView() {
        refreshingFeedRelativeLayout.setVisibility(View.VISIBLE);
        problemRelativeLayout.setVisibility(View.GONE);
        feedListView.setVisibility(View.GONE);
        feedListView.setAdapter(null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                GetArticles myReq = new GetArticles(sf.getInt(Constants.UserId, -1));
                final String response = HelperMethods.makePostRequest(FeedActivity.this, Constants.GET_ARTICLES, myReq);
                if (!response.equals("")) {
                    final GetArticlesResponse myResponse = new Gson().fromJson(response, GetArticlesResponse.class);
                    if (myResponse.success) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                feedListView.setVisibility(View.VISIBLE);
                                refreshingFeedRelativeLayout.setVisibility(View.GONE);
                                problemRelativeLayout.setVisibility(View.GONE);
                                currentArticles = myResponse.data;
                                myFeedAdapter = new FeedAdapter(FeedActivity.this, currentArticles);
                                feedListView.setAdapter(myFeedAdapter);
                                if (currentArticles.size() == 0) {
                                    Toast.makeText(FeedActivity.this, "No articles found. Please subscribe to some more categories", Toast.LENGTH_SHORT).show();
                                }
                                myRefreshLayout.setRefreshing(false);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                feedListView.setVisibility(View.GONE);
                                refreshingFeedRelativeLayout.setVisibility(View.GONE);
                                problemRelativeLayout.setVisibility(View.VISIBLE);
                                myRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            feedListView.setVisibility(View.GONE);
                            refreshingFeedRelativeLayout.setVisibility(View.GONE);
                            problemRelativeLayout.setVisibility(View.VISIBLE);
                            myRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        }).start();
    }

    private void saveCategories() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HelperMethods.getAndSaveCategories(FeedActivity.this);
            }
        }).start();
    }

    private void updateRegistrationId() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (checkPlayServices()) {
                    gcm = GoogleCloudMessaging.getInstance(FeedActivity.this);
                    regid = getRegistrationId(FeedActivity.this);
                    if (regid.isEmpty()) {
                        boolean result = registerInBackground();
                        if (result) {
                            UpdateRegistrationIdOnBackend();
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(FeedActivity.this, "Can't get gcm registration id", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        UpdateRegistrationIdOnBackend();
                    }
                }
            }
        }).start();
    }

    private boolean checkPlayServices() {
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GooglePlayServicesUtil.getErrorDialog(resultCode, FeedActivity.this,
                                PLAY_SERVICES_RESOLUTION_REQUEST).show();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FeedActivity.this, "This device is not supported for gcm", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getSharedPreferences(Constants.UserInfoSharedPref, Context.MODE_PRIVATE);
        String registrationId = prefs.getString(Constants.LastPushUri, "");
        if (registrationId.isEmpty()) {
            return "";
        }
        int registeredVersion = prefs.getInt(Constants.AppVersion, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            return "";
        }
        return registrationId;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    private boolean registerInBackground() {
        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(FeedActivity.this);
            }
            String SENDER_ID = "592627529206";
            regid = gcm.register(SENDER_ID);
            storeRegistrationId(FeedActivity.this, regid);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getSharedPreferences(Constants.UserInfoSharedPref, Context.MODE_PRIVATE);
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.LastPushUri, regId);
        editor.putInt(Constants.AppVersion, appVersion);
        editor.apply();
    }

    private void UpdateRegistrationIdOnBackend() {
        UpdateRegistrationId myInfo = new UpdateRegistrationId(sf.getInt(Constants.UserId, -2), regid);
        String response = HelperMethods.makePostRequest(FeedActivity.this.getApplicationContext(), Constants.UPDATE_REGID, myInfo);
        if (!response.equals("")) {
            UpdateRegistrationIdResponse myResponse = new Gson().fromJson(response, UpdateRegistrationIdResponse.class);
            if (!myResponse.success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FeedActivity.this, "Can't update gcm registration id at backend", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FeedActivity.this, "Updated gcm registration id at backend", Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = sf.edit();
                        editor.putBoolean(Constants.IsReLogin, false);
                        editor.apply();
                    }
                });
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(FeedActivity.this, "Can't update gcm registration id at backend", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_feed, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select:
                startActivity(new Intent(FeedActivity.this, SelectSubscriptionActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {
            String title = i.getStringExtra("title");
            String email = i.getStringExtra("email");
            String name = i.getStringExtra("name");
            Long timestamp = i.getLongExtra("timestamp", 0);
            Integer id = i.getIntExtra("id", 0);
            Integer categoryId = i.getIntExtra("categoryId", 0);
            Article article = new Article(id, name, email, title, categoryId, timestamp, 0);
            currentArticles.add(0, article);
            myFeedAdapter.notifyDataSetChanged();
            SharedPreferences.Editor editor = sf.edit();
            editor.putBoolean(Constants.ReloadFeeds, false);
            editor.commit();
        }
    };

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("notification_received"));
        if (sf.getBoolean(Constants.ReloadFeeds, false)) {
            LoadListView();
            SharedPreferences.Editor editor = sf.edit();
            editor.putBoolean(Constants.ReloadFeeds, false);
            editor.apply();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }
}
