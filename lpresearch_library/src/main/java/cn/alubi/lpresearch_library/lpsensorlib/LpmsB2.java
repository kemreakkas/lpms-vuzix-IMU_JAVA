package cn.alubi.lpresearch_library.lpsensorlib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

// move lpservice check connection to here
// check conenction streaming state before enabling streaming
public class LpmsB2 {
    //////////////////////////////////////////////
    // Stream frequency enable bits
    //////////////////////////////////////////////
    public static final int LPMS_STREAM_FREQ_5HZ = 5;
    public static final int LPMS_STREAM_FREQ_10HZ = 10;
    public static final int LPMS_STREAM_FREQ_25HZ = 25;
    public static final int LPMS_STREAM_FREQ_50HZ = 50;
    public static final int LPMS_STREAM_FREQ_100HZ = 100;
    public static final int LPMS_STREAM_FREQ_200HZ = 200;
    public static final int LPMS_STREAM_FREQ_400HZ = 400;

    public static final int LPMS_FILTER_GYR = 0;
    public static final int LPMS_FILTER_GYR_ACC = 1;
    public static final int LPMS_FILTER_GYR_ACC_MAG = 2;
    public static final int LPMS_FILTER_MADGWICK_GYR_ACC = 3;
    public static final int LPMS_FILTER_MADGWICK_GYR_ACC_MAG = 4;
    //Magnetometer correction
    public static final int LPMS_FILTER_PRESET_DYNAMIC = 0;
    public static final int LPMS_FILTER_PRESET_STRONG= 1;
    public static final int LPMS_FILTER_PRESET_MEDIUM= 2;
    public static final int LPMS_FILTER_PRESET_WEAK = 3;
    //Low Pass Filter
    public static final int LPMS_LOW_FILTER_OFF = 0;
    public static final int LPMS_LOW_FILTER_40HZ = 1;
    public static final int LPMS_LOW_FILTER_20HZ = 2;
    public static final int LPMS_LOW_FILTER_4HZ = 3;
    public static final int LPMS_LOW_FILTER_2HZ = 4;
    public static final int LPMS_LOW_FILTER_04HZ = 5;
    // Gyro Range
    public static final int LPMS_GYR_RANGE_125DPS  = 125;
    public static final int LPMS_GYR_RANGE_245DPS  = 245;
    public static final int LPMS_GYR_RANGE_250DPS  = 250;
    public static final int LPMS_GYR_RANGE_500DPS  = 500;
    public static final int LPMS_GYR_RANGE_1000DPS  = 1000;
    public static final int LPMS_GYR_RANGE_2000DPS  = 2000;
    // Acc Range
    public static final int LPMS_ACC_RANGE_2G = 2;
    public static final int LPMS_ACC_RANGE_4G = 4;
    public static final int LPMS_ACC_RANGE_8G = 8;
    public static final int LPMS_ACC_RANGE_16G = 16;
    // Mag Range
    public static final int LPMS_MAG_RANGE_4GAUSS = 4;
    public static final int LPMS_MAG_RANGE_8GAUSS = 8;
    public static final int LPMS_MAG_RANGE_12GAUSS =12;
    public static final int LPMS_MAG_RANGE_16GAUSS =16;
    public static final int PARAMETER_SET_DELAY =10;
    // Connection status
    public static final int SENSOR_STATUS_CONNECTED = 1;
    public static final int SENSOR_STATUS_CONNECTING = 2;
    public static final int SENSOR_STATUS_DISCONNECTED = 3;
    // Offset mode
    public static final int  LPMS_OFFSET_MODE_OBJECT = 0;
    public static final int  LPMS_OFFSET_MODE_HEADING = 1;
    public static final int  LPMS_OFFSET_MODE_ALIGNMENT = 2;

    //Flash State
    public static final int FLASH_STATE_OK              = 0;
    public static final int FLASH_STATE_MEMORY_FULL     = 1;
    public static final int FLASH_STATE_META_TABLE_FULL = 2;
    public static final int FLASH_STATE_MEMORY_ERROR    = 3;



    final String TAG = "LpmsB2";
    final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final UUID MY_UUID_SERVICE        = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB");
    final UUID MY_UUID_CHARACTERISTIC = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB");
    final int PACKET_ADDRESS0 	= 0;
    final int PACKET_ADDRESS1 	= 1;
    final int PACKET_FUNCTION0 	= 2;
    final int PACKET_FUNCTION1 	= 3;
    final int PACKET_LENGTH0 	= 4;
    final int PACKET_LENGTH1 	= 5;
    final int PACKET_RAW_DATA 	= 6;
    final int PACKET_LRC_CHECK0 = 7;
    final int PACKET_LRC_CHECK1 = 8;
    final int PACKET_END 		= 9;

   	//////////////////////////////////////////////
    // Command Registers
   	//////////////////////////////////////////////
    final int REPLY_ACK 			= 0;
    final int REPLY_NACK 			= 1;
    final int UPDATE_FIRMWARE 		= 2;
    final int UPDATE_IAP 			= 3;
    final int GET_CONFIG 			= 4;
    final int GET_STATUS 			= 5;
    final int GOTO_COMMAND_MODE 	= 6;
    final int GOTO_STREAM_MODE 		= 7;
    final int GET_SENSOR_DATA 		= 9;
    final int SET_TRANSMIT_DATA 	= 10;
    final int SET_STREAM_FREQ 		= 11;
	 // Register value save and reset
	 final int WRITE_REGISTERS    	= 15;
	 final int RESTORE_FACTORY_VALUE = 16;
	 // Reference setting and offset reset
	 final int RESET_REFERENCE  	= 17;
	 final int SET_ORIENTATION_OFFSET = 18;
	 final int RESET_ORIENTATION_OFFSET= 82;
	//IMU ID setting
	final int SET_IMU_ID 			= 20;
	final int GET_IMU_ID 			= 21;
	// Gyroscope settings
	final int START_GYR_CALIBRA 	= 22;
	final int ENABLE_GYR_AUTOCAL   	= 23;
	final int ENABLE_GYR_THRES 		= 24;
	final int SET_GYR_RANGE    		= 25;
	final int GET_GYR_RANGE    		= 26;
	// Accelerometer settings
	final int SET_ACC_BIAS     		= 27;
	final int GET_ACC_BIAS     		= 28;
	final int SET_ACC_ALIGN_MATRIX 	= 29;
	final int GET_ACC_ALIGN_MATRIX 	= 30;
	final int SET_ACC_RANGE    		= 31;
	final int GET_ACC_RANGE    		= 32;
	final int SET_GYR_ALIGN_BIAS   	= 48;
	final int GET_GYR_ALIGN_BIAS   	= 49;
	final int SET_GYR_ALIGN_MATRIX 	= 50;
	final int GET_GYR_ALIGN_MATRIX 	= 51;
	// Magnetometer settings
	final int SET_MAG_RANGE    		= 33;
	final int GET_MAG_RANGE    		= 34;
	final int SET_HARD_IRON_OFFSET 	= 35;
	final int GET_HARD_IRON_OFFSET 	= 36;
	final int SET_SOFT_IRON_MATRIX 	= 37;
	final int GET_SOFT_IRON_MATRIX 	= 38;
	final int SET_FIELD_ESTIMATE   	= 39;
	final int GET_FIELD_ESTIMATE   	= 40;
	final int SET_MAG_ALIGNMENT_MATRIX  = 76;
	final int SET_MAG_ALIGNMENT_BIAS = 77;
	final int SET_MAG_REFRENCE 		= 78;
	final int GET_MAG_ALIGNMENT_MATRIX = 79;
	final int GET_MAG_ALIGNMENT_BIAS = 80;
	final int GET_MAG_REFERENCE		= 81;
	// Filter settings
	final int SET_FILTER_MODE  		= 41;
	final int GET_FILTER_MODE  		= 42;
	final int SET_FILTER_PRESET		= 43;
	final int GET_FILTER_PRESET		= 44;
	final int SET_LIN_ACC_COMP_MODE	= 67;
	final int GET_LIN_ACC_COMP_MODE	= 68;

    // Debug Flash Logging
    final int START_DEBUG_LOGGING         = 110;
    final int STOP_DEBUG_LOGGING          = 111;
    final int CLEAR_DEBUG_LOG             = 112;
    final int FULL_FLASH_ERASE            = 113;
    final int GET_DEBUG_LOGGING_STATUS    = 114;
    final int GET_DEBUG_LOG_TABLE_SIZE    = 115;
    final int GET_DEBUG_LOG_TABLE         = 116;
    final int GET_DEBUG_LOG_SIZE          = 117;
    final int GET_DEBUG_LOG               = 118;
    final int GET_FLASH_MEMORY_STATE      = 119;

    //////////////////////////////////////////////
    // Status register contents
    //////////////////////////////////////////////
	final int SET_CENTRI_COMP_MODE 	= 69;
	final int GET_CENTRI_COMP_MODE 	= 70;
	final int SET_RAW_DATA_LP  		= 60;
	final int GET_RAW_DATA_LP  		= 61;
    final int SET_TIME_STAMP		= 66;
    final int SET_LPBUS_DATA_MODE 	= 75;
    final int GET_FIRMWARE_VERSION 	= 47;
    final int GET_BATTERY_LEVEL		= 87;
    final int GET_BATTERY_VOLTAGE   = 88;
    final int GET_CHARGING_STATUS   = 89;
    final int GET_SERIAL_NUMBER     = 90;
    final int GET_DEVICE_NAME       = 91;
    final int GET_FIRMWARE_INFO     = 92;
    final int START_SYNC            = 96;
    final int STOP_SYNC             = 97;
    final int GET_PING              = 98;
    final int GET_TEMPERATURE       = 99;

