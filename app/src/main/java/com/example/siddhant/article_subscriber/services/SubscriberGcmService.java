package com.example.siddhant.article_subscriber.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.example.siddhant.article_subscriber.Constants;
import com.example.siddhant.article_subscriber.DummyActivity;
import com.example.siddhant.article_subscriber.R;
import com.example.siddhant.article_subscriber.activities.ContentActivity;
import com.example.siddhant.article_subscriber.networkClasses.Article;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

/**
 * Created by siddhant on 1/2/16.
 */
public class SubscriberGcmService extends IntentService {


    public SubscriberGcmService() {
        super("SubscriberGcmService");
    }

    SharedPreferences sf;

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Bundle extras = intent.getExtras();
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            sf = getSharedPreferences(Constants.UserInfoSharedPref, MODE_PRIVATE);
            String messageType = gcm.getMessageType(intent);

            if (!extras.isEmpty()) {
                if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
//                    String data = extras.getString("data");
                    Article article = new Article(
                            Integer.parseInt(extras.getString("id")),
                            extras.getString("name"),
                            extras.getString("email"),
                            extras.getString("title"),
                            Integer.parseInt(extras.getString("category")),
                            Long.parseLong(extras.getString("timestamp")),
                            extras.getInt("publisher")
                    );
//                    Article article =
//                            new Gson().fromJson(data, Article.class);
                    if (sf.getBoolean(Constants.SignInDone, false)) {
                        sendNotification(article);
                    }
                }
            }
            GcmBroadcastReceiver.completeWakefulIntent(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendNotification(Article article) {
        try {
            Intent resultIntent = new Intent(this, ContentActivity.class);
            resultIntent.putExtra("name", article.name);
            resultIntent.putExtra("email", article.email);
            resultIntent.putExtra("id", article.id);
            resultIntent.putExtra("categoryId", article.category);
            resultIntent.putExtra("timestamp", article.timestamp);
            resultIntent.putExtra("title", article.title);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                    resultIntent, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder mNotifyBuilder;
            NotificationManager mNotificationManager;

            Bitmap largeImage = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mNotifyBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle("An article just got published")
                    .setContentText("\"" + article.title + "\" by " + article.name)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(largeImage);
            mNotifyBuilder.setContentIntent(resultPendingIntent);

            int defaults = 0;
            defaults = defaults | Notification.DEFAULT_LIGHTS;
            defaults = defaults | Notification.DEFAULT_VIBRATE;
            defaults = defaults | Notification.DEFAULT_SOUND;

            mNotifyBuilder.setDefaults(defaults);
            mNotifyBuilder.setAutoCancel(true);
            mNotificationManager.notify(4321, mNotifyBuilder.build());
        } catch (Exception ignored) {

        }
    }
}
