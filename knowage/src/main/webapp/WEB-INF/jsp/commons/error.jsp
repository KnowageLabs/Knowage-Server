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
 
  


<%@ include file="/WEB-INF/jsp/commons/portlet_base410.jsp"%>

<%@ page import="it.eng.spago.error.EMFErrorHandler, 
                 java.util.Collection, 
                 it.eng.spago.error.EMFAbstractError,
                 it.eng.spago.navigation.LightNavigationManager,
                 java.util.HashMap,
                 java.util.Set,
                 java.util.Iterator" %>
<%@page import="java.util.Map"%>


<%


    // recover error handler and error collection 
    EMFErrorHandler errorHandler = aResponseContainer.getErrorHandler();  
	Collection errors = errorHandler.getErrors();
	Iterator iter = errors.iterator();  

%>


<div>
	<md-toolbar class="md-knowage-theme">
		<div class="md-toolbar-tools">
			<h1>
				<spagobi:message key = "SBIErrorPage.title" />
			</h1>
		</div>
	</md-toolbar>
</div>




<div style='width:100%;display:flex;justify-content:center;align-items:center'>
	<div class="kn-error flex-50" style="font-size:.8rem">
    	<% 
    	while (iter.hasNext()) {
    		EMFAbstractError error = (EMFAbstractError) iter.next();
    		String description = error.getDescription();
    	%>
		<%= StringEscapeUtils.escapeHtml(description) %>
		<br/>
		<% } %>
	</div>
</div>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>