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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.mahout.classifier.df.Bagging;
import org.apache.mahout.classifier.df.builder.DecisionTreeBuilder;
import org.apache.mahout.classifier.df.data.Data;
import org.apache.mahout.classifier.df.data.DataConverter;
import org.apache.mahout.classifier.df.data.Dataset;
import org.apache.mahout.classifier.df.data.Instance;
import org.apache.mahout.classifier.df.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class MyTreeBuilder {

	private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

	private ExecutorService treePool;

	private DataOutputStream out;

	private static final Logger log = LoggerFactory
			.getLogger(MyTreeBuilder.class);

	private DataConverter converter;

	private double progress;

	private double update;

	private int splitNum;

	// private Random rng;

	/** number of trees to be built by this mapper */
	private int nbTrees;

	/** id of the first tree */
	private int firstTreeId;

	private String datapath;

	private Dataset dataset;

	private DecisionTreeBuilder treeBuilder;
	
	//----------------7-18-------------
	private int[] importances;//属性重要程度
	private HashMap<Instance, int[]> estimateOOB;//
	private ArrayList<Bagging> bags;//
	int attrNum;
	private double error;
    
//	private Object trees;
	//----------------7-18--------------

	/** will contain all instances if this mapper's split */
	// private final List<Instance> instances = Lists.newArrayList();

	public int getFirstTreeId() {
		return firstTreeId;
	}

	public MyTreeBuilder(Long seed, DecisionTreeBuilder treeBuilder,
			String dataPath, String datasetPath, int nbTrees) {

		Dataset ds;
		try {
			ds = MyDataset.load(datasetPath);

			this.dataset = ds;
			this.treeBuilder = treeBuilder;
			this.datapath = dataPath;
			this.nbTrees = nbTrees;
			attrNum = ds.nbAttributes();
			
			converter = new DataConverter(dataset);
			firstTreeId = 0;
			
			bags = new ArrayList<Bagging>(nbTrees);
			estimateOOB = new HashMap<Instance, int[]>(ds.nbInstances());

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	protected synchronized List<Instance> buildInstances(File file)
			throws IOException, InterruptedException {

		BufferedReader br = new BufferedReader(new FileReader(file));

		List<Instance> instances = Lists.newArrayList();
		String line;
		while ((line = br.readLine()) != null && !"".equals(line)) {
			if (converter.convert(line) != null) {
				instances.add(converter.convert(line));
			}
		}
		br.close();
		return instances;
	}

	/**
	 * 将文件分片
	 * 
	 * @return
	 * @throws IOException
	 */
	public String splitFile(String outpath) throws IOException {

		if (dataset.nbInstances() <= 250000) {
			splitNum = 1;
			return null;
		} else {
			splitNum = dataset.nbInstances() / 250000;
		}

		update = 100 / ((double) splitNum);
		progress = 0;// 进度

		String saveDir = datapath.substring(0, datapath.lastIndexOf("/")) + "/"
				+ "filesplits"
				+ new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		File d = new File(saveDir);
		if (d.exists()) {
			d.delete();
		} else {
			d.mkdir();
		}

		BufferedWriter[] bws = new BufferedWriter[splitNum];
		for (int i = 0; i < splitNum; i++) {
			File i_sf = new File(saveDir + "/split-" + i);
			if (i_sf.exists()) {
				i_sf.delete();
			} else {
				i_sf.createNewFile();
			}

			BufferedWriter bw = new BufferedWriter(new FileWriter(i_sf));
			bws[i] = bw;
		}

		BufferedReader br = new BufferedReader(new FileReader(datapath));
		String line;
		int i = 0;
		while ((line = br.readLine()) != null && !"".equals(line)) {
			int value = (i++) % splitNum;
			bws[value].write(line + "\r\n");
		}

		br.close();
		for (BufferedWriter i_bw : bws) {
			i_bw.close();
		}
		return saveDir;
	}

	/**
	 * 分次读取数据，生成10*nbTrees棵决策树
	 * 
	 * @param outpath
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public MyDecisionForest buildTree(String outpath) throws IOException,
			InterruptedException {

		String splitfileDir = splitFile(datapath);

		File[] sfiles;
		if (splitfileDir == null) {
			sfiles = new File[] { new File(datapath) };
		} else {
			File file = new File(splitfileDir);
			if (!file.isDirectory())
				return null;
			sfiles = file.listFiles();
		}

		out = new DataOutputStream(new FileOutputStream(outpath));
		out.writeInt(nbTrees * splitNum);
		 
//		treePool = Executors.newFixedThreadPool(1);
		for (File i_file : sfiles) {
			
			treePool = Executors.newFixedThreadPool(5);

			List<Instance> instances = buildInstances(i_file);

			Data data = new Data(dataset, instances);
//			Bagging bagging = new Bagging(treeBuilder, data);

			log.info("Building {} trees", nbTrees);
			
			int sampleN = (int)(instances.size());
			for (int treeId = 0; treeId < 1/*nbTrees*/; treeId++) {
//				log.info("Building tree number : {}", treeId);
//
//				Node tree = bagging.build(rng);
//
//				if (tree != null) {
//					tree.write(out);
//				}
				
//				DecisionTreeBuilder treebuilder = new DecisionTreeBuilder();
//				treebuilder.setM(treeBuilder.getM());
//				treebuilder.setMinSplitNum(treeBuilder.getMinSplitNum());
//				treebuilder.setMinVarianceProportion(treeBuilder.getMinVarianceProportion());
//				treebuilder.setComplemented(treeBuilder.isComplemented());
//				
//				Bagging bagging = new Bagging(treebuilder, data);
				
				treePool.execute(new CreateTree(this,data,treeId,sampleN));
			}
			
			treePool.shutdown();
			try {
				treePool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS); // effectively
																				// infinity
			} catch (InterruptedException ignored) {
				System.out.println("interrupted exception in Random Forests");
			}
		}
		
		out.close();
		
