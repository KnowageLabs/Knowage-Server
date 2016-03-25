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


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!doctype html>
<html ng-app="domainManagementApp">

<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 
	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/domain/domainManagement.js"></script>
	
</head>

<body>
	<div ng-controller="Controller as ctrl" layout="column" layout-wrap layout-fill>
		<md-toolbar class="md-blue">
			    <div class="md-toolbar-tools">
			      <h2 class="md-flex">Domain Management</h2>
			    </div>
		</md-toolbar>
	 
		 <div  layout="row" layoput-wrap >
				<md-button class="md-raised" ng-click="addRow()">{{translate.load("sbi.generic.add")}}</md-button>
				<md-button 	class="md-raised" ng-click="deleteRow()">{{translate.load("sbi.generic.delete")}}</md-button>
				<md-button 	class="md-raised " ng-click="editRow()">{{translate.load("sbi.generic.update2")}}</md-button>
		 </div>
			
		  
				<angular-table flex
					id="table" ng-model="data" 
					columns='["valueCd","valueName","domainCode","domainName","valueDescription"]'
					columns-search='["valueCd","valueName","domainCode","domainName","valueDescription"]'
					highlights-selected-item = "true"
					show-search-bar="true"
					no-pagination="true"
					selected-item="itemSelected"
				></angular-table>
	  
	</div>
</body>
</html>
