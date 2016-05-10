<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="it.eng.spagobi.commons.utilities.UserUtilities"%>

<%
	//	HttpSession session2222 = request.getSession();
	IEngUserProfile profile = (IEngUserProfile)session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);;
		
	%>
	

	<md-toolbar class="header secondaryToolbar" ng-hide="isParameterPanelDisabled()">
			<div layout="row" layout-align="{{sidenavCenter}}">	
				<md-button title="Reset" aria-label="Reset Parameter" class="toolbar-button-custom" 
						ng-click="clearListParametersForm();">
					<i class="fa fa-eraser" style="color:white"></i>
				</md-button>	
				<% 
				if(UserUtilities.haveRoleAndAuthorization(profile, null, new String[]{SpagoBIConstants.SEE_VIEWPOINTS_FUNCTIONALITY}) || 
								UserUtilities.haveRoleAndAuthorization(profile, SpagoBIConstants.ADMIN_ROLE_TYPE, new String[0])){
				%>					
				<md-button title="Open Saved" aria-label="Open Saved Parameters" class="toolbar-button-custom" 
						ng-click="urlViewPointService.getViewpoints();">
					<i class="fa fa-pencil" style="color:white"></i>
				</md-button>	
				  <%} %>
				<% 
				if(UserUtilities.haveRoleAndAuthorization(profile, null, new String[]{SpagoBIConstants.SEE_VIEWPOINTS_FUNCTIONALITY}) || 
								UserUtilities.haveRoleAndAuthorization(profile, SpagoBIConstants.ADMIN_ROLE_TYPE, new String[0])){
				%>			
				<md-button title="Save" aria-label="Save Parameters" class="toolbar-button-custom" 
						ng-click="urlViewPointService.createNewViewpoint();">
					<i class="fa fa-floppy-o" style="color:white"></i>
				</md-button>
			  <%} %>
			</div>
		</md-toolbar>
		
		<md-content ng-show="showSelectRoles" ng-cloak>
			<md-input-container class="small counter" flex>
				<label>{{::translate.load("sbi.users.roles")}}</label>
				<md-select aria-label="aria-label" ng-model="selectedRole.name" ng-disabled="::crossNavigationScope.isNavigationInProgress()" >
					<md-option ng-click="changeRole(role)" ng-repeat="role in roles" value="{{role}}">
						{{::role|uppercase}}
					</md-option>
				</md-select>
			</md-input-container>
		</md-content>

		<md-content flex>
			<md-list ng-hide="isParameterPanelDisabled()" layout="{{filterDropping}}">
				<md-list-item ng-repeat="parameter in documentParameters"
						layout="row" aria-label="" class="md-3-line">

					<document-paramenter-element parameter="parameter" 
							layout="row" flex layout-align="start" ng-show="parameter.visible"/>
					
				</md-list-item>
			</md-list>
		</md-content>
		
		<!-- execute button -->
		<md-button ng-cloak class="toolbar-button-custom md-raised" ng-disabled="paramRolePanelService.isExecuteParameterDisabled()"
				title="{{::translate.load('sbi.execution.parametersselection.executionbutton.message')}}"  
				ng-click="executeParameter()" ng-hide="isParameterPanelDisabled()">
			{{::translate.load("sbi.execution.parametersselection.executionbutton.message")}}
		</md-button>		