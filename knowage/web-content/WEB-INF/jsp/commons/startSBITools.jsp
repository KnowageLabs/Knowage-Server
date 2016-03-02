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


<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         import="it.eng.spagobi.commons.constants.SpagoBIConstants,
         		 it.eng.spago.configuration.ConfigSingleton,
                 it.eng.spago.base.SourceBean,
                 javax.portlet.PortletURL,
                 it.eng.spago.navigation.LightNavigationManager" %>
<%@page import="it.eng.spago.base.RequestContainer"%>
<%@page import="it.eng.spago.base.SessionContainer"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>

<%@ taglib uri='http://java.sun.com/portlet' prefix='portlet'%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>


<portlet:defineObjects/>

<%
	PortletURL scheduleUrl = renderResponse.createActionURL();
	scheduleUrl.setParameter("PAGE", "JobManagementPage");
	scheduleUrl.setParameter(SpagoBIConstants.MESSAGEDET, SpagoBIConstants.MESSAGE_GET_ALL_JOBS);

%>



<div class="div_background">
    <br/>	
	<table>
		<% if (userProfile.isAbleToExecuteAction(SpagoBIConstants.IMPORT_EXPORT_MANAGEMENT)) {%>
			<tr class="portlet-font" vAlign="middle">
				<td width="100" align="center">
					<img src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/tools/importexport/importexport64.png", currTheme)%>' />
				</td>
				<td width="20">
					&nbsp;
				</td>
				<td vAlign="middle">
				    <br/> 
					<a href='<portlet:actionURL>
									<portlet:param name="PAGE" value="BIObjectsPage"/>
									<portlet:param name="OPERATION" value="<%=SpagoBIConstants.IMPORTEXPORT_OPERATION %>"/>
									<portlet:param name="OBJECTS_VIEW" value="<%=SpagoBIConstants.VIEW_OBJECTS_AS_TREE%>"/>
							</portlet:actionURL>' 
						class="link_main_menu" >
						<spagobi:message key = "SBISet.importexport" />
					</a>
				</td>
			</tr>
		<%} %>
		<% if (userProfile.isAbleToExecuteAction(SpagoBIConstants.SCHEDULER_MANAGEMENT)) {%>
			<tr class="portlet-font" vAlign="middle">
				<td width="100" align="center">
					<img src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/tools/scheduler/scheduleIcon64_blu.png", currTheme)%>' />
				</td>
				<td width="20">
					&nbsp;
				</td>
				<td vAlign="middle">
				    <br/> 
					<a href='<%=scheduleUrl.toString()%>' 
						class="link_main_menu" >
						<spagobi:message key = "scheduler.Schedule" />
					</a>
				</td>
			</tr>
		<%} %>
	</table>
	<br/>
</div>
<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>

