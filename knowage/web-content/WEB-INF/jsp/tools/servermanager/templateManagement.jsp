<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="templateManagement">

<head>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<!-- non c'entra	<script type="text/javascript" src="/knowage/js/src/angular_1.4/tools/glossary/commons/LayerTree.js"></script> -->
<%-- breadCrumb --%>
<script type="text/javascript"	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/BreadCrumb.js"></script>
<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/glossary/css/bread-crumb.css">
<link rel="stylesheet" type="text/css" href="/knowage/themes/commons/css/customStyle.css">

<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/themes/importexport/css/importExportStyle.css">
<!-- controller -->
<script type="text/javascript"	src="/knowage/js/src/angular_1.4/tools/servermanager/templateManagement.js"></script>




</head>
<body class="bodyStyle">

	<div ng-controller="Controller " layout="column" layout-wrap>

		<md-toolbar class="miniheadimportexport">
		<div class="md-toolbar-tools">

			<h2 class="md-flex">{{translate.load("sbi.templatemanagemenent");}}</h2>
		</div>
		</md-toolbar>


		<md-content layout="column" layout-wrap >
		<div >
			<md-input-container class="small counter">
				<p><h4>{{translate.load("sbi.templatemanagemenent.firstmessage");}}</h4></p>
			</md-input-container>
		</div>
		<div layout="row" layout-wrap>
			<div flex=10>
				<md-input-container class="small counter">
				<h4>{{translate.load("sbi.templatemanagemenent.choosedate");}}:</h4>
				</md-input-container>
			</div>
			<div flex=20>
				<md-datepicker ng-model="dateSelected.data" name="Select Data"
					ng-change="parseDate()" md-placeholder={{translate.load("sbi.templatemanagemenent.selectdata");}} ></md-datepicker>

			</div>
			<div flex=5 class="dialog-demo-content">
				<md-input-container class="small counter"> <md-button
					ng-click="loadDocuments($event)" aria-label="load Documents"
					class="md-fab md-ExtraMini"> <md-icon
					md-font-icon="fa fa-search"> </md-icon> </md-button> </md-input-container>
			</div>

		</div>
		<div layout="row" layout-wrap  ng-show="documents.length!=0">
			<div flex=60>
				<h4>{{translate.load("sbi.templatemanagemenent.secondmessage");}}</h4>
			</div>
			<div flex=5 class="dialog-demo-content">
				<md-input-container class="small counter"> <md-button
					ng-click="deleteTemplate($event)" aria-label="delete Templates"
					class="md-fab md-ExtraMini">
				<md-icon md-font-icon="fa fa-trash"> </md-icon> </md-button> </md-input-container>
			</div>
		</div>
			<div id="lista"   >
			<document-tree ng-model="tree" id="impExpTree" create-tree="true"
										selected-item="docChecked" multi-select="true" show-files="true">
									</document-tree>
	<!-- <div layout="row" layout-wrap>
				<div>
					<md-checkbox ng-checked="flagCheck" ng-click="selectAll()">{{translate.load("sbi.importusers.selectall");}}</md-checkbox>
				</div>
			</div>
			<div layout="row" layout-wrap flex>
				<div flex="90" ng-repeat="doc in documents">
					<md-checkbox ng-checked="exists(doc, docChecked)"
						ng-click="toggle(doc, docChecked)"> {{ doc.name }}</md-checkbox>

				</div>

			</div>  -->
	</div>


	</md-content>

	</div>

</body>
</html>

