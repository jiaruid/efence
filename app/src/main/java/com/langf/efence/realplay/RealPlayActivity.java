package com.langf.efence.realplay;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.langf.efence.R;
import com.langf.efence.base.BaseActivity;
import com.langf.efence.utils.Logger;
import com.langf.efence.widget.loading.LoadingTextView;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.realplay.RealPlayStatus;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LocalInfo;
import com.videogo.util.Utils;
import com.videogo.widget.CheckTextButton;
import com.videogo.widget.CustomRect;
import com.videogo.widget.CustomTouchListener;
import com.videogo.widget.TitleBar;

import java.util.Random;

/**
 * Created by dujr on 16-11-8.
 */
public class RealPlayActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener, Handler.Callback, SurfaceHolder.Callback{

    //loading控件
    private RelativeLayout mRealPlayLoadingRl;
    private TextView mRealPlayTipTv;
    private ImageView mRealPlayPlayIv;
    private LoadingTextView mRealPlayPlayLoading;
    private LinearLayout mRealPlayPlayPrivacyLy;

    private RelativeLayout mRealPlayPlayRl;
    private SurfaceView mRealPlaySv;
    private SurfaceHolder mRealPlaySh;
    private CustomTouchListener mRealPlayTouchListener;

    //控制bar
    private LinearLayout mRealPlayControlRl;
    private ImageButton mRealPlayBtn;
    private ImageButton mRealPlaySoundBtn;
    private Button mRealPlayQualityBtn;
    private CheckTextButton mFullscreenButton;

    //横竖屏titlebar
    private TitleBar mPortraitTitleBar = null;
    private TitleBar mLandscapeTitleBar = null;

    //横屏titlebar返回键
    private CheckTextButton mFullScreenTitleBarBackBtn;

    private ScreenOrientationHelper mScreenOrientationHelper;
    /**
     * 屏幕当前方向
     */
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;
    private int mForceOrientation = 0;
    private Rect mRealPlayRect = null;

    private float mRealRatio = Constant.LIVE_VIEW_RATIO;
    /**
     * 标识是否正在播放
     */
    private int mStatus = RealPlayStatus.STATUS_INIT;
    private EZCameraInfo mCameraInfo;
    private EZDeviceInfo mDeviceInfo;
    private String mRtspUrl;
    private EZConstants.EZVideoLevel mCurrentQulityMode = EZConstants.EZVideoLevel.VIDEO_LEVEL_HD;
    private RealPlaySquareInfo mRealPlaySquareInfo;
    private LocalInfo mLocalInfo;
    private EZPlayer mEZPlayer;
    private Handler mHandler = new Handler(this);
    // 对讲模式
    private boolean mIsOnTalk = false;
    // 录像模式
    private boolean mIsOnPtz = false;
    // 播放比例
    private float mPlayScale = 1;

    private int mControlDisplaySec = 0;

    @Override
    protected int getContentView() {
        return R.layout.realplay_activity;
    }

    @Override
    protected void findView() {
        initTitleBar();
        initLoadingUI();

        mRealPlayPlayRl = (RelativeLayout) findViewById(R.id.realplay_play_rl);
        mRealPlaySv = (SurfaceView) findViewById(R.id.realplay_sv);
        mRealPlaySh = mRealPlaySv.getHolder();
        mRealPlaySh.addCallback(this);


        mRealPlayControlRl = (LinearLayout) findViewById(R.id.realplay_control_rl);
        mRealPlayBtn = (ImageButton) findViewById(R.id.realplay_play_btn);
        mRealPlaySoundBtn = (ImageButton) findViewById(R.id.realplay_sound_btn);
        mRealPlayQualityBtn = (Button) findViewById(R.id.realplay_quality_btn);
        mFullscreenButton = (CheckTextButton) findViewById(R.id.fullscreen_button);

        mScreenOrientationHelper = new ScreenOrientationHelper(this, mFullscreenButton, mFullScreenTitleBarBackBtn);
        setOnTouchListener();
    }

