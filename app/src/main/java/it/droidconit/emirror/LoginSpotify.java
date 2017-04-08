package it.droidconit.emirror;


import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;

import it.droidconit.emirror.server_responses.LoginResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginSpotify {

    private LoginResponse loginResponse;
    private SpotifyService spotifyService;
    private WebView mWebView;
    private OkHttpClient mClient;
    private Retrofit mRetrofit;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private String JSESSIONID;
    private Callback firstCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

        }
    };


    public LoginSpotify(WebView webView) {
        mClient = new OkHttpClient();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(SpotifyService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        spotifyService = mRetrofit.create(SpotifyService.class);


        mWebView.clearCache(true);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

                if (request.getUrl().toString().contains("/connect/facebook;jsessionid")) {

                    return true;
                }

                return super.shouldOverrideUrlLoading(view, request);
            }
        });

//        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
//                .followRedirects(false)
//                .followSslRedirects(false)
//                .build();
    }



    public void performLogin() {
        RequestBody body = RequestBody.create(JSON, "");

        Request firstRequestSpotify = new Request.Builder()
                .url("https://api.radiosa.biz:8443/connect/facebook")
                .addHeader("Authorization", loginResponse.getAccess_token())
                .post(body)
                .build();
        mClient.newCall(firstRequestSpotify).enqueue(firstCallback);
    }
}
