package session;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;


import rpc.RPCclient;
import rpc.RPCserver;
import rpc.Server;
import session.Session;


/**
 * Servlet implementation class Session
 */
@WebServlet("/Session")
public class Manager extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Session s;
	private String metadata = "0";
//	private static final int sessionAge = 60 * 10 * 10 * 1000;
	private static final int sessionAge = 60 * 10 * 10;
	private static final int cookieAge = sessionAge;
	private static final int delta = 60 * 10;
	private static final String cookieName = "CS5300PROJ1SESSION";
	private static final String accessKey = "AKIAIOO6HTOHZF5LG65Q";
	private static final String secretKey = "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";
	private static final String cookieDomain = ".bigdata.systems";
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private static final boolean onAWS = true;
	// sessionInfo  key: 
	public static ConcurrentHashMap<String, Session> sessionInfo = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Integer, Server> serverTable = new ConcurrentHashMap<>();
    public static int R = 2;
    public static int W = 3;
    public static int WQ = 3;
    public static int serverId = 0;
    public static int rebootNum = 0;
    public static int sessionCounter = 0;
    public String debugInfo = "";
    /**
     * @see HttpServlet#HttpServlet()
     */

    public Manager() {
        super();
        // TODO Auto-generated constructor stub
    }
    
	@Override
	public void init() {
		System.out.println("P1 Initialization");
		initParameter();
        updateForFiveMinutes();
        initiaServerTable();
        setServerData();
        System.out.println(serverTable);
        try {
			new RPCserver().start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    private void initParameter() {
		// TODO Auto-generated method stub
    	String path = getServletContext().getRealPath("/system_info.txt");
    	if(onAWS){
        	path = getServletContext().getRealPath("/");
        	path += "../system_info.txt";
    	}
    	
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(path));
			String line = br.readLine();
			br.close();
//			debugInfo += " file content " + line;
			System.out.println("server data:   " + line);
			String[] twoParts = line.split("__");
			
			int N = Integer.parseInt(line.split("__")[0].split(":")[1].trim());
			int F = Integer.parseInt(line.split("__")[1].split(":")[1].trim());
			Manager.R = F + 1;
			Manager.WQ = F + 1;
			Manager.W = 2 * F + 1;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
     * Get all the server info from the simpleDB
     * and store them in the serverTable
     */
    private void initiaServerTable() {
		// TODO Auto-generated method stub
    	String path = getServletContext().getRealPath("/NodesDB.txt");
    	if(onAWS){
        	path = getServletContext().getRealPath("/");
        	path += "../NodesDB.txt";
    	}
    	String data = readDB(path);
		JSONObject obj = new JSONObject(data);
		
		JSONArray ItemArr = obj.getJSONArray("Items");
		for (int i = 0; i < ItemArr.length(); i++)
		{	
			JSONArray AttrArr = ItemArr.getJSONObject(i).getJSONArray("Attributes");
			int index = 0;
			String public_ip = "";
			String private_ip = "";
			for(int j = 0; j < AttrArr.length(); j++){
		    	String name = AttrArr.getJSONObject(j).getString("Name");
		    	switch(name){
			    	case "Public_ip":
						public_ip = AttrArr.getJSONObject(j).getString("Value");
						System.out.println("P1Public_ip:  " + public_ip);
						break;
					case "Private_ip":
						private_ip = AttrArr.getJSONObject(j).getString("Value");
						System.out.println("private_ip:  " + private_ip);
						break;
					case "Index":
						index = Integer.parseInt(AttrArr.getJSONObject(j).getString("Value"));
						System.out.println("index:  " + index);
					default:
						break;
		    	}
		    }
			serverTable.put(index, new Server(public_ip, private_ip));
		}	
	}
	private static String readDB(String path) {
		// TODO Auto-generated method stub
		String res = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = br.readLine();
			while(line != null){
				res += line;
				line = br.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return res;
	}
    
    public void setServerData(){
    	// use the following path when export the war file
    	String path = getServletContext().getRealPath("/server_data.txt");
    	if(onAWS){
        	path = getServletContext().getRealPath("/");
        	path += "../server_data.txt";
    	}
    	System.out.println(path);
    	debugInfo += path;
    	
    	try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = br.readLine();
			br.close();
//			debugInfo += " file content " + line;
			System.out.println("server data:   " + line);
			String index = line.split("__")[0].split(":")[1].trim();
			String reboot = line.split("__")[1].split(":")[1].trim();
			this.rebootNum = Integer.parseInt(reboot);
			this.serverId = Integer.parseInt(index);
			
			// update the reboot number in java
//			System.out.println(path);
//			int updatedRebootNum = this.rebootNum + 1;
//			String outInfo = "ami-launch-index: " + this.serverId + " TT " + " RebootNum: " + updatedRebootNum;
//			File myFoo = new File(path);
//			FileWriter fooWriter = new FileWriter(myFoo, false); // false to overwrite
//			fooWriter.write(outInfo);
//			fooWriter.close();
//			System.out.println(outInfo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
    

	// new thread to automatically check the session timeout
	public void updateForFiveMinutes() {
		final Runnable up = new Runnable() {
			public void run() { 
//				System.out.println("Collection executed!!");
				Date date = new Date();
				Timestamp now = new Timestamp(date.getTime());
				Iterator<String> keys = sessionInfo.keySet().iterator();
				while(keys.hasNext()){
					String key = keys.next();
					if(now.after(sessionInfo.get(key).getTimeout())){
						sessionInfo.remove(key);
					}
				}
			}
		};
		
		// every five minutes will check the timeout
		scheduler.scheduleAtFixedRate(up, 5, 5, TimeUnit.SECONDS);
		
	}
    
   
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Cookie[] cookies = request.getCookies();
		System.out.println("do get");
		//check it is new user
		boolean newbee = true;
		int readable = 0;
		if(cookies != null){
			for(Cookie cookie : cookies){
//				if(cookie.getName().equals(cookieName) && cookie.getDomain() != null && cookie.getDomain().equals(cookieDomain)){
				if(cookie.getName().equals(cookieName)){
					System.out.println(cookie.getValue());
					String[] infos = cookie.getValue().split("__");
					String sId = cookie.getValue().split("__")[0];
					int sVersion = Integer.parseInt(cookie.getValue().split("__")[1]);
					String id_ver = sId + "_" + sVersion;
					/**
					 * here we need to parse the cookie value and get the session info
					 */
					 ArrayList<Server> serverList= new ArrayList<Server>();
					 for(int i = 2; i < infos.length; i++){
						 serverList.add(serverTable.get(Integer.parseInt(infos[i])));
						 
//					 		serverList.add(infos[i]);
					 }
					 
					 Collections.shuffle(serverList);
					 ArrayList<Server> selectedServer = new ArrayList<Server>();
					 for(int i = 0; i < R; i++){
						 selectedServer.add(serverList.get(i));
					 }
					 
					 System.out.println("RPC read start");
					 String fdbk = "";
					 int counter = 0;
					 do{
						 counter++;
						 fdbk = RPCclient.read(new Session(sId, sVersion), serverList);
					 }while(fdbk.split("#").length < 1 && counter < 3);
					 if(counter >= 3|| fdbk.split("#").length < 2){
						readable = 1;
						break;
					 }
					 System.out.print("RPC read end with info: " + fdbk);
					 if(fdbk.split("#")[1].equals("false")){
						 readable = 2;
						 break;
					 }
					 
//					 String success = fdbk.split("#")[1];
//					 if(success.equals("false")){
//						 break;
//					 }
					 
					 String msg = fdbk.split("#")[2];
					 s = new Session(sId, sVersion + 1, msg, sessionAge + delta);
					 newbee = false;
				}
			}
		}
		
		// This is a new user, the server will generate a new session
		
		if(newbee != false){
			String sessionID = getSessionID();
			int version = 1;
			s = new Session(sessionID, version, "Hello world", sessionAge + delta); 
			// sessionInfo 
			String id_ver = s.getSessionId() + "_" + s.getVersion();
	    }
		

		Map<String, String[]> map = request.getParameterMap();
		
		if(map.containsKey("Replace")){
			s.setMessage(request.getParameter("Replace"));
	    }
		
// 		we can put the RPC write code here, 
//		Set serverSet = RPCwrite(s);
		Set<Server> serverSet = getWriteServer(W);
		metadata = RPCclient.write(s, serverSet);
		//metadata = "0";
		System.out.println("write Result:  " + metadata);
		// and then put the serverSet info in the cookie info
		
		//check what button the user has pressed
		if(map.containsKey("refresh")){
			
	    	update(request,response,s);
	    }else if(map.containsKey("logout")){
	    	
	    	logout(request,response,s);
	    }else if(map.containsKey("Replace")){
	    	
	    	replace(request,response,s);
	    }else{
	    	PrintWriter out = response.getWriter();
	    	String output = readFile(s); 
	    
	    	Cookie sessionCookie = new Cookie(cookieName, s.getSessionId() + "__" + s.getVersion() + "__" + metadata);
	    	
	    	//show we cannot find the session
	    	if(readable == 1){
	    		output = output.replace("#cookieId#", "The session time out " + "#cookieId#");
	    	}
	    	if(readable == 2){
	    		output = output.replace("#cookieId#", "The session do not find " + "#cookieId#");
	    	}
	    	
	    	output = output.replace("#cookieId#", sessionCookie.getValue());
	    	out.println(output);
			
	    	sessionCookie.setMaxAge(cookieAge);
	    	sessionCookie.setDomain(cookieDomain);
	    	sessionCookie.setPath("/");
			response.setContentType("text/html"); 
			System.out.println("cookieValue:  " + sessionCookie.getValue());
			response.addCookie(sessionCookie);
	    }
	}
	
	private String getSessionID(){
		String sessionId = this.serverId + "-" + this.rebootNum + "-" + this.sessionCounter;
		this.sessionCounter++;
		return sessionId;
	}
	
	private Set<Server> getWriteServer(int num) {
		// TODO Auto-generated method stub
		Set<Server> serverSet = new HashSet<Server>();
		Random generator = new Random();
		Object[] values = (Object[]) serverTable.values().toArray();
		while(serverSet.size() < num){
			Server randomServer = (Server)values[generator.nextInt(values.length)];
			serverSet.add(randomServer);
		}
		
//		serverSet.add(serverTable.get(0));
		return serverSet;
	}

	//if the user press the refresh button
	protected void update(HttpServletRequest request, HttpServletResponse response, Session s) throws IOException{
	 	PrintWriter out = response.getWriter();
    	String output = readFile(s); 
    	
    	
    	Cookie sessionCookie = new Cookie(cookieName, s.getSessionId() + "__" + s.getVersion() + "__" + metadata);
    	
    	output = output.replace("#cookieId#", sessionCookie.getValue());
    	out.println(output);
    	
		sessionCookie.setMaxAge(cookieAge);
		sessionCookie.setDomain(cookieDomain);
    	sessionCookie.setPath("/");
		response.setContentType("text/html"); 
		response.addCookie(sessionCookie);		 	 
	}
	
	//if the user press the logout button
	protected void logout(HttpServletRequest request, HttpServletResponse response, Session s) throws IOException{
		String id = s.getSessionId();
		String ver = ""+s.getVersion();
		String id_ver = id + "_" + ver;
		sessionInfo.remove(id_ver);
		
		PrintWriter out = response.getWriter();
		s = null;
	
    	String output = readFile(s); 
    	
    	Cookie sessionCookie = new Cookie(cookieName, "");
    	output = output.replace("#cookieId#", sessionCookie.getValue());
    	
    	out.println(output);
		sessionCookie.setDomain(cookieDomain);
    	sessionCookie.setPath("/");
		sessionCookie.setMaxAge(0);
		response.setContentType("text/html"); 
		response.addCookie(sessionCookie);
		
	}
	
	//if the user press the replace button
	protected void replace(HttpServletRequest request, HttpServletResponse response, Session s) throws IOException{
		s.setMessage(request.getParameter("Replace"));
		String id = s.getSessionId();
		String ver = "" + s.getVersion();
		String id_ver = id + "_" + ver;
//		sessionInfo.get(id_ver).setMessage(s.getMessage());
		
		String output = readFile(s); 
    	
		Cookie sessionCookie = new Cookie(cookieName, s.getSessionId() + "__" + s.getVersion() + "__" + metadata);
    	
		output = output.replace("#cookieId#", sessionCookie.getValue());
    	
    	PrintWriter out = response.getWriter();
    	out.println(output);
    	
    	sessionCookie.setMaxAge(sessionAge);
		sessionCookie.setDomain(cookieDomain);
    	sessionCookie.setPath("/");
		response.setContentType("text/html"); 
		response.addCookie(sessionCookie);
		
	}
	
	//read the html file
	protected String readFile(Session session) throws IOException{
		
		String output = new String();
		String path = getServletContext().getRealPath("/home.html");
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = br.readLine();
		while(line != null){
			 output += line;
		     output += System.lineSeparator();
		     line = br.readLine();
		}
		br.close();
		if(session != null){
			output = output.replace("#sessionId#", session.getSessionId());
			output = output.replace("#message#", session.getMessage());
			output = output.replace("#version#", session.getVersion()+"");
			output = output.replace("#timeout#", session.getTimeout()+"");
			output = output.replace("#begintime#", session.getBegin()+"");
		}else{
			output = output.replace("#sessionId#", "You don't have session");
			output = output.replace("#message#", "Hello world");
			output = output.replace("#version#", "");
			output = output.replace("#timeout#", "");
			output = output.replace("#begintime#", "");
		}
		String serverInfo = "Server ID: " + this.serverId + " Reboot Number:  " + this.rebootNum;
		return output + debugInfo;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
}
