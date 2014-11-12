package com.wsc.myexample.dataprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * "准开通，正常服务，呼叫限制"为正常用户，其他为离网用户
 * @author Administrator
 *
 */
public class DataLabel {
	
	private int dataCnt = 0;
	private int negetiveCnt = 0;//离网用户数
	private int attrCnt;
	
	/**
	 * 统计数据个数和离网用户数
	 * @param srcfile
	 * @throws IOException
	 */
	public void statisticsDataCnt(String srcfile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(srcfile));
		String line;
//		String[] arr;
		while ((line = br.readLine()) != null) {
			
//			arr = line.split(",");
			dataCnt++;
			if(line.endsWith("1")){
				negetiveCnt++;
			}
		}
		br.close();
	}
	
	/**
	 * 根据状态判断是否离网
	 * @param srcfile
	 * @param statusFile
	 * @return
	 * @throws IOException
	 */
	public String labelData(String srcfile,String statusFile) throws IOException{
		
		HashMap<String,Integer> statusMap = new HashMap<String, Integer>();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(statusFile), "GBK"));
		String line;
		String[] arr;
		while((line = br.readLine())!=null){
			arr = line.split("\t");
			if(arr.length<4) continue;
			if(arr[3].trim().equals("准开通")||arr[3].trim().equals("正常服务")||arr[3].trim().equals("呼叫限制")){
				statusMap.put(arr[1], 0);
			}else{
				//离网用户
				statusMap.put(arr[1], 1);
			}
		}
		br.close();
		
		String output = srcfile.substring(0, srcfile.indexOf(".")) + "_label.txt";
		File fout = new File(output);
		if (fout.exists()) {
			fout.delete();
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(fout));
		br = new BufferedReader(new InputStreamReader(
				new FileInputStream(srcfile), "GBK"));

		while ((line = br.readLine()) != null) {
			arr = line.split("\t");
			if(statusMap.containsKey(arr[0].trim())){
				int status = statusMap.get(arr[0].trim());
				line = line.replaceAll("\t", ",");
				line += ","+status;
				
				bw.write(line+"\r\n");
				
				dataCnt++;
				if(status==1){
					negetiveCnt++;
				}
			}
		}
		bw.close();
		br.close();
		return output;
	}
	public int getDataCnt() {
		return dataCnt;
	}
	public void setDataCnt(int dataCnt) {
		this.dataCnt = dataCnt;
	}
	public int getNegetiveCnt() {
		return negetiveCnt;
	}
	public void setNegetiveCnt(int negetiveCnt) {
		this.negetiveCnt = negetiveCnt;
	}
	public int getAttrCnt() {
		return attrCnt;
	}
	public void setAttrCnt(int attrCnt) {
		this.attrCnt = attrCnt;
	}
	
	public static void main(String[] args) {

	}
}