    private void setOnTouchListener() {
        mRealPlayTouchListener = new CustomTouchListener() {

            @Override
            public boolean canZoom(float scale) {
                if (mStatus == RealPlayStatus.STATUS_PLAY) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean canDrag(int direction) {
                if (mStatus != RealPlayStatus.STATUS_PLAY) {
                    return false;
                }
                if (mEZPlayer != null && mDeviceInfo != null) {
                    // 出界判断
                    if (DRAG_LEFT == direction || DRAG_RIGHT == direction) {
                        // 左移/右移出界判断
                        if (mDeviceInfo.isSupportPTZ()) {
                            return true;
                        }
                    } else if (DRAG_UP == direction || DRAG_DOWN == direction) {
                        // 上移/下移出界判断
                        if (mDeviceInfo.isSupportPTZ()) {
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public void onSingleClick() {
                onRealPlaySvClick();
            }

            @Override
            public void onDoubleClick(MotionEvent e) {
            }

            @Override
            public void onZoom(float scale) {
                Logger.d("onZoom:" + scale);
                if (mEZPlayer != null && mDeviceInfo != null &&  mDeviceInfo.isSupportZoom()) {
//                    startZoom(scale);
                }
            }

            @Override
            public void onDrag(int direction, float distance, float rate) {
                Logger.d("onDrag:" + direction);
                if (mEZPlayer != null) {
                    //Utils.showLog(RealPlayActivity.this, "onDrag rate:" + rate);
//                    startDrag(direction, distance, rate);
                }
            }

            @Override
            public void onEnd(int mode) {
               Logger.d("onEnd:" + mode);
//                if (mEZPlayer != null) {
//                    stopDrag(false);
//                }
//                if (mEZPlayer != null && mDeviceInfo != null && mDeviceInfo.isSupportZoom()) {
//                    stopZoom();
//                }
            }

            @Override
            public void onZoomChange(float scale, CustomRect oRect, CustomRect curRect) {
                Logger.d("onZoomChange:" + scale);
                if (mEZPlayer != null && mDeviceInfo != null && mDeviceInfo.isSupportZoom()) {
                    //采用云台调焦
                    return;
                }
                if (mStatus == RealPlayStatus.STATUS_PLAY) {
                    if (scale > 1.0f && scale < 1.1f) {
                        scale = 1.1f;
                    }
//                    setPlayScaleUI(scale, oRect, curRect);
                }
            }
        };
        mRealPlaySv.setOnTouchListener(mRealPlayTouchListener);
    }

    private void initTitleBar() {
        mPortraitTitleBar = (TitleBar) findViewById(R.id.title_bar_portrait);
        mPortraitTitleBar.addBackButton(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                closePtzPopupWindow();
//                closeTalkPopupWindow(true, false);
//                if (mStatus != RealPlayStatus.STATUS_STOP) {
//                    stopRealPlay();
//                    setRealPlayStopUI();
//                }
                finish();
            }
        });
        if (mRtspUrl == null) {
        } else {
            //mPortraitTitleBar.setBackgroundColor(getResources().getColor(R.color.black_bg));
        }
        mLandscapeTitleBar = (TitleBar) findViewById(R.id.title_bar_landscape);
        mLandscapeTitleBar.setStyle(Color.rgb(0xff, 0xff, 0xff), getResources().getDrawable(R.color.dark_bg_70p),
                getResources().getDrawable(R.drawable.message_back_selector));
        mLandscapeTitleBar.setOnTouchListener(this);
        mFullScreenTitleBarBackBtn = new CheckTextButton(this);
        mFullScreenTitleBarBackBtn.setBackgroundResource(R.drawable.common_title_back_selector);
        mLandscapeTitleBar.addLeftView(mFullScreenTitleBarBackBtn);
    }

    private void initLoadingUI() {
        mRealPlayLoadingRl = (RelativeLayout) findViewById(R.id.realplay_loading_rl);
        mRealPlayTipTv = (TextView) findViewById(R.id.realplay_tip_tv);
        mRealPlayPlayIv = (ImageView) findViewById(R.id.realplay_play_iv);
        mRealPlayPlayLoading = (LoadingTextView) findViewById(R.id.realplay_loading);
        mRealPlayPlayPrivacyLy = (LinearLayout) findViewById(R.id.realplay_privacy_ly);

        // 设置点击图标的监听响应函数
        mRealPlayPlayIv.setOnClickListener(this);
    }


    @Override
    protected void init() {
        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 获取配置信息操作对象
        mLocalInfo = LocalInfo.getInstance();
        // 获取屏幕参数
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mLocalInfo.setScreenWidthHeight(metric.widthPixels, metric.heightPixels);
        mLocalInfo.setNavigationBarHeight((int) Math.ceil(25 * getResources().getDisplayMetrics().density));

        Intent intent = getIntent();
        if (intent != null) {
            mCameraInfo = intent.getParcelableExtra(IntentConsts.EXTRA_CAMERA_INFO);
            mDeviceInfo = intent.getParcelableExtra(IntentConsts.EXTRA_DEVICE_INFO);
            mRtspUrl = intent.getStringExtra(IntentConsts.EXTRA_RTSP_URL);
            if (mCameraInfo != null) {
                mCurrentQulityMode = (mCameraInfo.getVideoLevel());
                mPortraitTitleBar.setTitle(mCameraInfo.getCameraName());
                mLandscapeTitleBar.setTitle(mCameraInfo.getCameraName());
            }
            Logger.d("rtspUrl:" + mRtspUrl);

            getRealPlaySquareInfo();
        }
        if (mDeviceInfo != null && mDeviceInfo.getIsEncrypt() == 1) {
//            mVerifyCode = DataManager.getInstance().getDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial());
        }
        startRealPlay();
    }

    private void getRealPlaySquareInfo() {
        if (TextUtils.isEmpty(mRtspUrl)) {
            return;
        }
        mRealPlaySquareInfo = new RealPlaySquareInfo();
        Uri uri = Uri.parse(mRtspUrl.replaceFirst("&", "?"));
        try {
            mRealPlaySquareInfo.mSquareId = Integer.parseInt(uri.getQueryParameter("squareid"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            mRealPlaySquareInfo.mChannelNo = Integer.parseInt(Utils.getUrlValue(mRtspUrl, "channelno=", "&"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        mRealPlaySquareInfo.mCameraName = uri.getQueryParameter("cameraname");
        try {
            mRealPlaySquareInfo.mSoundType = Integer.parseInt(uri.getQueryParameter("soundtype"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        mRealPlaySquareInfo.mCoverUrl = uri.getQueryParameter("md5Serial");
        if (!TextUtils.isEmpty(mRealPlaySquareInfo.mCoverUrl)) {
            mRealPlaySquareInfo.mCoverUrl = mLocalInfo.getServAddr() + mRealPlaySquareInfo.mCoverUrl + "_mobile.jpeg";
        }
    }

    /**
     * 开始播放
     *
     */
    private void startRealPlay() {
        // 增加手机客户端操作信息记录
        Logger.d("--------startRealPlay-------");
        if (mStatus == RealPlayStatus.STATUS_START || mStatus == RealPlayStatus.STATUS_PLAY) {
            return;
        }

        // 检查网络是否可用
        if (!ConnectionDetector.isNetworkAvailable(this)) {
            // 提示没有连接网络
//            setRealPlayFailUI(getString(R.string.realplay_play_fail_becauseof_network));
            return;
        }

        mStatus = RealPlayStatus.STATUS_START;
        setRealPlayLoadingUI();

        if (mCameraInfo != null) {
            if (mEZPlayer == null) {
                mEZPlayer = EZOpenSDK.getInstance().createPlayer(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo());
//              mEZPlayer = mEZOpenSDK.createPlayerWithUrl(EZRealPlayActivity.this, "ysproto://vtm.ys7.com:8554/live?dev=473224256&chn=1&stream=1&cln=1&isp=0&biz=3");
//              mEZPlayer = EzvizApplication.getOpenSDK().createPlayerWithDeviceSerial(EZRealPlayActivity.this, mCameraInfo.getDeviceSerial(), mCameraInfo.getChannelNo(), 1);
            }
            if (mEZPlayer == null)
                return;

            if (mDeviceInfo == null) {
                return;
            }
            if (mDeviceInfo.getIsEncrypt() == 1) {
//                mEZPlayer.setPlayVerifyCode(DataManager.getInstance().getDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial()));
            }

            mEZPlayer.setHandler(mHandler);
            mEZPlayer.setSurfaceHold(mRealPlaySh);
            mEZPlayer.startRealPlay();
        } else if (mRtspUrl != null) {
            mEZPlayer = EZOpenSDK.getInstance().createPlayerWithUrl(mRtspUrl);
            //mStub.setCameraId(mCameraInfo.getCameraId());////****  mj
        }
        if(mEZPlayer != null){
            mEZPlayer.setHandler(mHandler);
            mEZPlayer.setSurfaceHold(mRealPlaySh);
            mEZPlayer.startRealPlay();
        }
        updateLoadingProgress(0);
    }

    private void updateLoadingProgress(final int progress) {
        mRealPlayPlayLoading.setTag(Integer.valueOf(progress));
        mRealPlayPlayLoading.setText(progress + "%");
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mRealPlayPlayLoading != null) {
                    Integer tag = (Integer) mRealPlayPlayLoading.getTag();
                    if (tag != null && tag.intValue() == progress) {
                        Random r = new Random();
                        mRealPlayPlayLoading.setText((progress + r.nextInt(20)) + "%");
                    }
                }
            }

        }, 500);
    }

    private void setRealPlayLoadingUI() {
//        mStartTime = System.currentTimeMillis();
        mRealPlaySv.setVisibility(View.INVISIBLE);
        mRealPlaySv.setVisibility(View.VISIBLE);
        setStartloading();
        mRealPlayBtn.setBackgroundResource(R.drawable.play_stop_selector);

        if (mCameraInfo != null  && mDeviceInfo != null) {
//            mRealPlayCaptureBtn.setEnabled(false);
//            mRealPlayRecordBtn.setEnabled(false);
            if (mDeviceInfo.getStatus() == 1) {
                mRealPlayQualityBtn.setEnabled(true);
            } else {
                mRealPlayQualityBtn.setEnabled(false);
            }
//            mRealPlayPtzBtn.setEnabled(false);
//
//            mRealPlayFullPlayBtn.setBackgroundResource(R.drawable.play_full_stop_selector);
//            mRealPlayFullCaptureBtn.setEnabled(false);
//            mRealPlayFullRecordBtn.setEnabled(false);
//            mRealPlayFullFlowLy.setVisibility(View.GONE);
//            mRealPlayFullPtzBtn.setEnabled(false);
        }

        showControlRlAndFullOperateBar();
    }


    private void setStartloading() {
        mRealPlayLoadingRl.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setVisibility(View.GONE);
        mRealPlayPlayLoading.setVisibility(View.VISIBLE);
        mRealPlayPlayIv.setVisibility(View.GONE);
        mRealPlayPlayPrivacyLy.setVisibility(View.GONE);
    }

    public void setStopLoading() {
        mRealPlayLoadingRl.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setVisibility(View.GONE);
        mRealPlayPlayLoading.setVisibility(View.GONE);
        mRealPlayPlayIv.setVisibility(View.VISIBLE);
        mRealPlayPlayPrivacyLy.setVisibility(View.GONE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mOrientation = newConfig.orientation;

        onOrientationChanged();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mEZPlayer != null) {
            mEZPlayer.release();

        }
        mScreenOrientationHelper = null;
    }

    /**
     * 屏幕方向调整
     */
    private void onOrientationChanged() {
//        mRealPlaySv.setVisibility(View.INVISIBLE);
        setRealPlaySvLayout();
//        mRealPlaySv.setVisibility(View.VISIBLE);
//
        updateOperatorUI();
//        updateCaptureUI();
//        updateTalkUI();
//        updatePtzUI();
    }

    private void updateOperatorUI() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // 显示状态栏
            fullScreen(false);
            updateOrientation();
            mPortraitTitleBar.setVisibility(View.VISIBLE);
            mLandscapeTitleBar.setVisibility(View.GONE);
            mRealPlayControlRl.setVisibility(View.VISIBLE);
//            if (mRtspUrl == null) {
//                mRealPlayPageLy.setBackgroundColor(getResources().getColor(R.color.common_bg));
//                mRealPlayOperateBar.setVisibility(View.VISIBLE);
//                mRealPlayFullOperateBar.setVisibility(View.GONE);
//                mFullscreenFullButton.setVisibility(View.GONE);
//                if (mIsRecording) {
//                    mRealPlayRecordBtn.setVisibility(View.GONE);
//                    mRealPlayRecordStartBtn.setVisibility(View.VISIBLE);
//                } else {
//                    mRealPlayRecordBtn.setVisibility(View.VISIBLE);
//                    mRealPlayRecordStartBtn.setVisibility(View.GONE);
//                }
//            }
        } else {
            // 隐藏状态栏
            fullScreen(true);
            mPortraitTitleBar.setVisibility(View.GONE);
            // hide the
            mRealPlayControlRl.setVisibility(View.GONE);
            if (!mIsOnTalk && !mIsOnPtz) {
                mLandscapeTitleBar.setVisibility(View.VISIBLE);
            }
//            if (mRtspUrl == null) {
//                mRealPlayOperateBar.setVisibility(View.GONE);
//                mRealPlayPageLy.setBackgroundColor(getResources().getColor(R.color.black_bg));
//                mRealPlayFullOperateBar.setVisibility(View.GONE);
//                if (!mIsOnTalk && !mIsOnPtz) {
//                    mFullscreenFullButton.setVisibility(View.GONE);
//                }
//                if (mIsRecording) {
//                    mRealPlayFullRecordBtn.setVisibility(View.GONE);
//                    mRealPlayFullRecordStartBtn.setVisibility(View.VISIBLE);
//                } else {
//                    mRealPlayFullRecordBtn.setVisibility(View.VISIBLE);
//                    mRealPlayFullRecordStartBtn.setVisibility(View.GONE);
//                }
//            }
        }

        //        mRealPlayControlRl.setVisibility(View.GONE);
//        closeQualityPopupWindow();
        if (mStatus == RealPlayStatus.STATUS_START) {
            showControlRlAndFullOperateBar();
        }
    }

    private void showControlRlAndFullOperateBar() {
        if (mRtspUrl != null || mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mRealPlayControlRl.setVisibility(View.VISIBLE);
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (!mIsOnTalk && !mIsOnPtz) {
                    mLandscapeTitleBar.setVisibility(View.VISIBLE);
                }
            } else {
                mLandscapeTitleBar.setVisibility(View.GONE);
            }
            mControlDisplaySec = 0;
        } else {
            if (!mIsOnTalk && !mIsOnPtz) {
//                mRealPlayFullOperateBar.setVisibility(View.VISIBLE);
                //                mFullscreenFullButton.setVisibility(View.VISIBLE);
                mLandscapeTitleBar.setVisibility(View.VISIBLE);
            }
            mControlDisplaySec = 0;
        }
    }

    /**
     * 设置播放全屏
     * @param enable
     */
    private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private void onRealPlaySvClick() {
        if (mCameraInfo != null && mEZPlayer != null && mDeviceInfo != null) {
            if (mDeviceInfo.getStatus() != 1) {
                return;
            }
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                setRealPlayControlRlVisibility();
            } else {
                setRealPlayFullOperateBarVisibility();
            }
        } else if (mRtspUrl != null) {
            setRealPlayControlRlVisibility();
        }
    }

    private void setRealPlayControlRlVisibility() {
        if (mLandscapeTitleBar.getVisibility() == View.VISIBLE || mRealPlayControlRl.getVisibility() == View.VISIBLE) {
            //            mRealPlayControlRl.setVisibility(View.GONE);
            mLandscapeTitleBar.setVisibility(View.GONE);
//            closeQualityPopupWindow();
        } else {
            mRealPlayControlRl.setVisibility(View.VISIBLE);
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (!mIsOnTalk && !mIsOnPtz) {
                    mLandscapeTitleBar.setVisibility(View.VISIBLE);
                }
            } else {
                mLandscapeTitleBar.setVisibility(View.GONE);
            }
            mControlDisplaySec = 0;
        }
    }

    private void setRealPlayFullOperateBarVisibility() {
        if (mLandscapeTitleBar.getVisibility() == View.VISIBLE) {
//            mRealPlayFullOperateBar.setVisibility(View.GONE);
            if (!mIsOnTalk && !mIsOnPtz) {
//                mFullscreenFullButton.setVisibility(View.GONE);
            }
            mLandscapeTitleBar.setVisibility(View.GONE);
        } else {
            if (!mIsOnTalk && !mIsOnPtz) {
                //mj mRealPlayFullOperateBar.setVisibility(View.VISIBLE);
                //                mFullscreenFullButton.setVisibility(View.VISIBLE);
                mLandscapeTitleBar.setVisibility(View.VISIBLE);
            }
            mControlDisplaySec = 0;
        }
    }

    /**
     * 调整播放窗口
     */
    private void setRealPlaySvLayout() {
        // 设置播放窗口位置
        final int screenWidth = mLocalInfo.getScreenWidth();
        final int screenHeight = (mOrientation == Configuration.ORIENTATION_PORTRAIT) ? (mLocalInfo.getScreenHeight() - mLocalInfo
                .getNavigationBarHeight()) : mLocalInfo.getScreenHeight();
        final RelativeLayout.LayoutParams realPlaySvlp = Utils.getPlayViewLp(mRealRatio, mOrientation,
                mLocalInfo.getScreenWidth(), (int) (mLocalInfo.getScreenWidth() * Constant.LIVE_VIEW_RATIO),
                screenWidth, screenHeight);

        RelativeLayout.LayoutParams loadingR1Lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                realPlaySvlp.height);
        //        loadingR1Lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        //        mRealPlayLoadingRl.setLayoutParams(loadingR1Lp);
        //        mRealPlayPromptRl.setLayoutParams(loadingR1Lp);
        RelativeLayout.LayoutParams svLp = new RelativeLayout.LayoutParams(realPlaySvlp.width, realPlaySvlp.height);
        //mj svLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mRealPlaySv.setLayoutParams(svLp);

        if (mRtspUrl == null) {
            //            LinearLayout.LayoutParams realPlayPlayRlLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
            //                    LayoutParams.WRAP_CONTENT);
            //            realPlayPlayRlLp.gravity = Gravity.CENTER;
            //            mRealPlayPlayRl.setLayoutParams(realPlayPlayRlLp);
        } else {
            LinearLayout.LayoutParams realPlayPlayRlLp = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            realPlayPlayRlLp.gravity = Gravity.CENTER;
            //realPlayPlayRlLp.weight = 1;
            mRealPlayPlayRl.setLayoutParams(realPlayPlayRlLp);
        }
        mRealPlayTouchListener.setSacaleRect(Constant.MAX_SCALE, 0, 0, realPlaySvlp.width, realPlaySvlp.height);
        setPlayScaleUI(1, null, null);
    }

    private void setPlayScaleUI(float scale, CustomRect oRect, CustomRect curRect) {
        if (scale == 1) {
            if (mPlayScale == scale) {
                return;
            }
//            mRealPlayRatioTv.setVisibility(View.GONE);
            try {
                if (mEZPlayer != null) {
                    mEZPlayer.setDisplayRegion(false, null, null);
                }
            } catch (BaseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            if (mPlayScale == scale) {
                try {
                    if (mEZPlayer != null) {
                        mEZPlayer.setDisplayRegion(true, oRect, curRect);
                    }
                } catch (BaseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return;
            }
//            RelativeLayout.LayoutParams realPlayRatioTvLp = (RelativeLayout.LayoutParams) mRealPlayRatioTv
//                    .getLayoutParams();
//            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
//                realPlayRatioTvLp.setMargins(Utils.dip2px(this, 10), Utils.dip2px(this, 10), 0, 0);
//            } else {
//                realPlayRatioTvLp.setMargins(Utils.dip2px(this, 70), Utils.dip2px(this, 20), 0, 0);
//            }
//            mRealPlayRatioTv.setLayoutParams(realPlayRatioTvLp);
//            String sacleStr = String.valueOf(scale);
//            mRealPlayRatioTv.setText(sacleStr.subSequence(0, Math.min(3, sacleStr.length())) + "X");
//            //mj mRealPlayRatioTv.setVisibility(View.VISIBLE);
//            mRealPlayRatioTv.setVisibility(View.GONE);
//            hideControlRlAndFullOperateBar(false);
            try {
                if (mEZPlayer != null) {
                    mEZPlayer.setDisplayRegion(true, oRect, curRect);
                }
            } catch (BaseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        mPlayScale = scale;
    }

    private void updateOrientation() {
        if (mIsOnTalk) {
            if (mEZPlayer != null && mDeviceInfo != null && mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport) {
                setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else {
                setForceOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            if (mStatus == RealPlayStatus.STATUS_PLAY) {
                setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else {
                if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }
        }
    }

    private void setOrientation(int sensor) {
        if (mForceOrientation != 0) {
            Logger.d("setOrientation mForceOrientation:" + mForceOrientation);
            return;
        }

        if (sensor == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
            mScreenOrientationHelper.enableSensorOrientation();
        else
            mScreenOrientationHelper.disableSensorOrientation();
    }

    public void setForceOrientation(int orientation) {
        if (mForceOrientation == orientation) {
            Logger.d("setForceOrientation no change");
            return;
        }
        mForceOrientation = orientation;
        if (mForceOrientation != 0) {
            if (mForceOrientation != mOrientation) {
                if (mForceOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    mScreenOrientationHelper.portrait();
                } else {
                    mScreenOrientationHelper.landscape();
                }
            }
            mScreenOrientationHelper.disableSensorOrientation();
        } else {
            updateOrientation();
        }
    }

    /**
     * 停止播放
     *
     */
    private void stopRealPlay() {
        Logger.d("--------stopRealPlay-------");
        mStatus = RealPlayStatus.STATUS_STOP;

//        stopUpdateTimer();
        if (mEZPlayer != null) {
//            stopRealPlayRecord();

            mEZPlayer.stopRealPlay();
        }
    }

    /**
     * 处理播放成功的情况
     *
     * @see
     * @since V1.0
     */
    private void handlePlaySuccess(Message msg) {
        mStatus = RealPlayStatus.STATUS_PLAY;

        // 声音处理
        setRealPlaySound();

//        // temp solution for OPENSDK-92
//        // Android 预览3Q10的时候切到流畅之后 视频播放窗口变大了
//        //        if (msg.arg1 != 0) {
//        //            mRealRatio = (float) msg.arg2 / msg.arg1;
//        //        } else {
//        //            mRealRatio = Constant.LIVE_VIEW_RATIO;
//        //        }
//        mRealRatio = Constant.LIVE_VIEW_RATIO;
//
//        boolean bSupport = true;//(float) mLocalInfo.getScreenHeight() / mLocalInfo.getScreenWidth() >= BIG_SCREEN_RATIO;
//        if (bSupport) {
//            initOperateBarUI(mRealRatio <= Constant.LIVE_VIEW_RATIO);
//            initUI();
//            if (mRealRatio <= Constant.LIVE_VIEW_RATIO) {
//                setBigScreenOperateBtnLayout();
//            }
//        }
//        setRealPlaySvLayout();
        setRealPlaySuccessUI();
//        updatePtzUI();、
//        //        startPrivacyAnim();
//        updateTalkUI();
//        if (mDeviceInfo != null && mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport) {
//            mRealPlayTalkBtn.setEnabled(true);
//        }else{
//            mRealPlayTalkBtn.setEnabled(false);
//        }
    }

    private void setRealPlaySuccessUI() {
//        mStopTime = System.currentTimeMillis();
//        showType();

        updateOrientation();
        setLoadingSuccess();
        //        mRealPlayFlowTv.setVisibility(View.VISIBLE);
//        mRealPlayFullFlowLy.setVisibility(View.VISIBLE);
        mRealPlayBtn.setBackgroundResource(R.drawable.play_stop_selector);

        if (mCameraInfo != null && mDeviceInfo != null) {
//            mRealPlayCaptureBtn.setEnabled(true);
//            mRealPlayRecordBtn.setEnabled(true);
            if (mDeviceInfo.getStatus() == 1) {
                mRealPlayQualityBtn.setEnabled(true);
            } else {
                mRealPlayQualityBtn.setEnabled(false);
            }
//            if (getSupportPtz() == 1) {
//                mRealPlayPtzBtn.setEnabled(true);
//            }

//            mRealPlayFullPlayBtn.setBackgroundResource(R.drawable.play_full_stop_selector);
//            mRealPlayFullCaptureBtn.setEnabled(true);
//            mRealPlayFullRecordBtn.setEnabled(true);
//            mRealPlayFullPtzBtn.setEnabled(true);
        }

//        setRealPlaySound();

//        startUpdateTimer();
    }

    private void setRealPlaySound() {
        if (mEZPlayer != null) {
            if (mRtspUrl == null) {
                if (mLocalInfo.isSoundOpen()) {
                    mEZPlayer.openSound();
                } else {
                    mEZPlayer.closeSound();
                }
            } else {
                if (mRealPlaySquareInfo.mSoundType == 0) {
                    mEZPlayer.closeSound();
                } else {
                    mEZPlayer.openSound();
                }
            }
        }
    }

    private void setLoadingSuccess() {
        mRealPlayLoadingRl.setVisibility(View.INVISIBLE);
        mRealPlayTipTv.setVisibility(View.GONE);
        mRealPlayPlayLoading.setVisibility(View.GONE);
        mRealPlayPlayIv.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.realplay_play_btn:
            case R.id.realplay_play_iv:
                if (mStatus != RealPlayStatus.STATUS_STOP) {
                    stopRealPlay();
//                    setRealPlayStopUI();
                } else {
                    startRealPlay();
                }
                break;
        }
    }

    /**
     * 获取设备信息成功
     *
     * @see
     * @since V1.0
     */
    private void handleGetCameraInfoSuccess() {
        Logger.d("handleGetCameraInfoSuccess");

        //通过能力级设置
//        updateUI();

    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case EZConstants.EZRealPlayConstants.MSG_GET_CAMERA_INFO_SUCCESS:
                updateLoadingProgress(20);
                handleGetCameraInfoSuccess();
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_START:
                updateLoadingProgress(40);
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_CONNECTION_START:
                updateLoadingProgress(60);
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_CONNECTION_SUCCESS:
                updateLoadingProgress(80);
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_SUCCESS:
                handlePlaySuccess(msg);
                break;
            case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_FAIL:
//                handlePlayFail(msg.arg1, msg.arg2);
                break;
        }
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(holder);
        }
        mRealPlaySh = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(null);
        }
        mRealPlaySh = null;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()){
//            case R.id.realplay_full_operate_bar:
//                return true;
        }
        return false;
    }
}
