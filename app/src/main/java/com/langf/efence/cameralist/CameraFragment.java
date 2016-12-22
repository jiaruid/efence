package com.langf.efence.cameralist;

import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.langf.efence.R;
import com.langf.efence.base.BaseFragment;
import com.langf.efence.http.LoadingDataTask;
import com.langf.efence.realplay.RealPlayActivity;
import com.langf.efence.utils.ActivityUtil;
import com.langf.efence.utils.Logger;
import com.langf.efence.utils.ToastUtil;
import com.videogo.constant.IntentConsts;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;

import java.util.List;

/**
 * Created by dujr on 16-11-6.
 */
public class CameraFragment extends BaseFragment {

    private PullToRefreshListView lvCameraList;
    private CameraListAdapter cameraListAdapter;
    private View mNoMoreView;

    @Override
    protected int getContentView() {
        return R.layout.camera_fragment;
    }

    @Override
    protected void findChildView(View view) {
        lvCameraList = (PullToRefreshListView) view.findViewById(R.id.lv_camera_list);
        mNoMoreView = LayoutInflater.from(getActivity()).inflate(R.layout.no_more_footer, null);
    }

    @Override
    protected void init() {
        lvCameraList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(
                        getActivity(),
                        System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                // 显示最后更新的时间
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                getCameraList();
            }
        });
        getCameraList();
        final CameraService cameraService = new CameraService();
        lvCameraList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position--;
                final EZDeviceInfo deviceInfo = cameraListAdapter.getItem(position);
                if (deviceInfo.getCameraNum() <= 0 || deviceInfo.getCameraInfoList() == null || deviceInfo.getCameraInfoList().size() <= 0) {
                    Logger.e("cameralist is null or cameralist size is 0");
                    return;
                }
                if (deviceInfo.getCameraNum() == 1 && deviceInfo.getCameraInfoList() != null && deviceInfo.getCameraInfoList().size() == 1) {
                    Logger.d("cameralist have one camera");
                    final EZCameraInfo cameraInfo = cameraService.getCameraInfoFromDevice(deviceInfo, 0);
                    if (cameraInfo == null) {
                        return;
                    }

                    Intent intent = new Intent(getActivity(), RealPlayActivity.class);
                    intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                    intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
                    ActivityUtil.getInstance(getActivity()).startActivity(intent);
                    return;
                }
            }
        });
    }

    private void getCameraList() {
        new LoadingDataTask() {
            private List<EZDeviceInfo> devices;
            @Override
            public void onPreExecute() {
//                lvCameraList.getRefreshableView().removeFooterView(mNoMoreView);
            }

            @Override
            public void doInBackground() throws BaseException {
                if(!isAdded()){
                    return;
                }
                devices = EZOpenSDK.getInstance().getDeviceList(0, 20);
            }

            @Override
            public void onPostExecute() {
                lvCameraList.onRefreshComplete();
                if(!isAdded()){
                    return;
                }
                if(devices!=null && devices.size()>0){
                    if(cameraListAdapter == null){
                        cameraListAdapter = new CameraListAdapter(getActivity(), devices);
                        lvCameraList.setAdapter(cameraListAdapter);
                    }else{
                        cameraListAdapter.notifyDataSetChanged(devices);
                    }
                }

            }

            @Override
            public void onError(Exception e) {
                if(!isAdded()){
                    return;
                }
                lvCameraList.onRefreshComplete();
                if(e instanceof BaseException){
                    ToastUtil.centerShowToast(getActivity(), getString(R.string.get_camera_list_fail));
                }
            }
        }.execute();
    }
}
