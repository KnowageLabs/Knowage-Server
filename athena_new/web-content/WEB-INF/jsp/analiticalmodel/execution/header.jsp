<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 

<%@ taglib prefix="execution" tagdir="/WEB-INF/tags/analiticalmodel/execution" %>

<%@page import="it.eng.spagobi.commons.constants.ObjectsTreeConstants"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
<%@page import="org.safehaus.uuid.UUID"%>
<%@page import="org.safehaus.uuid.UUIDGenerator"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.SubObject"%>
<%@page import="java.util.Map"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="it.eng.spagobi.monitoring.dao.AuditManager"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="java.util.List"%>
<%@page import="it.eng.spagobi.commons.utilities.ParameterValuesEncoder"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter"%>
<%@page import="it.eng.spago.base.SourceBean"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="it.eng.spagobi.commons.bo.Role"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.Snapshot"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.handlers.ExecutionManager"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.handlers.BIObjectNotesManager"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.service.ExecuteBIObjectModule"%>

<%@page import="it.eng.spago.base.RequestContainer"%>
<%@page import="it.eng.spago.base.SessionContainer"%>
<%@page import="it.eng.spagobi.engines.config.dao.IEngineDAO"%>
<%@page import="it.eng.spagobi.engines.config.bo.Engine"%>
<%@page import="java.util.ArrayList"%>
<%@page import="it.eng.spagobi.commons.metadata.SbiDomains"%>
<%@page import="it.eng.spagobi.engines.config.metadata.SbiExporters"%>
<%@page import="it.eng.spagobi.engines.config.bo.Exporters"%>
<%@page import="it.eng.spagobi.commons.bo.Domain"%>
<%@page import="it.eng.spagobi.commons.dao.IDomainDAO"%>
<LINK rel='StyleSheet' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/analiticalmodel/portal_admin.css",currTheme)%>' type='text/css' />
<LINK rel='StyleSheet' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/analiticalmodel/form.css",currTheme)%>' type='text/css' />
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/analiticalmodel/execution/box.js")%>"></script>

<%--
boolean areAllParametersTransient(List parametersList) {
	boolean toReturn = true;
	if (parametersList != null && parametersList.size() > 0) {
		for (int i = 0; i < parametersList.size(); i++) {
			BIObjectParameter parameter = (BIObjectParameter) parametersList.get(i);
			if (!parameter.isTransientParmeters()) {
				toReturn = false;
				break;
			}
		}
	}
	return toReturn;
}
--%>

<%!
// get the virtual role (a role that containes all permissions of the correct execution roles)
Role getVirtualRole(IEngUserProfile profile, BIObject obj, String baseRoleName) throws Exception {
	
	Role virtualRole = new Role(baseRoleName, "");
	virtualRole.setIsAbleToSeeSubobjects(false);
	virtualRole.setIsAbleToSeeSnapshots(false);
	virtualRole.setIsAbleToSeeViewpoints(false);
	virtualRole.setIsAbleToSeeMetadata(false);
	virtualRole.setIsAbleToSendMail(false);
	virtualRole.setIsAbleToSeeNotes(false);
	virtualRole.setIsAbleToSaveRememberMe(false);
	virtualRole.setIsAbleToSaveIntoPersonalFolder(false);
	
	List correctRoles = null;
	if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV)
			|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_USER)
			|| profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN))
		correctRoles = DAOFactory.getBIObjectDAO()
				.getCorrectRolesForExecution(obj.getId(), profile);
	else
		correctRoles = DAOFactory.getBIObjectDAO()
				.getCorrectRolesForExecution(obj.getId());
	if (correctRoles == null || correctRoles.size() == 0) {
		throw new Exception("No correct roles for execution!!!!");
	}
	Iterator it = correctRoles.iterator();
	while (it.hasNext()) {
		String roleName = (String) it.next();
		Role anotherRole = DAOFactory.getRoleDAO().loadByName(roleName);
		if (anotherRole.isAbleToSeeViewpoints()) virtualRole.setIsAbleToSeeSubobjects(true);
		if (anotherRole.isAbleToSeeSnapshots()) virtualRole.setIsAbleToSeeSnapshots(true);
		if (anotherRole.isAbleToSeeViewpoints()) virtualRole.setIsAbleToSeeViewpoints(true);
		if (anotherRole.isAbleToSeeMetadata()) virtualRole.setIsAbleToSeeMetadata(true);
		if (anotherRole.isAbleToSendMail()) virtualRole.setIsAbleToSendMail(true);
		if (anotherRole.isAbleToSeeNotes()) virtualRole.setIsAbleToSeeNotes(true);
		if (anotherRole.isAbleToSaveRememberMe()) virtualRole.setIsAbleToSaveRememberMe(true);
		if (anotherRole.isAbleToSaveIntoPersonalFolder()) virtualRole.setIsAbleToSaveIntoPersonalFolder(true);
	}
	return virtualRole;
}
%>

