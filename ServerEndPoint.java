package com.notification;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;


@ServerEndpoint( value="/chat/{eId}", decoders = MessageDecoder.class, encoders = MessageEncoder.class )
public class ServerEndPoint {
 
    private Session session;
    private static Set<ServerEndPoint> chatEndpoints 
      = new CopyOnWriteArraySet<>();
    private static HashMap<String, String> users = new HashMap<>();
    private String username;
    

    @OnOpen
    public void onOpen(
      Session session, 
      @PathParam("eId") Integer eId) throws IOException {
        this.session = session;
        chatEndpoints.add(this);
        users.put(session.getId(), String.valueOf(eId));
        System.out.println(eId);
        Message message = new Message();
        message.setFrom(String.valueOf(eId));
        message.setTo("Server");
        message.setContent("Got Connected!!");
        
        String sessionId = getSessionIdOfUser(String.valueOf(eId));
        //retrieveMessageOf(String.valueOf(eId));
        
        try {
			broadcast(message);
		} catch (EncodeException e) {
	
			e.printStackTrace();
		}
    }
    
    @OnMessage
    public void onMessage(Session session, Message message, @PathParam("eId") Integer eId) 
      throws IOException {
    	
    	//System.out.println("eid"+eId);
    	//System.out.println("message content"+message.getContent());
    	//String from = extractFrom(message.getContent()); //extracting sender
    	String from = String.valueOf(eId);
    	System.out.println("from "+from);
    	  String to = extractTo(message.getContent()); //extracting recipient
    	  System.out.println("to "+to);
    	  //String actualMessage = extractActualMessage(message); //extracting actual message
    	  sendMessageToUser(to, from, message.getContent()); //sending this message to recipient
 
       /* message.setFrom(users.get(session.getId()));
        try {
        	System.out.println("H");
			broadcast(message);
			System.out.println("Hi");
		} catch (EncodeException e) {
			
			e.printStackTrace();
		}*/
    }

    @OnClose
    public void onClose(Session session) throws IOException {
 
        chatEndpoints.remove(this);
        Message message = new Message();
        message.setFrom(users.get(session.getId()));
        message.setContent("Disconnected!");
        try {
			broadcast(message);
		} catch (EncodeException e) {
			
			e.printStackTrace();
		}
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
    }
    
    
    private String extractTo(String msg)
    {
    	int i=msg.length()-1;
    	String to="";
    	while(i<msg.length() && msg.charAt(i) != '/')
    			to+=msg.charAt(i--);
    	return to;
    }
    
    private void sendMessageToUser(String to, String from, String actualMessage) {
    		//System.out.println("ActualMessage"+actualMessage);
    	  String toSessionId = getSessionIdOfUser(to); //getting sessionid of recipient
    	  String fromSessionId = getSessionIdOfUser(from);
    	  System.out.println("toSessionId "+toSessionId);
    	  String messageToSend = prepareMessage(to, from, actualMessage); //preparing proper format of msg
    	  String messageYouSent = prepareMessageYouSent(to, from, actualMessage);
    	  System.out.println("message to send "+messageToSend);
    	  broadcast(messageToSend, toSessionId); //sending the message to recipient
    	  broadcast(messageYouSent, fromSessionId);
    	  
    	  //LocalDateTime current = LocalDateTime.now(); 
    	  //DateTimeFormatter format =  DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");   
    		    
    	 // String time = current.format(format);   
    	 // save(time,to,messageToSend);
    	 // save(time,from,messageYouSent);
    	  }
    
    
    private void broadcast(String messageToSend, String toSessionId) {
  	  for (ServerEndPoint client : chatEndpoints) {
  	  try {
  	  synchronized (client) {
  	  //comparing the session id
  	  if (client.session.getId().equals(toSessionId)) {
  	  client.session.getBasicRemote().sendText(messageToSend); //send message to the user
  	  }
  	  }
  	  } catch (IOException e) {
  		  chatEndpoints.remove(client);
  	  try {
  	  client.session.close();
  	  } catch (IOException e1) {
  	  }
  	  String message = String.format("* %s %s",
  	  client.username, "has been disconnected.");
  	  Message msg = new Message();
  	  String mesg = "has been disconnected";
  	  msg.setContent(mesg);
  	  try {
			broadcast(msg);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (EncodeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
  	  }
  	  }
  	  }
  	  
  	  private String getSessionIdOfUser(String to) {
  		  if (users.containsValue(to)) {
  		  for (String key : users.keySet()) {
  		  if (users.get(key).equals(to)) {
  		  return key;
  		  }
  		  }
  		  }
  		  return null;
  		  }
  	 private String prepareMessage(String to, String from, String msg)
  	 {
  		 String mesg = "";
  			int i=0;
  			while(i<msg.length() && msg.charAt(i) != '/')
  				mesg+=msg.charAt(i++);
  			/*i++;
  			while(i<msg.length() && msg.charAt(i) != '/')
  			{
  				mesg+=msg.charAt(i++);
  			}*/
  			//return msg;
  		 return " from: "+from+" content: "+mesg;
  	 }
  	
  	 private String prepareMessageYouSent(String to, String from, String msg)
  	 {
  		 String mesg = "";
  			int i=0;
  			while(i<msg.length() && msg.charAt(i) != '/')
  				mesg+=msg.charAt(i++);
  			/*i++;
  			while(i<msg.length() && msg.charAt(i) != '/')
  			{
  				mesg+=msg.charAt(i++);
  			}*/
  			//return msg;
  		 return "to: "+to+" content: "+mesg;
  	 }
  	
  	 private static void broadcast(Message message) 
  		      throws IOException, EncodeException {
  		 
  		        chatEndpoints.forEach(endpoint -> {
  		            synchronized (endpoint) {
  		                try {
  		                    endpoint.session.getBasicRemote().
  		                      sendObject(message);
  		                } catch (IOException | EncodeException e) {
  		                    e.printStackTrace();
  		                }
  		            }
  		        });
  		    }
}
   
