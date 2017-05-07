package com.exclusivetrace.exclusivetrace.welcome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Window;
import android.view.WindowManager;

import com.exclusivetrace.exclusivetrace.Map.MainActivity;
import com.exclusivetrace.exclusivetrace.R;

/**
 * Created by Administrator on 2017/5/4.
 */

public class Welcome extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取消标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.welcome);

        /*CountDownTimer有四个方法：onTick，onFinsh、cancel和start。
        其中前面两个是抽象方法，所以要重写一下。
        CountDownTimer构造器的两个参数分别是第一个参数表示总时间，第二个参数表示间隔时间。
        意思就是每隔xxx毫秒会回调一次方法onTick，然后xxx毫秒之后会回调onFinish方法。*/
        CountDownTimer timer = new CountDownTimer(0, 1000) {
            //运行时
            public void onTick(long millisUntilFinished) {
            }

            //结束时
            public void onFinish() {
                startActivity(new Intent(Welcome.this, MainActivity.class));
                finish();
            }
        }.start();
        ;
    }
}
