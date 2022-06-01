package cn.alubi.myapplication.Adapter;

import java.util.ArrayList;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import cn.alubi.myapplication.Activity.ScanActivity;
import cn.alubi.myapplication.R;
import cn.alubi.lpresearch_library.lpsensorlib.LpmsBDevice;

public class MyListViewAdapter extends ArrayAdapter {

	    private Context mContext;
	    private ArrayList<LpmsBDevice> mLpmsBDevicelist;

	    public MyListViewAdapter(ArrayList<LpmsBDevice> mLpmsBDevicelist, Context context) {

			super(context, R.layout.list_item,mLpmsBDevicelist);
	        this.mLpmsBDevicelist = mLpmsBDevicelist;
	        this.mContext = context;
	        
	    }

	    @Override
	    public int getCount() {
	        return mLpmsBDevicelist.size();
	    }

	    @Override
	    public Object getItem(int position) {
	        return null;
	    }

	    @Override
	    public long getItemId(int position) {
	        return position;
	    }

		private static class ViewHolder {
			public ImageView img_icon;
			public TextView txt_name;
			public TextView txt_address;
			public CheckBox chkBox;
		}

	    @Override
	    public View getView(final int position, View convertView, ViewGroup parent) {

			ViewHolder holder = new ViewHolder();

	        if(convertView == null){
				//api????
				convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);
	            holder.img_icon = (ImageView) convertView.findViewById(R.id.img_b2icon);
	            holder.txt_name = (TextView) convertView.findViewById(R.id.tv_b2name);
	            holder.txt_address = (TextView) convertView.findViewById(R.id.tv_b2address);
				holder.chkBox = (CheckBox) convertView.findViewById(R.id.cb_b2);
				holder.chkBox.setOnCheckedChangeListener((ScanActivity) mContext);
				//这个很重要，用来第二次添加数据时使用
				final ViewHolder finalHolder = holder;
				convertView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						finalHolder.chkBox.performClick();
					}
				});
				convertView.setTag(finalHolder);
	        }else{
	            holder = (ViewHolder) convertView.getTag();
	        }
			LpmsBDevice device = mLpmsBDevicelist.get(position);

	        holder.img_icon.setImageResource(R.drawable.bluetooth1);
	        holder.txt_name.setText(device.getDeviceName());
	        holder.txt_address.setText(device.getDeviceAddress());

			holder.chkBox.setChecked(device.isSelect());
			holder.chkBox.setTag(device);

	        return convertView;
	    }

	    public void add(LpmsBDevice device) {
	        if (mLpmsBDevicelist == null) {
	        	mLpmsBDevicelist = new ArrayList<LpmsBDevice>();
	        }
	        mLpmsBDevicelist.add(device);
	        notifyDataSetChanged();
	    }

}
