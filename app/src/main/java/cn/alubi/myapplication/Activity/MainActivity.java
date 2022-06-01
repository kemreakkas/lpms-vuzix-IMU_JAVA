package cn.alubi.myapplication.Activity;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.os.StatFs;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

import cn.alubi.myapplication.Fragment.ConnectFragment1;
import cn.alubi.myapplication.Fragment.FunctionFragment4;
import cn.alubi.myapplication.Fragment.ModelFragment3;
import cn.alubi.myapplication.Fragment.RealtimeFragment2;
import cn.alubi.myapplication.Fragment.WebsiteFragment5;
import cn.alubi.myapplication.R;
import cn.alubi.myapplication.Service.LpService;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener {
	private ConnectFragment1 mfragment;
	private Fragment mfragment2,mfragment3,mfragment4,mfragment5;
	private  FragmentManager fm;

	private String[] mDeviceName;
	private String[] mDeviceAddress;

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	public static final String EXTRAS_CONNECT_MODEL = "CONNECT_MODEL";
	private LpService mService;
	boolean mBound = false;
	MyResultReceiver resultReceiver;

	long preTime;
	public static final long TWO_SECOND = 2*1250;
	private static Toast toast;
	public FileOutputStream outputStream ;
	public OutputStreamWriter streamWriter ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mDeviceName = getIntent().getStringArrayExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = getIntent().getStringArrayExtra(EXTRAS_DEVICE_ADDRESS);

		//获取ResultReceiver
		resultReceiver = new MyResultReceiver(null);

		//ShareSDK必须要初始化
		ShareSDK.initSDK(this,"1526af6283048");

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		//Fab menu
		allFloatingActionButtonInit();

		//Actionbar connect with NavigationView
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
		setDefaultFragment();
	}

	private void allFloatingActionButtonInit() {
		//可以用于动态加载fab
		FloatingActionMenu fabMeuw = (FloatingActionMenu) findViewById(R.id.fab_menu);
		for(int i = 0 ; i < mDeviceAddress.length; i++) {
			if(mDeviceAddress[i]!=null){
				final FloatingActionButton mFab = new FloatingActionButton(this);
				mFab.setImageResource(R.drawable.ic_bluetooth_64);
				mFab.setButtonSize(FloatingActionButton.SIZE_MINI);
				mFab.setLabelText(mDeviceName[i]);
				mFab.setId(i);
				mFab.setColorPressed(R.color.colorGreen);
				mFab.setColorRipple(R.color.colorPrimary);
				fabMeuw.addMenuButton(mFab,i);
				mFab.setOnClickListener(new View.OnClickListener() {
					@SuppressLint("ResourceType")
					@Override
					public void onClick(View v) {
						if(v.getId()<=7) {
							classicalOrBLEconnect(v.getId());
						}else {
							showToast("Too much devices");
						}
					}
				});
				mFab.setOnLongClickListener(new View.OnLongClickListener() {
					@SuppressLint("ResourceType")
					@Override
					public boolean onLongClick(View v) {
						if(v.getId()<=7) {
							disconnnect(v.getId());
							//showToast("long click :"+v.getId());
						}
						//true为不加短按,false为加入短按
						return true;
					}
				});
			}
		} 
	}

	private void setDefaultFragment()
	{
		fm = getFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		mfragment = new ConnectFragment1();
		transaction.replace(R.id.fragmentContainer,mfragment);
		transaction.commit();
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
				super.onBackPressed();
				unbindService(mConnection);
				stopService(new Intent(getBaseContext(), LpService.class));
				this.finish();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (drawer.isDrawerOpen(GravityCompat.START)) {
				drawer.closeDrawer(GravityCompat.START);
			}else{
				long currentTime = new Date().getTime();
				// 如果时间间隔大于3秒, 不处理
				if ((currentTime - preTime) > TWO_SECOND) {
					// 显示消息
					Toast.makeText(this, "Press again to exit",
							Toast.LENGTH_SHORT).show();
					// 更新时间
					preTime = currentTime;
					// 截获事件,不再处理
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		//noinspection SimplifiableIfStatement
		if (id == R.id.action_share) {
			showShare();
			//Log.d("test","mDeviceName"+mDeviceName[0]+" "+mDeviceName[1]+" "+mDeviceName[2]+" "+mDeviceName[3]+" ");
			//Log.d("test","mDeviceAddress"+mDeviceAddress[0]+" "+mDeviceAddress[1]+" "+mDeviceAddress[2]+" "+mDeviceAddress[3]+" ");
			return true;
		}else if(id==R.id.action_disconnect){
			mService.disconnect();
			//返回true表示处理完菜单项的事件，不需要将该事件继续传播下去了
			return true;
		}else if (id==R.id.action_alldisconnect){
			mService.disconnectAll();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	//切换Fragment的转换
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		FragmentTransaction transaction = fm.beginTransaction();
		hideAllFragment(transaction);
		switch (item.getItemId()){
			case R.id.nav_connect:
				if(mfragment == null){
					mfragment = new ConnectFragment1();
					transaction.add(R.id.fragmentContainer,mfragment);
				}else{
					transaction.show(mfragment);
				}
				break;
			case R.id.nav_realtime:
				if(mfragment2 == null){
					mfragment2 = new RealtimeFragment2();
					transaction.add(R.id.fragmentContainer,mfragment2);
				}else{
					transaction.show(mfragment2);
				}
				break;
			case R.id.nav_3d_model:
				if(mfragment3 == null){
					mfragment3 = new ModelFragment3();
					transaction.add(R.id.fragmentContainer,mfragment3);
				}else{
					transaction.show(mfragment3);
				}
				break;
			case R.id.nav_function:
				if(mfragment4 == null){
					mfragment4 = new FunctionFragment4();
					transaction.add(R.id.fragmentContainer,mfragment4);
				}else{
					transaction.show(mfragment4);
				}
				break;
			case R.id.nav_share:
				if(mfragment5 == null){
					mfragment5 = new WebsiteFragment5();
					transaction.add(R.id.fragmentContainer,mfragment5);
				}else{
					transaction.show(mfragment5);
				}
				break;
//			case R.id.nav_send:
//				if(mfragment == null){
//					mfragment = new ConnectFragment1();
//					transaction.add(R.id.fragmentContainer,mfragment);
//				}else{
//					transaction.show(mfragment);
//				}
//				break;
		}
		transaction.commit();
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	private void hideAllFragment(FragmentTransaction fragmentTransaction){
		if(mfragment != null)fragmentTransaction.hide(mfragment);
		if(mfragment2 != null)fragmentTransaction.hide(mfragment2);
		if(mfragment3 != null)fragmentTransaction.hide(mfragment3);
		if(mfragment4 != null)fragmentTransaction.hide(mfragment4);
		if(mfragment5 != null)fragmentTransaction.hide(mfragment5);
	}

	//ShareSDK
	private void showShare() {
			//ShareSDK.initSDK(this);
			OnekeyShare oks = new OnekeyShare();
			oks.disableSSOWhenAuthorize();
			oks.setTitle("LPMS-B2");
			oks.setTitleUrl("http://www.alubi.cn");
			oks.setText("你好呀！我正在使用阿路比的LPMS-B2传感器，性能很好呀，也推荐你来使用。");
			oks.setUrl("http://www.alubi.cn");
			oks.setSilent(true);
			oks.show(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		startOrBindToService();


	}
	public static long getAvailableSpaceInMBInternal(){
		final long SIZE_KB = 1024L;
		final long SIZE_MB = SIZE_KB * SIZE_KB;
		long availableSpace = -1L;
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		availableSpace = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
		return availableSpace/SIZE_MB;
	}
	public static long getAvailableSpaceInMbSD(){
		final long SIZE_KB = 1024L;
		final long SIZE_MB = SIZE_KB * SIZE_KB;
		long availableSpace = -1L;
		StatFs stat = new StatFs(Environment.getRootDirectory().getParent() + "/storage/external/");
		availableSpace = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
		return availableSpace/SIZE_MB;
	}
	private boolean isSdMounted(){
		File file = new File("/storage/external/");
		//hardcoded path to external storage as getExternalStorageDirectory() returns emulated storage.
		return android.os.Environment.getExternalStorageState(file).equals(android.os.Environment.MEDIA_MOUNTED) &&
				android.os.Environment.isExternalStorageRemovable(file);
	}
	private boolean isExternalStorageWritable() {

		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());

	}
	private boolean isExternalStorageReadable() {

		String state = Environment.getExternalStorageState();

		return (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));

	}

	@Override
	protected void onDestroy() {
		try {
			outputStream.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		super.onDestroy();
		ShareSDK.stopSDK(this);
	}

	//开启并连接Service
	public void startOrBindToService() {
		if(mService.isRunning()){
			bindToService();
		}else {
			Intent intent = new Intent(this, LpService.class);
			intent.putExtra("receiver", resultReceiver);
			startService(intent);
			bindToService();
		}
	}
	private void bindToService() {
		// Bind to LocalService
		Intent intent = new Intent(this, LpService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	/**
	 * Defines callbacks for service binding, passed to bindService()
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LpService.LocalBinder binder = (LpService.LocalBinder) service;
			//得到service
			mService = binder.getService();
			classicalOrBLEconnect(0);
			mBound = true;
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mService = null;
			mBound = false;
		}
	};

	private void classicalOrBLEconnect(int num) {
		if (mDeviceAddress[num] != null && !mDeviceAddress[num].isEmpty()){
			//Get connect Model chose BLE or Classical;
			if (!getIntent().getBooleanExtra(EXTRAS_CONNECT_MODEL,false)){
				mService.connectSensor(num,mDeviceAddress[num]);
			}else {
				mService.connectSensorBLE(num,mDeviceAddress[num]);
			}
			viewUpdate();
		}else {
			showToast("not Device Address");
		}
	}

	public void viewUpdate(){
		//viewUpdate;
		if(mfragment!=null)mfragment.viewUpdate();
	}

	public void disconnnect(int num){
		if (mDeviceAddress[num] != null && !mDeviceAddress[num].isEmpty()){
			if (!getIntent().getBooleanExtra(EXTRAS_CONNECT_MODEL,false)){
				mService.disconnect(num);
			}
		}
	}

	//fixed too much showToast at time.
	private void showToast(String message) {
		if (toast == null) {
			toast = Toast.makeText(this,
					message,
					Toast.LENGTH_SHORT);
		} else {
			toast.setText(message);
		}
		toast.show();
	}

	private void showSnackBar(String message){
		final Snackbar sb = Snackbar.make(findViewById(R.id.main_content),message,Snackbar.LENGTH_LONG);
		View sbView = sb.getView();
		//add layout &  logo picture
		Snackbar.SnackbarLayout sbLayout = (Snackbar.SnackbarLayout)sbView;
		View add_view = LayoutInflater.from(sbLayout.getContext()).inflate(R.layout.snackbar_layout,null);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		params.gravity= Gravity.CENTER_VERTICAL;
		sbLayout.addView(add_view,0,params);
		//set message TextView
		((TextView) sbView.findViewById(R.id.snackbar_text)).setEllipsize(TextUtils.TruncateAt.START);
		// Action TextView with onClick
		sb.setAction("OK", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sb.dismiss();
			}
		}).setActionTextColor(Color.GREEN)
			.show();
	}

	///////////////////////////////
	// Service Callbacks
	//////////////////////////////
	@SuppressLint("ParcelCreator")
	class MyResultReceiver extends ResultReceiver {
		public MyResultReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			if (resultCode == LpService.MSG_SENSOR_CONNECTED) {
				String msg = resultData.getString(LpService.KEY_MESG);
				showToastOnUI(msg);
			} else if (resultCode == LpService.MSG_SENSOR_DISCONNECTED) {
				String msg = resultData.getString(LpService.KEY_MESG);
				showToastOnUI(msg);
			} else if (resultCode == LpService.MSG_SENSOR_CONNECTION_ERROR) {
				String msg = resultData.getString(LpService.KEY_MESG);
				showToastOnUI(msg);
			} else if(resultCode == LpService.MSG_LOGGING_STARTED) {
				String msg = resultData.getString(LpService.KEY_MESG);
				//showToastOnUI(msg);
				showSnackBar(msg);
			} else if (resultCode == LpService.MSG_LOGGING_STOPPED) {
				String msg = resultData.getString(LpService.KEY_MESG);
				showToastOnUI(msg);
			} else if (resultCode == LpService.MSG_LOGGING_ERROR) {
				String msg = resultData.getString(LpService.KEY_MESG);
				showToastOnUI(msg);
			}else {
				showToast("yay");
			}
		}

		void showToastOnUI(String msg) {
			final String lMsg = msg;
			Handler h = new Handler(getBaseContext().getMainLooper());
			// Although you need to pass an appropriate context
			h.post(new Runnable() {
				@Override
				public void run() {
					showToast(lMsg);
				}
			});
		}
	}
}
