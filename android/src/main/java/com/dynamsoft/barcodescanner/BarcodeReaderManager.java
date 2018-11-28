package com.dynamsoft.barcodescanner;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.dynamsoft.camera.DBR;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import org.json.JSONException;
import org.json.JSONObject;

public class BarcodeReaderManager extends ReactContextBaseJavaModule {

    private Callback mResultCallback;
    private static final int REQUEST_CODE = 2017;
    private static final String CANCELLED = "cancelled";
    private static final String TEXT = "text";
    private static final String FORMAT = "format";
    private static final String LOG_TAG = "BarcodeScanner";

    public BarcodeReaderManager(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(mActivityEventListener);
    }

    @Override
    public String getName() {
        return "BarcodeReaderManager";
    }

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
            if (requestCode == REQUEST_CODE) {
                if (mResultCallback != null) {
                    if (resultCode == Activity.RESULT_OK) {
                        JSONObject obj = new JSONObject();
                        try {
                            obj.put(TEXT, intent.getStringExtra("SCAN_RESULT"));
                            obj.put(FORMAT, intent.getStringExtra("SCAN_RESULT_FORMAT"));
                        } catch (JSONException e) {
                            Log.d(LOG_TAG, "This should never happen");
                        }
                        mResultCallback.invoke(obj.toString());
                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        Toast.makeText(getReactApplicationContext(), "Cancelled", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getReactApplicationContext(), "Unexpected error", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    };

    @ReactMethod
    public void readBarcode(String licenseKey, Callback resultCallback, Callback errorCallback) {
        mResultCallback = resultCallback;

        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            errorCallback.invoke("Activity doesn't exist");
            return;
        }

        Intent cameraIntent = new Intent(currentActivity.getBaseContext(), DBR.class);
        cameraIntent.setAction("com.dynamsoft.dbr");
        cameraIntent.putExtra("licenseKey", licenseKey);

        // avoid calling other phonegap apps
        cameraIntent.setPackage(currentActivity.getApplicationContext().getPackageName());
        //currentActivity.startActivity(cameraIntent);
        currentActivity.startActivityForResult(cameraIntent, REQUEST_CODE);
    }
}