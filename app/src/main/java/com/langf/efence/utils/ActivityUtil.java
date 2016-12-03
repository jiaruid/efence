package com.langf.efence.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.langf.efence.R;

/**
 * Created by dujr on 16-11-10.
 */
public class ActivityUtil {

    private static ActivityUtil instance;
    private Context context;

    public static ActivityUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (ActivityUtil.class) {
                if (instance == null) {
                    instance = new ActivityUtil();
                }
            }
        }
        instance.context = context;
        return instance;
    }

    public void startActivity(Class<?> activityClass) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, activityClass);
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public void startActivity(Intent intent) {
        if (context == null) {
            return;
        }
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.tran_next_in, R.anim.tran_next_out);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
