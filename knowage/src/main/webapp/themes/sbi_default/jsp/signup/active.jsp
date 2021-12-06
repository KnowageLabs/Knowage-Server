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
<%@page import="it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory"%>
<%@page import="it.eng.spagobi.commons.utilities.urls.IUrlBuilder"%>
<%@page import="it.eng.spagobi.utilities.themes.ThemesManager"%>

 <%
 IUrlBuilder urlBuilder = UrlBuilderFactory.getUrlBuilder("WEB");
 String baseUrl = urlBuilder.getResourceLink(request, "restful-services/signup/active");
 String getURL=request.getRequestURL().toString().substring(0, request.getRequestURL().toString().indexOf("knowage")) + "knowage/";
 String currTheme = ThemesManager.getDefaultTheme();
%>
	
<html>
<head>
	    <title>Knowage - Activation</title>
    	<link rel="shortcut icon" href="<%=urlBuilder.getResourceLinkByTheme(request, "img/favicon.ico",currTheme)%>" />
		   <!-- Bootstrap -->
		<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request,"/js/lib/angular/angular-material_1.1.0/angular-material.min.css")%>">
    	<link rel="stylesheet" href="<%=urlBuilder.getResourceLink(request, "themes/commons/css/customStyle.css")%>">
    	<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/angular/angular_1.4/angular.js")%>"></script> 
