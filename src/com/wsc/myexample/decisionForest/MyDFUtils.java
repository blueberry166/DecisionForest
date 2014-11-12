package com.wsc.myexample.decisionForest;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.mahout.classifier.df.node.Node;

/**
 * Utility class that contains various helper methods
 */
public final class MyDFUtils {
  private MyDFUtils() { }
  
  /**
   * Writes an Node[] into a DataOutput
   * @throws java.io.IOException
   */
  public static void writeArray(DataOutput out, Node[] array) throws IOException {
    out.writeInt(array.length);
    for (Node w : array) {
      w.write(out);
    }
  }
  
  /**
   * Reads a Node[] from a DataInput
   * @throws java.io.IOException
   */
  public static Node[] readNodeArray(DataInput in) throws IOException {
    int length = in.readInt();
    Node[] nodes = new Node[length];
    for (int index = 0; index < length; index++) {
      nodes[index] = Node.read(in);
    }
    
    return nodes;
  }
  
  /**
   * Writes a double[] into a DataOutput
   * @throws java.io.IOException
   */
  public static void writeArray(DataOutput out, double[] array) throws IOException {
    out.writeInt(array.length);
    for (double value : array) {
      out.writeDouble(value);
    }
  }
  
  /**
   * Reads a double[] from a DataInput
   * @throws java.io.IOException
   */
  public static double[] readDoubleArray(DataInput in) throws IOException {
    int length = in.readInt();
    double[] array = new double[length];
    for (int index = 0; index < length; index++) {
      array[index] = in.readDouble();
    }
    
    return array;
  }
  
  /**
   * Writes an int[] into a DataOutput
   * @throws java.io.IOException
   */
  public static void writeArray(DataOutput out, int[] array) throws IOException {
    out.writeInt(array.length);
    for (int value : array) {
      out.writeInt(value);
    }
  }
  
  /**
   * Reads an int[] from a DataInput
   * @throws java.io.IOException
   */
  public static int[] readIntArray(DataInput in) throws IOException {
    int length = in.readInt();
    int[] array = new int[length];
    for (int index = 0; index < length; index++) {
      array[index] = in.readInt();
    }
    
    return array;
  }
  
  /**
   * Return a list of all files in the output directory
   * @throws IOException if no file is found
   */
  public static File[] listOutputFiles(String outputPath) throws IOException {
//    Path[] outfiles = OutputUtils.listOutputFiles(fs, outputPath);
	  
	File[] outfiles = null;
	File d = new File(outputPath);
	if(d.isDirectory()){
		outfiles = d.listFiles();
	}
	  
//    if (outfiles.length == 0 || outfiles == null) {
//      throw new IOException("No output found !");
//    }
    
    return outfiles;
  }
  
  /**
   * Formats a time interval in milliseconds to a String in the form "hours:minutes:seconds:millis"
   */
  public static String elapsedTime(long milli) {
    long seconds = milli / 1000;
    milli %= 1000;
    
    long minutes = seconds / 60;
    seconds %= 60;
    
    long hours = minutes / 60;
    minutes %= 60;
    
    return hours + "h " + minutes + "m " + seconds + "s " + milli;
  }

  public static void storeWritable(String path, MyDataset writable) throws IOException {
//    FileSystem fs = path.getFileSystem(conf);
//
//    FSDataOutputStream out = fs.create(path);
	  
	  DataOutputStream out = new DataOutputStream(new FileOutputStream(path)); //  throw FileNotFoundException
	  
	    try {
	      writable.write(out);
	    } finally {
	    	out.close();
	    }
  }
}
