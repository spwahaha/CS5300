package rpc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import session.Manager;
import session.Session;


public class RPCclient {
	public final static int maxPacket = 512;
	public final static int timeout = 2000;
	public final static int wq = 2;

	/**
	 * encode the string into byte array
	 * @param s original string 
	 * @return encoded byte array
	 */
	public static byte[] encode(String s){
		byte[] result = new byte[maxPacket];
		
		if( s == null || s.length() == 0){
			return result;
		}
		result = s.getBytes();
		return result;
	}
	
	/**
	 * Decode the byte array into original string
	 * @param b byte array to be decoded
	 * @return the original string
	 */
	public static String decode(byte[] b){
		String result = new String();
		
		if(b == null || b.length == 0){
			return result;
		}
		result = new String(b);
		return result;
	}
	
	/**
	 * RPC read, read session info from dest servers
	 * @param s the session need to read, use s.id and s.versionNum to read
	 * @param dest the destination servers to read session info
	 * @return empty byte array if read failed
	 * or date with the following format: callerID#true/false#session Msg# useless info
	 * @throws IOException
	 */
	public static String read(Session s, List<Server> dest) throws IOException{
		// no necessary to use uuid, a counter is just fine
		System.out.println("rpc read start with server info" + dest);
		String callID =  UUID.randomUUID().toString();
		DatagramSocket rpcsocket = new DatagramSocket();
		rpcsocket.setSoTimeout(timeout);
		String result = ""; 
		
		byte[] outbuf = new byte[512];
		/**
		 * encode the read request
		 * callID#1(read)#sessionID#versionNum#
		 */
		String out = callID + "#1#" + s.getSessionId()+ "#" + s.getVersion() + "#";
//		System.out.println("out info:  " + out);
		outbuf = encode(out);
		
		/**
		 * send read request to RPC servers using server private ip
		 */
		for(Server server : dest){
			System.out.println("send request to: " + dest);
			DatagramPacket sendpkt = new DatagramPacket(outbuf, outbuf.length,server.private_ip, server.port);
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
		}
		/**
		 * decode the read information to string and return result data
		 */
		result = decode(inbuf);
		rpcsocket.close();
//		System.out.println("read end with result:  " + result);
		return result;
	}
	
	/**
	 * RPC write, write session info to W servers (WQ success)
	 * This method should return a list of index of the server that save the session info
	 * @param s session need to be written
	 * @param dest destination server to write the session info
	 * @return return the string containing server index that store the session
	 * info successfully with the following format£º 0__1__2
	 * @throws IOException
	 */
	public static String write(Session s, Set<Server> dest) throws IOException{
		int count = 0;
		String callID =  UUID.randomUUID().toString();
		DatagramSocket rpcsocket = new DatagramSocket();
		rpcsocket.setSoTimeout(timeout);
		String result = ""; 
		byte[] outbuf = new byte[maxPacket];
		
		/**
		 * encode the write request with the following format
		 * callID#2(write)#sessionID#versionNum#discardTime#sessionMSG#
		 */
		int version = s.getVersion();
		String out = callID + "#2#" + s.getSessionId()+ "#" + version + "#" + s.getTimeout().getTime() + "#" + s.getMessage() + "#";
		/**
		 * cut session message if request string is greater that 512
		 */
		if(out.length() > 512){
			int cutLen = out.length() - 512;
			int preserveLen = s.getMessage().length() - cutLen;
			s.setMessage(s.getMessage().substring(0, preserveLen));
		}
		out = callID + "#2#" + s.getSessionId()+ "#" + version + "#" + s.getTimeout().getTime() + "#" + s.getMessage() + "#";
		outbuf = encode(out);
		boolean done = false;
		
		/**
		 * send write request to RPC servers, the feedback information with 
		 * following format: callID#true#serverID#
		 */
		while(!done){
			for(Server server : dest){
				System.out.println("write dest server:  " + server);
				DatagramPacket sendpkt = new DatagramPacket(outbuf, outbuf.length,server.public_ip, server.port);
				try{
					rpcsocket.send(sendpkt);
				}catch(IOException e){
					continue;
				}
			}
			System.out.println("retrying");
			byte[] inbuf = new byte[maxPacket];
			DatagramPacket recvpkt = new DatagramPacket(inbuf, inbuf.length);
			try{
				while(count < Manager.WQ){
					recvpkt.setLength(inbuf.length);
					rpcsocket.receive(recvpkt);
					// do we need to check whether write is 
					System.out.println("inbuf info:  " + decode(inbuf));
					if(decode(inbuf).split("#")[0].equals(callID)){
						int serverIndex = Integer.parseInt(decode(inbuf).split("#")[2]);
						dest.remove(Manager.serverTable.get(serverIndex));
						count++;
						result += "__" + decode(inbuf).split("#")[2];
					}
					if(count >= Manager.WQ){
						done = true;
						break;
					}
				}
			}catch(SocketTimeoutException stoe){
				recvpkt = null;
			}
		}
		rpcsocket.close();
		
		return result.substring(2);
	}
	
}

