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

<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
<%@page import="it.eng.spagobi.commons.utilities.ObjectsAccessVerifier"%>
<%@page import="it.eng.spagobi.utilities.engines.rest.ExecutionSession"%>

<%@ page language="java" pageEncoding="utf-8" session="true"%>

<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<%
BIObject obj;
Integer objId;
String objLabel;
IEngUserProfile profile;
List<String> executionRoleNames = new ArrayList();

try{
	profile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	objId = new Integer(request.getParameter("OBJECT_ID"));
	objLabel = request.getParameter("OBJECT_LABEL");
	
	executionRoleNames = ObjectsAccessVerifier.getCorrectRolesForExecution(objId, profile);
}catch (Throwable t) {
	
}

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
	<!-- Styles -->
	<link rel="stylesheet" type="text/css" href="/knowage/themes/commons/css/customStyle.css"> 
	<link rel="stylesheet" type="text/css" href="/knowage/themes/documentexecution/css/documentexecution.css"> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentbrowser/md-data-table.min.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js")%>"></script>
	
	<style type="text/css">
		#metadataDlg md-dialog-content {margin: 0;padding-bottom: 10px;}
		#metadataDlg expander-box > md-content {border: 0!important; padding:0;}
		/*#metadataDlg expander-box > md-content md-tabs {padding:0 16px;}*/
		.animate-accordion.ng-hide-add, .animate-accordion.ng-hide-remove {transition: linear all 1s;}
		.animate-accordion.ng-hide {height:0;}
	</style>
</head>

