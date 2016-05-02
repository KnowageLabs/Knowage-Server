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

<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
<%@page import="it.eng.spagobi.commons.utilities.ObjectsAccessVerifier"%>
<%@page import="it.eng.spagobi.utilities.engines.rest.ExecutionSession"%>

<%@ page language="java" pageEncoding="utf-8" session="true"%>

<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<%
BIObject obj = null;
Integer objId = null;
String objLabel = null;
IEngUserProfile profile = null;
List<String> executionRoleNames = new ArrayList();

try{
	profile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	
	obj = (BIObject) aServiceResponse.getAttribute(SpagoBIConstants.OBJECT);
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
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentbrowser/md-data-table.min.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/component-tree/componentTree.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/ngWYSIWYG/wysiwyg.min.js")%>"></script>	
	<link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "js/lib/angular/ngWYSIWYG/editor.min.css")%>"> 
	
	<!-- 	breadCrumb -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/tools/commons/BreadCrumb.js"></script>
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/themes/glossary/css/bread-crumb.css">
	
	<!-- cross navigation -->
	<script type="text/javascript"  src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/cross-navigation/crossNavigationDirective.js")%>"></script>
	<!--  -->
	<style type="text/css">
		.requiredField {color: red!important; font-weight: bold;}
		.norequiredField {}
	</style>
	<style type="text/css">
		.topsidenav {min-width:100% !important; max-width:100% !important; min-height:40%;}
		.lateralsidenav {min-width:350px !important; max-width:350px !important;}
	</style>
</head>

