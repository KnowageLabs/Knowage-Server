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


<%@page import="it.eng.spagobi.utilities.engines.rest.ExecutionSession"%>
<%@ page language="java" pageEncoding="utf-8" session="true"%>

<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	
	<!-- Styles -->
	<link rel="stylesheet" type="text/css" href="/knowage/themes/commons/css/customStyle.css"> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentbrowser/md-data-table.min.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js")%>"></script>
</head>

<body class="bodyStyle" ng-app="documentExecutionModule">
	<div layout="column" ng-controller="documentExecutionController as ctrl" ng-init="initSelectedRole()" ng-cloak>
			
		<md-toolbar class="miniheadimportexport">
            <div class="md-toolbar-tools" layout="row" layout-align="center center">
                <i class="fa fa-file-text-o fa-2x"></i>
                <h2 class="md-flex">
                	{{translate.load("sbi.generic.document")}}: <%= request.getParameter("OBJECT_NAME") %> - {{translate.load("sbi.browser.defaultRole.role")}} {{selectedRole}}
                </h2>
                
                <span flex=""></span>

				<md-button class="toolbar-button-custom" title="Parameters"	aria-label="Parameters" 
						ng-click="toggleParametersPanel()" ng-disabled="isParameterPanelDisabled()">
					<i class="fa fa-cog header"></i> 
				</md-button>
	
			</div>
        </md-toolbar>

		<section layout="row" >
			<md-input-container class="small counter">
				<md-select aria-label="aria-label" ng-model="selectedRole" ng-show="showSelectRoles">
					<md-option ng-click="changeRole(role)" ng-repeat="role in roles" value="{{role}}">{{role|uppercase}}</md-option>
				</md-select>
			</md-input-container>
			
			<md-content layout-padding flex>
				<iframe ng-src="{{documentUrl}}" layout-fill
				<%--
					style="overflow:hidden;height:100%;width:100%" height="100%"
				--%>
					> </iframe>
			</md-content>
		
			<md-sidenav class="md-sidenav-right" md-component-id="right" 
					ng-class="{'md-locked-open': showParametersPanel}" md-is-locked-open="$mdMedia('gt-md')" 
			> 
			<!-- 
			-->
				<!--
				<md-toolbar class="header" style="height: 75px;">
					<h1 class="md-toolbar-tools" style="text-align:center; display:inline;">
						{{translate.load("sbi.execution.parametersselection.parameters")}}
					</h1>
					<div layout="row" layout-align="center center">
						<md-button title="{{translate.load('sbi.execution.parametersselection.toolbar.next')}}"
								aria-label="Execute Document" class="toolbar-button-custom"
								ng-click="executeDocument()" ng-disabled="canExecuteDocument()">
							<i class="fa fa-play-circle"></i>
						</md-button>
						
						<md-button title="Edit Document" aria-label="Edit Document" class="toolbar-button-custom" 
								ng-click="editDocument()">
							<i class="fa fa-pencil"></i>
						</md-button>
						
						<md-button title="Delete Document" aria-label="Delete Document" class="toolbar-button-custom" 
								ng-click="deleteDocument()">
							<i class="fa fa-trash-o"></i>
						</md-button>
					</div>
				</md-toolbar>
				-->
				
				<md-list>
					<md-list-item layout="row" ng-repeat="param in documentParameters">
						<md-input-container class="small counter">
							<label>{{param.label}}</label>
							<input class="input_class" ng-model="param.parameterValue" ng-required="param.mandatory" > 
						</md-input-container>
					</md-list-item>
				</md-list>
				
				<!-- execute button -->
				<md-button class="toolbar-button-custom md-raised" ng-hide="isExecuteParameterHidden()"
						title="{{translate.load('sbi.execution.parametersselection.executionbutton.message')}}"  
						ng-click="executeParameter()">
					{{translate.load("sbi.execution.parametersselection.executionbutton.message")}}
				</md-button>
			</md-sidenav>
		</section>
	</div>
		
	<script type="text/javascript">
	//Module creation
	angular.module('documentExecutionModule', ['md.data.table', 'ngMaterial', 'ui.tree', 'sbiModule', 'document_tree']);
	angular.module('documentExecutionModule').factory('execProperties', function() {
		var obj = {
			roles: [<%for(Object roleObj : userRoles) out.print("'" + (String)roleObj + "',");%>],
			executionInstance: {
				'OBJECT_ID' : '<%= request.getParameter("OBJECT_ID") %>', 
				'OBJECT_LABEL' : '<%= request.getParameter("OBJECT_LABEL") %> ',
				'isFromCross' : false, 
				'isPossibleToComeBackToRolePage' : false
			}
		};
		return obj;
	});
	</script>
	<script type="text/javascript" 
			src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/documentExecution.js")%>"></script>
</body>
</html>
