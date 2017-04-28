package com.exclusivetrace.exclusivetrace;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.exclusivetrace.exclusivetrace.arcmenu.ArcMenu;

/**
 * 定位图标箭头指向手机朝向
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private RelativeLayout root_layout;
    private ArcMenu arcMenu;
    private ImageView start_bt_image;

    //判断开始按钮是否按下
    private Boolean memoty_status = false;

    //判断是否登录


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_main);

        bindView();
    }

    private void bindView() {
        root_layout = (RelativeLayout) findViewById(R.id.map_root_layout);
        arcMenu = (ArcMenu) findViewById(R.id.map_arc_menu);
        start_bt_image = (ImageView) findViewById(R.id.map_start_memory_iamge);

        start_bt_image.setOnClickListener(this);
        root_layout.setOnClickListener(this);
    }

    //对记忆按钮的动画处理
    private void startMemoryAnimation() {
        if (!memoty_status) {
            //改变status的值
            memoty_status = memoty_status ? false : true;


            //旋转动画
            getRotaAnim(200, 0, 180).start();

            //运动动画
            AnimatorSet animationSet = new AnimatorSet();

            animationSet.play(getScaleXAnimator()).with(getTransAnimX()).with(getScaleYAnimator()).with(getTransAnimY());
            animationSet.setStartDelay(100);
            animationSet.setDuration(400);

            animationSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                        //图片渐变效果
                        TransitionDrawable td = (TransitionDrawable) start_bt_image.getDrawable();
                        td.startTransition(500);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (animation.isRunning()) {
                        //图片渐变效果
                        TransitionDrawable td = (TransitionDrawable) start_bt_image.getDrawable();
                        td.startTransition(400);
                    }
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    if (animation.isRunning()) {
                        //图片渐变效果
                        TransitionDrawable td = (TransitionDrawable) start_bt_image.getDrawable();
                        td.startTransition(400);
                    }
                }
            });
            animationSet.start();


//            if (animation.isRunning()) {
//                //图片渐变效果
//                TransitionDrawable td = (TransitionDrawable) start_bt_image.getDrawable();
//                td.startTransition(400);
//            }

            //旋转动画
            getRotaAnim(300, 180, 180).start();
        }
    }

    //缩放效果
    private Animator getScaleXAnimator() {
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(start_bt_image, "scaleX", 1.0f, 0.3f);
        return scaleXAnim;
    }

    //缩放效果
    private Animator getScaleYAnimator() {
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(start_bt_image, "scaleY", 1.0f, 0.3f);
        return scaleYAnim;
    }

    //按轨迹方程来运动
    private Animator getTransAnimX() {
        ValueAnimator transAnimX = ValueAnimator.ofFloat(0, root_layout.getWidth() / 2 - 130);
        transAnimX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                start_bt_image.setTranslationX((Float) animation.getAnimatedValue());
            }
        });
        return transAnimX;
    }

    //按轨迹方程来运动
    private Animator getTransAnimY() {
        ValueAnimator transAnimY = ValueAnimator.ofFloat(0, -root_layout.getHeight() / 2 + 130);
        transAnimY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                start_bt_image.setTranslationY((Float) animation.getAnimatedValue());
            }
        });
        return transAnimY;
    }

    //旋转效果
    private Animator getRotaAnim(int durationMills, float fromdegrees, float todegrees) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(start_bt_image, "rotation", 0, 360);
        objectAnimator.setDuration(durationMills);
        return objectAnimator;
    }

    @Override
    public void onClick(View v) {
        //只要有点击事件就调用关闭arcmenu菜单的方法
        arcMenu.changeArcMenu();
        switch (v.getId()) {
            case R.id.map_start_memory_iamge:
                System.out.println("点击了开始按钮");
                startMemoryAnimation();
                break;
        }
    }
}