/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.whatif.crossnavigation;


import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.whatif.template.WhatIfXMLTemplateParser;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
 * Target in cross navigation configuration
 */
public class Target implements Serializable{
	
	private static final long serialVersionUID = -4036176618363112988L;
	String documentLabel;
	String customizedView;
	String title;
	String description;
	String targetCross;
	String titleCross;
	List<TargetParameter> parameters;

	public Target() {
	}

	public Target(SourceBean sb) {		
		documentLabel = (String) sb.getAttribute(WhatIfXMLTemplateParser.TAG_TG_DOCUMENT_LABEL);
		customizedView = (String) sb.getAttribute(WhatIfXMLTemplateParser.TAG_TG_CUSTOMIZED_VIEW);		
		List list= sb.getAttributeAsList(WhatIfXMLTemplateParser.TAG_TG_TITLE);
		targetCross = (String) sb.getAttribute(WhatIfXMLTemplateParser.TAG_TG_TARGET);
		titleCross = (String) list.get(0);
		if (customizedView != null && customizedView.trim().equals("")) {
			customizedView = null;
		}
		SourceBean descrSB = (SourceBean) sb.getAttribute(WhatIfXMLTemplateParser.TAG_CN_DESCRIPTION);
		description = descrSB.getCharacters();
		SourceBean titleSB = (SourceBean) list.get(1);
		title = (String) titleSB.getCharacters();
		List parametersSB = sb.getAttributeAsList(WhatIfXMLTemplateParser.TAG_TN_PARAMETERS);
		List parameterSB = ((SourceBean) parametersSB.get(0)).getAttributeAsList(WhatIfXMLTemplateParser.TAG_TN_PARAMETER);
		boolean hasParameters = parameterSB != null && !parameterSB.isEmpty();
		parameters = new ArrayList<TargetParameter>();
		if (hasParameters) {
			for (int i = 0; i < parameterSB.size(); i++) {
				SourceBean temp = (SourceBean) parameterSB.get(i);
				TargetParameter aParameter = new TargetParameter(temp);
				if (aParameter != null) {
					parameters.add(aParameter);
				}
			}
		}
		
	}

	public String getDocumentLabel() {
		return documentLabel;
	}

	public void setDocumentLabel(String documentLabel) {
		this.documentLabel = documentLabel;
	}

	public String getCustomizedView() {
		return customizedView;
	}

	public void setCustomizedView(String customizedView) {
		this.customizedView = customizedView;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTargetCross() {
		return targetCross;
	}

	public void setTargetCross(String targetCross) {
		this.targetCross = targetCross;
	}

	public String getTitleCross() {
		return titleCross;
	}

	public void setTitleCross(String titleCross) {
		this.titleCross = titleCross;
	}

	public List<TargetParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<TargetParameter> parameters) {
		this.parameters = parameters;
	}
	

}