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

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-title" content="Knowage">
    <title>Knowage - Signup</title>
    <link rel="shortcut icon" href="<%=urlBuilder.getResourceLinkByTheme(request, "img/favicon.ico",currTheme)%>" />
		   <!-- Bootstrap -->
		<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "js/lib/bootstrap/css/bootstrap.min.css")%>">
		<link rel="stylesheet" type="text/css"  href="<%= urlBuilder.getResourceLink(request,"/node_modules/toastify-js/src/toastify.css")%>">
    	<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
  </head>

  <body class="kn-login" ng-app="signUp" ng-controller="signUpCtrl" ng-cloak>
 	<div class="container-fluid signUpContainer" style="height:100%;">
  		<div class="col-sm-5 col-sm-offset-7" style="height:100%;background-color:white;display:flex;flex-direction:column;padding:20px;justify-content:center;align-items:center">
  			<img id="profile-img" class="col-xs-10" src='<%=urlBuilder.getResourceLinkByTheme(request, "../commons/img/defaultTheme/logoCover.svg", currTheme)%>' />
        	<h3><%=msgBuilder.getMessage("signup")%></h3>
  			<div class="col-xs-8">
      			<form name="signUpForm" class="form-signin">
		        	<input type="text" id="name" name="name" class="form-control smallerInput" ng-model="newUser.name" placeholder="<%=msgBuilder.getMessage("signup.form.name")%>" required autofocus>
		        	<input type="text" id="surname" name="surname" class="form-control smallerInput" ng-model="newUser.surname" placeholder="<%=msgBuilder.getMessage("signup.form.surname")%>" required>
		        	<input type="text" id="username" name="username" class="form-control smallerInput" ng-model="newUser.username" placeholder="<%=msgBuilder.getMessage("signup.form.username")%>" required>
		        	<input type="email" id="email" name="email" class="form-control smallerInput" ng-model="newUser.email" placeholder="<%=msgBuilder.getMessage("signup.form.email")%>" required>
		        	<input type="password" id="password" name="password" class="form-control smallerInput" ng-model="newUser.password" placeholder="<%=msgBuilder.getMessage("signup.form.password")%>" required>
		        	<input type="password" id="confirmPassword" name="confirmPassword" class="form-control smallerInput" ng-model="newUser.confirmPassword" placeholder="<%=msgBuilder.getMessage("signup.form.confirmpassword")%>" required>
					<div id="sticky" class="captcha" style="background-image:url('<%=urlBuilder.getResourceLink(request, "stickyImg")%>');background-size: contain;"></div>
					<input type="text" id="captcha" name="captcha" class="form-control smallerInput" ng-model="newUser.captcha" placeholder="<%=msgBuilder.getMessage("signup.form.captcha")%>" required>
	
	          		<button class="btn btn-lg btn-primary btn-block btn-signin" ng-click="register()" type="submit"><%=msgBuilder.getMessage("signup.form.register")%></button>
           		</form>
          			<button class="btn btn-lg btn-primary btn-block btn-signup" ng-click="goToLogin()"><%=msgBuilder.getMessage("login")%></button>
          	</div>
         </div>
     </div>
  
	<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "themes/sbi_default/fonts/font-awesome-4.4.0/css/font-awesome.min.css")%>">
	
	<!-- angular reference-->
	<!-- START-DEBUG -->
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/angular_1.4/angular.js")%>"></script> 
	<!-- END-DEBUG -->
	
	
	<!-- START-PRODUCTION 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/angular_1.4/angular.min.js")%>"</script> 
	END-PRODUCTION -->
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/angular_1.4/angular-animate.min.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/angular_1.4/angular-aria.min.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/angular_1.4/angular-sanitize.min.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/angular_1.4/angular-messages.min.js")%>"></script> 
	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/angular_1.4/angular-cookies.js")%>"></script> 


  	<!-- Angular Material Library -->
  	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular-material_1.1.0/angular-material.min.js")%>"></script>
  
  	<!-- Your application bootstrap  -->
  	<script type="text/javascript">    

    angular.module('signUp', ['ngMaterial'])
    .controller('signUpCtrl', function($scope,$http,$window,$timeout) {
    	$scope.popup = function(type,message){
    		Toastify({
    			text: message,
    			duration: 10000,
    			close: true,
    			className: type == 'error' ? 'kn-warningToast' : 'kn-infoToast',
    			stopOnFocus: true
    		}).showToast();
    	}
    	
    	
	  $scope.newUser = {};
	  $scope.emailFormat = /^[a-z]+[a-z0-9._]+@[a-z]+\.[a-z.]{2,5}$/;
	  $scope.$watch("newUser.confirmPassword", function(newValue, oldValue) {
		    if (newValue == $scope.newUser.password) {
		    	$scope.signUpForm.confirmPassword.$setValidity('correct',true) ;
		    }else{
		    	$scope.signUpForm.confirmPassword.$setValidity('correct',false) ;
		    }
		});
	  $scope.goToLogin = function(){
		  $window.location.href = '<%=urlBuilder.getResourceLink(request, "servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE")%>';
		  
	  }
	  $scope.register = function(){
		  if($scope.signUpForm.$valid){
			  if($scope.newUser.password != $scope.newUser.confirmPassword){
				  $scope.signUpForm.confirmPassword.$setValidity('incorrect', false);
				  $mdToast.show(
				    $mdToast.simple()
				      .textContent('the password inserted are different')
				  );
				  return;
			  }
			  $http.post('<%=urlBuilder.getResourceLink(request, "restful-services/signup/create?SBI_EXECUTION_ID=-1")%>', $scope.newUser)
			  .then(function(response) {
				  if(response.data.errors){
					  $scope.popup('error',response.data.errors[0].message)
				  }else{
					  $scope.popup('message',response.data.message)
					  $scope.newUser = {};
					  $timeout(function(){
						  $window.parent.location.href = '<%=urlBuilder.getResourceLink(request, "servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE")%>';
					  }, 5000);
				  }
				  
				  // Reset the captcha
				  angular.element(document.getElementById("sticky"))
				  	.css("background-image", "url('<%= urlBuilder.getResourceLink(request, "stickyImg") %>?" + Math.random() + "')");
				  $scope.newUser.captcha = "";
				  
			  });
		  }else{
			  return;
		  }
		  
	  }
		
	});
    
    
  	</script>
  	<script type="text/javascript" src="<%= urlBuilder.getResourceLink(request,"/node_modules/toastify-js/src/toastify.js")%>"></script>
  </body>
</html>
