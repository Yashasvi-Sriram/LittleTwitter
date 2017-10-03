package org.littletwitter.littletwitter.entities;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    public String uid;
    public String name;
    public String email;

    public User(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
    }

    @Override
    public String toString() {
        return this.uid + " | " + this.name + " | " + this.email;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject ans = new JSONObject();
        ans.put("uid", this.uid);
        ans.put("name", this.name);
        ans.put("email", this.email);
        return ans;
    }
}
