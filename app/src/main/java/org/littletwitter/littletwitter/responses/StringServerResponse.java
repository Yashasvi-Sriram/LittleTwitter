package org.littletwitter.littletwitter.responses;

import org.json.JSONException;
import org.json.JSONObject;

public class StringServerResponse extends ServerResponse {
    private String data = null;

    // not a pure data class and
    // json keys are hardcoded
    // bad for long term use
    public StringServerResponse(String seed) throws JSONException {
        JSONObject seedObject = new JSONObject(seed);
        status = seedObject.getBoolean("status");
        if (!status) {
            errorMessage = seedObject.getString("status");
        } else {
            data = seedObject.getString("data");
        }
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
