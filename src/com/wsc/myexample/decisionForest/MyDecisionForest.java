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

package com.wsc.myexample.decisionForest;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.mahout.classifier.df.data.Data;
import org.apache.mahout.classifier.df.data.DataUtils;
import org.apache.mahout.classifier.df.data.Dataset;
import org.apache.mahout.classifier.df.data.Instance;
import org.apache.mahout.classifier.df.node.Node;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

/**
 * Represents a forest of decision trees.
 */
public class MyDecisionForest{
  
  private final List<Node> trees;
  
  private MyDecisionForest() {
    trees = Lists.newArrayList();
  }
  
  public MyDecisionForest(List<Node> trees) {
    Preconditions.checkArgument(trees != null && !trees.isEmpty(), "trees argument must not be null or empty");

    this.trees = trees;
  }
  
  List<Node> getTrees() {
    return trees;
  }

  /**
   * Classifies the data and calls callback for each classification
   */
  public void classify(Data data, double[] predictions) {
    Preconditions.checkArgument(data.size() == predictions.length, "predictions.length must be equal to data.size()");

    if (data.isEmpty()) {
      return; // nothing to classify
    }

    for (Node tree : trees) {
      for (int index = 0; index < data.size(); index++) {
        predictions[index] = tree.classify(data.get(index));
      }
    }
  }
  
  /**
   * predicts the label for the instance
   * 
   * @param rng
   *          Random number generator, used to break ties randomly
   * @return -1 if the label cannot be predicted
   */
  public double classify(Dataset dataset, Random rng, Instance instance) {
    if (dataset.isNumerical(dataset.getLabelId())) {
      double sum = 0;
      int cnt = 0;
      for (Node tree : trees) {
        double prediction = tree.classify(instance);
        if (prediction != -1) {
          sum += prediction;
          cnt++;
        }
      }
      return sum / cnt;
    } else {
      int[] predictions = new int[dataset.nblabels()];
      for (Node tree : trees) {
        double prediction = tree.classify(instance);
        if (prediction != -1) {
          predictions[(int) prediction]++;
        }
      }
      
      if (DataUtils.sum(predictions) == 0) {
        return -1; // no prediction available
      }
      
      return DataUtils.maxindex(rng, predictions);
    }
  }
  
  /**
   * @return Mean number of nodes per tree
   */
  public long meanNbNodes() {
    long sum = 0;
    
    for (Node tree : trees) {
      sum += tree.nbNodes();
    }
    
    return sum / trees.size();
  }
  
  /**
   * @return Total number of nodes in all the trees
   */
  public long nbNodes() {
    long sum = 0;
    
    for (Node tree : trees) {
      sum += tree.nbNodes();
    }
    
    return sum;
  }
  
  /**
   * @return Mean maximum depth per tree
   */
  public long meanMaxDepth() {
    long sum = 0;
    
    for (Node tree : trees) {
      sum += tree.maxDepth();
    }
    
    return sum / trees.size();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MyDecisionForest)) {
      return false;
    }
    
    MyDecisionForest rf = (MyDecisionForest) obj;
    
    return trees.size() == rf.getTrees().size() && trees.containsAll(rf.getTrees());
  }
  
  @Override
  public int hashCode() {
    return trees.hashCode();
  }

  public void write(DataOutput dataOutput) throws IOException {
    dataOutput.writeInt(trees.size());
    for (Node tree : trees) {
      tree.write(dataOutput);
    }
  }

  /**
   * Reads the trees from the input and adds them to the existing trees
   */
  public void readFields(DataInput dataInput) throws IOException {
    int size = dataInput.readInt();
    for (int i = 0; i < (size/2); i++) {
      trees.add(Node.read(dataInput));
    }
  }

  private static MyDecisionForest read(DataInput dataInput) throws IOException {
	  MyDecisionForest forest = new MyDecisionForest();
    forest.readFields(dataInput);
    return forest;
  }

  /**
   * Load the forest from a single file or a directory of files
   * @throws java.io.IOException
   */
  public static MyDecisionForest load(String forestPath) throws IOException {
//    FileSystem fs = forestPath.getFileSystem(conf);
    File[] files = MyDFUtils.listOutputFiles(forestPath);
    
    if (files == null || files.length == 0) {
      files = new File[]{new File(forestPath)};
    }

    MyDecisionForest forest = null;
    for (File file : files) {
      DataInputStream dataInput = new DataInputStream(new FileInputStream(file));
      try {
        if (forest == null) {
          forest = read(dataInput);
        } else {
          forest.readFields(dataInput);
        }
      } finally {
        Closeables.closeQuietly(dataInput);
      }
    }

    return forest;
    
  }

}
