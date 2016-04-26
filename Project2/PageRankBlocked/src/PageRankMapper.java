import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;

public class PageRankMapper extends Mapper<IntWritable, Node, IntWritable, NodeOrDouble>{
	
	public void map(IntWritable key, Node value, Context context) throws IOException, InterruptedException{
		
		// emit node to maintain graph structure
		context.write(key, new NodeOrDouble(value));
		context.getCounter(Counter.counters.GLOBALNODE).increment(1);
		
		if(value.outgoing != null && value.outgoingSize() > 0){
			double emitRank = value.pageRank / value.outgoingSize();
			for(int out : value.outgoing){
				context.write(new IntWritable(out), new NodeOrDouble(emitRank));
			}
		}
		System.out.println("Page Rank Mapper : " + key);
	}

}
