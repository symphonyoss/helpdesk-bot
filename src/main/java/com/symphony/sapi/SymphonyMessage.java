package com.symphony.sapi;

public class SymphonyMessage {

	private Long from;
	private long time;
	private String message;

	public SymphonyMessage(long time, long from, String message) {
		super();
		this.time = time;
		this.from = from;
		this.message = message;
	}

	public long getFrom() {
		return from;
	}

	public long getTime() {
		return time;
	}

	public String getMessage() {
		return message;
	}

}
