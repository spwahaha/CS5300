import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class Pair implements Writable{
	int sId;
	int dId;
	double pageRank;
	
	
	public Pair(int did, int sid, double pr){
		this.dId = did;
		this.sId = sid;
		this.pageRank = pr;
	}
	
	public Pair(int did, int sid){
		this.sId = sid;
		this.dId = did;
	}
	
	public Pair(int did, double pr){
		this.dId = did;
		this.pageRank = pr;
		this.sId = -1;
	}
	
	public boolean isSameBlock(){
		return sId != -1;
	}
	
	public int getSid(){
		return this.sId;
	}
	
	public int getDid(){
		return this.dId;
	}
	
	public double getPageRank(){
		return this.pageRank;
	}
	

	@Override
	public void readFields(DataInput in) throws IOException {
		// TODO Auto-generated method stub
		this.dId = in.readInt();
		this.sId = in.readInt();
		this.pageRank = in.readDouble();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		// TODO Auto-generated method stub
		out.writeInt(this.dId);
		out.writeInt(this.sId);
		out.writeDouble(this.pageRank);
	}
}
