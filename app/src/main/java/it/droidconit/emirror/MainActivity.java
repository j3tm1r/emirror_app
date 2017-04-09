package it.droidconit.emirror;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import java.util.Iterator;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, LoginSpotify.LoginListener {

    private GoogleApiClient mGoogleApiClient;
    private String TAG = this.getClass().getSimpleName();
    private int RC_SIGN_IN = 127;
    private TextView mStatusTextView;
    private WebView mWebView;
    private LoginSpotify mLoginSpotify;

    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private AdvertiseCallback mAdvertisementCallback = new SampleAdvertiseCallback();
    private String mJessionId;
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattServer myGattServer;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(
                        new Scope("https://www.googleapis.com/auth/calendar.readonly"),
                        new Scope("https://www.googleapis.com/auth/gmail.readonly"))
                .requestServerAuthCode("265467573813-dvtth5t7i55gklr9i6qs6isq13d9l8b5.apps.googleusercontent.com", false)
                .requestId()
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        mStatusTextView = (TextView) findViewById(R.id.status_text_view);
        mWebView = (WebView) findViewById(R.id.webview_spotify_login);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothLeAdvertiser.stopAdvertising(mAdvertisementCallback);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            // ...
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        GoogleSignInAccount acct = null;
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            acct = result.getSignInAccount();
            mStatusTextView.setText(acct.getDisplayName());


            Iterator<Scope> iterator = acct.getGrantedScopes().iterator();

            while (iterator.hasNext()) {
                Log.d(TAG, "Authorized scope " + iterator.next().toString());
            }

            updateUI(acct);
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(acct);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null) {
            mWebView.setVisibility(View.VISIBLE);

            mLoginSpotify = new LoginSpotify(mWebView, this);
            mLoginSpotify.performLogin(account.getServerAuthCode());
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Returns an AdvertiseData object which includes the Service UUID and Device Name.
     */
    private AdvertiseData buildAdvertiseData() {

        /**
         * Note: There is a strict limit of 31 Bytes on packets sent over BLE Advertisements.
         *  This includes everything put into AdvertiseData including UUIDs, device info, &
         *  arbitrary service or manufacturer data.
         *  Attempting to send packets over this limit will result in a failure with error code
         *  AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE. Catch this error in the
         *  onStartFailure() method of an AdvertiseCallback implementation.
         */

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addServiceUuid(
                ParcelUuid.fromString("0000b81d-0000-1000-8000-00805f9b34fb"));
        dataBuilder.setIncludeDeviceName(true);
        dataBuilder.setIncludeTxPowerLevel(true);

        /* For example - this will cause advertising to fail (exceeds size limit) */
        //String failureData = "asdghkajsghalkxcjhfa;sghtalksjcfhalskfjhasldkjfhdskf";
        //dataBuilder.addServiceData(Constants.Service_UUID, failureData.getBytes());

        return dataBuilder.build();
    }

    /**
     * Returns an AdvertiseSettings object set to use low power (to help preserve battery life)
     * and disable the built-in timeout since this code uses its own timeout runnable.
     */
    private AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM);
        settingsBuilder.setTimeout(0);
        settingsBuilder.setConnectable(true);
        return settingsBuilder.build();
    }

    @Override
    public void onLoginSucceded(final String JSESSIONID) {
        mJessionId = JSESSIONID;

        BluetoothManager bleManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothLeAdvertiser = bleManager.getAdapter().getBluetoothLeAdvertiser();


        myGattServer = bleManager.openGattServer(MainActivity.this, new BluetoothGattServerCallback() {
            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
                myGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, JSESSIONID.getBytes());
            }

        });

        bluetoothGattService =
                new BluetoothGattService(
                        UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb"),
                        BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fc"),
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);

        characteristic.setValue(JSESSIONID);

        bluetoothGattService.addCharacteristic(characteristic);


        Log.d(TAG, "Ble service started " + myGattServer.addService(bluetoothGattService));

        bluetoothLeAdvertiser.startAdvertising(
                buildAdvertiseSettings(),
                buildAdvertiseData(),
                mAdvertisementCallback);

        String url = "http://hackathonit6301.cloudapp.net:80/login/spotify?jsessionid=" + JSESSIONID;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

//    private AdvertiseData buildScanResponseData() {
//
//        AdvertiseData.Builder scanresponseBuilder = new AdvertiseData.Builder();
//
//        scanresponseBuilder.addServiceData(ParcelUuid.fromString("0000a72c-0000-1000-8000-00805f9b34fb"), mJessionId.getBytes());
//
//
//        return scanresponseBuilder.build();
//    }

    @Override
    public void onLoginError() {

    }

    /**
     * Custom callback after Advertising succeeds or fails to start. Broadcasts the error code
     * in an Intent to be picked up by AdvertiserFragment and stops this Service.
     */
    private class SampleAdvertiseCallback extends AdvertiseCallback {

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);

            Log.d(TAG, "Advertising failed " + errorCode);

        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d(TAG, "Advertising successfully started");
        }
    }
}
