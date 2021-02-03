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


<%@ page language="java"
 		 extends="it.eng.spago.dispatching.httpchannel.AbstractHttpJspPagePortlet"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         session="true"
          import="it.eng.spago.base.*,
                 it.eng.spagobi.commons.constants.SpagoBIConstants" 
%>
<%@page import="it.eng.spagobi.services.common.SsoServiceInterface"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.IUrlBuilder"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="it.eng.spagobi.utilities.themes.ThemesManager"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>

<%      
	String userId = (request.getParameter("user_id") == null) ? "" : request.getParameter("user_id");
	String contextName = ChannelUtilities.getSpagoBIContextName(request);
	String authFailed = (request.getAttribute(SpagoBIConstants.AUTHENTICATION_FAILED_MESSAGE) == null) ? ""
			: (String) request.getAttribute(SpagoBIConstants.AUTHENTICATION_FAILED_MESSAGE);
	
	String oldEncMethodMessage = (request.getParameter("old_enc_method_message") == null) ? "" : request.getParameter("old_enc_method_message");
	
	ResponseContainer responseContainer = ResponseContainerAccess.getResponseContainer(request);
	RequestContainer requestContainer = RequestContainer.getRequestContainer();
	
	String currTheme = ThemesManager.getDefaultTheme();
	if (requestContainer != null) {
		currTheme = ThemesManager.getCurrentTheme(requestContainer);
		if (currTheme == null)
			currTheme = ThemesManager.getDefaultTheme();
	
		if (responseContainer != null) {
			SourceBean aServiceResponse = responseContainer.getServiceResponse();
			if (aServiceResponse != null) {
				SourceBean loginModuleResponse = (SourceBean) aServiceResponse.getAttribute("LoginModule");
				if (loginModuleResponse != null) {
					userId = (String) loginModuleResponse.getAttribute("user_id");
					String authFailedMessage = (String) loginModuleResponse.getAttribute(SpagoBIConstants.AUTHENTICATION_FAILED_MESSAGE);
					if (authFailedMessage != null)
						authFailed = authFailedMessage;
	
					String tmpOldEncMethodMessage = (String) loginModuleResponse.getAttribute("old_enc_method_message");
					if (tmpOldEncMethodMessage != null)
						oldEncMethodMessage = tmpOldEncMethodMessage;
	
				}
			}
		}
	
	}
	
	IMessageBuilder msgBuilder = MessageBuilderFactory.getMessageBuilder();
	
	String sbiMode = "WEB";
	IUrlBuilder urlBuilder = null;
	urlBuilder = UrlBuilderFactory.getUrlBuilder(sbiMode);
%>

