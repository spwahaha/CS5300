package rpc;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Timestamp;

import session.Manager;
import session.Session;

public class RPCserver extends Thread{
	public final static int portPROJ1BRPC = 5300;
	public final static int maxPacket = 512;
	private static final int sessionAge = 60 * 10 * 10;
	
	public RPCserver() throws IOException{
		super("RPCserver Thread");
	}
	
	
	public void run(){
		try {
			init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * use while loop to execute RPC server
	 * @throws IOException
	 */
	public void init() throws IOException{
		DatagramSocket rpcsocket = new DatagramSocket(portPROJ1BRPC);
		while(true){
			byte[] inbuf = new byte[maxPacket];
			DatagramPacket recvpkt = new DatagramPacket(inbuf, inbuf.length);
			rpcsocket.receive(recvpkt);
			InetAddress returnaddr = recvpkt.getAddress();
			int returnport = recvpkt.getPort();
			
			/**
			 * receive request decode it into string array and 
			 * the first 4 element in array has the following format
			 * input[0] -> callID, input[1] -> operationcode 
			 * input[2] -> sessionID input[3] -> version
			 * (input[4] -> expire_data input[5] -> message) for write requst 
			 */

			System.out.println("server received data: " + RPCclient.decode(inbuf));
			String[] inputs = RPCclient.decode(inbuf).split("#");
						
			
			String[] output = null;
			byte[] outbuf = new byte[maxPacket];
			
			int operations =  Integer.parseInt(inputs[1]);
			if( operations == 1){
				/**
				 *  read request with operation code: 1
				 *  read session info, encode session info
				 *  to byte array and response
				 *  response format:
				 *  callID#true/false#sessionMSG(if true)
				 */
				System.out.println("read session");
				
				output = sessionRead(inputs);
				
				for(String str : output){
					System.out.println("read session info:  " + str);
				}
				if(output[1].equals("true")){	
					outbuf = RPCclient.encode(output[0] + "#" + output[1] + "#" + output[2] + "#" + 
								Manager.serverId + "#");
				}else{
					outbuf = RPCclient.encode(output[0] + "#" + output[1] + "#");
				}
			}else if (operations == 2){
				/**
				 * write request with operation code 2
				 * write the session info and response
				 * response format:
				 * callID#true#serverID#
				 */
				System.out.println("write session");
				output = sessionWrite(inputs);
				for(String str : output){
					System.out.println("output info:  " + str);
				}
				outbuf = RPCclient.encode(output[0] + "#" + output[1] + "#" + output[2] + "#");
			}
			
			DatagramPacket sendPkt = new DatagramPacket(outbuf, outbuf.length, returnaddr, returnport);
			rpcsocket.send(sendPkt);
		}
	}
	

	/**
	 * read session info 
	 * @param in the request info with the following format
	 * input[0] -> callID, input[1] -> operationcode, 
	 * input[2] -> sessionID input[3] -> version
	 * @return the response message with the following format
	 * result[0] callID, result[1] true or false, result[2] data
	 */
	public String[] sessionRead(String[] in){

		String[] result = new String[3];
		result[0] = in[0];
		result[1] = "false";
		if(in.length < 4){
			return result;
		}
		/**
		 * parse the request and response the correct session info
		 */
		String callID = in[0];
		String sessionID = in[2];
		String version = in[3];
		String key = sessionID+"_"+version;
		System.out.println("key :  " + key);
		System.out.println(Manager.sessionInfo);
		result[0] = callID;
		if(Manager.sessionInfo.containsKey(key)){
			System.out.println("contains session info");
			result[1] = "true";
			result[2] = Manager.sessionInfo.get(key).getMessage();
		}else{
			result[1] = "false";
		}
		return result;
	}
	
	/**
	 * write session info
	 * @param in the request info with the following format
	 * input[0] -> callID, input[1] -> operationcode, input[2] -> sessionID
	 * input[3] -> version, input[4] -> expire_data input[5] -> message
	 * @return response info with the following format
	 * result[0]:callID, result[1]:true/false, result[2]:serverID
	 */
	public String[] sessionWrite(String[] in){

		/**
		 * parse the request and response
		 */
		for(String str : in){
			System.out.println("in info:  " + str);
		}
		// the result should include callID
		String[] result = new String[3];
		result[0] = in[0]; // callID
		result[1] = "false";
		
		if(in.length < 6){
			//the input format is not correct
			return result;
		}
		String sessionID = in[2];
		int version = Integer.parseInt(in[3]);
		Timestamp timout = new Timestamp(Long.parseLong(in[4]));
		String message = in[5];
		
		String key = sessionID+"_"+version;
		Session newSession = new Session(sessionID, version, message, sessionAge);
		newSession.setTimeout(timout);
		System.out.println("put session in server");
		Manager.sessionInfo.put(key, newSession);
		result[1] = "true";
		result[2] = ""+Manager.serverId;
		return result;
	}
}
