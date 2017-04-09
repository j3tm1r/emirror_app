package it.droidconit.emirror;

import com.google.gson.annotations.SerializedName;

public class GoogleToken {
    @SerializedName("authCode")
    String authCode;
    
    public GoogleToken(String authToken) {
        authCode = authToken;
    }

}