</head>
  <body class="kn-activation" ng-app="userActivation" ng-controller="userActivationController" ng-cloak> 
  	<div class="top layout-row layout-xs-column">
  		<div class="logoKnowage">
  			<img align="center" alt="Image" border="0" src="https://www.knowage-suite.com/site/wp-content/uploads/2016/03/KNOWAGE_logo_color.png" title="Image" width="250">
  		</div>
  		<div class="social">
			<a href="https://www.facebook.com/knowagesuite/" target="_blank">
				<img alt="Facebook" height="32" src="https://www.knowage-suite.com/landing/external_resources/activationMail/images/facebook@2x.png" style="text-decoration:none;height:auto;border:none;display:block" title="Facebook" width="32" />
			</a>
			<a href="http://twitter.com/knowage_suite" target="_blank">
				<img alt="Twitter" height="32" src="https://www.knowage-suite.com/landing/external_resources/activationMail/images/twitter@2x.png" style="text-decoration:none;height:auto;border:none;display:block" title="Twitter" width="32" />
			</a>
			<a href="http://linkedin.com/company/knowage" target="_blank">
				<img alt="LinkedIn" height="32" src="https://www.knowage-suite.com/landing/external_resources/activationMail/images/linkedin@2x.png" style="text-decoration:none;height:auto;border:none;display:block" title="LinkedIn" width="32" />
			</a>
			<a href="https://www.youtube.com/channel/UCikC4ug3psdnzpwNMDY9Q7g?sub_confirmation=1" target="_blank">
				<img alt="YouTube" height="32" src="https://www.knowage-suite.com/landing/external_resources/activationMail/images/youtube@2x.png" style="text-decoration:none;height:auto;border:none;display:block" title="YouTube" width="32" />
			</a>
			<a href="mailto:https://www.knowage-suite.com/site/newsletter-subscription/" target="_blank">
				<img alt="E-Mail" height="32" src="https://www.knowage-suite.com/landing/external_resources/activationMail/images/mail@2x.png" style="text-decoration:none;height:auto;border:none;display:block" title="E-Mail" width="32" />
			</a>
		</div>
  	</div>
  	<div class="center">
  		<div class="loading" ng-if="loading">
  			<p>{{translate.load('sbi.general.loading')}}</p>
  		</div>
  		<div class="activation" ng-if="activation" >
  			<p>{{activation}}</p>
			<button class="md-button md-raised md-primary md-knowage-theme" onclick="location.href='<%=getURL%>'">{{translate.load('sbi.execution.sendmail.login')}}</button>
  		</div>
  		<div class="error" ng-if="error">
  			<p>{{error}}</p>
  			<!-- <button class="md-button md-raised md-primary md-knowage-theme" ng-if="!expired">{{translate.load('sbi.generic.back')}}</button>
  			<button class="md-button md-raised md-primary md-knowage-theme" ng-if="expired">{{translate.load('signup.active.resend')}}</button> -->
  		</div>
  		<div class="colorOverlay" ng-class="{'warning':error, 'success': !error}"></div>
  	</div>
  	<div class="bottom layout-row layout-xs-column">
  		<div class="third flex-33 flex-xs-100">
  			<a href="https://knowage-suite.readthedocs.io/en/{{version}}/" target="_blank"> 
  				<img align="center" alt="Image" border="0" src="https://www.knowage-suite.com/site/wp-content/uploads/2019/10/book.png" style="text-decoration:none;height:auto;border:none;width:100%;max-width:58px;display:block" title="Image" width="58"/>
  			</a>
  			<p>
  				{{translate.load('signup.active.documentation')}}
  			</p>
  		</div>
  		<div class="third flex-33 flex-xs-100">
  			<a href="https://www.knowage-suite.com/qa/" target="_blank"> 
  				<img align="center" alt="Image" border="0" src="https://www.knowage-suite.com/site/wp-content/uploads/2019/10/question-sign.png" style="text-decoration:none;height:auto;border:none;width:100%;max-width:58px;display:block" title="Image" width="58"/>
  			</a>
  			<p>
  				{{translate.load('signup.active.qa')}}
  			</p>
  		</div>
  		<div class="third flex-33 flex-xs-100">
  			<a href="https://github.com/KnowageLabs/Knowage-Server" target="_blank"> 
  				<img align="center" alt="Image" border="0" src="https://www.knowage-suite.com/site/wp-content/uploads/2019/10/data-graphic.png" style="text-decoration:none;height:auto;border:none;width:100%;max-width:58px;display:block" title="Image" width="58"/>
  			</a>
  			<p>
  				{{translate.load('signup.active.github')}}
  			</p>
  		</div>
  	</div>
    <script>
   var baseUrl = "<%= baseUrl %>";
    
    angular.module('userActivation',[])
    	.config(['$locationProvider', function($locationProvider) { 
    		$locationProvider.html5Mode({ enabled: true, requireBase: false }); }]
    	)
    	.service('sbiModule_translate', function() {
			this.addMessageFile = function(file){
				messageResource.load([file,"messages"], function(){});
			};
			
			this.load = function(key,sourceFile) {
				var sf= sourceFile == undefined? 'messages' : sourceFile;
				return messageResource.get(key, sf);
			};
			
			this.format = function() {
			    // The string containing the format items (e.g. "{0}")
			    // will and always has to be the first argument.
			    var theString = arguments[0];
			    
			    // start with the second argument (i = 1)
			    for (var i = 1; i < arguments.length; i++) {
			        // "gm" = RegEx options for Global search (more than one instance)
			        // and for Multiline search
			        var regEx = new RegExp("\\{" + (i - 1) + "\\}", "gm");
			        theString = theString.replace(regEx, arguments[i]);
			    }
			    
			    return theString;
			}
			
		})
    	.controller('userActivationController',userActivationController)
    	
    	function userActivationController($scope, $location, $http, sbiModule_translate){
    		$scope.translate = sbiModule_translate;
    		$scope.loading = true;
    		$scope.location = $location;
    		debugger;
    		$scope.version = $location.search().version.match(/^[0-9]{1,2}\.[0-9]{1,2}$/) ? $location.search().version : "master";
    		
    		 $http.get(baseUrl + "?token="+$location.search().token+"&locale="+$location.search().locale+"&version="+$scope.version)
	    		 .then(function(response){
	    			 $scope.loading = false;
	    			 if(response.data.errors){
	    				 $scope.error = response.data.errors;
	    				 $scope.expired = response.data.expired;
	    			 }else{
	    				 $scope.activation = response.data.message;
	    			 }
	        	}, function(error){
	        		$scope.loading = false;
	        		$scope.error = 'connection error';
	        	});
    	}

	</script>
  </body>
</html>
