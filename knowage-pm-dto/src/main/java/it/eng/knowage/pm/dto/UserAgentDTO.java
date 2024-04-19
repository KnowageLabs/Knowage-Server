package it.eng.knowage.pm.dto;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class UserAgentDTO implements Serializable {

	private static final long serialVersionUID = -3894312530357951725L;

	private String userAgent;
	private String sourceIpAddress;
	private Boolean sourceSocketEnabled;
	private String os;

	public String getOs() {
		return os;
	}

	public String getSourceIpAddress() {
		return sourceIpAddress;
	}

	public Boolean getSourceSocketEnabled() {
		return sourceSocketEnabled;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public void setSourceIpAddress(String sourceIPAddress) {
		this.sourceIpAddress = sourceIPAddress;
	}

	public void setSourceSocketEnabled(Boolean sourceSocketEnabled) {
		this.sourceSocketEnabled = sourceSocketEnabled;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
