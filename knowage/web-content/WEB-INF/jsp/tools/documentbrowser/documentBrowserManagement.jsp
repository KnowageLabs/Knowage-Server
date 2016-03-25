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

<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	
	<!-- Styles -->
	<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 

<!-- 	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/documentbrowser/md-data-table.min.js"></script> -->
	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js"></script>
	<!-- 	breadCrumb -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/BreadCrumb.js"></script>
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/glossary/css/bread-crumb.css">
	
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/documentbrowser/directive/document-view/documentView.js"></script>
	
	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/documentbrowser/documentBrowserNavigation.js"></script>
	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/documentbrowser/documentBrowser.js"></script>
	
	
	 
</head>

<body   ng-app="documentBrowserModule" id="ng-app" class="kn-documentBrowser" ng-controller="documentBrowserNavigationController">

	<md-tabs layout-fill md-autoselect class="documentNavigationToolbar" md-selected="documentNavigationToolbarSelectedIndex">
	<md-tab >
	 <md-tab-label><md-icon class="documentBrowserIcon" md-font-icon="fa  fa-folder-open-o " ></md-icon></md-tab-label>
	 <md-tab-body>
	  <%@ include file="/WEB-INF/jsp/tools/documentbrowser/templates/documentBrowserTemplate.jsp"%>
	 </md-tab-body>
	 </md-tab>
	 
	 
	 <md-tab ng-repeat="doc in runningDocuments">
	 <md-tab-label>
	 {{doc.label}}
	  <md-button class="md-icon-button tabCloseButton" aria-label="close document" ng-click="removeDocumentFromList(doc.id)">
            <md-icon md-font-icon="fa fa-times"></md-icon>
      </md-button>
	 </md-tab-label>
	 <md-tab-body> 
	 <iframe layout-fill class=" noBorder" ng-src="{{doc.url}}"> </iframe>
	 
	 </md-tab-body>
	 </md-tab>
	</md-tabs>
	
	 
		<md-button  md-tab-fixed-first class="documentBrowserTabButton " aria-label="close document" ng-class="{'selectedDocumentBrowserTabButton' : documentNavigationToolbarSelectedIndex==0}"ng-click="documentNavigationToolbarSelectedIndex=0">
            <md-icon md-font-icon="fa fa-folder-open-o"></md-icon>
     	</md-button> 
</body>
</html>
