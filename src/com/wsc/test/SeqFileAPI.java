package com.wsc.test;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

public class SeqFileAPI {
	
	public static void read() throws IOException{
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		Path seqFile = new Path("hdfs://192.168.0.106:9000/user/wsc/KDD_DF/forest_result/forest.seq");
		// Reader内部类用于文件的读取操作
		SequenceFile.Reader reader = new SequenceFile.Reader(fs, seqFile, conf);
		// 通过reader从文档中读取记录
		Text key = new Text();
		Text value = new Text();
		while (reader.next(key, value)) {
			System.out.println(key);
			System.out.println(value);
		}
		IOUtils.closeStream(reader);// 关闭read流
	}
	
	public static void write() throws IOException{
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		Path seqFile = new Path("hdfs://192.168.0.106:9000/user/wsc/KDD_DF/forest_result/forest.seq");
		// Reader内部类用于文件的读取操作
		SequenceFile.Reader reader = new SequenceFile.Reader(fs, seqFile, conf);
		// Writer内部类用于文件的写操作,假设Key和Value都为Text类型
		SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf, seqFile,
				Text.class, Text.class);
		// 通过writer向文档中写入记录
		writer.append(new Text("key"), new Text("value"));
		IOUtils.closeStream(writer);// 关闭write流
		// 通过reader从文档中读取记录
		Text key = new Text();
		Text value = new Text();
		while (reader.next(key, value)) {
			System.out.println(key);
			System.out.println(value);
		}
		IOUtils.closeStream(reader);// 关闭read流
	}

	public static void main(String[] args) throws IOException {
		read();
	}

}
