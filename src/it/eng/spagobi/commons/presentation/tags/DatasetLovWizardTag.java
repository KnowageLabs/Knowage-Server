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
import it.eng.spago.navigation.LightNavigationManager;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.commons.utilities.urls.IUrlBuilder;
import it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
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
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class DatasetLovWizardTag extends CommonWizardLovTag {

	static private Logger logger = Logger.getLogger(DatasetLovWizardTag.class);
	private String datasetId;
	private HttpServletRequest httpRequest = null;
    protected RequestContainer requestContainer = null;
	protected ResponseContainer responseContainer = null;
	protected IUrlBuilder urlBuilder = null;
    protected IMessageBuilder msgBuilder = null;
    String readonly = "readonly" ;
	boolean isreadonly = true ;
	String disabled = "disabled" ;
	protected String currTheme="";
	
	
	/**
	 * Gets the data set id
	 * 
	 * @return the data set id
	 */
	public String getDatasetId() {
		return datasetId;
	}
	
	/**
	 * Sets the data set id
	 * 
	 * @param dataSetLabel the new dataset id
	 */
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
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
		logger.debug("DatasetLovWizardTag::doStartTag:: invoked");
		httpRequest = (HttpServletRequest) pageContext.getRequest();
		requestContainer = ChannelUtilities.getRequestContainer(httpRequest);
		responseContainer = ChannelUtilities.getResponseContainer(httpRequest);
		urlBuilder = UrlBuilderFactory.getUrlBuilder();
		msgBuilder = MessageBuilderFactory.getMessageBuilder();
		String datasetLabelField = msgBuilder.getMessage("SBIDev.datasetLovWiz.datasetLabelField", "messages", httpRequest);
		
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
		output.append("			&nbsp;&nbsp;&nbsp;"+ msgBuilder.getMessage("SBIDev.datasetLovWiz.wizardTitle", "messages", httpRequest) +"\n");
		output.append("		</td>\n");
		output.append("		<td class='titlebar_level_2_empty_section'>&nbsp;</td>\n");
		output.append("		<td class='titlebar_level_2_button_section'>\n");
		output.append("			<a style='text-decoration:none;' href='javascript:opencloseDatasetWizardInfo()'> \n");
		output.append("				<img width='22px' height='22px'\n");
		output.append("				 	 src='" + urlBuilder.getResourceLinkByTheme(httpRequest, "/img/info22.jpg",currTheme)+"'\n");
		output.append("					 name='info'\n");
		output.append("					 alt='"+msgBuilder.getMessage("SBIDev.datasetLovWiz.info", "messages", httpRequest)+"'\n");
		output.append("					 title='"+msgBuilder.getMessage("SBIDev.datasetLovWiz.info", "messages", httpRequest)+"'/>\n");
		output.append("			</a>\n");
		output.append("		</td>\n");
		String urlImgProfAttr = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/profileAttributes22.jpg",currTheme);
		output.append(generateProfAttrTitleSection(urlImgProfAttr));
		output.append("	</tr>\n");
		output.append("</table>\n");
		
		output.append("<br/>\n");
		
	    output.append("<div class='div_detail_area_forms_lov'>\n");	
	    output.append("		<div class='div_detail_label_lov'>\n");
		output.append("					<span class='portlet-form-field-label'>\n");
		output.append(msgBuilder.getMessage("SBIDev.datasetLovWiz.datasetLabelField", "messages", httpRequest));
		output.append("					</span>\n");
		output.append("			<span class='portlet-form-field-label'>\n");
		output.append("			</span>\n");
		output.append("		</div>\n");
	
		
		output.append("<div class='div_detail_label' id='datasetLabel' >\n");

		output.append("				</div>\n");
		output.append("				\n");
		String url=GeneralUtilities.getSpagoBiHost()+GeneralUtilities.getSpagoBiContext() + GeneralUtilities.getSpagoAdapterHttpUrl() + 
		"?" + "PAGE=SelectDatasetLookupPage&NEW_SESSION=TRUE&" + LightNavigationManager.LIGHT_NAVIGATOR_DISABLED + "=TRUE";

		String currDataSetLabel="";
		Integer currDataSetId=null;
		String currDataSetIdValue="";
		
		//aggiunto condizione (!getDatasetId().isEmpty())
		if((getDatasetId()!=null) && (!getDatasetId().isEmpty())) {
			currDataSetId=new Integer(getDatasetId());
			currDataSetIdValue=currDataSetId.toString();
			IDataSet dataSet=null;
			try {
				dataSet = DAOFactory.getDataSetDAO().loadDataSetById(currDataSetId);
			} catch (EMFUserError e) {
				logger.error("Error Dataset Loading");
				e.printStackTrace();
			} 
			if(dataSet!=null){	
				currDataSetLabel=dataSet.getLabel();
			}
		}
		
		
		output.append("				<div class='div_detail_form' id='datasetForm' >\n");
		output.append("				  	<input type='hidden' name='dataset' id='dataset' value='"+currDataSetIdValue+"' />	\n");
		output.append("												\n");
		output.append("					<input class='portlet-form-input-field' style='width:230px;' type='text'  readonly='readonly'\n");
		output.append("									name='datasetReadLabel' id='datasetReadLabel' value='"+StringEscapeUtils.escapeHtml(currDataSetLabel)+"' maxlength='400' onchange='setLovProviderModified(true);' /> \n");
		output.append("				\n");
		output.append("					<a href='javascript:void(0);' id='datasetLink'>\n");
		output.append("						<img src="+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/detail.gif", currTheme) +" title='Lookup' alt='Lookup' />\n");
		output.append("					</a> 	\n");
		output.append("				</div>\n");
		output.append("			\n");
		output.append("			\n");
		output.append("		<script>\n");
		output.append("			var win_dataset;\n");
		output.append("			Ext.get('datasetLink').on('click', function(){\n");
		output.append("			if(!win_dataset){\n");
		output.append("				win_dataset = new Ext.Window({\n");
		output.append("				id:'popup_dataset',\n");
		output.append("				title:'dataset',\n");
		output.append("				bodyCfg:{\n");
		output.append("					tag:'div', \n");
		output.append("					cls:'x-panel-body', \n");
		output.append("					children:[{tag:'iframe', \n");
		output.append("								name: 'iframe_par_dataset',   \n");     			
		output.append("								id  : 'iframe_par_dataset',  \n");      			
		output.append("								src: '"+url+"',  \n"); 
		output.append("								frameBorder:0,\n");
		output.append("								width:'100%',\n");
		output.append("								height:'100%',\n");
		output.append("								style: {overflow:'auto'}  \n");
		output.append("								}]\n");
		output.append("						},\n");
		output.append("					layout:'fit',\n");
		output.append("					width:800,\n");
		output.append("					height:320,\n");
		output.append("					closeAction:'hide',\n");
		output.append("					plain: true\n");
		output.append("					});\n");
		output.append("					};\n");
		output.append("				win_dataset.show();\n");
		output.append("				}\n");
		output.append("				);\n");
		output.append("				\n");
		output.append("		</script>\n");
		
		output.append("		</div>\n");

		
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
