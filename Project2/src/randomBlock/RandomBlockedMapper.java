package randomBlock;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class RandomBlockedMapper extends Mapper<LongWritable, Text, LongWritable, Text>{
	long nodeNum = 685230;
	long blkNum = 68;
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
		return (int)((nodeID + "").hashCode() % blkNum);
//		return (int)nodeID;
	}
}
