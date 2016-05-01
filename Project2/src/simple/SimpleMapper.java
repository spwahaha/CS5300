package simple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class SimpleMapper extends Mapper<LongWritable, Text, LongWritable, Text>{
	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException{
		StringTokenizer st = new StringTokenizer(value.toString(), " ,\t");
		int sid = Integer.parseInt(st.nextToken());
		double PR = Double.parseDouble(st.nextToken());
		List<Integer> outs = new ArrayList<Integer>();
		while(st.hasMoreTokens()){
			outs.add(Integer.parseInt(st.nextToken()));
		}
		// emit the node itself
		double emitPR = PR / outs.size();
		for(int out : outs){
			context.write(new LongWritable(out), new Text("PR " + emitPR));
		}
		context.write(new LongWritable(sid), new Text("node " + value.toString()));
//		System.out.println("node " + value.toString());

	}
}

