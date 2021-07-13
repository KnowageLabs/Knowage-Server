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

<%@page import="java.util.regex.Matcher"%>
<%@page import="java.util.regex.Pattern"%>
<%@page import="org.jgrapht.util.PrefetchIterator.NextElementFunctor"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
<%@page import="it.eng.spagobi.commons.utilities.ObjectsAccessVerifier"%>
<%@page import="it.eng.spagobi.engines.config.bo.Engine"%>
<%@page import="it.eng.spagobi.utilities.engines.rest.ExecutionSession"%>
<%@page import="it.eng.knowage.security.ProductProfiler"%>
<%@page import="java.util.Enumeration"%>

<%@page import="it.eng.spagobi.tools.dataset.dao.IBIObjDataSetDAO"%>
<%@page import="it.eng.spagobi.tools.dataset.dao.BIObjDataSetDAOHibImpl"%>
<%@page import="it.eng.spagobi.tools.dataset.bo.BIObjDataSet"%>

<%@ page language="java" pageEncoding="UTF-8" session="true"%>

<%@ include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<%
BIObject obj = null;
/* 
Integer objId = null; 
*/
String objId = null;
String objLabel = null;

List<String> executionRoleNames = new ArrayList();

Engine executingEngine = null;
String engineName = null;
String isFromDocumentWidget = null;
String isForExport = null;
String cockpitSelections = null;
String documentType = null;
boolean isNotOlapDoc = true;
boolean canExecuteDocument = true;

// author: danristo
String executedFrom = null;

boolean isFromCross = false;

try{
    objId = (String)(request.getParameter(SpagoBIConstants.OBJECT_ID));
    objLabel = (String)(request.getParameter(SpagoBIConstants.OBJECT_LABEL));
    //For default gets the object document through the DAO by the label found into the request:
    IBIObjectDAO biObjectDAO = DAOFactory.getBIObjectDAO();
    obj = biObjectDAO.loadBIObjectByLabel(objLabel);
    if(obj == null){
        obj = biObjectDAO.loadBIObjectById(Integer.valueOf(objId));
    }    

    /*
        This request parameter is sent from the controller of the document execution application (documentViewer.js) and it
        serves as indicator of the previous page, i.e. the starting point of the document execution - from where the execution
        started. Originally, this option was used for needs of the Workspace Organizer. Namely, if this parameter indicates
        that we are coming from this instance (the Organizer), we should have remove the "Add to my workspace" option from the
        document execution menu.
        
        NOTE: This parameter can be used for forwarding information from other starting points (pages), as well.
        
        @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
    */
    executedFrom = (String)(request.getParameter("EXEC_FROM"));
     
    /*
     * Validation check for exec_from variable for security reasons
     */
     
     if (!"WORKSPACE_ORGANIZER".equals(executedFrom)) {
    	 executedFrom = null;
     }
   
    isFromDocumentWidget = (String)(request.getParameter("IS_FROM_DOCUMENT_WIDGET"));
    isForExport = (String)(request.getParameter(SpagoBIConstants.IS_FOR_EXPORT));
    if(isForExport == null) {
        isForExport = "false";
    }
    
    cockpitSelections = (String)(request.getParameter(SpagoBIConstants.COCKPIT_SELECTIONS));
    

    Object crossParameters = request.getParameter("CROSS_PARAMETER");
    if(crossParameters != null && crossParameters != "" && !crossParameters.toString().equalsIgnoreCase("null")){
        isFromCross = true;
    }
    
    executingEngine = obj.getEngine();
    engineName = executingEngine.getName();
    documentType = obj.getBiObjectTypeCode();
    
   	if(documentType.equals("OLAP")) {
   		isNotOlapDoc = false;
   	} else {
   		isNotOlapDoc = true;
   	}
    
    if(objId != null && !("null".equalsIgnoreCase(objId))) {
        Integer objIdInt = new Integer(objId);
        executionRoleNames = ObjectsAccessVerifier.getCorrectRolesForExecution(objIdInt, userProfile);
    } else {
        executionRoleNames = ObjectsAccessVerifier.getCorrectRolesForExecution(obj.getLabel(), userProfile);        
    }
    
    canExecuteDocument = ProductProfiler.canExecuteDocument(obj);
    
}catch (Exception e) {
    e.printStackTrace();
}

/*
    These two variables are needed for checking if the "Add to workspace" should be available for the current user. This option is available when 
    the document is executed and it serves to add link to that particular document in the Organizer (Documents view) in the Workspace (for that 
    particular user). Variables are at disposal for using for other purposes as well.
    @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
*/
boolean isAdmin = UserUtilities.isAdministrator(userProfile);
boolean isSuperAdmin = (Boolean)((UserProfile)userProfile).getIsSuperadmin();
        
