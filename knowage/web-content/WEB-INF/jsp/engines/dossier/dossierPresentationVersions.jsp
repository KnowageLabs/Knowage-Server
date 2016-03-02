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
				it.eng.spagobi.engines.dossier.constants.DossierConstants,
				java.util.List,
				java.util.Iterator,
				it.eng.spagobi.commons.bo.Role,
				it.eng.spagobi.engines.dossier.bo.ConfiguredBIDocument,
				it.eng.spagobi.engines.dossier.bo.WorkflowConfiguration" %>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.engines.dossier.utils.DossierUtilities"%>
<%@page import="it.eng.spagobi.engines.dossier.bo.DossierPresentation"%>

<%
   SourceBean moduleResponse = (SourceBean)aServiceResponse.getAttribute("ExecuteBIObjectModule"); 
   if(moduleResponse==null){
	   moduleResponse = (SourceBean)aServiceResponse.getAttribute(DossierConstants.DOSSIER_COLLABORATION_MODULE); 
   }
   if(moduleResponse==null){
	   moduleResponse = (SourceBean)aServiceResponse.getAttribute(DossierConstants.DOSSIER_REFRESH_MODULE); 
   }
   
   List presVersions = (List)moduleResponse.getAttribute(DossierConstants.DOSSIER_PRESENTATION_VERSIONS);
   String dossierIdStr = (String)moduleResponse.getAttribute(DossierConstants.DOSSIER_ID);
   
   Map backUrlPars = new HashMap();
   backUrlPars.put("LIGHT_NAVIGATOR_BACK_TO", "1");
   String backUrl = urlBuilder.getUrl(request, backUrlPars);
   
   Map refreshUrlPars = new HashMap();
   refreshUrlPars.put("PAGE", DossierConstants.DOSSIER_REFRESH_PAGE);
   refreshUrlPars.put(DossierConstants.DOSSIER_ID, dossierIdStr);
   String refreshUrl = urlBuilder.getUrl(request, refreshUrlPars);
   
   Map runCollaborationUrlPars = new HashMap();
   runCollaborationUrlPars.put("PAGE", DossierConstants.DOSSIER_COLLABORATION_PAGE);
   runCollaborationUrlPars.put("OPERATION", DossierConstants.OPERATION_RUN_NEW_COLLABORATION);
   runCollaborationUrlPars.put(DossierConstants.DOSSIER_ID, dossierIdStr);
   runCollaborationUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
   String runCollaborationUrl = urlBuilder.getUrl(request, runCollaborationUrlPars);
   
   Map eraseVersionUrlPars = new HashMap();
   eraseVersionUrlPars.put("PAGE", DossierConstants.DOSSIER_COLLABORATION_PAGE);
   eraseVersionUrlPars.put("OPERATION", DossierConstants.OPERATION_DELETE_PRESENTATION_VERSION);
   eraseVersionUrlPars.put(DossierConstants.DOSSIER_ID, dossierIdStr);
   eraseVersionUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
   String eraseVersionUrl = urlBuilder.getUrl(request, eraseVersionUrlPars);
   
   Map publishUrlPars = new HashMap();
   publishUrlPars.put("PAGE", DossierConstants.DOSSIER_COLLABORATION_PAGE);
   publishUrlPars.put("OPERATION", DossierConstants.OPERATION_PREPARE_PUBLISH_PRESENTATION_PAGE);
   publishUrlPars.put(DossierConstants.DOSSIER_ID, dossierIdStr);
   String publishUrl = urlBuilder.getUrl(request, publishUrlPars);
   
   String downloadVersionUrl = DossierUtilities.getDossierServiceUrl(request) + "&" +
   							   DossierConstants.DOSSIER_SERVICE_TASK + "=" + 
   							   DossierConstants.DOSSIER_SERVICE_TASK_DOWN_PRESENTATION_VERSION + "&" +
		                       DossierConstants.DOSSIER_ID+"="+dossierIdStr + "&" + LightNavigationManager.LIGHT_NAVIGATOR_DISABLED + "=true";
   
%>




	<!-- ********************* TITOLO **************************  -->

