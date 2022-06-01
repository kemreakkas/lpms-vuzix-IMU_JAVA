package cn.alubi.myapplication.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.util.Log;



import java.util.Timer;
import java.util.TimerTask;

import cn.alubi.myapplication.Activity.MainActivity;
import cn.alubi.myapplication.R;
import cn.alubi.lpresearch_library.lpsensorlib.LpmsB2;
import cn.alubi.lpresearch_library.lpsensorlib.LpmsB2Manager;
import cn.alubi.lpresearch_library.lpsensorlib.LpmsBData;

public class LpService extends Service {
    private static final String TAG = "LpService";

    //////////////////////////////////////////////////////////////////////
    // Globals
    //////////////////////////////////////////////////////////////////////
    // Main UI communication protocol
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_LOGGING_STARTED = 3;
    public static final int MSG_LOGGING_STOPPED = 4;
    public static final int MSG_LOGGING_ERROR = 5;
    public static final int MSG_SENSOR_CONNECTED = 6;
    public static final int MSG_SENSOR_DISCONNECTED = 7;
    public static final int MSG_SENSOR_CONNECTION_ERROR = 8;
    public static final String KEY_MESG = "MESG";
    ResultReceiver resultReceiver;

    // Thread related
    Timer timer;
    private int UPDATE_RATE = 10; // ms
    private static boolean isRunning = false;

    // Binder given to clients
    public class LocalBinder extends Binder {
        public LpService getService() {
            return LpService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    // Notification
    private NotificationManager nm;
    int notificationId = 2;

    // Service Status
    boolean serviceQuit = false;
    long startTime = System.currentTimeMillis();

    // LpmsB
    BluetoothAdapter btAdapter;
    LpmsB2 lpmsB;
    LpmsBData d;

    int sensorDataQueueSize = 0;

    //////////////////////////////////////////////////////////////////////

    public LpService() {
        Log.d(TAG, "LpLpService()");
    }

    @Override
    public void onCreate() {
        // Init
		// Bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        lpmsB = new LpmsB2();
        d = new LpmsBData();

        // Sensor read Timer
        timer = new Timer();
        timer.scheduleAtFixedRate(getLpmsBDataTask, 0, UPDATE_RATE);
        timer.scheduleAtFixedRate(getLpmsBBatteryLevel, 0, 5000);
        /*
        serviceQuit = false;
        Thread t = new Thread(new getLpmsBDataThread());
        t.start();
        */
       showNotification();

        // Service Status
        startTime = System.currentTimeMillis();
        isRunning = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            //TODO
            if(intent!=null)
            resultReceiver = intent.getParcelableExtra("receiver");
        } catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        serviceQuit = true;
        // Clean up
        // Lpms
        disconnectAll();

        // Timer
        timer.cancel();

        // Notification.
        nm.cancel(notificationId);

        // Service status
        isRunning = false;
    }

    ///////////////////////////////
    // method for clients
    ///////////////////////////////
    public double getUptime() {
        if (isRunning)
            return (double) (System.currentTimeMillis() - startTime) / 1000.0;
        return 0.0;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    /********************************
    *             LmsB2Manager
    *            Connect Classical
     * */
    LpmsB2Manager lpm = new LpmsB2Manager();

    //multi connection
    public  void connectSensor(int number,String address){
        final int num = number;
        final String add = address;
        lpmsB =lpm.getLpmsB2(num);
        if (lpm.getLpmsB2(num).getConnectionStatus() != LpmsB2.SENSOR_STATUS_DISCONNECTED) {
            sendMessageToUI(KEY_MESG, "Sensor "+num+" connected", MSG_SENSOR_CONNECTED);
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                //connect  LpmsB;
                if (lpm.connect(num,add)){
                    sendMessageToUI(KEY_MESG, "Sensor "+num+" connected", MSG_SENSOR_CONNECTED);
                }else {
                    sendMessageToUI(KEY_MESG, "Sensor "+num+" disconnected", MSG_SENSOR_DISCONNECTED);
                }
            }
        }).start();
    }

