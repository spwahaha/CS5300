package common;

public class OutNode {
	public Integer nodeId;
	public String PR;
	public OutNode(Integer nodeId, String PR){
		this.nodeId = nodeId;
		this.PR = PR;
	}
	
	public String toString(){
		return "" + this.nodeId + " " + PR + "\n";
	}
}
