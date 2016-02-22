<%@ page language="java" pageEncoding="utf-8" session="true"%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>


<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="measureRoleManager">

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Measure Role definition</title>

<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<link rel="stylesheet" type="text/css"	href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css"> 
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.css">
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/theme/eclipse.css">  
  <script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/lib/codemirror.js"></script>  
 <script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/ui-codemirror.js"></script> 
 <script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/mode/sql/sql.js"></script>  

<link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.css" />
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/show-hint.js"></script>
<script src="${pageContext.request.contextPath}/js/lib/angular/codemirror/CodeMirror-master/addon/hint/sql-hint.js"></script>

<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLinkByTheme(request, "/css/angularjs/kpi/measureRoleCustomStyle.css", currTheme)%>">
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/measureRoleDefinition.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/measureRoleSubController/measureRoleQueryController.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/kpi/measureRoleSubController/measureRoleMetadataController.js"></script>

</head>
<body>
	<angular-list-detail ng-controller="measureRoleMasterController" new-function="newMeasureFunction" full-screen="false">
		<list label="translate.load('sbi.kpi.measure.list')" ng-controller="measureListController">
			<angular-table id='measureListTable' ng-model=measureList
				columns='measureColumnsList'
			 	 show-search-bar=true
			 	 speed-menu-option=measureMenuOption
				highlights-selected-item=true click-function="measureClickFunction(item);" > </angular-table>
		</list>
		<detail ng-controller="measureDetailController">
			<md-tabs layout-fill class="absolute">
			
				<md-tab id="tab1">
       				<md-tab-label>{{translate.load("sbi.ds.query")}}</md-tab-label>
        			<md-tab-body >
        				<%@include	file="./measureRoleTemplate/queryTemplate.jsp"%>
        			</md-tab-body>
				</md-tab>
				
				<md-tab id="tab2"  ng-click="loadMetadata()">
       				<md-tab-label>{{translate.load("sbi.execution.executionpage.toolbar.metadata")}}</md-tab-label>
        			<md-tab-body  >
        			<%@include	file="./measureRoleTemplate/metadataTemplate.jsp"%>
					</md-tab-body>
				</md-tab>
				
				<md-tab id="tab3">
       				<md-tab-label>{{translate.load("sbi.ds.test")}}</md-tab-label>
        			<md-tab-body>
        			<%@include	file="./measureRoleTemplate/previewTemplate.jsp"%>
        			
					</md-tab-body>
				</md-tab>
				
			</md-tabs>
		</detail>
	</angular-list-detail>
</body>
</html>