package com.exclusivetrace.exclusivetrace.Map;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.AMapGLOverlay;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.Tile;
import com.amap.api.maps.model.TileOverlayOptions;
import com.amap.api.maps.model.TileProvider;
import com.autonavi.ae.gmap.gloverlay.GLOverlay;
import com.exclusivetrace.exclusivetrace.AddDiary;
import com.exclusivetrace.exclusivetrace.ChangePwd;
import com.exclusivetrace.exclusivetrace.Customized;
import com.exclusivetrace.exclusivetrace.DiaryItem;
import com.exclusivetrace.exclusivetrace.Login;
import com.exclusivetrace.exclusivetrace.R;
import com.exclusivetrace.exclusivetrace.arcmenu.ArcMenu;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private RelativeLayout root_layout, memoryLayout;
    private ImageView arcmenu_bt, startMemory, endMemory;
    private ArcMenu arcMenu;
    private PopupWindow popupWindow;
    private View view;
    private MapView mapView;
    private AMap aMap;

    //判断开始按钮是否按下
    private Boolean memory_status = false;

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

        //aMap.setCustomRenderer(new MaskMapRender(aMap));

        //添加面
        //setPlane();
        //添加线段
        setLine();
        //添加Marker点
        setMarker();
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
                System.out.println("点击了开始按钮");
                startMemoryAnimation();
                break;
            case R.id.map_arcmenu_bt:
                //按钮旋转动画
                System.out.println("点击了即将正向打开旋转");
                showPopupWindow();
                break;
        }
    }

    //加载地图
    private void startMap() {
        //初始化地图控制器对象
        if (aMap == null) {
            aMap = mapView.getMap();
        }
    }

    //添加线段
    private void setLine() {
        LatLng center = new LatLng(39.999391,116.135972);
        double halfHeight = 1;
        double halfWidth = 1;
        List<LatLng> latLngs = new ArrayList<LatLng>();
        latLngs.add(new LatLng(center.latitude - halfHeight, center.longitude - halfWidth));
        latLngs.add(new LatLng(center.latitude - halfHeight, center.longitude + halfWidth));
        latLngs.add(new LatLng(center.latitude + halfHeight, center.longitude + halfWidth));
        latLngs.add(new LatLng(center.latitude + halfHeight, center.longitude - halfWidth));
        aMap.addPolygon(new PolygonOptions()
                .addAll(latLngs).fillColor(Color.argb(100, 255, 255, 255)).zIndex(1));
    }

    //添加面
    private void setPlane() {
        LatLng center = new LatLng(39.999391,116.135972);
        double halfHeight = 2;
        double halfWidth = 2;
        List<LatLng> latLngs = new ArrayList<LatLng>();
        latLngs.add(new LatLng(center.latitude - halfHeight, center.longitude - halfWidth));
        latLngs.add(new LatLng(center.latitude - halfHeight, center.longitude + halfWidth));
        latLngs.add(new LatLng(center.latitude + halfHeight, center.longitude + halfWidth));
        latLngs.add(new LatLng(center.latitude + halfHeight, center.longitude - halfWidth));
        aMap.addPolygon(new PolygonOptions()
                .addAll(latLngs).fillColor(Color.argb(100, 100, 100, 100)).strokeColor(Color.RED).strokeWidth(1).zIndex(1));

        //return latLngs;
    }

    //添加marker点
    private void setMarker() {
        LatLng latLng = new LatLng(39.999391,116.135972);
        //aMap.addMarker(new MarkerOptions().position(latLng).title("北京").snippet("DefaultMarker"));
        MarkerOptions markerOption = new MarkerOptions();
        markerOption.position(latLng);
        markerOption.title("北京").snippet("DefaultMarker");

        markerOption.draggable(false);//设置Marker no可拖动
        markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.maker)));
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