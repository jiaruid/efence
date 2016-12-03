package com.langf.efence.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

/**
 * Created by dujr on 16-11-7.
 */
public class ImageView extends android.widget.ImageView {
    private Context context;
    private int resId = -1;//默认图片的资源位置

    public ImageView(Context context) {
        super(context);
        this.context = context;
    }

    public ImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void setUrl(String url) {
        if (resId != -1) {
            Glide.with(context).load(url).placeholder(resId).dontAnimate().centerCrop().into(this);
        } else {
            Glide.with(context).load(url).dontAnimate().centerCrop().into(this);
        }
    }

    public void setUrl(String url, final Finisher finisher) {
        Glide.with(context).load(url).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {

            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                if (resource != null) {
                    setImageBitmap(resource);
                }
                if (finisher != null) {
                    finisher.finished(resource);
                }
            }
        });
    }

    public void setUrl(String url, int defaultId) {
        if (TextUtils.isEmpty(url)) {
            setImageResource(defaultId);
        } else {
            Glide.with(context).load(url).placeholder(defaultId).dontAnimate().centerCrop().into(this);
        }
    }

    public void setUrl(String url, int defaultId, boolean isShowAnimation) {
        if (TextUtils.isEmpty(url)) {
            setImageResource(defaultId);
        } else {
            if (isShowAnimation) {
                Glide.with(context).load(url).placeholder(defaultId).centerCrop().into(this);
            } else {
                Glide.with(context).load(url).placeholder(defaultId).dontAnimate().centerCrop().into(this);
            }
        }
    }

    public void setUrl(String url, int defaultId, int errorId) {
        if (TextUtils.isEmpty(url)) {
            setImageResource(defaultId);
        } else {
            Glide.with(context).load(url).placeholder(defaultId).dontAnimate().error(errorId).centerCrop().into(this);
        }
    }

    public void setGif(int resId) {
        Glide.with(context).load(resId).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(this);
    }

    //利用Glide处理加载图片资源
    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        this.resId = resId;
    }

    public interface Finisher {
        void finished(Bitmap bitmap);
    }
}

