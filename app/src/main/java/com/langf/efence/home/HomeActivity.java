package com.langf.efence.home;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.langf.efence.R;
import com.langf.efence.base.BaseActivity;
import com.langf.efence.camera.CameraFragment;
import com.langf.efence.widget.NoScrollViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dujr on 16-11-6.
 */
public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private List<Fragment> mFragmentList = new ArrayList<Fragment>();

    private NoScrollViewPager vpHomeContainer;
    private TextView tvTabCamera;
    private TextView tvTabMe;

    @Override
    public int getContentView() {
        return R.layout.home_activity;
    }

    @Override
    protected void findView() {
        vpHomeContainer = (NoScrollViewPager) findViewById(R.id.vp_home_container);
        LinearLayout llCameraTab = (LinearLayout) findViewById(R.id.ll_home_tab_camera);
        LinearLayout llMeTab = (LinearLayout) findViewById(R.id.ll_home_tab_me);
        llCameraTab.setOnClickListener(this);
        llMeTab.setOnClickListener(this);

        tvTabCamera = (TextView) findViewById(R.id.tv_tab_camera);
        tvTabMe = (TextView) findViewById(R.id.tv_tab_me);
    }

    @Override
    protected void init() {
        CameraFragment cameraFragment = new CameraFragment();
        MeFragment meFragment = new MeFragment();
        mFragmentList.add(cameraFragment);
        mFragmentList.add(meFragment);
        FragmentAdapter fragmentAdapter = new FragmentAdapter(this.getSupportFragmentManager(), mFragmentList);
        vpHomeContainer.setAdapter(fragmentAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_home_tab_camera:
                vpHomeContainer.setCurrentItem(0, false);
                tvTabCamera.setTextColor(Color.BLUE);
                tvTabMe.setTextColor(Color.BLACK);
                break;
            case R.id.ll_home_tab_me:
                vpHomeContainer.setCurrentItem(1, false);
                tvTabMe.setTextColor(Color.BLUE);
                tvTabCamera.setTextColor(Color.BLACK);
                break;
            default:
                break;
        }
    }
}
