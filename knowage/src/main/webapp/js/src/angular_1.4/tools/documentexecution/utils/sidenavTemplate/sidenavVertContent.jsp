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
<md-button ng-cloak class="toolbar-button-custom md-raised" ng-disabled="paramRolePanelService.isExecuteParameterDisabled()" ng-click="executeParameter()" ng-hide="isParameterPanelDisabled()">
	{{::translate.load("sbi.execution.parametersselection.executionbutton.message")}}
</md-button>		