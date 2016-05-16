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

<% 
	String contextName = request.getParameter(SpagoBIConstants.SBI_CONTEXT); 
%>

<%
// check for user profile autorization
// 		IEngUserProfile userProfile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		boolean canSee=false,canSeeAdmin=false;
		if(UserUtilities.haveRoleAndAuthorization(userProfile, null, new String[]{SpagoBIConstants.MANAGE_GLOSSARY_TECHNICAL})){
			canSee=true;
		 canSeeAdmin=UserUtilities.haveRoleAndAuthorization(userProfile, SpagoBIConstants.ADMIN_ROLE_TYPE, new String[]{SpagoBIConstants.MANAGE_GLOSSARY_TECHNICAL});
		}
// 		System.out.println("User canSee? -------> "+canSee);
// 		System.out.println("User canSeeAdmin? -------> "+canSeeAdmin);
%>

<% if(canSee ){ %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="glossaryTecnicalFunctionality">

<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 

<!-- glossary tree -->
<link rel="stylesheet" type="text/css" href="/knowage/themes/glossary/css/tree-style.css">
<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/glossary/commons/GlossaryTree.js"></script>

<!-- document-viewer --> 
<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/commons/document-viewer/documentViewer.js"></script>

<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLinkByTheme(request, "/css/angularjs/glossary/glossaryTecCustomStyle.css", currTheme)%>">
<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLinkByTheme(request, "/css/angularjs/glossary/generalStyle.css", currTheme)%>">
<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/glossary/technicaluser/glossaryTec.js"></script>
<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/glossary/technicaluser/glossaryTec_BusinessClass.js"></script>
<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/glossary/technicaluser/glossaryTec_Table.js"></script>

</head>

<body >
	<div ng-controller="Controller_tec as global" layout-fill>
		<md-content class="glossaryTec"> 
			<md-tabs md-border-bottom>
				<md-tab label='{{translate.load("sbi.glossary.glossary");}}'
						md-on-select="global.init('glossTreePage')"> 
					<md-content class="abs100">
						<glossary-tree tree-id="GlossTree"
								tree-options=ctrl.TreeOptions show-root=false glossary={}
								show-select-glossary=false show-search-bar=true show-info=true></glossary-tree> 
					</md-content> 
				</md-tab> 
				<% if( canSeeAdmin){ %>
				<md-tab label='{{translate.load("sbi.generic.navigation");}}' md-on-select="global.init('navigation')"> 
					<md-content	class="abs100"> 
					<%@include file="/WEB-INF/jsp/tools/glossary/technicaluser/glossary_navigation.jspf"%>
					</md-content> 
				</md-tab> 
				<%} %> 
				<md-tab label='{{translate.load("sbi.generic.document.management");}}'
						md-on-select="global.init('docAssoc')"> 
					<md-content class="abs100">
					<%@include file="/WEB-INF/jsp/tools/glossary/technicaluser/documents_and_wordsAssociations.jspf"%>
					</md-content> 
				</md-tab> 
				<% if(canSeeAdmin){ %>
				<md-tab label='{{translate.load("sbi.generic.dataset.management");}}' md-on-select="global.init('datasetAssoc')">
					<md-content class="abs100">
					<%@include file="/WEB-INF/jsp/tools/glossary/technicaluser/dataset_and_wordsAssociations.jspf"%>
					</md-content> 
				</md-tab> 
				<%} %> 
				<% if(canSeeAdmin){ %>
				<md-tab label='{{translate.load("sbi.generic.table.management");}}' ">
					<md-content class="abs100">
					<%@include file="/WEB-INF/jsp/tools/glossary/technicaluser/table_and_wordsAssociations.jspf"%>
					</md-content> 
				</md-tab> 
				<%} %> 
				<% if(canSeeAdmin){ %>
				<md-tab label='{{translate.load("sbi.generic.businessclass.management");}}'">
					<md-content class="abs100">
					<%@include file="/WEB-INF/jsp/tools/glossary/technicaluser/business_class_and_wordsAssociations.jspf"%>
					</md-content> 
				</md-tab> 
				<%} %> 
			</md-tabs> 
		</md-content>
	</div>
</body>
</html>

<%}else{ %>
UNAUTHORIZED
<%} %>






