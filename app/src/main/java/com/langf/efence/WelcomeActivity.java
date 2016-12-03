package com.langf.efence;

import com.langf.efence.base.BaseActivity;
import com.videogo.openapi.EZOpenSDK;

public class WelcomeActivity extends BaseActivity {


    @Override
    protected int getContentView() {
        return R.layout.welcome_activity;
    }

    @Override
    protected void findView() {

    }

    @Override
    protected void init() {
        EZOpenSDK.getInstance().openLoginPage();
        finish();
    }
}
