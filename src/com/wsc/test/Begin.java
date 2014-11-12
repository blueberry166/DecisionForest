package com.wsc.test;

import java.io.IOException;

import com.wsc.myexample.dataprocess.DataProcessMain;

public class Begin {

	public static void main(String[] args) throws IOException {
		try {
			
			DataProcessMain dpmain = new DataProcessMain();
			String file = dpmain.dataprocess("F:/交往圈分析/718/temp1_label.txt", "");
			
			MyDFMain tmd = new MyDFMain();
	    	tmd.build(file,"F:/交往圈分析/718/forest_result");
	    	tmd.predict(file,"F:/交往圈分析/718/predict","F:/交往圈分析/718/forest_result");
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}
}