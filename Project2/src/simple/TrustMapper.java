import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;


public class TrustMapper extends Mapper<IntWritable, Node, IntWritable, NodeOrDouble> {
    public void map(IntWritable key, Node value, Context context) throws IOException, InterruptedException {

        //Implement
    	if(value == null ){
    		return;
    	}
    	
    	NodeOrDouble nd = new NodeOrDouble(value);
    	context.write(key, nd);

    	context.getCounter(Counter.counter.all).increment(1);
    	
    	if(value.outgoingSize()==0){    	
    		double pagerank = value.getPageRank()*10000000;
		System.out.println("can you tell me what's wrong " + pagerank);
		
    		context.getCounter(Counter.counter.leftover).increment((long) pagerank);
		
    	}
   	double rank = value.getPageRank()/value.outgoingSize();
   	for(int out : value){
    		NodeOrDouble children = new NodeOrDouble(rank);
    		IntWritable k = new IntWritable(out);
    		context.write(k, children);
   	}
    }
}
