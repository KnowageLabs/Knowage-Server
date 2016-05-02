<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html >
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
				
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
		<link rel="stylesheet" href="css/knowageCalc.css">

		<title>Knowage Price Calculator</title>
	</head>




	<body ng-app="calcManager" ng-controller="calculatorRuntimeCtrl as ctrl" ng-cloak >
		<md-toolbar flex class="md-hue-1">
		<div class="md-toolbar-tools">
			<h2>
			<span>Knowage Price Calculator</span>
			</h2>
			
		</div>
		</md-toolbar>
		<md-content layout-padding layout-xs="column" layout="row" ng-if=ctrl.showCalculate>
			<md-card layout="column" flex>
			<img ng-src="img/banner.png" class="md-card-image" alt="Knowage banner">
			
			<md-card-title>
			<md-card-title-text>
			<span class="md-headline">Select your products</span>
			</md-card-title-text>
			</md-card-title>
			<md-card-content>
			
			<div layout-gt-sm="row" layout="column"  flex layout-align="center center">
				<!-- ho dovuto spezzare in due l'elenco per far si che fosse responsive, se riesci a fare 2 ng-repeat è meglio -->
	
				<div class="kn-product" ng-repeat="item in ctrl.items" ng-click="ctrl.addProduct(item)">
					<img ng-src="img/{{item}}.png" class="kn-product-logo {{ctrl.itemDetail[item].selected}}"/>			
					<md-tooltip md-direction="bottom">
						{{ctrl.itemDetail[item].name}}
					</md-tooltip>
				</div> 
				
			</div>
			<div layout="row" layout-padding layout-margin layout-align="center center">
				<md-input-container>
				<label>Number of cores</label>
				<md-select ng-model="ctrl.selectedNumCores">
				<md-option ng-repeat="coreNum in ctrl.cores" value="{{coreNum}}" ng-selected="{{coreNum == '8'}}">
				{{coreNum}}
				</md-option>
				</md-select>
				</md-input-container>
			</div>
			</md-card-content>
			<md-card-actions layout="row" layout-align="end center">
			<md-button  class="md-raised md-primary" ng-click="ctrl.getCost()">Calculate</md-button>
			</md-card-actions>
			</md-card>
		</md-content>
		
		
		<md-content layout-padding layout-xs="column" layout="row" ng-if="ctrl.showSimpleResults"> 
			<md-card layout="column" flex>
				
					<p>
						Cost silver Subscription = {{ctrl.silverCost}} &euro;
					</p>
				
					<p>	
						Cost gold Subscription   = {{ctrl.goldCost}} &euro;	
					</p>		
					
			</md-card>	
		</md-content>
		 
		
		
		
		
		
		<md-content layout-padding layout-xs="column" layout="column" ng-if="ctrl.productsOEMintDataResults">
			<md-card layout="column" flex>
			<img ng-src="img/banner.png" class="md-card-image" alt="Knowage banner" ng-if="!ctrl.showCalculate">
			
	
			<md-card-title>
			<md-card-title-text>
			<span class="md-headline">Silver prices:</span>
			</md-card-title-text>
			</md-card-title>
			<md-card-content>
			




			<md-list>
				<md-list-item class="list-toolbar">
					<span flex >Products</span>
					<span flex>Max 1 client</span>
					<span flex>Max 20 clients</span>
					<span flex>Max 50 clients</span>
					<span flex>Max 100 clients</span>
					<span flex>Max 200 clients</span>
					<span flex>Unlimited clients</span>
				</md-list-item>
				<md-list-item  ng-class-even="'list-even'" ng-class-odd="'list-odd'" ng-repeat="product in ctrl.productsOEMintDataSilver">
					<span flex hide-xs hide-sm layout="row"> <!--  togliere layout= row se si vogliono mettere le scritte -->
						<div layout-padding ng-repeat="p in product.products.split(',')">
							<img ng-src="img/{{p}}.png" width="40"/>
						</div>
					</span>
					<span flex hide-gt-sm>{{product.products}}</span>
					<span flex>{{product.max_1_clients_price}}&euro;</span>
					<span flex>{{product.max_20_clients_price}}&euro;</span>
					<span flex>{{product.max_50_clients_price}}&euro;</span>
					<span flex>{{product.max_100_clients_price}}&euro;</span>
					<span flex>{{product.max_200_clients_price}}&euro;</span>	
					<span flex>{{product.Unlimited_max_number_of_clients_price}}&euro;</span>						    
				</md-list-item >
			</md-list>
			
			</md-card-content>
			</md-card>
			
			
			
			<md-card layout="column" flex>
			<img ng-src="img/banner.png" class="md-card-image" alt="Knowage banner" ng-if=false>
			
	
			<md-card-title>
			<md-card-title-text>
			<span class="md-headline">Gold prices:</span>
			</md-card-title-text>
			</md-card-title>
			<md-card-content>
			


			<md-list>
				<md-list-item class="list-toolbar">
					<span flex >Products</span>
					<span flex>Max 1 client</span>
					<span flex>Max 20 clients</span>
					<span flex>Max 50 clients</span>
					<span flex>Max 100 clients</span>
					<span flex>Max 200 clients</span>
					<span flex>Unlimited clients</span>
				</md-list-item>
				<md-list-item  ng-class-even="'list-even'" ng-class-odd="'list-odd'" ng-repeat="product in ctrl.productsOEMintDataGold">
					<span flex hide-xs hide-sm layout="row"> <!--  togliere layout= row se si vogliono mettere le scritte -->
						<div layout-padding ng-repeat="p in product.products.split(',')">
							<img ng-src="img/{{p}}.png" width="40"/>
						</div>
					</span>
					<span flex hide-gt-sm>{{product.products}}</span>
					<span flex>{{product.max_1_clients_price}}&euro;</span>
					<span flex>{{product.max_20_clients_price}}&euro;</span>
					<span flex>{{product.max_50_clients_price}}&euro;</span>
					<span flex>{{product.max_100_clients_price}}&euro;</span>
					<span flex>{{product.max_200_clients_price}}&euro;</span>	
					<span flex>{{product.Unlimited_max_number_of_clients_price}}&euro;</span>						    
				</md-list-item >
			</md-list>
			
			</md-card-content>
			</md-card>
						
			
		</md-content>
		
		
		
		
		
		<md-content layout-padding layout-xs="column" layout="column" ng-if="ctrl.showTableResults">
			<md-card layout="column" flex>
			<img ng-src="img/banner.png" class="md-card-image" alt="Knowage banner" ng-if="!ctrl.showCalculate">
			
	
			<md-card-title>
			<md-card-title-text>
			<span class="md-headline">Prices:</span>
			</md-card-title-text>
			</md-card-title>
			<md-card-content>
			
				
				<md-list>
					<md-list-item class="list-toolbar">
						<span flex>Max number of clients</span>
						<span flex>Silver Price</span>
						<span flex>Gold Price</span>
					</md-list-item>
					<md-list-item  ng-class-even="'list-even'" ng-class-odd="'list-odd'" ng-repeat="category in ctrl.categoryData">
						<span flex>{{category.Category}}</span>
						<span flex>{{category.silverPrice}}&euro;</span>
						<span flex>{{category.goldPrice}}&euro;</span>					    
					</md-list-item >
				</md-list>
				
					

			
			
			
			</md-card-content>
			</md-card>
		
		</md-content>
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	</body>
</html>