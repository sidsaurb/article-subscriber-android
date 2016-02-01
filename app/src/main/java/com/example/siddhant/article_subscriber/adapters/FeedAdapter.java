package com.example.siddhant.article_subscriber.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.siddhant.article_subscriber.Globals;
import com.example.siddhant.article_subscriber.R;
import com.example.siddhant.article_subscriber.activities.ContentActivity;
import com.example.siddhant.article_subscriber.networkClasses.Article;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by siddhant on 1/2/16.
 */
public class FeedAdapter extends ArrayAdapter<Article> {

    private final Context context;

    public FeedAdapter(Context context, ArrayList<Article> chains) {
        super(context, R.layout.feed_row, chains);
        this.context = context;
    }


    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        final Article singleChain = getItem(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.feed_row, parent, false);
            viewHolder.titleTextView = (TextView) convertView.findViewById(R.id.titleTextView);
            viewHolder.detailsTextView = (TextView) convertView.findViewById(R.id.detailsTextView);
            viewHolder.outerLinerLayout = (LinearLayout) convertView.findViewById(R.id.outerLinerLayout);
            viewHolder.titleTextView.setTypeface(Globals.typeface);
            viewHolder.detailsTextView.setTypeface(Globals.typeface);

            if (!singleChain.animationState) {
                singleChain.animationState = true;
                Animation an = AnimationUtils.loadAnimation(context, R.anim.fade_in);
                an.setStartOffset(position * 200);
                convertView.startAnimation(an);
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.titleTextView.setText(singleChain.title);
        String details = "by " + singleChain.name + ", ";
        DateFormat df1 = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault());

        Date date2 = new Date(singleChain.timestamp);
        try {
            details += df1.format(date2);
        } catch (Exception ignored) {
        }

        viewHolder.detailsTextView.setText(details);
        viewHolder.outerLinerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ContentActivity.class);
                i.putExtra("name", singleChain.name);
                i.putExtra("email", singleChain.email);
                i.putExtra("id", singleChain.id);
                i.putExtra("categoryId", singleChain.category);
                i.putExtra("timestamp", singleChain.timestamp);
                i.putExtra("title", singleChain.title);
                context.startActivity(i);
            }
        });
        return convertView;
    }

    static class ViewHolder {
        TextView titleTextView, detailsTextView;
        LinearLayout outerLinerLayout;
    }
}
