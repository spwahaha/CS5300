package rpc;

import java.net.InetAddress;

public class Server {
	public InetAddress ip;
	public int port;
	public static final int defaultPort = 5300;
	public Server(InetAddress ip){
		this.ip = ip;
		this.port = this.defaultPort;
	}
	
	
	public Server(InetAddress ip, int port){
		this.ip = ip;
		this.port = port;
	}
	
	
	
	
}
