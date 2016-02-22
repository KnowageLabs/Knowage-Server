<%@ page language="java" pageEncoding="utf-8" session="true"%>

<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	
	<!-- Styles -->
	<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/glossary/css/generalStyle.css")%>">
	<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "themes/catalogue/css/catalogue.css")%>">
	
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentbrowser/md-data-table.min.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js")%>"></script>
</head>

<body class="bodyStyle" ng-app="documentExecutionModule">
	<div layout="column" ng-controller="documentExecutionController as ctrl" 
			ng-init="initSelectedRole()" ng-cloak>

		<md-content>executionInstance = {{executionInstance}}</md-content>
		<md-content>roles.length = {{roles.length}}</md-content>
		<md-content>selectedRole = {{selectedRole}}</md-content>
		
		<section layout="row" layout-padding>
			<md-input-container class="small counter">
				<md-select aria-label="aria-label" ng-model="selectedRole" >
					<md-option ng-repeat="role in roles" value="{{role}}">{{role}}</md-option>
				</md-select>
			</md-input-container>
		</section>
	</div>
		
	<script type="text/javascript">
	//Module creation
	angular.module('documentExecutionModule', ['md.data.table', 'ngMaterial', 'ui.tree', 'sbiModule', 'document_tree']);
	
	angular.module('documentExecutionModule').factory('execProperties', function() {
		var obj = {
			roles: [<%for(Object roleObj : userRoles) out.print("'" + (String)roleObj + "',");%>],
			executionInstance: {}
		};
		return obj;
	});
	</script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/documentExecution.js")%>"></script>
</body>
</html>