package it.eng.knowage.pm.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PrivacyDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2313489224466722385L;

	private PrivacyEventType eventType;
	
	private UserAgentDTO userAgent;
	
	private String description;
	
	private Long timestamp;
	
	private String module;
	
	private String request;
	
	private Map<String,String> requestMetadatas;
	
	private List<String> responses;
	
	private Outcome outcome;
	
	private SessionDTO session;
	

	
	

	public PrivacyEventType getEventType() {
		return eventType;
	}

	public void setEventType(PrivacyEventType eventType) {
		this.eventType = eventType;
	}

	public UserAgentDTO getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(UserAgentDTO userAgent) {
		this.userAgent = userAgent;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public Map<String, String> getRequestMetadatas() {
		return requestMetadatas;
	}

	public void setRequestMetadatas(Map<String, String> requestMetadatas) {
		this.requestMetadatas = requestMetadatas;
	}

	public List<String> getResponses() {
		return responses;
	}

	public void setResponses(List<String> responses) {
		this.responses = responses;
	}

	public Outcome getOutcome() {
		return outcome;
	}

	public void setOutcome(Outcome outcome) {
		this.outcome = outcome;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public SessionDTO getSession() {
		return session;
	}

	public void setSession(SessionDTO session) {
		this.session = session;
	}
	
	
}
