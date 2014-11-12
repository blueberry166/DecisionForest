package com.wsc.test;

import java.io.IOException;
import java.util.Arrays;

import org.apache.mahout.classifier.df.data.DescriptorException;

import com.wsc.myexample.decisionForest.MyBuildForest;
import com.wsc.myexample.decisionForest.MyDescribe;
import com.wsc.myexample.decisionForest.MyTestForest;

/**
 * Created by ashqal on 14-3-7.
 */
public class TestUnicomdata {
    
    /**
     * 生成描述文件
     * @throws IOException
     * @throws DescriptorException
     */
    public void Step1() throws IOException, DescriptorException {

        //-p testdata/origin_label.txt -f testdata/origin_label.info
        // N 3 C 2 N C 4 N C 8 N 2 C 19 N L
        // -d N 3 C 2 N C 4 N C 8 N 2 C 19 N L

        String[] arg = new String[]{
                "-p","/home/wsc/tmp/project0711/origin_label.txt"
                ,"-f","/home/wsc/tmp/project0711/origin_label.info"
//                ,"-d","N" ,"3", "C", "2" ,"N" ,"C" ,"4" ,"N" ,"C" ,"8" ,"N" ,"2" ,"C" ,"19" ,"N" ,"L"
                ,"-d","10" ,"N" ,"L"
        };
        System.out.println(arg[Arrays.asList(arg).indexOf("-f")+1]);
//        HadoopUtil.delete(new Configuration(), new Path(arg[Arrays.asList(arg).indexOf("-f") + 1]));
        MyDescribe.main(arg);
    }
    
    //训练
    public void Step2() throws Exception {

//		-oob -d /user/root/origin_label.txt -ds /user/root/origin_label.info -sl 5 -p -t 5 -o forest_result
        String[] arg = new String[]{
        		"-d","/home/wsc/tmp/project0711/origin_label.txt","-ds","/home/wsc/tmp/project0711/origin_label.info",
        		"-sl","5","-p","-t","5","-o","/home/wsc/tmp/project0711/forest_result/"
        };
        System.out.println(arg[Arrays.asList(arg).indexOf("-o")+1]);
//        HadoopUtil.delete(new Configuration(), new Path(arg[Arrays.asList(arg).indexOf("-o") + 1]));
        MyBuildForest.main(arg);
    }
    
    //测试
    public void Step3() throws Exception {

//      bin/hadoop jar  mahout-0.4.jar org.apache.mahout.df.mapreduce.TestForest -i  /user/root/origin_label.txt  
//      -ds  /user/root/origin_label.info -m forest_result -a -o predictions
    	
    	 String[] arg = new String[]{
         		"-i","/home/wsc/tmp/project0711/origin_label.txt","-ds","/home/wsc/tmp/project0711/origin_label.info",
         		"-m","/home/wsc/tmp/project0711/forest_result","-a","-o","/home/wsc/tmp/project0711/predictions"};
    	 MyTestForest.main(arg);
    }
    
    public static void main(String[] args) throws IOException, DescriptorException {
        try {
//        	new TestUnicomdata().Step1();
//			new TestUnicomdata().Step2();
			new TestUnicomdata().Step3();
		} catch (Exception e) {
			e.printStackTrace();
		}
//    	readModel();
    }
}

