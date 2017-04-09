package it.droidconit.emirror;


import android.util.ArrayMap;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import it.droidconit.emirror.server_responses.LoginResponse;
import it.droidconit.emirror.server_responses.ResponseLogin;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginSpotify {
    private String TAG = this.getClass().getSimpleName();
    private LoginResponse loginResponse;
    private SpotifyService spotifyService;
    private WebView mWebView;
    private OkHttpClient mClient;
    private Retrofit mRetrofit;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private String jSessionId;

    private LoginListener mLoginListener;

    public interface LoginListener {
        void onLoginSucceded(String jsessionid);

        void onLoginError();
    }

    public LoginSpotify(WebView webView, LoginListener loginListener) {
        mClient = new OkHttpClient();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(SpotifyService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        spotifyService = mRetrofit.create(SpotifyService.class);
        mWebView = webView;
        mLoginListener = loginListener;
    }


    public void performLogin(String authToken) {
        Log.d(TAG, authToken);
        Call<ResponseLogin> call = spotifyService.doLogin(new GoogleToken(authToken));
        call.enqueue(new retrofit2.Callback<ResponseLogin>() {
            @Override
            public void onResponse(Call<ResponseLogin> call, retrofit2.Response<ResponseLogin> response) {
                if (response.body() == null) {
                    Log.d(TAG, "body null " + call.request().url());
                    mLoginListener.onLoginError();
                    return;
                }
                jSessionId = response.body().getSessionId();
                mLoginListener.onLoginSucceded(jSessionId);
            }

            @Override
            public void onFailure(Call<ResponseLogin> call, Throwable t) {
                Log.d(TAG, "Response error");
            }
        });
    }

    public void authSpotify() {
        mWebView.clearCache(true);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

                Log.d(TAG, request.getUrl().toString());
                if (request.getUrl().toString().contains("/connect/facebook;jsessionid")) {

                    return true;
                }

                return super.shouldOverrideUrlLoading(view, request);
            }
        });
        ArrayMap<String, String> headers = new ArrayMap<>();
        headers.put("Cookie", "JSESSIONID=" + jSessionId + ";");
        Log.d(TAG, "jsessionid = " + jSessionId);
        mWebView.loadUrl(SpotifyService.BASE_URL + "/login/spotify", headers);
    }
}
