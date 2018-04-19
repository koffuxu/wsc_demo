package com.example.wscdemo.activity;

import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.example.wscdemo.R;
import com.example.wscdemo.base.AppContext;
import com.example.wscdemo.base.BaseActivity;
import com.example.wscdemo.common.Consts;
import com.example.wscdemo.receiver.WifiBroadcaseReceiver;
import com.example.wscdemo.utils.LogUtils;
import com.example.wscdemo.utils.NetUtils;
import com.example.wscdemo.wifitools.WifiMgr;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.wscdemo.common.Consts.CMD_FILE_RECEIVER_INIT_SUCCESS;
import static com.example.wscdemo.common.Consts.CMD_INIT_ACK_OK;
import static com.example.wscdemo.common.Consts.CMD_SSID_CONTENT_ACK_OK;
import static com.example.wscdemo.common.Consts.CONTENT_PW;
import static com.example.wscdemo.common.Consts.CONTENT_SSID;
import static com.example.wscdemo.common.Consts.DEFAULT_SERVER_UDP_PORT;
import static com.example.wscdemo.common.Consts.HOTSPOT_PW;
import static com.example.wscdemo.common.Consts.HOTSPOT_SSID;
import static com.example.wscdemo.common.Consts.MSG_FILE_RECEIVER_INIT_SUCCESS;
import static com.example.wscdemo.common.Consts.MSG_INIT_ACKOK;
import static com.example.wscdemo.common.Consts.MSG_SET_STATUS;
import static com.example.wscdemo.common.Consts.MSG_SSID_CONTENT_ACK_OK;
import static com.example.wscdemo.common.Consts.UTF_8;

/**
 * Created by Administrator on 2018/1/29.
 */

public class DeviceActivity extends BaseActivity {
    @BindView(R.id.tv_common_info)
    TextView tvCommonInfo;
    WifiMgr mWifiMgr;

    private String mSelectedSSID;

    private DatagramSocket mDatagramSocket;
    private boolean mIsSendInitOrder=false;
    private boolean mIsFoundHotspot = false;
    private boolean mWifiScanning = false;

    private Handler mHandler = new Handler() {



        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_FILE_RECEIVER_INIT_SUCCESS) {
                //告知发送端，接收端初始化完毕
                refreshTextView("Client:--->>>"+CMD_FILE_RECEIVER_INIT_SUCCESS+"\n");
                sendInitSuccessToFileSender();
            } else if (msg.what == MSG_SET_STATUS) {
                //设置当前状态
                refreshTextView(msg.obj.toString()+"\n");
            }  else if(msg.what == MSG_SSID_CONTENT_ACK_OK){
                LogUtils.i("HOST收到SSID和密码正确，即将连接网络\n");
                refreshTextView("Client: <<<---CMD_SSID_CONTENT_ACK_OK\n");
            } else if(msg.what == MSG_INIT_ACKOK){
                refreshTextView("Client: <<<---CMD_INIT_ACK_OK\n");
                refreshTextView("Client:--->>>{SSID CONTENT}\n");
                sendSsidConent((msg.obj).toString());
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_common_info;
    }

    @Override
    protected String getTitleText() {
        return "Client";
    }

    @Override
    protected void initData() {
        //开启WiFi，监听WiFi广播
        registerWifiReceiver();
        mWifiMgr = new WifiMgr(getContext());
        if (mWifiMgr.isWifiEnabled()) {
            refreshTextView("Init Data:正在扫描可用WiFi...\n");
            mWifiMgr.startScan();
            mWifiScanning = true;
        } else {
            refreshTextView("正在打开WiFi...\n");
            mWifiMgr.openWifi();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    //onClick Lisenter
    public void btScannerWifi(View v){
        if (mWifiMgr.isWifiEnabled()) {
            refreshTextView("Button On Click 正在扫描可用WiFi...\n");
            mWifiMgr.startScan();
        } else {
            mWifiMgr.openWifi();
        }
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }
    /**
     * 关闭此Activity
     */
    private void finishActivity() {
        LogUtils.i("DeviceActivity finishActivity");
        mIsFoundHotspot = false;
        //断开UDP Socket
        closeUdpSocket();

        //清除WiFi网络
        //mWifiMgr.clearWifiConfig();
        //清除指定的AP Host
        mWifiMgr.clearWifiConfig(HOTSPOT_SSID);

        unregisterReceiver(mWifiBroadcaseReceiver);

        //清除info textview
        tvCommonInfo.setText("");

        finish();
    }
    /**
     * 关闭UDP Socket
     */
    private void closeUdpSocket() {
        if(mDatagramSocket != null) {
            if(!mDatagramSocket.isClosed()){
                mDatagramSocket.close();
            }
            mDatagramSocket.disconnect();
            mDatagramSocket = null;
        }
    }


    private void registerWifiReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mWifiBroadcaseReceiver, filter);
    }


