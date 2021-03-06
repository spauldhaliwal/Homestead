package com.spauldhaliwal.homestead;

import java.io.Serializable;

/**
 * Created by pauldhaliwal on 2018-03-07.
 */

class UserModel implements Serializable {

    private String uid;
    private String tokenId;
    private String name;
    private String email;
    private String profileImage;
    private String homesteadId;
    private String homesteadName;
    private String jobs;
    private String notifications;

    public UserModel() {}

    public UserModel(String uid, String tokenId, String name, String email, String profileImage, String homesteadId, String homesteadName, String jobs, String notifications) {
        this.uid = uid;
        this.tokenId = tokenId;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.homesteadId = homesteadId;
        this.homesteadName = homesteadName;
        this.jobs = jobs;
        this.notifications = notifications;
    }

    public UserModel(String uid, String tokenId, String name, String email, String profileImage, String jobs, String notifications) {
        this.uid = uid;
        this.tokenId = tokenId;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.jobs = jobs;
        this.notifications = notifications;
    }

    public String getUid() {
        return uid;
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getHomesteadId() {
        return homesteadId;
    }

    public String getHomesteadName() {
        return homesteadName;
    }

    public String getJobs() {
        return jobs;
    }

    public String getNotifications() {
        return notifications;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "uid='" + uid + '\'' +
                ", tokenId='" + tokenId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", profileImage='" + profileImage + '\'' +
                ", homesteadId='" + homesteadId + '\'' +
                ", homesteadName='" + homesteadName + '\'' +
                ", jobs='" + jobs + '\'' +
                ", notifications='" + notifications + '\'' +
                '}';
    }
}
