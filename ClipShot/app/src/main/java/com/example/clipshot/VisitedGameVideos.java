package com.example.clipshot;

// Declaring Variables which will recieve DB data
public class VisitedGameVideos {
    private String Description;
    private String Url;
    private String GameName;
    private String UserID;
    private String Likes;
    private String DocumentName;
    private String Email;

    public VisitedGameVideos() {
    }

    // Gives other variables the data
    public VisitedGameVideos(String description, String url, String gameName, String userID, String likes, String documentName , String email) {

        Description = description;
        Url = url;
        GameName = gameName;
        UserID = userID;
        Likes = likes;
        DocumentName = documentName;
        Email=email;
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

    public String getEmail() {
        return Email;
    }
}