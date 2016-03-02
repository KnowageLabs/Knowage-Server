<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spago.navigation.LightNavigationManager,
				java.util.*,
				it.eng.spagobi.commons.bo.Domain" %>
<%@page import="it.eng.spago.base.SourceBean"%>
<%@page import="it.eng.spagobi.engines.dossier.constants.DossierConstants"%>
		
		
<%
	SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("DossierCollaborationModule"); 
	String dossierIdStr = (String)moduleResponse.getAttribute(DossierConstants.DOSSIER_ID);
	String versionId = (String)moduleResponse.getAttribute(DossierConstants.VERSION_ID);
	List listStates = (List)moduleResponse.getAttribute(DossierConstants.DOSSIER_PRESENTATION_LIST_STATES);

	String label = (String)moduleResponse.getAttribute("label");
	if(label==null) label="";
	String name = (String)moduleResponse.getAttribute("name");
	if(name==null) name="";
	String description = (String)moduleResponse.getAttribute("description");
	if(description==null) description="";
	String publishMessage = (String)moduleResponse.getAttribute("PublishMessage");
	
	Map backUrlPars = new HashMap();
	backUrlPars.put("LIGHT_NAVIGATOR_BACK_TO", "1");
	String backUrl = urlBuilder.getUrl(request, backUrlPars);

	Map saveUrlPars = new HashMap();
	saveUrlPars.put("PAGE", DossierConstants.DOSSIER_COLLABORATION_PAGE);
	saveUrlPars.put("OPERATION", DossierConstants.OPERATION_PUBLISH_PRESENTATION);
	saveUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
	saveUrlPars.put(DossierConstants.DOSSIER_ID, dossierIdStr);
	saveUrlPars.put(DossierConstants.VERSION_ID, versionId);
   	String saveUrl = urlBuilder.getUrl(request, saveUrlPars);
	
%>		
		
				
<form method='POST' action='<%=saveUrl%>' id='publishForm' name='publishForm' >


<table class='header-table-portlet-section'>
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section' 
		    style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key="dossier.Execution" bundle="component_dossier_messages" />
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>		
		<td class='header-button-column-portlet-section'>
			<input type='image' class='header-button-image-portlet-section' 
      			   title='<spagobi:message key = "dossier.save" bundle="component_dossier_messages" />' 
      			   src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/save32.png", currTheme)%>' 
      			   alt='<spagobi:message key = "dossier.save"  bundle="component_dossier_messages"/>' />
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%= backUrl %>'> 
	      			<img class='header-button-image-portlet-section' 
	      				 title='<spagobi:message key = "dossier.back" bundle="component_dossier_messages" />' 
	      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/back.png", currTheme)%>' 
	      				 alt='<spagobi:message key = "dossier.back"  bundle="component_dossier_messages"/>' />
			</a>
		</td>
	</tr>
</table>



<div class='div_background_no_img' >


	
	<table width="100%" cellspacing="0" border="0" id = "fieldsTable" >
		<tr>
			<td>
				<div class="div_detail_area_forms">
					<div class='div_detail_label'>
						<span class='portlet-form-field-label'>
							<spagobi:message key = "SBIDev.docConf.docDet.labelField" />
						</span>
					</div>
					<div class='div_detail_form'>
						<input class='portlet-form-input-field' type="text" style='width:230px;' 
							   name="label" id="label" value="<%=StringEscapeUtils.escapeHtml(label)%>" maxlength="20">
						&nbsp;*
					</div>
					<div class='div_detail_label'>
						<span class='portlet-form-field-label'>
							<spagobi:message key = "SBIDev.docConf.docDet.nameField" />
						</span>
					</div>
					<div class='div_detail_form'>
						<input class='portlet-form-input-field' type="text" style='width:230px;' 
								name="name" id="name" value="<%=StringEscapeUtils.escapeHtml(name)%>" maxlength="40">
						&nbsp;*
					</div>
					<div class='div_detail_label'>
						<span class='portlet-form-field-label'>
							<spagobi:message key ="SBIDev.docConf.docDet.descriptionField" />
						</span>
					</div>
					<div class='div_detail_form'>
						<input class='portlet-form-input-field' style='width:230px;' type="text" 
 								name="description" id="description" value="<%=StringEscapeUtils.escapeHtml(description)%>" maxlength="160">
					</div>
					
					
					<!-- DISPLAY COMBO FOR STATE SELECTION -->
					<div class='div_detail_label'>
						<span class='portlet-form-field-label'>
							<spagobi:message key = "SBIDev.docConf.docDet.stateField" />
						</span>
					</div>  
 					<div class='div_detail_form'>
						<select class='portlet-form-input-field' style='width:230px;' name="state" id="state">
			      			<% 
			      		    Iterator iterstates = listStates.iterator();
			      		    while(iterstates.hasNext()) {
			      		    	Domain state = (Domain)iterstates.next();
			      		    	String objState = "REL";
			      		    	String currState = state.getValueCd();
			      		    	boolean isState = false;
			      		    	if(objState.equals(currState)){
			      		    		isState = true;   
			      		    	}
			      			%>
			      				<option value="<%=state.getValueId() + "," + state.getValueCd()  %>"<%if(isState) out.print(" selected='selected' ");  %>><%=state.getTranslatedValueName(locale)%></option>
			      			<%  
			      		    }
			      			%>
			      		</select>	
					</div>
					
					
					<!-- DISPLAY RADIO BUTTON FOR VISIBLE SELECTION -->
			    	<div class='div_detail_label'>
						<span class='portlet-form-field-label'>
							<spagobi:message key = "SBIDev.docConf.docDet.visibleField" />
						</span>
					</div>
					<div class='div_detail_form'>
						<% 
			      	      boolean isVisible = true;
			      	    %> 
					   	<input type="radio" name="visible" value="1" <% if(isVisible) { out.println(" checked='checked' "); } %>>
									<span class="portlet-font">True</span>
						</input>
			      		<input type="radio" name="visible" value="0" <% if(!isVisible) { out.println(" checked='checked' "); } %>>
								<span class="portlet-font">False</span>
						</input>
					</div>
					
					
				</div> 
				
				
				
				
				
				<%
					if((publishMessage!=null) && !publishMessage.trim().equals("") ){
				%>
				
					<div class="div_detail_area_forms">
						<span class='portlet-form-field-label'>
							<%=publishMessage%>
						</span>
					</div>
				
				<%
					}
				%>
				
				<spagobi:error/>
				
			</td>
			<!-- OPEN COLUMN WITH TREE FUNCTIONALITIES   -->	     
			<td width="60%">
				<div style='display:inline;' id='folderTree'>
					<spagobi:treeObjects moduleName="DossierCollaborationModule"  
	 						 htmlGeneratorClass="it.eng.spagobi.analiticalmodel.functionalitytree.presentation.FunctionalitiesTreeInsertObjectHtmlGenerator" />    	
				</div>
			</td>
      	</tr>
   </table>  

</div>

   <br/>
   <br/>
    	
</form>
    	
    	
    	
    	
