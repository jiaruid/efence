package com.langf.efence.camera;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.langf.efence.R;
import com.langf.efence.utils.ImageUtil;
import com.langf.efence.widget.ImageView;
import com.videogo.openapi.bean.EZDeviceInfo;

import java.util.List;
/**
 * Created by dujr on 16-11-7.
 */
public class CameraListAdapter extends BaseAdapter {

    private Context context;
    private List<EZDeviceInfo> data;

    public CameraListAdapter(Context context, List<EZDeviceInfo> data) {
        this.context = context.getApplicationContext();
        this.data = data;
    }

    public void notifyDataSetChanged(List<EZDeviceInfo> data) {
        this.data = data;
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public EZDeviceInfo getItem(int position) {
        EZDeviceInfo item = null;
        if (position >= 0 && getCount() > position) {
            item = data.get(position);
        }
        return item;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.camera_item_layout, null);
            holder.tvCameraName = (TextView) convertView.findViewById(R.id.tv_camera_name);
            holder.tvCameraPic = (ImageView) convertView.findViewById(R.id.iv_camera_pic);
            holder.rlOfflineLayout = (RelativeLayout) convertView.findViewById(R.id.rl_offline_layout);
            ImageUtil.measureLayout(holder.tvCameraPic, 9f/16);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        EZDeviceInfo deviceInfo =  data.get(position);
        if (deviceInfo != null){
            if (deviceInfo.getStatus() == 2) {
                holder.rlOfflineLayout.setVisibility(View.VISIBLE);
            } else {
                holder.rlOfflineLayout.setVisibility(View.INVISIBLE);
            }
            holder.tvCameraName.setText(deviceInfo.getDeviceName());
            holder.tvCameraPic.setUrl(deviceInfo.getDeviceCover());
        }

        return convertView;
    }

    class ViewHolder {
        com.langf.efence.widget.ImageView tvCameraPic;
        TextView tvCameraName;
        RelativeLayout rlOfflineLayout;
    }
}
