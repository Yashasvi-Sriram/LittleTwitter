package org.littletwitter.littletwitter.responses;

import org.json.JSONException;
import org.json.JSONObject;

public class ObjectServerResponse extends ServerResponse {
    private JSONObject data = null;

    // not a pure data class and
    // json keys are hardcoded
    // bad for long term use
    public ObjectServerResponse(String seed) throws JSONException {
        JSONObject seedObject = new JSONObject(seed);
        status = seedObject.getBoolean("status");
        if (!status) {
            errorMessage = seedObject.getString("status");
        } else {
            data = seedObject.getJSONObject("data");
        }
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }
}
