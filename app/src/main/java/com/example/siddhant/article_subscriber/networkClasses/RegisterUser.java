package com.example.siddhant.article_subscriber.networkClasses;

import java.security.MessageDigest;
import java.util.Formatter;

/**
 * Created by siddhant on 31/1/16.
 */
public class RegisterUser {

    public String name;
    public String email;
    public String password;
    public String regid;

    public RegisterUser(String name, String email, String password, String regid) {
        this.name = name;
        this.email = email;
        this.regid = regid;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes());
            this.password = toHexString(digest);
        } catch (Exception ignored) {
            this.password = "";
        }
    }

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

}
