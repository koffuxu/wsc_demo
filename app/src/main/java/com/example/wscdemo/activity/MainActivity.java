package com.example.wscdemo.activity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.android.bbtmanager.BbtManager;
import com.example.wscdemo.R;
import com.example.wscdemo.base.BaseActivity;

public class MainActivity extends BaseActivity {
    private static final String TAG = "wsc";
    ToggleButton upEyeTb;
    ToggleButton downEyeTb;

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
        upEyeTb = (ToggleButton) findViewById(R.id.tb_up_eye);
        downEyeTb = (ToggleButton) findViewById(R.id.tb_down_eye);
        setToolbarLeftIcon(0);
        int value = BbtManager.getTestValue(1,2);
        Log.e("wsc_demo", "value is:"+value);
        getVolume();

        upEyeTb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    BbtManager.setLedStatus(1,0,0,0);
                }else {
                    BbtManager.setLedStatus(0,0,0,0);
                }
            }
        });

        downEyeTb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    BbtManager.setLedStatus(0,1,0,0);
                }else {
                    BbtManager.setLedStatus(0,0,0,0);
                }
            }
        });

    }




    public void startHotspot(View view) {
        pushActivity(HotSpotActivity.class);
    }
    public void startConnectSpot(View view){
        pushActivity(DeviceActivity.class);
    }
    public void setMouseOn(View view){
        BbtManager.setLedStatus(0,0,1,0);
    }
    public void setMouseOff(View view){
        BbtManager.setLedStatus(0,0,0,0);
    }
    public void setMouseBreathe(View view){
        BbtManager.setLedStatus(0,0,2,0);
    }


    private void getVolume(){

    }
}
