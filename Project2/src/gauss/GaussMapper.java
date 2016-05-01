package gauss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class GaussMapper extends Mapper<LongWritable, Text, LongWritable, Text>{
	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
		StringTokenizer st = new StringTokenizer(value.toString(), " ,\t");
		int sid = Integer.parseInt(st.nextToken());
		double PR = Double.parseDouble(st.nextToken());
		List<Integer> outs = new ArrayList<Integer>();
		while(st.hasMoreTokens()){
			outs.add(Integer.parseInt(st.nextToken()));
		}
		int srcBlkId = blockIDofNode(sid);
		// emit the node itself
		double emitPR = PR / outs.size();
		for(int out : outs){
			int desBlkId = blockIDofNode(out);
			if(srcBlkId == desBlkId){
				// in the same block, so BE case
				context.write(new LongWritable(desBlkId), new Text("BE " + out + " " + sid));
			}else{
				// not in the same block, so BC case
				context.write(new LongWritable(desBlkId), new Text("BC " + out + " " + emitPR));
			}
		}
		context.write(new LongWritable(srcBlkId), new Text("node " + value.toString()));
		context.getCounter(Counter.counters.GLOBALNODE).increment(1);
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
