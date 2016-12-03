package com.langf.efence.http;

import android.content.Context;
import android.os.Environment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.GlideModule;

import java.io.InputStream;

/**
 * Created by dujr on 16-11-6.
 */
public class OkHttpGlideModule implements GlideModule{

    String downloadDirectoryPath= Environment.getExternalStorageDirectory().getPath()+"/langfTempImages";  //缓存的外部地址
    int DiskLruCacheSizeMegaBytes=1024*1024*250;  //外部缓存的最大值
    int InternalCacheSizeMegaBytes=1024*1024*100;  //内部缓存的最大值
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDecodeFormat(DecodeFormat.PREFER_RGB_565);//设置图片解码的格式，节约内存
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())){
            builder.setDiskCache(new DiskLruCacheFactory(downloadDirectoryPath,"Langf_cache",DiskLruCacheSizeMegaBytes));//设置外部缓存
        }else {
            builder.setDiskCache(new InternalCacheDiskCacheFactory(context,InternalCacheSizeMegaBytes));//设置内部缓存
        }
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        glide.register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory());
    }

}