<body class="kn-documentExecution" ng-app="documentExecutionModule" ng-controller="documentExecutionController" layout="row" >
	
	<div  layout-fill ng-hide="hideProgressCircular.status" style="z-index: 10000; position: absolute; background-color: rgba(0, 0, 0, 0.21);">
			      		<md-progress-circular 
        				md-mode="indeterminate" 
        				md-diameter="60" 
        				style="left: 50%;top: 50%;margin-left: -30px;margin-top: -30px;"   
        				>
      					</md-progress-circular>
    </div>
	<md-sidenav class="md-sidenav-right md-whiteframe-4dp lateralsidenav" ng-if="'<%=obj.getParametersRegion() %>' == 'west'" md-component-id="parametersPanelSideNav" layout="column" md-is-locked-open="showParametersPanel.status" ng-include="'/knowage/js/src/angular_1.4/tools/documentexecution/utils/sidenavTemplate/sidenavVertContent.html'">		
	</md-sidenav>
	
	<div layout="column"  ng-init="initSelectedRole()" ng-cloak layout-fill>
	        
	    <md-sidenav id="sidenavOri" class="md-sidenav-right md-whiteframe-4dp topsidenav" ng-if="'<%=obj.getParametersRegion() %>' == 'north'" md-component-id="parametersPanelSideNav" layout="column" md-is-locked-open="showParametersPanel.status" ng-include="'/knowage/js/src/angular_1.4/tools/documentexecution/utils/sidenavTemplate/sidenavOriContent.html'">
		</md-sidenav>
		<md-toolbar class="documentExecutionToolbar secondaryToolbar" flex="nogrow">
            <div class="md-toolbar-tools" layout="row" layout-align="center center">
                <i class="fa fa-file-text-o fa-2x"></i>
                <span>&nbsp;&nbsp;</span>
                <h2 class="md-flex" ng-hide="::crossNavigationScope.isNavigationInProgress()">
                	{{::translate.load("sbi.generic.document")}}: {{executionInstance.OBJECT_LABEL}}
                </h2>
                <cross-navigation cross-navigation-helper="crossNavigationScope.crossNavigationHelper" flex>
				<cross-navigation-bread-crumb id="clonedCrossBreadcrumb"> </cross-navigation-bread-crumb>
 				</cross-navigation>
	
                <span flex=""></span>
                
                <md-button class="md-icon-button" aria-label="{{::translate.load('sbi.generic.helpOnLine')}}" ng-click="openHelpOnLine()"
                		title="{{::translate.load('sbi.generic.helpOnLine')}}">
					 <md-icon md-font-icon="fa fa-book"></md-icon>
				</md-button>
				
				<md-button class="md-icon-button" aria-label="{{::translate.load('sbi.scheduler.parameters')}}" ng-click="executeParameter()" 
						title="{{::translate.load('sbi.scheduler.parameters')}}">
					 <md-icon md-font-icon="fa fa-refresh"></md-icon>
				</md-button>
				<md-button class="md-icon-button" aria-label="{{::translate.load('sbi.scheduler.parameters')}}" ng-click="paramRolePanelService.toggleParametersPanel()"
						title="{{::translate.load('sbi.scheduler.parameters')}}" ng-if="!isParameterRolePanelDisabled.status">
					<md-icon md-font-icon="fa fa-filter"></md-icon> 
					<!-- ng-if="!isParameterRolePanelDisabled.status" -->
				</md-button>
				
				<md-menu-bar id="menu">
                	<md-menu>
		                <md-button id="menuButton" class="md-icon-button" aria-label="Menu" ng-click="$mdOpenMenu()" >
		                	<md-icon md-font-icon="fa  fa-ellipsis-v"></md-icon>
					    </md-button>
					    <md-menu-content>
					    	<span class="divider">{{translate.load("sbi.ds.wizard.file")}}</span>
						    <md-menu-item class="md-indent">
			                	<md-icon class="fa fa-print "></md-icon>
				                <md-button ng-click="printDocument()">
				                	{{translate.load("sbi.execution.executionpage.toolbar.print")}}
				                </md-button>
				            </md-menu-item>
				            				     				          
								<span class="divider" >Export</span>
								<md-menu-item>
				                <md-menu>
				                <md-menu-item class="md-indent">
				                  <md-icon class="fa fa-download"></md-icon>
				                  <md-button ng-click="$mdOpenMenu()">Export</md-button>
				                </md-menu-item>
				                  <md-menu-content>                    
				                    <md-menu-item class="md-indent" ng-repeat="exportationFormat in urlViewPointService.exportation">
				                    	<md-icon class="{{exportationFormat.iconClass}}"></md-icon>
				                    	<md-button ng-click="exportationFormat.func()">{{exportationFormat.description}}</md-button>
				                     </md-menu-item>
				                  </md-menu-content>
				                </md-menu>
				              </md-menu-item>
				          
				            <span class="divider">{{translate.load("sbi.generic.info")}}</span>
				            <% if (userProfile.isAbleToExecuteAction(SpagoBIConstants.SEE_METADATA_FUNCTIONALITY)) { %>
				            <md-menu-item class="md-indent">
				            	<md-icon class="fa fa-info-circle"></md-icon>
		                    	<md-button ng-click="openInfoMetadata()">{{translate.load("sbi.execution.executionpage.toolbar.metadata")}}</md-button>
				            </md-menu-item>
				            <%} %>
				            <md-menu-item class="md-indent">
				            	<md-icon class="fa fa-star"></md-icon>
				            	<md-button aria-label="{{translate.load('sbi.execution.executionpage.toolbar.rating')}}" class="toolbar-button-custom"
                                	ng-click="rankDocument()">{{translate.load('sbi.execution.executionpage.toolbar.rating')}}
				                </md-button> 
				            </md-menu-item>
				            <md-menu-item class="md-indent">
				            	<md-icon class="fa fa-sticky-note-o"></md-icon>
				            	<md-button aria-label="{{translate.load('sbi.execution.executionpage.toolbar.annotate')}}" class="toolbar-button-custom"
                                	ng-click="noteDocument()">{{translate.load('sbi.execution.executionpage.toolbar.annotate')}}
				                </md-button> 
				            </md-menu-item>
				            <span class="divider">{{translate.load("sbi.execution.executionpage.toolbar.shortcuts")}}</span>
				           <!--  
				            <md-menu-item class="md-indent">
				            	<md-button aria-label="{{translate.load('sbi.execution.executionpage.toolbar.showbookmark')}}" class="toolbar-button-custom"
                                	ng-click="alert('TODO')">{{translate.load('sbi.execution.executionpage.toolbar.showbookmark')}}
				                </md-button> 
				            </md-menu-item>
				            <md-menu-item class="md-indent">
				            	<md-button aria-label="{{translate.load('sbi.execution.executionpage.toolbar.addbookmark')}}" class="toolbar-button-custom"
                                	ng-click="alert('TODO')">{{translate.load('sbi.execution.executionpage.toolbar.addbookmark')}}
				                </md-button> 
				            </md-menu-item>
				            <md-menu-item class="md-indent">
				            	<md-button aria-label="{{translate.load('sbi.execution.executionpage.toolbar.showview')}}" class="toolbar-button-custom"
                                	ng-click="alert('TODO')">{{translate.load('sbi.execution.executionpage.toolbar.showview')}}
				                </md-button> 
				            </md-menu-item>
				            <md-menu-item class="md-indent">
				            	<md-button aria-label="{{translate.load('sbi.execution.executionpage.toolbar.saveview')}}" class="toolbar-button-custom"
                                	ng-click="alert('TODO')">{{translate.load('sbi.execution.executionpage.toolbar.saveview')}}
				                </md-button> 
				            </md-menu-item>
				            -->
				            
				            <md-menu-item class="md-indent">
				            	<md-button ng-disabled="true" aria-label="{{translate.load('sbi.execution.executionpage.toolbar.saveview')}}" class="toolbar-button-custom"
                                	>{{translate.load('sbi.execution.executionpage.toolbar.savemyworkspace')}}
				                </md-button> 
				            </md-menu-item>
				            
				            <md-menu-item class="md-indent">
				            	<md-button aria-label="{{translate.load('sbi.execution.executionpage.toolbar.showscheduled')}}" class="toolbar-button-custom"
                                	ng-click="urlViewPointService.getSchedulers()">{{translate.load('sbi.execution.executionpage.toolbar.showscheduled')}}
				                </md-button> 
				            </md-menu-item>
					    </md-menu-content>
                	</md-menu>
                </md-menu-bar>
                
               	<md-button class="md-icon-button" title="close" aria-label="Clear"  ng-click="closeDocument()">
					   <md-icon md-font-icon="fa fa-times"></md-icon>
				</md-button>
			</div>
        </md-toolbar>
       
        <div layout="row" flex="grow" ng-switch on="currentView.status">
 		
	 		<md-content id="documentFrameContainer" layout="row" flex="grow" ng-switch-when="DOCUMENT">  
			      
			      <div layout="row" flex layout-align="center center" ng-hide="urlViewPointService.frameLoaded">
			      <md-progress-circular 
        				md-mode="indeterminate" 
        				md-diameter="70"       
        				>
      				</md-progress-circular>
      				</div>
				<iframe class="noBorder" id="documentFrame" ng-src="{{urlViewPointService.documentUrl}}" iframe-onload="iframeOnload()"
					iframe-set-dimensions-onload flex="grow" ng-show="urlViewPointService.frameLoaded">
				</iframe>
			</md-content>
										
			<div flex layout ng-switch-when="PARAMETERS"> 
				<div ng-if="parameterView.status == 'FILTER_SAVED'" layout flex>
					<parameter-view-point-handler flex layout="column"/>
				</div>
				<div ng-if="parameterView.status == 'SCHEDULER'" layout flex>
					<document-scheduler flex layout="column"/>
				</div>
			</div>
			
				 
		</div>	
		 										
	</div>
	
	<md-sidenav class="md-sidenav-left md-whiteframe-4dp lateralsidenav"  ng-if="'<%=obj.getParametersRegion() %>' == 'east'" md-component-id="parametersPanelSideNav" layout="column" md-is-locked-open="showParametersPanel.status" ng-include="'/knowage/js/src/angular_1.4/tools/documentexecution/utils/sidenavTemplate/sidenavVertContent.html'">
	</md-sidenav>
		
	<script type="text/javascript">
	///Module creation
	(function() {
		
		angular.module('documentExecutionModule', 
				['md.data.table', 'ngMaterial', 'ui.tree', 'sbiModule', 'document_tree', 'componentTreeModule', 'angular_table', 'ngSanitize', 'expander-box', 'ngAnimate', 'ngWYSIWYG','angular_list','cross_navigation']);
		
		angular.module('documentExecutionModule').factory('execProperties', function() {
			 
			var selRole= '<%= request.getParameter("SELECTED_ROLE") %>'=='null' ? '' : '<%= request.getParameter("SELECTED_ROLE") %>';
			var crossParams= <%= request.getParameter("CROSS_PARAMETER") %>==null ? {} : <%= request.getParameter("CROSS_PARAMETER") %>;
			
			 var obj = {
				roles: [<%for(Object roleObj : executionRoleNames) out.print("'" + (String)roleObj + "',");%>],
				executionInstance: {
					'OBJECT_ID' : <%= request.getParameter("OBJECT_ID") %>, 
					'OBJECT_LABEL' : '<%= request.getParameter("OBJECT_LABEL") %>',
					'OBJECT_TYPE_CODE' : '',
					'isFromCross' : false, 
					'isPossibleToComeBackToRolePage' : false,
					'SBI_EXECUTION_ID' : '',
					'CROSS_PARAMETER' : crossParams,
					'ENGINE_LABEL' : ''
				},
				parametersData: {
					documentParameters: []
				},
				selectedRole : {name : selRole },
 				currentView :  {status : "DOCUMENT"},
 				parameterView : {status : ""},
 				isParameterRolePanelDisabled : {status : false},
 				showParametersPanel : {status : false},
 				hideProgressCircular : {status : true}
 				
			};
			return obj;
		});
		
	})();
	</script>
	<script type="text/javascript" 
			src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/utils/documentExecutionServices.js")%>"></script>
	<script type="text/javascript" 
			src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/utils/documentExecutionFactories.js")%>"></script>
	<script type="text/javascript" 
			src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/parameterViewPointHandler/parameterViewPointHandlerController.js")%>"></script>
	<script type="text/javascript" 
		src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/documentScheduler/documentSchedulerController.js")%>"></script>
	<script type="text/javascript" 
			src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/documentParamenterElement/documentParamenterElementController.js")%>"></script>
	<script type="text/javascript" 
			src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/menuFunctions/infoMetadataService.js")%>"></script>
	<script type="text/javascript" 
			src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/documentExecution.js")%>"></script>
	
	<script type="text/javascript" 
			src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/documentExecutionNote.js")%>"></script>
	<script type="text/javascript" 
			src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/documentExecutionRank.js")%>"></script>
	
	

</body>
</html>
