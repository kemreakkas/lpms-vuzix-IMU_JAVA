package cn.alubi.myapplication.Fragment;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import cn.alubi.myapplication.R;

public class WebsiteFragment5 extends Fragment{

	private WebView webView;
	private GestureDetector mDetector;
	private final static int MIN_MOVE = 120;   //最小距离
	private MySimpleGestureListener mgListener;
	private ProgressBar mProgressBar;
	private SwipeRefreshLayout swipeRefreshLayout;

	//手势识别，自定义一个GestureListener,这个是View类下的.
	private class MySimpleGestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float v, float v1) {
			//向左滑动
			if(e1.getX() - e2.getX() > MIN_MOVE){
				if(webView.canGoBack())webView.goBack();
				return true;
			//向右滑动
			}else if(e1.getX() - e2.getX()  < -MIN_MOVE){
				if(webView.canGoForward())webView.goForward();
				return true;
			}
			return false;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View  rootView= inflater.inflate(R.layout.fragment_website, container, false);
		webView = (WebView)rootView.findViewById(R.id.webview);
		webView.loadUrl("http://www.lp-research.com/");
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
//			@Override
//			public void onReceivedError(WebView view, int errorCode, String description,
//										String failingUrl) {
//				super.onReceivedError(view, errorCode, description, failingUrl);
//				webView.loadUrl("file:///android_asset/404-Error-Page-Design-PSD.psd");
//			}
//			@Override
//			public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
//				super.onReceivedHttpError(view, request, errorResponse);
//				webView.loadUrl("file:///android_asset/");
//			}
		});
		//设置WebView属性,运行执行js脚本
		webView.getSettings().setJavaScriptEnabled(true);
		/*
		webView.getSettings().supportMultipleWindows();
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setTextZoom(5);
		*/
		//设置下载,打开一个对话框供你选择其中一个浏览器进行下载.
		webView.setDownloadListener(new DownloadListener(){
			@Override
			public void onDownloadStart(String url, String userAgent, String contentDisposition,
										String mimetype, long contentLength) {
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW,uri);
				startActivity(intent);
			}
		});
		//手势识别，实例化SimpleOnGestureListener与GestureDetector对象
		mgListener = new MySimpleGestureListener();
		mDetector = new GestureDetector(getActivity(), mgListener);
		webView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return mDetector.onTouchEvent(event);
			}
		});

		//mProgressBar
		mProgressBar = (ProgressBar)rootView.findViewById(R.id.myProgressBar);

		webView.setWebChromeClient(new WebChromeClient(){
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if (newProgress == 100) {
					mProgressBar.setVisibility(ProgressBar.GONE);
				} else {
					if (ProgressBar.GONE == mProgressBar.getVisibility()) {
						mProgressBar.setVisibility(ProgressBar.VISIBLE);
					}
					mProgressBar.setProgress(newProgress);
				}
				super.onProgressChanged(view, newProgress);
			}
		});

		//SwipeRefreshLayout
		swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout2);
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
				swipeRefreshLayout.setRefreshing(false);
				webView.reload();
			}
		});
		return rootView;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
