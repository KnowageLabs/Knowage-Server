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

<%@ page language="java" pageEncoding="UTF-8" session="true"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="it.eng.spagobi.tools.dataset.service.SelfServiceDatasetAction" %>
<%@page import="java.util.Map" %>
<%@page import="org.json.JSONObject"%>
<%@page import="java.util.HashMap"%>	
<%@page import="it.eng.spagobi.commons.utilities.messages.IMessageBuilder"%>
<%@page import="it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory"%>
<%@page import="java.util.HashMap"%>	
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%
	List comunities = (request.getAttribute("communities")==null)?new ArrayList():(List)request.getAttribute("communities");
	Map data = (request.getAttribute("data")==null)?new HashMap():(Map)request.getAttribute("data");
	String name = (data.get("name")==null)?"":(String)data.get("name");
	String surname = (data.get("surname")==null)?"":(String)data.get("surname");
	String email = (data.get("email")==null)?"":(String)data.get("email");
	String username = (data.get("username")==null)?"":(String)data.get("username");
	String userIn = (data.get("userIn")==null)?"":(String)data.get("userIn");
    boolean activeSignup = (request.getAttribute("activeSignup")==null)?false:(Boolean)request.getAttribute("activeSignup");
%>

<!DOCTYPE html>
<html>

<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<meta name="apple-mobile-web-app-capable" content="yes">
	<meta name="apple-mobile-web-app-title" content="Knowage">
	<title>Knowage - Modify Account</title>
	<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, " js/lib/angular/angular-material_1.1.0/angular-material.min.css ")%>">
	<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, " themes/commons/css/customStyle.css ")%>"> </head>

