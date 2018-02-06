package com.example.wscdemo.activity;

import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.wscdemo.R;
import com.example.wscdemo.base.AppContext;
import com.example.wscdemo.base.BaseActivity;
import com.example.wscdemo.common.Consts;
import com.example.wscdemo.receiver.HotSpotBroadcaseReceiver;
import com.example.wscdemo.receiver.WifiBroadcaseReceiver;
import com.example.wscdemo.utils.LogUtils;
import com.example.wscdemo.wifitools.ApMgr;
import com.example.wscdemo.wifitools.WifiMgr;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.wscdemo.common.Consts.CMD_FILE_RECEIVER_INIT_SUCCESS;
import static com.example.wscdemo.common.Consts.CMD_INIT_ACK_OK;
import static com.example.wscdemo.common.Consts.CMD_SSID_CONTENT_ACK_OK;
import static com.example.wscdemo.common.Consts.CMD_START_SEND;
import static com.example.wscdemo.common.Consts.HOTSPOT_PW;
import static com.example.wscdemo.common.Consts.HOTSPOT_SSID;
import static com.example.wscdemo.common.Consts.MSG_FILE_RECEIVER_INIT_SUCCESS;
import static com.example.wscdemo.common.Consts.MSG_SSID_CONTENT;
import static com.example.wscdemo.common.Consts.MSG_START_SEND;
import static com.example.wscdemo.utils.PermissionUtils.grantSettingsWritePermission;
import static com.example.wscdemo.wifitools.ApMgr.closeAp;

/**
 * Created by Administrator on 2018/1/29.
 */

