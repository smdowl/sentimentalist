package com.whereismydot;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;


public class App{
    
	public static void main( String[] args ) throws IOException{
    
        JobConf job = new JobConf(FeatureExtractionPipeline.class);
        job.setMapperClass(FeatureExtractionPipeline.class);
        job.setReducerClass(FeatureExtractionPipeline.class);
        
        job.setInputFormat(TextInputFormat.class);
        job.setOutputFormat(TextOutputFormat.class);
        
        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        
        System.out.println("mapreduce:" + job.getInt("mapreduce.task.timeout",-1) 
        						+ " mapred" + job.getInt("mapred.task.timeout",-1));
        
        job.setInt("mapreduce.task.timeout", 40 * 60 * 1000);
        job.setInt("mapred.task.timeout"   , 40 * 60 * 1000);
        
        System.out.println("mapreduce:" + job.getInt("mapreduce.task.timeout",-1) 
				+ " mapred" + job.getInt("mapred.task.timeout",-1));
        
        JobClient.runJob(job);
    }
}
