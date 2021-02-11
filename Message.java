package com.notification;

public class Message {
	
	private String from;
    private String to;
    private String content;
    
    
    
    
	public Message() {
		super();
		// TODO Auto-generated constructor stub
	}


	public Message(String content) {
		super();
		this.content = content;
	}


	public Message(String from, String to) {
		super();
		this.from = from;
		this.to = to;
	}


	public Message(String from, String to, String content) {
		super();
		this.from = from;
		this.to = to;
		this.content = content;
	}
	
	
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}



}
