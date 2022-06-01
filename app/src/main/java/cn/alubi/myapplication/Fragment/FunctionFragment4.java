package cn.alubi.myapplication.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import cn.alubi.myapplication.Adapter.ImageTextButton;
import cn.alubi.myapplication.R;
import cn.alubi.myapplication.Service.LpService;

public class FunctionFragment4 extends Fragment implements View.OnClickListener {

	private LpService lpService;
	boolean mBound = false;

	private ImageTextButton btn_saveData,btn_setOffset,btn_resetOffset;
	private ImageTextButton btn_startFlashLog,btn_stopFlashLog,btn_clearFlashLog,btn_saveFlashLog,btn_fullFlashErase;
	private ProgressDialog pd,flashErasePd;
	private AlertDialog.Builder aDialog;

	private Handler mHandler;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_function, container, false);
		mHandler = new Handler();
		//button logging
		btn_saveData = (ImageTextButton) rootView.findViewById(R.id.btn_logging);
		btn_saveData.setImageResource(R.drawable.save_black_64dp);
		btn_saveData.setTextViewText("    Save Data   ");
		btn_saveData.setOnClickListener(this);
		//button setOffset
		btn_setOffset = (ImageTextButton) rootView.findViewById(R.id.btn_setOffset);
		btn_setOffset.setImageResource(R.drawable.set_offset_onclick_64dp);
		btn_setOffset.setTextViewText("    Set Offset   ");
		btn_setOffset.setOnClickListener(this);
		//button resetOffset
		btn_resetOffset = (ImageTextButton) rootView.findViewById(R.id.btn_resetOffset);
		btn_resetOffset.setImageResource(R.drawable.reset_offset_onclick_64dp);
		btn_resetOffset.setTextViewText("  Reset Offset  ");
		btn_resetOffset.setOnClickListener(this);

		//button startFlashLog
		btn_startFlashLog = (ImageTextButton) rootView.findViewById(R.id.btn_startFlashLog);
		btn_startFlashLog.setImageResource(R.drawable.start_flash_onclick_64dp);
		btn_startFlashLog.setTextViewText("Start Flash Log");
		btn_startFlashLog.setOnClickListener(this);
		//button Stop Flash Log
		btn_stopFlashLog = (ImageTextButton) rootView.findViewById(R.id.btn_stopFlashLog);
		btn_stopFlashLog.setImageResource(R.drawable.stop_flash_onclick_64dp);
		btn_stopFlashLog.setTextViewText("Stop Flash Log");
		btn_stopFlashLog.setOnClickListener(this);
		//button Clear Flash Log
		btn_clearFlashLog = (ImageTextButton) rootView.findViewById(R.id.btn_clearFlashLog);
		btn_clearFlashLog.setImageResource(R.drawable.clear_flash_onclick_64dp);
		btn_clearFlashLog.setTextViewText("Clear Flash Log");
		btn_clearFlashLog.setOnClickListener(this);
		//button Save Flash Log
		btn_saveFlashLog = (ImageTextButton) rootView.findViewById(R.id.btn_saveFlashLog);
		btn_saveFlashLog.setImageResource(R.drawable.save_flash_onclick_64dp);
		btn_saveFlashLog.setTextViewText("Save Flash Log");
		btn_saveFlashLog.setOnClickListener(this);
		//button Full Flash Erase
		btn_fullFlashErase = (ImageTextButton) rootView.findViewById(R.id.btn_fullFlashErase);
		btn_fullFlashErase.setImageResource(R.drawable.full_flash_onclick_64dp);
		btn_fullFlashErase.setTextViewText("Full Flash Erase");
		btn_fullFlashErase.setOnClickListener(this);

		pd = new ProgressDialog(getActivity());
		flashErasePd = new ProgressDialog(getActivity());
		aDialog = new AlertDialog.Builder(getActivity());

		return rootView;
	}

	Runnable UIRunnable = new Runnable() {
		@Override
		public void run() {
			if(lpService.isLogging()){
				btn_saveData.setImageResource(R.drawable.stop_red_64dp);
			}else {
				btn_saveData.setImageResource(R.drawable.save_black_64dp);
			}
			if(lpService.isDebugSaving()) {
				pd.incrementProgressBy(lpService.getUpdataProgress() - pd.getProgress());
			}else {
				pd.dismiss();
			}
			mHandler.postDelayed(this,200);
		}
	};

	@Override
	public void onResume() {
		//bindService
		Intent intent = new Intent(getActivity(), LpService.class);
		//mConnection
		getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		mHandler.postDelayed(UIRunnable,200);
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeCallbacks(UIRunnable);
		getActivity().unbindService(mConnection);
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
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_logging:
				if (!lpService.isLogging()) {
					lpService.startLogging();
				}else {
					lpService.stopLogging();
				}
				break;
			case R.id.btn_setOffset:
				lpService.setOrientationOffset(0);
				break;
			case R.id.btn_resetOffset:
				lpService.resetOrientationOffset();
				break;
			case R.id.btn_startFlashLog:
				lpService.startDebugLogging();
				break;
			case R.id.btn_stopFlashLog:
				lpService.stopDebugLogging();
				break;
			case R.id.btn_clearFlashLog:
				lpService.clearDebugLog();
				break;
			case R.id.btn_saveFlashLog:
				showProgressDialog();
				lpService.saveDebugLogging();
				break;
			case R.id.btn_fullFlashErase:
				if (lpService.getFlashState()==0){
					showDialog();
				}else{
					showFlashEraseWaitingProcessDialog();
				}
				break;
		}
	}

	public void showProgressDialog(){
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条
		//pd.setCancelable(true);// 设置是否可以通过点击Back键取消
		pd.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
		pd.setIcon(R.drawable.ic_header);// 设置提示的title的图标，默认是没有的
		pd.setTitle("Flash data in downloading");
		pd.setMax(100);
		pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						lpService.stopDebugSaving();
					}
				});
		pd.setMessage("The data is kept in LpLogger folder");
		pd.show();
	}

	public void showDialog(){
		aDialog.setTitle("Attention");
		aDialog.setMessage("Check your flash state is OK.\r\nIf you erase the flash will take a long time!\r\nSure you want to continue?");
		aDialog.setPositiveButton("sure", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showFlashEraseWaitingProcessDialog();
			}
		});
		aDialog.setNegativeButton("no", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		aDialog.show();
	}

	public void showFlashEraseWaitingProcessDialog(){
		flashErasePd.setTitle("Flash Erase");
		flashErasePd.setMessage("Please be patient");
		flashErasePd.setIndeterminate(true);
		flashErasePd.setCancelable(false);

		new Thread(new Runnable() {
			@Override
			public void run() {
				//full Flash Erase running ...
				if (lpService.fullFlashErase()){
					//full Flash Erase finish
					flashErasePd.dismiss();
				}
			}
		}).start();

		flashErasePd.show();
	}
}
