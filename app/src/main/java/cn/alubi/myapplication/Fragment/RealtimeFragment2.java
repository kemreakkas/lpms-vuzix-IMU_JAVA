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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import cn.alubi.myapplication.R;
import cn.alubi.myapplication.Service.LpService;
import cn.alubi.lpresearch_library.lpsensorlib.LpmsB2;
import cn.alubi.lpresearch_library.lpsensorlib.LpmsBData;

public class RealtimeFragment2 extends Fragment{

	private Handler mHandler = new Handler();
	private GraphView graph;
	private LpService lpService;
	private LineGraphSeries<DataPoint> mSeries1,mSeries2,mSeries3;
	private double graph2LastXValue = 4d;
	private LpmsBData d = null;
	boolean mBound = false;
	private int connectionStatus;
	private LegendRenderer legendRenderer;
	boolean flag = false;
	float[] data = new float[4];
	int dataSelect = 0 ;
	long updateRate = 20;//(default 20ms)

	//Button
	private RadioGroup radioGroup;
	private RadioButton rb_Acc,rb_Mag,rb_Gyr,rb_AngVel,rb_Quat,rb_EulerAngel,rb_LinAcc, rb_Stop;
	private CheckBox cb_Xaxis,cb_Yaxis,cb_Zaxis;

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_realtime, container, false);
		//onDraw realTime view.
		graph = (GraphView) rootView.findViewById(R.id.graph2);
		onDraw();

		//Radio Button
		radioGroup = (RadioGroup) rootView.findViewById(R.id.radioGroup);
		rb_Acc = (RadioButton) rootView.findViewById(R.id.radioAcc);
		rb_Acc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					removeAndRedraw(0);}return;
			}
		});
		rb_Mag = (RadioButton) rootView.findViewById(R.id.radioMag);
		rb_Mag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					removeAndRedraw(1);}return;
			}
		});
		rb_Gyr = (RadioButton) rootView.findViewById(R.id.radioGyr);
		rb_Gyr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					removeAndRedraw(2);}return;
			}
		});
		rb_AngVel = (RadioButton) rootView.findViewById(R.id.radioAngVel);
		rb_AngVel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					removeAndRedraw(3);}return;
			}
		});
		/*
		rb_Quat = (RadioButton) rootView.findViewById(R.id.radioQuaternion);
		rb_Quat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					removeAndRedraw(4);}return;
			}
		});
		*/
		rb_EulerAngel = (RadioButton) rootView.findViewById(R.id.radioEulerAngle);
		rb_EulerAngel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					removeAndRedraw(5);}return;
			}
		});
		rb_LinAcc = (RadioButton) rootView.findViewById(R.id.radioLinAcc);
		rb_LinAcc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					removeAndRedraw(6);}return;
			}
		});
		//Stop
		rb_Stop = (RadioButton) rootView.findViewById(R.id.radioStop);
		rb_Stop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){onDraw();}return;
			}
		});
		// X Y Z CheckBox
		cb_Xaxis = (CheckBox) rootView.findViewById(R.id.cb_Xaxis);
		cb_Yaxis = (CheckBox) rootView.findViewById(R.id.cb_Yaxis);
		cb_Zaxis = (CheckBox) rootView.findViewById(R.id.cb_Zaxis);
		cb_Xaxis.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					graph.addSeries(mSeries1);
				}else {
					graph.removeSeries(mSeries1);
				}
			}
		});
		cb_Yaxis.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					graph.addSeries(mSeries2);
				}else {
					graph.removeSeries(mSeries2);
				}
			}
		});
		cb_Zaxis.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					graph.addSeries(mSeries3);
				}else {
					graph.removeSeries(mSeries3);
				}
			}
		});
		return rootView;
	}

	public void changeData(){
		switch (dataSelect) {
			case 0:
				for(int i = 0 ; i<3;i++){
					data[i] = d.acc[i];
				}
				break;
			case 1:
				for(int i = 0 ; i<3;i++){
					data[i] = d.mag[i];
				}
				break;
			case 2:
				for(int i = 0 ; i<3;i++){
					data[i] = d.gyr[i];
				}
				break;
			case 3:
				for(int i = 0 ; i<3;i++){
					data[i] = d.angVel[i];
				}
				break;
		/*	case 4:
				for(int i = 0 ; i<4;i++){
					data[i] = d.quat[i];
				}
				break;
		*/
			case 5:
				for(int i = 0 ;i<3;i++){
					data[i] = d.euler[i];
				}
				break;
			case 6:
				for(int i = 0 ;i<3;i++){
					data[i] = d.linAcc[i];
				}
				break;
			default:
				break;
			}
	}

	public void removeAndRedraw(int n){
		dataSelect = n;
		graph.removeAllSeries();
		cb_Xaxis.setChecked(false);
		cb_Yaxis.setChecked(false);
		cb_Zaxis.setChecked(false);
		mHandler.removeCallbacks(mTimer);
		onDraw();
		mHandler.postDelayed(mTimer,100);
		cb_Xaxis.setChecked(true);
		cb_Yaxis.setChecked(true);
		cb_Zaxis.setChecked(true);
	}

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

	private void onDraw() {
		mSeries1 = new LineGraphSeries<DataPoint>();
		mSeries2 = new LineGraphSeries<DataPoint>();
		mSeries3 = new LineGraphSeries<DataPoint>();
		//To set a fixed manual viewport use this
		//graph.getViewport().setXAxisBoundsManual(true);
		//graph.getViewport().setYAxisBoundsManual(true);
		graph.getViewport().setMinX(0);
		graph.getViewport().setMaxX(50);
		//To activate scaling and zooming use this methods
		graph.getViewport().setScrollable(true);
		graph.getViewport().setScalable(true);
		//线条粗细
		mSeries1.setThickness(3);
		mSeries2.setThickness(3);
		mSeries3.setThickness(3);
		//线条颜色设置
		mSeries1.setColor(Color.BLUE);
		mSeries2.setColor(Color.GREEN);
		mSeries3.setColor(Color.RED);
		//graph.getGridLabelRenderer().setHorizontalAxisTitle("ACC");

		//Right-Top Chart Legend.
		mSeries1.setTitle("X");
		mSeries2.setTitle("Y");
		mSeries3.setTitle("Z");
		legendRenderer = graph.getLegendRenderer();
		legendRenderer.setWidth(120);
		legendRenderer.setVisible(true);
		legendRenderer.setBackgroundColor(Color.TRANSPARENT);
		legendRenderer.setAlign(LegendRenderer.LegendAlign.TOP);

	}

	private Runnable mTimer = new Runnable() {
			@Override
			public void run() {
				if(mBound){
					d = lpService.getSensorData();			//从service那里得到data
					connectionStatus = lpService.getSensorConnectionStatus();
					updateRate = 1000/lpService.getStreamFrequency();
					if (connectionStatus == LpmsB2.SENSOR_STATUS_CONNECTED) {
						if(lpService.isStreamingMode()){
							//得到时间
							graph2LastXValue +=1d ;
							changeData();
							//if(data[0]+data[1]+data[2]==0)return;

							//添加数据
							mSeries1.appendData(new DataPoint(graph2LastXValue,data[0]), true, 50);
							mSeries2.appendData(new DataPoint(graph2LastXValue,data[1]), true, 50);
							mSeries3.appendData(new DataPoint(graph2LastXValue,data[2]), true, 50);

							flag = true;
						}
						mHandler.postDelayed(this,updateRate);
					} else if (connectionStatus != LpmsB2.SENSOR_STATUS_CONNECTED) {
						//第一次运行默认不连接。
						//判断是否曾经连接过。
						if(flag) {
							flag = false;
							//清除旧的service(线);
							graph.removeAllSeries();
							//重新绘制GraphView
							onDraw();
						}
						mHandler.postDelayed(this,1000);
					}
				}
			}
		};

	@Override
	public void onResume() {
		super.onResume();
		//bindService
		Intent intent = new Intent(getActivity(), LpService.class);
		//mConnection
		getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		mHandler.postDelayed(mTimer,100);
	}

	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeCallbacks(mTimer);
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
