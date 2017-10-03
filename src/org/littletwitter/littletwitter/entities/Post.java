package org.littletwitter.littletwitter.entities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Post {
    public int postid;
    public String uid;
    public String text;
    public String timestamp;
    public List<Comment> comments = new ArrayList<>();
    public String uname;

    public Post(int postid, String uid, String text, String timestamp, String uname, List<Comment> comments) {
        this.postid = postid;
        this.uid = uid;
        this.text = text;
        this.timestamp = timestamp;
        this.uname = uname;
        this.comments = comments;
    }

    @Override
    public String toString() {
        return this.postid + " " + this.uid + " " + this.text + " " + this.timestamp + this.comments;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject ans = new JSONObject();
        ans.put("postid", this.postid);
        ans.put("uid", this.uid);
        ans.put("text", this.text);
        ans.put("timestamp", this.timestamp);
        ans.put("uname", this.uname);
        JSONArray jComments = new JSONArray();
        for (Comment comment : this.comments) {
            jComments.put(comment.toJSONObject());
        }
        ans.put("comments", jComments);

        return ans;
    }

}
