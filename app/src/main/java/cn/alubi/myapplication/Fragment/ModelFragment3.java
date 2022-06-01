package cn.alubi.myapplication.Fragment;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import cn.alubi.myapplication.OpenGL.OpenGLRenderer;
import cn.alubi.myapplication.R;
import cn.alubi.myapplication.Service.LpService;
import cn.alubi.lpresearch_library.lpsensorlib.LpmsB2;
import cn.alubi.lpresearch_library.lpsensorlib.LpmsBData;

public class ModelFragment3 extends Fragment implements View.OnClickListener{

	// 3D Cube
	OpenGLRenderer cubeRenderer = null;
	private GLSurfaceView mGlView;

	private LpService lpService;
	boolean mBound = false;

	private int connectionStatus;
	private LpmsBData d = null;
	private Handler mHandler = new Handler();

	private ImageButton btn_setOffset2,btn_resetOffset2;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_3d_model, container, false);

		//button setOffset
		btn_setOffset2 = (ImageButton) rootView.findViewById(R.id.btn_setOffset_3d);
		btn_setOffset2.setImageResource(R.drawable.set_offset_onclick_36dp);
		btn_setOffset2.setOnClickListener(this);
		//button resetOffset
		btn_resetOffset2 = (ImageButton) rootView.findViewById(R.id.btn_resetOffset_3d);
		btn_resetOffset2.setImageResource(R.drawable.reset_offset_onclick_36dp);
		btn_resetOffset2.setOnClickListener(this);

		cubeRenderer = new OpenGLRenderer(getActivity());
		mGlView = (GLSurfaceView)rootView.findViewById(R.id.glview1);
//		mGlView.setZOrderOnTop(true);
		mGlView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
	//mGlView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		mGlView.getHolder().setFormat(PixelFormat.RGBA_8888);
		mGlView.setRenderer(cubeRenderer);
		return rootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
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

	@Override
	public void onResume() {
		super.onResume();
		//bindService
		Intent intent = new Intent(getActivity(), LpService.class);
		//mConnection
		getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		final Runnable mTimer = new Runnable() {
			@Override
			public void run() {
				if(mBound){
					d = lpService.getSensorData();			//从service那里得到data
					connectionStatus = lpService.getSensorConnectionStatus();
					if (connectionStatus == LpmsB2.SENSOR_STATUS_CONNECTING || connectionStatus == LpmsB2.SENSOR_STATUS_CONNECTED) {
						if(lpService.isStreamingMode()){
							cubeRenderer.updateRotation(d.quat);
						}
						mHandler.postDelayed(this, 20);
					} else if (connectionStatus == LpmsB2.SENSOR_STATUS_DISCONNECTED) {
						mHandler.postDelayed(this,1000);
					}
				}
			}
		};
		mHandler.postDelayed(mTimer, 200);
	}

	@Override
	public void onPause() {
		super.onPause();
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

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_setOffset_3d:
				lpService.setOrientationOffset(0);
				break;
			case R.id.btn_resetOffset_3d:
				lpService.resetOrientationOffset();
				break;
		}
	}
}
