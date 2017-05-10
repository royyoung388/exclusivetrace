package com.exclusivetrace.exclusivetrace.map;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.Window;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.exclusivetrace.exclusivetrace.AddDiary;
import com.exclusivetrace.exclusivetrace.ChangePwd;
import com.exclusivetrace.exclusivetrace.Customized;
import com.exclusivetrace.exclusivetrace.DiaryItem;
import com.exclusivetrace.exclusivetrace.Login;
import com.exclusivetrace.exclusivetrace.R;
import com.exclusivetrace.exclusivetrace.arcmenu.ArcMenu;

public class MainActivity extends Activity implements View.OnClickListener {

    //UI
    private RelativeLayout root_layout, memoryLayout;
    private ImageView arcmenu_bt, startMemory, endMemory;
    private ArcMenu arcMenu;
    private PopupWindow popupWindow;
    private View view;
    private MapView mapView;

    //判断是否开始记忆
    private Boolean memory_status = false;

    //Map
    private AMap aMap;

    //poly
    //PolylineOptions
    private PolylineOptions lineOptions;
    private Polyline polyline;
    //LatLng
    private LatLng latLng;
    //time
    private String time;

    private LocationService.MyBinder binder;

    private LocalBroadcastManager localBroadcastManager;
    private MyBcReceiver localReceiver;

    private ServiceConnection conn = new ServiceConnection() {

        //Activity与Service断开连接时回调该方法
        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.out.println("------Service DisConnected-------");
        }

