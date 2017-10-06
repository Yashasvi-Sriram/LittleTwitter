package org.littletwitter.littletwitter.beans;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Comment {
    private String uid;
    private String name;
    private String text;
    private String timestamp;

    public Comment(JSONObject jsonObject) throws JSONException {
        this.uid = jsonObject.getString("uid");
        this.name = jsonObject.getString("name");
        this.text = jsonObject.getString("text");
        this.timestamp = jsonObject.getString("timestamp");
    }

    @Override
    public String toString() {
        return this.uid + " " + this.text + " " + this.timestamp;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject ans = new JSONObject();
        ans.put("uid", this.uid);
        ans.put("text", this.text);
        ans.put("timestamp", this.timestamp);
        return ans;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
