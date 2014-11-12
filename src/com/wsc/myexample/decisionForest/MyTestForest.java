/*
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.mahout.classifier.ClassifierResult;
import org.apache.mahout.classifier.RegressionResultAnalyzer;
import org.apache.mahout.classifier.ResultAnalyzer;
import org.apache.mahout.classifier.df.DFUtils;
import org.apache.mahout.classifier.df.data.DataConverter;
import org.apache.mahout.classifier.df.data.Dataset;
import org.apache.mahout.classifier.df.data.Instance;
import org.apache.mahout.common.CommandLineUtil;
import org.apache.mahout.common.RandomUtils;
import org.apache.mahout.common.commandline.DefaultOptionCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;

/**
 * Tool to classify a Dataset using a previously built Decision Forest
 */
public class MyTestForest{

  private static final Logger log = LoggerFactory.getLogger(MyTestForest.class);

  private String dataPath; // test data path

  private String datasetPath;

  private String modelPath; // path where the forest is stored

  private String outputPath; // path to predictions file, if null do not output the predictions

  private boolean analyze; // analyze the classification results ?

  private boolean useMapreduce; // use the mapreduce classifier ?

  public int init(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

    DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
    ArgumentBuilder abuilder = new ArgumentBuilder();
    GroupBuilder gbuilder = new GroupBuilder();

    Option inputOpt = DefaultOptionCreator.inputOption().create();

    Option datasetOpt = obuilder.withLongName("dataset").withShortName("ds").withRequired(true).withArgument(
      abuilder.withName("dataset").withMinimum(1).withMaximum(1).create()).withDescription("Dataset path")
        .create();

    Option modelOpt = obuilder.withLongName("model").withShortName("m").withRequired(true).withArgument(
        abuilder.withName("path").withMinimum(1).withMaximum(1).create()).
        withDescription("Path to the Decision Forest").create();

    Option outputOpt = DefaultOptionCreator.outputOption().create();

    Option analyzeOpt = obuilder.withLongName("analyze").withShortName("a").withRequired(false).create();

    Option mrOpt = obuilder.withLongName("mapreduce").withShortName("mr").withRequired(false).create();

    Option helpOpt = DefaultOptionCreator.helpOption();

    Group group = gbuilder.withName("Options").withOption(inputOpt).withOption(datasetOpt).withOption(modelOpt)
        .withOption(outputOpt).withOption(analyzeOpt).withOption(mrOpt).withOption(helpOpt).create();

    try {
      Parser parser = new Parser();
      parser.setGroup(group);
      CommandLine cmdLine = parser.parse(args);

      if (cmdLine.hasOption("help")) {
        CommandLineUtil.printHelp(group);
        return -1;
      }

      String dataName = cmdLine.getValue(inputOpt).toString();
      String datasetName = cmdLine.getValue(datasetOpt).toString();
      String modelName = cmdLine.getValue(modelOpt).toString();
      String outputName = cmdLine.hasOption(outputOpt) ? cmdLine.getValue(outputOpt).toString() : null;
      analyze = cmdLine.hasOption(analyzeOpt);
      useMapreduce = cmdLine.hasOption(mrOpt);

      if (log.isDebugEnabled()) {
        log.debug("inout     : {}", dataName);
        log.debug("dataset   : {}", datasetName);
        log.debug("model     : {}", modelName);
        log.debug("output    : {}", outputName);
        log.debug("analyze   : {}", analyze);
        log.debug("mapreduce : {}", useMapreduce);
      }

      dataPath =  dataName;
      datasetPath = datasetName;
      modelPath = modelName;
      if (outputName != null) {
        outputPath = outputName;
      }
    } catch (OptionException e) {
      log.warn(e.toString(), e);
      CommandLineUtil.printHelp(group);
      return -1;
    }

    testForest();

    return 0;
  }

