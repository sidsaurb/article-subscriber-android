package com.example.siddhant.article_subscriber.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.siddhant.article_subscriber.Constants;
import com.example.siddhant.article_subscriber.Globals;
import com.example.siddhant.article_subscriber.HelperMethods;
import com.example.siddhant.article_subscriber.R;
import com.example.siddhant.article_subscriber.TypefaceSpan;
import com.example.siddhant.article_subscriber.networkClasses.LoginUser;
import com.example.siddhant.article_subscriber.networkClasses.LoginUserResponse;
import com.example.siddhant.article_subscriber.networkClasses.RegisterUser;
import com.example.siddhant.article_subscriber.networkClasses.RegisterUserResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import cn.pedant.SweetAlert.SweetAlertDialog;
import info.hoang8f.widget.FButton;

public class RegistrationActivity extends AppCompatActivity {

    private EditText emailEditText, emailEditText1, nameEditText, passwordEditText, passwordEditText1;
    private GoogleCloudMessaging gcm;
    private String regid;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private SweetAlertDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            SpannableString s = new SpannableString("Sign in..");
            s.setSpan(new TypefaceSpan(Globals.typeface), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            actionBar.setTitle(s);
        }

        progress = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progress.setTitleText("Processing");
        progress.getProgressHelper().setBarColor(R.color.light_red);
        progress.setCancelable(false);

        ((FButton) findViewById(R.id.registerButton)).setTypeface(Globals.typeface);
        ((FButton) findViewById(R.id.loginButton)).setTypeface(Globals.typeface);
        ((TextView) findViewById(R.id.loginTextView)).setTypeface(Globals.typeface);
        ((TextInputLayout) findViewById(R.id.nameLayout)).setTypeface(Globals.typeface);
        ((TextInputLayout) findViewById(R.id.emailLayout)).setTypeface(Globals.typeface);
        ((TextInputLayout) findViewById(R.id.emailLayout1)).setTypeface(Globals.typeface);
        ((TextInputLayout) findViewById(R.id.passwordLayout)).setTypeface(Globals.typeface);
        ((TextInputLayout) findViewById(R.id.passwordLayout1)).setTypeface(Globals.typeface);

        emailEditText = (EditText) findViewById(R.id.emailEditText);
        emailEditText1 = (EditText) findViewById(R.id.emailEditText1);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        passwordEditText1 = (EditText) findViewById(R.id.passwordEditText1);

        emailEditText.setTypeface(Globals.typeface);
        emailEditText1.setTypeface(Globals.typeface);
        passwordEditText.setTypeface(Globals.typeface);
        passwordEditText1.setTypeface(Globals.typeface);
        nameEditText.setTypeface(Globals.typeface);

        FButton registerButton = (FButton) findViewById(R.id.registerButton);
        FButton loginButton = (FButton) findViewById(R.id.loginButton);

        registerButton.setOnClickListener(registerListener);
        loginButton.setOnClickListener(loginListener);
    }

    private View.OnClickListener registerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!nameEditText.getText().toString().isEmpty()
                    && !emailEditText.getText().toString().isEmpty()
                    && !passwordEditText.toString().isEmpty()) {
                progress.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (checkPlayServices()) {
                            gcm = GoogleCloudMessaging.getInstance(RegistrationActivity.this);
                            regid = getRegistrationId(RegistrationActivity.this);
                            if (regid.isEmpty()) {
                                boolean result = registerInBackground();
                                if (result) {
                                    SendRegistrationInfoToBackend();
                                } else {
                                    showDialogAndExit();
                                }
                            } else {
                                SendRegistrationInfoToBackend();
                            }
                        }
                    }
                }).start();
            } else {
                Toast.makeText(RegistrationActivity.this, "Please fill out every field", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!emailEditText1.getText().toString().isEmpty()
                    && !passwordEditText1.getText().toString().isEmpty()) {
                progress.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LoginUser myReq = new LoginUser(emailEditText1.getText().toString(), passwordEditText1.getText().toString());
                        String response = HelperMethods.makePostRequest(RegistrationActivity.this, Constants.LOGIN, myReq);
                        if (!response.isEmpty()) {
                            LoginUserResponse myResponse = new Gson().fromJson(response, LoginUserResponse.class);
                            if (myResponse.success) {
                                SharedPreferences sf = getSharedPreferences(Constants.UserInfoSharedPref, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sf.edit();
                                editor.putBoolean(Constants.SignInDone, true);
                                editor.putString(Constants.UserName, myResponse.name);
                                editor.putString(Constants.UserEmail, emailEditText1.getText().toString());
                                editor.putString(Constants.UserPassword, passwordEditText1.getText().toString());
                                editor.putInt(Constants.UserId, myResponse.id);
                                editor.apply();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.dismiss();
                                        startActivity(new Intent(RegistrationActivity.this, FeedActivity.class));
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.dismiss();
                                        Toast.makeText(RegistrationActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progress.dismiss();
                                    Toast.makeText(RegistrationActivity.this, "There is some problem in reaching servers", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            } else {
                Toast.makeText(RegistrationActivity.this, "Please fill out every field", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private boolean checkPlayServices() {
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GooglePlayServicesUtil.getErrorDialog(resultCode, RegistrationActivity.this,
                                PLAY_SERVICES_RESOLUTION_REQUEST).show();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showNotSupportedDialogAndExit();
                    }
                });
            }
            return false;
        }
        return true;
    }

    private void showNotSupportedDialogAndExit() {
        runOnUiThread(new Runnable() {
            public void run() {
                new SweetAlertDialog(RegistrationActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops..")
                        .setContentText("Your device is not supported")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                System.exit(0);
                            }
                        })
                        .show();
            }
        });
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
                gcm = GoogleCloudMessaging.getInstance(RegistrationActivity.this);
            }
            String SENDER_ID = "592627529206";
            regid = gcm.register(SENDER_ID);
            storeRegistrationId(RegistrationActivity.this, regid);
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

    private void SendRegistrationInfoToBackend() {
        RegisterUser myInfo = new RegisterUser(nameEditText.getText().toString(),
                emailEditText.getText().toString(),
                passwordEditText.getText().toString(),
                regid);
        String response = HelperMethods.makePostRequest(RegistrationActivity.this.getApplicationContext(), Constants.ADD_SUBSCRIBER, myInfo);
        if (!response.equals("")) {
            RegisterUserResponse myResponse = new Gson().fromJson(response, RegisterUserResponse.class);
            if (myResponse.success) {
                SharedPreferences sf = getSharedPreferences(Constants.UserInfoSharedPref, MODE_PRIVATE);
                SharedPreferences.Editor editor = sf.edit();
                editor.putBoolean(Constants.SignInDone, true);
                editor.putString(Constants.UserName, nameEditText.getText().toString());
                editor.putString(Constants.UserEmail, emailEditText.getText().toString());
                editor.putString(Constants.UserPassword, passwordEditText.getText().toString());
                editor.putInt(Constants.UserId, myResponse.id);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        startActivity(new Intent(RegistrationActivity.this, FeedActivity.class));
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.dismiss();
                        Toast.makeText(RegistrationActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            showDialogAndExit();
        }
    }

    private void showDialogAndExit() {
        runOnUiThread(new Runnable() {
            public void run() {
                progress.dismiss();
                new SweetAlertDialog(RegistrationActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops..")
                        .setContentText("Can't connect to servers")
                        .show();
            }
        });
    }
}
