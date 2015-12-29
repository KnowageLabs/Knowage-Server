package it.eng.spagobi.engines.whatif.crossnavigation;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.whatif.template.WhatIfXMLTemplateParser;

import java.io.Serializable;
import java.util.List;
import java.util.TreeMap;


public class TargetClickable implements Serializable{

	/*
	 * Target clickable in mdx configuration
	 */
	private static final long serialVersionUID = 7603427843810725619L;
	private String targetDocument;
	private String target;	
	private String title;
	private String uniqueName;
	private TreeMap<String,String> parametersList;
		
	public TargetClickable() {
	}
	
	public TargetClickable(SourceBean sb) {
		targetDocument = (String) sb.getAttribute(WhatIfXMLTemplateParser.TAG_CNC_DOCUMENT);
		target = (String) sb.getAttribute(WhatIfXMLTemplateParser.TAG_CNC_TARGET);		
		title = (String) sb.getAttribute(WhatIfXMLTemplateParser.TAG_CNC_TITLE);
		uniqueName = (String) sb.getAttribute(WhatIfXMLTemplateParser.TAG_CNC_UNIQUENAME);
		List parameters = sb.getAttributeAsList(WhatIfXMLTemplateParser.TAG_CNC_PARAMETERS);		
		boolean hasParameters = parameters != null && !parameters.isEmpty();
		if (hasParameters) {
			parametersList = new TreeMap<String, String>();
			for (int i = 0; i < parameters.size(); i++) {
				SourceBean temp = (SourceBean) parameters.get(i);
				String clickParameterName = (String) temp.getAttribute(WhatIfXMLTemplateParser.TAG_CNC_NAME);
				String clickParameterValue = (String) temp.getAttribute(WhatIfXMLTemplateParser.TAG_CNC_VALUE);
				parametersList.put(clickParameterName, clickParameterValue);
			}
		}
	}
	
	public String getTargetDocument() {
		return targetDocument;
	}
	public void setTargetDocument(String targetDocument) {
		this.targetDocument = targetDocument;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUniqueName() {
		return uniqueName;
	}
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public TreeMap<String, String> getParametersList() {
		return parametersList;
	}
	public void setParametersList(TreeMap<String, String> parametersList) {
		this.parametersList = parametersList;
	}
	
	
	
	
	

}
