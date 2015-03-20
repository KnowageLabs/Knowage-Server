/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */


package it.eng.spagobi.commons.presentation.tags;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.commons.utilities.urls.IUrlBuilder;
import it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory;
import it.eng.spagobi.tools.dataset.bo.DataSetParameterItem;
import it.eng.spagobi.tools.dataset.bo.DataSetParametersList;
import it.eng.spagobi.utilities.themes.ThemesManager;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;


/**   @author Giulio Gavardi
 *     giulio.gavardi@eng.it
 */

public class DatasetWizardTag  extends TagSupport {

		private String parametersXML;
		private HttpServletRequest httpRequest = null;
		protected RequestContainer requestContainer = null;
		protected ResponseContainer responseContainer = null;
		protected IUrlBuilder urlBuilder = null;
	    protected IMessageBuilder msgBuilder = null;
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
				//output.append("<table id='tag'><tr><td>");				
				output.append("<table width='100%' cellspacing='0' border='0'>\n");
				output.append("	<tr>\n");
				output.append("		<td class='titlebar_level_2_text_section' style='vertical-align:middle;'>\n");
				output.append("			&nbsp;&nbsp;&nbsp;"+ msgBuilder.getMessage("SBIDev.DataSetWiz.wizardTitle", "messages", httpRequest) +"\n");
				output.append("		</td>\n");
				output.append("		<td class='titlebar_level_2_empty_section'>&nbsp;</td>\n");
				output.append("		<td class='titlebar_level_2_button_section'>\n");
				output.append("			<a style='text-decoration:none;' href='javascript:opencloseDatasetListWizardInfo()'> \n");
				output.append("				<img width='22px' height='22px'\n");
				output.append("				 	 src='" + urlBuilder.getResourceLinkByTheme(httpRequest, "/img/info22.jpg",currTheme)+"'\n");
				output.append("					 name='info'\n");
				output.append("					 alt='"+msgBuilder.getMessage("SBIDev.DataSetWiz.rulesTitle", "messages", httpRequest)+"'\n");
				output.append("					 title='"+msgBuilder.getMessage("SBIDev.DataSetWiz.rulesTitle", "messages", httpRequest)+"'/>\n");
				output.append("			</a>\n");
				output.append("		</td>\n");
				String urlImgProfAttr = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/profileAttributes22.jpg",currTheme);
				output.append(generateProfAttrTitleSection(urlImgProfAttr));
				output.append("	</tr>\n");
				output.append("</table>\n");

				output.append("<br/>\n");
				
				String newItemTypeField = msgBuilder.getMessage("SBIDev.DataSetWiz.newItemTypeField", "messages", httpRequest);
				String newItemNameField = msgBuilder.getMessage("SBIDev.DataSetWiz.newItemNameField", "messages", httpRequest);
				output.append("<input type='hidden' id='insertDatasetParameterItem' name='' value=''/>\n");
				output.append("<div class='div_detail_area_forms_lov'>\n");	
				output.append("		<div class='div_detail_label_lov'>\n");
				output.append("			<span class='portlet-form-field-label'>\n");
				output.append(newItemNameField);
				output.append("			</span>\n");
				output.append("		</div>\n");
				output.append("		<div class='div_detail_form'>\n");
				output.append("			<input class='portlet-form-input-field' type='text' "+readonly+" id='nameOfDatasetParameterItemNew' name='nameOfDatasetParameterItemNew' size='50' value=''/>&nbsp;*\n");
				output.append("		</div>\n");
				output.append("		<div class='div_detail_label_lov'>\n");
				output.append("			<span class='portlet-form-field-label'>\n");
				output.append(newItemTypeField);
				output.append("			</span>\n");
				output.append("		</div>\n");
				output.append("		<div class='div_detail_form'>\n");
				output.append("			<select class='portlet-form-input-field' "+readonly+" name='typeOfDatasetParameterItemNew'>&nbsp;*\n");
				output.append("			<option value=\"String\">String</option>");
				output.append("			<option value=\"Number\">Number</option>");
				output.append("			<option value=\"Raw\">Raw</option>");
				output.append("			<option value=\"Generic\">Generic</option>");
				//output.append("			<option value=\"Date\">Date</option>");
				output.append("</select>");
				//output.append("			<input class='portlet-form-input-field' type='text' "+readonly+" name='typeOfDatasetParameterItemNew' size='50' value=''/>&nbsp;*\n");
				output.append("		</div>\n");
				output.append("		<div class='div_detail_label_lov'>\n");
				output.append("			&nbsp;\n");
				output.append("		</div>\n");
				if(!isreadonly){
				output.append("		<div class='div_detail_form'>\n");
				output.append("			<input onclick='setParametersXMLModified(true);' type='image' name='insertDatasetParameterItem' value='insertDatasetParameterItem'\n");
				output.append("				src='" + urlBuilder.getResourceLinkByTheme(httpRequest, "/img/attach.gif",currTheme) + "'\n");
				String addButtMsg = msgBuilder.getMessage("SBIDev.DataSetWiz.addButt", "messages", httpRequest);
				output.append("				title='" + addButtMsg + "' alt='" + addButtMsg + "'\n");
				output.append("			/>\n");
				output.append("			<a href='javascript:setParametersXMLModified(true);newDatasetParameterFormSubmit();' class='portlet-form-field-label' style='text-decoration:none;'>\n");
				output.append("				" + addButtMsg + "\n");
				output.append("			</a>\n");
				output.append("		</div>\n");
				}
				output.append("</div>\n");
				List parameters = new ArrayList();
				if (parametersXML != null  &&  !parametersXML.equals("")){
					//lovProvider = GeneralUtilities.substituteQuotesIntoString(lovProvider);
					parameters = DataSetParametersList.fromXML(parametersXML).getItems();
				}
				
