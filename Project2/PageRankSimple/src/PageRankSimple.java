import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class PageRankSimple {
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
		//int iterationNum = 5;
		double residuals = 1;
		int i = 0;
		
		while(residuals > 0.001){
			
			Configuration conf = new Configuration();
			File[] files = new File("input/").listFiles();
			String inputPath = null;
			if(i == 0){
				for(File file : files){
					inputPath = file.getPath();
					//System.out.println("Use this input file: " + inputPath);
					break;
				}
			}else{
				inputPath = "stage" + (i - 1);
			}
			
			String outputPath = "stage" + i;
			
			Job job = new Job(conf, "Simple Page Rank");
			
			
			job.setJarByClass(PageRankSimple.class);
	        job.setMapperClass(PageRankMapper.class);
	        job.setReducerClass(PageRankReducer.class);
	        
	        job.setMapOutputKeyClass(IntWritable.class);
	        job.setMapOutputValueClass(NodeOrDouble.class);
	        
	        job.setOutputKeyClass(IntWritable.class);
	        job.setOutputValueClass(Node.class);
	        
	        job.setInputFormatClass(NodeInputFormat.class);
	        job.setOutputFormatClass(NodeOutputFormat.class);
	        
	        FileInputFormat.addInputPath(job, new Path(inputPath));
	        FileOutputFormat.setOutputPath(job, new Path(outputPath));
	        job.waitForCompletion(true);
	        
	        long NodeNum = job.getCounters().findCounter(Counter.counters.GLOBALNODE).getValue();
	        residuals = job.getCounters().findCounter(Counter.counters.RESIDUALS).getValue() / (double)1000000000 / NodeNum;
	        System.out.println("pass " + i + ", the current residuals is " + residuals);
	        //System.out.println("Total number of nodes is: " + NodeNum);
            job.getCounters().findCounter(Counter.counters.GLOBALNODE).setValue(0);
            job.getCounters().findCounter(Counter.counters.RESIDUALS).setValue(0);
            i++;
		}
		System.out.print("the iteration num is   " + i);
		//System.out.println("The residuals after " + iterationNum + " iterations is " + residuals);
	}
	
}
