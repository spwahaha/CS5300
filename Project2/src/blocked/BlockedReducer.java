package blocked;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class BlockedReducer extends Reducer<LongWritable, Text, LongWritable, Text>{
	long nodeNum = 685230;
	double d = 0.85;
	Hashtable<Integer, ArrayList<String>> nodeTable = new Hashtable<Integer, ArrayList<String>>();
	Hashtable<Integer, List<Integer>> blockStruct = new Hashtable<Integer, List<Integer>>();
	Hashtable<Integer, List<Double>> boundaryTable = new Hashtable<Integer, List<Double>>();
	Hashtable<Integer, Double> PRtable = new Hashtable<Integer, Double>();
	
	public void reduce(LongWritable key, Iterable<Text> values, Context context) 
			throws IOException, InterruptedException{
//		System.out.println("reducer:  "+key);
		int maxIte = 100;
		nodeTable = new Hashtable<Integer, ArrayList<String>>();
		blockStruct = new Hashtable<Integer, List<Integer>>();
		boundaryTable = new Hashtable<Integer, List<Double>>();
		PRtable = new Hashtable<Integer, Double>();
		
		for(Text text : values){
			StringTokenizer st = new StringTokenizer(text.toString().trim(), " ,\t");
			String syb = st.nextToken();
//			System.out.println(text.toString());
			if(syb.equals("node")){
				// this is a node and parse node info
				ArrayList<String> nodeInfo = new ArrayList<String>();
				while(st.hasMoreTokens()){
					nodeInfo.add(st.nextToken());
				}
				int nid = Integer.parseInt(nodeInfo.get(0));
				nodeTable.put(nid, nodeInfo);
				PRtable.put(nid, Double.parseDouble(nodeInfo.get(1)));
//				System.out.println("put into node table, nid: " + nid + "nodeInfo: " + nodeInfo);
			}else if(syb.equals("BE")){
				// in the same block case
				int desId = Integer.parseInt(st.nextToken());
				int srcId = Integer.parseInt(st.nextToken());
				List<Integer> sIds = blockStruct.get(desId);
				if(sIds == null){
					sIds = new ArrayList<Integer>();
				}
				sIds.add(srcId);
				blockStruct.put(desId, sIds);
			}else{
				// not in the same block case
				int desId = Integer.parseInt(st.nextToken());
				double emitPR = Double.parseDouble(st.nextToken());
				
				List<Double> boundaryPRs = boundaryTable.get(desId);
				if(boundaryPRs == null){
					boundaryPRs = new ArrayList<Double>();
				}
				boundaryPRs.add(emitPR);
				boundaryTable.put(desId, boundaryPRs);
			}
		}
		
		int cnt = 0;
		while(cnt < maxIte){
			double residual = BlockIterateOnce();
			cnt++;
//			System.out.println("loop num: " + cnt +" residuals: " + residual);
			if(residual < 0.001) break;
		}
		System.out.println("loop number: " + cnt);
		context.getCounter(Counter.counters.LOOPNUM).increment(cnt);
		context.getCounter(Counter.counters.BLOCKNUM).increment(1);
		double residual = 0;
		
		for(Integer nid : nodeTable.keySet()){
			List<String> nodeInfo = nodeTable.get(nid);
			double NPR = PRtable.get(nid);
			double PR = Double.parseDouble(nodeInfo.get(1));
			residual = Math.abs(PR - NPR) / NPR;
			context.getCounter(Counter.counters.RESIDUALS).increment((long)(residual * 1000000));
			if(residual > 1000){
				System.out.println("nid: " + nid + " old: " + PR + " new: " + NPR + " residual: " + residual );
			}
			nodeInfo.set(1, "" + NPR);
			
			StringBuilder sb = new StringBuilder();
			for(int i = 1; i < nodeInfo.size(); i++){
				if(i <= 1){
					sb.append(nodeInfo.get(i) + " ");
				}else{
					sb.append(nodeInfo.get(i) + ",");
				}
			}
			context.write(new LongWritable(Long.parseLong(nodeInfo.get(0))),
					new Text(sb.substring(0, sb.length() - 1)));
		}

	}
	
	
	
	private double BlockIterateOnce(){
		Hashtable<Integer, Double> OPR = new Hashtable<Integer, Double>();
		for(Integer nid : PRtable.keySet()){
			OPR.put(nid, PRtable.get(nid));
		}
		double residuals = 0;
		int cnt = 0;
		for(Integer did : blockStruct.keySet()){
			cnt++;
			List<Integer> sids = blockStruct.get(did);
			double NPR = 0;
			// for each source node within the same block
			for(Integer sid : sids){
				Integer degree = nodeTable.get(sid).size() - 2;
				NPR += OPR.get(sid) / degree;
			}
			
			List<Double> PRS = boundaryTable.get(did);
			if(PRS != null){
				for(double PR : PRS){
					NPR += PR;
				}
			}
			NPR = d * NPR + (1 - d) / nodeNum;
			residuals += Math.abs(NPR - OPR.get(did)) / NPR;
//			System.out.println("old: " + OPR.get(did) + " new: " + NPR + " residual: " + residuals );
			PRtable.put(did, NPR);
		}
		return residuals / cnt;
	}
	
	
}
