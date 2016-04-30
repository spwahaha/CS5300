package gauss;
import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class GaussPageRank {
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
		int iterationNum = 10;
		double residuals = 0;
		int i = 0;
		for(i = 0; i < iterationNum; i++){
			residuals = 0;
			Configuration conf = new Configuration();
			File[] files = new File("input/").listFiles();
//			String inputPath = null;
//			if(i == 0){
//				for(File file : files){
//					inputPath = file.getPath();
//					System.out.println("Use this input file: " + inputPath);
//					break;
//				}
//			}else{
//				inputPath = "stage" + (i - 1);
//			}
//			
//			String outputPath = "stage" + i;
			
			String inputPath = null;
			if(i == 0){
				inputPath = args[0];
			}else{
				inputPath = args[1] + "/stage" + (i - 1);
			}			
			String outputPath = args[1] + "/stage" + i;
			
			Job job = new Job(conf, "Simple Page Rank");
			
			job.setJarByClass(GaussPageRank.class);
	        job.setMapperClass(GaussMapper.class);
	        job.setReducerClass(GaussReducer.class);
	        
	        job.setMapOutputKeyClass(LongWritable.class);
	        job.setMapOutputValueClass(Text.class);
	        
	        job.setOutputKeyClass(LongWritable.class);
	        job.setOutputValueClass(Text.class);
	        
	        
	        FileInputFormat.addInputPath(job, new Path(inputPath));
	        FileOutputFormat.setOutputPath(job, new Path(outputPath));
	        job.waitForCompletion(true);
	        
	        long NodeNum = job.getCounters().findCounter(Counter.counters.GLOBALNODE).getValue();
	        residuals = job.getCounters().findCounter(Counter.counters.RESIDUALS).getValue() / (double)1000000;
	        residuals /= NodeNum;
	        long loopNum = job.getCounters().findCounter(Counter.counters.LOOPNUM).getValue();
	        long blkNum = job.getCounters().findCounter(Counter.counters.BLOCKNUM).getValue();
	        System.out.println("pass " + i + ", the current residuals is " + residuals);
	        System.out.println("Total number of nodes is: " + NodeNum);
	        System.out.println("loops: " + loopNum + " blockNum " + blkNum + "Average number of loop is : " + loopNum / (double)blkNum);
            job.getCounters().findCounter(Counter.counters.GLOBALNODE).setValue(0);
            job.getCounters().findCounter(Counter.counters.RESIDUALS).setValue(0);
            job.getCounters().findCounter(Counter.counters.LOOPNUM).setValue(0);
            job.getCounters().findCounter(Counter.counters.BLOCKNUM).setValue(0);
            if(residuals < 0.001) break;
		}
		
		System.out.println("The residuals after " + i + " iterations is " + residuals);
	}
	
}
