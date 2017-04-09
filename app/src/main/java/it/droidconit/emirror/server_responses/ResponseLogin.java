package it.droidconit.emirror.server_responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jetmir on 09/04/2017.
 */

public class ResponseLogin {
    @SerializedName("sessionId")
    String sessionId;

    public String getSessionId() {
        return sessionId;
    }
}
