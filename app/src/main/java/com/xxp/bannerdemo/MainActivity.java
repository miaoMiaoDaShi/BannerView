package com.xxp.bannerdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

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
            public void setBannerViews(List<ImageView> views) {
                Log.e("图片", "setBannerViews: "+views.size() );
                //实例
                views.get(0).setImageResource(R.mipmap.banner_1);
                views.get(1).setImageResource(R.mipmap.banner_2);
                views.get(2).setImageResource(R.mipmap.banner_3);
                views.get(3).setImageResource(R.mipmap.banner_2);
                views.get(4).setImageResource(R.mipmap.banner_1);
            }
        });
        //设置点击事件
        bannerView.setBannerOnclickListener(new BannerView.BannerListener() {
            @Override
            public void onClick(int position) {

            }
        });
    }
}
