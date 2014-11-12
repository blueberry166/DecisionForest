/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mahout.classifier.df;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.mahout.classifier.df.builder.TreeBuilder;
import org.apache.mahout.classifier.df.data.Data;
import org.apache.mahout.classifier.df.data.Dataset;
import org.apache.mahout.classifier.df.data.Instance;
import org.apache.mahout.classifier.df.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wsc.myexample.decisionForest.MyTreeBuilder;

/**
 * Builds a tree using bagging
 */
public class Bagging {

	private static final Logger log = LoggerFactory.getLogger(Bagging.class);

	private final TreeBuilder treeBuilder;

	private final Data data;
	
	private final boolean[] sampled;

	// -----------7-18 start----------
	private int correct;
	private int[] importances;
	private Node tree;
	private MyTreeBuilder forest;
	int attrNum;
	ArrayList<Double> predictions;

	// -----------7-18 end----------

	public Bagging(TreeBuilder treeBuilder, Data data,MyTreeBuilder forest) {
		this.treeBuilder = treeBuilder;
		this.data = data;
		this.forest = forest;
		
		sampled = new boolean[data.size()];
		this.attrNum = data.getDataset().nbAttributes();
		importances = new int[attrNum];
	}

	/**
	 * Builds one tree
	 * @throws CloneNotSupportedException 
	 */
	public Node build(Random rng, int sampleN) throws CloneNotSupportedException {
		log.debug("Bagging...");
		Arrays.fill(sampled, false);
		
		ArrayList<Instance> test = new ArrayList<Instance>();
		
		Data bag = data.bagging(rng, sampled,sampleN,test);

		log.debug("Building...");
		
		Node tree = treeBuilder.build(rng, bag);
		this.tree = tree;
		
		CalcTreeVariableImportanceAndError(test);
		return tree;
	}

	public Node build(Random rng) {
		log.debug("Bagging...");
		// Arrays.fill(sampled, false);
		Data bag = data.bagging(rng);

		log.debug("Building...");
		return treeBuilder.build(rng, bag);
	}

	/**
	 * Responsible for gauging the error rate of this tree and calculating the
	 * importance values of each attribute
	 * 
	 * @param test
	 *            The left out data matrix
	 * @throws CloneNotSupportedException 
	 */
	private void CalcTreeVariableImportanceAndError(ArrayList<Instance> test) throws CloneNotSupportedException {
		correct = CalcTreeErrorRate(test);
		CalculateClasses(test);
		
		//计算每类
		for (int i = 0; i < attrNum; i++) {
			
			ArrayList<Instance> iData = CloneData(test);
			ArrayList<Instance> data = RandomlyPermuteAttribute(iData, i);
			
			System.out.println(data.get(0).get(3)+":"+data.get(0).get(6));
			int correctAfterPermute = 0;
			for (Instance arr : data) {
				double prediction = Evaluate(arr);
				if (prediction == GetClass(arr))
					correctAfterPermute++;
			}
			importances[i] += (correct - correctAfterPermute);
		}
		System.out.println("Importances of tree ");
		for (int m = 0; m < importances.length; m++) {
			System.out.println(" " + importances[m]);
		}
	}
	
	 /**
     * Given a data record, return the Y value - take the last index
     *
     * @param record	the data record
     * @return	its y value (class)
     */
    public double GetClass(Instance record) {
    	Dataset ds = data.getDataset();
    	double k = ds.getLabel(record);
    	
//        return record[RandomForest.M];
    	return k;
    }

