package cn.alubi.myapplication.Fragment;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import cn.alubi.myapplication.Activity.MainActivity;
import cn.alubi.myapplication.R;
import cn.alubi.myapplication.Service.LpService;
import cn.alubi.lpresearch_library.lpsensorlib.LpmsB2;

public class ConnectFragment1 extends Fragment implements AdapterView.OnItemSelectedListener{

	private static int UI_update_rate = 100;//ms
	private static int viewUpdateTime = 6;
	private LpService lpService;
	boolean mBound = false;
	Spinner rateSpinner, gyrSpinner, accSpinner, magSpinner;
	Spinner filterSpinner, magCorrectionSpinner, lowPassSpinner, dataModeSpinner;
	CheckBox cbAccBox, cbMagBox, cbGyrBox, cbEulerBox, cbQuaterBox, cbLinAccBox, cbPressureBox, cbAngularVel, cbAltitude, cbTemperature;
	TextView tv_SensorStatus, tv_deviceId, tv_firmware,tv_battery;
	Button btnSendCommand, btnStreamingMode, btnCommandMode;
	private Handler mHandler;
	private int sensorConnectionStatus = LpmsB2.SENSOR_STATUS_DISCONNECTED;
	int timeout = 0;
	int configurationRegister = 0;
	private SwipeRefreshLayout swipeRefreshLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_connect, container, false);

		allCheckBox(rootView);
		allSpinner(rootView);

		swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
		// 设置刷新动画的颜色，可以设置1或者更多.
		// 暂时使用三个Android系统自带的颜色。
		swipeRefreshLayout.setColorSchemeResources(
				android.R.color.holo_blue_bright,
				android.R.color.holo_orange_light,
				android.R.color.holo_green_light);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			// SwipeRefreshLayout接管其包裹的ListView下拉事件。
			// 每一次对ListView的下拉动作，将触发SwipeRefreshLayout的onRefresh()。
			@Override
			public void onRefresh() {
				if(!mBound)return;
				lpService.getSensorSetting();
				viewUpdate();
//				if(getActivity().getIntent().getBooleanExtra(MainActivity.EXTRAS_CONNECT_MODEL,false)){
//				}
			}
		});

		//TextView
		tv_SensorStatus = (TextView) rootView.findViewById(R.id.tv_connection);
		tv_deviceId = (TextView) rootView.findViewById(R.id.tv_device_id);
		tv_firmware = (TextView) rootView.findViewById(R.id.tv_firmware);
		tv_battery = (TextView) rootView.findViewById(R.id.tv_battery);
		//Button
		btnSendCommand = (Button) rootView.findViewById(R.id.btn_sendCommand);
		//btnSendCommand
		btnSendCommand.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!mBound)return;
				lpService.setTransmissionData(configurationRegister);
				Toast.makeText(getActivity(),"Sending Command",Toast.LENGTH_SHORT).show();
			}
		});
		btnStreamingMode = (Button) rootView.findViewById(R.id.btn_streamingmode);
		btnStreamingMode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				lpService.setStreamingMode();
			}
		});
		btnCommandMode = (Button) rootView.findViewById(R.id.btn_commandmode);
		btnCommandMode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				lpService.setCommandMode();
			}
		});
		mHandler = new Handler();
		return rootView;
	}

	Runnable UI_update = new Runnable() {
		@Override
		public void run() {
			if (mBound) parseConfigToUI();
			mHandler.postDelayed(this, UI_update_rate);
		}
	};

	//绑定之后调用此方法。
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LpService.LocalBinder binder = (LpService.LocalBinder) service;
			lpService = binder.getService();
			mBound = true;
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		Intent intent = new Intent(getActivity(), LpService.class);
		getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		mHandler.postDelayed(UI_update, 100);
		viewUpdate();
	}

	public void parseConfigToUI() {
		sensorConnectionStatus = lpService.getSensorConnectionStatus();
		if (sensorConnectionStatus == LpmsB2.SENSOR_STATUS_DISCONNECTED) {
			tv_SensorStatus.setText("Disconnected");
			tv_SensorStatus.setTextColor(getResources().getColor(R.color.colorRed));
		}
		if (sensorConnectionStatus == LpmsB2.SENSOR_STATUS_CONNECTING) {
			tv_SensorStatus.setText("Connecting");
			tv_SensorStatus.setTextColor(getResources().getColor(R.color.colorOrange));
		}
		if (sensorConnectionStatus == LpmsB2.SENSOR_STATUS_CONNECTED) {
			tv_SensorStatus.setText("Connected");
			tv_SensorStatus.setTextColor(getResources().getColor(R.color.colorDarkGreen));
			if (timeout< viewUpdateTime) {
				viewUpdate();
				timeout++;
			}
		}
	}

	public void viewUpdate() {
		if(!mBound)return;
		swipeRefreshLayout.setRefreshing(true);
		lpService.getSendBattery();
		//TextView
		tv_deviceId.setText(lpService.getDeviceName());
		tv_firmware.setText(lpService.getFirmwareInfo());
		//CheckBox
		cbAccBox.setChecked(lpService.isAccDataEnabled());
		cbMagBox.setChecked(lpService.isMagDataEnabled());
		cbGyrBox.setChecked(lpService.isGyroDataEnabled());
		cbEulerBox.setChecked(lpService.isEulerDataEnabled());
		cbQuaterBox.setChecked(lpService.isQuaternionDataEnabled());
		cbLinAccBox.setChecked(lpService.isLinAccDataEnabled());
		cbPressureBox.setChecked(lpService.isPressureDataEnabled());
		cbAngularVel.setChecked(lpService.isAngularVelDataEnabled());
		cbTemperature.setChecked(lpService.isTemperatureDataEnabled());
		cbAltitude.setChecked(lpService.isAltitudeDataEnabled());
		rateSpinner.setSelection(getPositionUtil(lpService.getStreamFrequency(), new int[]{5, 10, 25, 50, 100, 200, 400}));
		gyrSpinner.setSelection(getPositionUtil(lpService.getGyroRange(), new int[]{125, 245, 500, 1000, 2000}));
		accSpinner.setSelection(getPositionUtil(lpService.getAccRange(), new int[]{2, 4, 8, 16}));
		magSpinner.setSelection(getPositionUtil(lpService.getMagRange(), new int[]{4, 8, 12, 16}));
		filterSpinner.setSelection(lpService.getFilterMode());
		magCorrectionSpinner.setSelection(lpService.getMagCorrection());
		lowPassSpinner.setSelection(lpService.getLowPassFilter());
		dataModeSpinner.setSelection(lpService.is16BitDataEnabled()==false?0:1);
		tv_battery.setText(String.format("%.1f%%  %.1fV",lpService.getBatteryLevel(),lpService.getBatteryVoltage()));
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				swipeRefreshLayout.setRefreshing(false);
			}
		},1000);
	}

	private int getPositionUtil(int current, final int[] b) {
		int currentChoice;
		for (currentChoice = 0; currentChoice < b.length; ++currentChoice) {
			if (current == b[currentChoice]) {
				break;
			}
		}
		if (currentChoice == b.length) return 0;
		return currentChoice;
	}

	private void allCheckBox(View rootView) {
		cbAccBox = (CheckBox) rootView.findViewById(R.id.cb_raw_acc);
		cbAccBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				changeConfigurationRegisterUtil(isChecked,LpmsB2.LPMS_ACC_RAW_OUTPUT_ENABLED);
			}
		});
		cbMagBox = (CheckBox) rootView.findViewById(R.id.cb_raw_mag);
		cbMagBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				changeConfigurationRegisterUtil(isChecked,LpmsB2.LPMS_MAG_RAW_OUTPUT_ENABLED);
			}
		});
		cbGyrBox = (CheckBox) rootView.findViewById(R.id.cb_raw_gyr);
		cbGyrBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				changeConfigurationRegisterUtil(isChecked,LpmsB2.LPMS_GYR_RAW_OUTPUT_ENABLED);
			}
		});
		cbEulerBox = (CheckBox) rootView.findViewById(R.id.cb_euler_angle);
		cbEulerBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				changeConfigurationRegisterUtil(isChecked,LpmsB2.LPMS_EULER_OUTPUT_ENABLED);
			}
		});
		cbQuaterBox = (CheckBox) rootView.findViewById(R.id.cb_quaternion);
		cbQuaterBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				changeConfigurationRegisterUtil(isChecked,LpmsB2.LPMS_QUAT_OUTPUT_ENABLED);
			}
		});
		cbLinAccBox = (CheckBox) rootView.findViewById(R.id.cb_lin_acc);
		cbLinAccBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				changeConfigurationRegisterUtil(isChecked,LpmsB2.LPMS_LINACC_OUTPUT_ENABLED);
			}
		});
		cbPressureBox = (CheckBox) rootView.findViewById(R.id.cb_bar_pressure);
		cbPressureBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				changeConfigurationRegisterUtil(isChecked,LpmsB2.LPMS_PRESSURE_OUTPUT_ENABLED);
			}
		});
		cbAngularVel = (CheckBox) rootView.findViewById(R.id.cb_angularVel);
		cbAngularVel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				changeConfigurationRegisterUtil(isChecked,LpmsB2.LPMS_ANGULAR_VELOCITY_OUTPUT_ENABLED);
			}
		});
		cbAltitude = (CheckBox) rootView.findViewById(R.id.cb_altitude);
		cbAltitude.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				changeConfigurationRegisterUtil(isChecked,LpmsB2.LPMS_ALTITUDE_OUTPUT_ENABLED);
			}
		});
		cbTemperature = (CheckBox) rootView.findViewById(R.id.cb_temperature);
		cbTemperature.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				changeConfigurationRegisterUtil(isChecked,LpmsB2.LPMS_TEMPERATURE_OUTPUT_ENABLED);
			}
		});
	}

	private void changeConfigurationRegisterUtil(boolean b, int configuration){
		if (b)
			configurationRegister |= configuration;
		else
			configurationRegister &= ~configuration;
	}

	private void allSpinner(View rootView) {
		//change spinnerArrayList at values/spinnerArrays.xml
		rateSpinner = (Spinner) rootView.findViewById(R.id.spinner_rate);
		rateSpinner.setOnItemSelectedListener(this);
		gyrSpinner = (Spinner) rootView.findViewById(R.id.spinner_GYR_range);
		gyrSpinner.setOnItemSelectedListener(this);
		accSpinner = (Spinner) rootView.findViewById(R.id.spinner_ACC_range);
		accSpinner.setOnItemSelectedListener(this);
		magSpinner = (Spinner) rootView.findViewById(R.id.spinner_MAG_range);
		magSpinner.setOnItemSelectedListener(this);
		filterSpinner = (Spinner) rootView.findViewById(R.id.spinner_Filter_mode);
		filterSpinner.setOnItemSelectedListener(this);
		magCorrectionSpinner = (Spinner) rootView.findViewById(R.id.spinner_MAG_correction);
		magCorrectionSpinner.setOnItemSelectedListener(this);
		lowPassSpinner = (Spinner) rootView.findViewById(R.id.spinner_Low_pass);
		lowPassSpinner.setOnItemSelectedListener(this);
		dataModeSpinner = (Spinner) rootView.findViewById(R.id.spinner_data_mode);
		dataModeSpinner.setOnItemSelectedListener(this);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (!mBound) return;
		switch (parent.getId()){
			case R.id.spinner_rate:
				if(position == 0){
					if(lpService.getStreamFrequency()==5)return;
					lpService.setStreamFrequency(5);
				}else if (position == 1){
					if(lpService.getStreamFrequency()==10)return;
					lpService.setStreamFrequency(10);
				}else if (position == 2){
					if(lpService.getStreamFrequency()==25)return;
					lpService.setStreamFrequency(25);
				}else if (position == 3){
					if(lpService.getStreamFrequency()==50)return;
					lpService.setStreamFrequency(50);
				}else if(position == 4){
					if(lpService.getStreamFrequency()==100)return;
					lpService.setStreamFrequency(100);
				}else if (position == 5){
					if(lpService.getStreamFrequency()==200)return;
					lpService.setStreamFrequency(200);
				}else if (position == 6){
					if(lpService.getStreamFrequency()==400)return;
					lpService.setStreamFrequency(400);
				}else {
					break;
				}
				break;
			case R.id.spinner_GYR_range:
				if (position == 0) {
					if(lpService.getGyroRange()==125)return;
					lpService.setGyroRange(125);
				}else if (position == 1){
					if(lpService.getGyroRange()==245)return;
					lpService.setGyroRange(245);
				}else if (position == 2){
					if(lpService.getGyroRange()==500)return;
					lpService.setGyroRange(500);
				}else if (position == 3){
					if(lpService.getGyroRange()==1000)return;
					lpService.setGyroRange(1000);
				}else if (position == 4){
					if(lpService.getGyroRange()==2000)return;
					lpService.setGyroRange(2000);
				}else {
					break;
				}
				break;
			case R.id.spinner_ACC_range:
				if(position == 0){
					if(lpService.getAccRange()==2)return;
					lpService.setAccRange(2);
				}else if (position ==1){
					if(lpService.getAccRange()==4)return;
					lpService.setAccRange(4);
				}else if (position ==2){
					if(lpService.getAccRange()==8)return;
					lpService.setAccRange(8);
				}else if (position ==3){
					if(lpService.getAccRange()==16)return;
					lpService.setAccRange(16);
				}else {
					break;
				}
				break;
			case R.id.spinner_MAG_range:
				if(position == 0){
					if(lpService.getMagRange()==2)return;
					lpService.setMagRange(2);
				}else if (position ==1){
					if(lpService.getMagRange()==8)return;
					lpService.setMagRange(8);
				}else if (position ==2){
					if(lpService.getMagRange()==12)return;
					lpService.setMagRange(12);
				}else if (position ==3){
					if(lpService.getMagRange()==16)return;
					lpService.setMagRange(16);
				}else {
					break;
				}
				break;
			case R.id.spinner_Filter_mode:
				if(lpService.getFilterMode()==position)return;
				lpService.setFilterMode(position);
				break;
			case R.id.spinner_MAG_correction:
				if(lpService.getMagCorrection()==position)return;
				lpService.setMagCorrection(position);
				break;
			case R.id.spinner_Low_pass:
				if(lpService.getLowPassFilter()==position)return;
				lpService.setLowPassFilter(position);
				break;
			case R.id.spinner_data_mode:
				if(position == 0){
					if(!lpService.is16BitDataEnabled())return;
					lpService.enable32BitData();
				}else if(position == 1){
					if(lpService.is16BitDataEnabled())return;
					lpService.enable16BitData();
				}
				break;
			default:
				break;
		}
	}
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeCallbacks(UI_update);
		getActivity().unbindService(mConnection);
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
