package com.wizarpos.aidl.tester;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.wizarpos.utils.Logger;
import com.wizarpos.utils.TextViewUtil;
import com.wizarpos.wizarviewagentassistant.aidl.ISystemExtApi;
import com.wizarpos.wizarviewagentassistant.aidl.NetworkType;


public class MainActivity extends AbstractActivity implements OnClickListener , ServiceConnection{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_run3 = (Button) this.findViewById(R.id.btn_run3);
        Button btn_run4 = (Button) this.findViewById(R.id.btn_run4);
        Button btn_run5 = (Button) this.findViewById(R.id.btn_run5);
        ((Button) this.findViewById(R.id.btn_test6)).setOnClickListener(this);
        ((Button) this.findViewById(R.id.btn_test5)).setOnClickListener(this);
        ((Button) this.findViewById(R.id.btn_test4)).setOnClickListener(this);
        log_text = (TextView) this.findViewById(R.id.text_result);
        log_text.setMovementMethod(ScrollingMovementMethod.getInstance());

        findViewById(R.id.settings).setOnClickListener(this);
        btn_run3.setOnClickListener(this);
        btn_run4.setOnClickListener(this);
        btn_run5.setOnClickListener(this);


        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == R.id.log_default) {
                    log_text.append("\t" + msg.obj + "\n");
                } else if (msg.what == R.id.log_success) {
                    String str = "\t" + msg.obj + "\n";
                    TextViewUtil.infoBlueTextView(log_text, str);
                } else if (msg.what == R.id.log_failed) {
                    String str = "\t" + msg.obj + "\n";
                    TextViewUtil.infoRedTextView(log_text, str);
                } else if (msg.what == R.id.log_clear) {
                    log_text.setText("");
                }
            }
        };
        bindSystemExtService();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unbindService(this);
    }


    public void bindSystemExtService() {
        try {
            startConnectService(MainActivity.this,
                    "com.wizarpos.wizarviewagentassistant",
                    "com.wizarpos.wizarviewagentassistant.SystemExtApiService", this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected boolean startConnectService(Context mContext, String packageName, String className, ServiceConnection connection) {
        boolean isSuccess = startConnectService(mContext, new ComponentName(packageName, className), connection);
        writerInLog("bind service result" + isSuccess, R.id.log_default);
        return isSuccess;
    }

    protected boolean startConnectService(Context context, ComponentName comp, ServiceConnection connection) {
        Intent intent = new Intent();
        intent.setPackage(comp.getPackageName());
        intent.setComponent(comp);
        boolean isSuccess = context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        Logger.debug("bind service (%s, %s)", isSuccess, comp.getPackageName(), comp.getClassName());
        return isSuccess;
    }

    @Override
    public void onClick(View arg0) {
        int index = arg0.getId();
        if (index == R.id.btn_run3) {
            getNetworkModeArray();
        } else if (index == R.id.btn_run4) {
            getCurrentNetworkMode();
        } else if (index == R.id.btn_run5) {
            testAll();
        } else if (index == R.id.settings) {
            log_text.setText("");
        } else if (index == R.id.btn_test4) {
            testSetType(2);
        } else if (index == R.id.btn_test5) {
            testSetType(11);
        } else if (index == R.id.btn_test6) {
            testSetType(12);
        }
    }

    private void testSetType(int networkType){
        writerInLog("setType : " + networkType, R.id.log_default);
        try {
            systemExtApi.setPreferredNetworkType(0, networkType);
            int result = getCurrentNetworkMode();
            if(result == networkType ){
                writerInSuccessLog("set success!");
            }else{
                writerInFailedLog("set failed!");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private void testApi(){
        String [] arr = queryModeArrayInNetworkSetting("preferred_network_mode_values_wcdma_nogsm");
        writerInLog("testApi : " + debugIntArray(arr), R.id.log_default);

        arr = queryModeArrayInNetworkSetting("preferred_network_mode_values_wcdma_nogsm");
        writerInLog("testApi : " + debugIntArray(arr), R.id.log_default);

        arr = queryModeArrayInNetworkSetting("preferred_network_mode_values");
        writerInLog("testApi : " + debugIntArray(arr), R.id.log_default);
    }
    private String[] queryModeArrayInNetworkSetting(String key){
        Context otherContext = null;
        try {
//            com.android.phone com.qualcomm.qti.networksetting
            String packageName = "com.android.phone";
            otherContext = this.createPackageContext(packageName, 0);
            int arrId = otherContext.getResources().getIdentifier(key, "array", packageName);
            String[] arr = otherContext.getResources().getStringArray(arrId);
            Logger.debug("SQLTestFindOtherRes..found arr.." + debugIntArray(arr));
            return arr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private String debugIntArray(String[] strArray){
        String debug = "";
        if(strArray != null ){
            for(String i : strArray){
                debug = debug + ", " + i;
            }
        }else{
            debug = "int array is null.";
        }
        return debug;
    }
    private void getNetworkModeArray(){
        try {
            NetworkType[] networkTypes = systemExtApi.getSupportedNetworkTypes();
            writerInLog("getTypes : " + debugInNetworkArr(networkTypes), R.id.log_default);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private int getCurrentNetworkMode(){
        try {
            int result = systemExtApi.getPreferredNetworkType(0);
            writerInLog("getCurrentNetworkMode : " + result, R.id.log_default);
            return result;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }
    private String debugInNetworkArr(NetworkType[] networkModes){
        String debug = "\t";
        if(networkModes != null ){
            for(NetworkType i : networkModes){
                debug = debug + ", \n\t" + i;
            }
        }else{
            debug = "network type array is null.";
        }
        return debug;
    }
    private Thread th;
    private void testAll(){
        if(th == null || th.getState() == Thread.State.TERMINATED){
            th = new Thread(){
                @Override
                public void run() {
                    super.run();
                    try {
                        NetworkType [] networkTypes = systemExtApi.getSupportedNetworkTypes();
//                        writerInLog("NetworkTypes \n: " + debugInNetworkArr(networkTypes), R.id.log_default);
                        for(NetworkType networkType : networkTypes){
                            writerInLog("setType : " + networkType, R.id.log_default);
                            testSetType(networkType.getTypeId());
                            int result = getCurrentNetworkMode();
                            try {
                                for(int i = 3; i >= 0; i--){
                                    writerInLog("start waiting for " + i +"s.", R.id.log_default);
                                    Thread.sleep(1000);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            };
            th.start();
        }

    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        try {
            writerInLog("onServiceConnected:" + service.getInterfaceDescriptor(), R.id.log_success);
            systemExtApi = ISystemExtApi.Stub.asInterface(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
    ISystemExtApi systemExtApi;
}
