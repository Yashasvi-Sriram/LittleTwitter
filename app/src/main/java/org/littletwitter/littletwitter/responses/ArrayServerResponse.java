package org.littletwitter.littletwitter.responses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ArrayServerResponse extends ServerResponse {
    private JSONArray data = null;

    // not a pure data class and
    // json keys are hardcoded
    // bad for long term use
    public ArrayServerResponse(String seed) throws JSONException {
        JSONObject seedObject = new JSONObject(seed);
        status = seedObject.getBoolean("status");
        if (!status) {
            errorMessage = seedObject.getString("message");
        } else {
            data = seedObject.getJSONArray("data");
        }
    }

    public JSONArray getData() {
        return data;
    }

    public void setData(JSONArray data) {
        this.data = data;
    }
}
