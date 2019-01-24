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

<%@ page language="java" pageEncoding="utf-8" session="true"%>
<%@ page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<% String contextName = request.getParameter(SpagoBIConstants.SBI_CONTEXT); %>

<%
		//TODO check for user profile autorization
		boolean canSee=false,canSeeAdmin=false;
		if(UserUtilities.haveRoleAndAuthorization(userProfile, null, new String[]{SpagoBIConstants.MANAGE_CROSS_NAVIGATION})){
			canSee=true;
		 canSeeAdmin=UserUtilities.haveRoleAndAuthorization(userProfile, SpagoBIConstants.ADMIN_ROLE_TYPE, new String[]{SpagoBIConstants.MANAGE_CROSS_NAVIGATION});
		}
%>

<% if(canSee ){ %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html >

<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/news/newsManagement.js")%>"></script>
	
	</head>

<body class="kn-news-management" ng-app="newsManagement" id="ng-app">
	<angular-list-detail ng-controller="newsManagementController" full-screen="false"  >
       <list label="News Management" new-function="newNews">

		</list>
		
        <detail label="{{selectedNews.title || 'New News'}}" save-function="saveFunc" cancel-function="cancelFunc">
			
		</detail>
	</angular-list-detail>
</body>
</html>

<%}else{ %>
Access Denied
<%} %>

