/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.presentation.tags;

import it.eng.spagobi.commons.utilities.PortletUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

public class CommonWizardLovTag extends TagSupport {

	protected IMessageBuilder msgBuilder = null;
	protected String _bundle = null;
	protected HttpServletRequest httpRequest = null;
	
	protected String generateProfAttrTitleSection(String urlImg) {
	
		msgBuilder = MessageBuilderFactory.getMessageBuilder();
		if (_bundle == null)
			_bundle = "messages";

		httpRequest = (HttpServletRequest) pageContext.getRequest();
		
		StringBuffer output = new StringBuffer();
		output.append("		<td class='titlebar_level_2_empty_section'>&nbsp;</td>\n");
		output.append("		<td class='titlebar_level_2_button_section'>\n");
		output.append("			<a style='text-decoration:none;' href='javascript:opencloseProfileAttributeWin()'> \n");
		output.append("				<img width='22px' height='22px'\n");
		output.append("				 	 src='" + urlImg +"'\n");
		output.append("					 name='info'\n");
		output.append("					 alt='"+msgBuilder.getMessage("SBIDev.lov.avaiableProfAttr", _bundle,httpRequest)+"'\n");
		output.append("					 title='"+msgBuilder.getMessage("SBIDev.lov.avaiableProfAttr", _bundle, httpRequest)+"'/>\n");		
		//output.append("					 alt='"+PortletUtilities.getMessage("SBIDev.lov.avaiableProfAttr", "messages")+"'\n");
		//output.append("					 title='"+PortletUtilities.getMessage("SBIDev.lov.avaiableProfAttr", "messages")+"'/>\n");
		output.append("			</a>\n");
		output.append("		</td>\n");
		String outputStr = output.toString();
		return outputStr;
	}
	
}
