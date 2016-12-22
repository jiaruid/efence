package com.langf.efence.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by dujr on 16-11-6.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        findViews();
        init();
    }

    protected abstract int getContentView();
    protected abstract void findViews();
    protected abstract void init();
}
