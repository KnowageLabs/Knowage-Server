<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spago.navigation.LightNavigationManager,
				it.eng.spagobi.engines.dossier.constants.DossierConstants,
				java.util.List,
				java.util.Iterator,
				it.eng.spagobi.commons.bo.Role,
				it.eng.spagobi.engines.dossier.bo.ConfiguredBIDocument,
				it.eng.spagobi.commons.constants.SpagoBIConstants,
				it.eng.spagobi.engines.dossier.bo.WorkflowConfiguration" %>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.service.DetailBIObjectModule"%>
<%@page import="it.eng.spagobi.commons.constants.ObjectsTreeConstants"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.engines.dossier.dao.IDossierDAO"%>
<%@page import="it.eng.spagobi.engines.dossier.dao.DossierDAOHibImpl"%>
<%@page import="it.eng.spagobi.engines.dossier.utils.DossierUtilities"%>

<%
   SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute(DossierConstants.DOSSIER_MANAGEMENT_MODULE); 
   List confDocList = (List)moduleResponse.getAttribute(DossierConstants.CONFIGURED_DOCUMENT_LIST);
   String tempFolderPath = (String) moduleResponse.getAttribute(DossierConstants.DOSSIER_TEMP_FOLDER);
   String templateOOFileName = (String)moduleResponse.getAttribute(DossierConstants.OO_TEMPLATE_FILENAME);
   String wfProcDefFileName = (String)moduleResponse.getAttribute(DossierConstants.WF_PROCESS_DEFINTIION_FILENAME);
   
   Iterator iterDoc = confDocList.iterator();
   
   Map backUrlPars = new HashMap();
   backUrlPars.put("PAGE", DossierConstants.DOSSIER_MANAGEMENT_PAGE);
   backUrlPars.put("OPERATION", DossierConstants.OPERATION_EXIT_FROM_DETAIL);
   backUrlPars.put(DossierConstants.DOSSIER_TEMP_FOLDER, tempFolderPath);
   backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
   String backUrl = urlBuilder.getUrl(request, backUrlPars);
   
   Map formDetailUrlPars = new HashMap();
   formDetailUrlPars.put("PAGE", DossierConstants.DOSSIER_MANAGEMENT_PAGE);
   formDetailUrlPars.put("OPERATION", DossierConstants.OPERATION_DETAIL_CONFIGURED_DOCUMENT);
   formDetailUrlPars.put(DossierConstants.DOSSIER_TEMP_FOLDER, tempFolderPath);
   formDetailUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
   String formDetailUrl = urlBuilder.getUrl(request, formDetailUrlPars);
   
   Map formEraseUrlPars = new HashMap();
   formEraseUrlPars.put("PAGE", DossierConstants.DOSSIER_MANAGEMENT_PAGE);
   formEraseUrlPars.put("OPERATION", DossierConstants.OPERATION_DELETE_CONFIGURED_DOCUMENT);
   formEraseUrlPars.put(DossierConstants.DOSSIER_TEMP_FOLDER, tempFolderPath);
   formEraseUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
   String formEraseUrl = urlBuilder.getUrl(request, formEraseUrlPars);
   
   Map saveUrlPars = new HashMap();
   saveUrlPars.put("PAGE", DossierConstants.DOSSIER_MANAGEMENT_PAGE);
   saveUrlPars.put("OPERATION", DossierConstants.OPERATION_SAVE_DETAIL_DOSSIER);
   saveUrlPars.put(DossierConstants.DOSSIER_TEMP_FOLDER, tempFolderPath);
   saveUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
   String saveUrl = urlBuilder.getUrl(request, saveUrlPars);
   saveUrlPars.put("SAVE_AND_GO_BACK", "true");
   String saveAndGoBackUrl = urlBuilder.getUrl(request, saveUrlPars);
   
   //Map saveVersionUrlPars = new HashMap();
   //saveVersionUrlPars.put("PAGE", DossierConstants.DOSSIER_MANAGEMENT_PAGE);
   //saveVersionUrlPars.put("OPERATION", DossierConstants.OPERATION_SAVE_NEW_VERSION_DOSSIER);
   //saveVersionUrlPars.put(SpagoBIConstants.OBJECT_ID, idBiObjStr);
   //saveVersionUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
   //String saveVersionUrl = urlBuilder.getUrl(request, saveVersionUrlPars);
   
   Map formNewConfDocUrlPars = new HashMap();
   formNewConfDocUrlPars.put("PAGE", DossierConstants.DOSSIER_MANAGEMENT_PAGE);
   formNewConfDocUrlPars.put("OPERATION", DossierConstants.OPERATION_NEW_CONFIGURED_DOCUMENT);
   formNewConfDocUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
   String formNewConfDocUrl = urlBuilder.getUrl(request, formNewConfDocUrlPars);
   
   String savedOkMsgCode = (String) aServiceRequest.getAttribute(DossierConstants.DOSSIER_SAVED_MSG_CODE_ATTR_NAME);
