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


<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<html ng-app="BlankApp">
<head>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>

  <style>

    .md-container {
      position: static;
      display: -webkit-flex;
      display: -ms-flexbox;
      display: flex;
      box-sizing: border-box;
      -webkit-flex-direction: column;
      -ms-flex-direction: column;
      flex-direction: column;
      z-index: 60;
      bottom: 0;
      overflow: auto;
    }

    /* ===============
      queste classi sono per visualizzare le icone di fontawesome come anche lo stile che ne carica il cdn
      diteci voi a quale libreria rifarci
     =================*/

    .s64 {
      font-size:64px;
    }

    .s32 {
      
      font-size:24px;

    }

    md-icon.fa {
      display:block;
      padding-left:0px;
    }

    md-icon.s32 span {
      padding-left:8px;
    }

  </style>

<script type="text/javascript">    
    var app = angular.module('BlankApp', ['ngMaterial','angular-list-detail']);
    app.controller('parentController', ["$scope",parFunc]);
    function parFunc($scope){
    	$scope.addFunc=function(){
    		console.log("add");
    	}
    	
    	$scope.saveFunc=function(){
    		console.log("saveFunc");
    	}
    	
    	$scope.deletFunc=function(){
    		console.log("deletFunc");
    	}
    	
    	$scope.test1="ciao"
    }
    
    app.controller('viewController', ["$scope",viewFunc]);
    function viewFunc($scope){
    	
    	
    	$scope.test="ciao22222"
    }
  </script>
  
</head>
<body>

<angular-list-detail ng-controller="parentController" new-function="addFunc" save-function="saveFunc" cancel-function="deletFunc" >
	<list  label='lista'   >
	</list>
	<detail label='dettaglio' >
	</detail>
</angular-view-detail>

</body>
</html>
