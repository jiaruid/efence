package com.langf.efence.service;

import com.videogo.openapi.bean.EZAccessToken;

/**
 * Created by dujr on 16-11-6.
 */
public class UrlService {

    public String getUrlPrefix() {
        return URL_PREFIX;
    }

    private final String URL_PREFIX = "https://open.ys7.com/api/lapp/";

    private static UrlService instance;
    private EZAccessToken accessToken;
    private UrlService(){};
    public static UrlService getInstance(){
        if(instance == null)
            synchronized (UrlService.class) {
                if(instance == null)
                    instance = new UrlService();
            }
        return instance;
    }

    public String getCameraListUrl(){
        return URL_PREFIX + "camera/list";
    }

    public String getAccessToken() {
        if(accessToken==null || accessToken.getExpire()!=0&&accessToken.getExpire()<System.currentTimeMillis()){
            return null;
        }
        return accessToken.getAccessToken();
    }

    public void setAccessToken(EZAccessToken accessToken) {
        this.accessToken = accessToken;
    }
}