<%
//identity string for object of the page
ExecutionInstance instance = contextManager.getExecutionInstance(ExecutionInstance.class.getName());
String uuid = instance.getExecutionId();

BIObject obj = instance.getBIObject();
BIObjectNotesManager objectNManager = new BIObjectNotesManager();
String execIdentifier = objectNManager.getExecutionIdentifier(obj);

//get module response, subobject, parameters map
SourceBean moduleResponse = (SourceBean) aServiceResponse.getAttribute("ExecuteBIObjectModule");
SubObject subObj = (SubObject) moduleResponse.getAttribute(SpagoBIConstants.SUBOBJECT);
boolean isExecutingSubObject = subObj != null;
Snapshot snapshot = (Snapshot) moduleResponse.getAttribute(SpagoBIConstants.SNAPSHOT);
boolean isExecutingSnapshot = snapshot != null;
Map documentParametersMap = (Map) moduleResponse.getAttribute(ObjectsTreeConstants.REPORT_CALL_URL);

String title = obj.getName();

RequestContainer reqCont = RequestContainer.getRequestContainer();
SessionContainer sessCont = reqCont.getSessionContainer();
SessionContainer permSess = sessCont.getPermanentContainer();
String language=(String)permSess.getAttribute(SpagoBIConstants.AF_LANGUAGE);
String country=(String)permSess.getAttribute(SpagoBIConstants.AF_COUNTRY);

Map executionParameters = new HashMap();
if (documentParametersMap != null) executionParameters.putAll(documentParametersMap);
executionParameters.put(SpagoBIConstants.SBI_CONTEXT, GeneralUtilities.getSpagoBiContext());
//executionParameters.put(SpagoBIConstants.SBI_BACK_END_HOST, GeneralUtilities.getSpagoBiHostBackEnd());
executionParameters.put(SpagoBIConstants.SBI_HOST, GeneralUtilities.getSpagoBiHost());
executionParameters.put("SBI_EXECUTION_ID", instance.getExecutionId());
executionParameters.put(SpagoBIConstants.EXECUTION_ROLE, instance.getExecutionRole());

if(language!=null && country!=null){
	executionParameters.put(SpagoBIConstants.SBI_LANGUAGE, language);
	executionParameters.put(SpagoBIConstants.SBI_COUNTRY, country);	
}

// Auditing
AuditManager auditManager = AuditManager.getInstance();
String modality = instance.getExecutionModality();

// execution role
String executionRole = instance.getExecutionRole();
Role virtualRole = getVirtualRole(userProfile, obj, executionRole);

Integer executionAuditId = (Integer)moduleResponse.getAttribute(AuditManager.AUDIT_ID);
if(executionAuditId==null){
    executionAuditId = auditManager.insertAudit(obj, subObj, userProfile, executionRole, modality);
}
// adding parameters for AUDIT updating
if (executionAuditId != null) {
	executionParameters.put(AuditManager.AUDIT_ID, executionAuditId.toString());
}
Map refreshUrlPars = new HashMap();
refreshUrlPars.put("PAGE", "ExecuteBIObjectPage");
refreshUrlPars.put(SpagoBIConstants.MESSAGEDET, SpagoBIConstants.EXEC_PHASE_REFRESH);
refreshUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
if (subObj != null) {
	refreshUrlPars.put(SpagoBIConstants.SUBOBJECT_ID, subObj.getId().toString());
}
String refreshUrl = urlBuilder.getUrl(request, refreshUrlPars);


