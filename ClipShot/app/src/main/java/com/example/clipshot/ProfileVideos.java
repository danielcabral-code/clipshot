package com.example.clipshot;

import android.net.Uri;

public class ProfileVideos {

    private String Description;
    private String Url;

    public ProfileVideos() {
    }



    public ProfileVideos(String description, String url) {

        this.Description = description;
        this.Url=url;
    }

    public String getDescription() {
        return Description;
    }

    public String getUrl() {
        return Url;
    }

}
