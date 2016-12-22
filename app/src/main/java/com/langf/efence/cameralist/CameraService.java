package com.langf.efence.cameralist;

import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;

/**
 * Created by dujr on 16-11-21.
 */
public class CameraService {

    /**
     * 通过ezdevice 得到其中通道信息
     * @param deviceInfo
     * @return
     */
    public EZCameraInfo getCameraInfoFromDevice(EZDeviceInfo deviceInfo,int camera_index) {
        if (deviceInfo == null) {
            return null;
        }
        if (deviceInfo.getCameraNum() > 0 && deviceInfo.getCameraInfoList() != null && deviceInfo.getCameraInfoList().size() > camera_index) {
            return deviceInfo.getCameraInfoList().get(camera_index);
        }
        return null;
    }
}
