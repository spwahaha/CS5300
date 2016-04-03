package rpc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import session.Session;


public class RPCclient {
	public final static int maxPacket = 512;
	public final static int timeout = 2000;
	public final static int wq = 1;

	public static byte[] encode(String s){
		byte[] result = new byte[maxPacket];
		
		if( s == null || s.length() == 0){
			return result;
		}
		// result may exceed maxPacket size 
		result = s.getBytes();
		return result;
	}
	
	public static String decode(byte[] b){
		String result = new String();
		
		if(b == null || b.length == 0){
			return result;
		}
		result = new String(b);
		return result;
	}
	
	public static String read(Session s, List<Server> dest) throws IOException{
		// no necessary to use uuid, a counter is just fine
		System.out.println("rpc read start with server info" + dest);
		String callID =  UUID.randomUUID().toString();
		DatagramSocket rpcsocket = new DatagramSocket();
		rpcsocket.setSoTimeout(timeout);
		String result = ""; 
		
		byte[] outbuf = new byte[512];
		// 1 means read operation 
		String out = callID + "#1#" + s.getSessionId()+ "#" + s.getVersion() + "#";
		System.out.println("out info:  " + out);
		outbuf = encode(out);
		
		for(Server server : dest){
			System.out.println("send request to: " + dest);
			DatagramPacket sendpkt = new DatagramPacket(outbuf, outbuf.length,server.public_ip, server.port);
			rpcsocket.send(sendpkt);
		}
		
		byte[] inbuf = new byte[maxPacket];
		DatagramPacket recvpkt = new DatagramPacket(inbuf, inbuf.length);
		
		try{
			while(true){
				recvpkt.setLength(inbuf.length);
				rpcsocket.receive(recvpkt);
				System.out.println("received one packet from server: " + decode(inbuf));
				if(decode(inbuf).split("#")[0].equals(callID)){
					System.out.println("success read from other server");
					break;
				}
			}
		}catch(SocketTimeoutException stoe){
			recvpkt = null;
		}// here, if its IOException, my retry receive 
		result = decode(inbuf);
		rpcsocket.close();
		System.out.println("read end with result:  " + result);
		return result;
	}
	
	/**
	 * This method should return a list of index of the server that save the session info
	 * @param s
	 * @param dest
	 * @return
	 * @throws IOException
	 */
	public static String write(Session s, Set<Server> dest) throws IOException{
		int count = 0;
		String callID =  UUID.randomUUID().toString();
		DatagramSocket rpcsocket = new DatagramSocket();
		rpcsocket.setSoTimeout(timeout);
		String result = ""; 
		
		byte[] outbuf = new byte[maxPacket];
		
		// here, not necessary to increase the version number, version number should 
		// be increased in manager
//		int version = s.getVersion() + 1; 
		
		int version = s.getVersion();
		String out = callID + "#2#" + s.getSessionId()+ "#" + version + "#" + s.getTimeout().getTime() + "#" + s.getMessage() + "#";
		
		outbuf = encode(out);
		boolean done = false;
		
		while(!done){
			for(Server server : dest){
				System.out.println("write dest server:  " + server);
				DatagramPacket sendpkt = new DatagramPacket(outbuf, outbuf.length,server.public_ip, server.port);
				rpcsocket.send(sendpkt);
			}
			System.out.println("retrying");
			byte[] inbuf = new byte[maxPacket];
			DatagramPacket recvpkt = new DatagramPacket(inbuf, inbuf.length);
			try{
				while(count < wq){
					recvpkt.setLength(inbuf.length);
					rpcsocket.receive(recvpkt);
					// do we need to check whether write is 
					System.out.println("inbuf info:  " + decode(inbuf));
					if(decode(inbuf).split("#")[0].equals(callID)){
						count++;
						result += "__" + decode(inbuf).split("#")[2];
					}
					if(count >= wq){
						done = true;
					}
				}
//				done = true;
			}catch(SocketTimeoutException stoe){
				recvpkt = null;
			}
//			result = decode(inbuf);
		}
		rpcsocket.close();
//		return result;
		
		return result.substring(2);
	}
	
}

