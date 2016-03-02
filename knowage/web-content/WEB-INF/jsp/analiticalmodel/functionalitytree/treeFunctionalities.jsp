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

<%@ page import="javax.portlet.PortletURL,
				it.eng.spago.navigation.LightNavigationManager" %>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.HashMap"%>

<%  
	Map backUrlPars = new HashMap();
	backUrlPars.put("ACTION_NAME", "START_ACTION");
	backUrlPars.put("PUBLISHER_NAME", "LoginSBIAnaliticalModelPublisher");
	backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_RESET, "true");
	String backUrl = urlBuilder.getUrl(request, backUrlPars);
%>


<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<table class='header-table-portlet-section'>
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section' style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "SBISet.treeFunct.title" />
		</td>
		<%
			if(ChannelUtilities.isPortletRunning()) {
		%>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%=backUrl%>'> 
      			<img class='header-button-image-portlet-section' 
      			     title='<spagobi:message key = "SBISet.treeFunct.backButt" />' 
      			     src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>' 
      			     alt='<spagobi:message key = "SBISet.treeFunct.backButt" />' />
			</a>
		</td>
	    <%
			}
	    %>
	</tr>
</table>

<div class="div_background">

<spagobi:treeObjects moduleName="TreeObjectsModule"  htmlGeneratorClass="it.eng.spagobi.analiticalmodel.functionalitytree.presentation.FunctionalitiesTreeHtmlGenerator" />

<br/>
<br/>
<br/>
<br/>
<br/>

</div>








