package com.exclusivetrace.exclusivetrace.map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.LatLng;
import com.exclusivetrace.exclusivetrace.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/5/8.
 */

public class LocationService extends Service implements AMapLocationListener {

    private final String TAG = "LocationServer";

    private LatLng latLng;
    private String time;

    private LocalBroadcastManager broadcastManager;

    //Location
    //声明AMapLocationClient类对象
    private AMapLocationClient aMapLocationClient = null;
    //声明AMapLocationClientOption对象
    private AMapLocationClientOption aMapLocationClientOption = null;

    //定义onBinder方法所返回的对象
    private MyBinder binder = new MyBinder();

    private Intent intent = new Intent("GETLOCATION");

    public class MyBinder extends Binder {
        private LatLng latLng;
        private String time;

        public LatLng getLatLng() {
            return latLng;
        }

        public void setLatLng(LatLng latLng) {
            this.latLng = latLng;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

    //定位
    private void startLocation() {
        if (aMapLocationClient == null) {
            //初始化定位
            aMapLocationClient = new AMapLocationClient(getApplicationContext());
            //设置定位回调监听
            aMapLocationClient.setLocationListener(this);
            //初始化AMapLocationClientOption对象
            aMapLocationClientOption = new AMapLocationClientOption();
            //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
            aMapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
            aMapLocationClientOption.setInterval(2000);
            //设置是否强制刷新WIFI，默认为true，强制刷新。
            //aMapLocationClientOption.setWifiActiveScan(false);
            //设置是否返回地址信息（默认返回地址信息）
            //aMapLocationClientOption.setNeedAddress(true);
            //设置是否允许模拟位置,默认为false，不允许模拟位置
            //aMapLocationClientOption.setMockEnable(false);
            //给定位客户端对象设置定位参数
            aMapLocationClient.setLocationOption(aMapLocationClientOption);
            //启动定位
            aMapLocationClient.startLocation();
            System.out.println("启动定位成功");
        }
    }

    //用于接收异步返回的定位结果，回调参数是AMapLocation
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        System.out.println("位置改变");
        //首先，可以判断AMapLocation对象不为空，当定位错误码类型为0时定位成功。
        if (aMapLocation != null /*&& mListener != null*/) {
            if (aMapLocation.getErrorCode() == 0) {
                //可在其中解析amapLocation获取相应内容。
                /******
                 * aMapLocation.getLatitude();//获取纬度
                 * aMapLocation.getLongitude();//获取经度
                 */
                LatLng mylocation = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                binder.setLatLng(mylocation);

                //获取定位时间
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(aMapLocation.getTime());
                String time = df.format(date);
                System.out.println("位置改变时间:" + time);
                binder.setTime(time);

                broadcastManager.sendBroadcast(intent);
            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }

    //Service被创建时回调
    @Override
    public void onCreate() {
        super.onCreate();
        //开始定位
        startLocation();
        System.out.println("定位服务创建");
        if (broadcastManager == null)
            broadcastManager = LocalBroadcastManager.getInstance(this);
        Log.i(TAG, "onCreate方法被调用!");
        startNotification();
    }

    //开启notification
    private void startNotification() {
        Notification.Builder localBuilder = new Notification.Builder(this);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, MainActivity.)
        localBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT));
        localBuilder.setAutoCancel(false);
        localBuilder.setSmallIcon(R.drawable.map_bt_fog);
        localBuilder.setTicker("Foreground Service Start");
        localBuilder.setContentTitle("独家迹忆");
        localBuilder.setContentText("正在运行...");
        Notification notification = localBuilder.build();
        //NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        startForeground(1, notification);
    }

    //Service断开连接时回调
    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind方法被调用!");
        return true;
    }

    //Service被关闭前回调
    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("定位结束");
        if (aMapLocationClient != null) {
            aMapLocationClient.stopLocation();
            aMapLocationClient.onDestroy();
        }
        aMapLocationClient = null;
        Log.i(TAG, "onDestroyed方法被调用!");
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "onRebind方法被调用!");
        super.onRebind(intent);
    }

    //必须实现的方法,绑定改Service时回调该方法
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
