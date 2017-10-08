package org.littletwitter.littletwitter.configuration;

public class URLSource {
    private static final String IP_ADDRESS = "http://192.168.0.5:8080";

    public static String login() {
        return IP_ADDRESS + "/Login";
    }

    public static String logout() {
        return IP_ADDRESS + "/LogoutServlet";
    }

    public static String seePosts() {
        return IP_ADDRESS + "/SeePosts";
    }

    public static String addComment() {
        return IP_ADDRESS + "/NewComment";
    }
}
