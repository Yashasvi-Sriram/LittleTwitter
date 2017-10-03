package org.littletwitter.littletwitter.entities;

import org.json.JSONException;
import org.json.JSONObject;

public class Comment {
    public int commentid;
    public int postid;
    public String uid;
    public String text;
    public String timestamp;
    public String uname;

    public Comment(int commentid, int postid, String uid, String text, String timestamp, String uname) {
        this.commentid = commentid;
        this.postid = postid;
        this.uid = uid;
        this.text = text;
        this.timestamp = timestamp;
        this.uname = uname;
    }

    @Override
    public String toString() {
        return this.commentid + " " + this.postid + " " + this.uid + " " + this.text + " " + this.timestamp;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject ans = new JSONObject();
        ans.put("commentid", this.commentid);
        ans.put("postid", this.postid);
        ans.put("uid", this.uid);
        ans.put("text", this.text);
        ans.put("timestamp", this.timestamp);
        ans.put("uname", this.uname);
        return ans;
    }
}
