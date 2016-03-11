<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="calcManager">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	
	<!--Angular.js Libraries -->
	<script src="libraries/angular_1.4/angular.js"></script>
	<script src="libraries/angular_1.4/angular-sanitize.min.js"></script>
	<script src="libraries/angular_1.4/angular-messages.min.js"></script>
	<script src="libraries/angular_1.4/angular-aria.min.js"></script>
	<script src="libraries/angular_1.4/angular-animate.min.js"></script>
	
	<!-- Angular Material Library -->
	<script src="libraries/angular-material_0.10.0/angular-material.js"></script>
	
	<!-- Angular Material style sheet -->
	<link rel="stylesheet" href="libraries/angular-material_0.10.0/angular-material.min.css">
	

	
	
	
	
	<script type="text/javascript" src="js/controllerCalc.js"></script>	


	<!-- Styles -->
	<link rel="stylesheet" href="css/knowageCalc.css">
	<title>Knowage Cost Web Calculator</title>
</head>
<body layout-margin layout-padding ng-controller="calculatorRuntimeCtrl as ctrl">
	<md-toolbar flex> <div class="md-toolbar-tools">Knowage Price Calculator </div> </md-toolbar>
	<md-content layout-margin layout-padding flex layout="column" layout-align="center center">
		  
		  
		  
		<fieldset class="standard" >
          	<legend>Selected Products</legend>
          	<div layout="row" layout-wrap flex>
           		<div flex ng-repeat="item in ctrl.items">
              		<md-checkbox ng-checked="ctrl.exists(item, ctrl.selected)" ng-click="ctrl.toggle(item, ctrl.selected)">
               		{{item}}
               		</md-checkbox>
            	</div>
          	</div>
        </fieldset>
		<div layout="row" layout-padding layout-margin>
			<md-input-container>
		       	<label>Number of cores</label>
		       	<md-select ng-model="ctrl.selectedNumCores">
		         		<md-option ng-repeat="coreNum in ctrl.cores" value="{{coreNum}}" ng-selected="{{coreNum == '8'}}">
		           		{{coreNum}}
		         		</md-option>
		       	</md-select>
		   	</md-input-container>		     
        </div>	
		
		<md-button class="md-raised md-primary" ng-click="ctrl.getCost()">Calculate</md-button>

		
	</md-content>


	<md-content ng-if=ctrl.showResults>
		<p>
			Cost silver Subscription = {{ctrl.silverCost}}€
		</p>
		
		<p>	
			Cost gold Subscription   = {{ctrl.goldCost}}€	
		</p>
		
	</md-content>
	












</body>
</html>