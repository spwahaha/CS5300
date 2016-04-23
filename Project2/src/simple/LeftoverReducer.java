import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;


public class LeftoverReducer extends Reducer<IntWritable, Node, IntWritable, Node> {
    public static double alpha = 0.85;
    public void reduce(IntWritable nid, Iterable<Node> Ns, Context context) throws IOException, InterruptedException {
        //Implement
    	
    	double m = context.getConfiguration().getDouble("leftover", 0)/10000000;
    	double G = context.getConfiguration().getDouble("size", 0);
    	
    	//double m = context.getCounter(Counter.counter.leftover).getValue()/1000000;
    	//double G = context.getCounter(Counter.counter.all).getValue();

	System.out.println("G's value" + G);
    	System.out.println("m's value" + m);
    	
	Node n = null;

    	Iterator<Node> iterator = Ns.iterator();
		
    	n = iterator.next();
    	double newPagerank = alpha / G + (1-alpha) * (m/G + n.getPageRank());
	System.out.println("oldoagerank" + n.getPageRank());    		
	n.setPageRank(newPagerank);
	System.out.println("newpagerank" + n.getPageRank());
	
    	
    	//Iterator<Node> iterator = Ns.iterator();
    	//while(iterator.hasNext()){
    	//	n = iterator.next();
    	//	double newPagerank = alpha *(1/G) + (1-alpha) * (m/G + n.getPageRank());
	//	System.out.println("oldoagerank" + n.getPageRank());
	//	System.out.println("newpagerank" + newPagerank);
    	//	n.setPageRank(newPagerank);
    	//}
    	context.write(nid,n);
    }
}
