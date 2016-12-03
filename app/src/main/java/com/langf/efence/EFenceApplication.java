package com.langf.efence;

import android.app.Application;

import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EzvizAPI;

/**
 * Created by dujr on 16-10-22.
 */
public class EFenceApplication extends Application {

    private final String APP_KEY = "5f56033758b24a99ac9b15bc575fc851";

    @Override
    public void onCreate() {
        super.onCreate();

        //init sdk
        EZOpenSDK.initLib(this, APP_KEY, "");
    }
}
