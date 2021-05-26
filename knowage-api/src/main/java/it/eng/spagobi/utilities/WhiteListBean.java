package it.eng.spagobi.utilities;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("WHITELIST")
public class WhiteListBean {

	@XStreamImplicit(itemFieldName = "service")
	public List<Service> service;

}
