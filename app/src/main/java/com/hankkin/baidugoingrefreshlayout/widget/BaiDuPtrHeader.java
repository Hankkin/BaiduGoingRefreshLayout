package com.hankkin.baidugoingrefreshlayout.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.hankkin.baidugoingrefreshlayout.R;
import com.hankkin.baidugoingrefreshlayout.interfaces.PtrHeader;
import com.hankkin.baidugoingrefreshlayout.widget.config.RefreshState;

/**
 * 封装 header
 */
public class BaiDuPtrHeader extends LinearLayout implements PtrHeader {
    private static final String TAG = "BaiDuPtrHeader";
    private RefreshState mState = RefreshState.DONE;
    private int mContentHeight;

    //view
    private ImageView ivWheel1, ivWheel2;    //轮组图片组件
    private ImageView ivRider;  //骑手图片组件
    private ImageView ivSun, ivBack1, ivBack2;    //太阳、背景图片1、背景图片2

    //Anima
    private Animation wheelAnimation, sunAnimation;  //轮子、太阳动画
    private Animation backAnimation1, backAnimation2;    //两张背景图动画

    private Animation bickAnimation;

    public BaiDuPtrHeader(Context context) {
        this(context, null);
    }

    public BaiDuPtrHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaiDuPtrHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.headview, this, true);
        RelativeLayout headerView = (RelativeLayout) findViewById(R.id.header_root);
        //获取头布局图片组件
        ivRider = (ImageView) headerView.findViewById(R.id.iv_rider);
        ivSun = (ImageView) headerView.findViewById(R.id.ivsun);
        ivWheel1 = (ImageView) headerView.findViewById(R.id.wheel1);
        ivWheel2 = (ImageView) headerView.findViewById(R.id.wheel2);
        ivBack1 = (ImageView) headerView.findViewById(R.id.iv_back1);
        ivBack2 = (ImageView) headerView.findViewById(R.id.iv_back2);
        measureView(headerView);
        mContentHeight = headerView.getMeasuredHeight();
        setPadding(0, -getContentHeight(), 0, 0);
        buildAnima();
        Log.d(TAG, "height====  " + mContentHeight);
    }

    private void buildAnima() {
        //获取动画
        wheelAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.tip);
        sunAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.tip1);

        backAnimation1 = AnimationUtils.loadAnimation(getContext(), R.anim.a);
        backAnimation2 = AnimationUtils.loadAnimation(getContext(), R.anim.b);

       /* bickAnimation=new TranslateAnimation(Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0,Animation
                .RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,-0.05f);*/
        bickAnimation=new ScaleAnimation(1,1,1,1.1f,Animation.RELATIVE_TO_SELF,.5f,Animation.RELATIVE_TO_SELF,1f);
        bickAnimation.setRepeatCount(-1);
        bickAnimation.setRepeatMode(Animation.RESTART);
        bickAnimation.setDuration(300);
        bickAnimation.setInterpolator(new SinInterpolator());
    }

    //=============================================================ViewTool↓↓↓

    /**
     * 测量View
     */
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        }
        else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    /**
     * 开启动画
     */
    public void startAnim() {
        ivBack1.startAnimation(backAnimation1);
        ivBack2.startAnimation(backAnimation2);
        ivSun.startAnimation(sunAnimation);
        ivWheel1.startAnimation(wheelAnimation);
        ivWheel2.startAnimation(wheelAnimation);
        ivRider.startAnimation(bickAnimation);
    }

    /**
     * 关闭动画
     */
    public void stopAnim() {
        ivBack1.clearAnimation();
        ivBack2.clearAnimation();
        ivSun.clearAnimation();
        ivWheel1.clearAnimation();
        ivWheel2.clearAnimation();
        ivRider.clearAnimation();
    }

    //=============================================================CallBack↓↓↓

    @Override
    public void onReset(RefreshState state) {
        mState = state;
        setPadding(0, -getContentHeight(), 0, 0);
        stopAnim();
    }

    @Override
    public void onPullToRefresh(RefreshState state) {
        mState = state;
    }

    @Override
    public void onReleaseToRefresh(RefreshState state) {
        mState = state;
        startAnim();
    }

    @Override
    public void onRefreshing(RefreshState state) {
        mState = state;
    }

    @Override
    public int getContentHeight() {
        return mContentHeight;
    }

    @Override
    public RefreshState getState() {
        return mState;
    }

    //=============================================================Interpolator

    /**
     * 自定义插值器，使数值在一个周期内以sin函数变化
     */
    static class SinInterpolator extends LinearInterpolator {

        public SinInterpolator() {
        }


        @Override
        public float getInterpolation(float input) {
            return (float) Math.sin(input*Math.PI);
        }
    }
}