<table class='header-table-portlet-section'>
		<tr class='header-row-portlet-section'>
			<td class='header-title-column-portlet-section' style='vertical-align:middle;padding-left:5px;'>
				<spagobi:message key="dossier.Execution" bundle="component_dossier_messages" />
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
			<td class='header-button-column-portlet-section'>
				<a href='<%= refreshUrl%>'> 
	      			<img class='header-button-image-portlet-section' 
	      				 title='<spagobi:message key = "dossier.refresh" bundle="component_dossier_messages" />' 
	      				 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/refresh.gif", currTheme)%>' 
	      				 alt='<spagobi:message key = "dossier.refresh"  bundle="component_dossier_messages"/>' />
				</a>
			</td>
			
		</tr>
	</table>
	
	
	
	<br/>
	<br/>
	
	
	<div style="float:left;" class='portlet-form-field-label'>
		&nbsp;&nbsp;&nbsp;<spagobi:message key = "dossier.StartDossierDiscussion"  bundle="component_dossier_messages"/> ...
	</div>
	<div style="float:left;padding-left:15px;">
		<a href='<%= runCollaborationUrl %>'> 
	    <img class='header-button-image-portlet-section' 
	    	 title='<spagobi:message key = "dossier.StartDossierDiscussion" bundle="component_dossier_messages" />' 
	    	 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/exec.jpg", currTheme)%>' 
	    	 alt='<spagobi:message key = "dossier.StartDossierDiscussion"  bundle="component_dossier_messages"/>' />
		</a>
	</div>
	<div style="clear:left;">&nbsp;</div>

	
	
	<div style='width:100%;visibility:visible;' 
		 class='UITabs' 
		 id='presapprov' 
		 name='presapprov'>
		<div class="first-tab-level" style="background-color:#f8f8f8">
			<div style="overflow: hidden; width:100%">
				<div class='tab'>
					<spagobi:message key = "dossier.ApprovedPresentations"  bundle="component_dossier_messages"/>
				</div>
			</div>
		</div>
	</div>
	
	
	<table style='width:100%;'> 
	     <tr>
	       <td style='vertical-align:middle;' align="left" class="portlet-section-header">
	           <spagobi:message key = "dossier.PresentationName"  bundle="component_dossier_messages"/>
	       </td>
	       <td align="left" class="portlet-section-header">&nbsp;</td>
	       <td style='vertical-align:middle;' align="left" class="portlet-section-header">
	          <spagobi:message key = "dossier.CreationDate"  bundle="component_dossier_messages"/>
	       </td>
	       <td align="left" class="portlet-section-header">&nbsp;</td>
	       <td align="center" style='vertical-align:middle;' align="left" class="portlet-section-header">
	          <spagobi:message key = "dossier.VersionName"  bundle="component_dossier_messages"/>
	       </td>
	       <td align="left" class="portlet-section-header">&nbsp;</td>
	       <td align="center" style='vertical-align:middle;' align="center" class="portlet-section-header">
	          <spagobi:message key = "dossier.Approved"  bundle="component_dossier_messages"/>
	       </td>
	       <td align="left" class="portlet-section-header" colspan='4' >&nbsp;</td>
	     </tr>
	     <tr> 
		 <% Iterator iterPresVersions =  presVersions.iterator();
     		boolean alternate = false;
			String rowClass = "";
			while(iterPresVersions.hasNext()) {
				DossierPresentation presVer = (DossierPresentation)iterPresVersions.next();
				rowClass = (alternate) ? "portlet-section-alternate" : "portlet-section-body";
				alternate = !alternate;
		 %>
         <tr class='portlet-font'>
         	<td style='vertical-align:middle;' class='<%= rowClass %>'>
           		<%= presVer.getName() %>
            </td>
            <td class='<%= rowClass %>' width="20px">&nbsp;</td> 
            <td style='vertical-align:middle;' class='<%= rowClass %>' >
            	<%= presVer.getCreationDate() %>
            </td>
            <td class='<%= rowClass %>' width="20px">&nbsp;</td> 
            <td align="center" style='vertical-align:middle;' class='<%= rowClass %>' >
            	<%= presVer.getProg().toString() %>
            </td>
            <td class='<%= rowClass %>' width="20px">&nbsp;</td>
            <td align="center" style='vertical-align:middle;' class='<%= rowClass %>' >
            	<% out.print(presVer.getApproved() != null && presVer.getApproved().booleanValue()); %>
            </td> 
            <td class='<%= rowClass %>' width="20px">&nbsp;</td> 
            <td style='vertical-align:middle;' class='<%= rowClass %>' width="40px">
                <a href='<%=eraseVersionUrl+"&"+DossierConstants.VERSION_ID+"="+presVer.getProg()%>'> 
			    <img title='<spagobi:message key = "dossier.erase" bundle="component_dossier_messages" />' 
			    	 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/erase.gif", currTheme)%>' 
			    	 alt='<spagobi:message key = "dossier.erase"  bundle="component_dossier_messages"/>' />
				</a>
            </td>
            <td style='vertical-align:middle;' class='<%= rowClass %>' width="40px">
                <a href='<%=downloadVersionUrl + "&" + DossierConstants.VERSION_ID + "=" + presVer.getProg()%>'> 
			    <img title='<spagobi:message key = "dossier.download" bundle="component_dossier_messages" />' 
			    	 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/download16.gif", currTheme)%>' 
			    	 alt='<spagobi:message key = "dossier.download"  bundle="component_dossier_messages"/>' />
				</a>              		
            </td>
            <td style='vertical-align:middle;' class='<%= rowClass %>' width="40px">
            	<% if(presVer.getApproved() != null && presVer.getApproved().booleanValue()) { %>
                <a href='<%=publishUrl+"&"+DossierConstants.VERSION_ID+"="+presVer.getProg()%>'> 
			    <img title='<spagobi:message key = "dossier.deploy" bundle="component_dossier_messages" />' 
			    	 src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/dossier/deploy16.png", currTheme)%>' 
			    	 alt='<spagobi:message key = "dossier.deploy"  bundle="component_dossier_messages"/>' />
				</a>
				<% } else { out.print("&nbsp"); } %>        		
            </td>
         </tr> 
         <% } %>
    </table>          


	<br/>
	<br/>