	/**
	 * Calculates the tree error rate, displays the error rate to console, and
	 * updates the total forest error rate
	 * 
	 * @param test
	 *            the test data matrix
	 * @return the number correct
	 */
	public int CalcTreeErrorRate(ArrayList<Instance> test) {
		int correct = 0;
		
		Dataset ds = data.getDataset();
		
		for (Instance record : test) {
			double Class = Evaluate(record);
			if(Class == -1.0)continue;
			
			forest.UpdateOOBEstimate(record, Class);
//			int k = record[record.length - 1];
			
			double k = ds.getLabel(record);
			
			// if (Evaluate(record) == GetClass(record))
			if (Class == k)
				correct++;
		}

		double err = 1 - correct / ((double) test.size());
		// System.out.print("\n");
		System.out.println("Number of correct  = " + correct + ", out of :"
				+ test.size());
		System.out.println("Test-Data error rate of tree " + "  is: "
				+ (err * 100) + " %");

		return correct;
	}

	/**
	 * This method will get the classes and will return the updates
	 * 
	 */
	public ArrayList<Double> CalculateClasses(ArrayList<Instance> test) {
		ArrayList<Double> corest = new ArrayList<Double>();
//		int k = 0;
		int korect = 0;
		
		Dataset ds = data.getDataset();
		for (Instance record : test) {
			double kls = Evaluate(record);
			corest.add(kls);
//			int k1 = record.get(record. - 1);
			double k1 = ds.getLabel(record);
			if (kls == k1)
				korect++;
		}
		predictions = corest;
		return corest;
	}

	/**
	 * This will classify a new data record by using tree recursion and testing
	 * the relevant variable at each node.
	 * 
	 * This is probably the most-used function in all of <b>GemIdent</b>. It
	 * would make sense to inline this in assembly for optimal performance.
	 * 
	 * @param record
	 *            the data record to be classified
	 * @return the class the data record was classified into
	 */
	public double Evaluate(Instance instance) {// need to write this
		
		return tree.classify(instance);

		// TreeNode evalNode = root;
		//
		// while (true) {
		// if (evalNode.isLeaf)
		// return evalNode.Class;
		// if (record[evalNode.splitAttributeM] <= evalNode.splitValue)
		// evalNode = evalNode.left;
		// else
		// evalNode = evalNode.right;
		// }
	}
	 /**
     * Takes a list of data records, and switches the mth attribute across data
     * records. This is important in order to test the importance of the
     * attribute. If the attribute is randomly permuted and the result of the
     * classification is the same, the attribute is not important to the
     * classification and vice versa.
     *
     * @see <a
     * href="http://www.stat.berkeley.edu/~breiman/RandomForests/cc_home.htm#varimp">Variable
     * Importance</a>
     * @param test	The data matrix to be permuted
     * @param m	The attribute index to be permuted
     * @return	The data matrix with the mth column randomly permuted
     */
    private ArrayList<Instance> RandomlyPermuteAttribute(ArrayList<Instance> test, int m) {
        int num = test.size() * 2;
        for (int i = 0; i < num; i++) {
            int a = (int) Math.floor(Math.random() * test.size());
            int b = (int) Math.floor(Math.random() * test.size());
            Instance arrA = test.get(a);
            Instance arrB = test.get(b);
            double temp = arrA.get(m);
            arrA.set(m, arrB.get(m));
            arrB.set(m, temp);
            
//            arrA[m] = arrB.get(m);
//            arrB[m] = temp;
        }
        return test;
    }

    /**
     * Creates a copy of the data matrix
     *
     * @param data	the data matrix to be copied
     * @return	the cloned data matrix
     * @throws CloneNotSupportedException 
     */
    private ArrayList<Instance> CloneData(ArrayList<Instance> data) throws CloneNotSupportedException {
        ArrayList<Instance> clone = new ArrayList<Instance>(data.size());
//        int M = data;
        
        for (int i = 0; i < data.size(); i++) {
            Instance arr = data.get(i);
            
//            DenseVector vector = new DenseVector(nbattrs);
            
//            for (int j = 0; j < M; j++) {
//            	vector[j] = arr[j];
//            }
            
            Instance arrClone = arr.clone();
            
            clone.add(arrClone);
        }
        return clone;
    }
    /**
     * Get the importance level of attribute m for this tree
     */
    public int getImportanceLevel(int m) {
        return importances[m];
    }
}