  private void testForest() throws IOException, ClassNotFoundException, InterruptedException {

    // make sure the output file does not exist
    if (outputPath != null) {
//      outFS = outputPath.getFileSystem(getConf());
      if (new File(outputPath).exists()) {
//        throw new IllegalArgumentException("Output path already exists");
    	  new File(outputPath).delete();
      }
    }

    // make sure the decision forest exists
//    FileSystem mfs = modelPath.getFileSystem(getConf());
    if (!new File(modelPath).exists()) {
      throw new IllegalArgumentException("The forest path does not exist");
    }

    // make sure the test data exists
//    dataFS = dataPath.getFileSystem(getConf());
    if (!new File(dataPath).exists()) {
      throw new IllegalArgumentException("The Test data path does not exist");
    }

    sequential();
  }

  
  private void sequential() throws IOException {

    log.info("Loading the forest...");
    MyDecisionForest forest = MyDecisionForest.load(modelPath);

    if (forest == null) {
      log.error("No Decision Forest found!");
      return;
    }

    // load the dataset
    Dataset dataset = MyDataset.load(datasetPath);
    DataConverter converter = new DataConverter(dataset);

    log.info("Sequential classification...");
    long time = System.currentTimeMillis();

    Random rng = RandomUtils.getRandom();

//    List<double[]> resList = new ArrayList<double[]>();
    
    //----------------0711---------------
    ResultAnalyzer analyzer = new ResultAnalyzer(Arrays.asList(dataset.labels()), "unknown");
    //----------------0711---------------
    
    if (new File(dataPath).isDirectory()) {
      //the input is a directory of files
      testDirectory(outputPath, converter, forest, dataset, /*resList,*/ rng, analyzer);
    }  else {
      // the input is one single file
      testFile(dataPath, outputPath, converter, forest, dataset, /*resList,*/ rng, analyzer);
    }

    time = System.currentTimeMillis() - time;
    log.info("Classification Time: {}", DFUtils.elapsedTime(time));
    log.info("{}", analyzer);

//    if (analyze) {
//      if (dataset.isNumerical(dataset.getLabelId())) {
//        RegressionResultAnalyzer regressionAnalyzer = new RegressionResultAnalyzer();
//        double[][] results = new double[resList.size()][2];
//        regressionAnalyzer.setInstances(resList.toArray(results));
//        log.info("{}", regressionAnalyzer);
//      } else {
//        ResultAnalyzer analyzer = new ResultAnalyzer(Arrays.asList(dataset.labels()), "unknown");
//        for (double[] r : resList) {
//          analyzer.addInstance(dataset.getLabelString(r[0]),
//            new ClassifierResult(dataset.getLabelString(r[1]), 1.0));
//        }
//        log.info("{}", analyzer);
//      }
//    }
  }

  private void testDirectory(String outPath, DataConverter converter, MyDecisionForest forest,
    Dataset dataset, /*List<double[]> results,*/ Random rng,ResultAnalyzer analyzer) throws IOException {
    File[] infiles = MyDFUtils.listOutputFiles(dataPath);

    for (File file : infiles) {
      log.info("Classifying : {}", file);
//      File outfile = outPath != null ? new Path(outPath, path.getName()).suffix(".out") : null;
      testFile(file.getAbsolutePath(), outPath, converter, forest, dataset, /*results, */rng,analyzer);
    }
  }

  private void testFile(String inPath, String outPath, DataConverter converter, MyDecisionForest forest,
    Dataset dataset, /*List<double[]> results,*/ Random rng,ResultAnalyzer analyzer) throws IOException {
    // create the predictions file
    DataOutputStream ofile = null;

    if (outPath != null) {
      ofile = new DataOutputStream(new FileOutputStream(outPath));
    }

    DataInputStream input = new DataInputStream(new FileInputStream(inPath));
    try {
      Scanner scanner = new Scanner(input);
      
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.isEmpty()) {
          continue; // skip empty lines
        }

        Instance instance = converter.convert(line);
        if(instance == null) continue;
        
        double prediction = forest.classify(dataset, rng, instance);

        if (ofile != null) {
          ofile.writeChars(Double.toString(prediction)); // write the prediction
          ofile.writeChar('\n');
        }
        
//        results.add(new double[] {dataset.getLabel(instance), prediction});
        
        analyzer.addInstance(dataset.getLabelString(dataset.getLabel(instance)),new ClassifierResult(dataset.getLabelString(prediction), 1.0));
      }

      scanner.close();
    } finally {
      Closeables.closeQuietly(input);
      ofile.close();
    }
  }

  public static void main(String[] args) throws Exception {
    new MyTestForest().init(args);
  }

}