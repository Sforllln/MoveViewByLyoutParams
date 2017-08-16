package com.zs.moveviewbylyoutparams;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import java.lang.reflect.Field;

import static android.util.TypedValue.applyDimension;

public class MainActivity extends AppCompatActivity {

    Button mResetBtn;
    ImageView mMoveIv;
    SeekBar mChangeSizeSk;

    private boolean isFirstWindowFocusChanged = false; //第一次 视图真正的加载完成标志

    private int screenWidth; //获取屏幕的宽度

    private int screenHeight;   //获取屏幕的高度

    private int[] moveViewDefaultLocation;  //移动改的view默认的位置信息.

    //如果有这两个东西就需要获取一下
    private int statusBarHeight;
    private int contentTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initView();
        bindEvent();
        getScreenWidthHeight();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        contentTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        statusBarHeight = getStatusBarHeight();
        screenHeight = screenHeight - contentTop - statusBarHeight;  // 重新就算屏幕的高度.
        if (hasFocus && !isFirstWindowFocusChanged) {
            isFirstWindowFocusChanged = true;
            moveViewDefaultLocation = getViewDefaultLocation(mMoveIv);
        }
    }

    private void bindEvent() {
        mChangeSizeSk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                zoomOutViewSize(i, mMoveIv);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reSetLocation(mMoveIv, moveViewDefaultLocation[0], moveViewDefaultLocation[1], moveViewDefaultLocation[4], moveViewDefaultLocation[5]);
            }
        });
    }

    private void initView() {
        mMoveIv = (ImageView) findViewById(R.id.moveImage_iv);
        mChangeSizeSk = (SeekBar) findViewById(R.id.changeSize_sk);
        mResetBtn = (Button) findViewById(R.id.reSetLocation_btn);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("MainActivity", "maxX: " + screenWidth + "  maxY: " + screenHeight + "  X: " + event.getX() + " " + "  Y: " + event.getY());
        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                moveViewByLayout(mMoveIv, rawX, rawY);
                break;
        }
        return true;
    }

    //设置View的宽高
    private void zoomOutViewSize(int progress, View view) {
        //progress /5 是保证VIEW不会变形 先改变其位置,再进行方法缩小
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
        if (lp.leftMargin + lp.width >= screenWidth) {
            lp.setMargins(lp.leftMargin - progress / 5, lp.topMargin, 0, 0);
        }
        if (lp.bottomMargin + lp.width >= screenWidth) {
            lp.setMargins(lp.leftMargin, lp.topMargin + progress / 5, 0, 0);
        }
        if (lp.rightMargin + lp.width >= screenHeight) {
            lp.setMargins(lp.leftMargin + progress / 5, lp.topMargin, 0, 0);
        }
        if (lp.topMargin + lp.width >= screenHeight) {
            lp.setMargins(lp.leftMargin, lp.topMargin - progress / 5, 0, 0);
        }

        lp.width = (int) dip2px(this, progress);
        lp.height = (int) dip2px(this, progress);
        view.setLayoutParams(lp);

    }

    /**
     * 采用layoutPaeams的方式
     * 1.方法缩小可在原地,但是会造成view变形.
     **/
    private void moveViewByLayout(View view, int rawX, int rawY) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(view.getWidth(), view.getHeight());

        int currentRawY = rawY - (contentTop + statusBarHeight);

        int width = view.getWidth();
        int height = view.getHeight();

        int left = rawX - width / 2;
        int top = currentRawY - height / 2;
        Log.d("tag2", "left: " + left + "---" + "top: " + top + "--" + contentTop + "==" + statusBarHeight);


        int right = rawX + width / 2;
        int bottom = currentRawY + height / 2;
        Log.d("tag2", "right: " + right + "---" + "bottom: " + bottom + "----maxX:" + screenWidth + "---maxY" + screenHeight);

        //设置可以移动的位置
        if (left < 0) left = 0;
        if (top < 0) top = 0;
        if (right > screenWidth) left = screenWidth - width;
        if (bottom > screenHeight)
            top = screenHeight - height;
        //设置位置
        lp.setMargins(left, top, 0, 0);
        view.setLayoutParams(lp);
//        view.layout(left, top, 0,0);
    }


    private void reSetLocation(View view, int l, int t, int w, int h) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.width = w;
        params.height = h;
        params.setMargins(l, t, params.rightMargin, params.bottomMargin);
        view.setLayoutParams(params);


//        view.layout(l, t, r, b);
        mChangeSizeSk.setProgress(100);
    }

    public static float dip2px(Context context, float dipValue) {
        DisplayMetrics mDisplayMetrics = context.getResources().getDisplayMetrics();
        return applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, mDisplayMetrics);
    }


    private void getScreenWidthHeight() {
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
    }

    private int[] getViewDefaultLocation(View view) {
        int[] location = new int[6];
        location[0] = view.getLeft();
        location[1] = view.getTop();
        location[2] = view.getRight();
        location[3] = view.getBottom();
        location[4] = view.getWidth();
        location[5] = view.getHeight();
        return location;
    }

    private int getStatusBarHeight() {
        Class<?> c = null;

        Object obj = null;

        Field field = null;

        int x = 0, sbar = 0;

        try {

            c = Class.forName("com.android.internal.R$dimen");

            obj = c.newInstance();

            field = c.getField("status_bar_height");

            x = Integer.parseInt(field.get(obj).toString());

            sbar = this.getResources().getDimensionPixelSize(x);

        } catch (Exception e1) {

            e1.printStackTrace();

        }

        return sbar;
    }


}