// author: danristo
boolean isAbleToExecuteAction = userProfile.isAbleToExecuteAction(SpagoBIConstants.SEE_SNAPSHOTS_FUNCTIONALITY);
boolean isAbleToExecuteActionSnapshot = userProfile.isAbleToExecuteAction(SpagoBIConstants.RUN_SNAPSHOTS_FUNCTIONALITY);

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<% 
if(executionRoleNames.size() > 0 && canExecuteDocument) {
%>
    <head>
        <%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
        <link rel="shortcut icon" href="<%=urlBuilder.getResourceLinkByTheme(request, "img/favicon.ico", currTheme)%>" />
        
<%-- ---------------------------------------------------------------------- --%>
<%-- INCLUDE Persist JS                                                     --%>
<%-- ---------------------------------------------------------------------- --%>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/persist-0.1.0/persist.js")%>"></script>
<script type="text/javascript">
	//defining GLOBAL context url for following directives and template usage
	_CURRENTCONTEXTURL="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution")%>"
</script>
		<script>
			if(!PleaseRotateOptions){
				var PleaseRotateOptions = {
				    message: "Please Rotate Your Device",
				    subMessage: "For a better mobile experience",
				    allowClickBypass: false,
				    onlyMobile: true,
				    zIndex: 9999
				};
			}
		</script>
		
		<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "node_modules/pleaserotate.js/pleaserotate.min.js")%>"></script>
    
        <!--  Drivers Execution -->
        <script type="text/javascript"  src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/driversexecution/driversExecutionModule.js")%>"></script>        
        <script type="text/javascript"  src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/driversexecution/driversDependencyService.js")%>"></script>
        <script type="text/javascript"  src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/driversexecution/driversExecutionService.js")%>"></script>

        <!-- Styles -->
        <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "node_modules/ng-wysiwyg/dist/wysiwyg.min.js")%>"></script>  
        <link rel="stylesheet" type="text/css" href="<%=urlBuilder.getResourceLink(request, "node_modules/ng-wysiwyg/dist/editor.min.css")%>"> 
        
         <!-- Wheel navigator -->
        <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/wheelnav/raphael.min.js")%>""></script>
	    <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/wheelnav/raphael.icons.min.js")%>"></script>
	    <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/wheelnav/wheelnav.js")%>"></script>
        
        <!--    breadCrumb -->
        <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/BreadCrumb.js")%>"></script>
        
        <!-- cross navigation -->
        <script type="text/javascript"  src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/commons/cross-navigation/crossNavigationDirective.js")%>"></script>
        <!--  -->
        
        <!-- dataset preview - Birt report -->
		<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/birtReportDatasetPreview/datasetPreview_service.js")%>"></script>

        <style type="text/css">
            .requiredField {color: red!important; font-weight: bold;}
            .norequiredField {}
            md-select.requiredField > md-select-value{color: red!important;}
        </style>
        <style type="text/css">
            .topsidenav {min-width:100% !important; max-width:100% !important; min-height:40%;}
            .lateralsidenav {min-width:350px !important; max-width:350px !important;}
        </style>
    </head>

    <body class="kn-documentExecution" ng-app="documentExecutionModule" ng-controller="documentExecutionController" layout="row" ng-cloak >
    <wheel-navigator ng-if="navigatorVisibility" navigator-style="navigatorStyle"></wheel-navigator>
        
        <!--
            Move these Java variables to the Javascript so they can eventually be attached to the scope of the controller
            of the document execution application (page). They will then be used for binding on this page, later on. 
            @author Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
        -->
        <script type="text/javascript">
            var executedFrom = '<%=executedFrom%>';
            var isAdmin = <%=isAdmin%>;
            var isSuperAdmin = <%=isSuperAdmin%>;
            var isAbleToExecuteAction = <%=isAbleToExecuteAction%>;
            var isNotOlapDoc = <%=isNotOlapDoc%>;
        </script>
    
        <div  layout-fill ng-hide="hideProgressCircular.status" style="z-index: 10000; position: absolute; background-color: rgba(0, 0, 0, 0.21);">
            <md-progress-circular md-mode="indeterminate" md-diameter="60" 
                    style="left: 50%;top: 50%;margin-left: -30px;margin-top: -30px;"></md-progress-circular>
        </div>
                    
        <md-sidenav class="md-sidenav-right md-whiteframe-4dp lateralsidenav" 
                ng-if="'<%=obj.getParametersRegion() %>' == 'west'" md-component-id="parametersPanelSideNav" 
                layout="column" md-is-locked-open="showParametersPanel.status" 
                ng-include="'<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/utils/sidenavTemplate/sidenavVertContent.jsp")%>'">     
        </md-sidenav>
    
        <div layout="column"  ng-init="initSelectedRole()" ng-cloak layout-fill>
            <md-sidenav id="sidenavOri" class="md-sidenav-right md-whiteframe-4dp topsidenav" 
                    ng-if="'<%=obj.getParametersRegion() %>' == 'north'" md-component-id="parametersPanelSideNav" 
                    layout="column" md-is-locked-open="showParametersPanel.status" 
                    ng-include="'<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/utils/sidenavTemplate/sidenavVertContent.jsp")%>'">
            </md-sidenav>
    <% 
    
    if(isFromDocumentWidget == null || ("false").equalsIgnoreCase(isFromDocumentWidget)) {

    	if(request.getParameter("TOOLBAR_VISIBLE") != null && request.getParameter("TOOLBAR_VISIBLE").equalsIgnoreCase("false")){
        	
        }
        else{
    
    %>
            <md-toolbar class="documentExecutionToolbar" flex="nogrow">
                <div class="md-toolbar-tools noPadding" layout="row" layout-align="center center">
                    <md-button ng-if="navigatorEnabled" class="md-icon-button" ng-click="goBackHome()"
                            aria-label="homepage"
                            title="go to homepage">
                         <md-icon md-font-icon="fa fa-home"></md-icon>
                    </md-button>
                    <h2 class="md-flex text-transform-none" style="padding-left: 8px" ng-hide="::crossNavigationScope.isNavigationInProgress()">
                        {{i18n.getI18n(executionInstance.OBJECT_NAME)}}
                    </h2>
                    <cross-navigation cross-navigation-helper="crossNavigationScope.crossNavigationHelper" flex>
                        <cross-navigation-bread-crumb id="clonedCrossBreadcrumb"> </cross-navigation-bread-crumb>
                    </cross-navigation>
        
                    
        <% if(engineName.equalsIgnoreCase( SpagoBIConstants.COCKPIT_ENGINE_NAME)
                            && (isAdmin || userId.equals(obj.getCreationUser()))) {%>
                    <md-button ng-if="cockpitEditing.documentMode == 'EDIT'" class="md-icon-button" ng-click="cockpitEditing.stopCockpitEditing()"
                            aria-label="{{::translate.load('sbi.execution.executionpage.toolbar.viewcockpitdoc')}}">
                            <md-tooltip direction="bottom">{{::translate.load('sbi.execution.executionpage.toolbar.viewcockpitdoc')}}</md-tooltip>
                         <md-icon md-font-icon="fa fa-eye"></md-icon>
                    </md-button>
                    <md-button ng-if="cockpitEditing.documentMode != 'EDIT'" class="md-icon-button" ng-click="cockpitEditing.startCockpitEditing()"
                            aria-label="{{::translate.load('sbi.execution.executionpage.toolbar.editcockpitdoc')}}">
                            <md-tooltip direction="bottom">{{::translate.load('sbi.execution.executionpage.toolbar.editcockpitdoc')}}</md-tooltip>
                         <md-icon md-font-icon="fa fa-pencil-square-o"></md-icon>
                    </md-button>
        <%} %>
                    <md-button class="md-icon-button" ng-if="navigatorEnabled" ng-click="toggleNavigator($event)">
                         <md-tooltip direction="bottom">Navigator</md-tooltip>
                         <md-icon md-font-icon="fa fa-compass"></md-icon>
                    </md-button>
                    
                    <md-button class="md-icon-button"  ng-if="checkHelpOnline()"  aria-label="{{::translate.load('sbi.generic.helpOnLine')}}" ng-click="openHelpOnLine()">
                         <md-tooltip direction="bottom">{{::translate.load('sbi.generic.helpOnLine')}}</md-tooltip>
                         <md-icon md-font-icon="fa fa-book"></md-icon>
                    </md-button>
                    
                    <md-button class="md-icon-button" aria-label="{{::translate.load('sbi.scheduler.parameters')}}" ng-click="executeParameter()" >
                         <md-tooltip direction="bottom">{{::translate.load('sbi.scheduler.refresh')}}</md-tooltip>   
                         <md-icon md-font-icon="fa fa-refresh"></md-icon>
                    </md-button>
                    
                    <% 
                    if(request.getParameter("CAN_RESET_PARAMETERS") != null && request.getParameter("CAN_RESET_PARAMETERS").equalsIgnoreCase("false")){
                    }
                    else{
                    %>
                    <md-button class="md-icon-button" aria-label="{{::translate.load('sbi.scheduler.parameters')}}" ng-click="paramRolePanelService.toggleParametersPanel()"
                             ng-if="!isParameterRolePanelDisabled.status">
                        <md-tooltip direction="bottom">{{::translate.load('sbi.scheduler.parameters')}}</md-tooltip>
                        <md-icon md-font-icon="fa fa-filter"></md-icon> 
                    </md-button>
                    <%} %>
                    
                    <md-menu-bar id="menu" ng-show="menuElementLength > 0">
                        <md-menu>
                            <md-button id="menuButton" class="md-icon-button" aria-label="Menu" ng-click="$mdOpenMenu(); closeMdMenu();" >
                            	<md-tooltip direction="bottom">Menu</md-tooltip>
                                <md-icon md-font-icon="fa  fa-ellipsis-v"></md-icon>
                            </md-button>
                            <md-menu-content>
                                <span class="divider" ng-if="canPrintDocuments">{{translate.load("sbi.ds.wizard.file")}}</span>
                                <md-menu-item class="md-indent" ng-if="canPrintDocuments">
                                    <md-icon class="fa fa-print "></md-icon>
                                    <md-button ng-click="printDocument()">
                                        {{translate.load("sbi.execution.executionpage.toolbar.print")}}
                                    </md-button>
                                </md-menu-item>
                                                                              
                                <span class="divider" ng-if="urlViewPointService.exportation.length>0 && cockpitEditing.documentMode != 'EDIT'">{{translate.load("sbi.execution.executionpage.toolbar.export")}}</span>
                                <md-menu-item ng-if="urlViewPointService.exportation.length>0 && cockpitEditing.documentMode != 'EDIT'">  
                                    <md-menu>
                                        <md-menu-item class="md-indent">
                                            <md-icon class="fa fa-download"></md-icon>
                                            <md-button ng-click="$mdOpenMenu()">
                                            	{{translate.load("sbi.execution.executionpage.toolbar.export")}}
                                            	<md-progress-circular ng-show="exportService.isExporting()" md-mode="indeterminate" md-diameter="20"></md-progress-circular>
                                           	</md-button>
                                        </md-menu-item>
                                        <md-menu-content>
                                            <md-menu-item class="md-indent" ng-repeat="exportationFormat in urlViewPointService.exportation">
                                                <md-icon class="{{exportationFormat.iconClass}}"></md-icon>
                                                <md-button ng-click="exportationFormat.func()">{{exportationFormat.description}}</md-button>
                                            </md-menu-item>
                                        </md-menu-content>
                                    </md-menu>
                                </md-menu-item>
                          
                                <% if (userProfile.isAbleToExecuteAction(SpagoBIConstants.SEND_MAIL_FUNCTIONALITY)
                                        &&  obj.getBiObjectTypeCode().equals("REPORT")) { %>
                                <md-menu-item class="md-indent">
                                    <md-icon class="fa fa-paper-plane"></md-icon>
                                    <md-button aria-label="{{translate.load('sbi.execution.executionpage.toolbar.annotate')}}" class="toolbar-button-custom"
                                            ng-click="sendMail()">{{translate.load('sbi.execution.executionpage.toolbar.send')}}
                                    </md-button>
                                </md-menu-item>
                                <%} %>
                          
                                <span class="divider" ng-if="showCollaborationMenu">{{translate.load("sbi.generic.info")}}</span>
                                <% if (userProfile.isAbleToExecuteAction(SpagoBIConstants.SEE_METADATA_FUNCTIONALITY)) { %>
                                <md-menu-item class="md-indent" ng-if="showCollaborationMenu">
                                    <md-icon class="fa fa-info-circle"></md-icon>
                                    <md-button ng-click="openInfoMetadata()">{{translate.load("sbi.execution.executionpage.toolbar.metadata")}}</md-button>
                                </md-menu-item>
                                <%} %>
                                <md-menu-item class="md-indent" ng-if="showCollaborationMenu && canRate">
                                    <md-icon class="fa fa-star"></md-icon>
                                    <md-button aria-label="{{translate.load('sbi.execution.executionpage.toolbar.rating')}}" class="toolbar-button-custom"
                                        ng-click="rankDocument()">{{translate.load('sbi.execution.executionpage.toolbar.rating')}}
                                    </md-button>
                                </md-menu-item>
                                <% if (userProfile.isAbleToExecuteAction(SpagoBIConstants.SEE_NOTES_FUNCTIONALITY)) { %>
                                <md-menu-item class="md-indent" ng-if="showCollaborationMenu">
                                    <md-icon class="fa fa-sticky-note-o"></md-icon>
                                    <md-button aria-label="{{translate.load('sbi.execution.executionpage.toolbar.annotate')}}" class="toolbar-button-custom"
                                            ng-click="noteDocument()">{{translate.load('sbi.execution.executionpage.toolbar.annotate')}}
                                    </md-button>
                                </md-menu-item>
                                <%} %>
                                
                                <!-- 
                                    -----------------------------------------------------------------
                                    --------------------------- SHORTCUTS ---------------------------
                                    -----------------------------------------------------------------
                                -->
                                <!-- 
                                    This DIV will gather all the DOM items, starting from the "Shortcuts" label and ending with the last option
                                    under it. If there are no options available (for themselves), we will disable this DOM element (the DIV) 
                                    completely, in order not to leave this label (for shortcuts) alone. Here we have all criteria gathered along
                                    in one ng-if directive - this includes all criteria from all different options under the "Shortcuts" labels.
                                    @author Danilo Ristovski (danristo, danilo.ristovski@mht.net) 
                                -->
                                <div ng-if="!(executedFrom=='WORKSPACE_ORGANIZER'||isAdmin||isSuperAdmin) || urlViewPointService.showOlapMenu || showScheduled">
                                    
                                    <span class="divider">{{translate.load("sbi.execution.executionpage.toolbar.shortcuts")}}</span>
                                    
                                    <!-- 
                                        Provided an ng-if criteria for this menu option for the executed document (whether it should be available).
                                        @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
                                    -->
                                    <md-menu-item class="md-indent" ng-if="isOrganizerEnabled()">
                                        <md-icon class="fa fa-suitcase"></md-icon>
                                        <md-button ng-disabled="false" class="toolbar-button-custom" ng-click="urlViewPointService.addToWorkspace()"
                                                aria-label="{{translate.load('sbi.execution.executionpage.toolbar.saveview')}}">
                                            {{translate.load('sbi.execution.executionpage.toolbar.savemyworkspace')}}
                                        </md-button>
                                    </md-menu-item>
                                   
                                    <!--  
                                    <md-menu-item class="md-indent">
                                        <md-icon class="fa fa-heart"></md-icon>
                                        <md-button ng-disabled="false" class="toolbar-button-custom" ng-click="urlViewPointService.openFavoriteDefinitionForm()"
                                                aria-label="{{translate.load('sbi.execution.executionpage.toolbar.saveview')}}">
                                            {{translate.load('sbi.execution.executionpage.toolbar.addbookmark')}}
                                        </md-button>
                                    </md-menu-item>
                                    -->
                                    
                                    <!-- 
                                        Provided an ng-if criteria for this menu option for the executed document (whether it should be available).
                                        @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
                                    -->
                                    <md-menu-item class="md-indent" ng-if="showScheduled">
                                        <md-button aria-label="{{translate.load('sbi.execution.executionpage.toolbar.showscheduled')}}"
                                                class="toolbar-button-custom" ng-click="urlViewPointService.getSchedulers()">
                                            {{translate.load('sbi.execution.executionpage.toolbar.showscheduled')}}
                                        </md-button>
                                    </md-menu-item>
                                    
                                    <!-- 
                                        Provided an ng-if criteria for this menu option for the executed document (whether it should be available).
                                        @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
                                    -->
                                    <md-menu-item class="md-indent" ng-if="urlViewPointService.showOlapMenu">
                                        <md-button  aria-label="{{translate.load('sbi.execution.executionpage.toolbar.show.olap.customized')}}"
                                                class="toolbar-button-custom" ng-click="urlViewPointService.getOlapDocs()" >
                                                {{translate.load('sbi.execution.executionpage.toolbar.show.olap.customized')}}
    
                                        </md-button>
                                    </md-menu-item>
                                    
                                </div> 
                                
                                <!-- SHORTCUTS: end -->

                                 <md-menu-item class="md-indent" ng-if="canCopyAndEmbedLink">
                                    <md-icon class="fa fa-share"></md-icon>
                                    <md-button aria-label="{{translate.load('sbi.execution.executionpage.toolbar.annotate')}}" 
                                    		   class="toolbar-button-custom"
                                               ng-click="copyLinkHTML(false)">{{translate.load('sbi.execution.executionpage.toolbar.copyLink')}}
                                    </md-button>
                                </md-menu-item>
                                
                                                                
                                 <md-menu-item class="md-indent" ng-if="canCopyAndEmbedLink">
                                    <md-icon class="fa fa-share"></md-icon>
                                    <md-button aria-label="{{translate.load('sbi.execution.executionpage.toolbar.annotate')}}" 
                                    		   class="toolbar-button-custom"
                                               ng-click="copyLinkHTML(true)">{{translate.load('sbi.execution.executionpage.toolbar.embedHTML')}}
                                    </md-button>
                                </md-menu-item>

                                
                            </md-menu-content>
                        </md-menu>
                    </md-menu-bar>
                
                    <md-button class="md-icon-button" title="close" aria-label="Clear"  ng-if="isCloseDocumentButtonVisible()" ng-click="closeDocument()">
                    	<md-tooltip direction="bottom">{{::translate.load('sbi.general.close')}}</md-tooltip>
                        <md-icon md-font-icon="fa fa-times"></md-icon>
                    </md-button>
                </div>
            </md-toolbar>
            
            
            
            
    <%               }   // close TOOLBAR_VISIBLE CASE
    
        } %>
       
            <div layout="row" flex="grow">
                <!-- "ng-show" is used instead of "ng-if" (or "ng-switch") in order to prevent the iframe reloading -->
                <md-content id="documentFrameContainer" layout="column" flex ng-show="currentView.status == 'DOCUMENT'">  
                      <div layout="row" flex layout-align="center center"
                            ng-hide="urlViewPointService.frameLoaded || hasValidOutputTypeParameter()">
                        <md-progress-circular md-mode="indeterminate" md-diameter="70" ></md-progress-circular>
                        
                    </div>
                    <iframe class="noBorder" id="documentFrame" name="documentFrame"  iframe-onload="iframeOnload()"
                        iframe-set-dimensions-onload flex ng-show="urlViewPointService.frameLoaded">
                    </iframe>
                    <md-sidenav class="md-sidenav-right md-whiteframe-4dp lateralsidenav"  id="parametersPanelSideNav-e"
			                ng-if="'<%=obj.getParametersRegion() %>' == 'east'" md-component-id="parametersPanelSideNav-e" 
			                layout="column"
			                ng-include="'<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/utils/sidenavTemplate/sidenavVertContent.jsp")%>'">
			        </md-sidenav>
                </md-content>
                                        
                <div flex layout ng-if="currentView.status == 'PARAMETERS'"> 
                    <div ng-if="parameterView.status == 'FILTER_SAVED'" layout flex>
                        <parameter-view-point-handler flex layout="column" execProperties="execProperties" execute="execute"/>
                    </div>
                    <div ng-if="parameterView.status == 'SCHEDULER'" layout flex>
                        <document-scheduler flex layout="column"/>
                    </div>
                </div>
                
                <div flex layout ng-if="currentView.status == 'OLAP'"> 
                    <div ng-if="parameterView.status == 'OLAP'" layout flex>
                        <document-olap flex layout="column"/>
                    </div>
                </div>

            </div>
        </div>
        
        

        <script type="text/javascript">
        ///Module creation
        (function() {
            
            angular.module('documentExecutionModule', 
                    ['ngMaterial', 'ui.tree', 'sbiModule', 'document_tree', 'componentTreeModule', 'angular_table', 'ngSanitize', 'expander-box', 'ngAnimate', 'ngWYSIWYG','angular_list','cross_navigation','file_upload','driversExecutionModule', 'datasetPreviewModule']);
            
            
            
            angular.module('documentExecutionModule').factory('execProperties', function() {
                var selRole= '<%= request.getParameter("SELECTED_ROLE") %>'=='null' ? '' : '<%= request.getParameter("SELECTED_ROLE") %>';
                var crossParams= <%= request.getParameter("CROSS_PARAMETER") %>==null ? {} : <%= request.getParameter("CROSS_PARAMETER") %>;
                var menuParams= <%= request.getParameter("MENU_PARAMETERS") %>==null ? {} : <%= request.getParameter("MENU_PARAMETERS") %>;
                var toolbarVisible= <%= request.getParameter("TOOLBAR_VISIBLE") %>==null ? '' : <%= request.getParameter("TOOLBAR_VISIBLE") %>;
                var canResetParameters= <%= request.getParameter("CAN_RESET_PARAMETERS") %>==null ? '' : <%= request.getParameter("CAN_RESET_PARAMETERS") %>;

                var obj = {
                    roles: [<%for(Object roleObj : executionRoleNames) out.print("'" + (String)roleObj + "',");%>],
                    executionInstance: {
                        'OBJECT_ID' : <%= obj.getId() %>,
                        'OBJECT_LABEL' : '<%= obj.getLabel().replaceAll(Pattern.quote("'"), Matcher.quoteReplacement("\\'")) %>',
                        'EDIT_MODE' : '<%= request.getParameter("EDIT_MODE") %>',
                        'OBJECT_NAME' : '<%= obj.getName().replaceAll(Pattern.quote("'"), Matcher.quoteReplacement("\\'")) %>',
                        'REFRESH_SECONDS' : <%= obj.getRefreshSeconds().intValue() %>,
                        'OBJECT_TYPE_CODE' : '<%= obj.getBiObjectTypeCode() %>',
                        'isFromCross' : <%=isFromCross%>,
                        'isPossibleToComeBackToRolePage' : false,
                        'SBI_EXECUTION_ID' : '',
                        'CROSS_PARAMETER' : crossParams,
                        'MENU_PARAMETER' : menuParams,
                        'ENGINE_LABEL' : '',
                        'TOOLBAR_VISIBLE' : toolbarVisible,
                        'CAN_RESET_PARAMETERS' : canResetParameters,     
                        'SidenavOri': '<%=obj.getParametersRegion() %>',
                        'IS_FOR_EXPORT' : <%= isForExport %>
                        <%
                        if(cockpitSelections != null && !cockpitSelections.equalsIgnoreCase("")) {
                        %>
                        , 'COCKPIT_SELECTIONS' : '<%=cockpitSelections%>'
                        <%
                        }
                        %>
                        <%
                        if(request.getParameter("SELECTED_ROLE") != null && !request.getParameter("SELECTED_ROLE").equalsIgnoreCase("")) {
                        %>
                        , 'SELECTED_ROLE' : '<%=request.getParameter("SELECTED_ROLE") %>'
                        <%
                        }
                        %>
                    },
                    parametersData: {
                        documentParameters: []
                    },
                    documentUrl : '',
                    selectedRole : {name : selRole },
                    currentView :  {status : "DOCUMENT"},
                    parameterView : {status : ""},
                    isParameterRolePanelDisabled : {status : false},
                    showParametersPanel : {status : false},
                    hideProgressCircular : {status : true},
                    //FLAGS FOR RESET DEPENDENCIES PARAMETERS VALUE 
                    initResetFunctionVisualDependency : {status : false},
                    returnFromVisualViewpoint : {status : false},
                    initResetFunctionDataDependency : {status : false},
                    returnFromDataDepenViewpoint : {status : false},
                    initResetFunctionLovDependency : {status : false},
                    returnFromLovDepenViewpoint : {status : false}
                
                };
                return obj;
            });
            
            angular.module('documentExecutionModule').service('cockpitEditing',
                    function($mdToast, execProperties, sbiModule_restServices, sbiModule_config, $filter, $mdDialog, sbiModule_translate) {
    <% 
    if(engineName.equalsIgnoreCase( SpagoBIConstants.COCKPIT_ENGINE_NAME)
        && (isAdmin || userId.equals(obj.getCreationUser()))) { 
    %>          
                var cockpitEditingService = this;
                
                cockpitEditingService.documentMode = 'VIEW';
                
                cockpitEditingService.startCockpitEditing = function() {
                
                	cockpitEditingService.documentMode = 'EDIT';
                	//var newUrl = cockpitEditingService.changeDocumentExecutionUrlParameter('documentMode', cockpitEditingService.documentMode);
                    
                    //if(newUrl != undefined && newUrl.length > 0){
                    if(!document.getElementById('postForm_documentMode')){
                    	var element = document.createElement("input");
				        element.type = "hidden";
				        element.id= 'postForm_documentMode';
				        element.name = 'documentMode';
				        element.value = 'VIEW';
				        document.getElementById('postForm_'+execProperties.executionInstance.OBJECT_ID).appendChild(element);	
                    }
                    
                    if(document.getElementById('postForm_documentMode').value == 'VIEW'){
                    	document.getElementById('postForm_documentMode').value = cockpitEditingService.documentMode;
                    	document.getElementById('postForm_'+execProperties.executionInstance.OBJECT_ID).submit();
                	}else{
                		var confirm = $mdDialog.alert()
								.title(sbiModule_translate.load('sbi.execution.executionpage.toolbar.editmode'))
								.content(sbiModule_translate.load('sbi.execution.executionpage.toolbar.editmode.documentnotexecuted'))
								.ariaLabel('Document not executed')
								.ok(sbiModule_translate.load("sbi.general.continue"));
						$mdDialog.show(confirm);
                	}
                };
                
                cockpitEditingService.stopCockpitEditing = function() {
                	/*var action = function() {
						cockpitEditingService.documentMode = 'VIEW';
						var newUrl = cockpitEditingService.changeDocumentExecutionUrlParameter('documentMode', cockpitEditingService.documentMode);
						execProperties.documentUrl = newUrl;
					};*/
					
					if(document.getElementById('postForm_documentMode').value == 'EDIT'){
						var confirm = $mdDialog.confirm()
								.title(sbiModule_translate.load('sbi.execution.executionpage.toolbar.editmode'))
								.content(sbiModule_translate.load('sbi.execution.executionpage.toolbar.editmode.quit'))
								.ariaLabel('Leave edit mode')
								.ok(sbiModule_translate.load("sbi.general.continue"))
								.cancel(sbiModule_translate.load("sbi.general.cancel"));
						$mdDialog.show(confirm).then(function(){
							cockpitEditingService.documentMode = 'VIEW';
							document.getElementById('postForm_documentMode').value = cockpitEditingService.documentMode;
							document.getElementById('postForm_'+execProperties.executionInstance.OBJECT_ID).submit();
						});
					
                	};
                };
                cockpitEditingService.changeDocumentExecutionUrlParameter = function(parameterName, parameterValue) {
                    var docurl = execProperties.documentUrl;
                    if(docurl.length == 0){
                    	return "";
                    }
                    
                    var startIndex = docurl.indexOf('?') + 1;
                    var endIndex = docurl.length;
                    var baseUrl = docurl.substring(0, startIndex);
                    
                    var docUrlPar = docurl.substring(startIndex, endIndex);                    
                    docUrlPar = docUrlPar.replace(/\+/g, " ");
                    
                    var parameterNameLastIndexOf = docUrlPar.lastIndexOf(parameterName);
                                        
                    if(parameterNameLastIndexOf == -1) {
                        docUrlPar = docUrlPar.replace(/&$/g, "");
                        docUrlPar += ("&" + parameterName + "=" + parameterValue);
                    } else {
                        var initialUrlPar = docUrlPar.substring(0, parameterNameLastIndexOf);
                        var middleUrlPar = docUrlPar.substring(parameterNameLastIndexOf);
                        var ampersandCharIndexOf = middleUrlPar.indexOf('&') != -1 ? middleUrlPar.indexOf('&') : middleUrlPar.length;
                        var lastUrlPar = middleUrlPar.substring(ampersandCharIndexOf);
                                                
                        middleUrlPar = (parameterName + "=" + parameterValue);
                        
                        docUrlPar = initialUrlPar + middleUrlPar + lastUrlPar;
                    }
                    
                    var endUrl = baseUrl + docUrlPar;
                    
                    return endUrl;
                };
                
    <%} %>
            });

        })();
        </script>
        <script type="text/javascript" 
                src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/utils/documentExecutionServices.js")%>"></script>
        <script type="text/javascript" 
                src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/utils/documentExecutionExportService.js")%>"></script>
        <script type="text/javascript" 
                src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/utils/documentExecutionFactories.js")%>"></script>
        <script type="text/javascript" 
                src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/parameterViewPointHandler/parameterViewPointHandlerController.js")%>"></script>
        <script type="text/javascript" 
            src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/documentScheduler/documentSchedulerController.js")%>"></script>
        <script type="text/javascript" 
            src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/documentOlap/documentOlapController.js")%>"></script>
        <script type="text/javascript" 
                src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/documentParamenterElement/documentParamenterElementController.js")%>"></script>
        <script type="text/javascript" 
                src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/menuFunctions/infoMetadataService.js")%>"></script>
        <script type="text/javascript" 
                src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/documentExecution.js")%>"></script>
        <script type="text/javascript" 
        		src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/wheelNavigator/ngWheelNavigator.js")%>"></script>
        <script type="text/javascript" 
                src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/documentExecutionNote.js")%>"></script>
        <script type="text/javascript" 
                src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/documentExecutionRank.js")%>"></script>
        <script type="text/javascript" 
                src="<%=urlBuilder.getResourceLink(request, "js/src/angular_1.4/tools/documentexecution/publicExecutionUrl/publicExecutionUrl.js")%>"></script>
        
        
    </body>