   	//////////////////////////////////////////////
    // Configuration register contents
   	//////////////////////////////////////////////
    public static final int LPMS_GYR_AUTOCAL_ENABLED = 0x00000001 << 30;
    public static final int LPMS_LPBUS_DATA_MODE_16BIT_ENABLED = 0x00000001 << 22;
    public static final int LPMS_LINACC_OUTPUT_ENABLED = 0x00000001 << 21;
    public static final int LPMS_DYNAMIC_COVAR_ENABLED = 0x00000001 << 20;
    public static final int LPMS_ALTITUDE_OUTPUT_ENABLED = 0x00000001 << 19;
    public static final int LPMS_QUAT_OUTPUT_ENABLED = 0x00000001 << 18;
    public static final int LPMS_EULER_OUTPUT_ENABLED = 0x00000001 << 17;
    public static final int LPMS_ANGULAR_VELOCITY_OUTPUT_ENABLED = 0x00000001 << 16;
    public static final int LPMS_GYR_CALIBRA_ENABLED = 0x00000001 << 15;
    public static final int LPMS_HEAVEMOTION_OUTPUT_ENABLED = 0x00000001 << 14;
    public static final int LPMS_TEMPERATURE_OUTPUT_ENABLED = 0x00000001 << 13;
    public static final int LPMS_GYR_RAW_OUTPUT_ENABLED = 0x00000001 << 12;
    public static final int LPMS_ACC_RAW_OUTPUT_ENABLED = 0x00000001 << 11;
    public static final int LPMS_MAG_RAW_OUTPUT_ENABLED = 0x00000001 << 10;
    public static final int LPMS_PRESSURE_OUTPUT_ENABLED = 0x00000001 << 9;
    final int LPMS_STREAM_FREQ_5HZ_ENABLED      = 0x00000000;
    final int LPMS_STREAM_FREQ_10HZ_ENABLED     = 0x00000001;
    final int LPMS_STREAM_FREQ_25HZ_ENABLED     = 0x00000002;
    final int LPMS_STREAM_FREQ_50HZ_ENABLED     = 0x00000003;
    final int LPMS_STREAM_FREQ_100HZ_ENABLED    = 0x00000004;
    final int LPMS_STREAM_FREQ_200HZ_ENABLED    = 0x00000005;
    final int LPMS_STREAM_FREQ_400HZ_ENABLED    = 0x00000006;
    final int LPMS_STREAM_FREQ_MASK             = 0x00000007;

    final int MAX_BUFFER = 512;
    final int DATA_QUEUE_SIZE = 64;
    final static float r2d = 57.2958f;
    // status
    int connectionStatus = SENSOR_STATUS_DISCONNECTED;

    // Protocol parsing related
    int rxState = PACKET_END;
    byte[] rxBuffer = new byte[MAX_BUFFER];
    byte[] txBuffer = new byte[MAX_BUFFER];
    byte[] txBLEBuffer = new byte[20];
    byte[] rawTxData = new byte[MAX_BUFFER];
    byte[] rawRxBuffer = new byte[MAX_BUFFER];
    int currentAddress = 0;
    int currentFunction = 0;
    int currentLength = 0;
    int rxIndex = 0;
    byte b = 0;
    int lrcCheck = 0;
    int nBytes = 0;
    boolean waitForAck = false;
    boolean waitForData = false;
    byte inBytes[] = new byte[2];

    // Connection related
    BluetoothDevice mDevice;
    String mAddress;
    InputStream mInStream;
    OutputStream mOutStream;
    BluetoothSocket mSocket;
    BluetoothAdapter mAdapter;
    //BLE
    BluetoothGatt mBluetoothGatt;
    BluetoothGattService sendService = null;
    BluetoothGattCharacteristic sendCharacteristic = null;

    // Settings related
    int imuId = 0;
    int gyrRange = 0;
    int accRange = 0;
    int magRange = 0;
    int streamingFrequency = 0;
    int filterMode = 0;
    int magCorrection = 0;
    int lowPassFilter = 0;
    float batteryLevel = 0;
    float batteryVoltage = 0;
    boolean isStreamMode = false;

    int configurationRegister = 0;
    private boolean configurationRegisterReady = false;
    private String serialNumber = "";
    private boolean serialNumberReady = false;
    private String deviceName = "";
    private boolean deviceNameReady = false;
    private String firmwareInfo="";
    private boolean firmwareInfoReady = false;
    private String firmwareVersion;

    boolean accEnable = false;
    boolean gyrEnable = false;
    boolean magEnable = false;
    boolean angularVelEnable = false;
    boolean quaternionEnable = false;
    boolean eulerAngleEnable = false;
    boolean linAccEnable = false;
    boolean pressureEnable = false;
    boolean altitudeEnable = false;
    boolean temperatureEnable = false;
    boolean heaveEnable = false;
    boolean sixteenBitDataEnable = false;
    boolean resetTimestampFlag = false;
    boolean diffFirmwareCheck = false;
    boolean readCharacteristicReady = false;

