package com.example.wscdemo.common;


public class Consts {

    public static final String KEY_EXIT = "exit";
    public static final String KEY_IPPORT_INFO = "ipport_info";

    public static final String HOTSPOT_SSID = "YJ263";
    public static final String HOTSPOT_PW= "888888888";

    public static final String CONTENT_SSID = "XYJWIFI";
    public static final String CONTENT_PW= "xyjwifi@2017";

    /**
     * 传输字节类型
     */
    public static final String UTF_8 = "UTF-8";



    /**
     * 最大尝试次数
     */
    public static final int DEFAULT_TRY_COUNT = 10;

    /**
     * WiFi连接成功时未分配的默认IP地址
     */
    public static final String DEFAULT_UNKNOW_IP = "0.0.0.0";

    /**
     * UDP通信服务端默认端口号
     */
    public static final int DEFAULT_SERVER_UDP_PORT = 8204;

    /**
     * 文件接收端监听默认端口号
     */
    public static final int DEFAULT_FILE_RECEIVE_SERVER_PORT = 8284;



    /**************************
     * Handler Message
     **************************/
    // * 接收端初始化完毕
    public static final int MSG_FILE_RECEIVER_INIT_SUCCESS = 661;
    // * 更新适配器
    public static final int MSG_UPDATE_ADAPTER = 662;

    public static final int MSG_START_SEND = 663;
    public static final int MSG_SSID_CONTENT = 664;
    // * 设置当前状态
    public static final int MSG_SET_STATUS = 666;

    // * Host端校验成功
    public static final int MSG_SSID_CONTENT_ACK_OK= 667;
    // * Host端校验失败
    public static final int MSG_HOST_CONCENT_RECEIVED_ERR = 668;
    public static final int MSG_INIT_ACKOK= 669;

    /**************************
     * UDPSOCKET COMMAND
     **************************/
    // * UDP通知：文件接收端初始化完毕
    public static final String CMD_FILE_RECEIVER_INIT_SUCCESS =     "CMD_CLIENT_INIT";
    public static final String CMD_INIT_ACK_OK=                     "CMD_INIT_ACK_OK";
    public static final String CMD_INIT_ACK_ERR=                    "CMD_INIT_ACK_ERR";
    public static final String CMD_SSID_CONTENT_ACK_OK=             "CMD_SSID_ACK_OK";
    public static final String CMD_SSID_CONTENT_ACK_ERR=            "CMD_SSID_CONTENT_ACK_ERR";
    public static final String CMD_SSID_CONTENT_VERIFY_OK=          "CMD_SSID_CONTENT_VERIFY_OK";
    public static final String CMD_SSID_CONTENT_VERIFY_ERR=         "CMD_SSID_CONTENT_VERIFY_ERR";

    // * UDP通知：开始发送文件
    public static final String CMD_START_SEND = "CMD_START_SEND";


    /**
     * Wifi Secrity Type
     */
    public static final int WIFICIPHER_NOPASS = 0;
    public static final int WIFICIPHER_WEP = 1;
    public static final int WIFICIPHER_WPA = 2;





}