// the toolbar (slider + buttons) visibility is determined by preferences
boolean toolbarIsVisible = instance.displayToolbar();
boolean sliderIsVisible = instance.displaySliders();
List subobjectsList = null;
List snapshotsList = null;
List viewpointsList = null;
if (sliderIsVisible) {
	// loads subobjects/snapshots/viewpoints only if sliders are visible
	if (virtualRole.isAbleToSeeSubobjects()) subobjectsList = DAOFactory.getSubObjectDAO().getAccessibleSubObjects(obj.getId(), userProfile);
	if (virtualRole.isAbleToSeeSnapshots()) snapshotsList = DAOFactory.getSnapshotDAO().getSnapshots(obj.getId());
	if (virtualRole.isAbleToSeeViewpoints()) viewpointsList = DAOFactory.getViewpointDAO().loadAccessibleViewpointsByObjId(obj.getId(), userProfile);
}
boolean subobjectsSliderVisible = subobjectsList != null && subobjectsList.size() > 0;
boolean snapshotsSliderVisible = snapshotsList != null && snapshotsList.size() > 0;
boolean viewpointsSliderVisible = viewpointsList != null && viewpointsList.size() > 0;
String outputType = (String)executionParameters.get("outputType");
ExecutionManager executionManager = (ExecutionManager) contextManager.get(ExecutionManager.class.getName());
String executionFlowId = instance.getFlowId();

String titleVisibileStr = (String) aServiceRequest.getAttribute(SpagoBIConstants.TITLE_VISIBLE);
boolean titleVisibile = titleVisibileStr == null || titleVisibileStr.equalsIgnoreCase("TRUE");

if (titleVisibile) {
	%>
	<div class='execution-page-title'>
		<%
		if (!executionFlowId.equals(uuid) && executionManager != null) {
			List list = executionManager.getBIObjectsExecutionFlow(executionFlowId);
			for (int i = 0; i < list.size(); i++) {
				ExecutionInstance anInstance = (ExecutionInstance) list.get(i);
				BIObject aBIObject = anInstance.getBIObject();
				Map recoverExecutionParams = new HashMap();
				//recoverExecutionParams.put("PAGE", ExecuteBIObjectModule.MODULE_PAGE);
				recoverExecutionParams.put(ObjectsTreeConstants.ACTION, SpagoBIConstants.EXECUTE_DOCUMENT_ACTION);	
				recoverExecutionParams.put(SpagoBIConstants.MESSAGEDET, SpagoBIConstants.RECOVER_EXECUTION_FROM_CROSS_NAVIGATION);
				recoverExecutionParams.put("EXECUTION_FLOW_ID", anInstance.getFlowId());
				recoverExecutionParams.put("EXECUTION_ID", anInstance.getExecutionId());
				recoverExecutionParams.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "TRUE");
				String recoverExecutionUrl = urlBuilder.getUrl(request, recoverExecutionParams);
				%>&nbsp;<a href='<%= recoverExecutionUrl %>' ><%= aBIObject.getName()%></a>&nbsp;&gt;<%
			}
		}
		%>
			<%=msgBuilder.getI18nMessage(title,  request)
			
			%>
		
	</div>
	<%
}
%>
<script type="text/javascript">
		function changeDivDisplay(id,display){
			elem = document.getElementById(id);
 			elem.style.visibility = display;
		}
	
