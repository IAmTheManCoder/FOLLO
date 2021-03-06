package com.example.follo;

// This class has all of the getters and setters for the FindFriendActivity RecyclerAdapter
public class FindFriends {

    public String profileimage, fullname, status;

    public FindFriends(String profileimage, String fullname, String status) {
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.status = status;
    }

    public FindFriends(){
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFullname() {
        return fullname;
    }

    public String getStatus() {
        return status;
    }
}
