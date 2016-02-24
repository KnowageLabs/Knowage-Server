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
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/BreadCrumb.js"></script>
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/themes/glossary/css/bread-crumb.css">
	


<link rel="stylesheet" type="text/css"
	href="/knowage/themes/commons/css/customStyle.css">

<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/themes/importexport/css/importExportStyle.css">
<!-- controller -->
<script type="text/javascript"
	src="/knowage/js/src/angular_1.4/tools/servermanager/templateManagement.js"></script>

</head>
<body class="bodyStyle kn-templatemanagement kn-importExportDocument">

	<div ng-controller="Controller " layout="column" layout-wrap>
		<md-toolbar class="miniheadimportexport">
		<div class="md-toolbar-tools">
			<i class="fa fa-file fa-2x"></i>
			<h2 class="md-flex">{{translate.load("sbi.templatemanagemenent");}}</h2>
		</div>
		</md-toolbar>
	


		<md-content layout="column" class="mainContainer" layout-wrap>
		<md-card> <md-toolbar class="cardHeader">
		<div class="md-toolbar-tools">
			<h2 class="md-flex">{{translate.load("sbi.templatemanagemenent.choosedate");}}</h2>
		</div>
		</md-toolbar> <md-content layout="row">
		<div flex=30>
			<p>{{translate.load("sbi.templatemanagemenent.firstmessage");}}</p>
		</div>
		<div flex>
			<md-datepicker ng-model="dateSelected.data" name="Select Data"
				ng-change="parseDate()" md-placeholder={{translate.load("sbi.templatemanagemenent.selectdata");}} ></md-datepicker>

		</div>
		</md-content> 
		<md-toolbar class="cardHeader"  ng-show="documents.length!=0">
		<div class="md-toolbar-tools">
			<h2 class="md-flex">{{translate.load("sbi.templatemanagemenent.documentselection");}}</h2>
		</div>
		</md-toolbar>
		<div layout-wrap ng-show="documents.length!=0">
			<p>{{translate.load("sbi.templatemanagemenent.secondmessage");}}</p>
			<md-button class="md-raised" ng-click="deleteTemplate($event)"
				aria-label="delete Templates">{{translate.load("sbi.federationdefinition.delete");}}</md-button>


		</div>
		<div id="lista"  ng-show="documents.length!=0">
			<document-tree ng-model="tree" id="impExpTree" create-tree="true"
				selected-item="docChecked" multi-select="true" show-files="true">
			</document-tree>

		</div>
	</div>
	</md-card>
	</md-content>

	</div>

</body>
</html>