public class HotSpotActivity extends BaseActivity {
    @BindView(R.id.tv_common_info)
    TextView tvCommonInfo;
    @BindView(R.id.ll_bt_bar)
    LinearLayout llBtBar;
    private boolean mIsInitialized = false;
    private boolean isConnecting = false;
    private boolean isContentReceived= false;
    private DatagramSocket mDatagramSocket;
    //Client Address Info
    private InetAddress inetAddress;
    private int port = -1;
    /**
     * WiFi工具类
     */
    private WifiMgr mWifiMgr;
    private String toConnectSSID;
    private String toConnectPW;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_FILE_RECEIVER_INIT_SUCCESS) {
                LogUtils.i("Client连接热点成功...\n");
                tvCommonInfo.append("Host:<<<---"+CMD_FILE_RECEIVER_INIT_SUCCESS+"\n");
                tvCommonInfo.append("Host:--->>>" + CMD_INIT_ACK_OK + "\n");
                sendCmdAck(inetAddress, port, CMD_INIT_ACK_OK);
            } else if (msg.what == MSG_START_SEND) {
                tvCommonInfo.append("Cient开始发送内容:\n");
            } else if (msg.what == MSG_SSID_CONTENT) {
                tvCommonInfo.append("<<<---" + msg.obj + "\n");
                parserSsidContent(msg.obj.toString());
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_common_info;
    }

    @Override
    protected String getTitleText() {
        return "Hotspot";
    }

    @Override
    protected void initData() {
        llBtBar.setVisibility(View.GONE);
        mWifiMgr = new WifiMgr(getContext());
        LogUtils.i("Hotspot Activity init Data");
        tvCommonInfo.setText("try to grant permission\n");
        grantSettingsWritePermission(this);
        tvCommonInfo.append("try to open WiFi Hotspot\n");
        openHotspot();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    private void finishActivity() {
        closeConnect();
        closeAp(getContext());
        finish();
    }

    private void closeConnect() {
        LogUtils.i("close udp socket\n");
        closeUdpSocket();
        unregisterReceiver(mHotSpotBroadcaseReceiver);
        unregisterReceiver(mWifiBroadcaseReceiver);
    }

    private void closeUdpSocket() {
        if (mDatagramSocket != null) {
            if (!mDatagramSocket.isClosed()) {
                mDatagramSocket.close();
            }
            mDatagramSocket.disconnect();
            mDatagramSocket = null;
        }
    }

    private void perConnectWifi() {
        sendCmdAck(inetAddress,port,CMD_SSID_CONTENT_ACK_OK);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                closeAp(getContext());
                mWifiMgr.openWifi();
            }
        }, 1000);
    }

    public void openHotspot() {
        String ssid = HOTSPOT_SSID;
        String password = HOTSPOT_PW;
        if (isEmptyString(ssid)) {
            ssid = Build.MODEL;
        }

        //开启热点前，先关闭WiFi，如有其他热点已开启，先关闭
        ApMgr.closeWifi(getContext());
        if (ApMgr.isApOn(getContext())) {
            closeAp(getContext());
        }

        //注册便携热点状态接收器
        registerHotSpotReceiver();
        registerWifiReceiver();

        //以手机型号为SSID，开启热点
        boolean isSuccess = ApMgr.openAp(getContext(), ssid, password);
        if (!isSuccess) {
            LogUtils.i("HotspotActivity Open Ap Failed");
            tvCommonInfo.append("创建热点失败\n");
        }/* else {
            showTipsDialog("获取权限失败，开启热点", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }*/
    }

    private void registerWifiReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mWifiBroadcaseReceiver, filter);
    }

    private void registerHotSpotReceiver() {
        IntentFilter filter = new IntentFilter(HotSpotBroadcaseReceiver.ACTION_HOTSPOT_STATE_CHANGED);
        registerReceiver(mHotSpotBroadcaseReceiver, filter);
    }


    private HotSpotBroadcaseReceiver mHotSpotBroadcaseReceiver = new HotSpotBroadcaseReceiver() {
        @Override
        public void onHotSpotEnabled() {
            //热点成功开启
            if (!mIsInitialized) {
                mIsInitialized = true;
                LogUtils.i("成功开启热点");
                tvCommonInfo.append("成功开启热点...\n");
                tvCommonInfo.append("ssid:" + HOTSPOT_SSID + "\n");
                tvCommonInfo.append("password:" + HOTSPOT_PW + "\n");
                tvCommonInfo.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvCommonInfo.append("正在等待连接...\n");

                        //等待接收端连接
                        Runnable mUdpServerRunnable = receiveInitSuccessOrderRunnable();
                        AppContext.MAIN_EXECUTOR.execute(mUdpServerRunnable);
                    }
                }, 2000);
            }
        }
    };


    /**
     * WiFi广播接收器
     */
    private WifiBroadcaseReceiver mWifiBroadcaseReceiver = new WifiBroadcaseReceiver() {
        @Override
        public void onWifiEnabled() {
            //WiFi已开启，开始扫描可用WiFi
            if(!isConnecting){
                mWifiMgr.startScan();
            }
        }

        @Override
        public void onWifiDisabled() {
            //WiFi已关闭，清除可用WiFi列表
            tvCommonInfo.append("onWifiDisabled\n");
            //mSelectedSSID = "";
            //mScanResults.clear();
            //setupWifiAdapter();
        }


        @Override
        public void onScanResultsAvailable(List<ScanResult> scanResults) {
            tvCommonInfo.append("WiFi Scan finished");
            //扫描周围可用WiFi成功，设置可用WiFi列表
            /*mScanResults.clear();
            mScanResults.addAll(scanResults);
            setupWifiAdapter();*/
            if (!isConnecting) {
                isConnecting = true;
                for (ScanResult printItem : scanResults) {
                    tvCommonInfo.append(printItem.SSID + "\n");
                }
                for (ScanResult item : scanResults) {

                    if (item.SSID != null && item.SSID.equals(toConnectSSID)) {
                        tvCommonInfo.append("找到将要连接的WIFI...\n");
                        try {
                            tvCommonInfo.append("正在连接..." + toConnectSSID + "\n");
                            mWifiMgr.connectWifi(toConnectPW, item);
                            break;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        tvCommonInfo.append("没有找到指定的网络\n");
                    }
                }
            }

        }

        @Override
        public void onWifiConnected(String connectedSSID) {
            if(isConnecting){
                tvCommonInfo.append("Wifi连接" + connectedSSID + "成功...\n");
                tvCommonInfo.append("连接WIFI成功，退出配网...\n");
                finishActivity();
            }
        }

        @Override
        public void onWifiDisconnected() {

        }

    };



    private Runnable receiveInitSuccessOrderRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    //开始接收接收端发来的指令
                    receiveInitSuccessOrder(Consts.DEFAULT_SERVER_UDP_PORT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void receiveInitSuccessOrder(int serverPort) throws Exception {
        //确保WiFi连接后获取正确IP地址
        int tryCount = 0;
        String localIpAddress = ApMgr.getHotspotLocalIpAddress(getContext());
        while (localIpAddress.equals(Consts.DEFAULT_UNKNOW_IP) && tryCount < Consts.DEFAULT_TRY_COUNT) {
            Thread.sleep(1000);
            localIpAddress = ApMgr.getHotspotLocalIpAddress(getContext());
            tryCount++;
        }

        /** 这里使用UDP发送和接收指令 */
        mDatagramSocket = new DatagramSocket(serverPort);
        while (true) {
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            mDatagramSocket.receive(receivePacket);
            String response = new String(receivePacket.getData()).trim();
            if (isNotEmptyString(response)) {
                LogUtils.e("HOST:接收到的消息 <<<---" + response);
                if (response.equals(Consts.CMD_FILE_RECEIVER_INIT_SUCCESS)) {
                    inetAddress = receivePacket.getAddress();
                    port = receivePacket.getPort();
                    //初始化成功指令
                    Message msg = Message.obtain();
                    msg.what = MSG_FILE_RECEIVER_INIT_SUCCESS;
                    msg.obj = response;
                    mHandler.sendMessage(msg);
                } else if (response.equals(CMD_START_SEND)) {
                    //开始发送指令,暂时没用
                    mHandler.sendEmptyMessage(MSG_START_SEND);
                } else {
                    //开始解析SSID和PW
                    //parserSsidContent(response);
                    if(!isContentReceived){
                        isContentReceived = true;
                        Message msg = Message.obtain();
                        msg.what = MSG_SSID_CONTENT;
                        msg.obj = response;
                        mHandler.sendMessage(msg);
                    }

                }
            }
        }
    }

    private void parserSsidContent(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if(jsonObject != null){
                String ssid = jsonObject.getString("SSID");
                String pw = jsonObject.getString("PW");
                LogUtils.i("SSID:" + ssid + "; PW:" + pw);
                tvCommonInfo.append("Host:--->>>" + jsonObject.toString() + "\n");
                //TODO 开始校验
                toConnectSSID = ssid;
                toConnectPW = pw;
                perConnectWifi();
            }else {
                LogUtils.i("parser SSID and PW error");
                tvCommonInfo.append("Host: parser SSID and PW error\n");

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 通过UDP发送文件列表给接收端
     *
     * @param ipAddress  IP地址
     * @param serverPort 端口号
     */
    private void sendCmdAck(final InetAddress ipAddress, final int serverPort, final String cmd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramPacket sendAckPacke = null;
                boolean cmdIsValid = false;
                switch (cmd) {
                    case CMD_INIT_ACK_OK:
                        sendAckPacke = new DatagramPacket(CMD_INIT_ACK_OK.getBytes(), CMD_INIT_ACK_OK.length(), ipAddress, serverPort);
                        cmdIsValid = true;
                        break;
                    case CMD_SSID_CONTENT_ACK_OK:
                        sendAckPacke = new DatagramPacket(CMD_SSID_CONTENT_ACK_OK.getBytes(), CMD_INIT_ACK_OK.length(), ipAddress, serverPort);
                        cmdIsValid = true;
                        break;

                }
                if (cmdIsValid) {
                    try {
                        //发送给客户端
                        mDatagramSocket.send(sendAckPacke);
                        LogUtils.i("Host:--->>>" + new String(sendAckPacke.getData()).trim());
                    } catch (IOException e) {
                        e.printStackTrace();
                        LogUtils.i("发送消息 --->>>" + new String(sendAckPacke.getData()).trim() + "=== 失败！");
                    }

                }

            }
        }).start();


    }
}