</script>
<%
if (toolbarIsVisible) {
	%>
	
	<div class="header">
		<% if (sliderIsVisible) { %>
		<div class="slider_header">
			<ul>
			    <li class="arrow"><a href="javascript:void(0);" id="toggle_Parameters<%= uuid %>" >&nbsp;<spagobi:message key='sbi.execution.parameters'/></a></li>
				<% if (viewpointsSliderVisible) { %><li class="arrow"><a href="javascript:void(0);" id="toggle_ViewPoint<%= uuid %>" >&nbsp;<spagobi:message key='sbi.execution.viewpoints'/></a></li><% } %>
				<li class="arrow" id="subobjectsSliderArrow<%= uuid %>"><a href="javascript:void(0);" id="toggle_SubObject<%= uuid %>" >&nbsp;<spagobi:message key='sbi.execution.subobjects'/></a></li>
				<% if (snapshotsSliderVisible) { %><li class="arrow"><a href="javascript:void(0);" id="toggle_Snapshot<%= uuid %>" >&nbsp;<spagobi:message key='sbi.execution.snapshots'/></a></li><% } %>
			</ul>
		</div>
		<% } %>
		<div class="toolbar_header">
			<ul>
				<% if (!modality.equalsIgnoreCase(SpagoBIConstants.SINGLE_OBJECT_EXECUTION_MODALITY)) { %>
			    <li>
			    	<%
			    	Map backUrlPars = new HashMap();
			    	backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
			    	%>
					<a href='<%= urlBuilder.getUrl(request, backUrlPars) %>'>
						<img width="22px" height="22px" title='<spagobi:message key = "SBIDev.docConf.execBIObjectParams.backButt" />'
							src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>'
							alt='<spagobi:message key = "SBIDev.docConf.execBIObjectParams.backButt" />' />
					</a>
			    </li>
			    <%
				}
			    %>
			
			</ul>
		
		
		
		</div>
	</div>
	
	<% if (sliderIsVisible) { %>
		<%-- Parameters --%>
		<div style="display:none">
			<div id="parametersContentEl<%= uuid %>">
				<spagobi:ParametersGenerator modality="EXECUTION_MODALITY"  requestIdentity="<%=uuid%>"/>
			</div>
		</div>
		<div id="popout_Parameters<%= uuid %>" class="popout"></div>
		<script>
		createToggledBox('<spagobi:message key='sbi.execution.parameters'/>:', 'parametersContentEl<%= uuid %>', 'popout_Parameters<%= uuid %>', 'toggle_Parameters<%= uuid %>', false);
		</script>
		<%-- End parameters --%>
		
		<%-- ViewPoints --%>
		<% if (viewpointsSliderVisible) { %>
		<div style="display:none">
			<div id="viewpointsContentEl<%= uuid %>">
				<execution:viewpointsList viewpointsList="<%= viewpointsList %>" uuid="<%=uuid%>" />
			</div>
		</div>
		<div id="popout_ViewPoint<%= uuid %>" class="popout"></div>
		<script>
		createToggledBox('<spagobi:message key='sbi.execution.viewpoints'/>:', 'viewpointsContentEl<%= uuid %>', 'popout_ViewPoint<%= uuid %>', 'toggle_ViewPoint<%= uuid %>', false);
		</script>
		<% } %>
		<%-- End viewPoints --%>
		
		<%-- SubObjects --%>
		<div style="display:none">
			<div id="subobjectsContentEl<%= uuid %>">
				<execution:subobjectsList subobjectsList="<%= subobjectsList %>" uuid="<%=uuid%>" />
			</div>
		</div>
		<div id="popout_SubObject<%= uuid %>" class="popout"></div>
		<script>
		createToggledBox('<spagobi:message key='sbi.execution.subobjects'/>:', 'subobjectsContentEl<%= uuid %>', 'popout_SubObject<%= uuid %>', 'toggle_SubObject<%= uuid %>', false);
		</script>
		<%-- End SubObjects --%>
		
		<%-- Snapshots --%>
		<% if (snapshotsSliderVisible) { %>
		<div style="display:none">
			<div id="snapshotsContentEl<%= uuid %>">
				<execution:snapshotsList snapshotsList="<%= snapshotsList %>" uuid="<%=uuid%>" />
			</div>
		</div>
		<div id="popout_Snapshot<%= uuid %>" class="popout"></div>
		<script>
		createToggledBox('<spagobi:message key='sbi.execution.snapshots'/>:', 'snapshotsContentEl<%= uuid %>', 'popout_Snapshot<%= uuid %>', 'toggle_Snapshot<%= uuid %>', false);
		</script>
		<% } %>
		<%-- End Snapshots --%>
	<% } %>
	
<%} %>
<spagobi:error/>