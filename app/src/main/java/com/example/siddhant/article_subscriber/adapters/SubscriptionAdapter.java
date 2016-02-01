package com.example.siddhant.article_subscriber.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.siddhant.article_subscriber.Globals;
import com.example.siddhant.article_subscriber.R;
import com.example.siddhant.article_subscriber.networkClasses.Categories;
import com.example.siddhant.article_subscriber.networkClasses.GetSubscriptionsResponse;

import java.util.ArrayList;

/**
 * Created by siddhant on 1/2/16.
 */
public class SubscriptionAdapter extends ArrayAdapter<Categories> {

    private final Context context;
    public GetSubscriptionsResponse mySubscriptions;

    public SubscriptionAdapter(Context context, ArrayList<Categories> categories, GetSubscriptionsResponse mySubscriptions) {
        super(context, R.layout.subscription_row, categories);
        this.context = context;
        this.mySubscriptions = mySubscriptions;
    }


    public View getView(final int position, View convertView, ViewGroup parent) {
        final Categories singleContact = getItem(position);
        final ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.subscription_row, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.categoryTextView = (TextView) convertView.findViewById(R.id.categoryTextView);
            viewHolder.outerRelativeLayout = (RelativeLayout) convertView.findViewById(R.id.outerRelativeLayout);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            if (!singleContact.animationStates) {
                singleContact.animationStates = true;
                Animation an = AnimationUtils.loadAnimation(context, R.anim.fade_in);
                an.setStartOffset(position * 50);
                convertView.startAnimation(an);
            }
            viewHolder.categoryTextView.setTypeface(Globals.typeface);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (mySubscriptions.data.contains(singleContact.id)) {
            viewHolder.checkBox.setChecked(true);
        } else {
            viewHolder.checkBox.setChecked(false);
        }
        viewHolder.categoryTextView.setText(singleContact.name);

        viewHolder.outerRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!viewHolder.checkBox.isChecked()) {
                    viewHolder.checkBox.setChecked(true);
                    mySubscriptions.data.add(position + 1);
                } else {
                    viewHolder.checkBox.setChecked(false);
                    mySubscriptions.data.remove((Integer) (position + 1));
                }
            }
        });
        return convertView;
    }

    class ViewHolder {
        public TextView categoryTextView;
        public CheckBox checkBox;
        RelativeLayout outerRelativeLayout;
    }

}
