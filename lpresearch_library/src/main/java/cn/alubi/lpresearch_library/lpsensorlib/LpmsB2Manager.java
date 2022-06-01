package cn.alubi.lpresearch_library.lpsensorlib;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Administrator on 8/2/2016.
 */
public class LpmsB2Manager {

	private ArrayList<LpmsB2> lpmsB2List = new ArrayList<LpmsB2>(8);

	public LpmsB2Manager(){
		//create new LpmsB2
		for(int i=0;i<=7;i++){
			lpmsB2List.add(i,new LpmsB2());
		}
	}

	public LpmsB2 getLpmsB2(int num){
		return lpmsB2List.get(num);
	}

	/**
	 * @param number
	 * @param address
	 * not run at UI Thread
	 */
	public boolean connect(int number,String address){
		if (getLpmsB2(number).getConnectionStatus() != LpmsB2.SENSOR_STATUS_DISCONNECTED) {
			return false;
		}
		// connect
		return lpmsB2List.get(number).connect(address);
	}

	// disconnect Classical and BLE
	public void disconnect(int number){
		if(lpmsB2List.get(number)!=null){
			lpmsB2List.get(number).disconnect();
		}
	}

	// disconnect Classical and BLE
	public void disconnectAll(){
		for(int i=0;i<lpmsB2List.size();i++){
			if(lpmsB2List.get(i)!=null)
				lpmsB2List.get(i).disconnect();
		}
	}

	public boolean connectBLE(int num , Context context, String address){
		if(lpmsB2List.get(num)==null)return false;
		if (getLpmsB2(num).getConnectionStatus() != LpmsB2.SENSOR_STATUS_DISCONNECTED) {
			return false;
		}
		return lpmsB2List.get(num).connectBLE(context,address);
	}

}
