package it.eng.knowage.pm.dto;

import java.io.Serializable;

public class SessionDTO implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8700216998699800468L;
	private String sessionId;
	private String userId;
	private String ipAddress;
	private Long sessionStart;
	private String applicationId;
	
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public Long getSessionStart() {
		return sessionStart;
	}
	public void setSessionStart(Long sessionStart) {
		this.sessionStart = sessionStart;
	}
	public String getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	
	
}