<body class="kn-account" ng-app="signUp" ng-controller="modifyCtrl" ng-cloak>
	<div layout="row" layout-align="center center" layout-fill class="signUpContainer">
		<md-card flex=50 flex-xs=100>
			<form name="signUpForm">
				<md-card-content layout="column" layout-align="start center"> <img class="headerLogoImg" src="<%=urlBuilder.getResourceLink(request, " themes/commons/img/defaultTheme/logotipo.svg ")%>" width="300px">
					<h3>Modify profile</h3>
					<div layout="row" layout-wrap>
						<md-input-container class="md-block" flex=50 flex-xs=100>
							<label>Name</label>
							<input ng-model="newUser.name" name="name" type="text" required> </md-input-container>
						<md-input-container class="md-block" flex=50 flex-xs=100>
							<label>Surname</label>
							<input ng-model="newUser.surname" name="surname" type="text" required> </md-input-container>
						<md-input-container class="md-block" flex=50 flex-xs=100>
							<label>Username</label>
							<input ng-model="newUser.username" name="username" type="text" required ng-disabled="true"> </md-input-container>
						<md-input-container class="md-block" flex=50 flex-xs=100>
							<label>Email address</label>
							<input ng-model="newUser.email" name="email" type="text" ng-pattern="emailFormat"> </md-input-container>
						<md-input-container class="md-block" flex=50 flex-xs=100>
							<label>Password</label>
							<input ng-model="newUser.password" name="password" type="password" required> </md-input-container>
						<md-input-container class="md-block" flex=50 flex-xs=100>
							<label>Confirm Password</label>
							<input ng-model="newUser.confirmPassword" name="confirmPassword" type="password" required> </md-input-container>
					</div>
				</md-card-content>
				<md-card-actions layout="column" layout-align="center">
					<md-button class="md-primary md-raised" ng-click="update()" type="submit">Modify</md-button>
					<md-button class="md-primary md-raised goTologin" ng-click="eraseAccount()" ng-if="activeSignup">Delete Account</md-button>
				</md-card-actions>
			</form>
		</md-card>
	</div>
	<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, " themes/sbi_default/fonts/font-awesome-4.4.0/css/font-awesome.min.css ")%>">
	<!-- angular reference-->
	<!-- START-DEBUG -->
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, " js/lib/angular/angular_1.4/angular.js ")%>"></script>
	<!-- END-DEBUG -->
	<!-- START-PRODUCTION 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/angular_1.4/angular.min.js")%>"</script> 
	END-PRODUCTION -->
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, " js/lib/angular/angular_1.4/angular-animate.min.js ")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, " js/lib/angular/angular_1.4/angular-aria.min.js ")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, " js/lib/angular/angular_1.4/angular-sanitize.min.js ")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, " js/lib/angular/angular_1.4/angular-messages.min.js ")%>"></script>
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, " js/lib/angular/angular_1.4/angular-cookies.js ")%>"></script>
	<!-- Angular Material Library -->
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request," /js/lib/angular/angular-material_1.1.0/angular-material.min.js ")%>"></script>
	
	<!-- Your application bootstrap  -->
	<script type="text/javascript">    

	    angular.module('signUp', ['ngMaterial','sbiModule'])
	    .controller('modifyCtrl', function($scope,$http,$window,$mdToast,$timeout,$mdDialog,sbiModule_messaging) {
		  $scope.newUser = {};
		  $scope.newUser.name = '<%=StringEscapeUtils.escapeJavaScript(name)%>';
		  $scope.newUser.surname = '<%=StringEscapeUtils.escapeJavaScript(surname)%>';
		  $scope.newUser.email = '<%=StringEscapeUtils.escapeJavaScript(email)%>';
		  $scope.newUser.username = '<%=StringEscapeUtils.escapeJavaScript(username)%>';
		  $scope.activeSignup= <%=activeSignup%>;
		  $scope.emailFormat = /^[a-zA-Z0-9.!#$%&*+\/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)+$/;
		  $scope.$watch("newUser.confirmPassword", function(newValue, oldValue) {
			    if (newValue == $scope.newUser.password) {
			    	$scope.signUpForm.confirmPassword.$setValidity('correct',true) ;
			    }else{
			    	$scope.signUpForm.confirmPassword.$setValidity('correct',false) ;
			    }
			});
		  $scope.eraseAccount = function(ev){
			  //$window.location.href = '<%=urlBuilder.getResourceLink(request, "servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE")%>';
			   // Appending dialog to document.body to cover sidenav in docs app
			    var confirm = $mdDialog.confirm()
			          .title('Would you like to delete your account?')
			          .textContent('If you delete your account you will not be able to login again inside Knowage.')
			          .ariaLabel('Erase account')
			          .targetEvent(ev)
			          .ok('Ok')
			          .cancel('Cancel');
	
			    $mdDialog.show(confirm).then(function() {
			     //OK
					  $http.post('<%=urlBuilder.getResourceLink(request, "restful-services/signup/delete?SBI_EXECUTION_ID=-1")%>'
							  , $scope.newUser)
					  .then(function(response) {
						  if(response.data.errors){
								$mdToast.show(
								    $mdToast.simple()
								      .textContent(response.data.errors[0].message)
								  );
						  }else{
							  $mdToast.show(
							    $mdToast.simple()
							      .textContent('Account deleted')
							      .hideDelay(3000)
							  );
							  $timeout(function(){
								  $window.parent.location.href = '<%=urlBuilder.getResourceLink(request, "/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE&NEW_SESSION=TRUE")%>';
							  }, 3000);
							  
						  }
					  });
				       
				//end
							     
			    }, function() {
			     //cancel
			    });
		  }
		  $scope.update = function(){
			  if($scope.signUpForm.$valid){
				  if($scope.newUser.password != $scope.newUser.confirmPassword){
					  $scope.signUpForm.confirmPassword.$setValidity('incorrect', false);
					  $mdToast.show(
					    $mdToast.simple()
					      .textContent('the password inserted are different')
					  );
					  return;
				  }
				  $http.post('<%=urlBuilder.getResourceLink(request, "restful-services/signup/update?SBI_EXECUTION_ID=-1")%>'
						  , $scope.newUser)
				  .then(function(response) {
					  if(response.data.errors){
							sbiModule_messaging.showAlertMessage('Account not updated',response.data.errors[0].message);
					  }else{
						  sbiModule_messaging.showAlertMessage('Account correctly updated');
					  }
				  });
			  }else{
				  return;
			  }
		  }
		});
  	</script>
  </body>
</html>
