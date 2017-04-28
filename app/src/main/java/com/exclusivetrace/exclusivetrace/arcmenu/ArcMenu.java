package com.exclusivetrace.exclusivetrace.arcmenu;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.exclusivetrace.exclusivetrace.R;


/**
 * Created by Administrator on 2017/4/25.
 */


/******
 * 不能使用属性动画实现消失效果，不然真的消失了
 */
public class ArcMenu extends ViewGroup implements View.OnClickListener {

    //菜单按钮的位置
    private Position mPosition = Position.LEFT_BOTTOM;

    private enum Position {
        LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM;
    }

    //赋给按钮的id值
    private final int ADD_BT_ID = 0;
    //按钮
    private View bt_add;

    //菜单半径
    private int mRadius = 100;

    //按钮状态
    private Boolean mStatus = false;

    //接口变量
    private OnMenuClickListener onMenuClickListener;

    //点击菜单的点击事件接口
    public interface OnMenuClickListener {
        void OnClik();
    }

    //暴露给外面的设置接口变量的方法
    public void setOnMenuClickListener(OnMenuClickListener onMenuClickListener) {
        this.onMenuClickListener = onMenuClickListener;
    }

    //构造函数
    public ArcMenu(Context context) {
        this(context, null);
    }

    public ArcMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // dp convert to px, 设置默认的半径
        mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mRadius, getResources().getDisplayMetrics());
        //获取值
        TypedArray typedValue = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ArcMenu, defStyle, 0);
        int tvCount = typedValue.getIndexCount();
        //处理位置和半径
        for (int i = 0; i < tvCount; i++) {
            int attr = typedValue.getIndex(i);
            switch (attr) {
                case R.styleable.ArcMenu_position:
                    int val_position = typedValue.getInt(attr, 0);
                    switch (val_position) {
                        case 0:
                            mPosition = Position.LEFT_TOP;
                            break;
                        case 1:
                            mPosition = Position.RIGHT_TOP;
                            break;
                        case 2:
                            mPosition = Position.LEFT_BOTTOM;
                            break;
                        case 3:
                            mPosition = Position.RIGHT_BOTTOM;
                            break;
                    }
                    break;
                case R.styleable.ArcMenu_radius:
                    // dp convert to px
                    mRadius = typedValue.getDimensionPixelSize(attr,
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f,
                                    getResources().getDisplayMetrics()));
                    break;
            }
        }
        typedValue.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        System.out.println("测量" + getChildCount());

        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            System.out.println("放入onLayout");
            //最后一个是按钮，放入按钮
            layoutButton();

            int childCount = getChildCount();

            //除了最后的倒数2,3是登录的之外，其余都是菜单项
            //一个夹角的度数
            double rad = Math.PI / 2 / (childCount - 4);
            for (int i = 0; i < childCount - 3; i++) {
                View child = getChildAt(i);
                child.setVisibility(GONE);

                //在左上排布是顺时针
                int cl = (int) (mRadius * Math.cos(rad * i));
                int ct = (int) (mRadius * Math.sin(rad * i));

                int cwidth = child.getMeasuredWidth();
                int cheight = child.getMeasuredHeight();

                if (mPosition == Position.RIGHT_TOP || mPosition == Position.RIGHT_BOTTOM)
                    cl = getMeasuredWidth() - cwidth - cl;

                if (mPosition == Position.LEFT_BOTTOM || mPosition == Position.RIGHT_BOTTOM)
                    ct = getMeasuredHeight() - cheight - ct;

                child.layout(cl, ct, cl + cwidth, ct + cheight);
            }

            //对最后的倒数2，3登录子菜单项做处理
            //偏移和上方最后一个一样， 为childcout - 4
            for (int i = 0; i < 2; i++) {
                View child = getChildAt(childCount + i - 3);
                child.setVisibility(GONE);

                //控件自己的大小
                int cwidth = child.getMeasuredWidth();
                int cheight = child.getMeasuredHeight();

                //在左上排布最下面
                //右边填充和母菜单一样
                //为了缩小之间的距离，有一些度量的不同
                int cl = (int) (mRadius * Math.cos(rad * (childCount - 4)));
                //倒数第3个为子菜单的第一个，倒数第2个为子菜单第二个
                int ct = (int) (mRadius * Math.sin(rad * (childCount - 4)) * (i + 2)) - (i + 1) * cwidth / 2;

                if (mPosition == Position.RIGHT_TOP || mPosition == Position.RIGHT_BOTTOM)
                    cl = getMeasuredWidth() - cwidth - cl;

                if (mPosition == Position.LEFT_BOTTOM || mPosition == Position.RIGHT_BOTTOM)
                    ct = getMeasuredHeight() - cheight - ct;

                child.layout(cl, ct, cl + cwidth, ct + cheight);
            }
        }
    }

    //放入按钮
    private void layoutButton() {
        System.out.println("放入按钮");

        View mButton = getChildAt(getChildCount() - 1);
        mButton.setId(ADD_BT_ID);
        mButton.setOnClickListener(this);

        int l = 0;
        int t = 0;
        int width = mButton.getMeasuredWidth();
        int heigh = mButton.getMeasuredHeight();

        switch (mPosition) {
            case LEFT_TOP:
                break;
            case RIGHT_TOP:
                l = getMeasuredWidth() - width;
                break;
            case LEFT_BOTTOM:
                t = getMeasuredHeight() - heigh;
                break;
            case RIGHT_BOTTOM:
                l = getMeasuredWidth() - width;
                t = getMeasuredHeight() - heigh;
                break;

        }
        mButton.layout(l, t, l + width, t + heigh);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        if (bt_add == null)
            bt_add = findViewById(ADD_BT_ID);
        //按钮旋转动画
        if (!mStatus) {
            rotateView(bt_add, 0f, 45f, 300);
            System.out.println("正向打开旋转");
        } else {
            rotateView(bt_add, 45f, 0f, 300);
            System.out.println("逆向关闭旋转");
        }

        //子控件动画
        toggleMenu(200);
    }

    //按钮旋转动画
    private void rotateView(View view, float fromDegrees, float toDegrees, int durationMills) {
        System.out.println("旋转动画");
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(view, "rotation", fromDegrees, toDegrees);
        rotateAnim.setDuration(durationMills);
        rotateAnim.start();
        /*RotateAnimation rotateAnimation = new RotateAnimation(fromDegrees, toDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(durationMills);
        rotateAnimation.setFillAfter(true);
        view.startAnimation(rotateAnimation);*/
    }

    //子控件动画
    private void toggleMenu(int durationMills) {
        int count = getChildCount();
        //一个夹角的度数
        double rad = Math.PI / 2 / (count - 4);
        //子菜单
        for (int i = 0; i < count - 3; i++) {
            final View child = getChildAt(i);
            child.setVisibility(View.VISIBLE);

            int cl = (int) (mRadius * Math.cos(rad * i));
            int ct = (int) (mRadius * Math.sin(rad * i));

            //x,y状态参量，用于计算菜单移动的距离和位置是开始时还是结束时
            int mFlagX = 1;
            int mFlagY = 1;

            if (mPosition == Position.LEFT_TOP || mPosition == Position.LEFT_BOTTOM)
                mFlagX = -1;

            if (mPosition == Position.LEFT_TOP || mPosition == Position.RIGHT_TOP)
                mFlagY = -1;

            AnimationSet animationSet = new AnimationSet(true);
            Animation animation = null;

            /*AnimatorSet animationSet = new AnimatorSet();
            ObjectAnimator transXAnim = null;
            ObjectAnimator transYAnim = null;*/

            //没有打开
            //移动动画
            if (!mStatus) {
                System.out.println("没有打开子控件旋转动画");
                /*transXAnim = ObjectAnimator.ofFloat(child, "translationX", mFlagX * cl - mFlagX * 30, 0);
                transYAnim = ObjectAnimator.ofFloat(child, "translationY", mFlagY * ct - mFlagY * 30, 0);*/
                animationSet.setInterpolator(new OvershootInterpolator(2));
                animation = new TranslateAnimation(mFlagX * cl - mFlagX * 30, 0, mFlagY * ct - mFlagY * 30, 0);
                child.setClickable(true);
                child.setFocusable(true);
            } else { //已经打开
                System.out.println("已经打开子控件旋转动画");
                /*transXAnim = ObjectAnimator.ofFloat(child, "translationX", 0, mFlagX * cl - mFlagX * 30);
                transYAnim = ObjectAnimator.ofFloat(child, "translationY", 0, mFlagY * ct - mFlagY * 30);*/
                animation = new TranslateAnimation(0, mFlagX * cl - mFlagX * 30, 0, mFlagY * ct - mFlagY * 30);
                child.setFocusable(false);
                child.setClickable(false);
            }

            //设置动画参数
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (!mStatus) {
                        child.setVisibility(GONE);
                    }
                }
            });
            animation.setFillAfter(true);
            animation.setDuration(durationMills);

            //设置延时
            animation.setStartOffset((i * 100) / (count - 1));
            /*transXAnim.setStartDelay((i * 100) / (count - 1));
            transYAnim.setStartDelay((i * 100) / (count - 1));*/

            //设置旋转
            RotateAnimation rotate = new RotateAnimation(0, 270,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setFillAfter(true);
            rotate.setDuration(durationMills);

            /*ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(child, "rotation", 0, 270);
            animationSet.setDuration(durationMills);
            animationSet.setStartDelay((i * 100) / (count - 1));
            animationSet.play(transXAnim).with(transYAnim).with(rotateAnim);
            animationSet.start();*/
            //rotate.start();
            animationSet.addAnimation(rotate);
            animationSet.addAnimation(animation);
            child.startAnimation(animationSet);

            final int index = i;
            child.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onMenuClickListener != null) {
                        onMenuClickListener.OnClik();
                    }
                    menuItemClik(index);
                    changeStatus();
                }
            });
        }
        changeStatus();
    }

    //改变status
    private void changeStatus() {
        mStatus = mStatus ? false : true;
    }

    //点击菜单项后的处理
    private void menuItemClik(int index) {
        for (int i = 0; i < getChildCount() - 3; i++) {
            View child = getChildAt(i);
            if (i == index) {
                child.startAnimation(menuBig(300));
            } else {
                child.startAnimation(menuSmall(300));
            }
            System.out.println("点击菜单后的处理");
            child.setFocusable(false);
            child.setClickable(false);
        }
        //旋转按钮
        if (mStatus) {
            rotateView(bt_add, 45f, 0f, 300);
            System.out.println("逆向关闭旋转");
        }
    }

    //点击菜单后变大动画
    private Animation menuBig(int durationMills) {
        AnimationSet animationSet = new AnimationSet(true);

        Animation animation = new ScaleAnimation(1, 2, 1, 2,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        Animation animation1 = new AlphaAnimation(1, 0);
        animationSet.setFillAfter(true);
        animationSet.setDuration(durationMills);
        animationSet.addAnimation(animation);
        animationSet.addAnimation(animation1);
        return animationSet;
    }

    //其他的缩小消失
    private Animation menuSmall(int durationMills) {
        Animation animation = new ScaleAnimation(1, 0, 1, 0,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setFillAfter(true);
        animation.setDuration(durationMills);
        return animation;
    }

    //用于点击空白时，外面的类调用关闭菜单的方法
    public void changeArcMenu() {
        if (bt_add == null)
            bt_add = findViewById(ADD_BT_ID);
        //按钮旋转动画
        if (mStatus) {
            rotateView(bt_add, 45f, 0f, 300);
            System.out.println("逆向关闭旋转");
            //子控件动画
            toggleMenu(200);
        }
    }
}
