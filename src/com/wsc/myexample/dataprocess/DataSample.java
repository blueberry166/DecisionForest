package com.wsc.myexample.dataprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class DataSample {
	/**
	 * 随机抽取100万正常用户
	 * @param args
	 * @throws IOException 
	 */
	public static String sample(String srcfile,int sampleN,int N) throws IOException{
		String output = srcfile.substring(0, srcfile.indexOf(".")) + "_sample.txt";
		File fout = new File(output);
		if (fout.exists()) {
			fout.delete();
		}

		ArrayList<Integer> trainIndices = new ArrayList<Integer>(sampleN);
		BootStrapSample(trainIndices,sampleN,N);//采样
		
		//生成训练数据--------
		Collections.sort(trainIndices);
		BufferedReader reader = new BufferedReader(new FileReader(srcfile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(fout));
		
		int cusor = 0;
		String i_line;
		String last_line = null;
		for(int i=0;i<trainIndices.size();i++){
			if(i-1>=0 && (trainIndices.get(i) == trainIndices.get(i-1)||trainIndices.get(i)<cusor)){//与上次的相等
				i_line = last_line;
			}else{
				String[] retData = getData(reader,cusor,trainIndices.get(i)).split(":");
				i_line = retData[0];
				cusor = Integer.parseInt(retData[1]);
			}
			
			if(!"".equals(i_line) && i_line!=null && !"null".equals(i_line)){
				writer.write(/*trainIndices.get(i)+","+*/i_line+"\r\n");//
			}
			last_line = i_line;
		}
		reader.close();
		writer.close();
		//----------------------
		return output;
	}
	
	private static void BootStrapSample(ArrayList<Integer> trainIndices,int SampleN, int numb) {
		for (int i = 0; i < SampleN; i++)
			trainIndices.add((int) Math.floor(Math.random() * numb));
	}

	private static String getData(BufferedReader reader,int cusor,int i) throws IOException{
		
		String retvalue = "";
		
		if(cusor == i){
			retvalue = getLine(reader);
			cusor++;
		}else if(cusor<i){
			//当前指针小于需要获取的数据编号，将当前指针往前移动，并将为“1”的保存
			while(cusor<i){
				String positiveline = getPositiveLine(reader);
				if(!"".equals(positiveline)){
					retvalue = retvalue + positiveline+"\r\n";//
				}
				cusor++;
			}
			retvalue = retvalue + getLine(reader);
			cusor++;
			
		}else{
			System.out.println("出错");
			return ""+":"+cusor;
		}
		return retvalue+":"+cusor;//返回值和当前下标
	}
	
	public static String getLine(BufferedReader reader) throws IOException{
		String retvalue;
		retvalue = reader.readLine();//
		while(retvalue.endsWith("1")){
			retvalue += "\r\n" + reader.readLine();
		}
		return retvalue;
	}
	
	public static String getPositiveLine(BufferedReader reader) throws IOException{
		String retvalue  = reader.readLine();//读取当前行
		
		if(retvalue.endsWith("1")){
//			retvalue += "\r\n" + reader.readLine();
			return retvalue;
		}
		return "";
	}
	
	public static void main(String[] args) {
		

	}

}
