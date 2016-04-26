import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class PageRankBlocked {
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
		int cnt = 0;
		double residuals = 0;
		while(true){
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
			
			Job job = new Job(conf, "Blocked PageRank");
			
			job.setJarByClass(PageRankBlocked.class);
			job.setMapperClass(BlockedMapper.class);
			job.setReducerClass(BlockedReducer.class);
			
			job.setMapOutputKeyClass(IntWritable.class);
			job.setMapOutputValueClass(NodeOrPair.class);
			
			job.setOutputKeyClass(IntWritable.class);
			job.setOutputValueClass(Node.class);
			
			job.setInputFormatClass(NodeInputFormat.class);
			job.setOutputFormatClass(NodeOutputFormat.class);
			
			FileInputFormat.addInputPath(job, new Path(inputPath));
			FileOutputFormat.setOutputPath(job, new Path(outputPath));
			job.waitForCompletion(true);
			long NodeNum = job.getCounters().findCounter(Counter.counters.GLOBALNODE).getValue();
			residuals = job.getCounters().findCounter(Counter.counters.RESIDUALS).getValue()/(double)1000000000 / NodeNum;
			if(residuals < 0.001) break;
			System.out.println("pass " + cnt + ", the current residuals is " + residuals);
	        System.out.println("Total number of nodes is: " + NodeNum);
            job.getCounters().findCounter(Counter.counters.GLOBALNODE).setValue(0);
            job.getCounters().findCounter(Counter.counters.RESIDUALS).setValue(0);
            cnt++;
		}
		
		System.out.println("The residuals after " + cnt + " iterations is " + residuals);
	}
}
