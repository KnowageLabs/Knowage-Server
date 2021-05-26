package it.eng.spagobi.utilities;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Service {

	@XStreamAsAttribute
	public String baseurl;

	@XStreamAsAttribute
	public String relativepath;

}
