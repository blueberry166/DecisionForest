package com.wsc.myexample.dataprocess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class DataProcessMain {
	
	private int dataCnt;
//	private int attrCnt;
	private int negetiveCnt;//离网用户数
	private int positiveCnt;//离网用户数
	
	public String dataprocess(String srcfile,String statusFile){
		
		try {
			//-------打标签---------
			String labeledFile;
			DataLabel dl = new DataLabel();
			if(!"".equals(statusFile)){
				labeledFile = dl.labelData(srcfile, statusFile);
				
				dataCnt = dl.getDataCnt();
				negetiveCnt = dl.getNegetiveCnt();
				positiveCnt = (int)(dataCnt - negetiveCnt);
			}else{
				labeledFile = srcfile;
				
				dl.statisticsDataCnt(srcfile);
				
				dataCnt = dl.getDataCnt();
				negetiveCnt = dl.getNegetiveCnt();
				positiveCnt = (int)(dataCnt - negetiveCnt);
			}
			
			//-------------对中文字符编码，保存----------
			DataCode dc = new DataCode();
			String codeFile = dc.labelDataforBuild(labeledFile);
			
			//-------------选择所需要的列--------------
			HashSet removeCols = new HashSet();
			removeCols.add(3);
			removeCols.add(9);
			removeCols.add(37);
			removeCols.add(39);
			
			ArrayList<int[]> mergeCols = new ArrayList<int[]>();
			mergeCols.add(new int[]{30,31});
			mergeCols.add(new int[]{33,34});
			
			String selectedFile = SelectCols.selectCols(codeFile,removeCols,mergeCols);
			
			//--------------采样---------------------
			String sampleF = DataSample.sample(selectedFile, (int)(positiveCnt*0.3), positiveCnt);
			return sampleF;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static void main(String[] args) {
		
	}

}
