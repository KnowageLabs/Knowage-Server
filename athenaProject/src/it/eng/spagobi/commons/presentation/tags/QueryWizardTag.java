/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.presentation.tags;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.commons.utilities.urls.IUrlBuilder;
import it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.themes.ThemesManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 * Presentation tag for Query Wizard details. 
 */
public class QueryWizardTag extends CommonWizardLovTag {
	static private Logger logger = Logger.getLogger(QueryWizardTag.class);
	private HttpServletRequest httpRequest = null;
    protected RequestContainer requestContainer = null;
	protected ResponseContainer responseContainer = null;
	protected IUrlBuilder urlBuilder = null;
    protected IMessageBuilder msgBuilder = null;
    private String dataSourceLabel;
    private String queryDef;
	  String readonly = "readonly" ;
	  boolean isreadonly = true ;
	  String disabled = "disabled" ;
	  protected String currTheme="";
	 
	
	/**
	 * Gets the data source label.
	 * 
	 * @return the data source label
	 */
	public String getDataSourceLabel() {
		return dataSourceLabel;
	}
	
	/**
	 * Sets the data source label.
	 * 
	 * @param dataSourceLabel the new data source label
	 */
	public void setDataSourceLabel(String dataSourceLabel) {
		this.dataSourceLabel = dataSourceLabel;
	}
	
	/**
	 * Gets the query def.
	 * 
	 * @return the query def
	 */
	public String getQueryDef() {
		return queryDef;
	}
	
