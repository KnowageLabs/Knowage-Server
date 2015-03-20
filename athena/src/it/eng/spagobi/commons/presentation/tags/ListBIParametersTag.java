/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.commons.presentation.tags;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Builds and presents all objects list for all admin 
 * SpagoBI's list modules. Once a list module has been executed, 
 * the list tag builds all the correspondent jsp page and gives the results
 */
public class ListBIParametersTag extends ListTag
{
    
	/**
	 * Starting from the module <code>buttonsSB</code> object, 
	 * creates all buttons for the jsp list. 
	 * 
	 * @throws JspException If any exception occurs.
	 */
	
	protected StringBuffer makeButton() throws JspException {

		StringBuffer htmlStream = new StringBuffer();
		SourceBean buttonsSB = (SourceBean)_layout.getAttribute("BUTTONS");
		List buttons = buttonsSB.getContainedSourceBeanAttributes();
		Iterator iter = buttons.listIterator();
		while(iter.hasNext()) {
			SourceBeanAttribute buttonSBA = (SourceBeanAttribute)iter.next();
			SourceBean buttonSB = (SourceBean)buttonSBA.getValue();
			List parameters = buttonSB.getAttributeAsList("PARAMETER");
			HashMap paramsMap = getParametersMap(parameters, null);
			String img = (String)buttonSB.getAttribute("image");
			String labelCode = (String)buttonSB.getAttribute("label");
			String label = msgBuilder.getMessage(labelCode, "messages", httpRequest);
			label = StringEscapeUtils.escapeHtml(label);
			htmlStream.append("<form action='"+urlBuilder.getUrl(httpRequest, new HashMap())+"' id='form"+label+"'  method='POST' >\n");
			htmlStream.append("	<td class=\"header-button-column-portlet-section\">\n");
			Set paramsKeys = paramsMap.keySet();
			Iterator iterpar = paramsKeys.iterator();
			while(iterpar.hasNext()) {
				String paramKey = (String)iterpar.next();
				String paramValue = (String)paramsMap.get(paramKey);
				while(paramValue.indexOf("%20") != -1) {
					paramValue = paramValue.replaceAll("%20", " ");
				}
				htmlStream.append("	  <input type='hidden' name='"+paramKey+"' value='"+paramValue+"' /> \n");
			}
			htmlStream.append("		<a href='javascript:document.getElementById(\"form"+label+"\").submit()'><img class=\"header-button-image-portlet-section\" title='" + label + "' alt='" + label + "' src='"+urlBuilder.getResourceLinkByTheme(httpRequest, img, currTheme)+"' /></a>\n");
			htmlStream.append("	</td>\n");
			htmlStream.append("</form>\n");
		}	
		return htmlStream;
	} 



}
	
	



