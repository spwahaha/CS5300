import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class BlockedMapper extends Mapper<IntWritable, Node, IntWritable, NodeOrPair>{
	public void map(IntWritable key, Node value, Context context) throws IOException, InterruptedException{
		
		// get the blockId of the node
		int curBlkId = blockIDofNode(key.get());
		
		// emit node to maintain graph structure
		context.write(new IntWritable(curBlkId), new NodeOrPair(value));
		context.getCounter(Counter.counters.GLOBALNODE).increment(1);
		
		if(value.outgoing != null && value.outgoingSize() > 0){
			double emitRank = value.pageRank / value.outgoingSize();
			for(int out : value.outgoing){
				int outBlkId = blockIDofNode(out);
				
				if(outBlkId == curBlkId){
					// in the same block, we emit the desId and srcId
					Pair p = new Pair(out, key.get());
					context.write(new IntWritable(outBlkId), new NodeOrPair(p));
				}else{
					// not in the same block, we emit the desId and pr
					Pair p = new Pair(out, value.getPageRank() / value.outgoingSize());
					context.write(new IntWritable(outBlkId), new NodeOrPair(p));
				}
				
			}
		}
		System.out.println("Page Rank Mapper : " + key);
	}
	
	private int blockIDofNode(long nodeID){
		int[] blocks = {10328,10045,10256,10016,9817,10379,9750,9527,10379,10004,10066,10378,10054,9575,10379,10379,9822,10360,10111,10379,10379,10379,9831,10285,10060,10211,10061,10263,9782,9788,10327,10152,10361,9780,9982,10284,10307,10318,10375,9783,9905,10130,9960,9782,9796,10113,9798,9854,9918,9784,10379,10379,10199,10379,10379,10379,10379,10379,9981,9782,9781,10300,9792,9782,9782,9862,9782,9782};
		int nodes = 0;
		for(int i = 0; i < blocks.length; i++){
			nodes += blocks[i];
			if(nodeID < nodes){
				return i;
			}
		}
		return -1;
		
	}
}
