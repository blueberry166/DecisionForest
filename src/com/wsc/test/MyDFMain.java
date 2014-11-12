package com.wsc.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.mahout.classifier.df.data.DescriptorException;

import com.wsc.myexample.decisionForest.DataCtg;
import com.wsc.myexample.decisionForest.MyBuildForest;
import com.wsc.myexample.decisionForest.MyDescribe;
import com.wsc.myexample.decisionForest.MyTestForest;

/**
 * Created by ashqal on 14-3-7.
 */
public class MyDFMain {
	
	private String datapath = null;
	private String datainfo = null;
	private String modelpath = null;
	
//	private int treeNum;
//	private int sl;//Number of variables to select randomly at each tree-node
	
	public MyDFMain(){
	}
	
	public void build(String datapath,String forestPath){
		
		this.datapath = datapath;
		this.modelpath = forestPath;
		
		try {
			Step1();
			Step2();
		
		}  catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void predict(String testfile,String resultFile,String modelpath){
		try {
			this.modelpath = modelpath;
			
			if(datainfo == null){
				if(datapath == null){
					datapath = testfile;
				}
				Step1();
			}
			
			Step3(testfile,resultFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    /**
     * 生成描述文件
     * @throws IOException
     * @throws DescriptorException
     */
    public void Step1() throws IOException, DescriptorException {
        
    	datainfo = datapath.substring(0, datapath.indexOf(".")) + ".info";
    	if(new File(datainfo).exists()) return;
    	
    	List<Entry<Integer, String>> idinfos = DataCtg.getColInfo(datapath);
    	ArrayList<String> ctgs = DataCtg.computeCtgCnt(idinfos);
    	
    	String[] arg = new String[6+ctgs.size()];
    	arg[0] = "-p";
    	arg[1] = datapath;
    	arg[2] = "-f";
    	arg[3] = datainfo;
    	arg[4] = "-d";
    	for(int i=0;i<ctgs.size();i++){
    		arg[5+i] = ctgs.get(i);
    	}
    	arg[5+ctgs.size()] = "L";
    	
//        String[] arg = new String[]{
//                "-p",datapath,
//                "-f",datainfo,
//                "-d","N" ,"3", "C", "2" ,"N" ,"C" ,"4" ,"N" ,"C" ,"8" ,"N" ,"2" ,"C" ,"19" ,"N" ,"L"
//        };
    	System.out.println(ctgs);
        System.out.println(arg[Arrays.asList(arg).indexOf("-f")+1]);
//        HadoopUtil.delete(new Configuration(), new Path(arg[Arrays.asList(arg).indexOf("-f") + 1]));
        MyDescribe.main(arg);
//		return datainfo;
    }
    
    //训练
    public void Step2() throws Exception {

//		-oob -d /user/root/KDDTrain_2.arff -ds /user/root/KDDTrain_2.info -sl 5 -p -t 5 -o forest_result
        String[] args = new String[]{
        		"-d",datapath,
        		"-ds",datainfo,
        		/*"-sl","5",*/"-p","-t","5","-o",modelpath
        };
        System.out.println(args[Arrays.asList(args).indexOf("-o")+1]);
//        HadoopUtil.delete(new Configuration(), new Path(arg[Arrays.asList(arg).indexOf("-o") + 1]));
//        MyBuildForest.main(arg);
        new MyBuildForest().init(args);
    }
    
    //测试
    public void Step3(String testfile,String resultFile) throws Exception {

//      bin/hadoop jar  mahout-0.4.jar org.apache.mahout.df.mapreduce.TestForest -i  /user/root/KDDTrain_2.arff  
//      -ds  /user/root/KDDTrain_2.info -m forest_result -a -o predictions
    	
    	 String[] arg = new String[]{
         		"-i",testfile,"-ds",datainfo,
         		"-m",modelpath,"-a","-o",resultFile};
    	 MyTestForest.main(arg);
    }
    
    public static void main(String[] args) throws IOException, DescriptorException {
    	
    	String file = "datafile/KDDTrain+.txt";
    	MyDFMain tmd = new MyDFMain();
    	tmd.build(file,"F:/交往圈分析/718/forest_result");
    	tmd.predict(file,"F:/交往圈分析/718/predict","F:/交往圈分析/718/forest_result");
    }
}