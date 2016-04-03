package rpc;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server {

	/**
	 * because each server can have a public ip and a local ip
	 * we store both of them, defalut ip is the public ip
	 */
	public InetAddress public_ip; 
	public InetAddress private_ip;
	public int port;
	public static final int defaultPort = 5300;
	public Server(InetAddress ip){
		this.public_ip = ip;
		this.port = this.defaultPort;
	}
	
	
	public Server(String public_ip, String private_ip){
		try {
			this.private_ip = InetAddress.getByName(private_ip);
			this.public_ip = InetAddress.getByName(public_ip);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.print("unknown host");
			e.printStackTrace();
		}
		this.port = defaultPort;
	}
	
	public Server(InetAddress ip, InetAddress private_ip, int port){
		this.public_ip = ip;
		this.private_ip = private_ip;
		this.port = port;
	}
	
	
	public Server(InetAddress ip, int port){
		this.public_ip = ip;
		this.port = port;
	}
	
	
	@Override
	public String toString(){
		return "public_ip:" + this.public_ip +" private_ip:" + this.private_ip + " prot:" + this.port;
	}
	
	@Override 
	public boolean equals(Object server){
		if(!(server instanceof Server)){
			return false;
		}
		return this.port == ((Server)server).port && this.public_ip.equals(((Server)server).public_ip)
					&& this.private_ip.equals(((Server)server).private_ip);
	}
	
}
