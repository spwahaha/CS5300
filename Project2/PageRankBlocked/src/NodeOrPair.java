import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class NodeOrPair implements Writable{
	private Node n;
	private Pair p;
	private boolean is_node;
	
	
	public NodeOrPair(Node n){
		this.n = n;
		this.is_node = true;
	}
	
	public NodeOrPair(Pair p){
		this.p = p;
		this.is_node = false;
	}
	
	public boolean isNode(){
		return this.is_node;
	}
	
	public Node getNode(){
		if(isNode()){
			return this.n;
		}
		return null;
	}
	
	public Pair getPair(){
		if(isNode()){
			return null;
		}
		return this.p;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		this.is_node = in.readBoolean();
		if(this.is_node){
			n.readFields(in);
		}else{
			p.readFields(in);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		out.writeBoolean(this.is_node);
		if(this.is_node){
			n.write(out);
		}else{
			p.write(out);
		}
	}
}
