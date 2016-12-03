package com.langf.efence.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.langf.efence.home.HomeActivity;
import com.langf.efence.service.UrlService;
import com.langf.efence.utils.ToastUtil;
import com.videogo.constant.Constant;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZAccessToken;

/**
 * Created by dujr on 16-10-22.
 */
public class EzvizBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action == null){
            return;
        }
        if(action.equals(Constant.OAUTH_SUCCESS_ACTION)){
            //登录成功
            ToastUtil.centerShowToast(context, "login success.");
            Intent toIntent = new Intent(context, HomeActivity.class);
            toIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            /*******   获取登录成功之后的EZAccessToken对象   *****/
            EZAccessToken token = EZOpenSDK.getInstance().getEZAccessToken();
            UrlService.getInstance().setAccessToken(token);
            context.startActivity(toIntent);
        }else if(action.equals("android.net.conn.CONNECTIVITY_CHANGE")){
            //网络变化
            ToastUtil.centerShowToast(context, "network changed.");
        }
    }
}
