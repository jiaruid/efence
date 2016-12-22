/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.langf.efence.devicelist.scan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;

import com.google.zxing.Result;
import com.langf.efence.R;
import com.langf.efence.base.BaseActivity;
import com.langf.efence.devicelist.AutoWifiNetConfigActivity;
import com.langf.efence.devicelist.SeriesNumSearchActivity;
import com.langf.efence.devicelist.camera.CameraManager;
import com.langf.efence.utils.BeepManager;
import com.langf.efence.utils.Logger;
import com.langf.efence.utils.ToastUtil;
import com.videogo.exception.BaseException;
import com.videogo.exception.ExtraException;
import com.videogo.util.Base64;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LocalValidate;
import com.videogo.widget.TitleBar;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public final class CaptureActivity extends BaseActivity implements SurfaceHolder.Callback{

    private TitleBar mTitleBar;
    private Button mBtnRight;
    private SurfaceView mSurfaceView;

    /** 扫描View变量 */
    private ViewfinderView mViewfinderView = null;

    /** 闪光灯变量 */
    private CheckBox ckbLight = null;

    /** 定时器变量 */
    private InactivityTimer mInactivityTimer = null;

    /** 本地数据合法性检测变量 */
    private LocalValidate mLocalValidate= null;

    /** 控制声音和振动 */
    private BeepManager beepManager;
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;

    /** 序列号结果变量 */
    private String mSerialNoStr = null;

    private String mSerialVeryCodeStr = null;

    private String deviceType = "";

    private boolean isHasSurface = false;
    private Result savedResultToShow;

    public static final int REQUEST_CODE_CLOUD = 1;
    public static final int REQUEST_ADD_PROBE = 2;

    @Override
    protected int getContentView() {
        return R.layout.capture_activity;
    }

    @Override
    protected void findViews() {
        initTitleBar();
        ckbLight = (CheckBox) findViewById(R.id.ckbLight);
        mViewfinderView = ((ViewfinderView) findViewById(R.id.viewfinder_view));
        mSurfaceView = (SurfaceView) findViewById(R.id.preview_view);
        //default no light
        ckbLight.setChecked(false);
        setPramaFrontLight(false);
        ckbLight.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setPramaFrontLight(!getPramaFrontLight());
                reScan();
            }
        });
    }

    /**
     * @return the mViewfinderView
     */
    public ViewfinderView getmViewfinderView() {
        return mViewfinderView;
    }

    public void drawViewfinder() {
        getmViewfinderView().drawViewfinder();
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public Handler getHandler() {
        return handler;
    }

    /**
     * 初始化标题栏
     */
    private void initTitleBar() {
        mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.setTitle(R.string.scan_title_txt);
        mTitleBar.addBackButton(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mBtnRight = mTitleBar.addRightButton(R.drawable.common_title_input_selector, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addCameraBySN();
            }
        });

        // 做400ms保护 ，解决上个页面过来 点击过快 直接跑下一个界面问题。
        mBtnRight.setClickable(false);
        mBtnRight.postDelayed(new Runnable() {

            @Override
            public void run() {
                mBtnRight.setClickable(true);
            }
        }, 400);
    }

    @Override
    protected void init() {
        //保持屏幕恒亮
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mInactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        mLocalValidate = new LocalValidate();
    }

    private boolean getPramaFrontLight() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CaptureActivity.this);
        boolean currentSetting = prefs.getBoolean(PreferencesActivity.KEY_FRONT_LIGHT, false);
        return currentSetting;
    }

    /**
     * 设置闪关灯状态
     * @param isChecked
     */
    private void setPramaFrontLight(boolean isChecked) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CaptureActivity.this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PreferencesActivity.KEY_FRONT_LIGHT, isChecked);
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // CameraManager must be initialized here, not in onCreate(). This is
        // necessary because we don't
        // want to open the camera driver and measure the screen size if we're
        // going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the
        // wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getApplication());
        mViewfinderView.setCameraManager(cameraManager);

        handler = null;

        if (isHasSurface) {
            // The activity was paused but not stopped, so the surface still
            // exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(mSurfaceView.getHolder());
        } else {
            // Install the callback and wait for surfaceCreated() to init the
            // camera.
            mSurfaceView.getHolder().addCallback(this);
        }

        mInactivityTimer.onResume();
        ckbLight.setChecked(getPramaFrontLight());
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        mInactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();
        if (!isHasSurface) {
            mSurfaceView.getHolder().removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //close light
        setPramaFrontLight(false);
    }

    @Override
    protected void onDestroy() {
        mInactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CLOUD) {
            setResult(REQUEST_CODE_CLOUD, data);
            finish();
        } else if (requestCode == REQUEST_ADD_PROBE && resultCode == -1) {
            setResult(-1, data);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 初始化搜索
     *
     * @see
     * @since V1.8.2
     */
    private void reScan() {
        onPause();
        onResume();
    }

    /**
     * 跳转手动输入序列号页面
     *
     * @throws
     */
    private void addCameraBySN() {
        // type -0 手动输入序列号， type - 1二维码扫描
        Bundle bundle = new Bundle();
        bundle.putInt("type", 0);
        Intent intent = new Intent(CaptureActivity.this, SeriesNumSearchActivity.class);
        intent.putExtras(bundle);
        CaptureActivity.this.startActivity(intent);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Logger.e("*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!isHasSurface) {
            isHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isHasSurface = false;
    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param resultString
     *            The contents of the barcode.
     * @param barcode
     *            A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(String resultString, Bitmap barcode) {
        mInactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();

        if (resultString == null) {
            Logger.e("handleDecode-> resultString is null");
            return;
        }
        Logger.d("resultString = " + resultString);

        // 关注二维码名片地址
        // 例如：https://test.shipin7.com/h5/qrcode/intro?
        if (resultString.startsWith("https://") && resultString.contains("h5/qrcode/intro")) {
//            HikStat.onEvent(CaptureActivity.this, HikAction.ACTION_QRCODE_focus);
/*            Intent intent = new Intent(this, FollowActivity.class);
            intent.putExtra(IntentConsts.EXTRA_URL, resultString);
            startActivityForResult(intent, REQUEST_CODE_CLOUD);
*/
            // 设备二维码名片
        } else if (resultString.startsWith("http://") && resultString.contains("smart.jd.com")) {
            mSerialNoStr = "";
            mSerialVeryCodeStr = "";
            deviceType = "";
            try {
                String deviceInfoMarker = "$$$";
                String contentMarker = "f=";
                resultString = URLDecoder.decode(resultString, "UTF-8");
                // 验证url有效性 f=打头的为 需要的内容
                int contentIndex = resultString.indexOf(contentMarker);
                if (contentIndex < 0) {
                    mSerialNoStr = resultString;
                    isValidate();
                    return;
                }
                contentIndex += contentMarker.length();
                resultString = new String(Base64.decode(resultString.substring(contentIndex).trim()));
                int index = resultString.indexOf(deviceInfoMarker);
                // 二次判断有效性 $$$打头的为萤石信息
                if (index < 0) {
                    mSerialNoStr = resultString;
                    isValidate();
                    return;
                }
                index += deviceInfoMarker.length();
                resultString = resultString.substring(index);
                String[] infos = resultString.split("\r\n");
                if (infos.length >= 2) {
                    mSerialNoStr = infos[1];
                }
                if (infos.length >= 3) {
                    mSerialVeryCodeStr = infos[2];
                }
                if (infos.length >= 4) {
                    deviceType = infos[3];
                }
                isValidate();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            // 初始化数据
            mSerialNoStr = "";
            mSerialVeryCodeStr = "";
            deviceType = "";
            Logger.d(resultString);
            // CS-F1-1WPFR
            // CS-A1-1WPFR
            // CS-C1-1FPFR
            // resultString = "www.xxx.com\n456654855\nABCDEF\nCS-C3-21PPFR\n";
            // 字符集合
            String[] newlineCharacterSet = {
                    "\n\r", "\r\n", "\r", "\n"};
            String stringOrigin = resultString;
            // 寻找第一次出现的位置
            int a = -1;
            int firstLength = 1;
            for (String string : newlineCharacterSet) {
                if (a == -1) {
                    a = resultString.indexOf(string);
                    if (a > stringOrigin.length() - 3) {
                        a = -1;
                    }
                    if (a != -1) {
                        firstLength = string.length();
                    }
                }
            }

            // 扣去第一次出现回车的字符串后，剩余的是第二行以及以后的
            if (a != -1) {
                resultString = resultString.substring(a + firstLength);
            }
            // 寻找最后一次出现的位置
            int b = -1;
            for (String string : newlineCharacterSet) {
                if (b == -1) {
                    b = resultString.indexOf(string);
                    if (b != -1) {
                        mSerialNoStr = resultString.substring(0, b);
                        firstLength = string.length();
                    }
                }
            }

            // 寻找遗失的验证码阶段
            if (mSerialNoStr != null && b != -1 && (b + firstLength) <= resultString.length()) {
                resultString = resultString.substring(b + firstLength);
            }

            // 再次寻找回车键最后一次出现的位置
            int c = -1;
            for (String string : newlineCharacterSet) {
                if (c == -1) {
                    c = resultString.indexOf(string);
                    if (c != -1) {
                        mSerialVeryCodeStr = resultString.substring(0, c);
                    }
                }
            }

            // 寻找CS-C2-21WPFR 判断是否支持wifi
            if (mSerialNoStr != null && c != -1 && (c + firstLength) <= resultString.length()) {
                resultString = resultString.substring(c + firstLength);
            }
            if (resultString != null && resultString.length() > 0) {
                deviceType = resultString;
            }

            if (b == -1) {
                mSerialNoStr = resultString;
            }

            if (mSerialNoStr == null) {
                mSerialNoStr = stringOrigin;
            }
            Logger.d("mSerialNoStr = " + mSerialNoStr + ",mSerialVeryCodeStr = " + mSerialVeryCodeStr
                    + ",deviceType = " + deviceType);
            // 判断是不是9位
            isValidate();
        }
        // else {
        // 传感器添加 暂不实现
        // 网页/R1登录 暂不实现
        // 无法识别
        // handleLocalValidateSerialNoFail(ExtraException.SERIALNO_IS_ILLEGAL);
        // }
    }

    /**
     * 判断是不是合法
     *
     * @throws
     */
    private void isValidate() {
        mLocalValidate = new LocalValidate();
        try {
            mLocalValidate.localValidatSerialNo(mSerialNoStr);
            Logger.d(mSerialNoStr);
        } catch (BaseException e) {
            handleLocalValidateSerialNoFail(e.getErrorCode());
            Logger.e("searchCameraBySN-> local validate serial no fail, errCode:" + e.getErrorCode());
            return;
        }

        if (!ConnectionDetector.isNetworkAvailable(this)) {
            ToastUtil.centerShowToast(this, getString(R.string.query_camera_fail_network_exception));
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("type", 1);
        bundle.putString("SerialNo", mSerialNoStr);
        bundle.putString("very_code", mSerialVeryCodeStr);
        bundle.putString(AutoWifiNetConfigActivity.DEVICE_TYPE, deviceType);
        Logger.d("very_code:" + mSerialVeryCodeStr);
        Intent intent = new Intent(CaptureActivity.this, SeriesNumSearchActivity.class);
        intent.putExtras(bundle);
        CaptureActivity.this.startActivity(intent);
    }

    private void handleLocalValidateSerialNoFail(int errCode) {
        switch (errCode) {
            case ExtraException.SERIALNO_IS_NULL:
                ToastUtil.centerShowToast(this, getString(R.string.serial_number_is_null));
                break;
            case ExtraException.SERIALNO_IS_ILLEGAL:
                // showToast(R.string.serial_number_is_illegal);
                ToastUtil.centerShowToast(this, getString(R.string.unable_identify_two_dimensional_code_tip));
                break;
            default:
                ToastUtil.centerShowToast(this, getString(R.string.serial_number_error));
                Logger.e("handleLocalValidateSerialNoFail-> unkown error, errCode:" + errCode);
                break;
        }
        reScan();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        try {
            mSerialNoStr = null;
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a
            // RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, null, null, cameraManager);
            }

            initCrop();
            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            Logger.e("", ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Logger.e("Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    /**
     * 初始化截取的矩形区域
     */
    private void initCrop() {

    }

    private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
        // Bitmap isn't used yet -- will be used soon
        if (handler == null) {
            savedResultToShow = result;
        } else {
            if (result != null) {
                savedResultToShow = result;
            }
            if (savedResultToShow != null) {
                Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
                handler.sendMessage(message);
            }
            savedResultToShow = null;
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        // camera error
        ToastUtil.centerShowToast(this, getString(R.string.open_camera_fail));
    }
}
