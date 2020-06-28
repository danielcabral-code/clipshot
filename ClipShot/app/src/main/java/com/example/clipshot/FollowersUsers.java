package com.example.clipshot;

// Declaring Variables which will recieve DB data
public class FollowersUsers {

    String Username;
    String UserUID;
    String Email;
    String Following;
    String Followers;

    public FollowersUsers() {
    }

    // Gives other variables the data
    public FollowersUsers(String username, String userUID, String email, String following, String followers) {

        this.Username = username;
        this.UserUID = userUID;
        this.Email =email;
        this.Following = following;
        this.Followers = followers;
    }

    // Will be used to fill in data in RecyclerView
    public String getUsername() {
        return Username;
    }

    public String getUserUID() {
        return UserUID;
    }

    public String getEmail() {
        return Email;
    }

    public String getFollowing() {
        return Following;
    }

    public String getFollowers() {
        return Followers;
    }
}