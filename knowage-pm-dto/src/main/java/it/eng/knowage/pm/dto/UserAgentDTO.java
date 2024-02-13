package it.eng.knowage.pm.dto;

import java.io.Serializable;

public class UserAgentDTO implements Serializable {

	private static final long serialVersionUID = -3894312530357951725L;

	private String userAgent;
	private String sourceIpAddress;
	private Boolean sourceSocketEnabled;
	private String os;

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getSourceIpAddress() {
		return sourceIpAddress;
	}

	public void setSourceIpAddress(String sourceIPAddress) {
		this.sourceIpAddress = sourceIPAddress;
	}

	public Boolean getSourceSocketEnabled() {
		return sourceSocketEnabled;
	}

	public void setSourceSocketEnabled(Boolean sourceSocketEnabled) {
		this.sourceSocketEnabled = sourceSocketEnabled;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

}
