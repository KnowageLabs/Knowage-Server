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
import it.eng.spagobi.behaviouralmodel.lov.bo.FixedListDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.FixedListItemDetail;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.commons.utilities.urls.IUrlBuilder;
import it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory;
import it.eng.spagobi.utilities.themes.ThemesManager;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

/**
 * Presentation tag for Fix Lov Wizard details. 
 */

public class LovWizardTag extends CommonWizardLovTag {

	private String lovProvider;
	private HttpServletRequest httpRequest = null;
	protected RequestContainer requestContainer = null;
	protected ResponseContainer responseContainer = null;
	protected IUrlBuilder urlBuilder = null;
    protected IMessageBuilder msgBuilder = null;
    String readonly = "readonly" ;
	  boolean isreadonly = true ;
	  String disabled = "disabled" ;
	  protected String currTheme="";

	
	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {
		TracerSingleton.log(SpagoBIConstants.NAME_MODULE , TracerSingleton.DEBUG, 
				            "LovWizardTag::doStartTag:: invocato");
		httpRequest = (HttpServletRequest) pageContext.getRequest();
		requestContainer = ChannelUtilities.getRequestContainer(httpRequest);
		responseContainer = ChannelUtilities.getResponseContainer(httpRequest);
		urlBuilder = UrlBuilderFactory.getUrlBuilder();
		msgBuilder = MessageBuilderFactory.getMessageBuilder();
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
		
		try {
			
			output.append("<table width='100%' cellspacing='0' border='0'>\n");
			output.append("	<tr>\n");
			output.append("		<td class='titlebar_level_2_text_section' style='vertical-align:middle;'>\n");
			output.append("			&nbsp;&nbsp;&nbsp;"+ msgBuilder.getMessage("SBIDev.lovWiz.wizardTitle", "messages", httpRequest) +"\n");
			output.append("		</td>\n");
			output.append("		<td class='titlebar_level_2_empty_section'>&nbsp;</td>\n");
			output.append("		<td class='titlebar_level_2_button_section'>\n");
			output.append("			<a style='text-decoration:none;' href='javascript:opencloseFixListWizardInfo()'> \n");
			output.append("				<img width='22px' height='22px'\n");
			output.append("				 	 src='" + urlBuilder.getResourceLinkByTheme(httpRequest, "/img/info22.jpg",currTheme)+"'\n");
			output.append("					 name='info'\n");
			output.append("					 alt='"+msgBuilder.getMessage("SBIDev.fixlovWiz.rulesTitle", "messages", httpRequest)+"'\n");
			output.append("					 title='"+msgBuilder.getMessage("SBIDev.fixlovWiz.rulesTitle", "messages", httpRequest)+"'/>\n");
			output.append("			</a>\n");
			output.append("		</td>\n");
			String urlImgProfAttr = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/profileAttributes22.jpg",currTheme);
			output.append(generateProfAttrTitleSection(urlImgProfAttr));
			output.append("	</tr>\n");
			output.append("</table>\n");
			
			output.append("<br/>\n");
			
			String newItemNameField = msgBuilder.getMessage("SBIDev.lovWiz.newItemNameField", "messages", httpRequest);
			String newItemValueField = msgBuilder.getMessage("SBIDev.lovWiz.newItemValueField", "messages", httpRequest);
			output.append("<input type='hidden' id='insertFixLovItem' name='' value=''/>\n");
			output.append("<div class='div_detail_area_forms_lov'>\n");	
			output.append("		<div class='div_detail_label_lov'>\n");
			output.append("			<span class='portlet-form-field-label'>\n");
			output.append(newItemValueField);
			output.append("			</span>\n");
			output.append("		</div>\n");
			output.append("		<div class='div_detail_form'>\n");
			output.append("			<input class='portlet-form-input-field' type='text' "+readonly+" id='valueOfFixedLovItemNew' name='valueOfFixedLovItemNew' size='50' value=''>&nbsp;*\n");
			output.append("		</div>\n");
			output.append("		<div class='div_detail_label_lov'>\n");
			output.append("			<span class='portlet-form-field-label'>\n");
			output.append(newItemNameField);
			output.append("			</span>\n");
			output.append("		</div>\n");
			output.append("		<div class='div_detail_form'>\n");
			output.append("			<input class='portlet-form-input-field' type='text' "+readonly+" name='nameOfFixedLovItemNew' size='50' value=''/>&nbsp;*\n");
			output.append("		</div>\n");
			output.append("		<div class='div_detail_label_lov'>\n");
			output.append("			&nbsp;\n");
			output.append("		</div>\n");
			if(!isreadonly){
			output.append("		<div class='div_detail_form'>\n");
			output.append("			<input onclick='setLovProviderModified(true);' type='image' name='insertFixLovItem' value='insertFixLovItem'\n");
			output.append("				src='" + urlBuilder.getResourceLinkByTheme(httpRequest, "/img/attach.gif",currTheme) + "'\n");
			String addButtMsg = msgBuilder.getMessage("SBIDev.lovWiz.addButt", "messages", httpRequest);
			output.append("				title='" + addButtMsg + "' alt='" + addButtMsg + "'\n");
			output.append("			/>\n");
			output.append("			<a href='javascript:setLovProviderModified(true);newFixLovItemFormSubmit();' class='portlet-form-field-label' style='text-decoration:none;'>\n");
			output.append("				" + addButtMsg + "\n");
			output.append("			</a>\n");
			output.append("		</div>\n");
			}
			output.append("</div>\n");
			List lovs = new ArrayList();
			if (lovProvider != null  &&  !lovProvider.equals("")){
				//lovProvider = GeneralUtilities.substituteQuotesIntoString(lovProvider);
				lovs = FixedListDetail.fromXML(lovProvider).getItems();
			}
			
			output.append("<table class=\"table_detail_fix_lov\">\n");
		  	output.append("	<tr>\n");
		  	output.append("		<td colspan='1' class='portlet-section-header'>\n");
		  	String tableCol1 = msgBuilder.getMessage("SBIDev.lovWiz.tableCol2", "messages", httpRequest);
		  	output.append(			tableCol1 + "\n");
		  	output.append("		</td>\n");
		  	output.append("		<td colspan='1' class='portlet-section-header'>\n");
		  	String tableCol2 = msgBuilder.getMessage("SBIDev.lovWiz.tableCol1", "messages", httpRequest);
		  	output.append(			tableCol2 + "\n");
		  	output.append("		</td>\n");
		  	output.append("		<td colspan='1' width='20' class='portlet-section-header'>&nbsp;\n");
		  	output.append("		</td>\n");
		  	output.append("		<td colspan='1' width='20' class='portlet-section-header'>&nbsp;\n");
		  	output.append("		</td>\n");
		  	output.append("		<td colspan='1' width='20' class='portlet-section-header'>&nbsp;\n");
		  	output.append("		</td>\n");
		  	output.append("		<td colspan='1' width='18' class='portlet-section-header'>&nbsp;\n");
		  	output.append("		</td>\n");
		  	output.append("	</tr>\n");
			if (lovs != null) {
				output.append("		<input type='hidden' id='indexOfFixedLovItemToDelete' name='' value=''/>\n");
				output.append("		<input type='hidden' id='indexOfFixedLovItemToChange' name='' value=''/>\n");
				output.append("		<input type='hidden' id='indexOfItemToDown' name='' value=''/>\n");
				output.append("		<input type='hidden' id='indexOfItemToUp' name='' value=''/>\n");
				boolean alternate = false;
		        String rowClass;
				for (int i = 0; i < lovs.size(); i++) {
					FixedListItemDetail lovDet = (FixedListItemDetail) lovs.get(i); 
					String name = lovDet.getValue();
					String description = lovDet.getDescription();
					
					//before sending name and description to the hidden input,
					//substitute single and double quotes with their html encoding
					name = GeneralUtilities.substituteQuotesIntoString(name);
					description= GeneralUtilities.substituteQuotesIntoString(description);
				
					output.append("		<input type='hidden' name='nameOfFixedListItem' value='" + name + "'/>\n");
					output.append("		<input type='hidden' id='valueItem'  name='valueOfFixedListItem' value='"+description+"'/>\n");
					rowClass = (alternate) ? "portlet-section-alternate" : "portlet-section-body";
		            alternate = !alternate;
					output.append("	<tr class='portlet-font'>\n");
					String descrDec = URLDecoder.decode(description,"UTF-8");
					output.append("		<td class='" + rowClass + "'>");
					output.append("          <span style='display:inline;' id='nameRow"+i+"'>"+name+"</span>");
					output.append("          <input type='text' style='display:none;' id='nameRow"+i+"InpText' name='nameRow"+i+"InpText' value='"+name+"' />");
					output.append("     </td>\n");
					output.append("		<td class='" + rowClass + "'>");
					output.append("          <span style='display:inline;' id='descrRow"+i+"'>"+descrDec+"</span>");
					output.append("          <input type='text' style='display:none;' id='descrRow"+i+"InpText' name='descrRow"+i+"InpText' value='"+descrDec+"' />");
					output.append("     </td>\n");
					if(!isreadonly){
					output.append("		<td class='" + rowClass + "'>\n");
					String tooltipRowDetail = msgBuilder.getMessage("SBIDev.lovWiz.tableCol3", "messages", httpRequest);
					String tooltipRowSave = msgBuilder.getMessage("SBIDev.lovWiz.tableCol3.1", "messages", httpRequest);
					output.append("			<div style='display:inline;' id='divBtnDetailRow"+i+"'>");
					output.append("				<a href='javascript:changeRowValues(\""+ i +"\")'>");
					output.append("				<img class ='portlet-menu-item' \n");
					output.append("					src= '" + urlBuilder.getResourceLinkByTheme(httpRequest, "/img/detail.gif",currTheme) + "' \n");
					output.append("					title='" + tooltipRowDetail + "' alt='" + tooltipRowDetail + "' />\n");
					output.append("				</a>");
					output.append("			</div>");
					output.append("			<div style='display:none;' id='divBtnSaveRow"+i+"'>");
					output.append("				<input type='image' onclick='setLovProviderModified(true);saveRowValues(\""+ i +"\")' class ='portlet-menu-item' \n");
					output.append("					src= '" + urlBuilder.getResourceLinkByTheme(httpRequest, "/img/save16.gif",currTheme) + "' \n");
					output.append("					title='" + tooltipRowSave + "' alt='" + tooltipRowSave + "' />\n");
					output.append("			</div>");
					output.append("		</td>\n");
					}
					
					output.append("		<td class='" + rowClass + "'>\n");
					String tableCol4 = msgBuilder.getMessage("SBIDev.lovWiz.tableCol4", "messages", httpRequest);
					output.append("			<input type='image' onclick='setLovProviderModified(true);setIndexOfFixedLovItemToDelete(\""+ i +"\")' class ='portlet-menu-item' \n");
					output.append("				src= '" + urlBuilder.getResourceLinkByTheme(httpRequest, "/img/erase.gif",currTheme) + "' \n");
					output.append("				title='" + tableCol4 + "' alt='" + tableCol4 + "' />\n");
		  			output.append("		</td>\n");
		  			
		  			output.append("		<td class='" + rowClass + "'>\n");
		  			if(i<(lovs.size()-1)) {
						String tableCol5 = msgBuilder.getMessage("SBIDev.lovWiz.tableCol5", "messages", httpRequest);
						output.append("			<input type='image' onclick='setLovProviderModified(true);downRow(\""+ i +"\")' class ='portlet-menu-item' \n");
						output.append("				src= '" + urlBuilder.getResourceLinkByTheme(httpRequest, "/img/down16.gif",currTheme) + "' \n");
						output.append("				title='" + tableCol5 + "' alt='" + tableCol5 + "' />\n");
		  			} else {
		  				output.append("	        &nbsp;");
		  			}
		  			output.append("		</td>\n");
		  			
		  			output.append("		<td class='" + rowClass + "'>\n");
		  			if(i>0) {
						String tableCol6 = msgBuilder.getMessage("SBIDev.lovWiz.tableCol6", "messages", httpRequest);
						output.append("			<input type='image' onclick='setLovProviderModified(true);upRow(\""+ i +"\")' class ='portlet-menu-item' \n");
						output.append("				src= '" + urlBuilder.getResourceLinkByTheme(httpRequest, "/img/up16.gif",currTheme) + "' \n");
						output.append("				title='" + tableCol6 + "' alt='" + tableCol6 + "' />\n");
		  			} else {
		  				output.append("	        &nbsp;");
		  			}
		  			output.append("		</td>\n");
		  			
		  			output.append("	</tr>\n");
		  		}
		  	}
		  				 
			output.append("</table>\n");
			output.append("<script>\n");
			output.append(" function setIndexOfFixedLovItemToDelete (i) {\n");
			output.append("		document.getElementById('indexOfFixedLovItemToDelete').name = 'indexOfFixedLovItemToDelete';\n");
			output.append("		document.getElementById('indexOfFixedLovItemToDelete').value = i;\n");
			output.append(" }\n");
			output.append(" function newFixLovItemFormSubmit () {\n");
			output.append("		document.getElementById('insertFixLovItem').name = 'insertFixLovItem';\n");
			output.append("		document.getElementById('insertFixLovItem').value = 'insertFixLovItem';\n");
			output.append("		document.getElementById('modalitiesValueForm').submit();\n");
			output.append(" }\n");
			
			output.append(" function changeRowValues(index) {\n");
			output.append("		document.getElementById('nameRow'+index).style.display = 'none';\n");
			output.append("		document.getElementById('descrRow'+index).style.display = 'none';\n");
			output.append("		document.getElementById('nameRow'+index+'InpText').style.display = 'inline';\n");
			output.append("		document.getElementById('descrRow'+index+'InpText').style.display = 'inline';\n");
			output.append("		document.getElementById('divBtnDetailRow'+index).style.display = 'none';\n");
			output.append("		document.getElementById('divBtnSaveRow'+index).style.display = 'inline';\n");
			output.append(" }\n");
			
			output.append(" function saveRowValues(i) {\n");
			output.append("		document.getElementById('indexOfFixedLovItemToChange').name = 'indexOfFixedLovItemToChange';\n");
			output.append("		document.getElementById('indexOfFixedLovItemToChange').value = i;\n");
			output.append(" }\n");
			
			output.append(" function downRow(i) {\n");
			output.append("		document.getElementById('indexOfItemToDown').name = 'indexOfItemToDown';\n");
			output.append("		document.getElementById('indexOfItemToDown').value = i;\n");
			output.append(" }\n");
			
			output.append(" function upRow(i) {\n");
			output.append("		document.getElementById('indexOfItemToUp').name = 'indexOfItemToUp';\n");
			output.append("		document.getElementById('indexOfItemToUp').value = i;\n");
			output.append(" }\n");
			
			output.append("</script>\n");
			
			
			output.append("<script>\n");
			output.append("		var infowizardfixlistopen = false;\n");
			output.append("		var winFLWT = null;\n");
			output.append("		function opencloseFixListWizardInfo() {\n");
			output.append("			if(!infowizardfixlistopen){\n");
			output.append("				infowizardfixlistopen = true;");
			output.append("				openFixListWizardInfo();\n");
			output.append("			}\n");
			output.append("		}\n");
			output.append("		function openFixListWizardInfo(){\n");
			output.append("			if(winFLWT==null) {\n");
			output.append("				winFLWT = new Window('winFLWTInfo', {className: \"alphacube\", title:\""+msgBuilder.getMessage("SBIDev.fixlovWiz.rulesTitle", "messages", httpRequest)+"\", width:650, height:110, destroyOnClose: false});\n");
			output.append("         	winFLWT.setContent('fixlistwizardinfodiv', false, false);\n");
			output.append("         	winFLWT.showCenter(false);\n");
			output.append("		    } else {\n");
			output.append("         	winFLWT.showCenter(false);\n");
			output.append("		    }\n");
			output.append("		}\n");
			output.append("		observerFLWT = { onClose: function(eventName, win) {\n");
			output.append("			if (win == winFLWT) {\n");
			output.append("				infowizardfixlistopen = false;");
			output.append("			}\n");
			output.append("		  }\n");
			output.append("		}\n");
			output.append("		Windows.addObserver(observerFLWT);\n");
			output.append("</script>\n");
			
			output.append("<div id='fixlistwizardinfodiv' style='display:none;'>\n");	
			output.append(msgBuilder.getMessageTextFromResource("it/eng/spagobi/commons/presentation/tags/info/fixlistwizardinfo", httpRequest));
			output.append("</div>\n");
			
            pageContext.getOut().print(output.toString());
        }
        catch (Exception ex) {
            TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "LovWizardTag::doStartTag::", ex);
            throw new JspException(ex.getMessage());
        }
	    
		return SKIP_BODY;
	}
	
    /* (non-Javadoc)
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    public int doEndTag() throws JspException {
        TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "LovWizardTag::doEndTag:: invocato");
        return super.doEndTag();
    }
	
	/**
	 * Gets the lov provider.
	 * 
	 * @return the lov provider
	 */
	public String getLovProvider() {
		return lovProvider;
	}

	/**
	 * Sets the lov provider.
	 * 
	 * @param lovProvider the new lov provider
	 */
	public void setLovProvider(String lovProvider) {
		this.lovProvider = lovProvider;
	}
	
}
