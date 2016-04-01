package rpc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.UUID;
import session.Session;


public class RPCclient {
	public final static int maxPacket = 512;
	public final static int timeout = 2000;
	public final static int wq = 2;

	public static byte[] encode(String s){
		byte[] result = new byte[maxPacket];
		
		if( s == null || s.length() == 0){
			return result;
		}
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
		String callID =  UUID.randomUUID().toString();
		DatagramSocket rpcsocket = new DatagramSocket();
		rpcsocket.setSoTimeout(timeout);
		String result = ""; 
		
		byte[] outbuf = new byte[512];
		
		String out = callID + "#1#" + s.getSessionId()+ "#" + s.getVersion();
		outbuf = encode(out);
		
		for(Server server : dest){
			DatagramPacket sendpkt = new DatagramPacket(outbuf, outbuf.length,server.ip, server.port);
			rpcsocket.send(sendpkt);
		}
		
		byte[] inbuf = new byte[maxPacket];
		DatagramPacket recvpkt = new DatagramPacket(inbuf, inbuf.length);
		
		try{
			while(true){
				recvpkt.setLength(inbuf.length);
				rpcsocket.receive(recvpkt);
				if(decode(inbuf).split("#")[0].equals(callID)){
					break;
				}
			}
		}catch(SocketTimeoutException stoe){
			recvpkt = null;
		}
		result = decode(inbuf);
		rpcsocket.close();
		return result;
	}
	
	public String write(Session s, List<Server> dest) throws IOException{
		int count = 0;
		String callID =  UUID.randomUUID().toString();
		DatagramSocket rpcsocket = new DatagramSocket();
		rpcsocket.setSoTimeout(timeout);
		String result = ""; 
		
		byte[] outbuf = new byte[maxPacket];
		
		int version = s.getVersion() + 1; 
		String out = callID + "#2#" + s.getSessionId()+ "#" + version + "#" + s.getTimeout() + "#" + s.getMessage();
		
		outbuf = encode(out);
		boolean done = false;
		
		while(!done){
			for(Server server : dest){
				DatagramPacket sendpkt = new DatagramPacket(outbuf, outbuf.length,server.ip, server.port);
				rpcsocket.send(sendpkt);
			}
			
			byte[] inbuf = new byte[maxPacket];
			DatagramPacket recvpkt = new DatagramPacket(inbuf, inbuf.length);
			
			try{
			
				while(count < wq){
					recvpkt.setLength(inbuf.length);
					rpcsocket.receive(recvpkt);
					if(decode(inbuf).split("#")[0].equals(callID)){
						count++;
					}
				}
				done = true;
			}catch(SocketTimeoutException stoe){
				recvpkt = null;
			}
			result = decode(inbuf);
		}
		rpcsocket.close();
		return result;
	}
	
}

