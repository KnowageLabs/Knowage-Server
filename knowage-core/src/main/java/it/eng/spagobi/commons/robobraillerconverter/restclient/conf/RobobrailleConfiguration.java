package it.eng.spagobi.commons.robobraillerconverter.restclient.conf;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="robobraille-configuration")
public class RobobrailleConfiguration {
	
	private String host;
	private int port;
	private String protocol;
	private String hawkId;
	private String hawkKey;
	private String algorithm;
	
	public String getHost() {
		return host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	public String getProtocol() {
		return protocol;
	}
	
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	 @XmlElement(name="hawk-id")
	public String getHawkId() {
		return hawkId;
	}
	public void setHawkId(String id) {
		this.hawkId = id;
	}
	@XmlElement(name="hawk-key")
	public String getHawkKey() {
		return hawkKey;
	}
	public void setHawkKey(String key) {
		this.hawkKey = key;
	}
	public String getAlgorithm() {
		return algorithm;
	}
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
	
	
}
