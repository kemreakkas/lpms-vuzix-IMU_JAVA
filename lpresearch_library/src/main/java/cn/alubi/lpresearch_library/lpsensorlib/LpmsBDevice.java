package cn.alubi.lpresearch_library.lpsensorlib;

public class LpmsBDevice {
	
	private int deviceId ;
	private String deviceAddress = null;
	private String deviceName = null;
	private  boolean isSelect = false;
	
	public LpmsBDevice(){
		
	}
	public LpmsBDevice(String deviceName,String deviceAddress){
		this.deviceName = deviceName;
		this.deviceAddress = deviceAddress;
	}

	public LpmsBDevice(int Id,String deviceAddress){
		this.deviceId = Id;
		this.deviceAddress = deviceAddress;
	}

	public int getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}
	public String getDeviceAddress() {
		return deviceAddress;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public boolean isSelect() {
		return isSelect;
	}
	public void setSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}

}
