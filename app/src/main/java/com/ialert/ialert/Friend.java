package com.ialert.ialert;

/**
 * Created by aashimagarg on 10/16/16.
 */

public class Friend {
    private String uid;
    private String name;
    private String imageUrl;

    public Friend() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Friend(String uid, String name, String imageUrl){
        this.uid = uid;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public Friend(String uid, String name){
        this.uid = uid;
        this.name = name;
        this.imageUrl = "http://emblemsbattlefield.com/uploads/posts/2014/10/facebook-default-photo-male_1.jpg";
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