//		CalcErrorRate();
        CalcImportances();

		return MyDecisionForest.load(outpath);
	}

	public void writeToOutputStream(Node tree) {
		try {
			synchronized (out) {
				tree.write(out);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class CreateTree implements Runnable {
		
//		Bagging bagging;
		int treeId;
		int sampleN;
		Data data;
		MyTreeBuilder myTreeBuilder;
		
		/**
		 * A default constructor
		 * @param myTreeBuilder 
		 */
		public CreateTree(MyTreeBuilder myTreeBuilder, Data data,int treeId,int sampleN) {
			this.data = data;
			this.treeId = treeId;
			this.sampleN = sampleN;
			this.myTreeBuilder = myTreeBuilder;
		}

		/**
		 * Creates the decision tree
		 */
		public void run() {
			
			Random rng = new Random();
			log.info(Thread.currentThread().getName() + "Building tree number : {}", treeId);

			try {
				DecisionTreeBuilder treebuilder = new DecisionTreeBuilder();
				treebuilder.setM(treeBuilder.getM());
				treebuilder.setMinSplitNum(treeBuilder.getMinSplitNum());
				treebuilder.setMinVarianceProportion(treeBuilder.getMinVarianceProportion());
				treebuilder.setComplemented(treeBuilder.isComplemented());
				
				Bagging bagging = new Bagging(treebuilder, data,myTreeBuilder);
				
				Node tree = bagging.build(rng,sampleN);
				if (tree != null) {
					writeToOutputStream(tree);
				}
				
				bags.add(bagging);
				progress += update;
				
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
     * Update the error map by recording a class prediction for a given data
     * record
     *
     * @param record	the data record classified
     * @param Class	the class
     */
	public void UpdateOOBEstimate(Instance record, double Class) {
		synchronized (estimateOOB) {
			try {
				if (estimateOOB.get(record) == null) {
					int[] map = new int[dataset.nblabels()];
					map[(int)(Class)]++;
					estimateOOB.put(record, map);
				} else {
					int[] map = estimateOOB.get(record);
					map[(int)(Class)]++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * This calculates the forest-wide importance levels for all attributes.
	 * 
	 */
	private void CalcImportances() {
		importances = new int[attrNum];
		for (Bagging bag : bags) {
			for (int i = 0; i < attrNum; i++)
				importances[i] += bag.getImportanceLevel(i);
		}
		for (int i = 0; i < attrNum; i++)
			importances[i] /= (nbTrees * splitNum);

//		 Datum.PrintImportanceLevels(importances);
		System.out.println("Importances of tree ");
		for (int m = 0; m < importances.length; m++) {
			System.out.println(" " + importances[m]);
		}
	}
	 /**
     * This calculates the forest-wide error rate. For each "left out" data
     * record, if the class with the maximum count is equal to its actual class,
     * then increment the number of correct. One minus the number correct over
     * the total number is the error rate.
     */
    private void CalcErrorRate() {
        double N = 0;
        int correct = 0;
        for (Instance record : estimateOOB.keySet()) {
            N++;
            int[] map = estimateOOB.get(record);
            if(map==null)continue;
            int Class = FindMaxIndex(map);
            if (Class == Integer.parseInt(dataset.getLabelString(dataset.getLabel(record)))) {
                correct++;
            }
        }
        error = 1 - correct / N;
        System.out.println("Forest error rate:" + error);
    }
    /**
     * Given an array, return the index that houses the maximum value
     *
     * @param arr	the array to be investigated
     * @return	the index of the greatest value in the array
     */
    public static int FindMaxIndex(int[] arr) {
        int index = 0;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > max) {
                max = arr[i];
                index = i;
            }
        }
        return index;
    }
}