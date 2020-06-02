package com.example.clipshot;

import android.net.Uri;

public class ProfileVideos {

    private String Description;
    private String Url;
    private String GameName;
    private String UserID;

    public ProfileVideos() {
    }



    public ProfileVideos(String description, String url, String gameName, String userId) {

        this.Description = description;
        this.Url=url;
        this.GameName=gameName;
        this.UserID=userId;
    }

    public String getDescription() {
        return Description;
    }

    public String getUrl() {
        return Url;
    }

    public String getGameName() {
        return GameName;
    }

    public String getUserID() {
        return UserID;
    }
}