        //Activity与Service连接成功时回调该方法
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            System.out.println("------Service Connected-------");
            binder = (LocationService.MyBinder) service;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_main);

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        bindView();

        //在activity执行onCreate时执行mapView.onCreate(savedInstanceState)，创建地图
        mapView.onCreate(savedInstanceState);
        startMap();
        //初始化PolylineOptions
        initPolylineOptions();

        //aMap.setCustomRenderer(new MaskMapRender(aMap));

        //添加Marker点
        //setMarker();
    }

    //绑定控件，处理监听事件
    private void bindView() {
        root_layout = (RelativeLayout) findViewById(R.id.map_root_layout);
        memoryLayout = (RelativeLayout) findViewById(R.id.map_memory_layout);
        arcmenu_bt = (ImageView) findViewById(R.id.map_arcmenu_bt);
        startMemory = (ImageView) findViewById(R.id.map_start_memory_iamge);
        endMemory = (ImageView) findViewById(R.id.map_end_memory_image);
        //获取地图控件引用
        mapView = (MapView) findViewById(R.id.map_map);

        memoryLayout.setOnClickListener(this);
        root_layout.setOnClickListener(this);
        arcmenu_bt.setOnClickListener(this);

        endMemory.setAlpha(0f);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        //只要有点击事件就调用关闭arcmenu菜单的方法
        //arcMenu.changeArcMenu();
        switch (v.getId()) {
            case R.id.map_memory_layout:
                if (memory_status) {
                    //打开了
                    //解除service绑定
                    unbindService(conn);
                    System.out.println("解除service绑定");
                    localBroadcastManager.unregisterReceiver(localReceiver);
                    System.out.println("注销BroadcastReceiver");
                } else {
                    //没有打开
                    System.out.println("点击了开始按钮");
                    startLocationService();
                    registerBroadcast();
                    startMemoryAnimation();
                }
                break;
            case R.id.map_arcmenu_bt:
                //按钮旋转动画
                System.out.println("点击了即将正向打开旋转");
                showPopupWindow();
                break;
        }
    }

    //开始定位的service
    private void startLocationService() {
        Intent intent = new Intent();
        intent.setAction("com.exclusivetrace.exclusivetrace.map.LOCATION");
        intent.setPackage("com.exclusivetrace.exclusivetrace");
        //绑定service
        bindService(intent, conn, Service.BIND_AUTO_CREATE);
        System.out.println("开启定位service");
    }

    //动态注册广播
    private void registerBroadcast() {
        //初始化广播接收者，设置过滤器
        if (localBroadcastManager == null)
            localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localReceiver = new MyBcReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("GETLOCATION");
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
        System.out.println("注册广播成功");
    }

    //初始化PolylineOptions
    private void initPolylineOptions() {
        lineOptions = new PolylineOptions();
        lineOptions.width(10f);
        lineOptions.color(Color.GRAY);
    }

    //加载地图
    private void startMap() {
        //初始化地图控制器对象
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        //aMap.setLocationSource();// 设置定位监听
        MyLocationStyle myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(1000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.radiusFillColor(Color.parseColor("#2E7BDE9E"));
        myLocationStyle.strokeColor(Color.parseColor("#2E7BDE9E"));
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.map_location));
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        //aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE);//设置定位模式旋转
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
    }

    //自定义一个广播接收器
    public class MyBcReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            System.out.println("广播接收器");
            latLng = binder.getLatLng();
            System.out.println("接收到latLng:" + latLng.toString());
            time = binder.getTime();
            System.out.println("接收到time:" + time);
            //移动镜头
            //aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
            //添加点，并绘画
            lineOptions.add(latLng);
            drawLine();
        }
    }

    //画线
    private void drawLine() {
        if (lineOptions.getPoints().size() > 1) {
            polyline = aMap.addPolyline(lineOptions);
            System.out.println("画线成功");
        }
    }

    //双击退出
    private long mPressedTime = 0;
    @Override
    public void onBackPressed() {
        long mNowTime = System.currentTimeMillis();//获取第一次按键时间
        if ((mNowTime - mPressedTime) > 2000) {//比较两次按键时间差
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mPressedTime = mNowTime;
        } else {//退出程序
            this.finish();
            System.exit(0);
        }
    }

    //添加marker点
    private void setMarker() {
        LatLng latLng = new LatLng(39.999391, 116.135972);
        //aMap.addMarker(new MarkerOptions().position(latLng).title("北京").snippet("DefaultMarker"));
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(latLng);
        markerOption.title("北京").snippet("DefaultMarker");

        markerOption.draggable(false);//设置Marker no可拖动
        //markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.maker)));
        // 将Marker设置为贴地显示，可以双指下拉地图查看效果
        markerOption.setFlat(true);//设置marker平贴地图效果
        markerOption.alpha(0.5f);
        aMap.addMarker(markerOption);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mapView.onDestroy()，销毁地图
        mapView.onDestroy();
        if (memory_status) {
            //解除service绑定
            unbindService(conn);
            System.out.println("解除service绑定");
            localBroadcastManager.unregisterReceiver(localReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

    //显示popupwindow
    private void showPopupWindow() {
        view = LayoutInflater.from(MainActivity.this).inflate(R.layout.map_arcmenu, null);
        arcMenu = (ArcMenu) view.findViewById(R.id.map_arc_menu);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindow.showAtLocation(root_layout, Gravity.CENTER, 0, 0);
        //设置按钮开始和结束动画时屏幕黑屏的效果
        arcMenu.setOnBtClose(new ArcMenu.OnBtClose() {
            @Override
            public void OnClose() {
                System.out.println("OnClose背景颜色变回来");
                popupWindow.dismiss();
                backgroundAlpha(1.0f); //背景颜色变回来
            }
        });
        arcMenu.setOnBtOn(new ArcMenu.OnBtOn() {
            @Override
            public void OnOn() {
                System.out.println("OnOn设置背景颜色变暗");
                backgroundAlpha(0.5f); // 设置背景颜色变暗
            }
        });
        //马上进行一次改变
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                arcMenu.changeArcMenu();
            }
        });

        //点击其他地方消失
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (view != null && view.isShown() && arcMenu.mStatus_arcmenu) {
                    //关闭菜单
                    arcMenu.changeArcMenu();
                    backgroundAlpha(1.0f); //背景颜色变回来
                }
                return false;
            }
        });
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    //关闭菜单
                    arcMenu.changeArcMenu();
                    System.out.println("逆向关闭旋转");
                    backgroundAlpha(1.0f); //背景颜色变回来
                    return true;
                }
                return false;
            }
        });

        //菜单的点击事件
        arcMenu.setOnMenuClickListener(new ArcMenu.OnMenuClickListener() {
            @Override
            public void OnClik(int index) {
                switch (index) {
                    case 0:
                        //退出登录
                        backgroundAlpha(1.0f); //背景颜色变回来
                        break;
                    case 1:
                        //改密码
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(MainActivity.this, ChangePwd.class));
                            }
                        }, 0);
                        backgroundAlpha(1.0f); //背景颜色变回来
                        break;
                    case 2:
                        //添加日记
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(MainActivity.this, AddDiary.class));
                            }
                        }, 0);
                        backgroundAlpha(1.0f); //背景颜色变回来
                        break;
                    case 3:
                        //日记列表
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(MainActivity.this, DiaryItem.class));
                            }
                        }, 0);
                        backgroundAlpha(1.0f); //背景颜色变回来
                        break;
                    case 4:
                        //自主定制
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(MainActivity.this, Customized.class));
                            }
                        }, 0);
                        backgroundAlpha(1.0f); //背景颜色变回来
                        break;
                    case 5:
                        //登录
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (false)
                                    startActivity(new Intent(MainActivity.this, Login.class));
                            }
                        }, 0);
                        if (false) {
                            backgroundAlpha(1.0f); //背景颜色变回来
                        }
                        break;
                    case 6:
                        //按钮
                        if (arcMenu.mStatus_arcmenu) backgroundAlpha(1.0f); //背景颜色变回来
                        /*else
                            backgroundAlpha(0.5f); // 设置背景颜色变暗*/
                        break;
                }
            }
        });
    }

    //修改popwindow弹出是背景颜色
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void backgroundAlpha(float dimAmount) {
        ViewGroup parent = (ViewGroup) getWindow().getDecorView().getRootView();
        if (dimAmount == 1.0) {
            ViewGroupOverlay overlay = parent.getOverlay();
            overlay.clear();
            return;
        }
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        dim.setAlpha((int) (255 * dimAmount));
        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.add(dim);
    }

    //对记忆按钮的动画处理
    private void startMemoryAnimation() {
        if (!memory_status) {
            //改变status的值
            memory_status = memory_status ? false : true;

            //运动动画
            AnimatorSet animationSet = new AnimatorSet();

            animationSet.play(getScaleXAnimator(400, 1f, 0.36f)).with(getScaleYAnimator(400, 1f, 0.36f))
                    .with(getTransAnimX(400)).with(getTransAnimY(400))
                    .with(getRotaAnim(300, 90, 360))
                    .with(getAlphaAnim(startMemory, 400, 1f, 0f)).with(getAlphaAnim(endMemory, 400, 0f, 1f))
                    .after(getRotaAnim(100, 0, 90));
            animationSet.start();
        }
    }

    //x缩放效果
    private Animator getScaleXAnimator(int mills, float fromScale, float toScale) {
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(memoryLayout, "scaleX", fromScale, toScale);
        scaleXAnim.setDuration(mills);
        return scaleXAnim;
    }

    //y缩放效果
    private Animator getScaleYAnimator(int mills, float fromScale, float toScale) {
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(memoryLayout, "scaleY", fromScale, toScale);
        scaleYAnim.setDuration(mills);
        return scaleYAnim;
    }

    //x开始按钮按轨迹方程来运动
    private Animator getTransAnimX(int mills) {
        /*ValueAnimator transAnimX = ValueAnimator.ofFloat(0, root_layout.getWidth() / 2 - 130);
        transAnimX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                memoryLayout.setTranslationX((Float) animation.getAnimatedValue());
            }
        });*/
        ObjectAnimator transAnimX = ObjectAnimator.ofFloat(memoryLayout, "translationX", 0, root_layout.getWidth() / 2 - 100);
        transAnimX.setDuration(mills);
        transAnimX.setInterpolator(new AnticipateInterpolator(1f));
        return transAnimX;
    }

    //y开始按钮按轨迹方程来运动
    private Animator getTransAnimY(int mills) {
        /*ValueAnimator transAnimY = ValueAnimator.ofFloat(0, -root_layout.getHeight() / 2 + 130);
        transAnimY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                memoryLayout.setTranslationY((Float) animation.getAnimatedValue());
            }
        });*/
        ObjectAnimator transAnimY = ObjectAnimator.ofFloat(memoryLayout, "translationY", 0, -root_layout.getHeight() / 2 + 330);
        transAnimY.setDuration(mills);
        transAnimY.setInterpolator(new AnticipateInterpolator(1f));
        return transAnimY;
    }

    //开始按钮旋转效果
    private Animator getRotaAnim(int durationMills, float fromdegrees, float todegrees) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(memoryLayout, "rotation", fromdegrees, todegrees);
        objectAnimator.setDuration(durationMills);
        objectAnimator.setInterpolator(new LinearInterpolator());
        return objectAnimator;
    }

    //图片的透明度变化
    private Animator getAlphaAnim(View view, int mills, float fromalph, float toalph) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", fromalph, toalph);
        objectAnimator.setDuration(mills);
        return objectAnimator;
    }
}