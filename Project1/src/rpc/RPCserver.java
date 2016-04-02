package rpc;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Timestamp;

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
			//input[4] -> expire_data input[5] -> message
			String[] inputs = RPCclient.decode(inbuf).split("#");
			
			// callID length might be greater than 1
			if(inputs[1].length() != 1) continue;
			
			String[] output = null;
			byte[] outbuf = new byte[maxPacket];
			
			int operations =  Integer.parseInt(inputs[1]);
			if( operations == 1){
				output = sessionRead(inputs);
				if(output[1].equals("true")){
					outbuf = RPCclient.encode(output[0] + "#" + output[1] + "#" + output[2]);
				}
			}else if (operations == 2){
				output = sessionWrite(inputs);
				if(output[1].equals("true")){
//					output[1] = inputs[0];
					outbuf = RPCclient.encode(output[0] + "#" + output[1]);
				}
			}
			
			DatagramPacket sendPkt = new DatagramPacket(outbuf, outbuf.length, returnaddr, returnport);
			rpcsocket.send(sendPkt);
			rpcsocket.close();
		}
		
	}
	
	//result include two string, String[0] -> flag, String[1] -> data
	public String[] sessionRead(String[] in){

		String[] result = new String[2];
		// result[0] callID, result[1] true or false, result[2] data
		result[0] = in[0];
		result[1] = "false";
		if(in.length < 4){
			return result;
		}
		String callID = in[0];
		String sessionID = in[2];
		String version = in[3];
		String key = sessionID+"_"+version;
		
		result[0] = callID;
		if(Manager.sessionInfo.containsKey(key)){
			result[1] = "true";
			result[2] = Manager.sessionInfo.get(key).getMessage();
		}else{
			result[1] = "false";
		}
		return result;
	}
	
	public String[] sessionWrite(String[] in){
		//in[0]: callID, [1]: operationFlag [2]: sessionID, [3]:versionNum [4]:timeOut [5]: message
		//String out = callID + "#2#" + s.getSessionId()+ "#" + version + "#" + s.getTimeout() + "#" + s.getMessage();

		// the result should include callID
		String[] result = new String[2];
		result[0] = in[0]; // callID
		result[1] = "false";
		if(in.length < 6){
			return result;
		}
		String sessionID = in[2];
		//different with read
		int version = Integer.parseInt(in[3]);
		Timestamp timout = new Timestamp(Long.parseLong(in[4]));
		String message = in[5];
		
		String key = sessionID+"_"+version;
		Session newSession = new Session(sessionID, version, message, sessionAge);
		newSession.setTimeout(timout);
		Manager.sessionInfo.put(key, newSession);
		result[1] = "true";
		return result;
	}
}
