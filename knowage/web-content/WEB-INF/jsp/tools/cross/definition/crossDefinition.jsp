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
		//TODO check for user profile autorization
		boolean canSee=false,canSeeAdmin=false;
		if(UserUtilities.haveRoleAndAuthorization(userProfile, null, new String[]{SpagoBIConstants.MANAGE_CROSS_NAVIGATION})){
			canSee=true;
		 canSeeAdmin=UserUtilities.haveRoleAndAuthorization(userProfile, SpagoBIConstants.ADMIN_ROLE_TYPE, new String[]{SpagoBIConstants.MANAGE_CROSS_NAVIGATION});
		}
		//TODO rimuovere la seguente riga
		canSee = true;
%>

<% if(canSee ){ %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html >

<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>


<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/themes/glossary/css/generalStyle.css">
<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/cross/definition/CrossDefinition.js"></script>

<style type="text/css">
	.sss{border-bottom: 2px solid rgb(176, 190, 197)}
</style>
</head>

<body class="bodyStyle" ng-app="crossDefinition" id="ng-app">

<script type="text/ng-template" id="nodes_renderer1.html">
  <div ui-tree-handle class="tree-node tree-node-content ">
<div class="tree-node-content">
    <div >
	  <span class="fa fa-bars"></span>
	  {{par.name}}
    </div>
    <div class="pull-right">
        {{par.type}}
    </div>
</div>
  </div>
</script>
<script type="text/ng-template" id="nodes_renderer2.html">
  <div class="tree-node tree-node-content" ng-if="!par.links">
    <div >
	  {{par.name}}
	  <input type="hidden" ng-value="par.id" />
    </div>
    <div class="pull-right">
        {{par.type}}
    </div>
  </div>

  <div class="tree-node tree-node-content" ng-if="par.links">
    <div >
	  {{par.emptyList[0].name}}
	  <span class="fa fa-link"></span>
	  {{par.toName}}
    </div>
    <div class="pull-right">
       x
    </div>
  </div>

	<ol ui-tree-nodes="" ng-model="par.emptyList" ng-class="{hidden: collapsed}" ng-hide="par.links" >
      <li ng-repeat="node in par.emptyList" ui-tree-node >
      </li>
    </ol>
  
</script>


	<div layout="row" ng-controller="navigationList as ctrl" layout-wrap layout-fill>
		<div layout="column" flex="20">
			<div layout="row">
				<!-- ricerca -->
				<angular-table 
						layout-fill
						id="dataSourceList"
						ng-model="ctrl.list"
						columns='[{"label":"Nav","name":"name","size":"50px"},{"label":"Doc A","name":"fromDoc","size":"70px"},{"label":"Doc B","name":"toDoc","size":"70px"}]'
						columns-search='["name","fromDoc","toDoc"]'
						show-search-bar=true
						highlights-selected-item=true
						click-function="ctrl.loadSelectedDataSource(item)"
						selected-item="selectedDataSourceItems"
						speed-menu-option="dsSpeedMenu"					
					>						
					</angular-table>
			</div>
		</div>
		<div layout="column" ng-if="ctrl.detail" flex>
			{{ctrl.detail.name}}
			<div layout="row">
				<div layout="column" flex="50" style="padding:10px">
					<h3 ng-model="ctrl.detail.fromDoc"></h3>
				    <div ui-tree="ctrl.treeOptions" id="tree1-root" data-nodrop-enabled="true" data-clone-enabled="true" style="padding:3px;">
				      <ol ui-tree-nodes="" ng-model="ctrl.detail.fromPars" data-nodrop-enabled="true">
				        <li ng-repeat="par in ctrl.detail.fromPars" ui-tree-node ng-include="'nodes_renderer1.html'" style="padding: 2px"></li>
				      </ol>
				    </div>
				</div>
				<div layout="column" flex style="padding:10px">
					<h3 ng-model="ctrl.detail.toDoc"></h3>
				    <div ui-tree id="tree2-root" style="padding:3px">
				      <ol ui-tree-nodes="" ng-model="ctrl.detail.toPars" >
				        <li ng-repeat="par in ctrl.detail.toPars" ui-tree-node ng-include="'nodes_renderer2.html'" style="padding: 2px" 
				        	ng-mouseenter="ctrl.selectItem($event)" ng-mouseleave="ctrl.unselectAll()" data-nodrag></li>
				      </ol>
				    </div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>


<%}else{ %>
accesso negato
<%} %>

