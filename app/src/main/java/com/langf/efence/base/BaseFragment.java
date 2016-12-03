package com.langf.efence.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by dujr on 16-11-6.
 */
public abstract class BaseFragment extends Fragment {

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView != null) {
            ViewGroup parent = (ViewGroup) mView.getParent();
            if (parent != null) {
                parent.removeView(mView);
            }
            return mView;
        }
        mView = inflater.inflate(getContentView(), null);
        findChildView(mView);
        init();
        return mView;
    }

    protected abstract int getContentView();
    protected abstract void findChildView(View view);
    protected abstract void init();

}