				output.append("<table class=\"table_detail__lov\">\n");
			  	output.append("	<tr>\n");
			  	output.append("		<td colspan='1' class='portlet-section-header'>\n");
			  	String tableCol1 = msgBuilder.getMessage("SBIDev.DataSetWiz.tableCol2", "messages", httpRequest);
			  	output.append(			tableCol1 + "\n");
			  	output.append("		</td>\n");
			  	output.append("		<td colspan='1' class='portlet-section-header'>\n");
			  	String tableCol2 = msgBuilder.getMessage("SBIDev.DataSetWiz.newItemTypeField", "messages", httpRequest);
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
				if (parameters != null) {
					output.append("		<input type='hidden' id='indexOfDatasetParameterItemToDelete' name='' value=''/>\n");
					output.append("		<input type='hidden' id='indexOfDatasetParameterItemToChange' name='' value=''/>\n");
					output.append("		<input type='hidden' id='indexOfItemToDown' name='' value=''/>\n");
					output.append("		<input type='hidden' id='indexOfItemToUp' name='' value=''/>\n");
					boolean alternate = false;
			        String rowClass;
					for (int i = 0; i < parameters.size(); i++) {
						DataSetParameterItem dsDet = (DataSetParameterItem) parameters.get(i); 
						String name = dsDet.getName();
						String type = dsDet.getType();
						
						//before sending name and description to the hidden input,
						//substitute single and double quotes with their html encoding
						name = GeneralUtilities.substituteQuotesIntoString(name);
						type= GeneralUtilities.substituteQuotesIntoString(type);
					
						output.append("		<input type='hidden' name='nameOfDatasetParameterListItem' value='" + name + "'/>\n");
						output.append("		<input type='hidden' id='valueItem'  name='nameOfDatasetParameterItem' value='"+type+"'/>\n");
						rowClass = (alternate) ? "portlet-section-alternate" : "portlet-section-body";
			            alternate = !alternate;
						output.append("	<tr class='portlet-font'>\n");
						String typeDec = URLDecoder.decode(type,"UTF-8");
						output.append("		<td class='" + rowClass + "'>");
						output.append("          <span style='display:inline;' id='nameRow"+i+"'>"+name+"</span>");
						output.append("          <input type='text' style='display:none;' id='nameRow"+i+"InpText' name='nameRow"+i+"InpText' value='"+name+"' />");
						output.append("     </td>\n");
						output.append("		<td class='" + rowClass + "'>");
						output.append("          <span style='display:inline;' id='typeRow"+i+"'>"+typeDec+"</span>");
						//output.append("          <input type='text' style='display:none;' id='typeRow"+i+"InpText' name='typeRow"+i+"InpText' value='"+typeDec+"' />");
						output.append("			<select class='portlet-form-input-field' style='display:none;' name='typeRow"+i+"InpText' id='typeRow"+i+"InpText'/>&nbsp;*\n");
						String str="";
						String num="";
						String raw="";
						String generic="";
						String dat="";
						if(typeDec.equals("String"))str="selected";
						else if(typeDec.equals("Number"))num="selected";
						else if(typeDec.equals("Raw"))raw="selected";
						else if(typeDec.equals("Generic"))generic="selected";
						//else if(typeDec.equals("Date"))dat="selected";
						output.append("			<option value=\"String\""+str+" >String</option>");
						output.append("			<option value=\"Number\""+num+">Number</option>");
						output.append("			<option value=\"Raw\""+raw+">Raw</option>");
						output.append("			<option value=\"Generic\""+generic+">Generic</option>");
						//output.append("			<option value=\"Date\""+dat+">Date</option></select>");
						output.append("     </td>\n");
						if(!isreadonly){
						output.append("		<td class='" + rowClass + "'>\n");
						String tooltipRowDetail = msgBuilder.getMessage("SBIDev.DataSetWiz.tableCol3", "messages", httpRequest);
						String tooltipRowSave = msgBuilder.getMessage("SBIDev.DataSetWiz.tableCol3.1", "messages", httpRequest);
						output.append("			<div style='display:inline;' id='divBtnDetailRow"+i+"'>");
						output.append("				<a href='javascript:changeRowValues(\""+ i +"\")'>");
						output.append("				<img class ='portlet-menu-item' \n");
						output.append("					src= '" + urlBuilder.getResourceLinkByTheme(httpRequest, "/img/detail.gif",currTheme) + "' \n");
						output.append("					title='" + tooltipRowDetail + "' alt='" + tooltipRowDetail + "' />\n");
						output.append("				</a>");
						output.append("			</div>");
						output.append("			<div style='display:none;' id='divBtnSaveRow"+i+"'>");
						output.append("				<input type='image' onclick='setParametersXMLModified(true);saveRowValues(\""+ i +"\")' class ='portlet-menu-item' \n");
						output.append("					src= '" + urlBuilder.getResourceLinkByTheme(httpRequest, "/img/save16.gif",currTheme) + "' \n");
						output.append("					title='" + tooltipRowSave + "' alt='" + tooltipRowSave + "' />\n");
						output.append("			</div>");
						output.append("		</td>\n");
						}
						
						output.append("		<td class='" + rowClass + "'>\n");
						String tableCol4 = msgBuilder.getMessage("SBIDev.DataSetWiz.tableCol4", "messages", httpRequest);
						output.append("			<input type='image' onclick='setParametersXMLModified(true);setIndexOfDatasetParameterItemToDelete(\""+ i +"\")' class ='portlet-menu-item' \n");
						output.append("				src= '" + urlBuilder.getResourceLinkByTheme(httpRequest, "/img/erase.gif",currTheme) + "' \n");
						output.append("				title='" + tableCol4 + "' alt='" + tableCol4 + "' />\n");
			  			output.append("		</td>\n");
			  			
			  			output.append("		<td class='" + rowClass + "'>\n");
			  		/*	if(i<(parameters.size()-1)) {
							String tableCol5 = msgBuilder.getMessage("SBIDev.DataSetWiz.tableCol5", "messages", httpRequest);
							output.append("			<input type='image' onclick='setParametersXMLModified(true);downRow(\""+ i +"\")' class ='portlet-menu-item' \n");
							output.append("				src= '" + urlBuilder.getResourceLink(httpRequest, "/img/down16.gif") + "' \n");
							output.append("				title='" + tableCol5 + "' alt='" + tableCol5 + "' />\n");
			  			} else {
			  				output.append("	        &nbsp;");
			  			}*/
			  			output.append("		</td>\n");
			  			
			  			output.append("		<td class='" + rowClass + "'>\n");
			  			/*if(i>0) {
							String tableCol6 = msgBuilder.getMessage("SBIDev.DataSetWiz.tableCol6", "messages", httpRequest);
							output.append("			<input type='image' onclick='setParametersXMLModified(true);upRow(\""+ i +"\")' class ='portlet-menu-item' \n");
							output.append("				src= '" + urlBuilder.getResourceLink(httpRequest, "/img/up16.gif") + "' \n");
							output.append("				title='" + tableCol6 + "' alt='" + tableCol6 + "' />\n");
			  			} else {
			  				output.append("	        &nbsp;");
			  			}*/
			  			output.append("		</td>\n");
			  			
			  			output.append("	</tr>\n");
			  		}
			  	}
			  				 
