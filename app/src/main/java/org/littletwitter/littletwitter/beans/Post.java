package org.littletwitter.littletwitter.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Post {
    private int postId;
    private String uid;
    private String image;
    private String text;
    private String timestamp;
    private List<Comment> comments = new ArrayList<>();

    public Post(JSONObject jsonObject) throws JSONException {
        this.postId = jsonObject.getInt("postid");
        this.uid = jsonObject.getString("uid");
        this.text = jsonObject.getString("text");
        this.image = jsonObject.getString("image");
        this.timestamp = jsonObject.getString("timestamp");
        JSONArray jComments = jsonObject.getJSONArray("Comment");
        List<Comment> comments = new ArrayList<>();
        for (int i = 0; i < jComments.length(); i++) {
            comments.add(new Comment(jComments.getJSONObject(i)));
        }
        this.comments = comments;
    }

    @Override
    public String toString() {
        return this.postId + " " + this.uid + " " + this.text + " " + this.timestamp + this.comments;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
