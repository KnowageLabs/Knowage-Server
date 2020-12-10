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

<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/jsp/wapp/homeBase.jsp"%>    


<%-- Javascript object useful for session expired management (see also sessionExpired.jsp) --%>
<script>
	sessionExpiredSpagoBIJS = 'sessionExpiredSpagoBIJS';
	var firstUrl =  '<%= StringEscapeUtils.escapeJavaScript(firstUrlToCall) %>';  
	firstUrlTocallvar = firstUrl;
</script>
<iframe src="<%= StringEscapeUtils.escapeHtml(firstUrlToCall) %>" id="iframeDoc" width="100%" height="100%" frameborder="0"></iframe>

<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/persist-0.1.0/persist.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/menu/menuAppAdmin.js")%>"></script> 

<!--  div id="goToLandscape">
	<i class="fa fa-4x fa-refresh"></i>
</div-->
<div data-ng-controller="menuCtrl" ng-app="menuAppAdmin" id="menuAppAdmin">
	<menu-aside></menu-aside>
</div>

<script>
	sessionExpiredSpagoBIJS = 'sessionExpiredSpagoBIJS';

</script>