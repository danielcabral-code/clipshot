package com.example.clipshot;

import android.net.Uri;

public class ProfileVideos {

    private String Description;
    private String Url;
    private String GameName;

    public ProfileVideos() {
    }



    public ProfileVideos(String description, String url, String gameName) {

        this.Description = description;
        this.Url=url;
        this.GameName=gameName;
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

}
