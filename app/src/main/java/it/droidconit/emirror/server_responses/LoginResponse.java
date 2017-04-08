package it.droidconit.emirror.server_responses;

/**
 * Created by Jetmir on 08/04/2017.
 */

public class LoginResponse {
    private String access_token;
    private String token_type;
    private String refresh_token;
    private long expires_in;
    private String scope;

    public String getToken_type() {
        return token_type;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public long getExpires_in() {
        return expires_in;
    }

    public String getScope() {
        return scope;
    }

    public String getAccess_token() {
        return "Bearer " + access_token;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
