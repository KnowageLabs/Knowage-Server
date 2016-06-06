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

	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js"></script>
	<!-- 	breadCrumb -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/BreadCrumb.js"></script>
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/glossary/css/bread-crumb.css">
	
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/documentbrowser/directive/document-view/documentView.js"></script>
	
	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/documentbrowser/documentBrowserNavigation.js"></script>
	
	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/documentbrowser/documentBrowser.js"></script>
	
	
	 
</head>

<body   ng-app="documentBrowserModule" id="ng-app" class="kn-documentBrowser" ng-controller="documentBrowserNavigationController">

	<md-tabs layout-fill md-autoselect class="documentNavigationToolbar secondaryToolbar" md-selected="documentNavigationToolbarSelectedIndex">
	<md-tab >
	 <md-tab-label><md-icon class="documentBrowserIcon" md-font-icon="fa  fa-folder-open-o " ></md-icon></md-tab-label>
	 <md-tab-body>
	  <%@ include file="/WEB-INF/jsp/tools/documentbrowser/templates/documentBrowserTemplate.jsp"%>
	 </md-tab-body>
	 </md-tab>
	 
	 
	 <md-tab ng-repeat="doc in runningDocuments">
	 <md-tab-label>
	 {{doc.name}}
<!-- 	  <md-button class="md-icon-button tabCloseButton" aria-label="close document" ng-click="removeDocumentFromList(doc.id)"> -->
<!--             <md-icon md-font-icon="fa fa-times"></md-icon> -->
<!--       </md-button> -->
	 </md-tab-label>
	 <md-tab-body> 
	 <iframe layout-fill class=" noBorder" ng-src="{{doc.url}}"> </iframe>
	 
	 </md-tab-body>
	 </md-tab>
	</md-tabs>
	
	 
		<md-button  md-tab-fixed-first-document-browser class="documentBrowserTabButton " aria-label="close document" ng-class="{'selectedDocumentBrowserTabButton' : documentNavigationToolbarSelectedIndex==0}" ng-click="documentNavigationToolbarSelectedIndex=0">
            <md-icon md-font-icon="fa fa-folder-open-o"></md-icon>
     	</md-button> 
     	

     	<md-menu md-tab-fixed-last-clear-tabs >
	      <md-button aria-label="Create new document" class="documentBrowserClearButton" ng-click="$mdOpenMenu($event)" ng-disabled="runningDocuments.length==0">
	        <md-icon md-menu-origin  md-font-icon="fa fa-times-circle-o" class="md-primary"></md-icon>
	      </md-button>
	      <md-menu-content width="4" class="documentBrowserDropdown"> 
	       <md-menu-item>
	           <md-button ng-click="closeTabs('current');">
	            <md-icon md-font-icon="fa fa-arrow-circle-o-down" md-menu-align-target></md-icon>
	             {{translate.load("sbi.browser.close.document.this")}}
	          </md-button>
	          </md-menu-item>
	         <md-menu-item>
	          <md-button ng-click="closeTabs('other');">
	            <md-icon md-font-icon="fa fa-times-circle-o" md-menu-align-target></md-icon>
	           {{translate.load("sbi.browser.close.document.other")}}
	          </md-button>
	        </md-menu-item>
	        <md-menu-item>
	          <md-button ng-click="closeTabs('right');">
	            <md-icon md-font-icon="fa fa-arrow-circle-o-right" md-menu-align-target></md-icon>
	          {{translate.load("sbi.browser.close.document.right")}}
	          </md-button>
	        </md-menu-item>
	         <md-menu-item>
	          <md-button ng-click="closeTabs('all');">
	            <md-icon md-font-icon="fa fa-times-circle-o" md-menu-align-target></md-icon>
	           {{translate.load("sbi.browser.close.document.all")}}
	          </md-button>
	        </md-menu-item>
		</md-menu-content>
		</md-menu>
     	
</body>
</html>