    public void disconnect(int number){
        //lpm.getLpmsB2(num).disconnect();
        final int num = number;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (lpm.getLpmsB2(num).disconnect()){
                    sendMessageToUI(KEY_MESG, "Sensor "+num+" disconnected", MSG_SENSOR_CONNECTED);
                }else {
                    sendMessageToUI(KEY_MESG, "Sensor "+num+" disconnected failed", MSG_SENSOR_DISCONNECTED);
                }
            }
        }).start();
     }

    public void disconnect(){
        lpmsB.disconnect();
    }

     public LpmsB2 getLpmsB2(int num){
        lpmsB = lpm.getLpmsB2(num);
         return lpmsB;
     }

     public void disconnectAll(){
         lpm.disconnectAll();
     }

     /******************************************
     *              Connect  BLE
     * */

    public void connectSensorBLE(int number,String address){
        //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        final int num = number;
        final String add = address;
        lpmsB =lpm.getLpmsB2(num);
        if (lpm.getLpmsB2(num).getConnectionStatus() != LpmsB2.SENSOR_STATUS_DISCONNECTED) {
            sendMessageToUI(KEY_MESG, "Sensor "+num+" connected", MSG_SENSOR_CONNECTED);
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
            //connect  LpmsB BLE;
              if (lpm.connectBLE(num , getApplication() , add)){
                  sendMessageToUI(KEY_MESG, "Sensor "+num+" connected", MSG_SENSOR_CONNECTED);
                }else {
                  sendMessageToUI(KEY_MESG, "Sensor "+num+" disconnected", MSG_SENSOR_DISCONNECTED);
                }
            }
        }).start();

    }

    /***********************************************
     *                LpmsB2
     */

    public int getSensorConnectionStatus() {
        return lpmsB.getConnectionStatus();
    }

    // Sensor settings
    public void setImuId(int id) {
        lpmsB.setImuId(id);
    }

    public int getImuId() {
        return lpmsB.getImuId();
    }

    public void setStreamFrequency(int freq) {
        lpmsB.setStreamFrequency(freq);
    }

    public int getStreamFrequency() {
        return lpmsB.getStreamFrequency();
    }

    public void setGyroRange(int range) {
        lpmsB.setGyroRange(range);
    }

    public int getGyroRange() {
       return lpmsB.getGyroRange();
    }

    public void setAccRange(int range) {
        lpmsB.setAccRange(range);
    }

    public int getAccRange() {
        return lpmsB.getAccRange();
    }

    public void setMagRange(int range) {
        lpmsB.setMagRange(range);
    }

    public int getMagRange() {
        return lpmsB.getMagRange();
    }

    // Data settings
    public void setTransmissionData(int v) {
        lpmsB.setTransmissionData(v);
    }

    public void enable16BitData() {
        lpmsB.enable16BitData();
    }

    public void enable32BitData() {
        lpmsB.enable32BitData();
    }

    public boolean is16BitDataEnabled() {
        return lpmsB.is16BitDataEnabled();
    }

    public void enableAccData(boolean b) {
        lpmsB.enableAccData(b);
    }

    public boolean isAccDataEnabled() {
        return lpmsB.isAccDataEnabled();
    }

    public void enableGyroData(boolean b) {
        lpmsB.enableGyroData(b);
    }

    public boolean isGyroDataEnabled() {
        return lpmsB.isGyroDataEnabled();
    }

    public void enableMagData(boolean b) {
        lpmsB.enableMagData(b);
    }

    public boolean isMagDataEnabled() {
        return lpmsB.isMagDataEnabled();
    }

    public void enableAngularVelData(boolean b) {
        lpmsB.enableAngularVelData(b);
    }

    public boolean isAngularVelDataEnabled() {
        return lpmsB.isAngularVelDataEnable();
    }

    public void enableQuaternionData(boolean b) {
        lpmsB.enableQuaternionData(b);
    }

    public boolean isQuaternionDataEnabled() {
        return lpmsB.isQuaternionDataEnabled();
    }


    public void enableEulerData(boolean b) {
        lpmsB.enableEulerData(b);
    }

    public boolean isEulerDataEnabled() {
        return lpmsB.isEulerDataEnabled();
    }

    public void enableLinAccData(boolean b) {
        lpmsB.enableLinAccData(b);
    }

    public boolean isLinAccDataEnabled() {
        return lpmsB.isLinAccDataEnabled();
    }

    public void enablePressureData(boolean b) {
        lpmsB.enablePressureData(b);
    }

    public boolean isPressureDataEnabled() {
        return lpmsB.isPressureDataEnabled();
    }

    public void enableAltitudeData(boolean b) {
        lpmsB.enableAltitudeData(b);
    }

    public boolean isAltitudeDataEnabled() {
        return lpmsB.isAltitudeDataEnabled();
    }

    public void enableTemperatureData(boolean b) {
        lpmsB.enableTemperatureData(b);
    }

    public boolean isTemperatureDataEnabled() {
        return lpmsB.isTemperatureDataEnabled();
    }

    public void setFilterMode(int mode) {
        lpmsB.setFilterMode(mode);
    }

    public int getFilterMode() {
        return lpmsB.getFilterMode();
    }

    public void setMagCorrection(int preset){lpmsB.setMAGCorrection(preset);}

    public int getMagCorrection(){return lpmsB.getMAGCorrection();}

    public void setLowPassFilter(int lowpass){lpmsB.setLowPassFilter(lowpass);}

    public int getLowPassFilter(){return lpmsB.getLowPassFilter();}

    public float getBatteryLevel() {
        return lpmsB.getBatteryLevel();
    }
    public float getBatteryVoltage(){
        return lpmsB.getBatteryVoltage();
    }

    public LpmsBData getSensorData() {
        return d;
    }

    public void getSensorDataCmd() {
        d = lpmsB.getLpmsBData();
    }

    public int getSensorDataQueueSize() {
        return sensorDataQueueSize;
    }

    public String getSerialNumber()
    {
        return lpmsB.getSerialNumber();
    }

    public String getDeviceName()
    {
        return lpmsB.getDeviceName();
    }

    public String getFirmwareInfo()
    {
        return lpmsB.getFirmwareInfo();
    }

    public void resetFactorySettings()
    {
        lpmsB.resetFactorySettings();
    }

    public void setCommandMode()
    {
        lpmsB.setCommandMode();
    }

    public void setStreamingMode()
    {
        lpmsB.setStreamingMode();
    }

    public void resetTimestamp()
    {
        lpmsB.resetTimestamp();
    }

    public void setTimestamp(int ts)
    {
        lpmsB.setTimestamp(ts);
    }

    public void setOrientationOffset(int offset) { lpmsB.setOrientationOffset(offset);}

    public void resetOrientationOffset() { lpmsB.resetOrientationOffset();}

    public boolean isStreamingMode(){
        return lpmsB.isStreamingMode();
    }

    public void getSendBattery()
    {
        lpmsB.sendBatteryVoltage();
        lpmsB.sendBatteryPercentage();
    }

    public void getChargingStatus()
    {
        lpmsB.getChargingStatus();
    }

    public void getSensorSetting(){
        lpmsB.initialization();
    }

    //Data Logging
    public void startLogging() {
        if (lpmsB.startLogging()) {
            sendMessageToUI(KEY_MESG, lpmsB.getLoggerStatusMesg(), MSG_LOGGING_STARTED);
        } else {
            sendMessageToUI(KEY_MESG, lpmsB.getLoggerStatusMesg(), MSG_LOGGING_ERROR);
        }
    }

    public void stopLogging() {
        if (lpmsB.stopLogging()) {
            sendMessageToUI(KEY_MESG, lpmsB.getLoggerStatusMesg(), MSG_LOGGING_STOPPED);
        } else {
            sendMessageToUI(KEY_MESG, lpmsB.getLoggerStatusMesg(), MSG_LOGGING_ERROR);
        }
    }

    public boolean isLogging() {
        return lpmsB.isLogging();
    }

    public String getOutputFilename() {
        return lpmsB.getOutputFilename();
    }

    //Flash Debug Data Log
    public void startDebugLogging(){
        if (lpmsB.getDebugLogStatus()) {
            lpmsB.startDebugLogging();
            sendMessageToUI(KEY_MESG, "Flash Logging start", MSG_LOGGING_ERROR);
        } else {
            sendMessageToUI(KEY_MESG, "Flash Logging has already started", MSG_LOGGING_ERROR);
        }
    }
    public void stopDebugLogging(){
        lpmsB.stopDebugLogging();
        int size = lpmsB.getDebugLogSize();
        sendMessageToUI(KEY_MESG, "Flash Logging Size : "+size+" ("+size*64+" bytes)", MSG_LOGGING_STOPPED);
    }
    public void clearDebugLog(){
        if (getFlashLogSize() == 0){
            sendMessageToUI(KEY_MESG, "No data in flash.", MSG_LOGGING_STOPPED);
            return;
        }
        if (lpmsB.clearDebugLog()) {
            sendMessageToUI(KEY_MESG, "Clear!", MSG_LOGGING_STOPPED);
        } else {
            //sendMessageToUI(KEY_MESG, lpmsB.getLoggerStatusMesg(), MSG_LOGGING_ERROR);
        }
    }

    public boolean fullFlashErase(){
        if (lpmsB.fullFlashErase()){
            sendMessageToUI(KEY_MESG, "Flash erase done!", MSG_LOGGING_STOPPED);
            return true;
        }else {
            sendMessageToUI(KEY_MESG, "Flash erase not finished!", MSG_LOGGING_STOPPED);
            return false;
        }
    }

    public int getFlashState(){
        return lpmsB.getFlashState();
    }

    public void saveDebugLogging(){
        int size = getFlashLogSize();
        if (size == 0){
            sendMessageToUI(KEY_MESG, "No data in flash.", MSG_LOGGING_STOPPED);
            return;
        }
        if(lpmsB.getDebugLogStatus()){
            lpmsB.getDebugLog(0,size-1);
        }
    }

    public int getFlashLogSize(){
        return lpmsB.getDebugLogSize();
    }

    public int getUpdataProgress(){
        return lpmsB.getUpdataProgress();
    }

    public boolean isDebugSaving() {
        return lpmsB.isDebugSaving();
    }
    public void stopDebugSaving(){
        lpmsB.stopDebugSaving();
    }

    ///////////////////////////////
    // Privates
    //////////////////////////////
    TimerTask getLpmsBDataTask = new TimerTask() {
        @Override
        public void run() {
            if (lpmsB.getConnectionStatus() != LpmsB2.SENSOR_STATUS_DISCONNECTED && lpmsB.isStreamingMode()) {
                // bSensorConnected = true;
                sensorDataQueueSize = lpmsB.hasNewData();
                while (sensorDataQueueSize > 0) {
                     d = lpmsB.getLpmsBData();
                    sensorDataQueueSize--;
                }
            }
        }
    };

    TimerTask getLpmsBBatteryLevel = new TimerTask() {
        @Override
        public void run() {
            if (lpmsB.getConnectionStatus() != LpmsB2.SENSOR_STATUS_DISCONNECTED){// && lpmsB.isStreamingMode()) {
                lpmsB.sendBatteryPercentage();
            }
        }
    };

    public class getLpmsBDataThread implements Runnable {

        public void run() {
            while (!serviceQuit) {
                if (lpmsB.getConnectionStatus() != LpmsB2.SENSOR_STATUS_DISCONNECTED) {// && lpmsB.isStreamingMode()) {
                    // bSensorConnected = true;
                    sensorDataQueueSize = lpmsB.hasNewData();
                    while (sensorDataQueueSize > 0) {
                        d = lpmsB.getLpmsBData();
                        sensorDataQueueSize--;
                    }
                }
            }
        }
    }

    private void showNotification() {
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.lp_logo)
                        .setContentTitle(getText(R.string.service_title))
                        .setContentText(getText(R.string.service_text));

       Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,i, 0);
        mBuilder.setContentIntent(contentIntent);
        Notification notification = mBuilder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        nm.notify(notificationId, notification);
        //startForeground(notificationId,notification);
    }

    private void sendMessageToUI(String key, String value, int what) {
        Bundle bundle = new Bundle();
        bundle.putString(key, value);
        try {
            resultReceiver.send(what, bundle);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}