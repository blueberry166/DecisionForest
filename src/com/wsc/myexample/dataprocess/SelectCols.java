package com.wsc.myexample.dataprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

public class SelectCols {
	
	public static String selectCols(String fileName,HashSet<Integer> colIndexs,ArrayList<int[]> mergeCols) throws IOException{
		
		String outf = fileName.substring(0, fileName.indexOf(".")) + "_selectCols.txt";
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outf));
		String line;
		String[] arr;
		boolean flg;
		int colCnt = 0;//当前列标号
		while((line = br.readLine())!=null){
			colCnt = 0;
			arr = line.split(",");
			
//			System.out.println(line);
			
			line = "";
			
			for(int i=0;i< arr.length;i++){
				flg = true;
				
				if(mergeCols!=null){
					for(int[] cols:mergeCols){
						//
						if(cols[0] ==i){
							line += (Integer.parseInt(arr[cols[0]])+Integer.parseInt(arr[cols[1]])) +",";
							flg = false;
							
						}else if(cols[1] ==i){
							flg = false;
						}
					}
				}
				
				if(!colIndexs.contains(i) && flg){
					line+=arr[i]+",";					
				}
			}
			
			bw.write(line.substring(0, line.length()-1) + "\r\n");
//			System.out.println(line.substring(0, line.length() - 1) + "\r\n");
		}
		System.out.println("列count:" + colCnt);
		
		br.close();
		bw.close();
		
		return outf;
	}
	
	public static void main(String[] args) throws IOException {
		
		HashSet removeCols = new HashSet();
		removeCols.add(3);
		removeCols.add(9);
		removeCols.add(37);
		removeCols.add(39);
//		removeCols.add(1);
//		removeCols.add(1);
		
		ArrayList<int[]> mergeCols = new ArrayList<int[]>();
		mergeCols.add(new int[]{30,31});
		mergeCols.add(new int[]{33,34});
		
		String infoIds= selectCols("/home/wsc/tmp/716/data714_2.txt",removeCols,mergeCols);
	}

}
