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
along with this program.  If not, see <http://www.gnu.org/licenses/>. --%>
 
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="org.springframework.security.ui.AbstractProcessingFilter"%>
<jsp:directive.include file="/WEB-INF/view/jsp/default/ui/includes/top.jsp" />
<div class="errors" id="status">
	<h2>Authorization Failure</h2>
	<p>You are not authorized to use this application for the following reason: 
	<%final Exception e = (Exception) request.getSession().getAttribute(AbstractProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY);%>
	<%=e.getMessage()%>.
	</p>
	<p>
</div>
<jsp:directive.include file="/WEB-INF/view/jsp/default/ui/includes/bottom.jsp" />