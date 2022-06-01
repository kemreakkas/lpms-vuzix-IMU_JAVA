package cn.alubi.myapplication.Activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler; 
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import cn.alubi.myapplication.Adapter.MyListViewAdapter;
import cn.alubi.myapplication.R;
import cn.alubi.lpresearch_library.lpsensorlib.LpmsBDevice;


public class ScanActivity extends Activity implements android.widget.CompoundButton.OnCheckedChangeListener{

	private Button btn_ClassicalScan,btn_connect,btn_Bleconnect,btn_BleScan;
	private ListView listView;
	private MyListViewAdapter listAdapter;
	private ArrayList<LpmsBDevice> mLpmsBDeviceList;
	BluetoothAdapter btAdapter;
	private final static int REQUEST_ENABLE_BT = 1;
	private final static int REQUEST_COARSE_LOCATION_PERMISSIONS = 2;
	private final static int MY_PERMISSION_WRITE_EXTERNAL_STORAGE = 3;
	private final static int MY_PERMISSION_READ_EXTERNAL_STORAGE = 4;

	private static final String APP_SharedPreferences = "LPMS-B2 APP";
	private static final String KEY_START_ACTIVITY = "isFirstIn";
	private static Toast toast;
//	Boolean isFirstIn = null;
	private boolean mScanning = false;
	private BluetoothLeScanner mLEScanner;
	private Handler mHandler;
	private final String TAG = "ScanActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scanactivity);
		mHandler = new Handler();

		btn_ClassicalScan = (Button) findViewById(R.id.btn_ClassicScan);
		btn_ClassicalScan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				prepareClassicDiscovery();
			}
		});

		btn_BleScan = (Button)findViewById(R.id.btn_BLEScan);
		btn_BleScan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				prepareBLEDiscovery();
			}
		});

		btn_connect = (Button) findViewById(R.id.btn_connect);
		btn_connect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				btAdapter.cancelDiscovery();
				passIntentToActivity(false);
			}
		});
		//button Bleconnect
		btn_Bleconnect = (Button)findViewById(R.id.btn_BleConnect);
		btn_Bleconnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				btAdapter.cancelDiscovery();
				passIntentToActivity(true);
			}

		});

//		SharedPreferences sharedPrefer = this.getSharedPreferences(APP_SharedPreferences,MODE_PRIVATE);
//		isFirstIn = sharedPrefer.getBoolean("isFirstIn", true);
//		if (isFirstIn){
//
//			SharedPreferences.Editor editor = sharedPrefer.edit();
//			editor.putBoolean("isFirstIn",true);
//			editor.commit();
//		}
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "BLE Not Supported",
					Toast.LENGTH_SHORT).show();
			finish();
		}
		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		//获取蓝牙对象
		btAdapter = bluetoothManager.getAdapter();

		mLpmsBDeviceList = new ArrayList<LpmsBDevice>();
		listAdapter = new MyListViewAdapter(mLpmsBDeviceList,this);
		listView = (ListView) findViewById(R.id.listView_B2);
		listView.setAdapter(listAdapter);
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			mLpmsBDeviceList.clear();
			listAdapter.notifyDataSetChanged();
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					showToast("Discover Finished");
					//btAdapter.stopLeScan(mLeScanCallback);
					mLEScanner.stopScan(mScanCallback);
				}
			}, 8000);
			mScanning = true;
			showToast("Discovering.....");
			//btAdapter.startLeScan(mLeScanCallback);
			mLEScanner.startScan(mScanCallback);
		}else {
			mScanning = false;
			mLEScanner.stopScan(mScanCallback);
			//btAdapter.stopLeScan(mLeScanCallback);
		}
	}
