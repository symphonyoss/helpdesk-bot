package com.symphony.web;

public class Message {

	String from;
	long time;
	String message;

	public Message(long time, String from, String message) {
		super();
		this.time = time;
		this.from = from;
		this.message = message;
	}

	public String getFrom() {
		return from;
	}

	public long getTime() {
		return time;
	}

	public String getMessage() {
		return message;
	}

}