    /**
     * WiFi广播接收器
     */
    private WifiBroadcaseReceiver mWifiBroadcaseReceiver = new WifiBroadcaseReceiver() {
        @Override
        public void onWifiEnabled() {
            //WiFi已开启，开始扫描可用WiFi
            //refreshTextView("正在扫描可用WiFi...\n");
            //mWifiMgr.startScan();
        }

        @Override
        public void onWifiDisabled() {
            //WiFi已关闭，清除可用WiFi列表
            refreshTextView("onWifiDisabled");
            //mSelectedSSID = "";
            //mScanResults.clear();
            //setupWifiAdapter();
        }


        @Override
        public void onScanResultsAvailable(List<ScanResult> scanResults) {
            refreshTextView("WiFi Scan finished");
            //扫描周围可用WiFi成功，设置可用WiFi列表
            /*mScanResults.clear();
            mScanResults.addAll(scanResults);
            setupWifiAdapter();*/
            for(ScanResult item : scanResults){
                refreshTextView(item.SSID);
                if(item.SSID !=null && item.SSID.equals(HOTSPOT_SSID)){
                    refreshTextView(" ----->找到YJ263热点...\n");
                    mIsFoundHotspot = true;
                    try {
                        refreshTextView("正在连接热点...\n");
                        mWifiMgr.connectWifi(HOTSPOT_PW, item);
                        break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else {
                    refreshTextView(" ----->mismatch\n");
                }
            }
        }

        @Override
        public void onWifiConnected(String connectedSSID) {
            refreshTextView("Wifi连接"+connectedSSID+"成功...\n");
            //判断指定WiFi是否连接成功
            if (connectedSSID.equals(HOTSPOT_SSID) && !mIsSendInitOrder) {
                //告知发送端，接收端初始化完毕
                mHandler.sendEmptyMessage(MSG_FILE_RECEIVER_INIT_SUCCESS);
                mIsSendInitOrder = true;
            } else {
                //连接成功的不是设备WiFi，清除该WiFi，重新扫描周围WiFi
/*                LogUtils.e("连接到错误WiFi，正在断开重连...");
                mWifiMgr.disconnectWifi(connectedSSID);
                mWifiMgr.startScan();*/
            }
        }

        @Override
        public void onWifiDisconnected() {
            refreshTextView("Wifi断开...\n");

        }

    };

    private Runnable sendInitCmdRunnable(){
        return new Runnable() {
            @Override
            public void run() {
                try {
                    //确保WiFi连接后获取正确IP地址
                    int tryCount = 0;
                    String serverIp = mWifiMgr.getIpAddressFromHotspot();
                    while (serverIp.equals(Consts.DEFAULT_UNKNOW_IP) && tryCount < Consts.DEFAULT_TRY_COUNT) {
                        Thread.sleep(1000);
                        serverIp = mWifiMgr.getIpAddressFromHotspot();
                        tryCount ++;
                    }

                    //是否可以ping通指定IP地址
                    tryCount = 0;
                    while (!NetUtils.pingIpAddress(serverIp) && tryCount < Consts.DEFAULT_TRY_COUNT) {
                        Thread.sleep(500);
                        LogUtils.i("Try to ping ------" + serverIp + " - " + tryCount);
                        tryCount ++;
                    }

                    //创建UDP通信
                    if(mDatagramSocket == null) {
                        //解决：java.net.BindException: bind failed: EADDRINUSE (Address already in use)
                        mDatagramSocket = new DatagramSocket(null);
                        mDatagramSocket.setReuseAddress(true);
                        mDatagramSocket.bind(new InetSocketAddress(DEFAULT_SERVER_UDP_PORT));
                    }
                    //发送初始化完毕指令
                    InetAddress ipAddress = InetAddress.getByName(serverIp);
                    byte[] sendData = CMD_FILE_RECEIVER_INIT_SUCCESS.getBytes(UTF_8);
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, DEFAULT_SERVER_UDP_PORT);
                    mDatagramSocket.send(sendPacket);
                    LogUtils.i("Client:--->>>" + CMD_FILE_RECEIVER_INIT_SUCCESS);

                    //接收文件列表
                    while (true) {
                        byte[] receiveData = new byte[1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        mDatagramSocket.receive(receivePacket);
                        String response = new String(receivePacket.getData()).trim();
                        if(isNotEmptyString(response)) {
                            //发送端发来的文件列表
                            LogUtils.e("Client:<<<---" + response);
                            if(response.equals(CMD_INIT_ACK_OK)){
                                Message msg = Message.obtain();
                                msg.what = MSG_INIT_ACKOK;
                                msg.obj = serverIp;
                                mHandler.sendMessage(msg);
                            }else if(response.equals(CMD_SSID_CONTENT_ACK_OK)){
                                mHandler.sendEmptyMessage(MSG_SSID_CONTENT_ACK_OK);
                                //hostConnectFinished();
                                //TODO
                                //进入下一步
                            }
                            //parseFileInfoList(response);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
    private void sendInitSuccessToFileSender() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Runnable initCmd = sendInitCmdRunnable();
                AppContext.MAIN_EXECUTOR.execute(initCmd);
            }
        },2000);
    }


    private void sendSsidConent(final String SIP) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtils.i("send SSID Content");
                JSONObject content = new JSONObject();
                try {
                    content.put("SSID", CONTENT_SSID);
                    content.put("PW", CONTENT_PW);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String result = content.toString();

                InetAddress ipAddress = null;
                try {
                    ipAddress = InetAddress.getByName(SIP);
                    byte[] sendData = result.getBytes(UTF_8);
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, DEFAULT_SERVER_UDP_PORT);
                    mDatagramSocket.send(sendPacket);
                    LogUtils.i("Client:--->>>"+result);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }).start();

    }

  private void refreshTextView(String msg) {
        tvCommonInfo.append(msg);
        int offset = tvCommonInfo.getLineCount() * tvCommonInfo.getLineHeight();
        if (offset > (tvCommonInfo.getHeight() - tvCommonInfo.getLineHeight() - 20)) {
            tvCommonInfo.scrollTo(0, offset - tvCommonInfo.getHeight() + tvCommonInfo.getLineHeight() + 20);
        }
    }
}
