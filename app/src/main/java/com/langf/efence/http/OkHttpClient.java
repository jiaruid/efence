package com.langf.efence.http;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.langf.efence.utils.InternalStorage;
import com.langf.efence.utils.JSONUtil;
import com.langf.efence.utils.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by dujr on 2016/8/3.
 */
public class OkHttpClient {

    private okhttp3.OkHttpClient okHttpClient;
    private static OkHttpClient instance;
    private Context context;
    private Handler mMainHander;
    private final ArrayBlockingQueue<Call> calls = new ArrayBlockingQueue<Call>(100);

    private OkHttpClient(){
        okhttp3.OkHttpClient.Builder builder = new okhttp3.OkHttpClient().newBuilder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS);
        X509TrustManager[] trustManagers = new X509TrustManager[]{new TrustAllManager()};
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new SecureRandom());
            builder.sslSocketFactory(sslContext.getSocketFactory(), trustManagers[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
            Logger.e("trust cert error!", e);
        }
        okHttpClient = builder.build();
        mMainHander = new Handler(Looper.getMainLooper());
    }

    public static  OkHttpClient getInstance(Context context) {
        if (instance == null)
            synchronized (OkHttpClient.class) {
                if (instance == null) {
                    instance = new OkHttpClient();
                }
        }
        instance.context = context;
        return instance;
    }

    class TrustAllManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            try {
                chain[0].checkValidity();
            }catch (Exception e){
                throw new CertificateException("Certificate not valid or trusted.");
            }
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };

    private Request.Builder addHeader(Request.Builder builder){
        return builder;
    }

    public <T> void sendGet(final RequestVO<T> requestVO){
        final Request request = addHeader(new Request.Builder()).url(requestVO.url).get().build();
        newCall(requestVO, request);
    }

    public <T> void sendPost(final RequestVO<T> requestVO){
        FormBody.Builder builder = new FormBody.Builder();
        if(requestVO.params != null) {
            for (String key : requestVO.params.keySet()) {
                Object value = requestVO.params.get(key);
                if (value instanceof String) {
                    builder.add(key, (String) value);
                } else if (value instanceof List<?>) {
                    List<?> list = (List<?>) value;
                    for (int i = 0; i < list.size(); i++) {
                        builder.add(key, JSONUtil.getJSON(list.get(i)));
                    }
                } else {
                    builder.add(key, JSONUtil.getJSON(value));
                }
            }
        }
        final Request request = addHeader(new Request.Builder()).url(requestVO.url).post(builder.build()).build();
        newCall(requestVO, request);
    }

    public <T> void sendDelete(final RequestVO<T> requestVO){
        final Request request = addHeader(new Request.Builder()).url(requestVO.url).delete().build();
        newCall(requestVO, request);
    }

    private int dealCount = 0;
    public <T> void sendImage(final RequestVO<T> requestVO){
        if(requestVO.params!=null){
            final List<BitmapUploadParams> bitmaps = (List<BitmapUploadParams>) requestVO.params.get("bitmaps");
            final List<T> datas = new ArrayList<T>();
            dealCount = 0;
            for(final BitmapUploadParams image : bitmaps){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                image.bitmap.compress(image.format, 100, baos);
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", "", RequestBody.create(MediaType.parse("image/jpeg"), baos.toByteArray()))
                        .build();
                final Request request = addHeader(new Request.Builder()).url(image.url).post(requestBody).build();
                final Call call = okHttpClient.newCall(request);
                addCall(image.url, call);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        dealCount++;
                        if (requestVO.listener != null) {
                            mMainHander.post(new Runnable() {
                                @Override
                                public void run() {
                                    requestVO.listener.onFailure("-1", e.getMessage());
                                }
                            });
                        }
                        removeCall(call);
                        Logger.e("post image error!", e);
                    }

                    @Override
                    public void onResponse(Call call, final Response response) {
                        dealCount++;
                        if(response.isSuccessful()) {
                            String json = null;
                            try {
                                json = response.body().string();
                            } catch (IOException e) {
                                Logger.e(image.url + " ->response error.", e);
                            }
                            if (json != null) {
                                T t = JSONUtil.getFromJSON(json, requestVO.clazz);
                                if (t != null) {
                                    datas.add(t);
                                }
                            }
                        }else{
                            if (requestVO.listener != null) {
                                mMainHander.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        requestVO.listener.onFailure(response.code()+"", "response fail.");
                                    }
                                });
                            }
                        }
                        if(dealCount == bitmaps.size()){
                            mMainHander.post(new Runnable() {
                                @Override
                                public void run() {
                                    paserResponse(JSONUtil.getJSON(datas), requestVO);
                                }
                            });
                        }
                        removeCall(call);
                    }
                });
            }
        }
    }

    private <T> void newCall(final RequestVO<T> requestVO, final Request request) {
        Call call = okHttpClient.newCall(request);
        addCall(requestVO.url, call);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                if (requestVO.listener != null) {
                    mMainHander.post(new Runnable() {
                        @Override
                        public void run() {
                            requestVO.listener.onFailure("-1", e.getMessage());
                        }
                    });
                }
                Logger.e("access to "+requestVO.url+" error!", e);
                removeCall(call);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                Logger.d(response.toString());
                if(response.isSuccessful()) {
                    String result = null;
                    try {
                        result = response.body().string();
                        if(requestVO.isForceCache){
                            InternalStorage.getStorage(context).save(requestVO.url, result);
                        }
                    } catch (Exception e) {
                        Logger.e(requestVO.url+" ->response error.", e);
                    }
                    final String finalResult = result;
                    mMainHander.post(new Runnable() {
                        @Override
                        public void run() {
                            paserResponse(finalResult, requestVO);
                        }
                    });
                } else{
                    if (requestVO.listener != null) {
                        mMainHander.post(new Runnable() {
                            @Override
                            public void run() {
                                requestVO.listener.onFailure(response.code()+"", "response fail.");
                            }
                        });
                    }
                }
                removeCall(call);
            }
        });
    }

    private <T> void paserResponse(String result, final RequestVO<T> requestVO) {
        final OnResultListener<T> listener = requestVO.listener;
        if(listener == null) {
            return;
        }
        if (!TextUtils.isEmpty(result)) {
            try {
                if (requestVO.isForceCache && !TextUtils.isEmpty(result)) {
                    InternalStorage.getStorage(context.getApplicationContext()).save(requestVO.url, result);
                }
            } catch (IOException e) {
                Logger.e("cache error.", e);
            }
            try {
                if (listener instanceof OnListResultListener) {
                    ((OnListResultListener<T>) listener).onSuccess(JSONUtil.parserJsonToList(requestVO.clazz, result), OnResultListener.LOAD_NET);
                } else {
                    listener.onSuccess(JSONUtil.getFromJSON(result, requestVO.clazz), OnResultListener.LOAD_NET);
                }
            } catch (final Exception e) {
                Logger.e("paser json error.", e);
                if (requestVO.listener != null) {
                    mMainHander.post(new Runnable() {
                        @Override
                        public void run() {
                            requestVO.listener.onFailure("-1", e.getMessage());
                        }
                    });
                }
            }
        } else {
            if (listener instanceof OnListResultListener) {
                ((OnListResultListener<T>) listener).onSuccess(new ArrayList<T>(), OnResultListener.LOAD_NET);
            } else {
                listener.onSuccess(null, OnResultListener.LOAD_NET);
            }
        }
    }

    /**
     * 将一个异步任务放入异步请求池
     * @param call
     */
    public void addCall(String url, Call call){
        calls.offer(call);
        Logger.w("add a new call, and "+calls.size()+" calls are waiting. url="+url);
    }

    /**
     * 将一个异步任务放入异步请求池
     * @param call
     */
    public void removeCall(Call call){
        boolean result = calls.remove(call);
        Logger.w("remove a running call "+(result?"success":"failure")+", and "+calls.size()+" calls are waiting");
    }

    /**
     * 清除所有的网络异步请求任务
     */
    public void clearCallQueue() {
        Logger.w("Cancel "+calls.size()+" calls except the running...");
        Call call = calls.poll();
        while(call!=null){
            if(!call.isCanceled())
                call.cancel();
            call = calls.poll();
        }
    }
}
