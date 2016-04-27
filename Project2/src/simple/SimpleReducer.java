package simple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class SimpleReducer extends Reducer<LongWritable, Text, LongWritable, Text>{
	
	public void reduce(LongWritable key, Iterable<Text> values, Context context) 
			throws IOException, InterruptedException{
//		System.out.println("reducer:  "+key);
		double d = 0.85;
		int nodeNum = 685230;
		nodeNum = 3;
		ArrayList<String> nodeinfos = new ArrayList<String>();
		double sum = 0;
		for(Text text : values){
			StringTokenizer st = new StringTokenizer(text.toString().trim(), " ,\t");
			String syb = st.nextToken();
			if(syb.equals("node")){
				// this is a node info
				while(st.hasMoreTokens()){
					nodeinfos.add(st.nextToken());
				}
			}else{
				// this is the emit info
				sum += Double.parseDouble(st.nextToken());
			}
		}
		sum = sum * d + (1 - d) / nodeNum;
		
		double oriPR = Double.parseDouble(nodeinfos.get(1));
		double NPR = sum;
		double rsd = Math.abs(oriPR - NPR) / NPR;
		context.getCounter(Counter.counters.RESIDUALS).increment((long)(rsd*1000000));
		context.getCounter(Counter.counters.GLOBALNODE).increment(1);
		// update the node info
		nodeinfos.set(1, "" + sum);
		// combine the string and emit
		StringBuilder sb = new StringBuilder();
		for(int i = 1; i < nodeinfos.size(); i++){
			if(i <= 1){
				sb.append(nodeinfos.get(i) + " ");
			}else{
				sb.append(nodeinfos.get(i) + ",");
			}
		}
		context.write(key, new Text(sb.substring(0, sb.length() - 1)));
	}
}