				output.append("</table>\n");
				output.append("<script>\n");
				output.append(" function setIndexOfDatasetParameterItemToDelete(i) {\n");
				output.append("		document.getElementById('indexOfDatasetParameterItemToDelete').name = 'indexOfDatasetParameterItemToDelete';\n");
				output.append("		document.getElementById('indexOfDatasetParameterItemToDelete').value = i;\n");
				output.append(" }\n");
				output.append(" function newDatasetParameterFormSubmit() {\n");
				output.append("		document.getElementById('insertDatasetParameterItem').name = 'insertDatasetParameterItem';\n");
				output.append("		document.getElementById('insertDatasetParameterItem').value = 'insertDatasetParameterItem';\n");
				output.append("		document.getElementById('dsForm').submit();\n");
				output.append(" }\n");
				
				output.append(" function changeRowValues(index) {\n");
				output.append("		document.getElementById('nameRow'+index).style.display = 'none';\n");
				output.append("		document.getElementById('typeRow'+index).style.display = 'none';\n");
				output.append("		document.getElementById('nameRow'+index+'InpText').style.display = 'inline';\n");
				output.append("		document.getElementById('typeRow'+index+'InpText').style.display = 'inline';\n");
				output.append("		document.getElementById('divBtnDetailRow'+index).style.display = 'none';\n");
				output.append("		document.getElementById('divBtnSaveRow'+index).style.display = 'inline';\n");
				output.append(" }\n");
				
