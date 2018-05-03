package com.example.wscdemo.activity;

import android.bbt.IBbtDaemonService;
import android.content.ComponentName;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.android.bbtmanager.BbtManager;
import com.example.wscdemo.R;
import com.example.wscdemo.base.BaseActivity;
import com.example.wscdemo.wifitools.WifiMgr;

import java.util.List;


public class MainActivity extends BaseActivity {
    private static final String TAG = "wsc";
    ToggleButton upEyeTb;
    ToggleButton downEyeTb;
    List<String> nullListtest;
    WifiMgr wifiMgr;

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
        Log.e(TAG, "value is:"+value);
        wifiMgr = new WifiMgr(this);
//        Log.e(TAG, "initData: exception:"+nullListtest.get(0) );
        getVolume();
        //defaultWiFi();

        upEyeTb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    Log.i(TAG, "onCheckedChanged: start play");
                    //startActivity();
                    BbtManager.setLedStatus(1,0,0,0);
                    playSound();
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

    private void playSound() {
        IBinder b = ServiceManager.getService("bbtdaemon");
        IBbtDaemonService service = IBbtDaemonService.Stub.asInterface(b);
        try {
            service.playSoundRes(0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    void startActivity(){
        Log.e(TAG, "startActivity: " );
        Intent mIntent = new Intent(Intent.ACTION_MAIN);
        ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.Settings");
        mIntent.setComponent(cn);
        //mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(mIntent);

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

    private void defaultWiFi() {
            wifiMgr.openWifi();
            WifiConfiguration configuration = wifiMgr.createWifiInfo("XYJWIFI", "xyjwifi@2017", 2);
//            configuration = WifiUtils.getInstance(this).configWifiInfo(this, "XYJ_TEST", "xyj888168test", 2);
            wifiMgr.addNetwork(configuration);

    }
    private void getVolume(){

    }
}