    boolean newDataFlag = false;
    LinkedBlockingDeque<LpmsBData> dataQueue = new LinkedBlockingDeque<LpmsBData>();
    LpmsBData mLpmsBData = new LpmsBData();
    int frameCounter = 0;
    // Data Logging
    DataLogger dl = new DataLogger();
    private float difftime = 0;
    //Flash Debug Logging
    final int LogEntrySize = 64;
    int debugLoggingStatus = 1;
    int debugLogSize = 0;
    int debugLogSizeIndex = 0;
    int updateProgress = 0;
    int flashState = FLASH_STATE_OK;
    //Thread pool
    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);

    public LpmsB2()
    {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    /////////////////////////////////////////////////////////////////////
    // Public methods
    /////////////////////////////////////////////////////////////////////
    /**
     * @param address is a valid Bluetooth MAC address
     * @return false or true
     * if true , the sensor will be at command mode.And after that, you should
     *  use setAcquisitionParameters() to set some  sensor param.
     */
    public boolean connect(String address) {
        //Log.i(TAG, "[LpmsBThread] Another connection establishing: " +address);
        if (connectionStatus != SENSOR_STATUS_DISCONNECTED) {
            //Log.i(TAG, "[LpmsBThread] Another connection establishing: " + mAddress);
            return false;
        }

        if (mAdapter == null) {
            //Log.i(TAG, "[LpmsBThread] Didn't find Bluetooth adapter");
            connectionStatus = SENSOR_STATUS_DISCONNECTED;
            return false;
        }

        mAddress = address;
        connectionStatus = SENSOR_STATUS_CONNECTING;
        //Log.i(TAG, "[LpmsBThread] Connecting to: " + mAddress);

        mAdapter.cancelDiscovery();

        //Log.i(TAG, "[LpmsBThread] Getting device");

        try {
            mDevice = mAdapter.getRemoteDevice(mAddress);
        } catch (IllegalArgumentException e) {
            //Log.i(TAG, "[LpmsBThread] Invalid Bluetooth address", e);
            connectionStatus = SENSOR_STATUS_DISCONNECTED;
            return false;
        }

        //device pairing
        if(mDevice.getBondState() != BluetoothDevice.BOND_BONDED){
            if(mDevice.createBond()){
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                connectionStatus = SENSOR_STATUS_DISCONNECTED;
                return false;
            }
        }

        mSocket = null;
        //Log.i(TAG, "[LpmsBThread] Creating socket");

        try {
            //mDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
        } catch (Exception e) {
            //Log.i(TAG, "[LpmsBThread] Socket create() failed", e);
            connectionStatus = SENSOR_STATUS_DISCONNECTED;
            return false;
        }

        //Log.i(TAG, "[LpmsBThread] Trying to connect..");
        try {
            mSocket.connect();
        } catch (IOException e) {
            try{
                mSocket.close();
            }catch (IOException closeException){

            }
            //Log.i(TAG, "[LpmsBThread] Couldn't connect to device", e);
            connectionStatus = SENSOR_STATUS_DISCONNECTED;
            return false;
        }

        Log.i(TAG, "[LpmsBThread] Connected!");
        try {
            mInStream = mSocket.getInputStream();
            mOutStream = mSocket.getOutputStream();

        } catch (IOException e) {
        	//Log.i(TAG, "[LpmsBThread] Streams not created", e);
            connectionStatus = SENSOR_STATUS_DISCONNECTED;
            return false;
        }
        Thread t = new Thread(new ClientReadThread());
        t.start();
        connectionStatus = SENSOR_STATUS_CONNECTING;

        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                initialization();
            }
        });
        frameCounter = 0;
        return true;
    }

    public void initialization(){
        _setCommandMode();
        _getSerialNumber();
        _getSensorSettings();
        if(firmwareInfoReady){
            diffFirmwareCheck = firmwareInfo.contains("MWT");
            Log.d(TAG,"what's different about Firmware:"+diffFirmwareCheck);
        }
        setStreamingMode();
        connectionStatus = SENSOR_STATUS_CONNECTED;
    }

    /**
     *  disconnect cancel discovery and socket close.
     * @return
     */
    public boolean disconnect() {
    	boolean res = false;
    	if (connectionStatus != SENSOR_STATUS_DISCONNECTED)
    		res = true;
    	//TODO add gatt disconnect
    	if(mBluetoothGatt!=null){
			mBluetoothGatt.close();
			connectionStatus = SENSOR_STATUS_DISCONNECTED;
			mBluetoothGatt = null;
			//isGattServicesSuccess = false;
			res = true;
			Log.i(TAG, "[LpmsBBLEThread] BLE Disconnect ");
			return res;
		}
    	mAdapter.cancelDiscovery();
        try {
            if(mSocket!=null)
            mSocket.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        // TODO: reset data
        // clean up
        // dataQueue.clear();
        //stop/Logging();
        //Log.i(TAG, "[LpmsBThread] Connection closed test");
        connectionStatus = SENSOR_STATUS_DISCONNECTED;
        return res;
    }

    public String getAddress() {
        return mAddress;
    }

    public BluetoothDevice getBluetoothDevice() {
        return mDevice;
    }
    /**
     * use command mode to communicate with sensor.
     */
    public void setCommandMode()
    {
        if (!assertConnected())
            return;
        waitForAck = true;
        lpbusSetNone(GOTO_COMMAND_MODE);
        //Log.i(TAG, " Send GOTO_COMMAND_MODE");
        _waitForAckLoop();
        isStreamMode = false;
    }

    /**
     * change sensor to Streaming mode.
     * it will send data too much ,and you have better to change sensor mode to set sensor or get some data you need.
     */
    public void setStreamingMode()
    {
        if (!assertConnected())
            return;
        waitForAck = true;
        lpbusSetNone(GOTO_STREAM_MODE);
        //Log.i(TAG, "Send GOTO_STREAM_MODE");
        _waitForAckLoop();
        isStreamMode = true;
    }

    public void setImuId(final int id)
    {
        if (!assertConnected())
            return;
        //Open Thread to send command;
        fixedThreadPool.execute(new CommandRunnable(SET_IMU_ID,id));
    }

    public int getImuId()
    {
        return imuId;
    }

    public int getGyroRange()
    {
        return gyrRange;
    }

    public void setGyroRange(final int range)
    {
        if (!assertConnected())
            return;
        if (range == LPMS_GYR_RANGE_125DPS ||
                range == LPMS_GYR_RANGE_245DPS ||
                range == LPMS_GYR_RANGE_500DPS ||
                range == LPMS_GYR_RANGE_1000DPS ||
                range == LPMS_GYR_RANGE_2000DPS) {

            fixedThreadPool.execute(new CommandRunnable(SET_GYR_RANGE,range));
        }
    }

    public int getAccRange()
    {
        return accRange;
    }

    public void setAccRange(final int range)
    {
        if (!assertConnected())
            return;
        if (range == LPMS_ACC_RANGE_2G ||
                range == LPMS_ACC_RANGE_4G ||
                range == LPMS_ACC_RANGE_8G ||
                range == LPMS_ACC_RANGE_16G ) {

            fixedThreadPool.execute(new CommandRunnable(SET_ACC_RANGE,range));
        }
    }


    public int getMagRange()
    {
        return magRange;
    }

    public void setMagRange(final int range)
    {
        if (!assertConnected())
            return;
        if (range == LPMS_MAG_RANGE_4GAUSS ||
                range == LPMS_MAG_RANGE_8GAUSS ||
                range == LPMS_MAG_RANGE_12GAUSS ||
                range == LPMS_MAG_RANGE_16GAUSS ) {

            fixedThreadPool.execute(new CommandRunnable(SET_MAG_RANGE,range));
        }
    }

    public int getFilterMode()
    {
        return filterMode;
    }

    public void setFilterMode(final int mode)
    {
        if (!assertConnected())
            return;
        if (mode == LPMS_FILTER_GYR ||
                mode == LPMS_FILTER_GYR_ACC ||
                mode == LPMS_FILTER_GYR_ACC_MAG ||
                mode == LPMS_FILTER_MADGWICK_GYR_ACC ||
                mode == LPMS_FILTER_MADGWICK_GYR_ACC_MAG) {

            fixedThreadPool.execute(new CommandRunnable(SET_FILTER_MODE,mode));
        }
    }

    public int getMAGCorrection()
    {
        return magCorrection;
    }

    public void setMAGCorrection(final int preset)
    {
        if (!assertConnected())
            return;
        if (preset == LPMS_FILTER_PRESET_DYNAMIC ||
                preset == LPMS_FILTER_PRESET_STRONG ||
                preset == LPMS_FILTER_PRESET_MEDIUM ||
                preset == LPMS_FILTER_PRESET_WEAK) {

            fixedThreadPool.execute(new CommandRunnable(SET_FILTER_PRESET,preset));
        }
    }

    public int getLowPassFilter()
    {
        return lowPassFilter;
    }

    public void setLowPassFilter(final int lowpass)
    {
        if (!assertConnected())
            return;
        if (lowpass == LPMS_LOW_FILTER_OFF ||
                lowpass == LPMS_LOW_FILTER_40HZ ||
                lowpass == LPMS_LOW_FILTER_20HZ ||
                lowpass == LPMS_LOW_FILTER_4HZ ||
                lowpass == LPMS_LOW_FILTER_2HZ ||
                lowpass == LPMS_LOW_FILTER_04HZ) {

            fixedThreadPool.execute(new CommandRunnable(SET_RAW_DATA_LP,lowpass));
        }
    }
    public int getStreamFrequency()
    {
        return streamingFrequency;
    }

    public void setStreamFrequency(final int freq)
    {
        if (!assertConnected())
            return;
        if (freq == LPMS_STREAM_FREQ_5HZ||
                freq == LPMS_STREAM_FREQ_10HZ ||
                freq == LPMS_STREAM_FREQ_25HZ ||
                freq == LPMS_STREAM_FREQ_50HZ ||
                freq == LPMS_STREAM_FREQ_100HZ ||
                freq == LPMS_STREAM_FREQ_200HZ ||
                freq == LPMS_STREAM_FREQ_400HZ ) {

            fixedThreadPool.execute(new CommandRunnable(SET_STREAM_FREQ,freq));
        }
    }

    public void setTransmissionData(final int v)
    {
        if (!assertConnected())
            return;
        fixedThreadPool.execute(new CommandRunnable(SET_TRANSMIT_DATA,v));
    }

    public void enableGyroData(boolean b)
    {
        _enableConfigUtil(b,LPMS_GYR_RAW_OUTPUT_ENABLED);
    }

    public boolean isGyroDataEnabled() {
        return gyrEnable;
    }

    public void enableAccData(boolean b)
    {
        _enableConfigUtil(b,LPMS_ACC_RAW_OUTPUT_ENABLED);
    }

    public boolean isAccDataEnabled() {
        return accEnable;
    }

    public void enableMagData(boolean b)
    {
        _enableConfigUtil(b,LPMS_MAG_RAW_OUTPUT_ENABLED);
    }

    public boolean isMagDataEnabled() {
        return magEnable;
    }

    public void enableAngularVelData(boolean b)
    {
        _enableConfigUtil(b,LPMS_ANGULAR_VELOCITY_OUTPUT_ENABLED);
    }

    public boolean isAngularVelDataEnable() {
        return angularVelEnable;
    }

    public void enableQuaternionData(boolean b)
    {
        _enableConfigUtil(b,LPMS_QUAT_OUTPUT_ENABLED);
    }

    public boolean isQuaternionDataEnabled() {
        return quaternionEnable;
    }

    public void enableEulerData(boolean b)
    {
        _enableConfigUtil(b,LPMS_EULER_OUTPUT_ENABLED);
    }

    public boolean isEulerDataEnabled() {
        return eulerAngleEnable;
    }

    public void enableLinAccData(boolean b)
    {
        _enableConfigUtil(b,LPMS_LINACC_OUTPUT_ENABLED);
    }

    public boolean isLinAccDataEnabled() {
        return linAccEnable;
    }

    public void enablePressureData(boolean b)
    {
        _enableConfigUtil(b,LPMS_PRESSURE_OUTPUT_ENABLED);
    }

    public boolean isPressureDataEnabled(){
        return pressureEnable;
    }

    public void enableAltitudeData(boolean b)
    {
        _enableConfigUtil(b,LPMS_ALTITUDE_OUTPUT_ENABLED);
    }

    public boolean isAltitudeDataEnabled() {
        return altitudeEnable;
    }

    public void enableTemperatureData(boolean b)
    {
        _enableConfigUtil(b,LPMS_TEMPERATURE_OUTPUT_ENABLED);
    }

    public boolean isTemperatureDataEnabled() {
        return temperatureEnable;
    }

    public void enable16BitData()
    {
        if (!assertConnected())
            return;
        _setDataMode(1);
    }

    public void enable32BitData()
    {
        if (!assertConnected())
            return;
        _setDataMode(0);
    }

    public boolean is16BitDataEnabled()
    {
        if (sixteenBitDataEnable)
            return true;
        else
            return false;
    }

    /**
     * @return
     *		Return the data length of the sensor.
     */
    public int hasNewData() {
        int n;
        synchronized (dataQueue) {
            n = dataQueue.size();
        }
        return n;
    }

    /**
     * @return LpmsBData
     * you can use this to read out data .
     */
    public LpmsBData getLpmsBData() {
        LpmsBData d=null;
        if (!assertConnected())
            return d;
        if (!isStreamMode) {

            synchronized (dataQueue) {
                while (dataQueue.size() > 0) {
                    d = dataQueue.getLast();
                    dataQueue.removeLast();
                }
            }
            waitForData = true;
            lpbusSetNone(GET_SENSOR_DATA);
            //Log.i(TAG, "Send GET_SENSOR_DATA");
            _waitForDataLoop();
        }else{
        synchronized (dataQueue) {
            if (dataQueue.size() > 0) {
                d = dataQueue.getLast();
                dataQueue.removeLast();
                }
            }
        }
        return d;
    }

    public String getSerialNumber()
    {
        return serialNumber;
    }

    public String getDeviceName()
    {
        return deviceName;
    }

    public boolean isStreamingMode()
    {
        return isStreamMode;
    }

    public int getConnectionStatus() {
        return connectionStatus;
    }

    public String getFirmwareInfo()
    {
        return firmwareInfo;
    }

    public void startSyncSensor()
    {
        if (!assertConnected())
            return;
        lpbusSetNone(START_SYNC);
        waitForAck = true;
        _waitForAckLoop();
    }

    public void stopSyncSensor()
    {
        if (!assertConnected())
            return;
        lpbusSetNone(STOP_SYNC);
        waitForAck = true;
        _waitForAckLoop();
    }

    private void testPing()
    {
        if (!assertConnected())
            return;
        lpbusSetNone(GET_PING);
        //Log.i(TAG, "Send GET_PING");
    }

    public void resetFactorySettings()
    {
        if (!assertConnected())
            return;
        _setCommandMode();
        waitForAck = true;
        lpbusSetNone(RESTORE_FACTORY_VALUE);
        _waitForAckLoop();
        _getSensorSettings();
        _saveParameters();
        if (isStreamMode)
            _setStreamingMode();
    }

    public void setOrientationOffset(int offset){

        if (!assertConnected())
            return;
        if (offset == LPMS_OFFSET_MODE_ALIGNMENT ||
                offset == LPMS_OFFSET_MODE_HEADING ||
                offset == LPMS_OFFSET_MODE_OBJECT) {
            //lpbusSetInt32(SET_ORIENTATION_OFFSET, offset);
            fixedThreadPool.execute(new CommandRunnable(SET_ORIENTATION_OFFSET,offset));
        }
    }

    public void resetOrientationOffset() {
        if (!assertConnected())
            return;
        //lpbusSetNone(RESET_ORIENTATION_OFFSET);
        fixedThreadPool.execute(new CommandRunnable(RESET_ORIENTATION_OFFSET,0));
    }

    public void resetTimestamp() {
        if (!assertConnected())
            return;
        lpbusSetInt32(SET_TIME_STAMP, 0);
    }

    public void setTimestamp(int ts) {

        if (!assertConnected())
            return;
        lpbusSetInt32(SET_TIME_STAMP, ts);
    }

    public void sendBatteryPercentage() {
        if (!assertConnected())
            return;
        lpbusSetNone(GET_BATTERY_LEVEL);
        //Log.i(TAG, "Send  GET_BATTERY_LEVEL");
    }

    public void sendBatteryVoltage() {
        if (!assertConnected())
            return;
        lpbusSetNone(GET_BATTERY_VOLTAGE);
        //Log.i(TAG, "Send  GET_BATTERY_VOLTAGE");
    }

    public float getBatteryLevel(){
        return batteryLevel;
    }
    public float getBatteryVoltage(){
        return batteryVoltage;
    }

    public void getChargingStatus() {
        if (!assertConnected())
            return;
        lpbusSetNone(GET_CHARGING_STATUS);
        //Log.i(TAG, "Send  GET_CHARGING_STATUS");
    }

    //Flash Data Log
    public boolean startDebugLogging(){
        if (!assertConnected())
            return false;
        _setCommandMode();
        waitForAck = true;
        lpbusSetNone(START_DEBUG_LOGGING);
        _waitForAckLoop();
        if (isStreamMode)
            _setStreamingMode();
        return true;
    }

    public boolean stopDebugLogging(){
        if (!assertConnected())
            return false;
        _setCommandMode();
        waitForAck = true;
        lpbusSetNone(STOP_DEBUG_LOGGING);
        _waitForAckLoop();
        if (isStreamMode)
            _setStreamingMode();
        return true;
    }
    public boolean clearDebugLog(){
        if (!assertConnected())
            return false;
        _setCommandMode();
        waitForAck = true;
        lpbusSetNone(CLEAR_DEBUG_LOG);
        _waitForAckLoop();
        if (isStreamMode)
            _setStreamingMode();
        return true;
    }
    //Thread block
    public boolean fullFlashErase(){
        if (!assertConnected())
            return false;
        _setCommandMode();
        waitForAck = true;
        lpbusSetNone(FULL_FLASH_ERASE);
        _waitForAckLoopWithoutTimeout();
        if (isStreamMode)
            _setStreamingMode();
        return true;
    }
    public boolean getDebugLogStatus(){
        if (!assertConnected())
            return false;
        _setCommandMode();
        waitForData = true;
        lpbusSetNone(GET_DEBUG_LOGGING_STATUS);
        _waitForDataLoop();
        if (isStreamMode)
            _setStreamingMode();
        return debugLoggingStatus==0?true:false;
    }
    public int getFlashState(){
        if (!assertConnected())
            return flashState;
        _setCommandMode();
        waitForData = true;
        lpbusSetNone(GET_FLASH_MEMORY_STATE);
        _waitForDataLoop();
        if (isStreamMode)
            _setStreamingMode();
        return flashState;
    }

    public int getDebugLogSize(){
        if (!assertConnected())
            return 0;
        _setCommandMode();
        waitForAck = true;
        lpbusSetNone(GET_DEBUG_LOG_SIZE);
        _waitForAckLoop();
        if (isStreamMode)
            _setStreamingMode();
        return debugLogSize;
    }

    public void getDebugLog(int indexStart,int indexStop){
        if (!assertConnected())
            return;
        _setCommandMode();
        frameCounter = 0;
        debugLogSize = indexStop - indexStart +1;
        debugLogSizeIndex = 0;
        sendGetDebugLog();
    }
    public int getUpdataProgress(){
        return updateProgress;
    }

    void sendGetDebugLog(){
        int indexStop;
        if (debugLogSize - debugLogSizeIndex>2){
            indexStop = debugLogSizeIndex+2;
        }else {
            indexStop = debugLogSizeIndex-1;
        }
        byte[] dataBF = new byte[8];
        convertIntToTxbytes(debugLogSizeIndex,0,dataBF);
        convertIntToTxbytes(indexStop,4,dataBF);
//        Log.d(TAG,"debugLogSizeIndex:"+debugLogSizeIndex);
//        Log.d(TAG,"indexStop:"+indexStop);
        lpbusSetData(GET_DEBUG_LOG,8,dataBF);
    }

    public boolean isDebugSaving(){
        return dl.isDebugLogging();
    }

    public boolean stopDebugSaving(){
        return  dl.stopDebugLogging();
    }


    //Data Log
    public boolean startLogging() {
        frameCounter = 0;
        if (connectionStatus == SENSOR_STATUS_DISCONNECTED) {
            dl.setStatusMesg("No sensor connected");
            return false;
        }
        if (dl.startLogging()) {
            //resetTimestamp();
            return true;
        }
        return false;
    }

    public boolean stopLogging() {
        return dl.stopLogging();
    }

    public boolean isLogging() {
        return dl.isLogging();
    }

    public String getLoggerStatusMesg() {
        return dl.getStatusMesg();
    }

    public String getOutputFilename() {
        return dl.getOutputFilename();
    }

    /////////////////////////////////////////////////////////////////////
    // LP Bus Related
    /////////////////////////////////////////////////////////////////////
    void lpbusSetInt32(int command, int v) {
        for (int i = 0; i < 4; ++i) {
            rawTxData[i] = (byte) (v & 0xff);
            v = v >> 8;
        }
        
        if(mBluetoothGatt!=null){
    		sendBLEData(command, 4);
    	}else{
            sendData(command, 4);
    	}
    }

    void lpbusSetNone(int command) {
       // Log.i(TAG, command+"");
    	if(mBluetoothGatt!=null){
    		sendBLEData(command, 0);
    	}else{
             sendData(command, 0);
    	}
    }

    void lpbusSetData(int command, int length, byte[] dataBuffer) {
        for (int i = 0; i < length; ++i) {
            rawTxData[i] = dataBuffer[i];
        }

        if(mBluetoothGatt!=null){
    		sendBLEData(command,length);
    	}else{
            sendData(command, length);
    	}
    }

    void parseDebugSensorData(int nData) {
        int o = 0;
        for (int n = 0;n<nData;n++){
            mLpmsBData.frameNumber = frameCounter;
            frameCounter++;
            mLpmsBData.timestamp = (float) convertRxbytesToInt(o, rxBuffer) * 0.0025f;
            o += 4;
            mLpmsBData.acc[0] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.acc[1] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.acc[2] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.gyr[0] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
            mLpmsBData.gyr[1] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
            mLpmsBData.gyr[2] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
            mLpmsBData.mag[0] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.mag[1] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.mag[2] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.quat[0] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.quat[1] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.quat[2] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.quat[3] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.pressure = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.temperature = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            dl.logDebugLpmsData(mLpmsBData);
        }
    }

    void parseSensorData() {
        int o = 0;

        mLpmsBData.imuId = imuId;
//           float ext = convertRxbytesToInt(o, rxBuffer);
//    	//这一次的timestamp 减去 上一次的timestamp
//    	float  diff = ext - mLpmsBData.timestamp;
//    	//Log.i("test",""+diff);
//    	//50hz diff = 8,100hz ==> 4 , 400hz ==> 1 25hz=16  10hz=40
//    	int rate = 40;
//    	if(diff > rate){
//   		 difftime +=diff-rate;
//   		 Log.i("test", "test "+ difftime/rate );
//    	}
        //TODO Test 上一次的timestamp 重新被赋值为这一次的timestamp
        mLpmsBData.frameNumber = frameCounter;
        frameCounter++;
        if(diffFirmwareCheck){
            mLpmsBData.timestamp = (float) convertRxbytesToInt(o, rxBuffer)*0.02f;//MWT firmware info
            o += 4;
        }else{
            mLpmsBData.timestamp = (float) convertRxbytesToInt(o, rxBuffer)*0.0025f;
            o += 4;
        }
        if ( gyrEnable )
        {
            mLpmsBData.gyr[0] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
            mLpmsBData.gyr[1] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
            mLpmsBData.gyr[2] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
        }

        if ( accEnable )
        {
            mLpmsBData.acc[0] = (convertRxbytesToFloat(o, rxBuffer));
            o += 4;
            mLpmsBData.acc[1] = (convertRxbytesToFloat(o, rxBuffer));
            o += 4;
            mLpmsBData.acc[2] = (convertRxbytesToFloat(o, rxBuffer));
            o += 4;
        }

        if ( magEnable )
        {
            mLpmsBData.mag[0] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.mag[1] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.mag[2] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
        }

        if ( angularVelEnable )
        {
            mLpmsBData.angVel[0] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
            mLpmsBData.angVel[1] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
            mLpmsBData.angVel[2] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
        }

        if ( quaternionEnable )
        {
            mLpmsBData.quat[0] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.quat[1] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.quat[2] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.quat[3] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
        }

        if ( eulerAngleEnable )
        {
            mLpmsBData.euler[0] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
            mLpmsBData.euler[1] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
            mLpmsBData.euler[2] = convertRxbytesToFloat(o, rxBuffer) * r2d;
            o += 4;
        }

        if ( linAccEnable )
        {
            mLpmsBData.linAcc[0] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.linAcc[1] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
            mLpmsBData.linAcc[2] = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
        }

        if ( pressureEnable )
        {
            mLpmsBData.pressure = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
        }

        if ( altitudeEnable )
        {
            mLpmsBData.altitude = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
        }

        if ( temperatureEnable )
        {
            mLpmsBData.temperature = convertRxbytesToFloat(o, rxBuffer);
            o += 4;
        }

        if ( heaveEnable )
        {
            mLpmsBData.heave = convertRxbytesToFloat(o, rxBuffer);
            //o += 4;
        }

        synchronized (dataQueue) {
            dl.logLpmsData(mLpmsBData);
            if (dataQueue.size() < DATA_QUEUE_SIZE)
                dataQueue.addFirst(new LpmsBData(mLpmsBData));
            else {
                dataQueue.removeLast();
                dataQueue.addFirst(new LpmsBData(mLpmsBData));
            }
        }
        newDataFlag = true;
    }

    void parseSensorData16Bit() {
        int o = 0;
        mLpmsBData.imuId = imuId;
        mLpmsBData.timestamp = (float) convertRxbytesToInt(o, rxBuffer)*0.0025f;

        o += 4;
        mLpmsBData.frameNumber = frameCounter;
        frameCounter++;

        if ( gyrEnable )
        {
            for (int i = 0; i < 3; ++i) {
                mLpmsBData.gyr[i] = (float) convertRxbytesToShort(o, rxBuffer) / 1000.0f * r2d;
                o += 2;
            }
            //Log.i(TAG, mLpmsBData.gyr[0]+" "+mLpmsBData.gyr[1]+" "+mLpmsBData.gyr[2]);
        }

        if ( accEnable )
        {
            for (int i = 0; i < 3; ++i) {
                mLpmsBData.acc[i] = (float) convertRxbytesToShort(o, rxBuffer) / 1000.0f;
                o += 2;
            }
        }

        if ( magEnable )
        {
            for (int i = 0; i < 3; ++i) {
                mLpmsBData.mag[i] = (float) convertRxbytesToShort(o, rxBuffer) / 100.0f;
                o += 2;
            }
        }

        if ( angularVelEnable )
        {
            for (int i = 0; i < 3; ++i) {
                mLpmsBData.angVel[i] = (float) convertRxbytesToShort(o, rxBuffer) / 1000.0f * r2d;
                o += 2;
            }
        }

        if ( quaternionEnable )
        {
            for (int i = 0; i < 4; ++i) {
                mLpmsBData.quat[i] = (float) convertRxbytesToShort(o, rxBuffer) / 10000.0f;
                o += 2;
            }
        }

        if ( eulerAngleEnable )
        {
            for (int i = 0; i < 3; ++i) {
                mLpmsBData.euler[i] = (float) convertRxbytesToShort(o, rxBuffer) / 10000.0f * r2d;
                o += 2;
            }
        }

        if ( linAccEnable )
        {
            for (int i = 0; i < 3; ++i) {
                mLpmsBData.linAcc[i] = (float) convertRxbytesToShort(o, rxBuffer) / 1000.0f;
                o += 2;
            }
        }

        if ( pressureEnable )
        {
            mLpmsBData.pressure = (float) convertRxbytesToShort(o, rxBuffer) / 100.0f;
            o += 2;
        }

        if ( altitudeEnable )
        {
            mLpmsBData.altitude = (float) convertRxbytesToShort(o, rxBuffer) / 10.0f;
            o += 2;
        }

        if ( temperatureEnable )
        {
            mLpmsBData.temperature = (float) convertRxbytesToShort(o, rxBuffer) / 100.0f;
            o += 2;
        }

        if ( heaveEnable )
        {
            mLpmsBData.heave = (float) convertRxbytesToShort(o, rxBuffer) / 1000.0f;
            //o += 2;
        }

        synchronized (dataQueue) {
            dl.logLpmsData(mLpmsBData);
            if (dataQueue.size() < DATA_QUEUE_SIZE)
                dataQueue.addFirst(new LpmsBData(mLpmsBData));
            else {
                dataQueue.removeLast();
                dataQueue.addFirst(new LpmsBData(mLpmsBData));
            }
        }
        newDataFlag = true;
    }

    void parseFunction() {
        switch (currentFunction) {
            case REPLY_ACK:
                Log.i(TAG, "Received ACK");
                waitForAck = false;
                break;

            case REPLY_NACK:
                Log.i(TAG, "Received NACK");
                waitForAck = false;
                break;

            case GET_CONFIG:
                configurationRegister = convertRxbytesToInt(0, rxBuffer);
                Log.i(TAG, " GET_CONFIG: " + configurationRegister);
                configurationRegisterReady = true;
                waitForData = false;
                break;

            case GET_STATUS:
                waitForData = false;
                break;

            case GET_SENSOR_DATA:
                if ( (configurationRegister & LPMS_LPBUS_DATA_MODE_16BIT_ENABLED) != 0) {
                    parseSensorData16Bit();
                } else {
                    parseSensorData();
                }
                waitForData = false;
                break;

            case GET_IMU_ID:
                imuId = convertRxbytesToInt(0, rxBuffer);
                waitForData = false;
                break;

            case GET_GYR_RANGE:
                gyrRange = convertRxbytesToInt(0, rxBuffer);
                waitForData = false;
                break;

            case GET_ACC_RANGE:
                accRange = convertRxbytesToInt(0, rxBuffer);
                waitForData = false;
                break;

            case GET_MAG_RANGE:
                magRange = convertRxbytesToInt(0, rxBuffer);
                waitForData = false;
                break;

            case GET_FILTER_MODE:
                filterMode = convertRxbytesToInt(0, rxBuffer);
                waitForData = false;
                break;

            case GET_FILTER_PRESET:
                magCorrection = convertRxbytesToInt(0, rxBuffer);
                waitForData = false;
                break;

            case GET_RAW_DATA_LP:
                lowPassFilter = convertRxbytesToInt(0, rxBuffer);
                waitForData = false;
                break;

            case GET_BATTERY_LEVEL:
                 batteryLevel = convertRxbytesToFloat(0, rxBuffer);
                mLpmsBData.batteryLevel = batteryLevel;
                //Log.i(TAG, "GET_BATTERY_LEVEL " +  mLpmsBData.batteryLevel);
                waitForData = false;
                break;

            case GET_CHARGING_STATUS:
                mLpmsBData.chargingStatus = convertRxbytesToInt(0, rxBuffer);
                //Log.i(TAG, "GET_CHARGING_STATUS ");
                waitForData = false;
                break;

            case GET_BATTERY_VOLTAGE:
               batteryVoltage = convertRxbytesToFloat(0, rxBuffer);
                mLpmsBData.batteryVoltage = batteryVoltage;
                //Log.i(TAG, "GET_BATTERY_VOLTAGE " + mLpmsBData.batteryVoltage);
                waitForData = false;
                break;

            case GET_SERIAL_NUMBER:
                serialNumber = convertRxbytesToString(24, rxBuffer);
                Log.i(TAG, serialNumber);
                serialNumberReady = true;
                waitForData = false;
                break;

            case GET_DEVICE_NAME:
                deviceName = convertRxbytesToString(16, rxBuffer);
                Log.i(TAG, deviceName);
                deviceNameReady = true;
                waitForData = false;
                break;

            case GET_FIRMWARE_INFO:
                firmwareInfo = convertRxbytesToString(16, rxBuffer);
                Log.i(TAG, firmwareInfo);
                firmwareInfoReady = true;
                waitForData = false;
                break;

            case GET_FIRMWARE_VERSION:
                int vmajor = convertRxbytesToInt(8, rxBuffer);
                int vminor = convertRxbytesToInt(4, rxBuffer);
                int vbuild = convertRxbytesToInt(0, rxBuffer);
                firmwareVersion  = Integer.toString(vmajor)+"."+Integer.toString(vminor)+"."+ Integer.toString(vbuild);
                //Log.i(TAG,  "FW Ver: " +  firmwareVersion);
                waitForData = false;
                break;

            case GET_PING:
                float mT;
                mT = (float) convertRxbytesToInt(0, rxBuffer)*0.0025f;
                //Log.i(TAG, "GET_PING: " +  mT );
                waitForData = false;
                break;

            case START_SYNC:
                //Log.i(TAG, "START_SYNC");
                waitForAck = false;
                break;

            case STOP_SYNC:
                //Log.i(TAG, "STOP_SYNC");
                waitForAck = false;
                break;

            case SET_TRANSMIT_DATA:
                waitForData = false;
                break;

            case GET_TEMPERATURE:
                mLpmsBData.temperature = convertRxbytesToFloat(0, rxBuffer);
                //Log.i(TAG, "GET_TEMPERATURE");
                waitForData = false;
                break;

            case GET_DEBUG_LOGGING_STATUS:
                debugLoggingStatus = convertRxbytesToInt(0,rxBuffer);
                waitForData = false;
                break;

            case GET_DEBUG_LOG_SIZE:
                debugLogSize = convertRxbytesToInt(0,rxBuffer)/LogEntrySize;
                waitForData = false;
                break;

            case GET_DEBUG_LOG:
                int nData = currentLength/64;
                //Log.d(TAG," parseFunction Get Debug Log! nData:" +nData);
                if (debugLogSizeIndex ==0){
                    dl.startDebugLogging();
                }
                parseDebugSensorData(nData);
                debugLogSizeIndex += nData;
                updateProgress = debugLogSizeIndex*100/debugLogSize;
                if(!dl.isDebugLogging() || debugLogSizeIndex>=debugLogSize){
                    dl.stopDebugLogging();
                    Log.d(TAG,"Debug Logging Download completed");
                    if (isStreamMode)
                        _setStreamingMode();
                }else {
                    sendGetDebugLog();
                }
                waitForData = false;
                break;

            case GET_FLASH_MEMORY_STATE:
                flashState = convertRxbytesToInt(0,rxBuffer);
                waitForData = false;
                break;
        }
        //waitForAck = false;
       // waitForData = false;
    }

    void parse() {
        int lrcReceived = 0;
        //String s="";
        for (int i = 0; i < nBytes; i++) {
            b = rawRxBuffer[i];
        	//s += Integer.toHexString(b& 0xFF) + " " ;
            switch (rxState) {
                case PACKET_END:
                    if (b == 0x3a) {
                        rxState = PACKET_ADDRESS0;
                    }
                    break;

                case PACKET_ADDRESS0:
                    inBytes[0] = b;
                    rxState = PACKET_ADDRESS1;
                    break;

                case PACKET_ADDRESS1:
                    inBytes[1] = b;
                    currentAddress = convertRxbytesToInt16(0, inBytes);
                    imuId = currentAddress;
                    rxState = PACKET_FUNCTION0;
                    break;

                case PACKET_FUNCTION0:
                    inBytes[0] = b;
                    rxState = PACKET_FUNCTION1;
                    break;

                case PACKET_FUNCTION1:
                    inBytes[1] = b;
                    currentFunction = convertRxbytesToInt16(0, inBytes);
                    rxState = PACKET_LENGTH0;
                    break;

                case PACKET_LENGTH0:
                    inBytes[0] = b;
                    rxState = PACKET_LENGTH1;
                    break;

                case PACKET_LENGTH1:
                    inBytes[1] = b;
                    currentLength = convertRxbytesToInt16(0, inBytes);
                    rxState = PACKET_RAW_DATA;
                    rxIndex = 0;
                    break;

                case PACKET_RAW_DATA:
                    if (rxIndex == currentLength) {
                        lrcCheck = (currentAddress & 0xffff) + (currentFunction & 0xffff) + (currentLength & 0xffff);
                        for (int j = 0; j < currentLength; j++) {
                            if (j < MAX_BUFFER) {
                                lrcCheck += (int) rxBuffer[j] & 0xff;
                            } else break;
                        }
                        inBytes[0] = b;
                        rxState = PACKET_LRC_CHECK1;
                    } else {
                        if (rxIndex < MAX_BUFFER) {
                            rxBuffer[rxIndex] = b;
                            rxIndex++;
                        } else break;
                    }
                    break;

                case PACKET_LRC_CHECK1:
                    inBytes[1] = b;

                    lrcReceived = convertRxbytesToInt16(0, inBytes);
                    lrcCheck = lrcCheck & 0xffff;

                    if (lrcReceived == lrcCheck)
                    {
                        parseFunction();
                    }
                    rxState = PACKET_END;
                    break;

                default:
                    rxState = PACKET_END;
                    break;
            }
        }
        //Log.i(TAG, "[LpmsBThread] Received: " + s);
    }

    void sendData(int function, int length) {
        int txLrcCheck;

        txBuffer[0] = 0x3a;
        convertInt16ToTxbytes(imuId, 1, txBuffer);
        convertInt16ToTxbytes(function, 3, txBuffer);
        convertInt16ToTxbytes(length, 5, txBuffer);

        for (int i = 0; i < length; ++i) {
            txBuffer[7 + i] = rawTxData[i];
        }

        txLrcCheck = (imuId & 0xffff) + (function & 0xffff) + (length & 0xffff);

        for (int j = 0; j < length; j++) {
            txLrcCheck += (int) rawTxData[j] & 0xff;
        }

        convertInt16ToTxbytes(txLrcCheck, 7 + length, txBuffer);
        txBuffer[9 + length] = 0x0d;
        txBuffer[10 + length] = 0x0a;

        String s="";

        for (int i = 0; i < 11 + length; i++) {
        	s += Integer.toHexString(txBuffer[i] & 0xFF) + " " ;
        }
        //Log.i(TAG, "[LpmsBThread] Sending: " + s);

        try {
           // Log.i(TAG, "[LpmsBThread] Sending data");
            mOutStream.write(txBuffer, 0, length + 11);
        } catch (Exception e) {
            //Log.i(TAG, "[LpmsBThread] Error while sending data");
            e.printStackTrace();
           // throw new RuntimeException(e);
        }
    }

    void sendAck() {
        sendData(REPLY_ACK, 0);
    }

    void sendNack() {
        sendData(REPLY_NACK, 0);
    }

    float convertRxbytesToFloat(int offset, byte buffer[]) {
    	int l;

        l = buffer[offset + 0];
        l &= 0xff;
        l |= ((long) buffer[offset + 1] << 8);
        l &= 0xffff;
        l |= ((long) buffer[offset + 2] << 16);
        l &= 0xffffff;
        l |= ((long) buffer[offset + 3] << 24);

        return Float.intBitsToFloat(l);
    }

    int convertRxbytesToInt(int offset, byte buffer[]) {
    	int v;
        v = (int) ((buffer[offset] & 0xFF)
                | ((buffer[offset+1] & 0xFF)<<8)
                | ((buffer[offset+2] & 0xFF)<<16)
                | ((buffer[offset+3] & 0xFF)<<24));
        return v;

    }

    int convertRxbytesToInt16(int offset, byte buffer[]) {
        int v;
        v= (int) ((buffer[offset]&0xFF)
                | ((buffer[offset+1]<<8) & 0xFF00));
        return v;
    }

    short convertRxbytesToShort(int offset, byte buffer[]) {
        short v;

        v= (short) ((buffer[offset]&0xFF)
                | (short)((buffer[offset+1] & 0xFF) <<8));

        return v;
    }

    String convertRxbytesToString(int length, byte buffer[]) {
        byte[] t = new byte[length];
        for (int i = 0; i < length; i++) {
            t[i] = buffer[i];
        }

        String decodedString = new String(t).trim();
        return decodedString;
    }

    void convertIntToTxbytes(int v, int offset, byte buffer[]) {
        byte[] t = ByteBuffer.allocate(4).putInt(v).array();

        for (int i = 0; i < 4; i++) {
            buffer[3 - i + offset] = t[i];
        }
    }

    void convertInt16ToTxbytes(int v, int offset, byte buffer[]) {
        byte[] t = ByteBuffer.allocate(2).putShort((short) v).array();

        for (int i = 0; i < 2; i++) {
            buffer[1 - i + offset] = t[i];
        }
    }

    void convertFloatToTxbytes(float f, int offset, byte buffer[]) {
        int v = Float.floatToIntBits(f);
        byte[] t = ByteBuffer.allocate(4).putInt(v).array();

        for (int i = 0; i < 4; i++) {
            buffer[3 - i + offset] = t[i];
        }
    }

  //without changing isStreamMode
    private void _setCommandMode(){
        if (!assertConnected())
            return;
        waitForAck = true;
        lpbusSetNone(GOTO_COMMAND_MODE);
        Log.i(TAG, " Send __GOTO_COMMAND_MODE");
        _waitForAckLoop();
    }

  //without changing isStreamMode
    private void _setStreamingMode()
    {
        if (!assertConnected())
            return;
        waitForAck = true;
        lpbusSetNone(GOTO_STREAM_MODE);
        //Log.i(TAG, "Send __GOTO_STREAM_MODE");
        _waitForAckLoop();
    }

    private void _enableConfigUtil(boolean b ,int contents){
        if (!assertConnected())
            return;
        if (b)
            configurationRegister |= contents;
        else
            configurationRegister &= ~contents;
        _setTransmissionData();
    }

    private void _waitForAckLoop()
    {
        int timeout = 0;
        while (timeout++ < 30  && waitForAck)
        {
            try {
                Thread.sleep(PARAMETER_SET_DELAY);
            }
            catch (Exception e) {
                e.printStackTrace();
               // throw new RuntimeException(e);
            }
        }
    }

    private void _waitForAckLoopWithoutTimeout()
    {
        while (waitForAck)
        {
            try {
                Thread.sleep(PARAMETER_SET_DELAY);
            }
            catch (Exception e) {
                e.printStackTrace();
                // throw new RuntimeException(e);
            }
        }
    }

    private void _waitForDataLoop()
    {
        int timeout = 0;
        while (timeout++ < 30  && waitForData)
        {
            try {
                Thread.sleep(PARAMETER_SET_DELAY);
            }
            catch (Exception e) {
                e.printStackTrace();
               // throw new RuntimeException(e);
            }
        }
    }

    private void _getSensorSettings()
    {
        //
        _getDeviceName();
        _getFirmwareInfo();
        //
        _getConfig();
        _getGyroRange();
        _getAccRange();
        _getMagRange();
        _getFilterMode();
        _getMagCorrection();
        _getLowPassFilter();
        sendBatteryPercentage();
        printConfig();
    }

    private void _getConfig()
    {
        configurationRegisterReady = false;
        lpbusSetNone(GET_CONFIG);
        //Log.i(TAG, "Send GET_CONFIG");
        int timeout = 0;
        while (timeout++ < 30  && !configurationRegisterReady)
        {
            try {
                Thread.sleep(PARAMETER_SET_DELAY);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Parse configuration
        parseConfig(configurationRegister);
    }

    private void parseConfig(int config )
    {
        //Log.i(TAG, "config: " + config);
        // Stream frequency
        if ((configurationRegister & LPMS_STREAM_FREQ_MASK) == LPMS_STREAM_FREQ_5HZ_ENABLED)
            streamingFrequency = LPMS_STREAM_FREQ_5HZ;
        else if ((configurationRegister & LPMS_STREAM_FREQ_MASK) == LPMS_STREAM_FREQ_10HZ_ENABLED)
            streamingFrequency = LPMS_STREAM_FREQ_10HZ;
        else if ((configurationRegister & LPMS_STREAM_FREQ_MASK) == LPMS_STREAM_FREQ_25HZ_ENABLED)
            streamingFrequency = LPMS_STREAM_FREQ_25HZ;
        else if ((configurationRegister & LPMS_STREAM_FREQ_MASK) == LPMS_STREAM_FREQ_50HZ_ENABLED)
            streamingFrequency = LPMS_STREAM_FREQ_50HZ;
        else if ((configurationRegister & LPMS_STREAM_FREQ_MASK) == LPMS_STREAM_FREQ_100HZ_ENABLED)
            streamingFrequency = LPMS_STREAM_FREQ_100HZ;
        else if ((configurationRegister & LPMS_STREAM_FREQ_MASK) == LPMS_STREAM_FREQ_200HZ_ENABLED)
            streamingFrequency = LPMS_STREAM_FREQ_200HZ;
        else if ((configurationRegister & LPMS_STREAM_FREQ_MASK) == LPMS_STREAM_FREQ_400HZ_ENABLED)
            streamingFrequency = LPMS_STREAM_FREQ_400HZ;

        if ( (config & LPMS_GYR_RAW_OUTPUT_ENABLED) != 0) {
            gyrEnable = true;
            //Log.i(TAG, "GYRO ENABLED");
        }else
        {
            gyrEnable = false;
            //Log.i(TAG, "GYRO DISABLED");
        }
        if ( (config  &LPMS_ACC_RAW_OUTPUT_ENABLED) != 0) {
            accEnable = true;
            //Log.i(TAG, "ACC ENABLED");
        }else
        {
            accEnable = false;
            //Log.i(TAG, "ACC DISABLED");
        }
        if ( (config & LPMS_MAG_RAW_OUTPUT_ENABLED) != 0) {
            magEnable = true;
            //Log.i(TAG, "MAG ENABLED");
        }
        else
        {
            magEnable = false;
            //Log.i(TAG, "MAG DISABLED");
        }
        if ( (config & LPMS_ANGULAR_VELOCITY_OUTPUT_ENABLED) != 0) {
            angularVelEnable = true;
            //Log.i(TAG, "AngVel ENABLED");
        }
        else
        {
            angularVelEnable = false;
            //Log.i(TAG, "AngVel DISABLED");
        }
        if ( (config & LPMS_QUAT_OUTPUT_ENABLED) != 0) {
            quaternionEnable = true;
            //Log.i(TAG, "QUAT ENABLED");
        }
        else
        {
            quaternionEnable = false;
            //Log.i(TAG, "QUAT DISABLED");
        }
        if ( (config & LPMS_EULER_OUTPUT_ENABLED) != 0) {
            eulerAngleEnable = true;
            //Log.i(TAG, "EULER ENABLED");
        }
        else
        {
            eulerAngleEnable = false;
            //Log.i(TAG, "EULER DISABLED");
        }
        if ( (config & LPMS_LINACC_OUTPUT_ENABLED) != 0) {
            linAccEnable =true;
            //Log.i(TAG, "LINACC ENABLED");
        }
        else
        {
            linAccEnable = false;
            //Log.i(TAG, "LINACC DISABLED");
        }
        if ( (config & LPMS_PRESSURE_OUTPUT_ENABLED) != 0) {
            pressureEnable = true;
            //Log.i(TAG, "PRESSURE ENABLED");
        }
        else
        {
            pressureEnable = false;
            //Log.i(TAG, "PRESSURE DISABLED");
        }

        if ( (config & LPMS_TEMPERATURE_OUTPUT_ENABLED) != 0) {
            temperatureEnable = true;
            //Log.i(TAG, "PRESSURE ENABLED");
        }
        else
        {
            temperatureEnable = false;
            //Log.i(TAG, "PRESSURE DISABLED");
        }

        if ( (config & LPMS_ALTITUDE_OUTPUT_ENABLED) != 0) {
            altitudeEnable = true;
            //Log.i(TAG, "PRESSURE ENABLED");
        }
        else
        {
            altitudeEnable = false;
            //Log.i(TAG, "PRESSURE DISABLED");
        }
        if ( (config & LPMS_HEAVEMOTION_OUTPUT_ENABLED) != 0) {
            heaveEnable = true;
            //Log.i(TAG, "heave ENABLED");
        }
        else
        {
            heaveEnable = false;
            //Log.i(TAG, "heave DISABLED");
        }
        if ( (config & LPMS_LPBUS_DATA_MODE_16BIT_ENABLED) != 0) {
            sixteenBitDataEnable = true;
        }
        else
        {
            sixteenBitDataEnable = false;
        }
        //Log.i(TAG, "Stream freq: " + streamingFrequency);
    }

    private void _getGyroRange()
    {
        waitForData = true;
        lpbusSetNone(GET_GYR_RANGE);
        //Log.i(TAG, "Send GET_GYR_RANGE");
        _waitForDataLoop();
        //Log.i(TAG, "Gyro range: " + gyrRange + "dps");
    }

    private void _getAccRange()
    {
        waitForData = true;
        lpbusSetNone(GET_ACC_RANGE);
        //Log.i(TAG, "Send GET_ACC_RANGE");
        _waitForDataLoop();
        //Log.i(TAG, "Acc range: " + accRange + "g");
    }

    private void _getMagRange()
    {
        waitForData = true;
        lpbusSetNone(GET_MAG_RANGE);
        //Log.i(TAG, "Send GET_MAG_RANGE");
        _waitForDataLoop();
        //Log.i(TAG, "Mag range: " + magRange + "mT");
    }

    private void _getFilterMode()
    {
        waitForData = true;
        lpbusSetNone(GET_FILTER_MODE);
        //Log.i(TAG, "Send GET_FILTER_MODE");
        _waitForDataLoop();
        //Log.i(TAG, "Filter mode: " + filterMode);
    }

    private void _getMagCorrection(){
        waitForData = true;
        lpbusSetNone(GET_FILTER_PRESET);
        //Log.i(TAG, "Send GET_FILTER_PRESET");
        _waitForDataLoop();
        //Log.i(TAG, "MagCorrection: " + magCorrection);
    }

    private void _getLowPassFilter(){
        waitForData = true;
        lpbusSetNone(GET_RAW_DATA_LP);
        //Log.i(TAG, "Send GET_RAW_DATA_LP");
        _waitForDataLoop();
        //Log.i(TAG, "Low-Pass Filter Mode: " + lowPassFilter);
    }

    private void _getSerialNumber()
    {
        serialNumberReady = false;
        lpbusSetNone(GET_SERIAL_NUMBER);
        int timeout = 0;
        while (timeout++ < 30 && !serialNumberReady)
        {
            try {
                Thread.sleep(PARAMETER_SET_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void _getDeviceName()
    {
        deviceNameReady = false;
        lpbusSetNone(GET_DEVICE_NAME);
        Log.i(TAG, "Send GET_DEVICE_NAME");
        int timeout = 0;
        while (timeout++ < 30 && !deviceNameReady)
        {
            try {
                Thread.sleep(PARAMETER_SET_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void _getFirmwareInfo()
    {
        firmwareInfoReady = false;
        lpbusSetNone(GET_FIRMWARE_INFO);
        Log.i(TAG, "Send GET_FIRMWARE_INFO");
        int timeout = 0;
        while (timeout++ < 30 && !firmwareInfoReady)
        {
            try {
                Thread.sleep(PARAMETER_SET_DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
                //throw new RuntimeException(e);
            }
        }
    }

    private void _saveParameters()
    {
        waitForAck = true;
        lpbusSetNone(WRITE_REGISTERS);
        _waitForAckLoop();
    }

    private void _setTransmissionData()
    {
        fixedThreadPool.execute(new CommandRunnable(SET_TRANSMIT_DATA,configurationRegister));
        //Log.i(TAG,"set TransmissionData config :"+configurationRegister);
    }

    private void _setDataMode(int identifier){
        fixedThreadPool.execute(new CommandRunnable(SET_LPBUS_DATA_MODE,identifier));
       // Log.i(TAG," set Data Mode");
    }

    private boolean assertConnected()
    {
        if ( connectionStatus != SENSOR_STATUS_DISCONNECTED )
            return true;
        return false;
    }


    private void printConfig()
    {
        //Log.i(TAG, "config: " + config);
        Log.i(TAG, "SN: " + serialNumber);
        Log.i(TAG, "FW: " + firmwareInfo);
        Log.i(TAG, "DN: " + deviceName);
        Log.i(TAG, "ImuId: " + imuId);
        Log.i(TAG, "StreamFreq: " + streamingFrequency );
        Log.i(TAG, "Gyro: " + gyrRange);
        Log.i(TAG, "Acc: " + accRange);
        Log.i(TAG, "Mag: " + magRange);

        if (gyrEnable) {
            Log.i(TAG, "GYRO ENABLED");
        }else {
            Log.i(TAG, "GYRO DISABLED");
        }
        if ( accEnable) {
            Log.i(TAG, "ACC ENABLED");
        }else {
            Log.i(TAG, "ACC DISABLED");
        }
        if ( magEnable ) {
            Log.i(TAG, "MAG ENABLED");
        } else {
            Log.i(TAG, "MAG DISABLED");
        }
        if (angularVelEnable) {
            Log.i(TAG, "AngVel ENABLED");
        } else {
            Log.i(TAG, "AngVel DISABLED");
        }
        if (quaternionEnable) {
            Log.i(TAG, "QUAT ENABLED");
        } else {
            Log.i(TAG, "QUAT DISABLED");
        }
        if ( eulerAngleEnable) {
            Log.i(TAG, "EULER ENABLED");
        } else {
            Log.i(TAG, "EULER DISABLED");
        }
        if (linAccEnable) {
            Log.i(TAG, "LINACC ENABLED");
        } else {
            Log.i(TAG, "LINACC DISABLED");
        }
        if ( pressureEnable) {
            Log.i(TAG, "PRESSURE ENABLED");
        } else {
            Log.i(TAG, "PRESSURE DISABLED");
        }
        if ( altitudeEnable) {
            Log.i(TAG, "ALTITUDE ENABLED");
        } else {
            Log.i(TAG, "ALTITUDE DISABLED");
        }
        if ( temperatureEnable) {
            Log.i(TAG, "TEMPERATURE ENABLED");
        } else {
            Log.i(TAG, "TEMPERATURE DISABLED");
        }

        if ( heaveEnable) {
            Log.i(TAG, "heave ENABLED");
        } else {
            Log.i(TAG, "heave DISABLED");
        }

        if (sixteenBitDataEnable) {
           Log.i(TAG, "16 bit ENABLED");
        } else {
            Log.i(TAG, "16 bit DISABLED");
        }

    }

    public class ClientReadThread implements Runnable {
		public void run() {
            connectionStatus = SENSOR_STATUS_CONNECTED;
            while (mSocket.isConnected()) {
                try {
                    nBytes = mInStream.read(rawRxBuffer);
                } catch (Exception e) {
                	break;
                }
                parse();
            }
            connectionStatus = SENSOR_STATUS_DISCONNECTED;
        }
    }
    // Command Thread
    public class CommandRunnable implements Runnable{
        private int command;
        private int v;

        public CommandRunnable(int command,int v){
            this.command = command;
            this.v = v;
        }
        @Override
        public void run() {
            _setCommandMode();
            waitForAck = true;
            lpbusSetInt32(command, v);
            _waitForAckLoop();
            _getSensorSettings();
            _saveParameters();
            if (isStreamMode)
                _setStreamingMode();
        }
    }


    /***************************************************************
     * BLE
     **************************************************************
     */
    public boolean connectBLE(final Context context, String currentAddress2){
        if (connectionStatus != SENSOR_STATUS_DISCONNECTED) {
           // Log.i(TAG, "[LpmsBbleThread] Another connection establishing: " + mAddress);
            return false;
        }
        if (mAdapter == null) {
            //Log.i(TAG, "[LpmsBbleThread] Didn't find Bluetooth adapter");
            connectionStatus = SENSOR_STATUS_DISCONNECTED;
            return false;
        }
        // Previously connected device.  Try to reconnect.
        if (mAddress != null && currentAddress2.equals(mAddress)
                && mBluetoothGatt != null) {
            //Log.i(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                connectionStatus = SENSOR_STATUS_CONNECTED;
                return true;
            } else {
                return false;
            }
        }
        mAddress = currentAddress2;
        //connectionStatus = SENSOR_STATUS_CONNECTING;
        //Log.i(TAG, "[LpmsBbleThread] Connecting to: " + mAddress);

        mAdapter.cancelDiscovery();

        //Log.i(TAG, "[LpmsBbleThread] Getting device");

        try {
            mDevice = mAdapter.getRemoteDevice(mAddress);

            if (mDevice == null) {
               // Log.w(TAG, "[LpmsBbleThread] Device not found.  Unable to connect.");
                return false;
            }
//	            // Create handler for main thread where mContext is application context
//	            Handler mHandler = new Handler(Looper.getMainLooper());
//	            // Connect to BLE device from mHandler
//	            mHandler.postDelayed(new Runnable() {
//	                @Override
//	                public void run() {
//	                	mBluetoothGatt = mDevice.connectGatt(context, false, mGattCallback);
//	                	Log.i(TAG, "[LpmsBbleThread] Trying to create a  mBluetoothGatt  "+mBluetoothGatt);
//	                }
//	            },10);

//	            new Thread(new Runnable() {
//					@Override
//					public void run() {
            connectionStatus = SENSOR_STATUS_CONNECTING;
            mBluetoothGatt = mDevice.connectGatt(context, false, mGattCallback);
            //Log.i(TAG, "[LpmsBbleThread] Trying to create a  mBluetoothGatt  "+mBluetoothGatt);
//					}
//				}).start();

            if(mBluetoothGatt==null){
                Log.i(TAG,"[LpmsBbleThread] can not get BluetoothGatt");
                return false;
            }

        } catch (IllegalArgumentException e) {
            //Log.i(TAG, "[LpmsBbleThread] Invalid Bluetooth address", e);
            connectionStatus = SENSOR_STATUS_DISCONNECTED;
            return false;
        }
        return true;
    }

    private void setBLEParameters() {
        //Log.i(TAG, "[LpmsBbleThread] setBLEParameters");
        readCharacteristic();
        _setCommandMode();
        while (waitForAck && readCharacteristicReady) {
            _setCommandMode();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        _getSensorSettings();
        _getSerialNumber();
        if(firmwareInfoReady){
            diffFirmwareCheck = firmwareInfo.contains("MWT");
        }
        setStreamingMode();
        connectionStatus = SENSOR_STATUS_CONNECTED;
    }

    BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            parseBLE(data);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //Log.i(TAG, "[LpmsBbleThread] onCharacteristicRead"+characteristic.toString());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            String s="";
//            for (int i = 0; i <20; i++) {
//                s += Integer.toHexString(txBLEBuffer[i] & 0xFF) + " " ;
//            }
           // Log.i(TAG, "[LpmsBbleThread] onCharacteristicWrite Revice :"+s);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.i(TAG, "[LpmsBbleThread] Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i(TAG, "[LpmsBbleThread] STATE_CONNECTED");
                    mBluetoothGatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "[LpmsBbleThread] STATE_DISCONNECTED");
                    gatt.disconnect();
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                    connectionStatus = SENSOR_STATUS_DISCONNECTED;
                    break;
                default:
                    //Log.i(TAG, "[LpmsBbleThread] STATE_OTHER : "+newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            //Log.i(TAG, "[LpmsBbleThread] servicesDiscovered status :"+status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                connectionStatus = SENSOR_STATUS_CONNECTING;
                fixedThreadPool.execute(new Runnable() {
                    public void run() {
                          setBLEParameters();
                    }
                });
            }
        }
    };

    //readCharacteristic when connect at first
    private void readCharacteristic() {
        BluetoothGattService service = mBluetoothGatt.getService(MY_UUID_SERVICE);
        if(service != null){
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(MY_UUID_CHARACTERISTIC);
            if(characteristic!=null){
                mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                mBluetoothGatt.readCharacteristic(characteristic);
                readCharacteristicReady = true;
                //Log.i(TAG, "[LpmsBbleThread] setNotifacation and readCharacteristic");
            }else{
                Log.e(TAG, "[LpmsBbleThread] not found the BluetoothGattCharacteristic");
            }
        }else{
            Log.e(TAG, "[LpmsBbleThread] not found the BluetoothGattService");
        }
    }

    public void  sendSetting(byte[] b){
        // each command is sent should wait for a while in BLE (100ms)
        synchronized(mBluetoothGatt) {
            BluetoothGattCharacteristic m_sendCharacteristic = getServiceAndCharacteristic();
            if (m_sendCharacteristic == null)return;
            m_sendCharacteristic.setValue(b);
            mBluetoothGatt.writeCharacteristic(m_sendCharacteristic);
            //Log.i(TAG, "[LpmsBBLEThread] writeCharacteristic");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public BluetoothGattCharacteristic getServiceAndCharacteristic (){
        if(sendService == null)sendService = mBluetoothGatt.getService(MY_UUID_SERVICE);
        if(sendCharacteristic == null){
            sendCharacteristic = sendService.getCharacteristic(MY_UUID_CHARACTERISTIC);
            sendCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        }
        return sendCharacteristic;
    }

    void parseBLE(byte[] data) {
        int lrcReceived = 0;
        //String s="";
        for (int i = 0; i <data.length; i++) {
            b = data[i];
            //s += Integer.toHexString(b& 0xFF) + " " ;
            //Log.i("rxState", "parseBLE  rxState = "+ rxState );
            switch (rxState) {
                case PACKET_END:
                    if (b == 0x3a) {
                        rxState = PACKET_ADDRESS0;
                    }
                    break;

                case PACKET_ADDRESS0:
                    inBytes[0] = b;
                    rxState = PACKET_ADDRESS1;
                    break;

                case PACKET_ADDRESS1:
                    inBytes[1] = b;
                    currentAddress = convertRxbytesToInt16(0, inBytes);
                    imuId = currentAddress;
                    rxState = PACKET_FUNCTION0;
                    break;

                case PACKET_FUNCTION0:
                    inBytes[0] = b;
                    rxState = PACKET_FUNCTION1;
                    break;

                case PACKET_FUNCTION1:
                    inBytes[1] = b;
                    currentFunction = convertRxbytesToInt16(0, inBytes);
                    rxState = PACKET_LENGTH0;
                    break;

                case PACKET_LENGTH0:
                    inBytes[0] = b;
                    rxState = PACKET_LENGTH1;
                    break;

                case PACKET_LENGTH1:
                    inBytes[1] = b;
                    currentLength = convertRxbytesToInt16(0, inBytes);
                    rxState = PACKET_RAW_DATA;
                    rxIndex = 0;
                    break;

                case PACKET_RAW_DATA:
                    if (rxIndex == currentLength) {
                        lrcCheck = (currentAddress & 0xffff) + (currentFunction & 0xffff) + (currentLength & 0xffff);
                        for (int j = 0; j < currentLength; j++) {
                            if (j < MAX_BUFFER) {
                                lrcCheck += (int) rxBuffer[j] & 0xff;
                            } else break;
                        }
                        inBytes[0] = b;
                        rxState = PACKET_LRC_CHECK1;
                    } else {
                        if (rxIndex < MAX_BUFFER) {
                            rxBuffer[rxIndex] = b;
                            rxIndex++;
                        } else {
                            //TODO break
                            rxState = PACKET_LRC_CHECK1;
                        }
                    }
                    break;

                case PACKET_LRC_CHECK1:
                    inBytes[1] = b;

                    lrcReceived = convertRxbytesToInt16(0, inBytes);
                    lrcCheck = lrcCheck & 0xffff;

                    if (lrcReceived == lrcCheck)
                    {
                        parseFunction();
                    }
                    rxState = PACKET_END;
                    break;

                default:
                    rxState = PACKET_END;
                    break;
            }
        }
        //Log.i(TAG, "[LpmsBBLEThread] Received: " + s);
    }

    void sendBLEData(int function, int length) {
        int txLrcCheck;
        txBLEBuffer[0] = 0x3a;
        convertInt16ToTxbytes(imuId, 1, txBLEBuffer);
        convertInt16ToTxbytes(function, 3, txBLEBuffer);
        convertInt16ToTxbytes(length, 5, txBLEBuffer);

        for (int i = 0; i < length; ++i) {
            txBLEBuffer[7 + i] = rawTxData[i];
        }

        txLrcCheck = (imuId & 0xffff) + (function & 0xffff) + (length & 0xffff);
        for (int j = 0; j < length; j++) {
            txLrcCheck += (int) rawTxData[j] & 0xff;
        }

        convertInt16ToTxbytes(txLrcCheck, 7 + length, txBLEBuffer);
        txBLEBuffer[9 + length] = 0x0d;
        txBLEBuffer[10 + length] = 0x0a;

//        String s="";
//        for (int i = 0; i < 11 + length; i++) {
//        	s += Integer.toHexString(txBLEBuffer[i] & 0xFF) + " " ;
//        }
        //Log.i(TAG, "[LpmsBBLEThread] Sending: " + s);

        try {
            //Log.i(TAG, "[LpmsBBLEThread] Sending data");
            sendSetting(txBLEBuffer);
        } catch (Exception e) {
            connectionStatus = SENSOR_STATUS_DISCONNECTED;
            Log.e(TAG, "[LpmsBBLEThread] Error while sending data"+e);
            //throw new RuntimeException(e);
        }
    }
}