<!DOCTYPE html>
<html lang="en">
   <head>
      <meta charset="utf-8">
      <meta http-equiv="X-UA-Compatible" content="IE=edge">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
      <title>Knowage</title>
      <link rel="shortcut icon" href="<%=urlBuilder.getResourceLinkByTheme(request, "img/favicon.ico",currTheme)%>" />
      <!-- Bootstrap -->
      <link rel="shortcut icon" href="<%=urlBuilder.getResourceLink(request, "img/favicon.ico")%>" />
		   <!-- Bootstrap -->
      <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular_1.4/angular.js")%>"></script> 
      <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular_1.4/angular-animate.min.js")%>"></script> 
      <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular_1.4/angular-aria.min.js")%>"></script> 
      <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular_1.4/angular-sanitize.min.js")%>"></script> 
      <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular_1.4/angular-messages.min.js")%>"></script> 
      <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular_1.4/angular-cookies.js")%>"></script> 
      <script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular-material_1.1.0/angular-material.min.js")%>"></script> 
      <link rel='StyleSheet' href='<%=urlBuilder.getResourceLink(request, "themes/commons/css/reset_2018.css")%>' type='text/css' />
      <link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular-material_1.1.0/angular-material.min.css")%>">
      <link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "js/lib/bootstrap/css/bootstrap.min.css")%>">
	  <link rel='StyleSheet' href='<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>' type='text/css' />
      <script>
         var app = angular.module('changePwdApp', ['ngMaterial']);
         
         app.controller('changePwdController', ['$scope', '$http', function($scope, $http) {
         
	         $scope.authFailed = "";
	         $scope.isCalling = false;
	         $scope.oldEncMethodMessage = "<%= oldEncMethodMessage %>";
	         $scope.changePwd = function(user) {
	           	$scope.isCalling = true;
	           	$scope.authFailed = "";
	           	
		        $http({
		        	method: 'PUT',
		        	data: $scope.changePwdData,
		        	headers: { "Accept": "text/plain" },
		        	url: '<%= urlBuilder.getResourceLink(request, "/restful-services/credential") %>'
		        }).then(function successCallback(response) {
		        	var status = response.status;
		        	$scope.authFailed = response.data;
		        	$scope.isCalling = false;
		        }, function errorCallback(response) {
		        	var status = response.status;
		        	$scope.authFailed = response.data;
		        	$scope.isCalling = false;
		        });
		     };
         
         }]);
      </script>
   </head>
   <body ng-app="changePwdApp" class="kn-changePassword" ng-controller="changePwdController">
      <!-- NEW FORM -->
      <div layout="row" style="width:100%" layout-align="center center">
	      <md-card flex="60" layout-padding>
	         <div id="oldEncMethodMessage" layout="row" layout-align="center center" >
	         	<div class="kn-info" flex>{{oldEncMethodMessage}}</div>
	         </div>
	         <div layout="row" layout-align="center center" class="noPadding" >
	         	<img id="profile-img" class="logoHeader" src='<%=urlBuilder.getResourceLink(request, "themes/commons/img/defaultTheme/logoCover.svg")%>' flex="60" flex-xs="100" aria-hidden="true"/>
	         </div>
	         <p id="profile-name" class="profile-name-card"></p>
	         <%--             <form class="form-signin"  role="form" action="<%=contextName%>/ChangePwdServlet" method="POST"> --%>
	         <div layout="row" layout-align="center center">
		         <form class="form-signin"  role="form" ng-submit="changePwd()" flex>
		            <fieldset ng-disabled="isCalling">
		               <input type="hidden" id="MESSAGE" name="MESSAGE" value="CHANGE_PWD" />
		               <input type="hidden" id="user_id" name="user_id" value="<%=userId%>" />
		               <label><%=msgBuilder.getMessage("change_password_here")%></label>
		               <input id="username" type="text" size="30"  class="form-control" placeholder="<%=msgBuilder.getMessage("username")%>" required autofocus ng-model="changePwdData.userId">
		               <input id="oldPassword" type="password" size="30"  class="form-control" placeholder="<%=msgBuilder.getMessage("old_password")%>" required ng-model="changePwdData.oldPassword">
		               <input id="NewPassword" type="password" size="30"  class="form-control" placeholder="<%=msgBuilder.getMessage("new_password")%>" required ng-model="changePwdData.newPassword">
		               <input id="NewPassword2" type="password" size="30" class="form-control" placeholder="<%=msgBuilder.getMessage("retype_new_password")%>" required ng-model="changePwdData.newPasswordConfirm">
		               <button class="btn btn-lg btn-primary btn-block btn-signin" type="submit">Change Password</button> 	
		               <button class="btn btn-lg btn-primary btn-block btn-signup" type="button" onclick="window.location.href='<%=urlBuilder.getResourceLink(request, "")%>'">Login</button>
		            </fieldset>
		            <div><label>{{authFailed}}</label></div>
		
		         </form>
	         </div>
	         <!-- /form -->
	         </md-card><!-- /card-container -->
         <spagobi:error/>
      </div>
      <!-- /container -->
      <!-- Include all compiled plugins (below), or include individual files as needed -->
      <script src="<%=urlBuilder.getResourceLink(request, "js/lib/jquery-1.11.3/jquery-1.11.3.min.js")%>"></script>
      <script src="<%=urlBuilder.getResourceLink(request, "js/lib/bootstrap/bootstrap.min.js")%>"></script>
      <script>
         $(document).ready(function(){
            // Select all elements with data-toggle="tooltips" in the document
         $('[data-toggle="tooltip"]').tooltip(); 
         
         // Select a specified element
         $('#myTooltip').tooltip();
         });
      </script>		
      <!-- END NEW FORM -->
   </body>
</html>
