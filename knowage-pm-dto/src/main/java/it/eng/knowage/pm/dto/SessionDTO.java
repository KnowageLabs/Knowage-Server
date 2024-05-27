package it.eng.knowage.pm.dto;

import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class SessionDTO implements Serializable {

	private static final long serialVersionUID = 8700216998699800468L;

	private String sessionId;
	private String userId;
	private String ipAddress;
	private Long sessionStart;
	private String applicationId;

	public String getApplicationId() {
		return applicationId;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getSessionId() {
		return sessionId;
	}

	public Long getSessionStart() {
		return sessionStart;
	}

	public String getUserId() {
		return userId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setSessionStart(Long sessionStart) {
		this.sessionStart = sessionStart;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
	

}
