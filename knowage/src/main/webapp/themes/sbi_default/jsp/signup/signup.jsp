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

<%@ page language="java" pageEncoding="utf-8" session="true"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="it.eng.spagobi.tools.dataset.service.SelfServiceDatasetAction" %>
<%@page import="java.util.Map" %>
<%@page import="org.json.JSONObject"%>


<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-title" content="Knowage">
    <title>Knowage - Signup</title>
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/js/lib/angular/angular-material_1.1.0/angular-material.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/themes/commons/css/customStyle.css">
  </head>

  <body class="kn-login" ng-app="signUp" ng-controller="signUpCtrl" ng-cloak>
  
  
  	<div layout="row" layout-align="center center" layout-fill class="signUpContainer">
      <md-card flex=50 flex-xs=100>
        <md-card-content layout="column" layout-align="start center">
        	<img class="headerLogoImg" src="${pageContext.request.contextPath}/themes/sbi_default/img/wapp/logo.png">
        	<h3>SignUp</h3>
        	<div layout="row" layout-wrap>
        		<md-input-container class="md-block" flex=50 flex-xs=100>
			        <label>Name</label>
			        <input ng-model="newUser.name" type="text">
			      </md-input-container>
			      <md-input-container class="md-block" flex=50 flex-xs=100>
			        <label>Surname</label>
			        <input ng-model="newUser.surname" type="text">
			      </md-input-container>
			      <md-input-container class="md-block" flex=50 flex-xs=100>
			        <label>Username</label>
			        <input ng-model="newUser.username" type="text">
			      </md-input-container>
			      <md-input-container class="md-block" flex=50 flex-xs=100>
			        <label>Email address</label>
			        <input ng-model="newUser.email" type="text">
			      </md-input-container>
			      <md-input-container class="md-block" flex=50 flex-xs=100>
			        <label>Password</label>
			        <input ng-model="newUser.password" type="password">
			      </md-input-container>
			      <md-input-container class="md-block" flex=50 flex-xs=100>
			        <label>Confirm Password</label>
			        <input ng-model="newUser.confirmPassword" type="password">
			      </md-input-container>
			      <div flex=50 flex-xs=100>
			      	<div id="sticky" style="background-image:url('${pageContext.request.contextPath}/stickyImg')">
			      		<!--  img src='${pageContext.request.contextPath}/stickyImg' width='250px' height='75px' /-->
			      	</div>
			      	</div>
			      <md-input-container class="md-block" flex=50 flex-xs=100>
			        <label>Insert captcha</label>
			        <input ng-model="newUser.captcha" type="text">
			      </md-input-container>
			
        	</div>
          
        </md-card-content>
        <md-card-actions layout="column" layout-align="center">
          <md-button class="md-primary md-raised" ng-click="register()">Register</md-button>
        </md-card-actions>
      </md-card>
     </div>
  
	<link rel="stylesheet" href="${pageContext.request.contextPath}/themes/sbi_default/fonts/font-awesome-4.4.0/css/font-awesome.min.css">

	<!-- angular reference-->
	<!-- START-DEBUG -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular.js"></script> 
	<!-- END-DEBUG -->
	
	<!-- START-PRODUCTION 
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular.min.js"></script> 
	END-PRODUCTION -->
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-animate.min.js"></script> 
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-aria.min.js"></script> 
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-sanitize.min.js"></script> 
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-messages.min.js"></script> 
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/angular/angular_1.4/angular-cookies.js"></script> 

  	<!-- Angular Material Library -->
  	<script src="https://ajax.googleapis.com/ajax/libs/angular_material/1.1.0/angular-material.min.js"></script>
  
  	<!-- Your application bootstrap  -->
  	<script type="text/javascript">    

    angular.module('signUp', ['ngMaterial'])
    .controller('signUpCtrl', function($scope,$http) {
	  $scope.helloworld = 'hello World';
	  $scope.newUser = {};
	  $scope.register = function(){
		  $http.post('${pageContext.request.contextPath}/restful-services/signup/create?SBI_EXECUTION_ID=-1', $scope.newUser)
		  .then(function(response) {console.log(response)},
				  function(error) {alert(error)})
	  }
		
	});
    
    
  	</script>
  </body>
</html>