				output.append(" function saveRowValues(i) {\n");
				output.append("		document.getElementById('indexOfDatasetParameterItemToChange').name = 'indexOfDatasetParameterItemToChange';\n");
				output.append("		document.getElementById('indexOfDatasetParameterItemToChange').value = i;\n");
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
				output.append("		var infowizarddatasetlistopen = false;\n");
				output.append("		var winFLWT = null;\n");
				output.append("		function opencloseDatasetListWizardInfo() {\n");
				output.append("			if(!infowizarddatasetlistopen){\n");
				output.append("				infowizarddatasetlistopen = true;");
				output.append("				openDatasetListWizardInfo();\n");
				output.append("			}\n");
				output.append("		}\n");
				output.append("		function openDatasetListWizardInfo(){\n");
				output.append("			if(winFLWT==null) {\n");
				output.append("				winFLWT = new Window('winFLWTInfo', {className: \"alphacube\", title:\""+msgBuilder.getMessage("SBIDev.DataSetWiz.rulesTitle", "messages", httpRequest)+"\", width:650, height:110, destroyOnClose: false});\n");
				output.append("         	winFLWT.setContent('datasetlistwizardinfodiv', false, false);\n");
				output.append("         	winFLWT.showCenter(false);\n");
				output.append("		    } else {\n");
				output.append("         	winFLWT.showCenter(false);\n");
				output.append("		    }\n");
				output.append("		}\n");
				output.append("		observerFLWT = { onClose: function(eventName, win) {\n");
				output.append("			if (win == winFLWT) {\n");
				output.append("				infowizarddatasetlistopen = false;");
				output.append("			}\n");
				output.append("		  }\n");
				output.append("		}\n");
				output.append("		Windows.addObserver(observerFLWT);\n");
				output.append("</script>\n");
				
				output.append("<div id='datasetlistwizardinfodiv' style='display:none;'>\n");	
				output.append(msgBuilder.getMessageTextFromResource("it/eng/spagobi/commons/presentation/tags/info/datasetlistwizardinfo", httpRequest));
				output.append("</div>\n");
				//output.append("</td></tr></table>\n");
				
				
	            pageContext.getOut().print(output.toString());
	        }
	        catch (Exception ex) {
	            throw new JspException(ex.getMessage());
	        }
		    
			return SKIP_BODY;
		}
		
	    /* (non-Javadoc)
    	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
    	 */
    	public int doEndTag() throws JspException {
	        return super.doEndTag();
	    }

		/**
		 * Gets the parameters xml.
		 * 
		 * @return the parameters xml
		 */
		public String getParametersXML() {
			return parametersXML;
		}

		/**
		 * Sets the parameters xml.
		 * 
		 * @param parametersXML the new parameters xml
		 */
		public void setParametersXML(String parametersXML) {
			this.parametersXML = parametersXML;
		}
		
		protected String generateProfAttrTitleSection(String urlImg) {
			StringBuffer output = new StringBuffer();
			output.append("		<td class='titlebar_level_2_empty_section'>&nbsp;</td>\n");
			output.append("		<td class='titlebar_level_2_button_section'>\n");
			output.append("			<a style='text-decoration:none;' href='javascript:opencloseProfileAttributeWin()'> \n");
			output.append("				<img width='22px' height='22px'\n");
			output.append("				 	 src='" + urlImg +"'\n");
			output.append("					 name='info'\n");
			output.append("					 alt='" + msgBuilder.getMessage("SBIDev.dataset.availableProfAttr", "messages", this.httpRequest) + "'\n");
			output.append("					 title='" + msgBuilder.getMessage("SBIDev.dataset.availableProfAttr", "messages", this.httpRequest) + "'/>\n");
			output.append("			</a>\n");
			output.append("		</td>\n");
			String outputStr = output.toString();
			return outputStr;
		}
	
	
	
	
	
	
}
