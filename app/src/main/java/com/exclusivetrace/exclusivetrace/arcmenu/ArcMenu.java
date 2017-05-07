package com.exclusivetrace.exclusivetrace.arcmenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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

    //方位的枚举型
    private enum Position {
        LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM;
    }

    //赋给按钮的id值
    private final int ADD_BT_ID = 0;

    //按钮
    private View bt_add;

    //菜单半径
    private int mRadius = 100;

    //菜单的角度
    private double angle = Math.PI / 2;

    //按钮打开状态
    public Boolean mStatus_arcmenu = false;

    //登录子菜单打开状态
    private Boolean mStatus_login = false;

    //接口变量
    private OnMenuClickListener onMenuClickListener;

    //点击菜单的点击事件接口
    public interface OnMenuClickListener {
        void OnClik(int index);
    }

    //暴露给外面的设置接口变量的方法
    public void setOnMenuClickListener(OnMenuClickListener onMenuClickListener) {
        this.onMenuClickListener = onMenuClickListener;
    }

    //接口变量
    private OnBtClose onBtClose;

    //按钮结束转动时调用的接口
    public interface OnBtClose {
        void OnClose();
    }

    //暴露给外面的设置接口变量的方法
    public void setOnBtClose(OnBtClose onBtClose) {
        this.onBtClose = onBtClose;
    }

    //接口变量
    private OnBtOn onBtOn;

    //按钮开始转动时调用的接口
    public interface OnBtOn {
        void OnOn();
    }

    //暴露给外面的设置接口变量的方法
    public void setOnBtOn(OnBtOn onBtOn) {
        this.onBtOn = onBtOn;
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
                case R.styleable.ArcMenu_angle:
                    angle = typedValue.getInt(attr, 90) / 180;
                    break;
            }
        }
        typedValue.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        System.out.println("测量" + getChildCount());

        /*angle = Math.PI * angle / (getChildCount() - 4);
        System.out.println("angle1:" + angle);*/
        angle = Math.PI * (77 / 180.0) / (getChildCount() - 4);
        System.out.println("angle:" + angle);

        measureChildren(widthMeasureSpec, heightMeasureSpec);

        /*for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        }*/
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    //放入控件
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            System.out.println("放入onLayout");
            //最后一个是按钮，放入按钮
            onLayoutButton();

            //放入arcmenu菜单
            onLayoutArcmenu();

            //放入login子菜单
            onLayoutLogin();
        }
    }

    //放入arcmenu菜单
    private void onLayoutArcmenu() {
        int childCount = getChildCount();

        //除了第1，2,是登录的子菜单和最后一个按钮之外，其余都是菜单项
        for (int i = 2; i < childCount - 1; i++) {
            View child = getChildAt(i);
            child.setVisibility(GONE);

            //在左上排布是顺时针
            int cl = (int) (mRadius * Math.cos(angle * (i - 2)));
            int ct = (int) (mRadius * Math.sin(angle * (i - 2)));

            int cwidth = child.getMeasuredWidth();
            int cheight = child.getMeasuredHeight();

            if (mPosition == Position.RIGHT_TOP || mPosition == Position.RIGHT_BOTTOM)
                cl = getMeasuredWidth() - cwidth - cl;

            if (mPosition == Position.LEFT_BOTTOM || mPosition == Position.RIGHT_BOTTOM)
                ct = getMeasuredHeight() - cheight - ct;

            child.layout(cl, ct, cl + cwidth, ct + cheight);
        }
    }

    //放入login子菜单
    private void onLayoutLogin() {
        int childCount = getChildCount();
        //对第1，2登录子菜单项做处理
        //偏移的角度和上方最后一个一样， 为childcout - 4
        //因为控件的上下层关系，1子菜单是最外面的
        for (int i = 0; i < 2; i++) {
            View child = getChildAt(i);
            child.setVisibility(GONE);

            //控件自己的大小
            int cwidth = child.getMeasuredWidth();
            int cheight = child.getMeasuredHeight();

            //在左上排布最下面
            //右边填充和母菜单一样
            //为了缩小之间的距离，进行了一些处理
            int cl = (int) (Math.cos(angle * (childCount - 4)) * (mRadius * (3 - i) - (2 - i) * cwidth / 1.3));
            int ct = (int) (Math.sin(angle * (childCount - 4)) * (mRadius * (3 - i) - (2 - i) * cheight / 1.3));

            if (mPosition == Position.RIGHT_TOP || mPosition == Position.RIGHT_BOTTOM)
                cl = getMeasuredWidth() - cwidth - cl;

            if (mPosition == Position.LEFT_BOTTOM || mPosition == Position.RIGHT_BOTTOM)
                ct = getMeasuredHeight() - cheight - ct;

            child.layout(cl, ct, cl + cwidth, ct + cheight);
        }
    }

    //放入按钮和点击事件的监听
    private void onLayoutButton() {
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

    //按钮的点击事件
    @Override
    public void onClick(View v) {
        if (onMenuClickListener != null) {
            onMenuClickListener.OnClik(getChildCount() - 1);
        }
        //主按钮
        if (bt_add == null)
            bt_add = findViewById(ADD_BT_ID);
        //按钮旋转动画
        if (!mStatus_arcmenu) {
            rotateView(bt_add, 0f, 45f, 300);
            System.out.println("正向打开旋转");
        } else {
            rotateView(bt_add, 45f, 0f, 300);
            System.out.println("逆向关闭旋转");
        }

        //arcmenu菜单动画
        toggleMenu(200);
        //如果在登录子菜单打开的情况下，另外的动画
        if (mStatus_login)
            loginAnim(100, true);
    }

    //按钮旋转动画
    private void rotateView(View view, float fromDegrees, float toDegrees, int durationMills) {
        System.out.println("旋转动画");
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(view, "rotation", fromDegrees, toDegrees);
        rotateAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                //super.onAnimationStart(animation);
                System.out.println("按钮动画开始监听");
                if (!mStatus_arcmenu)
                    onBtOn.OnOn();
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                //这里状态变化了
                if (!mStatus_arcmenu)
                    onBtClose.OnClose();
            }
        });

        rotateAnim.setDuration(durationMills);
        rotateAnim.start();
        /*RotateAnimation rotateAnimation = new RotateAnimation(fromDegrees, toDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(durationMills);
        rotateAnimation.setFillAfter(true);
        view.startAnimation(rotateAnimation);*/
    }

    //arcmenu菜单动画和点击事件监听
    private void toggleMenu(int durationMills) {
        final int count = getChildCount();
        //子菜单
        //除了第1,2个，和最后一个
        for (int i = 2; i < count - 1; i++) {
            final View child = getChildAt(i);
            child.setVisibility(View.VISIBLE);

            int cl = (int) (mRadius * Math.cos(angle * (i - 2)));
            int ct = (int) (mRadius * Math.sin(angle * (i - 2)));

            //x,y状态参量，用于计算菜单移动的距离和位置
            int mFlagX = 1;
            int mFlagY = 1;

            if (mPosition == Position.LEFT_TOP || mPosition == Position.LEFT_BOTTOM)
                mFlagX = -1;

            if (mPosition == Position.LEFT_TOP || mPosition == Position.RIGHT_TOP)
                mFlagY = -1;

            AnimationSet animationSet = new AnimationSet(true);
            Animation animation = null;

            //没有打开
            //移动动画
            //为了让控件消失在按钮中间，有一点位置的偏移
            if (!mStatus_arcmenu) {
                System.out.println("没有打开子控件旋转动画");
                animationSet.setInterpolator(new OvershootInterpolator(2));
                animation = new TranslateAnimation(mFlagX * cl - mFlagX * 50, 0, mFlagY * ct - mFlagY * 50, 0);
                child.setClickable(true);
                child.setFocusable(true);
            } else { //已经打开
                System.out.println("已经打开子控件旋转动画");
                animation = new TranslateAnimation(0, mFlagX * cl - mFlagX * 50, 0, mFlagY * ct - mFlagY * 50);
                child.setFocusable(false);
                child.setClickable(false);
            }

            //设置动画参数
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    System.out.println("动画结束监听");
                    if (!mStatus_arcmenu) {
                        System.out.println("控件消失");
                        child.setVisibility(GONE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animation.setFillAfter(true);
            animation.setDuration(durationMills);

            //设置延时
            animation.setStartOffset(((i - 2) * 100) / (count - 1));

            //设置旋转
            RotateAnimation rotate = new RotateAnimation(0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setFillAfter(true);
            rotate.setDuration(durationMills);

            animationSet.addAnimation(rotate);
            animationSet.addAnimation(animation);
            child.startAnimation(animationSet);

            final int index = i;
            child.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuItemClik(index);
                    if (onMenuClickListener != null) {
                        onMenuClickListener.OnClik(index);
                    }
                }
            });
        }
        changeArcMenuStatus();
    }

    //login登录的子菜单动画事件和点击事件监听
    private void loginAnim(int durationMills, Boolean whenOntoClose) {
        //子菜单
        for (int i = 0; i < 2; i++) {
            final View child = getChildAt(i);
            child.setVisibility(View.VISIBLE);

            //为了缩小之间的距离，有一些度量的不同
            int cl = (int) (Math.cos(angle * (getChildCount() - 4)) * (mRadius - child.getMeasuredWidth() / 1.3) * (2 - i));
            //1是最外面的
            //第1个为子菜单的第一个，第2个为子菜单第二个
            int ct = (int) (Math.sin(angle * (getChildCount() - 4)) * (mRadius - child.getMeasuredHeight() / 1.3) * (2 - i));

            //x,y状态参量，用于计算菜单移动的距离和位置
            int mFlagX = 1;
            int mFlagY = 1;

            if (mPosition == Position.LEFT_TOP || mPosition == Position.LEFT_BOTTOM)
                mFlagX = -1;

            if (mPosition == Position.LEFT_TOP || mPosition == Position.RIGHT_TOP)
                mFlagY = -1;

            AnimationSet animationSet = new AnimationSet(true);
            Animation animation = null;

            //没有打开
            //移动动画
            if (!mStatus_login) {
                System.out.println("没有打开登陆子控件，旋转动画");
                animationSet.setInterpolator(new OvershootInterpolator(2));
                animation = new TranslateAnimation(mFlagX * cl, 0, mFlagY * ct, 0);
                child.setClickable(true);
                child.setFocusable(true);
            } else { //已经打开
                System.out.println("已经打开登陆子控件，旋转动画");
                //在打开状态直接关闭所有
                if (whenOntoClose) {
                    /************
                     * 这里cl有问题，左边右边偏移不对称，需手动改+ -
                     **************/
                    cl = cl + (int) (Math.cos(angle * (getChildCount() - 4)) * mRadius) + 50 * mFlagX;
                    ct = ct + (int) (Math.sin(angle * (getChildCount() - 4)) * mRadius) - 50 * mFlagY;
                    durationMills *= 2;
                    animation = new TranslateAnimation(0, mFlagX * cl, 0, mFlagY * ct);
                } else {
                    //正常关闭
                    animation = new TranslateAnimation(0, mFlagX * cl, 0, mFlagY * ct);
                    //设置延时
                    animationSet.setStartOffset(i == 1 ? durationMills : 0);
                    child.setFocusable(false);
                    child.setClickable(false);
                }
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
                    System.out.println("登录子菜单动画结束监听");
                    if (!mStatus_login) {
                        System.out.println("登录子菜单控件消失");
                        child.setVisibility(GONE);
                    }
                }
            });
            animation.setFillAfter(true);


            //设置旋转
            RotateAnimation rotate = new RotateAnimation(0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setFillAfter(true);

            animationSet.addAnimation(rotate);
            animationSet.addAnimation(animation);
            //延时
            animationSet.setDuration(durationMills * (2 - i));

            child.startAnimation(animationSet);

            final int index = i;
            child.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuItemClik(index);
                    if (onMenuClickListener != null) {
                        onMenuClickListener.OnClik(index);
                    }
                }
            });
        }
        changeLoginStatus();
    }

    //点击菜单项后的处理
    private void menuItemClik(int index) {
        //点击登录，打开登录子菜单
        if (index == getChildCount() - 2) {
            //如果是登录状态,就打开菜单
            if (true) {
                loginAnim(150, false);
            }
        } else {
            //点击的不是登录按钮
            //对除了登录子菜单进行处理
            for (int i = 2; i < getChildCount() - 1; i++) {
                View child = getChildAt(i);
                if (i == index) {
                    //点击的变大
                    child.startAnimation(menuBig(200));
                } else {
                    //没点击的缩小
                    child.startAnimation(menuSmall(200));
                }

                System.out.println("点击菜单后的处理");
                child.setFocusable(false);
                child.setClickable(false);
            }

            //当点击的不是登录时,对登录的子菜单处理
            //打开状态，被点击就有动画
            if (mStatus_login) {
                for (int i = 0; i < 2; i++) {
                    View child = getChildAt(i);
                    if (i == index) {
                        //点击的变大
                        child.startAnimation(menuBig(200));
                    } else {
                        //没点击的缩小
                        child.startAnimation(menuSmall(200));
                    }

                    System.out.println("点击login菜单后的处理");
                    child.setFocusable(false);
                    child.setClickable(false);
                }
                changeLoginStatus();
            }

            //当点击的不是登录时，旋转按钮,并改变status的值
            if (mStatus_arcmenu && index != getChildCount() - 2) {
                System.out.println("逆向关闭旋转");
                rotateView(bt_add, 45f, 0f, 300);
                changeArcMenuStatus();
            }
        }
    }

    //改变login按钮的状态
    private void changeLoginStatus() {
        System.out.println("改变login按钮的状态");
        mStatus_login = mStatus_login ? false : true;
    }

    //改变status
    public void changeArcMenuStatus() {
        System.out.println("改变statusarcmenu");
        mStatus_arcmenu = mStatus_arcmenu ? false : true;
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

    //外面的类调用开关菜单的方法
    public void changeArcMenu() {
        if (bt_add == null)
            bt_add = findViewById(ADD_BT_ID);
        //按钮旋转动画
        //关闭按钮
        if (mStatus_arcmenu) {
            rotateView(bt_add, 45f, 0f, 300);
            System.out.println("逆向关闭旋转");
            //子控件动画
            toggleMenu(200);
            //登录子菜单动画
            if (mStatus_login)
                loginAnim(100, true);
        } else {
            //打开按钮
            rotateView(bt_add, 0f, 45f, 300);
            System.out.println("正向打开旋转");
            //子控件动画
            toggleMenu(200);
        }
    }
}
