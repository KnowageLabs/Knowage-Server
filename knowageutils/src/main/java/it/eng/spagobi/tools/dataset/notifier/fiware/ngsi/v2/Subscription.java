package it.eng.spagobi.tools.dataset.notifier.fiware.ngsi.v2;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public final class Subscription {

	private final static String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSZ";

	private String description;
	private Subject subject;
	private Notification notification;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ISO_8601_FORMAT)
	private Date expires;
	private int throttling;

	public Subscription() {

	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	public Notification getNotification() {
		return notification;
	}

	public void setNotification(Notification notification) {
		this.notification = notification;
	}

	public Date getExpires() {
		return expires;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}

	public int getThrottling() {
		return throttling;
	}

	public void setThrottling(int throttling) {
		this.throttling = throttling;
	}

}
