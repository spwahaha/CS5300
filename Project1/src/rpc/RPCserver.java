package rpc;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import session.Manager;
import session.Session;

public class RPCserver {
	public final static int portPROJ1BRPC = 5300;
	public final static int maxPacket = 512;
	private static final int sessionAge = 60 * 10 * 10;
	
	public RPCserver() throws IOException{
		init();
	}
	
	
	
	public void init() throws IOException{
		DatagramSocket rpcsocket = new DatagramSocket(portPROJ1BRPC);
		while(true){
			byte[] inbuf = new byte[maxPacket];
			DatagramPacket recvpkt = new DatagramPacket(inbuf, inbuf.length);
			rpcsocket.receive(recvpkt);
			InetAddress returnaddr = recvpkt.getAddress();
			int returnport = recvpkt.getPort();
			
			//input[0] -> callID, input[1] -> operationcode input[2] -> sessionID input[3] -> version
			//input[4] -> write data
			String[] inputs = RPCclient.decode(inbuf).split("#");
			
			if(inputs[1].length() != 1) continue;
			
			String[] output = null;
			byte[] outbuf = null;
			
			int operations =  Integer.parseInt(inputs[1]);
			if( operations == 1){
				output = sessionRead(inputs);
				if(output[0] == "true"){
					outbuf = RPCclient.encode(output[1]);
				}
			}else if (operations == 0){
				output = sessionWrite(inputs);
				if(output[0] == "true"){
					outbuf = RPCclient.encode(output[1]);
				}
			}
			DatagramPacket sendPkt = new DatagramPacket(outbuf, outbuf.length, returnaddr, returnport);
			rpcsocket.send(sendPkt);
			
		}
		
	}
	
	//result include two string, String[0] -> flag, String[1] -> data
	public String[] sessionRead(String[] in){
		String[] result = new String[2];
		String callID = in[0];
		String sessionID = in[2];
		String version = in[3];
		String key = sessionID+"_"+version;
		
		result[1] = callID;
		if(Manager.sessionInfo.containsKey(key)){
			result[0] = "true";
			result[1] = result[1] + "#" + Manager.sessionInfo.get(key).getMessage();
		}else{
			result[0] = "false";
		}
		return result;
	}
	
	public String[] sessionWrite(String[] in){
		String[] result = new String[2];
		String callID = in[0];
		String sessionID = in[2];
		
		//different with read
		int version = Integer.parseInt(in[3]);
		String message = in[4];
		String key = sessionID+"_"+version;
		
		result[1] = callID;
		if(Manager.sessionInfo.containsKey(key)){
			version++;
			key = sessionID + "_" + version;
			Manager.sessionInfo.put(key, new Session(sessionID, version, message,sessionAge));
			result[0] = "true";
		}else{
			result[0] = "false";
		}
		return result;
	}
	
	
	
}



