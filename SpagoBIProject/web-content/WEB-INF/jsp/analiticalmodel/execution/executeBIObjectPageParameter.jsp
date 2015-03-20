<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 
  
<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>
<%@ taglib prefix="execution" tagdir="/WEB-INF/tags/analiticalmodel/execution" %>

<%@page import="it.eng.spagobi.commons.constants.ObjectsTreeConstants"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="it.eng.spagobi.commons.bo.Role"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance"%>
<%@page import="java.util.List"%>

<script type="text/javascript" src="<%=linkProto%>"></script>
<script type="text/javascript" src="<%=linkProtoWin%>"></script>
<script type="text/javascript" src="<%=linkProtoEff%>"></script>
<link href="<%=linkProtoDefThem%>" rel="stylesheet" type="text/css"/>
<link href="<%=linkProtoAlphaThem%>" rel="stylesheet" type="text/css"/>

<LINK rel='StyleSheet' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/analiticalmodel/portal_admin.css",currTheme)%>' type='text/css' />
<LINK rel='StyleSheet' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/analiticalmodel/form.css",currTheme)%>' type='text/css' />
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/analiticalmodel/execution/box.js")%>"></script>

<%
ExecutionInstance instance = contextManager.getExecutionInstance(ExecutionInstance.class.getName());
BIObject obj = instance.getBIObject();
//execution modality
String modality = instance.getExecutionModality();
if (modality == null) modality = "NORMAL_EXECUTION";

//execution role
String executionRole = instance.getExecutionRole();
Role executionRoleObj = DAOFactory.getRoleDAO().loadByName(executionRole);

List subobjectsList = null;
List snapshotsList = null;
List viewpointsList = null;
// loads subobjects/snapshots/viewpoints only if sliders are visible
if (executionRoleObj.isAbleToSeeSubobjects()) subobjectsList = DAOFactory.getSubObjectDAO().getAccessibleSubObjects(obj.getId(), userProfile);
if (executionRoleObj.isAbleToSeeSnapshots()) snapshotsList = DAOFactory.getSnapshotDAO().getSnapshots(obj.getId());
if (executionRoleObj.isAbleToSeeViewpoints()) viewpointsList = DAOFactory.getViewpointDAO().loadAccessibleViewpointsByObjId(obj.getId(), userProfile);
boolean subobjectsSliderVisible = subobjectsList != null && subobjectsList.size() > 0;
boolean snapshotsSliderVisible = snapshotsList != null && snapshotsList.size() > 0;
boolean viewpointsSliderVisible = viewpointsList != null && viewpointsList.size() > 0;

// identity string for object of the page
String uuid = instance.getExecutionId();

String title = obj.getName();

%>

<div class='execution-page-title'>
	<%=msgBuilder.getI18nMessage(title, request)%>
</div>

<spagobi:error/>

<div class="header">
	<div class="slider_header">
		<ul>
		    <li class="arrow"><a href="javascript:void(0);" id="toggle_Parameters<%= uuid %>" >&nbsp;<spagobi:message key='sbi.execution.parameters'/></a></li>
			<% if (viewpointsSliderVisible) { %><li class="arrow"><a href="javascript:void(0);" id="toggle_ViewPoint<%= uuid %>" >&nbsp;<spagobi:message key='sbi.execution.viewpoints'/></a></li><% } %>
			<li class="arrow"><a href="javascript:void(0);" id="toggle_SubObject<%= uuid %>" >&nbsp;<spagobi:message key='sbi.execution.subobjects'/></a></li>
			<% if (snapshotsSliderVisible) { %><li class="arrow"><a href="javascript:void(0);" id="toggle_Snapshot<%= uuid %>" >&nbsp;<spagobi:message key='sbi.execution.snapshots'/></a></li><% } %>
		</ul>
	</div>
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
		    <% } %>
		</ul>
	</div>
</div>

<%-- Parameters --%>
<div style="display:none">
	<div id="parametersContentEl<%= uuid %>">
		<spagobi:ParametersGenerator modality="EXECUTION_MODALITY"  requestIdentity="<%=uuid%>"/>
	</div>
</div>
<div id="popout_Parameters<%= uuid %>" class="popout"></div>
<script>
createToggledBox('<spagobi:message key='sbi.execution.parameters'/>:', 'parametersContentEl<%= uuid %>', 
		'popout_Parameters<%= uuid %>', 'toggle_Parameters<%= uuid %>', true);
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
createToggledBox('<spagobi:message key='sbi.execution.viewpoints'/>:', 'viewpointsContentEl<%= uuid %>', 
		'popout_ViewPoint<%= uuid %>', 'toggle_ViewPoint<%= uuid %>', true);
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
createToggledBox('<spagobi:message key='sbi.execution.subobjects'/>:', 'subobjectsContentEl<%= uuid %>', 
		'popout_SubObject<%= uuid %>', 'toggle_SubObject<%= uuid %>', <%= subobjectsSliderVisible ? "true" : "false" %>);
</script>
<%-- End SubObjects --%>

<%-- Snapshots --%>
<% if (snapshotsSliderVisible) { %>
<div style="display:none">
	<div id="snapshotsContentEl<%= uuid %>">
		<%--<spagobi:snapshotsList snapshotsList="<%= snapshotsList %>" />--%>
		<execution:snapshotsList snapshotsList="<%= snapshotsList %>" uuid="<%=uuid%>" />
	</div>
</div>
<div id="popout_Snapshot<%= uuid %>" class="popout"></div>
<script>
createToggledBox('<spagobi:message key='sbi.execution.snapshots'/>:', 'snapshotsContentEl<%= uuid %>', 
		'popout_Snapshot<%= uuid %>', 'toggle_Snapshot<%= uuid %>', true);
</script>
<% } %>
<%-- End Snapshots --%>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
