package com.example.clipshot;

// Declaring Variables which will recieve DB data
public class ProfileVideos {

    private String Description;
    private String Url;
    private String GameName;
    private String UserID;
    private String Likes;
    private String DocumentName;

    public ProfileVideos() {
    }

    // Gives other variables the data
    public ProfileVideos(String description, String url, String gameName, String userId, String likes, String documentName) {

        this.Description = description;
        this.Url=url;
        this.GameName=gameName;
        this.UserID=userId;
        this.Likes=likes;
        this.DocumentName=documentName;
    }

    // Will be used to fill in data in RecyclerView
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

    public String getLikes() {
        return Likes;
    }

    public String getDocumentName() {
        return DocumentName;
    }
}