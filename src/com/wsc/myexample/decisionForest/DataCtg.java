package com.wsc.myexample.decisionForest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DataCtg {

//	public static ArrayList<String> getDatactg(String datapath)
//			throws IOException {
//		BufferedReader br = new BufferedReader(new FileReader(datapath));
//		String record;
//		String[] arr = null;
//		while ((record = br.readLine()) != null) {
//			arr = record.split(",");
//			break;
//		}
//		br.close();
//
//		ArrayList<String> dataCtg = new ArrayList<String>();
//
//		int ctgNum = 1;
//		String c_ctg;
//		String last_ctg = null;
//		for (int i = 0; i < arr.length - 1; i++) {
//			try {
//				Double.parseDouble(arr[i]);
//				c_ctg = "N";
//
//			} catch (NumberFormatException e) {
//				// e.printStackTrace();
//				c_ctg = "C";
//			}
//
//			if (c_ctg.equals(last_ctg)) {
//				ctgNum++;
//			} else {
//				// 保存之前的内容
//				if (last_ctg != null) {
//					if (ctgNum > 1) {
//						dataCtg.add(ctgNum + "");
//					}
//					dataCtg.add(last_ctg);
//				}
//				last_ctg = c_ctg;
//				ctgNum = 1;
//			}
//		}
//
//		if (last_ctg != null) {
//			if (ctgNum > 1) {
//				dataCtg.add(ctgNum + "");
//			}
//			dataCtg.add(last_ctg);
//		}
//		return dataCtg;
//	}

	public static ArrayList<Entry<Integer, String>> getColInfo(String datapath)
			throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(datapath));
		HashMap<Integer,String> ctgs = new HashMap<Integer,String>();//每列的类型
		
		String line = br.readLine();
		int arrcount = line.split(",").length -1;
		
		int count = 0;
		String[] arr;
		
		while(line!=null){
			arr = line.split(",");
			
			for(int i=0;i<arr.length-1;i++){//最后一列为label
				if(!ctgs.containsKey(i)){
					
					if(!arr[i].trim().equals("")){
						String c_ctg;
						
						try {
							Double.parseDouble(arr[i]);
							c_ctg = "N";
							
						} catch (NumberFormatException e) {
//							e.printStackTrace();
							c_ctg = "C";
						}
						
						ctgs.put(i, c_ctg);//number
						count++;
						if(count == arrcount){
							break;
						}
						
					}
				}
			}
			
			if(count == arrcount){
				break;
			}
			line = br.readLine();
		}
		br.close();
		
		ArrayList<Entry<Integer, String>> idinfos = new ArrayList<Entry<Integer, String>>(ctgs.entrySet());
		//排序
		Collections.sort(idinfos, new Comparator<Entry<Integer, String>>() {   
		    public int compare(Map.Entry<Integer, String> o1, Map.Entry<Integer, String> o2) {      
		        return (o1.getKey() - o2.getKey()); 
//		        return (o1.getKey()).toString().compareTo(o2.getKey());
		    }
		});
		return idinfos;
	}
	
	public static ArrayList<String> computeCtgCnt(List<Entry<Integer, String>> idinfos) {

		ArrayList<String> dataCtg = new ArrayList<String>();

		int ctgNum = 1;
		String c_ctg;
		String last_ctg = null;
		for (int i = 0; i < idinfos.size(); i++) {
			
			c_ctg = idinfos.get(i).getValue();
			if(c_ctg == null) {
				c_ctg = "N";
				System.out.println("error----------第"+ (i+1) +"列所有元素为空");
			}

			if (c_ctg.equals(last_ctg)) {
				ctgNum++;
			} else {
				// 保存之前的内容
				if (last_ctg != null) {
					if (ctgNum > 1) {
						dataCtg.add(ctgNum + "");
					}
					dataCtg.add(last_ctg);
				}
				last_ctg = c_ctg;
				ctgNum = 1;
			}
		}

		if (last_ctg != null) {
			if (ctgNum > 1) {
				dataCtg.add(ctgNum + "");
			}
			dataCtg.add(last_ctg);
		}
		return dataCtg;
	}

	public static void main(String[] args) {

	}

}
