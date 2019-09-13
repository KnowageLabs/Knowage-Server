<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="it.eng.spagobi.commons.utilities.UserUtilities"%>

<%
//	HttpSession session2222 = request.getSession();
IEngUserProfile profile = (IEngUserProfile)session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);;
%>
	

<md-toolbar class="header secondaryToolbar" ng-hide="isParameterPanelDisabled()" 
		ng-if="!(execProperties.isFromDocumentWidget)" >
		<div class="md-toolbar-tools" layout="row" layout-align="space-around center">
			<md-button aria-label="Reset Parameter" class="md-icon-button" ng-click="clearListParametersForm();">
				<md-tooltip md-delay="500" >{{::translate.load("sbi.execution.parametersselection.toolbar.clear")}}</md-tooltip>
				<md-icon md-font-icon="fa fa-eraser"></md-icon>
			</md-button>	
			<% 
			if(UserUtilities.haveRoleAndAuthorization(profile, null, new String[]{SpagoBIConstants.SEE_VIEWPOINTS_FUNCTIONALITY}) || 
							UserUtilities.haveRoleAndAuthorization(profile, SpagoBIConstants.ADMIN_ROLE_TYPE, new String[0])){
			%>					
			<md-button aria-label="Open Saved Parameters" class="md-icon-button" 
					ng-click="urlViewPointService.getViewpoints();">
					<md-tooltip md-delay="500" >{{::translate.load("sbi.execution.parametersselection.toolbar.open")}}</md-tooltip>
					<md-icon md-font-icon="fa fa-pencil"></md-icon>
			</md-button>	
			<md-button aria-label="Save Parameters" class="md-icon-button"	ng-click="urlViewPointService.createNewViewpoint();">
					<md-tooltip md-delay="500" >{{::translate.load("sbi.execution.parametersselection.toolbar.save")}}</md-tooltip>
					<md-icon md-font-icon="fa fa-floppy-o"></md-icon>
			</md-button>
		  	<%} %>
	  	</div>
</md-toolbar>
		
<div ng-show="showSelectRoles" ng-cloak layout-padding>
	<div class="kn-info" ng-if="!selectedRole.name">
		{{::translate.load("sbi.execution.parametersselection.info.selectuser")}}
	</div>
	<md-input-container class="md-block">
		<label>{{::translate.load("sbi.users.roles")}}</label>
		<md-select aria-label="aria-label" ng-model="selectedRole.name" ng-disabled="::crossNavigationScope.isNavigationInProgress()" >
			<md-option ng-click="changeRole(role)" ng-repeat="role in roles" value="{{role}}">
				{{::role|uppercase}}
			</md-option>
		</md-select>
	</md-input-container>
</div>

<md-content>
	<div ng-hide="isParameterPanelDisabled()" layout="{{filterDropping}}">
		<document-paramenter-element execProperties="execProperties" parameter="parameter" ng-repeat="parameter in documentParameters" ng-show="parameter.visible" layout="row" layout-align="start" />
	</div>
</md-content>
<div flex></div>
			
<!-- execute button -->
<div layout="row" layout-align="center center" class="kn-buttonBar">
	<md-button ng-cloak class="toolbar-button-custom md-raised kn-primaryButton" ng-disabled="paramRolePanelService.isExecuteParameterDisabled()" ng-click="executeParameter()" ng-hide="isParameterPanelDisabled()" flex>
		{{::translate.load("sbi.execution.parametersselection.executionbutton.message")}}
	</md-button>
	<md-menu ng-if="executionInstance.OBJECT_TYPE_CODE=='DOCUMENT_COMPOSITE'" ng-hide="isParameterPanelDisabled()">
	
      <md-button class="toolbar-button-custom md-raised" ng-class="{'kn-functionButton':executionInstance.OBJECT_TYPE_CODE=='DOCUMENT_COMPOSITE'}" ng-disabled="paramRolePanelService.isExecuteParameterDisabled()" ng-click="$mdOpenMenu();" md-menu-origin="">
        <md-icon md-font-icon="fa fa-chevron-down"></md-icon>
      </md-button>
      <md-menu-content width="4" class="md-dense">
        <md-menu-item>
          <md-button ng-click="exportCsv($event)">
            {{::translate.load("sbi.execution.parametersselection.executionbutton.exportcsv")}}
          </md-button>
        </md-menu-item>
      </md-menu-content>
    </md-menu>
	<!-- md-button ng-cloak class="toolbar-button-custom md-raised" ng-disabled="paramRolePanelService.isExecuteParameterDisabled()" ng-click="exportCsv()" ng-hide="isParameterPanelDisabled()" ng-if="executionInstance.OBJECT_TYPE_CODE=='DOCUMENT_COMPOSITE'">
		Export CSV
	</md-button-->
</div>