package com.mmscn.libform4n;

import java.util.List;
import java.util.Map;

public interface FormRefreshListener {
	public void onFormRefreshed(List<Map<String, String>> data);
}
