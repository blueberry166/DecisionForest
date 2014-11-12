package com.wsc.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.apache.mahout.classifier.df.DFUtils;
import org.apache.mahout.classifier.df.DecisionForest;
import org.apache.mahout.classifier.df.data.DataConverter;
import org.apache.mahout.classifier.df.data.Dataset;
import org.apache.mahout.classifier.df.data.DescriptorException;
import org.apache.mahout.classifier.df.mapreduce.BuildForest;
import org.apache.mahout.classifier.df.mapreduce.TestForest;
import org.apache.mahout.classifier.df.tools.Describe;
import org.apache.mahout.common.HadoopUtil;
import org.apache.mahout.common.RandomUtils;

/**
 * Created by ashqal on 14-3-7.
 */
public class StepDescribe {
    public StepDescribe(){
    }
    
    /**
     * 生成描述文件
     * @throws IOException
     * @throws DescriptorException
     */
    public void Step1() throws IOException, DescriptorException {

        //-p testdata/KDDTrain+.arff -f testdata/KDDTrain+.info
        // N 3 C 2 N C 4 N C 8 N 2 C 19 N L
        // -d N 3 C 2 N C 4 N C 8 N 2 C 19 N L

        String[] arg = new String[]{
                "-p","hdfs://192.168.0.106:9000/user/wsc/KDD_DF/KDDTrain+.arff"
                ,"-f","hdfs://192.168.0.106:9000/user/wsc/KDD_DF/KDDTrain+.info"
                ,"-d","N" ,"3", "C", "2" ,"N" ,"C" ,"4" ,"N" ,"C" ,"8" ,"N" ,"2" ,"C" ,"19" ,"N" ,"L"
        };
        System.out.println(arg[Arrays.asList(arg).indexOf("-f")+1]);
        HadoopUtil.delete(new Configuration(), new Path(arg[Arrays.asList(arg).indexOf("-f") + 1]));
        Describe.main(arg);
    }
    
    //训练
    public void Step2() throws Exception {

//		-oob -d /user/root/KDDTrain+.arff -ds /user/root/KDDTrain+.info -sl 5 -p -t 5 -o forest_result
        String[] arg = new String[]{
        		"-d","hdfs://192.168.0.106:9000/user/wsc/KDD_DF/KDDTrain+.arff","-ds","hdfs://192.168.0.106:9000/user/wsc/KDD_DF/KDDTrain+.info",
        		"-sl","5","-p","-t","5","-o","hdfs://192.168.0.106:9000/user/wsc/KDD_DF/forest_result/"
        };
        System.out.println(arg[Arrays.asList(arg).indexOf("-o")+1]);
        HadoopUtil.delete(new Configuration(), new Path(arg[Arrays.asList(arg).indexOf("-o") + 1]));
        BuildForest.main(arg);
    }
    
    //测试
    public void Step3() throws Exception {

//      bin/hadoop jar  mahout-0.4.jar org.apache.mahout.df.mapreduce.TestForest -i  /user/root/KDDTrain+.arff  
//      -ds  /user/root/KDDTrain+.info -m forest_result -a -o predictions
    	
    	 String[] arg = new String[]{
         		"-i","hdfs://192.168.0.106:9000/user/wsc/KDD_DF/KDDTrain+.arff","-ds","hdfs://192.168.0.106:9000/user/wsc/KDD_DF/KDDTrain+.info",
         		"-m","hdfs://192.168.0.106:9000/user/wsc/KDD_DF/forest_result","-a","-o","predictions"};
    	 TestForest.main(arg);
    }
    
    public static void readNode(FSDataInputStream dataInput) throws IOException{
    	int nodetype =  dataInput.readInt();
    	switch (nodetype) {
	        case 0:
	        	double label = dataInput.readDouble();
	        	System.out.println("节点类型：叶子节点，值："+label);
	        	break;
	        case 1:
	        	int attr = dataInput.readInt();
	            double split = dataInput.readDouble();
	            System.out.println("节点类型：连续节点，分裂属性："+attr+",分裂值:"+split);
	            readNode(dataInput);
	            readNode(dataInput);
	          break;
	        case 2:
	        	attr = dataInput.readInt();
	        	double[] values = DFUtils.readDoubleArray(dataInput);
	        	System.out.print("节点类型：离散节点，分裂属性："+attr+",分裂值:");
	        	for(double value:values){
	        		System.out.print(value+",");
	        	}
	        	System.out.println();
	            
	        	//读取孩子节点
	        	int length = dataInput.readInt();
	            for (int index = 0; index < length; index++) {
	              readNode(dataInput);
	            }
	          break;
	        default:
	          throw new IllegalStateException("This implementation is not currently supported");
    	}
    }
    
    public static void readModel() throws IOException{
    	Path f = new Path("/user/wsc/KDD_DF/forest_result/forest.seq");
//		FileSystem fs = FileSystem.get(new Configuration());			
//		FSDataInputStream dataInput = new FSDataInputStream(fs.open(f));
//	      try {
//	        	int size = dataInput.readInt();
//	        	System.out.println("决策树数量："+size);
//	            for (int i = 0; i < size; i++) {
//	            	System.out.println("--------------------------------第"+(i+1)+"棵决策树----------------------------------------");
//	            	readNode(dataInput);
//	            }
//	      } finally {
//	        Closeables.closeQuietly(dataInput);
//	      }
	      
	      DecisionForest forest = DecisionForest.load(new Configuration(), f);
	      Dataset dataset = Dataset.load(new Configuration(), new Path("hdfs://192.168.0.106:9000/user/wsc/KDD_DF/KDDTrain+.info"));
	      DataConverter converter = new DataConverter(dataset);
	      Random rng = RandomUtils.getRandom();
	      double prediction = forest.classify(dataset, rng, converter.convert("0,tcp,http,SF,300,440,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,7,7,0.00,0.00,0.00,0.00,1.00,0.00,0.00,255,255,1.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,normal"));
		  System.out.println(prediction);
    }

    public static void main(String[] args) throws IOException, DescriptorException {
        try {
        	new StepDescribe().Step1();
			new StepDescribe().Step2();
			new StepDescribe().Step3();
		} catch (Exception e) {
			e.printStackTrace();
		}
//    	readModel();
    }
}

