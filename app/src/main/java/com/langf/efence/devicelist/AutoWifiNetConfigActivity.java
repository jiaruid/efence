package com.langf.efence.devicelist;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.langf.efence.base.BaseActivity;

/**
 * 一键连接网络配置界面
 * 
 * @author chengjuntao
 * @data 2014-4-9
 */
public class AutoWifiNetConfigActivity extends BaseActivity implements OnClickListener {

    /** wifi密码 */
    public static final String WIFI_PASSWORD = "wifi_password";

    /** wifiSSID */
    public static final String WIFI_SSID = "wifi_ssid";

    /** deviceType */
    public static final String DEVICE_TYPE = "device_type";

    public static final String SUPPORT_WIFI = "support_Wifi";
    public static final String SUPPORT_NET_WORK = "support_net_work";

    private Button btnNext;

    private TextView tvSSID;

    private EditText edtPassword;

    private String seriaNo;

    private String veryCode = null;

    private boolean isSupportNetWork = false;

    private boolean isFromDeviceSetting;

    private String deviceType;

    private TextView tvTitle;


    @Override
    protected int getContentView() {
        return 0;
    }

    @Override
    protected void findViews() {

    }

    @Override
    protected void init() {

    }

    @Override
    public void onClick(View v) {

    }
}
