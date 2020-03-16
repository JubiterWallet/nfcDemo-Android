package com.ftsafe.nfcdemo.listener;

/**
 * Created by lxl on 2019/4/22.
 */
public interface ApiAsyncListener<T> {

	public void onUiChange();
	
	public void onResult(int errorCode, T result);
}
