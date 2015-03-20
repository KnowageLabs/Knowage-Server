<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@ page import="it.eng.spago.navigation.LightNavigationManager,
				java.util.List,
				java.util.Iterator,
				it.eng.spagobi.commons.bo.Role,
				java.util.Map,
				java.util.Set" %>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.engines.dossier.constants.DossierConstants"%>

<%
   SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute(DossierConstants.DOSSIER_MANAGEMENT_MODULE); 
   Map parnamemap = (Map)moduleResponse.getAttribute("parnamemap");
   Map parvaluemap = (Map)moduleResponse.getAttribute("parvaluemap");
   String description = (String)moduleResponse.getAttribute("description");
   String label = (String)moduleResponse.getAttribute("label");
   String name = (String)moduleResponse.getAttribute("name");
   //Integer idobj = (Integer)moduleResponse.getAttribute("idobj");
   String tempFolderPath = (String) moduleResponse.getAttribute(DossierConstants.DOSSIER_TEMP_FOLDER);
   String logicalname = (String)moduleResponse.getAttribute("logicalname");
   if(logicalname==null)
	   logicalname = "";
   
   Map backUrlPars = new HashMap();
   backUrlPars.put("PAGE", DossierConstants.DOSSIER_MANAGEMENT_PAGE);
   backUrlPars.put(SpagoBIConstants.OPERATION, DossierConstants.OPERATION_DETAIL_DOSSIER);
   backUrlPars.put(DossierConstants.DOSSIER_TEMP_FOLDER, tempFolderPath);
   backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
   String backUrl = urlBuilder.getUrl(request, backUrlPars);
   
   Map formSaveConfDocUrlPars = new HashMap();
   formSaveConfDocUrlPars.put("PAGE", DossierConstants.DOSSIER_MANAGEMENT_PAGE);
   formSaveConfDocUrlPars.put("OPERATION", DossierConstants.OPERATION_SAVE_CONFIGURED_DOCUMENT);
   formSaveConfDocUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
   String formSaveConfDocUrl = urlBuilder.getUrl(request, formSaveConfDocUrlPars);
   
%>

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
			<a href="javascript:document.getElementById('saveForm').submit();"> 
      			<img class='header-button-image-portlet-section' 
      				 title='<spagobi:message key = "dossier.save" bundle="component_dossier_messages" />' 
      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/save32.png", currTheme)%>' 
      				 alt='<spagobi:message key = "dossier.save" bundle="component_dossier_messages" />' />
			</a>
		</td>
	</tr>
</table>



	
	
<form action="<%=formSaveConfDocUrl%>" method='POST' id='saveForm' name='saveForm'>	
	
	<div style='padding-top:10px;margin-right:5px;' class='portlet-section-header' style="width:100%;">	
		<spagobi:message key="dossier.dataObject" bundle="component_dossier_messages" />
	</div>
	
	<input name="<%=DossierConstants.DOSSIER_TEMP_FOLDER%>" type="hidden" value="<%=tempFolderPath%>"/>
	<%--
	<input name="idbiobject" type="hidden" value="<%=idobj%>"/>
	--%>
	<input name="biobject_label" type="hidden" value="<%=StringEscapeUtils.escapeHtml(label)%>"/>
	<br/> 
	
	<div class="div_detail_area_forms" >
		<table style="margin:10px;">
			<tr>
				<td class='portlet-form-field-label' width="130px">
						<spagobi:message key="dossier.nameObject" bundle="component_dossier_messages" />
				</td>
				<td style="font-size:11px;"><%=StringEscapeUtils.escapeHtml(name) %></td>
			</tr>
			<tr>
				<td class='portlet-form-field-label' width="130px">
					<spagobi:message key="dossier.descrObject" bundle="component_dossier_messages" />
				</td>
				<td style="font-size:11px;"><%=StringEscapeUtils.escapeHtml(description) %></td>
			</tr>
			<tr>
				<td class='portlet-form-field-label' width="130px">
					<spagobi:message key="dossier.labelObject" bundle="component_dossier_messages" />
				</td>
				<td style="font-size:11px;"><%=StringEscapeUtils.escapeHtml(label) %></td>
			</tr>
			<tr><td colspan="2">&nbsp;</td></tr>
			<%
				String readonlyLogicalName = " ";
				if(!logicalname.trim().equals("")) {
					readonlyLogicalName = " readonly ";
				}
			%>
			<tr>
				<td class='portlet-form-field-label' width="130px">
						<spagobi:message key="dossier.logNameObject" bundle="component_dossier_messages" />
				</td>
				<td style="font-size:5;">
					<input type="text" size="30" name="logicalname" value="<%=StringEscapeUtils.escapeHtml(logicalname)%>" <%=readonlyLogicalName %> />
				</td>
			</tr>
		</table>
	</div>



    <spagobi:error/>

	
	<div style='padding-top:10px;margin-right:5px;' class='portlet-section-header' style="width:100%;">	
		<spagobi:message key="dossier.parametersObject" bundle="component_dossier_messages" />
	</div>
	
	<br/>
	
	<div class="div_detail_area_forms" >
		<table style="margin:10px;">
		<%
			Set names = parnamemap.keySet();
			Iterator iterParName = names.iterator();
			boolean findOutPar = false;
			while(iterParName.hasNext()){
				String parName = (String)iterParName.next();
				String urlName = (String)parnamemap.get(parName);
				String value = (String)parvaluemap.get(urlName);
				String readonly=" ";
				if(urlName.equalsIgnoreCase("outputType")){
					value="JPGBASE64";
					readonly = " readonly ";
					findOutPar = true;
				}
				
		%>
			<tr>
				<td class='portlet-form-field-label' width="160px"><%=StringEscapeUtils.escapeHtml(parName)%>:</td>
				<td><input type="text" size="30" name="<%=StringEscapeUtils.escapeHtml(urlName)%>" value="<%=StringEscapeUtils.escapeHtml(value)%>" <%=readonly%> /></td>
			</tr>
		<% 
			}
			if(!findOutPar){
		%>
			<tr>
				<td class='portlet-form-field-label' width="160px">Output:</td>
				<td><input type="text" size="30" name="outputType" value="JPGBASE64" readonly /></td>
			</tr>

		<%		
			}
		%>
		
		
		
		</table>
	</div>

<br/>
</br>


<br/>
</form>















