package it.droidconit.emirror;


import it.droidconit.emirror.server_responses.Device;
import it.droidconit.emirror.server_responses.RegisterResponse;
import it.droidconit.emirror.server_responses.ResponseLogin;
import it.droidconit.emirror.server_responses.SpotifyConnectResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SpotifyService {
    String BASE_URL = "http://hackathonit6301.cloudapp.net";

    @POST("login/google")
    Call<ResponseLogin> doLogin(@Body GoogleToken token);

    @POST("/api/v1/r/{radioId}/user")
    Call<RegisterResponse> registerDevice(@Path("radioId") String radioId, @Body Device device);
    

//    @POST("/oauth/token")
//    Call<LoginResponse> doLogin(@Header("Authorization") String authorizationHeader,
//                                @Query("username") String username, @Query("password") String password,
//                                @Query("grant_type") String grant_type, @Query("scope") String scope);

    @POST("/connect/facebook")
    Call<SpotifyConnectResponse> connectFacebook(@Header("Authorization") String authorization);

}
