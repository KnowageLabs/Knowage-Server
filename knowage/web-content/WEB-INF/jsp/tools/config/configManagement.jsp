<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!doctype html>
<html ng-app="configManagementApp">

<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/config/configManagement.js"></script>
	
</head>

<body>
	<div ng-controller="Controller as ctrl" layout="column" layout-wrap layout-fill>
		<md-toolbar class="md-blue">
		    <div class="md-toolbar-tools">
		      <h2 class="md-flex">Configuration Management</h2>
		    </div>
		 </md-toolbar>
		 
		 <div layout="row" layoput-wrap>
				<md-button class="md-raised md-ExtraMini" ng-click="addRow()">{{translate.load("sbi.generic.add")}}</md-button>
				<md-button 	class="md-raised md-ExtraMini" ng-click="deleteRow()">{{translate.load("sbi.generic.delete")}}</md-button>
				<md-button 	class="md-raised md-ExtraMini" ng-click="editRow()">{{translate.load("sbi.generic.update2")}}</md-button>
		 </div>
		<md-content flex >
			<angular-table 
				id="table" ng-model="data" 
				columns='["label","name","valueCheck","valueTypeId","category","active"]'
				columns-search='["label","name","valueCheck","valueTypeId","category","active"]'
				highlights-selected-item = "true"
				show-search-bar="true"
				no-pagination="true"
				selected-item="itemSelected"
			></angular-table>
		</md-content>
	</div>
</body>
</html>