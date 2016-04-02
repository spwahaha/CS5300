package rpc;

import java.net.InetAddress;

public class Server {

	/**
	 * because each server can have a public ip and a local ip
	 * we store both of them, defalut ip is the public ip
	 */
	public InetAddress ip; 
	public InetAddress private_ip;
	public int port;
	public static final int defaultPort = 5300;
	public Server(InetAddress ip){
		this.ip = ip;
		this.port = this.defaultPort;
	}
	
	public Server(InetAddress ip, InetAddress private_ip, int port){
		this.ip = ip;
		this.private_ip = private_ip;
		this.port = port;
	}
	
	
	public Server(InetAddress ip, int port){
		this.ip = ip;
		this.port = port;
	}
	
	
	
	
}