<% 
} else {
%>
    <head>
        <%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
    </head>
    <body ng-app="cantExecuteDocumentModule" ng-controller="cantExecuteDocumentController">
        <script>
        (function() {
            angular.module('cantExecuteDocumentModule', 
                    ['ngMaterial', 'sbiModule']);
            
            angular.module('cantExecuteDocumentModule')
            
            .factory('$documentBrowserScope', function($window) {
               
                var f = function(){};
                var fakeScope = {
                        changeNavigationRole: f,
                        closeDocument : f,
                        isCloseDocumentButtonVisible: f
                    };
                
                var ng = $window.parent.angular 
                    || $window.parent.parent.angular; // coming from cockpit DocumentWidget
                
                if(ng && $window.frameElement!=null) {
                    //return ng.element($window.frameElement).scope().$parent.$parent;
                    var scope = ng.element($window.frameElement).scope();
                    if(scope && scope.$parent && scope.$parent.$parent) {
                        return scope.$parent.$parent;
                    } else {
                        return fakeScope;
                    }
                
                } else if(ng ){ // coming from cockpit DocumentWidget
//                  var scope = ng.element($window.parent.parent.frameElement).scope().$parent;
                    var scope = ng.element($window.parent.parent.frameElement).scope();
                    
                    if(scope && scope.$parent) {
                        var scopeParent = scope.$parent;
                        
                        if (!scopeParent.changeNavigationRole) {
                            scopeParent.changeNavigationRole = function(){};
                        }
                        return scopeParent;
                    } else {
                        return fakeScope;
                    }
                    
                } else {
                    return fakeScope
                }
            })
            
            .controller( 'cantExecuteDocumentController', 
                ['$scope', '$mdDialog', 'sbiModule_translate', '$documentBrowserScope', 
                 cantExecuteDocumentController]);

            function cantExecuteDocumentController(
                    $scope, $mdDialog, sbiModule_translate, $documentBrowserScope) {
                
            	<% 
            	if(!canExecuteDocument) {
            	%>
                	var errorMessage = sbiModule_translate.load('sbi.execution.error.novalidproduct');
                <%
            	} else {
                %>
                	var errorMessage = sbiModule_translate.load('sbi.execution.error.novalidrole');
                <%
            	}
            	%>
                var okMessage = sbiModule_translate.load('sbi.general.ok');
                
                
                $mdDialog.show(
                    $mdDialog.alert()
                        .clickOutsideToClose(false)
                        .content(errorMessage)
                        .ariaLabel(errorMessage)
                        .ok(okMessage)
                ).then(function() { 
                    $documentBrowserScope.closeDocument(<%= obj.getId() %>);
                }, function() {});
            };
        })();
        </script>
        
       
        
        
        
    </body>
<% }%>
</html>