<body class="bodyStyle" ng-app="documentExecutionModule">
	<div layout="column" ng-controller="documentExecutionController" ng-init="initSelectedRole()" ng-cloak layout-fill>
		<md-toolbar class="miniheadimportexport" flex="nogrow">
            <div class="md-toolbar-tools" layout="row" layout-align="center center">
                <i class="fa fa-file-text-o fa-2x"></i>
                <span>&nbsp;&nbsp;</span>
                <h2 class="md-flex">
                	{{translate.load("sbi.generic.document")}}: <%= request.getParameter("OBJECT_NAME") %> - ({{translate.load("sbi.browser.defaultRole.role")}} {{selectedRole.name}})
                </h2>
                <span flex=""></span>
				<md-button class="toolbar-button-custom" aria-label="Parameters"
						title="{{::translate.load('sbi.scheduler.parameters')}}"
						ng-click="toggleParametersPanel()" 
						ng-disabled="isParameterRolePanelDisabled"
				>
					<i class="fa fa-filter header"></i> 
				</md-button>
				<md-menu-bar>
                	<md-menu>
		                <md-button class="toolbar-button-custom" aria-label="Menu" ng-click="$mdOpenMenu()" >
		                	<i class="fa fa-ellipsis-v header"></i>
					    </md-button>
					    <md-menu-content>
						    <md-menu-item>
				                <md-button ng-click="alert('TODO File section')">
				                	File
				                </md-button>
				            </md-menu-item>
				            <md-menu-item>
				                <md-menu>
				                	<md-button ng-click="$mdOpenMenu()">Info</md-button>
				                    <md-menu-content>
				                    	<md-menu-item><md-button ng-click="openInfoMetadata()">Metadata</md-button>
				                    	</md-menu-item>
				                    </md-menu-content>
				                </md-menu>
				            </md-menu-item>
					    </md-menu-content>
                	</md-menu>
                </md-menu-bar>
			</div>
        </md-toolbar>
        
        <div class="animate-switch-container" layout="row" flex="grow"  ng-switch on="currentView">
 		
 		<md-content layout="row" flex="grow" class="animate-switch switch-right" ng-switch-when="DOCUMENT"> 
			<iframe ng-src="{{documentUrl}}" iframe-onload="iframeOnload()"
				iframe-set-dimensions-onload flex="grow"></iframe>
				
			<md-sidenav class="md-sidenav-right" md-component-id="parametersPanelSideNav" layout="column"
					ng-class="{'md-locked-open': showParametersPanel}" md-is-locked-open="$mdMedia('gt-md')" >
							
				<md-toolbar class="header" ng-hide="isParameterPanelDisabled()">
					<div layout="row" layout-align="center center">						
						<md-button title="Reset" aria-label="Reset Parameter" class="toolbar-button-custom" 
								ng-click="clearListParametersForm();">
							<i class="fa fa-eraser" style="color:white"></i>
						</md-button>						
						<md-button title="Open Saved" aria-label="Open Saved Parameters" class="toolbar-button-custom" 
								ng-click="getViewpoints();">
							<i class="fa fa-pencil" style="color:white"></i>
						</md-button>						
						<md-button title="Save" aria-label="Save Parameters" class="toolbar-button-custom" 
								ng-click="createNewViewpoint();">
							<i class="fa fa-floppy-o" style="color:white"></i>
						</md-button>
					</div>
				</md-toolbar>
				
				<md-content ng-show="showSelectRoles">
					<md-input-container class="small counter" flex>
						<label>{{::translate.load("sbi.users.roles")}}</label>
						<md-select aria-label="aria-label" ng-model="selectedRole.name" >
							<md-option ng-click="changeRole(role)" ng-repeat="role in roles" value="{{role}}">
								{{::role|uppercase}}
							</md-option>
						</md-select>
					</md-input-container>
				</md-content>
				
				<md-list ng-hide="isParameterPanelDisabled()" layout="column">
					<md-list-item ng-repeat="parameter in documentParameters" layout="row" layout-align="start">
						<md-button class="md-icon-button" ng-click="documentExecuteUtils.resetParameter(parameter)">
							<i class="fa fa-eraser"></i>
						</md-button>
						<document-paramenter-element flex layout-align="start"/>
					</md-list-item>
				</md-list>
				
				<!-- execute button -->
				<md-button class="toolbar-button-custom md-raised" ng-disabled="isExecuteParameterDisabled()"
						title="{{::translate.load('sbi.execution.parametersselection.executionbutton.message')}}"  
						ng-click="executeParameter()" ng-hide="isParameterPanelDisabled()">
					{{::translate.load("sbi.execution.parametersselection.executionbutton.message")}}
				</md-button>				
			</md-sidenav>
		</md-content>
		
		
		<div class="animate-switch switch-left" flex  ng-switch-when="PARAMETERS"> 
			<div ng-if="parameterView == 'FILTER_SAVED'" layout="row">
					<document-paramenter-filter-handler flex/>
			</div>
		</div>		
		</div>												
	
	
	
	
	
	</div>
		
	<script type="text/javascript">
	//Module creation
	(function() {
		
		angular.module('documentExecutionModule', ['md.data.table', 'ngMaterial', 'ui.tree', 'sbiModule', 'document_tree','angular_table', 'ngSanitize', 'expander-box', 'ngAnimate']);
		
		angular.module('documentExecutionModule').factory('execProperties', function() {
			var obj = {
				roles: [<%for(Object roleObj : executionRoleNames) out.print("'" + (String)roleObj + "',");%>],
				executionInstance: {
					'OBJECT_ID' : '<%= request.getParameter("OBJECT_ID") %>', 
					'OBJECT_LABEL' : '<%= request.getParameter("OBJECT_LABEL") %>',
					'isFromCross' : false, 
					'isPossibleToComeBackToRolePage' : false
				},
				parametersData: {
					parameterList: [],
					showParametersPanel: false
				}
			};
			return obj;
		});
		
	})();
	</script>
	<script type="text/javascript" 
			src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/utils/documentExecutionUtils.js")%>"></script>
	<script type="text/javascript" 
			src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/documentExecution.js")%>"></script>
	
	<!-- script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/textAngular/1.5.0/textAngular.min.js"></script -->
	

</body>
</html>
