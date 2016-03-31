package rpc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class RPCserver {
	public final static int portPROJ1BRPC = 5300;
	public final static int maxPacket = 512;
	
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
			String[] inputs = RPCclient.decode(inbuf).split("#");
			
			if(inputs[1].length() != 1) continue;
			byte[] outbuf = null;
			int operations =  Integer.parseInt(inputs[1]);
			if( operations == 1){
				outbuf = sessionRead(recvpkt.getData(), recvpkt.getLength());
			}else{
				
			}
			
			DatagramPacket sendPkt = new DatagramPacket(outbuf, outbuf.length,returnaddr, returnport);
			rpcsocket.send(sendPkt);
			
		}
		
	}
	public byte[] sessionRead(byte[] outbuf, int length){
		byte[] result = new byte[maxPacket];
		
		
		return result;
	}
	
}
