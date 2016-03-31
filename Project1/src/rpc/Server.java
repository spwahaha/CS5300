package rpc;

import java.net.InetAddress;

public class Server {
	public InetAddress ip;
	public int port;
	
	public Server(InetAddress ip, int port){
		this.ip = ip;
		this.port = port;
	}
}