	/**
	 * Sets the query def.
	 * 
	 * @param queryDef the new query def
	 */
	public void setQueryDef(String queryDef) {
		this.queryDef = queryDef;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
	 */
	public int doEndTag() throws JspException {
        logger.debug("");
        return super.doEndTag();
    }
	
	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException{
		logger.debug("QueryWizardTag::doStartTag:: invoked");
		httpRequest = (HttpServletRequest) pageContext.getRequest();
		requestContainer = ChannelUtilities.getRequestContainer(httpRequest);
		responseContainer = ChannelUtilities.getResponseContainer(httpRequest);
		urlBuilder = UrlBuilderFactory.getUrlBuilder();
		msgBuilder = MessageBuilderFactory.getMessageBuilder();
		String dsLabelField = msgBuilder.getMessage("SBIDev.queryWiz.dsLabelField", "messages", httpRequest);
		String queryDefField = msgBuilder.getMessage("SBIDev.queryWiz.queryDefField", "messages", httpRequest);

    	currTheme=ThemesManager.getCurrentTheme(requestContainer);
    	if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();
		
		RequestContainer aRequestContainer = RequestContainer.getRequestContainer();
        SessionContainer aSessionContainer = aRequestContainer.getSessionContainer();
        SessionContainer permanentSession = aSessionContainer.getPermanentContainer();
		IEngUserProfile userProfile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
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
			
		List lstDs = new ArrayList();
		try{			
			lstDs =  DAOFactory.getDataSourceDAO().loadAllDataSources();			
		}catch (EMFUserError emf) {
			emf.printStackTrace();
		}
		Iterator itDs = lstDs.iterator();
		
		StringBuffer output = new StringBuffer();
		
		output.append("<table width='100%' cellspacing='0' border='0'>\n");
		output.append("	<tr>\n");
		output.append("		<td class='titlebar_level_2_text_section' style='vertical-align:middle;'>\n");
		output.append("			&nbsp;&nbsp;&nbsp;"+ msgBuilder.getMessage("SBIDev.queryWiz.wizardTitle", "messages", httpRequest) +"\n");
		output.append("		</td>\n");
		output.append("		<td class='titlebar_level_2_empty_section'>&nbsp;</td>\n");
		output.append("		<td class='titlebar_level_2_button_section'>\n");
		output.append("			<a style='text-decoration:none;' href='javascript:opencloseQueryWizardInfo()'> \n");
		output.append("				<img width='22px' height='22px'\n");
		output.append("				 	 src='" + urlBuilder.getResourceLinkByTheme(httpRequest, "/img/info22.jpg",currTheme)+"'\n");
		output.append("					 name='info'\n");
		output.append("					 alt='"+msgBuilder.getMessage("SBIDev.queryWiz.showSintax", "messages", httpRequest)+"'\n");
		output.append("					 title='"+msgBuilder.getMessage("SBIDev.queryWiz.showSintax", "messages", httpRequest)+"'/>\n");
		output.append("			</a>\n");
		output.append("		</td>\n");
		String urlImgProfAttr = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/profileAttributes22.jpg",currTheme);
		output.append(generateProfAttrTitleSection(urlImgProfAttr));
		output.append("	</tr>\n");
		output.append("</table>\n");
		
		output.append("<br/>\n");
		
	    output.append("<div class='div_detail_area_forms_lov'>\n");	
	    output.append("		<div class='div_detail_label_lov'>\n");
		output.append("			<span class='portlet-form-field-label'>\n");
		output.append(dsLabelField);
		output.append("			</span>\n");
		output.append("		</div>\n");
		output.append("		<div class='div_detail_form'>\n");
		output.append("			<select onchange='setLovProviderModified(true);' style='width:180px;' class='portlet-form-input-field' name='datasource' id='datasource' >\n");
		while (itDs.hasNext()) {
			IDataSource ds = (IDataSource)itDs.next();
			String dataSource = String.valueOf(ds.getLabel());
			String dataSourceDescription = ds.getDescr();
			dataSource = StringEscapeUtils.escapeHtml(dataSource);
			dataSourceDescription = StringEscapeUtils.escapeHtml(dataSourceDescription);
			
			String dsLabeleSelected = "";
			if (dataSourceLabel.equals(dataSource)) dsLabeleSelected = "selected=\"selected\"";
			output.append("			<option "+disabled+" value='" + dataSource + "' " + dsLabeleSelected + ">" + dataSourceDescription + "</option>\n");
		}
		output.append("			</select>\n");
		output.append("		</div>\n");
		output.append("		<div class='div_detail_label_lov'>\n");
		output.append("			<span class='portlet-form-field-label'>\n");
		output.append(queryDefField);
		output.append("			</span>\n");
		output.append("		</div>\n");
		output.append("		<div style='height:110px;' class='div_detail_form'>\n");
		output.append("			<textarea style='height:100px;' "+disabled+" class='portlet-text-area-field' name='queryDef' onchange='setLovProviderModified(true);'  cols='50'>" + queryDef + "</textarea>\n");
		output.append("		</div>\n");
		output.append("		<div class='div_detail_label_lov'>\n");
		output.append("			&nbsp;\n");
		output.append("		</div>\n");
		output.append("</div>\n");
	    
		
		output.append("<script>\n");
		output.append("		var infowizardqueryopen = false;\n");
		output.append("		var winQWT = null;\n");
		output.append("		function opencloseQueryWizardInfo() {\n");
		output.append("			if(!infowizardqueryopen){\n");
		output.append("				infowizardqueryopen = true;");
		output.append("				openQueryWizardInfo();\n");
		output.append("			}\n");
		output.append("		}\n");
		output.append("		function openQueryWizardInfo(){\n");
		output.append("			if(winQWT==null) {\n");
		output.append("				winQWT = new Window('winQWTInfo', {className: \"alphacube\", title:\""+msgBuilder.getMessage("SBIDev.queryWiz.showSintax", "messages", httpRequest)+"\", width:680, height:150, destroyOnClose: false});\n");
		output.append("         	winQWT.setContent('querywizardinfodiv', false, false);\n");
		output.append("         	winQWT.showCenter(false);\n");
		output.append("		    } else {\n");
		output.append("         	winQWT.showCenter(false);\n");
		output.append("		    }\n");
		output.append("		}\n");
		output.append("		observerQWT = { onClose: function(eventName, win) {\n");
		output.append("			if (win == winQWT) {\n");
		output.append("				infowizardqueryopen = false;");
		output.append("			}\n");
		output.append("		  }\n");
		output.append("		}\n");
		output.append("		Windows.addObserver(observerQWT);\n");
		output.append("</script>\n");
		
		output.append("<div id='querywizardinfodiv' style='display:none;'>\n");	
		output.append(msgBuilder.getMessageTextFromResource("it/eng/spagobi/commons/presentation/tags/info/querywizardinfo", httpRequest));
		output.append("</div>\n");	
		
        try {
            pageContext.getOut().print(output.toString());
        }
        catch (Exception ex) {
            logger.error(ex);
            throw new JspException(ex.getMessage());
        }
		return SKIP_BODY;
	}
	
	
}
