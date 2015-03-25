/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.presentation.tags;

import it.eng.spago.base.Constants;
import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.tracing.TracerSingleton;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.commons.utilities.urls.IUrlBuilder;
import it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory;
import it.eng.spagobi.utilities.themes.ThemesManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

/**
 * Presentation tag for Script details. 
 */

public class JavaClassWizardTag extends CommonWizardLovTag {
	
	private HttpServletRequest httpRequest = null;
	protected RequestContainer requestContainer = null;
	protected ResponseContainer responseContainer = null;
	protected IUrlBuilder urlBuilder = null;
    protected IMessageBuilder msgBuilder = null;
	private String javaClassName;
	private String currTheme="";
	 String readonly = "readonly" ;
	  boolean isreadonly = true ;
	  String disabled = "disabled" ;
	
	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		
		httpRequest = (HttpServletRequest) pageContext.getRequest();
		requestContainer = ChannelUtilities.getRequestContainer(httpRequest);
		responseContainer = ChannelUtilities.getResponseContainer(httpRequest);
		urlBuilder = UrlBuilderFactory.getUrlBuilder();
		msgBuilder = MessageBuilderFactory.getMessageBuilder();
		TracerSingleton.log(SpagoBIConstants.NAME_MODULE, TracerSingleton.DEBUG, 
				            "ScriptWizardTag::doStartTag:: invoked");
		RequestContainer aRequestContainer = RequestContainer.getRequestContainer();
        SessionContainer aSessionContainer = aRequestContainer.getSessionContainer();
        SessionContainer permanentSession = aSessionContainer.getPermanentContainer();
		IEngUserProfile userProfile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);

    	currTheme=ThemesManager.getCurrentTheme(requestContainer);
    	if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();
		
		boolean isable = false;
		try {
			isable = userProfile.isAbleToExecuteAction(SpagoBIConstants.LOVS_MANAGEMENT);
		} catch (EMFInternalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 if (isable){
			   	isreadonly = false;
			   	readonly = "";
			   	disabled = "";
			   }
		StringBuffer output = new StringBuffer();
		
		output.append("<table width='100%' cellspacing='0' border='0'>\n");
		output.append("	<tr>\n");
		output.append("		<td class='titlebar_level_2_text_section' style='vertical-align:middle;'>\n");
		output.append("			&nbsp;&nbsp;&nbsp;"+ msgBuilder.getMessage("SBIDev.javaClassWiz.title", "messages", httpRequest) +"\n");
		output.append("		</td>\n");
		output.append("		<td class='titlebar_level_2_empty_section'>&nbsp;</td>\n");
		output.append("		<td class='titlebar_level_2_button_section'>\n");
		output.append("			<a style='text-decoration:none;' href='javascript:opencloseJavaWizardInfo()'> \n");
		output.append("				<img width='22px' height='22px'\n");
		output.append("				 	 src='" + urlBuilder.getResourceLinkByTheme(httpRequest, "/img/info22.jpg",currTheme)+"'\n");
		output.append("					 name='info'\n");
		output.append("					 alt='"+msgBuilder.getMessage("SBIDev.javaClassWiz.SintaxLbl", "messages", httpRequest)+"'\n");
		output.append("					 title='"+msgBuilder.getMessage("SBIDev.javaClassWiz.SintaxLbl", "messages", httpRequest)+"'/>\n");
		output.append("			</a>\n");
		output.append("		</td>\n");
		String urlImgProfAttr = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/profileAttributes22.jpg",currTheme);
		output.append(generateProfAttrTitleSection(urlImgProfAttr));
		output.append("	</tr>\n");
		output.append("</table>\n");
		
		output.append("<br/>\n");
		
		output.append("<div class='div_detail_area_forms_lov'>\n");
		output.append("	<div class='div_detail_label_lov'>\n");
		String scriptLbl = msgBuilder.getMessage("SBIDev.javaClassWiz.javaClassNameLbl", "messages", httpRequest);
		output.append("			<span class='portlet-form-field-label'>\n");
		output.append(scriptLbl);
		output.append("			</span>\n");
		output.append("	</div>\n");
		output.append("	<div class='div_detail_form'>\n");
	    output.append("		<input type='text' "+readonly+" id='javaClassName' name='javaClassName' size='50' onchange='setLovProviderModified(true)' class='portlet-form-input-field' value='" + javaClassName + "' maxlength='100'/>&nbsp;*\n");
	    output.append("	</div>\n");
	    output.append("</div>\n");
		
	    output.append("<script>\n");
		output.append("		var infowizardjavaopen = false;\n");
		output.append("		var winJWT = null;\n");
		output.append("		function opencloseJavaWizardInfo() {\n");
		output.append("			if(!infowizardjavaopen){\n");
		output.append("				infowizardjavaopen = true;");
		output.append("				openJavaWizardInfo();\n");
		output.append("			}\n");
		output.append("		}\n");
		output.append("		function openJavaWizardInfo(){\n");
		output.append("			if(winJWT==null) {\n");
		output.append("				winJWT = new Window('winJWTInfo', {className: \"alphacube\", title:\""+msgBuilder.getMessage("SBIDev.javaClassWiz.SintaxLbl", "messages", httpRequest)+"\", width:680, height:150, destroyOnClose: false});\n");
		output.append("         	winJWT.setContent('javawizardinfodiv', true, false);\n");
		output.append("         	winJWT.showCenter(false);\n");
		output.append("		    } else {\n");
		output.append("         	winJWT.showCenter(false);\n");
		output.append("		    }\n");
		output.append("		}\n");
		output.append("		observerJWT = { onClose: function(eventName, win) {\n");
		output.append("			if (win == winJWT) {\n");
		output.append("				infowizardjavaopen = false;");
		output.append("			}\n");
		output.append("		  }\n");
		output.append("		}\n");
		output.append("		Windows.addObserver(observerJWT);\n");
		output.append("</script>\n");
		
		output.append("<div id='javawizardinfodiv' style='display:none;'>\n");	
		output.append(msgBuilder.getMessageTextFromResource("it/eng/spagobi/commons/presentation/tags/info/jclasswizardinfo", httpRequest));
		output.append("</div>\n");
		
        try {
            pageContext.getOut().print(output.toString());
        }
        catch (Exception ex) {
            TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "ScriptWizardTag::doStartTag::", ex);
            throw new JspException(ex.getMessage());
        }
		
		return SKIP_BODY;
	}
		
    /* (non-Javadoc)
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    public int doEndTag() throws JspException {
        TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "ScriptWizardTag::doEndTag:: invocato");
        return super.doEndTag();
    }
	
	
	/**
	 * Gets the java class name.
	 * 
	 * @return the java class name
	 */
	public String getJavaClassName() {
		return javaClassName;
	}
	
	/**
	 * Sets the java class name.
	 * 
	 * @param javaClassName the new java class name
	 */
	public void setJavaClassName(String javaClassName) {
		this.javaClassName = javaClassName;
	}
}
