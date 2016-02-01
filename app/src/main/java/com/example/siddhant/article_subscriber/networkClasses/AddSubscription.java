package com.example.siddhant.article_subscriber.networkClasses;

import java.util.ArrayList;

/**
 * Created by siddhant on 1/2/16.
 */
public class AddSubscription {
    public int id;
    public ArrayList<Integer> categories;

    public AddSubscription(int id, ArrayList<Integer> categories) {
        this.id = id;
        this.categories = categories;
    }
}
