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
	
	<!-- Angular Material Libraries -->
	<script src="libraries/angular-material_0.10.0/angular-material.js"></script>
	<link rel="stylesheet" href="libraries/angular-material_0.10.0/angular-material.min.css">
	
	<!--  Angular Table and relative utility libraries -->
	<script type="text/javascript" src="libraries/contextmenu/ng-context-menu.min.js"></script>
	<script type="text/javascript" src="libraries/pagination/dirPagination.js"></script>
	<link rel="stylesheet" href="libraries/font-awesome-4.4.0/css/font-awesome.min.css">
	<script type="text/javascript" src="libraries/angular-table/AngularTable.js"></script>
	<link rel="stylesheet" type="text/css" href="libraries/angular-table/AngularTable.css">
	
	
	<!-- Controller for this page -->
	<script type="text/javascript" src="js/controllerCalc.js"></script>	


	<!-- Styles -->
	<link rel="stylesheet" href="css/knowageCalc.css">
	<title>Knowage Cost Web Calculator</title>
</head>
<body layout-margin layout-padding ng-controller="calculatorRuntimeCtrl as ctrl">
	<md-toolbar flex> <div class="md-toolbar-tools">Knowage Price Calculator </div> </md-toolbar>
	<md-content layout-margin layout-padding flex layout="column" layout-align="center center" ng-if=ctrl.showCalculate>
		  
		  
		  
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


	<md-content ng-if=ctrl.showSimpleResults>
		<p>
			Cost silver Subscription = {{ctrl.silverCost}}€
		</p>
		
		<p>	
			Cost gold Subscription   = {{ctrl.goldCost}}€	
		</p>
		
	</md-content>
	
	<md-content ng-if=ctrl.showTableResults>
					<!-- {{ctrl.categoryData}} -->
					<!--<angular-table 	id='categoriesTable' 	ng-model=ctrl.categoryData
											       columns='["Category","goldPrice","silverPrice"]'
											highlights-selected-item = "true"
											no-pagination="false"
					></angular-table>-->
					<div layout="row" layout-align="center center">
						<table>
						  <tr>
						    <th>Max number of users</th>
						    <th>Silver price</th> 
						    <th>Gold price</th>
						  </tr>
						  <tr ng-repeat="category in ctrl.categoryData">
						    <td>{{category.Category}}</td>
						    <td>{{category.silverPrice}}€</td>
						    <td>{{category.goldPrice}}€</td>					    
						  </tr>
						</table>
					</div>


		
	</md-content>
	
	
	<md-content ng-if=ctrl.productsOEMintDataResults  layout-margin layout-padding flex layout="column" layout-align="center center" >

		<div>
			<p>Silver prices:</p>
			<table>
			  <tr>
			    <th class="oemIntTab">Products</th>
			    <th class="oemIntTab">Max 1 client</th> 
			    <th class="oemIntTab">Max 20 clients</th> 
			    <th class="oemIntTab">Max 50 clients</th>
			    <th class="oemIntTab">Max 100 clients</th> 
			    <th class="oemIntTab">Max 200 clients</th>
			    <th class="oemIntTab">Unlimited clients</th>
			  </tr>
			  <tr ng-repeat="product in ctrl.productsOEMintDataSilver">
			  	<td class="oemIntTab">{{product.products}}</td>
			  	<td class="oemIntTab">{{product.max_1_clients_price}}€</td>
			    <td class="oemIntTab">{{product.max_20_clients_price}}€</td>
			    <td class="oemIntTab">{{product.max_50_clients_price}}€</td>
			    <td class="oemIntTab">{{product.max_100_clients_price}}€</td>
			    <td class="oemIntTab">{{product.max_200_clients_price}}€</td>	
			    <td class="oemIntTab">{{product.Unlimited_max_number_of_clients_price}}€</td>						    
			  </tr>
			</table>
		</div>
		
		<div>
			<p>Gold prices:</p>
			<table>
			  <tr>
			    <th class="oemIntTab">Products</th>
				<th class="oemIntTab">Max 1 client</th> 	    
			    <th class="oemIntTab">Max 20 clients</th> 
			    <th class="oemIntTab">Max 50 clients</th>
			    <th class="oemIntTab">Max 100 clients</th> 
			    <th class="oemIntTab">Max 200 clients</th>
			    <th class="oemIntTab">Unlimited clients</th>
			  </tr>
			  <tr ng-repeat="product in ctrl.productsOEMintDataGold">
			  	<td class="oemIntTab">{{product.products}}</td>
			  	<td class="oemIntTab">{{product.max_1_clients_price}}€</td>	
			    <td class="oemIntTab">{{product.max_20_clients_price}}€</td>
			    <td class="oemIntTab">{{product.max_50_clients_price}}€</td>
			    <td class="oemIntTab">{{product.max_100_clients_price}}€</td>
			    <td class="oemIntTab">{{product.max_200_clients_price}}€</td>	
			    <td class="oemIntTab">{{product.Unlimited_max_number_of_clients_price}}€</td>						    
			  </tr>
			</table>
		</div>


		
	</md-content>
	
	
	




</body>
</html>