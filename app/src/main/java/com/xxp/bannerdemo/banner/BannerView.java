package com.xxp.bannerdemo.banner;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xxp.bannerdemo.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 钟大爷 on 2017/2/23.
 */

public class BannerView extends RelativeLayout {

    private final String TAG = "BannerView";
    private ViewPager viewPager;
    //轮播图的数量
    private int viewPagerCount ;
    //指示器的小圆点数组
    private Point[] mPointArray;
    //开始自动滑动
    private final int START_SCROLL = 0x11;

    private OnPagerChange onPagerChange;

    private final int DEFAULT_EMPTY_COLOR = Color.CYAN;
    private final int DEFAULT_FULL_COLOR = Color.WHITE;
    private final int DEFAULT_SCROLL_TIME = 5000;
    private final int DEFAULT_BANNER_COUNT = 5;
    private final int DEFAULT_SCROLL_DURATION = 2000;

    //轮播的Duration
    private int scroll_duration;
    //轮播加载时的图片
    private int loading_image;
    //自动轮播的时间
    private int scroll_time;
    //小圆点选中的颜色
    private int point_empty_color;

    //小圆点没选中的颜色
    private int point_full_color;

    //停止制动滑动
    private boolean stop = false;
    private List<ImageView> bannerView;
    private LinearLayout pointGroup;
    private Activity mActivity;

    private BannerScroll mScroller;

    //必要的接口
    private IBannerPrepare mIBannerPrepare;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == START_SCROLL) {
                //开始轮播
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        }
    };

    public BannerView(Context context) {
        this(context, null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.e(TAG, "BannerView: " + context);
        //解析自定义的属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BannerView);
        viewPagerCount = typedArray.getInt(R.styleable.BannerView_banner_count,DEFAULT_BANNER_COUNT);
        //空点
        point_empty_color = typedArray.getColor(R.styleable.BannerView_point_empty_color, DEFAULT_EMPTY_COLOR);
        //满点
        point_full_color = typedArray.getColor(R.styleable.BannerView_point_full_color, DEFAULT_FULL_COLOR);
        //切换时间
        scroll_time = typedArray.getInt(R.styleable.BannerView_auto_scroll_time, DEFAULT_SCROLL_TIME);
        //加载的图片
        loading_image = typedArray.getInt(R.styleable.BannerView_loading_image, R.drawable.bg_loading);
            //轮播Duration
        scroll_duration = typedArray.getInt(R.styleable.BannerView_scroll_speed,DEFAULT_SCROLL_DURATION);
    }

    public int getViewPagerCount() {
        return viewPagerCount;
    }

    //自动轮播
    private void autoScroll() {
        mScroller = new BannerScroll(getContext());
        mScroller.setmDuration(scroll_duration);
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            field.set(viewPager, mScroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (!stop) {
                    mHandler.sendEmptyMessage(START_SCROLL);
                }
            }
        };
        timer.schedule(timerTask, 0, scroll_time);
    }


    private void initView() {
        //初始化viewPager
        initViewPager();
        //圆点指示器
        initPoint();
        //进行布局
        bannerlayout();
        //设置到中间Item(滑无边界也)
        viewPager.setCurrentItem(Integer.MAX_VALUE / 2);
    }

    public void setupIbanner(IBannerPrepare iBannerPrepare) {
        this.mIBannerPrepare = iBannerPrepare;
        if (mIBannerPrepare != null) {
            //得到Activity
            mActivity = mIBannerPrepare.getActivity();
            //立马注册lifecycleCallback
            mActivity
                    .getApplication()
                    .registerActivityLifecycleCallbacks(lifecycleCallbacks);
            //外传bannerViews
            mIBannerPrepare.setBannerViews(bannerView);
        }
    }

    //设置切换滑动的时间
    public void setScrollDuration(int duration) {
        mScroller.setmDuration(duration);
    }

    private void bannerlayout() {
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        addView(viewPager, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(pointGroup, params);
    }


    //往里面加图片,并设置事件监听
    private void initViewPager() {
        viewPager = new ViewPager(getContext());
        bannerView = new ArrayList<>();
        for (int i = 0; i < viewPagerCount; i++) {
            ImageView image = new ImageView(getContext());
            image.setImageResource(loading_image);
            image.setScaleType(ImageView.ScaleType.FIT_XY);
            bannerView.add(image);
            //设置点击事件监听
            final int position = i;
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnClickListener != null) {
                        mOnClickListener.onClick(position);
                    }
                }
            });
        }

        viewPager.setAdapter(new BannerAdapter(bannerView));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.e(TAG, "onPageSelected: ");
                int newPosition = position % viewPagerCount;
                for (int i = 0; i < viewPagerCount; i++) {
                    mPointArray[newPosition].setShow(true);
                    if (newPosition != i) {
                        mPointArray[i].setShow(false);
                    }
                }

                if (null != onPagerChange) {
                    onPagerChange.onPagerChange(newPosition);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    private void initPoint() {
        pointGroup = new LinearLayout(getContext());
        mPointArray = new Point[viewPagerCount];
        for (int i = 0; i < viewPagerCount; i++) {
            Point point = new Point(getContext());
            point.setLayoutParams(new LinearLayout.LayoutParams(50, 40));
            point.setPadding(10, 0, 10, 0);
            mPointArray[i] = point;
            if (i == 0) {
                point.setShow(true);
            } else {
                point.setShow(false);
            }
            pointGroup.addView(point);
        }


    }


    public interface OnPagerChange {
        void onPagerChange(int position);
    }

    public void setOnPagerChange(OnPagerChange onPagerChange) {
        this.onPagerChange = onPagerChange;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
        autoScroll();
    }

    //小圆点
    private class Point extends View {

        Paint paint;
        boolean isShow;

        public boolean isShow() {
            return isShow;
        }

        public void setShow(boolean show) {
            isShow = show;
            invalidate();
        }

        public Point(Context context) {
            super(context);
            initPaint();
        }

        private void initPaint() {
            paint = new Paint();
            paint.setAntiAlias(true);

        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (isShow) {
                paint.setColor(point_full_color);
            } else {
                paint.setColor(point_empty_color);
            }
            canvas.drawCircle(7, 7, 7, paint);
        }
    }


    //监听Activity的生命周期(优化考虑)
    Application.ActivityLifecycleCallbacks lifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

            if (BannerView.this.mActivity.equals(activity)) {
                Log.e(TAG, "onActivityResumed: ");
                stop = false;
            }

        }

        @Override
        public void onActivityPaused(Activity activity) {
            if (BannerView.this.mActivity.equals(activity)) {
                Log.e(TAG, "onActivityPaused: ");
                stop = true;
            }

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };

    public Activity getActivity() {
        return mActivity;
    }

    //是否是停止状态
    public boolean isStop() {
        return stop;
    }

    //控制停止状态
    public void setStop(boolean stop) {
        this.stop = stop;
    }


    //点击事件.
    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void onClick(int position);
    }

    public void setBannerOnclickListener(OnClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;

    }

}
