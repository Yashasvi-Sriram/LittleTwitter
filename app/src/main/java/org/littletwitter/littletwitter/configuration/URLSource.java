package org.littletwitter.littletwitter.configuration;

import okhttp3.Cookie;

public class URLSource {
    public static final String IP_ADDRESS = "http://192.168.0.5:8080/";

    public static String login() {
        return IP_ADDRESS + "Login";
    }

    public static String logout() {
        return IP_ADDRESS + "LogoutServlet";
    }

    public static String seePosts() {
        return IP_ADDRESS + "SeePosts";
    }

    public static String addComment() {
        return IP_ADDRESS + "NewComment";
    }

    public static String addPost() {
        return IP_ADDRESS + "CreatePost";
    }

    public static String search() {
        return IP_ADDRESS + "SearchUser";
    }

    public static String seeMyPosts() {
        return IP_ADDRESS + "SeeMyPosts";
    }

    public static String seeUserPosts() {
        return IP_ADDRESS + "SeeUserPosts";
    }

    public static String getFollowers() {
        return IP_ADDRESS + "UserFollow";
    }

    public static String followUser() {
        return IP_ADDRESS + "Follow";
    }

    public static String unFollowUser() {
        return IP_ADDRESS + "Unfollow";
    }
}