%>




	<!-- ********************* TITOLO **************************  -->

<table class='header-table-portlet-section'>
		<tr class='header-row-portlet-section'>
			<td class='header-title-column-portlet-section' style='vertical-align:middle;padding-left:5px;'>
				<spagobi:message key="dossier.ConfTemp" bundle="component_dossier_messages" />
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
			<td class='header-empty-column-portlet-section'>&nbsp;</td>
			<td class='header-button-column-portlet-section'>
				<a href="<%= saveUrl %>"> 
	      			<img class='header-button-image-portlet-section' 
	      				 title='<spagobi:message key = "dossier.save" bundle="component_dossier_messages" />' 
	      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/save32.jpg", currTheme)%>' 
	      				 alt='<spagobi:message key = "dossier.save"  bundle="component_dossier_messages"/>' />
				</a>
			</td>
			<td class='header-empty-column-portlet-section'>&nbsp;</td>
			<td class='header-button-column-portlet-section'>
				<a href="<%= saveAndGoBackUrl %>"> 
	      			<img class='header-button-image-portlet-section' 
	      				 title='<spagobi:message key = "dossier.saveAndGoBack" bundle="component_dossier_messages" />' 
	      				 src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/saveAndGoBack.png", currTheme)%>' 
	      				 alt='<spagobi:message key = "dossier.saveAndGoBack"  bundle="component_dossier_messages"/>' />
				</a>
			</td>
		</tr>
	</table>
	
	<br/>
	
	
	<!-- Errors, if any -->
	<spagobi:error/>


	<%
		if (savedOkMsgCode != null && !savedOkMsgCode.trim().equals("")){
	%>
	
		<div class="div_detail_area_forms">
			<span class='portlet-form-field-label'>
				<spagobi:message key="<%= savedOkMsgCode %>" bundle="component_dossier_messages"/>
			</span>
		</div>
	
	<%
		}
	%>

	<!-- ********************* START LEFT DIV **************************  -->
	
	<div style="float:left;width:50%" class="div_detail_area_forms">
		
		
		
		
		
		
		<!-- ********************* LIST CONFIGURED DOCUMENT **************************  -->
		
		<div style='padding-top:10px;margin-right:5px;' class='portlet-section-header' style="float:left;width:100%;">	
			<spagobi:message key="dossier.ConfDocList" bundle="component_dossier_messages"/>
		</div>
		<div style="clear:left;margin-bottom:10px;padding-top:10px;">
			<table style="width:98%;">
			<%
				if(!iterDoc.hasNext()) {
			%>
				 <tr style="border:1px solid #eeeeee;">
					<td style="valign:middle;" class="portlet-form-field-label">
						<spagobi:message key = "dossier.noconfdocument" bundle="component_dossier_messages" />
					</td>
				</tr>
			<%
				}
			%>
			<%
						while(iterDoc.hasNext()) {
						ConfiguredBIDocument confDoc = (ConfiguredBIDocument)iterDoc.next();
			%>		
			  <tr style="border:1px solid #eeeeee;">
					<td style="valign:middle;" class="portlet-form-field-label">
							<%=confDoc.getLogicalName()%>&nbsp;&nbsp;&nbsp;(<%=confDoc.getName()%>)
					</td>
					<td width="20">
						<a href='<%=formDetailUrl + "&configureddocumentidentifier=" + confDoc.getLogicalName() %>' >
						<img 	title='<spagobi:message key = "dossier.detail" bundle="component_dossier_messages" />' 
      				 		src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/detail.gif", currTheme)%>' 
      				 		alt='<spagobi:message key = "dossier.detail"  bundle="component_dossier_messages"/>' />
      				 	</a>
					</td>
					<td  width="20">
					    <a href='<%=formEraseUrl + "&configureddocumentidentifier=" + confDoc.getLogicalName() %>' >
						<img 	title='<spagobi:message key = "dossier.erase" bundle="component_dossier_messages" />' 
      				 		src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/erase.gif", currTheme)%>' 
      				 		alt='<spagobi:message key = "dossier.erase"  bundle="component_dossier_messages"/>' />
      				 	</a>
					</td>
				</tr> 
			<%
 			}
 			%>
			</table>
		</div>
		
		
		
		
		
		<!-- ********************* TEMPLATE FORM **************************  -->

		<form action="<%=urlBuilder.getUrl(request, new HashMap())%>" method='POST' id='loadTemplatePresentationForm' 
				name='loadTemplatePresentationForm' enctype="multipart/form-data">
			<input type="hidden" name="<%=DossierConstants.DOSSIER_TEMP_FOLDER %>"  value="<%=tempFolderPath%>"/>
			<input type="hidden" name="PAGE" value="<%=DossierConstants.DOSSIER_MANAGEMENT_PAGE%>"/>
			<input type="hidden" name="OPERATION" value="<%=DossierConstants.OPERATION_LOAD_PRESENTATION_TEMPLATE%>"/>
			<input type="hidden" name="<%=LightNavigationManager.LIGHT_NAVIGATOR_DISABLED%>"  value="TRUE"/>
		
		<br/>
				
		<div style='padding-top:10px;margin-right:5px;' class='portlet-section-header' style="float:left;width:100%;">	
			<spagobi:message key="dossier.template" bundle="component_dossier_messages"/>
		</div>
		<br/>
		<span style='margin-top:5px;padding-top:5px;' class="portlet-form-field-label">
			<spagobi:message key="dossier.currenttemplate" bundle="component_dossier_messages"/>: 
		</span>
		&nbsp;&nbsp;&nbsp;
		<% 
			if( (templateOOFileName==null) || templateOOFileName.trim().equals("")) {	
		%>
		    <span style='font-size:11px;font-family:verdana;'>
			     <spagobi:message key="dossier.templatenotloaded" bundle="component_dossier_messages"/>  
		    </span>
		<% 	} else { 
		    out.print("<span style='font-size:11px;font-family:verdana;'>"+templateOOFileName+"</span>");
				String downOOTemplateUrl = DossierUtilities.getDossierServiceUrl(request) + "&" + 
						                   DossierConstants.DOSSIER_SERVICE_TASK + "=" + 
						                   DossierConstants.DOSSIER_SERVICE_TASK_DOWN_OOTEMPLATE + "&" +
						                   DossierConstants.DOSSIER_TEMP_FOLDER + "=" + tempFolderPath;				   
		%>
			&nbsp;&nbsp;&nbsp;
			<a style='text-decoration:none;' href='<%=downOOTemplateUrl%>' target="iframeForDownload">
				<img title='<spagobi:message key="dossier.download" bundle="component_dossier_messages" />' 
					 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/download16.gif", currTheme)%>' 
					 alt='<spagobi:message key="dossier.download"  bundle="component_dossier_messages"/>' />
			</a>
		<%
			}
		%>
		<br/>
		<br/>
			<input size="30" type="file" name="templatefile" onchange="document.getElementById('loadTemplatePresentationFormButton').style.display='inline';" />
			<a style='text-decoration:none;display:none;' id='loadTemplatePresentationFormButton' 
					href='javascript:document.getElementById("loadTemplatePresentationForm").submit();'>
				<img title='<spagobi:message key="dossier.upload.presentationTemplate" bundle="component_dossier_messages" />' 
					 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/upload32.png", currTheme)%>' 
					 alt='<spagobi:message key="dossier.upload.presentationTemplate"  bundle="component_dossier_messages"/>' />
			</a>
 		<br/>
 		<br/>
		</form>
		
  
  
  
  
  		<!-- ********************* WORKFLOW FORM **************************  -->
  		
   		<form action="<%=urlBuilder.getUrl(request, new HashMap())%>" method='POST' id='loadProcessDefinitionFileForm' 
   				name='loadProcessDefinitionFileForm' enctype="multipart/form-data">
			<input type="hidden" name="<%=DossierConstants.DOSSIER_TEMP_FOLDER%>"  value="<%=tempFolderPath%>"/>
			<input type="hidden" name="PAGE" value="<%=DossierConstants.DOSSIER_MANAGEMENT_PAGE%>"/>
			<input type="hidden" name="OPERATION" value="<%=DossierConstants.OPERATION_LOAD_PROCESS_DEFINITION_FILE%>"/>
			<input type="hidden" name="<%=LightNavigationManager.LIGHT_NAVIGATOR_DISABLED%>"  value="TRUE"/>
     	
		<div style='padding-top:10px;margin-right:5px;' class='portlet-section-header' style="float:left;width:100%;">	
			<div style='width:100%;float:left;'>
				<spagobi:message key="dossier.workflowData" bundle="component_dossier_messages"/>
			</div>
		</div>
		<br/>
		<span style='margin-top:5px;padding-top:5px;' class="portlet-form-field-label">
			<spagobi:message key="dossier.currentWFprocessDefFile" bundle="component_dossier_messages"/>: 
		</span>
		&nbsp;&nbsp;&nbsp;
		<% 
			if( (wfProcDefFileName==null) || wfProcDefFileName.trim().equals("")) {	
		%>
		    <span style='font-size:11px;font-family:verdana;'>
			     <spagobi:message key="dossier.WFprocessDefFileNotloaded" bundle="component_dossier_messages"/> 
		    </span>
    <% 	} else { 
				out.print("<span style='font-size:11px;font-family:verdana;'>"+wfProcDefFileName+"</span>");
				String downWorkDefUrl = DossierUtilities.getDossierServiceUrl(request) + "&" + 
            							DossierConstants.DOSSIER_SERVICE_TASK + "=" + 
            							DossierConstants.DOSSIER_SERVICE_TASK_DOWN_WORKFLOW_DEFINITION + "&" +
            							DossierConstants.DOSSIER_TEMP_FOLDER + "=" + tempFolderPath;	
		%>
		
			&nbsp;&nbsp;&nbsp;
			<a style='text-decoration:none;' href='<%=downWorkDefUrl%>' target="iframeForDownload">
				<img title='<spagobi:message key="dossier.download" bundle="component_dossier_messages" />' 
					 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/download16.gif", currTheme)%>' 
				 	alt='<spagobi:message key="dossier.download"  bundle="component_dossier_messages"/>' />
			</a>
		
		<%
		}
		%>
		<br/>
		<br/>
			<input size="30" type="file" name="wfdefinitionfile" onchange="document.getElementById('loadProcessDefinitionFileFormButton').style.display='inline';"/>
			<a style='text-decoration:none;display:none;' id='loadProcessDefinitionFileFormButton'
					href='javascript:document.getElementById("loadProcessDefinitionFileForm").submit();'>
				<img title='<spagobi:message key="dossier.upload.processDefinitionFile" bundle="component_dossier_messages" />' 
					 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/upload32.png", currTheme)%>' 
					 alt='<spagobi:message key="dossier.upload.processDefinitionFile"  bundle="component_dossier_messages"/>' />
			</a>
		<br/>
		<br/>
		
		</form>
		
	</div>







	<!-- ********************* START RIGHT DIV **************************  -->

	<div style="float:left;width:45%" class="div_detail_area_forms">
		<form action="<%=formNewConfDocUrl%>" method='POST' id='newForm' name='newForm'>
		<input type="hidden" value="<%=tempFolderPath%>" name="<%=DossierConstants.DOSSIER_TEMP_FOLDER%>" />
		<div style='padding-top:10px;margin-right:5px;' class='portlet-section-header'>	
				<div style='width:90%;float:left;'>
						<spagobi:message key="dossier.addConfDoc" bundle="component_dossier_messages"/>
				</div>
				<div style="width:8%;float:left;">
					<input style="margin-left:10px;" type="image" 
								 title='<spagobi:message key="dossier.addDocument" bundle="component_dossier_messages" />' 
								 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/add.gif", currTheme)%>' 
								 alt='<spagobi:message key="dossier.addDocument"  bundle="component_dossier_messages"/>' />
				</div>
		</div>
		<div>
			<spagobi:treeObjects moduleName="<%=DossierConstants.DOSSIER_MANAGEMENT_MODULE%>"  
								 htmlGeneratorClass="it.eng.spagobi.engines.dossier.treegenerators.DocumentsTreeHtmlGenerator" />
		    <br/>
		    <br/>
		</div>
		</form>
	</div>


	<div style="clear:left;">
		&nbsp;
	</div>

	

<br/>

<div id="iframeForDownload" style="display:none;">
	<iframe name="iframeForDownload" src="" style="width:0px;height:0px;" ></iframe> 
</div>










