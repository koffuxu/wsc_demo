package com.example.wscdemo.activity;

import android.view.View;

import com.example.wscdemo.R;
import com.example.wscdemo.base.BaseActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected String getTitleText() {
        return "首页";
    }

    @Override
    protected void initData() {
        setToolbarLeftIcon(0);

    }


    public void startHotspot(View view) {
        pushActivity(HotSpotActivity.class);
    }
    public void startConnectSpot(View view){
        pushActivity(DeviceActivity.class);
    }
}
