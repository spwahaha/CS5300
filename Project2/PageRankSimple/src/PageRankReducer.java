import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobID;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapreduce.Reducer;

public class PageRankReducer extends Reducer<IntWritable, NodeOrDouble, IntWritable, Node>{
	
	long nodeNum = 3;
	
	public void configure(JobConf conf) throws IOException {
	    JobClient client = new JobClient(conf);
	    RunningJob parentJob = 
	        client.getJob(JobID.forName( conf.get("mapred.job.id") ));
	    nodeNum = parentJob.getCounters().getCounter(Counter.counters.GLOBALNODE);
	    //System.out.println("Node Number: " + nodeNum);
	}
	
	public void reduce(IntWritable key, Iterable<NodeOrDouble> values, Context context) throws IOException, InterruptedException{
		double d = 0.85;
		double oriRank = 0;
		Node outNode = null;
		double curRank = 0;
		
		for(NodeOrDouble val : values){
			if(val.isNode()){
				outNode = val.getNode();
				oriRank = outNode.pageRank;
			}else{
				curRank += val.getDouble();
			}
		}
//		long numNode = context.getCounter(Counter.counters.GLOBALNODE).getValue();
		// NPR[v] = d*NPR[v] + (1-d)/N;
//		System.out.println("curRank: " + curRank + " nodeNum: " + nodeNum + " oriRank " + oriRank);
		curRank = d * curRank + (1 - d) / nodeNum;
		
		double residual = Math.abs(oriRank - curRank) / curRank;
		context.getCounter(Counter.counters.RESIDUALS).increment((long)(residual * 1000000000));
		outNode.setPageRank(curRank);
		
//		System.out.println("reducer write :" + key);
//		System.out.println("residual: " + residual);
		context.write(key, outNode);
		
	}
}
