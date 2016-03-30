
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import session.Session;


/**
 * Servlet implementation class Session
 */
@WebServlet("/Session")
public class index extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Session s;
	private String metadata = "1a";
	private static final int cookieAge = 60 * 10;
	private static final int sessionAge = 60 * 10 * 10;
	private static final String cookieName = "CS5300PROJ1SESSION";
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	// sessionInfo  key: 
	HashMap<String, Session> sessionInfo = new HashMap<>();
      
    /**
     * @see HttpServlet#HttpServlet()
     */

    public index() {
        super();
        
        updateForFiveMinutes();
        
        // TODO Auto-generated constructor stub
    }
    
    // new thread to automatically check the session timeout
	public void updateForFiveMinutes() {
		final Runnable up = new Runnable() {
			public void run() { 
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
		scheduler.scheduleAtFixedRate(up, 5, 5, TimeUnit.MINUTES);
	}
    
   
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Cookie[] cookies = request.getCookies();
		
		//check it is new user
		boolean newbee = true;
		if(cookies != null){
			for(Cookie cookie : cookies){
				if(cookie.getName().equals(cookieName)){
					String[] infos = cookie.getValue().split("__");
					String sId = cookie.getValue().split("__")[0];
					String sVersion = cookie.getValue().split("__")[1];
					String id_ver = sId + "_" + sVersion;
					/**
					 * here we need to parse the cookie value and get the session info
					 * ArrayList<String> serverList= new ArrayList<String>();
					 * for(int i = 2; i < infos.length; i++){
					 * 		serverList.add(infos[i]);
					 * }
					 * s = RPCRead(sId, sVersion, serverList);
					 */
					if(sessionInfo.containsKey(id_ver)){
						Date date = new Date();
						Timestamp now = new Timestamp(date.getTime());	
						s = sessionInfo.get(id_ver);
						//double check the session is timeout
						if(now.before(s.getTimeout())){
							Long time = date.getTime();
							s.setTimeout(new Timestamp(time + sessionAge));
							s.setBegin(new Timestamp(time));
							s.setVersion(sessionInfo.get(id_ver).getVersion()+1);
							
							newbee = false;
							break;
						}else{
							sessionInfo.remove(id_ver);
						}
					}
				}
			}
		}
		
		// This is a new user, the server will generate a new session
		if(newbee != false){
			String sessionID = UUID.randomUUID().toString();
			int version = 1;
			s = new Session(sessionID, version, "Hello world", sessionAge); 
			// sessionInfo 
			String id_ver = s.getSessionId() + "_" + s.getVersion();
			sessionInfo.put(id_ver, s);
	    }
		
		// we can put the RPC write code here, 
		// Set serverSet = RPCwrite(s);
		// and then put the serverSet info in the cookie info
		
		
		Map<String, String[]> map = request.getParameterMap();
		
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
	    	
	    	output = output.replace("#cookieId#", sessionCookie.getValue());
	    	out.println(output);
			
	    	sessionCookie.setMaxAge(cookieAge);
			response.setContentType("text/html"); 
			response.addCookie(sessionCookie);
	    }
	}
	
	//if the user press the refresh button
	protected void update(HttpServletRequest request, HttpServletResponse response, Session s) throws IOException{
	 	PrintWriter out = response.getWriter();
    	String output = readFile(s); 
    	
    	
    	Cookie sessionCookie = new Cookie(cookieName, s.getSessionId() + "__" + s.getVersion() + "__" + metadata);
    	
    	output = output.replace("#cookieId#", sessionCookie.getValue());
    	out.println(output);
    	
		sessionCookie.setMaxAge(cookieAge);
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
		sessionInfo.get(id_ver).setMessage(s.getMessage());
		
		String output = readFile(s); 
    	
		Cookie sessionCookie = new Cookie(cookieName, s.getSessionId() + "__" + s.getVersion() + "__" + metadata);
    	
		output = output.replace("#cookieId#", sessionCookie.getValue());
    	
    	PrintWriter out = response.getWriter();
    	out.println(output);
    	
    	sessionCookie.setMaxAge(sessionAge);
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
		return output;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
}
