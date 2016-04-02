package session;

import java.sql.Timestamp;
import java.util.Date;


public class Session {
	String sessionId;
	int version;
	String message;
	Timestamp begintime;
	Timestamp timeout; 

	public Session(String sessionId, int version, String message, long time){
		Date date= new Date();
		this.sessionId = sessionId;
		this.version = version;
		this.message = message;
		long begin = date.getTime();
		begintime = new Timestamp(begin);
		timeout = new Timestamp(begin + time);
	}
	
	public Session(String sessionId, int version){
		this.sessionId = sessionId;
		this.version = version;
	}
	
	public Session(Session session){
		this.sessionId = session.sessionId;
		this.version = session.version;
		this.message = session.message;
		this.begintime = new Timestamp(session.begintime.getTime());
		this.timeout = new Timestamp(session.timeout.getTime());
	}
	
	public void setSessionId(String s){
		sessionId = s;
	}
	
	public void setMessage(String m){
		message = m;
	}
	
	public void setVersion(int v){
		version = v;
	}
	
	public void setBegin(Timestamp t){
		begintime = t;
	}
	
	public void setTimeout(Timestamp t){
		timeout = t;
	}
	
	public String getSessionId(){
		return sessionId;
	}
	
	public int getVersion(){
		return version;
	}
	
	public String getMessage(){
		return message;
	}
	
	public Timestamp getTimeout(){
		return timeout;
	}
	
	public Timestamp getBegin(){
		return begintime;
	}
}
