<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  


<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         import="it.eng.spagobi.commons.constants.SpagoBIConstants,
         		 it.eng.spago.configuration.ConfigSingleton,
                 it.eng.spago.base.SourceBean,
                 it.eng.spago.security.IEngUserProfile,
                 java.util.Collection,
                 java.util.Iterator" %>
<%@page import="it.eng.spago.base.RequestContainer"%>
<%@page import="it.eng.spago.base.SessionContainer"%>
<%@ taglib uri='http://java.sun.com/portlet' prefix='portlet'%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>


<portlet:defineObjects/>

<div class="div_background">
    <br/>	
	<table>
		<% if (userProfile.isAbleToExecuteAction(SpagoBIConstants.DISTRIBUTIONLIST_USER)) {%>
			<tr class="portlet-font" vAlign="middle">
				<td width="100" align="center">
					<img src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/tools/distributionlist/distributionlistuser.png", currTheme)%>' />
				</td>
				<td width="20">
					&nbsp;
				</td>
				<td vAlign="middle">
					    <br/> 
						<a href='<portlet:actionURL> 
						        <portlet:param name="PAGE" value="ListDistributionListUserPage"/>
								</portlet:actionURL>' 
							class="link_main_menu" >
						 	<spagobi:message key="SBISet.linkDLUConf" />
						</a>
					</td>
			</tr>
		<%} %>
		<% if (userProfile.isAbleToExecuteAction(SpagoBIConstants.DISTRIBUTIONLIST_MANAGEMENT)) {%>		
			<tr class="portlet-font">
					<td width="100" align="center">
						<img src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/tools/distributionlist/distributionlist.gif", currTheme)%>' />
					</td>
					<td width="20">
						&nbsp;
					</td>
					<td vAlign="middle">
					    <br/> 
						<a href='<portlet:actionURL><portlet:param name="PAGE" value="ListDistributionListPage"/></portlet:actionURL>' 
							class="link_main_menu" >
						 	<spagobi:message key="SBISet.linkDLConf" />
						</a>
					</td>
			</tr>
		<%} %>
	</table>
	<br/>
</div>
<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