//	// Device scan callback.
//	private BluetoothAdapter.LeScanCallback mLeScanCallback =
//			new BluetoothAdapter.LeScanCallback() {
//				@Override
//				public void onLeScan(final BluetoothDevice device, int rssi,
//									 byte[] scanRecord) {
//					runOnUiThread(new Runnable() {
//						@Override
//						public void run() {
//							listAdapter.add(new LpmsBDevice(device.getName(), device.getAddress()));
//							//mLeDeviceListAdapter.addDevice(device);
//							//mLeDeviceListAdapter.notifyDataSetChanged();
//						}
//					});
//				}
//			};

	private ScanCallback mScanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			//Log.i("callbackType", String.valueOf(callbackType));
			//Log.i("result", result.toString());
			BluetoothDevice btDevice = result.getDevice();
			//避免重复搜出蓝牙设备。
			for (ListIterator<LpmsBDevice> it = mLpmsBDeviceList.listIterator(); it.hasNext();) {
				if (btDevice.getAddress().equals(it.next().getDeviceAddress())) {
					return;
				}
			}
			listAdapter.add(new LpmsBDevice(btDevice.getName(), btDevice.getAddress()));
		}

		@Override
		public void onBatchScanResults(List<ScanResult> results) {
			Log.i("ScanResult - Results :", "none");
			for (ScanResult sr : results) {
				Log.i("ScanResult - Results", sr.toString());
			}
		}

		@Override
		public void onScanFailed(int errorCode) {
			Log.e("Scan Failed", "Error Code: " + errorCode);
		}
	};

	//传入多个被选中的LPMS-B给MainActivity
	private void passIntentToActivity(boolean isBLE) {
		Intent intent = new Intent(getApplicationContext(),MainActivity.class);
		//清空Intent。
		String[] stringNames = new String[10];
		String[] stringAddresss = new String[10];
		boolean flag = false;
		int k=0;

		for(int i=0;i<mLpmsBDeviceList.size();i++){
			//选出被选中的，存放到数组中去。
			if(mLpmsBDeviceList.get(i).isSelect()){
				stringNames[k]=mLpmsBDeviceList.get(i).getDeviceName();
				stringAddresss[k]=mLpmsBDeviceList.get(i).getDeviceAddress();
				k++;
				flag = true;
			}
		}
		intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME,stringNames);
		intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS,stringAddresss);
		//Classical set False; BLE set True;
		intent.putExtra(MainActivity.EXTRAS_CONNECT_MODEL,isBLE);
		if(flag){
			startActivity(intent);
		}else {
			showToast("Not sensor selected !");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!btAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		prepareDiscoveredDevicesList();
		mLEScanner = btAdapter.getBluetoothLeScanner();
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
					MY_PERMISSION_WRITE_EXTERNAL_STORAGE);
		} else {
			Log.d("Permission Request"," Write External Storage get it");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
//		if (btAdapter != null && btAdapter.isEnabled()) {
//			scanLeDevice(false);
//		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	
		if (buttonView==null)return;
		int pos = listView.getPositionForView(buttonView);
		if(pos!=ListView.INVALID_POSITION){
			LpmsBDevice device = mLpmsBDeviceList.get(pos);
			device.setSelect(isChecked);
//			if(isChecked){
//				//isSelected and create Bond
//				BluetoothDevice btDevice = btAdapter.getRemoteDevice(device.getDeviceAddress());
//				if(btDevice.getBondState() != BluetoothDevice.BOND_BONDED){
//					btDevice.createBond();
//					//btDevice.
//					Log.d(TAG,"bt creatBond");
//				}
//			}
			/*Toast.makeText(
					this, "chicked on "+ device.getDeviceName()+" state "+ isChecked, Toast.LENGTH_SHORT).show();
					*/
			}
	}

	public void prepareDiscoveredDevicesList() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);//搜索发现设备
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//蓝牙设备状态改变
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//开始搜索
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//结束搜索
		registerReceiver(mReceiver, filter);
	}

	public void prepareClassicDiscovery(){
		int hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
		if (hasPermission == PackageManager.PERMISSION_GRANTED) {
			startBtDiscovery();
			return;
		}
		ActivityCompat.requestPermissions(this, new String[]{
						android.Manifest.permission.ACCESS_COARSE_LOCATION},
				REQUEST_COARSE_LOCATION_PERMISSIONS);
		startBtDiscovery();
	}

	public void prepareBLEDiscovery(){
		int hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
		if (hasPermission == PackageManager.PERMISSION_GRANTED) {
			if (!mScanning){
				scanLeDevice(true);
			}else {
				scanLeDevice(false);
			}
			return;
		}
		ActivityCompat.requestPermissions(this, new String[]{
						android.Manifest.permission.ACCESS_COARSE_LOCATION},
				REQUEST_COARSE_LOCATION_PERMISSIONS);
		if (!mScanning){
			scanLeDevice(true);
		}else {
			scanLeDevice(false);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case REQUEST_COARSE_LOCATION_PERMISSIONS: {
				if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					showToast("Get permission for Bluetooth");
				} else {
					showToast("Can't get permission for Bluetooth");
					//cancelOperation();
				}
				return;
				}
			case MY_PERMISSION_WRITE_EXTERNAL_STORAGE:
			{
				if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					showToast("Get permission for DataLog");
				} else {
					showToast("Can't get permission for DataLog");
					//cancelOperation();
				}
				return;
			}

		}
	}

	public void startBtDiscovery() {
		//Toast.makeText(this, "Starting discovery..", Toast.LENGTH_SHORT).show();
		//synchronized (mLpmsBDeviceList) {
			mLpmsBDeviceList.clear();
		//}
		listAdapter.notifyDataSetChanged();
		if (btAdapter.isDiscovering()	) {
			btAdapter.cancelDiscovery();
		}
		btAdapter.startDiscovery();
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d("Action :","action:"+action);
			// BT Discovery
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				showToast("Discovering.....");
			}
			if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				showToast("Discover Finished");
			}
			BluetoothDevice device;
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				Log.i(TAG, "Found device with Bluetooth scan");
				device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device == null) return;
				//synchronized (mLpmsBDeviceList) {
				Log.i(TAG, "Name of Bluetooth Device is " + device.getName());

				if ((device.getName() != null) && (device.getName().length() > 0) && (device.getAddress() != null)) {
					//避免重复搜出蓝牙设备。
					for (ListIterator<LpmsBDevice> it = mLpmsBDeviceList.listIterator(); it.hasNext(); ) {
						if (device.getAddress().equals(it.next().getDeviceAddress())) {
							return;
						}
					}
					listAdapter.add(new LpmsBDevice(device.getName(), device.getAddress()));
//					if (device.getName().contains("LPMS")) {
//					}
				}
			}
			if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				switch (device.getBondState()) {
					case BluetoothDevice.BOND_BONDING://正在配对
						Log.d("BluetoothDevice", "create bonding......");
						break;
					case BluetoothDevice.BOND_BONDED://配对结束
						Log.d("BluetoothDevice", "create bonded finished");
						break;
					case BluetoothDevice.BOND_NONE://取消配对/未配对
						Log.d("BluetoothDevice", "cancel bond");
					default:
						break;
				}
			}
		}
	};


	private void showToast(String message) {
		if (toast == null) {
			toast = Toast.makeText(getApplicationContext(),
					message,
					Toast.LENGTH_SHORT);
		} else {
			toast.setText(message);
		}
		toast.show();
	}

}
