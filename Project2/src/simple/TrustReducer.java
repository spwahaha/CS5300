import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;

public class TrustReducer extends Reducer<IntWritable, NodeOrDouble, IntWritable, Node> {
    public void reduce(IntWritable key, Iterable<NodeOrDouble> values, Context context)
        throws IOException, InterruptedException {
        //Implement
    	Node m = null;
    	double sum = 0;
    	
    	for(NodeOrDouble nd : values){
    		if(nd.isNode()){
    			m = nd.getNode();
    		}else{
    			sum += nd.getDouble();
    		}
    	} 
	
    	m.setPageRank(sum);
	
    	context.write(key, m);
    
    }
}
