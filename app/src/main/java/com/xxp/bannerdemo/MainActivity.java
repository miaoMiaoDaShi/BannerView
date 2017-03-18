package com.xxp.bannerdemo;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.xxp.bannerdemo.banner.BannerView;
import com.xxp.bannerdemo.banner.IBannerPrepare;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BannerView bannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bannerView = (BannerView) findViewById(R.id.vp_banner);

        bannerView.setupIbanner(new IBannerPrepare() {
            @Override
            public Activity getActivity() {
                return MainActivity.this;
            }

            //获取Banner中的View 你就可以设置图片拉
            @Override
            public void setBannerViews(List<View> views) {
                //实例
                views.get(0).setBackgroundColor(Color.BLUE);
                views.get(1).setBackgroundColor(Color.CYAN);
                views.get(2).setBackgroundColor(Color.YELLOW);
            }
        });
        //设置点击事件
        bannerView.setBannerOnclickListener(new BannerView.OnClickListener() {
            @Override
            public void onClick(int position) {
                Toast.makeText(MainActivity.this, "点击" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
