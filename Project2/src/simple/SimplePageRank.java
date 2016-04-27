package simple;

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

public class SimplePageRank {
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
		int iterationNum = 5;
		double residuals = 0;
		int cnt = 0;
		for(cnt = 0; cnt < iterationNum; cnt++){
			residuals = 0;
			Configuration conf = new Configuration();
			File[] files = new File("input/").listFiles();
			String inputPath = null;
			if(cnt == 0){
				for(File file : files){
					inputPath = file.getPath();
					System.out.println("Use this input file: " + inputPath);
					break;
				}
			}else{
				inputPath = "stage" + (cnt - 1);
			}
			
			String outputPath = "stage" + cnt;
			
			Job job = new Job(conf, "Simple Page Rank");
			
			job.setJarByClass(SimplePageRank.class);
	        job.setMapperClass(SimpleMapper.class);
	        job.setReducerClass(SimpleReducer.class);
	        
	        job.setMapOutputKeyClass(LongWritable.class);
	        job.setMapOutputValueClass(Text.class);
	        
	        job.setOutputKeyClass(LongWritable.class);
	        job.setOutputValueClass(Text.class);
//	        
//	        job.setInputFormatClass(NodeInputFormat.class);
//	        job.setOutputFormatClass(NodeOutputFormat.class);
	        
	        FileInputFormat.addInputPath(job, new Path(inputPath));
	        FileOutputFormat.setOutputPath(job, new Path(outputPath));
	        
	        job.waitForCompletion(true);
	        
	       
	        long NodeNum = job.getCounters().findCounter(Counter.counters.GLOBALNODE).getValue();
	        residuals = job.getCounters().findCounter(Counter.counters.RESIDUALS).getValue() / (double)1000000 / NodeNum;
	        System.out.println("pass " + cnt + ", the current residuals is " + residuals);
	        System.out.println("Total number of nodes is: " + NodeNum);
            job.getCounters().findCounter(Counter.counters.GLOBALNODE).setValue(0);
            job.getCounters().findCounter(Counter.counters.RESIDUALS).setValue(0);
//            if(residuals < 0.001){
//            	break;
//            }
		}
		
		System.out.println("The residuals after " + cnt + " iterations is " + residuals);
	}
	
}
