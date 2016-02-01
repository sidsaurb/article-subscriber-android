package com.example.siddhant.article_subscriber.networkClasses;

/**
 * Created by siddhant on 1/2/16.
 */
public class Article {
    public int id;
    public String name;
    public String email;
    public String title;
    public int category;
    public long timestamp;
    public int publisher;

    public boolean animationState;

    public Article(int id, String name, String email, String title, int category, long timestamp, int publisher) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.title = title;
        this.category = category;
        this.timestamp = timestamp;
        this.publisher = publisher;
    }
}
