package com.exclusivetrace.exclusivetrace;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.exclusivetrace.exclusivetrace.arcmenu.ArcMenu;

import java.lang.reflect.Method;

/**
 * 定位图标箭头指向手机朝向
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private RelativeLayout root_layout, memoryLayout;
    private ImageView arcmenu_bt, startMemory, endMemory;
    private ArcMenu arcMenu;
    private PopupWindow popupWindow;
    private View view;

    //判断开始按钮是否按下
    private Boolean memory_status = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_main);

        bindView();
    }


    //确保布局加载完毕，之后加载popupwindow
    /*@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            //showPopupWindow();
        }
    }*/

    //显示popupwindow
    private void showPopupWindow() {
        view = LayoutInflater.from(MainActivity.this).inflate(R.layout.map_arcmenu, null);
        arcMenu = (ArcMenu) view.findViewById(R.id.map_arc_menu);
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindow.showAtLocation(root_layout, Gravity.CENTER, 0, 0);
        //马上进行一次改变
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                arcMenu.changeArcMenu();
            }
        });
        arcMenu.setOnBtClose(new ArcMenu.OnBtClose() {
            @Override
            public void OnClose() {
                popupWindow.dismiss();
            }
        });

        //点击其他地方消失
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (view != null && view.isShown()) {
                    //关闭菜单
                    arcMenu.changeArcMenu();
                    backgroundAlpha(1.0f); //背景颜色变回来
                    popupWindow.setOutsideTouchable(false);

                    //arcMenu.changeArcMenuStatus();
                    //setPopupWindowTouchModal(popupWindow, false);
                    //popupWindow = null;
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
                    popupWindow.setOutsideTouchable(false);
                    //setPopupWindowTouchModal(popupWindow, false);
                    //popupWindow = null;
                    return true;
                }
                return false;
            }
        });

        /*popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //背景颜色变回来
                backgroundAlpha(1.0f);
            }
        });*/
        //菜单的点击事件
        arcMenu.setOnMenuClickListener(new ArcMenu.OnMenuClickListener() {
            @Override
            public void OnClik(int index) {
                /*if (!arcMenu.mStatus_arcmenu) {
                    //背景颜色变回来
                    backgroundAlpha(1.0f);
                    popupWindow.setOutsideTouchable(false);
                    //setPopupWindowTouchModal(popupWindow, false);
                }
                //当点击的不是登录时，旋转按钮,并改变status的值
                if (arcMenu.mStatus_arcmenu && index != 5) {
                    System.out.println("逆向关闭旋转");
                    backgroundAlpha(1.0f); //背景颜色变回来
                }*/
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
                        if (arcMenu.mStatus_arcmenu)
                            backgroundAlpha(1.0f); //背景颜色变回来
                        else
                            backgroundAlpha(0.5f); // 设置背景颜色变暗
                        break;
                }
            }
        });
    }

    //反射使点击传递到下层
    /*private void setPopupWindowTouchModal(PopupWindow popupWindow, boolean touchModal) {
        if (null == popupWindow) {
            return;
        }
        Method method;
        try {
            method = PopupWindow.class.getDeclaredMethod("setTouchModal",
                    boolean.class);
            method.setAccessible(true);
            method.invoke(popupWindow, touchModal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    //b绑定控件，处理监听事件
    private void bindView() {
        root_layout = (RelativeLayout) findViewById(R.id.map_root_layout);
        memoryLayout = (RelativeLayout) findViewById(R.id.map_memory_layout);
        arcmenu_bt = (ImageView) findViewById(R.id.map_arcmenu_bt);
        startMemory = (ImageView) findViewById(R.id.map_start_memory_iamge);
        endMemory = (ImageView) findViewById(R.id.map_end_memory_image);

        memoryLayout.setOnClickListener(this);
        root_layout.setOnClickListener(this);
        arcmenu_bt.setOnClickListener(this);

        endMemory.setAlpha(0f);
    }

    /**
     * 设置popupwindo添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
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
                backgroundAlpha(0.5f); // 设置背景颜色变暗
                showPopupWindow();
                break;
        }
    }
}