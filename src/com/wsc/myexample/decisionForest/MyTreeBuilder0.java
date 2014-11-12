package com.wsc.myexample.decisionForest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.mahout.classifier.df.Bagging;
import org.apache.mahout.classifier.df.builder.DecisionTreeBuilder;
import org.apache.mahout.classifier.df.data.Data;
import org.apache.mahout.classifier.df.data.DataConverter;
import org.apache.mahout.classifier.df.data.Dataset;
import org.apache.mahout.classifier.df.data.Instance;
import org.apache.mahout.classifier.df.node.Node;
import org.apache.mahout.common.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class MyTreeBuilder0 {

	private static final Logger log = LoggerFactory.getLogger(MyTreeBuilder0.class);
	
	private DataConverter converter;

	private Random rng;

	/** number of trees to be built by this mapper */
	private int nbTrees;

	/** id of the first tree */
	private int firstTreeId;
	
	private String datapath;

	private Dataset dataset;

	private DecisionTreeBuilder treeBuilder;

	/** will contain all instances if this mapper's split */
//	private final List<Instance> instances = Lists.newArrayList();

	public int getFirstTreeId() {
		return firstTreeId;
	}

	public MyTreeBuilder0(Long seed, DecisionTreeBuilder treeBuilder,
			String dataPath, String datasetPath, int nbTrees) {

		Dataset ds;
		try {
			ds = MyDataset.load(datasetPath);
			
			this.dataset = ds;
			this.treeBuilder = treeBuilder;
			this.datapath = dataPath;

			configure(seed, nbTrees);//
			
//			buildInstances(dataPath);
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	protected void configure(Long seed, int numTrees) {
		converter = new DataConverter(dataset);

		// prepare random-numders generator
		log.info("seed : {}", seed);
		if (seed == null) {
			rng = RandomUtils.getRandom();
		} else {
			rng = RandomUtils.getRandom(seed);
		}

		// compute number of trees to build
		nbTrees = numTrees;

		// compute first tree id
		firstTreeId = 0;

		log.info("nbTrees : {}", nbTrees);
		log.info("firstTreeId : {}", firstTreeId);
	}

	protected List<Instance> buildInstances(File file) throws IOException,
			InterruptedException {
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		List<Instance> instances = Lists.newArrayList();
		String line;
		while((line = br.readLine())!=null && !"".equals(line)){
			if(converter.convert(line)!=null){
				instances.add(converter.convert(line));
			}
		}
		br.close();
		return instances;
	}
	
	/**
	 * 将文件分片
	 * @return
	 * @throws IOException 
	 */
	public String splitFile(String outpath,int splitNum) throws IOException{
		
		if(dataset.nbInstances()<=50000){
			splitNum = 1;
			return null;
		}else{
			splitNum = dataset.nbInstances()/50000;
		}
		
		String saveDir = datapath.substring(0, datapath.lastIndexOf("/"))+"/"+"filesplits"+new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		File d = new File(saveDir);
		if(d.exists()){
			d.delete();
		}else{
			d.mkdir();
		}
		
		BufferedWriter[] bws = new BufferedWriter[splitNum];
		for(int i=0;i<splitNum;i++){
			File i_sf = new File(saveDir+"/split-"+i);
			if(i_sf.exists()){
				i_sf.delete();
			}else{
				i_sf.createNewFile();
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(i_sf));
			bws[i] = bw;
		}
		
		BufferedReader br = new BufferedReader(new FileReader(datapath));
		String line;
		int i = 0;
		while((line = br.readLine())!=null && !"".equals(line)){
			int value = (i++)%splitNum;
			bws[value].write(line+"\r\n");
		}
		
		br.close();
		for(BufferedWriter i_bw:bws){
			i_bw.close();
		}
		return saveDir;
	}

	/**
	 * 分10次读取数据，生成10*nbTrees棵决策树
	 * @param outpath
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public MyDecisionForest buildTree(String outpath,int splitNum) throws IOException, InterruptedException {
		
		String splitfileDir = splitFile(datapath,splitNum);
		
		File[] sfiles;
		if(splitfileDir==null){
			sfiles = new File[]{new File(datapath)};
		}else{
			File file = new File(splitfileDir);
			if(!file.isDirectory()) return null;
			sfiles = file.listFiles();
		}
		
		DataOutputStream out = new DataOutputStream(new FileOutputStream(outpath));
		out.writeInt(nbTrees * splitNum);
		
		for(File i_file:sfiles){
			List<Instance> instances = buildInstances(i_file);
			
			Data data = new Data(dataset, instances);
			Bagging bagging = new Bagging(treeBuilder, data, null);

			log.info("Building {} trees", nbTrees);
			for (int treeId = 0; treeId < nbTrees; treeId++) {
				log.info("Building tree number : {}", treeId);

				Node tree = bagging.build(rng);

				if (tree != null) {
					tree.write(out);
				}
			}
		}
		out.close();
		
//		BufferedReader br = new BufferedReader(new FileReader(datapath));
//		int i_instanceCnt = dataset.nbInstances()/splitNum;
//		for(int i = 0;i<splitNum;i++){
//			
//			List<Instance> instances = buildInstances(br,i_instanceCnt);//
//			
//			Data data = new Data(dataset, instances);
//			Bagging bagging = new Bagging(treeBuilder, data);
//
//			log.info("Building {} trees", nbTrees);
//			for (int treeId = 0; treeId < nbTrees; treeId++) {
//				log.info("Building tree number : {}", treeId);
//
//				Node tree = bagging.build(rng);
//
//				if (tree != null) {
//					tree.write(out);
//				}
//			}
//		}
//		out.close();
//		br.close();
		
		return MyDecisionForest.load(outpath);
	}
}
