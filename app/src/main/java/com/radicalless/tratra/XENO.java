package com.radicalless.tratra;

import org.json.JSONException;
import org.json.JSONObject;

public class XENO {

    public static JSONObject stringToJSON(String jsonString) {
        JSONObject responseJSON = null;
        try {
            responseJSON = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return responseJSON;
    }

}
