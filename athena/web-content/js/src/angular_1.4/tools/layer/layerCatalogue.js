var app = angular.module('layerWordManager', [ 'ngMaterial', 'ui.tree',
		'angularUtils.directives.dirPagination', 'ng-context-menu',
		'angular_rest', 'angular_list' ]);

app.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey').accentPalette(
			'blue-grey');
});

app.service('translate', function() {
	this.load = function(key) {
		return messageResource.get(key, 'messages');
	};
});


var showme = false;

var EmptyLayer = {
		name : "",
		label : "",
		descr : "",
		type : "",
		baseLayer : false,
		propsUrl : "",
		propsName : "",
		propsLab: "",
		propsZoom : "",
		propsId :"",
		propsCentralPoint : "",

};
//this variable is used to split add new entry from update entry
//var flag = false;


app.controller('Controller', [ "translate", "restServices", "$scope","$mdDialog", funzione ]);




function funzione(translate, restServices, $scope, $mdDialog) {
	$scope.translate = translate;
	$scope.layerList = [];
	$scope.selectLayer = {};
	$scope.image = "/athena/web-content/WEB-INF/jsp/tools/layer/img/logo_globo.png";
	$scope.flag=false;
	
	$scope.loadLayer = function(){
		console.log("dentro loadLayer");
		restServices.get("layers", '').success(
				function(data, status, headers, config) {
					
					console.log(data);
					if (data.hasOwnProperty("errors")) {
						console.log("layer non Ottenuti bo");
					} else {
						$scope.layerList = data.root;
						

					}
					
				}).error(function(data, status, headers, config) {
					console.log("layer non Ottenuti " + status);
			
				})
				
		
	}

	$scope.listType = [
	                {value : 'File', label : translate.load("sbi.tools.layer.props.type.file")},
	                {value : 'WFS', label: translate.load("sbi.tools.layer.props.type.wfs")},
	                {value : 'WMS', label: translate.load("sbi.tools.layer.props.type.wms")},
	                {value : 'TMS', label: translate.load("sbi.tools.layer.props.type.tms")},
	                {value : 'Google', label: translate.load("sbi.tools.layer.props.type.google")},
	                {value : 'OSM', label: translate.load("sbi.tools.layer.props.type.osm")}
	                      
	                      ];

	
	$scope.loadLayer();
	//$scope.translate = translate;
	
	$scope.saveLayer = function(){
		if($scope.flag){
			
	//		console.log("contengo:");
	//		console.log($scope.selectedLayer.layerFile);
			restServices.put("layers", '', $scope.selectedLayer).success(
					
					function(data, status, headers, config) {
						console.log(data)
					
						if (data.hasOwnProperty("errors")) {
							console.log("layer non Ottenuti");
							
						} else {
							//$scope.layerList = selectedLayer;
							$scope.flag=false;
							$scope.loadLayer();
							
						}
						
					}).error(function(data, status, headers, config) {
						console.log("layer non Ottenuti " + status);
						$scope.loadLayer();
					})
					
					
		} else{
		//	console.log("contengo:");
		//	console.log($scope.selectedLayer.layerFile);
			restServices.post("layers", '', $scope.selectedLayer).success(
					
					function(data, status, headers, config) {
						console.log(data)
						
						if (data.hasOwnProperty("errors")) {
							console.log("layer non Ottenuti");
							
						} else {
							//$scope.layerList = selectedLayer;
							$scope.loadLayer();
						}
						
					}).error(function(data, status, headers, config) {
						
						console.log("layer non Ottenuti " + status);
						$scope.loadLayer();
					})
					
				
			
		}
				
	}

	$scope.loadLayerList = function(item){
		//function calls when you clic on the list of layers
		if(item!= null){
			//siamo in una condizione di caricamento dati dalla lista
			if($scope.selectedLayer != null){
				console.log("beccato");
				var confirm = $mdDialog
				.confirm()
				.title(translate.load("sbi.layer.modify.progress"))
				.content(
						translate
								.load("sbi.layer.modify.progress.message.modify"))
				.ariaLabel('Lucky day').ok(
						translate.load("sbi.general.continue")).cancel(
						translate.load("sbi.general.cancel"));

				$mdDialog.show(confirm).then(function() {
					$scope.selectedLayer = item;	

				}, function() {
					console.log('Annulla');
				});

			} else {
				console.log("carico"+item);
				$scope.flag = true;
				$scope.selectedLayer = item;
			}
		} else {
			if($scope.selectedLayer != null ){
				console.log("beccato");
				var confirm = $mdDialog
				.confirm()
				.title(translate.load("sbi.layer.modify.progress"))
				.content(
						translate
								.load("sbi.layer.modify.progress.message.modify"))
				.ariaLabel('Lucky day').ok(
						translate.load("sbi.general.continue")).cancel(
						translate.load("sbi.general.cancel"));

				$mdDialog.show(confirm).then(function() {
					$scope.cancel();		

				}, function() {
					console.log('Annulla');
				});

			} else if($scope.selectedLayer == null ) {
				$scope.newLayer = JSON.parse(JSON.stringify(EmptyLayer));
				$scope.showme=true;
			}
		}
		

	}
		
	

	$scope.cancel = function(){
		console.log("Reset");
		$scope.selectedLayer = {};
		$scope.flag=false;
	
	}
	
	$scope.menuLayer= [{
		label : translate.load('sbi.generic.delete'),
		action : function(item,event) {
			console.log("prima");
			$scope.selectedLayer = item;
			console.log("dopo");
			console.log("cancello:"+item);
			
			$scope.deleteLayer();
			
		}
	}];	
	
	$scope.deleteLayer = function(){
		
		restServices.remove("layers", 'deleteLayer',"id="+$scope.selectedLayer.id).success(
				
				function(data, status, headers, config) {
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						console.log("layer non Ottenuti");
					} else {
						//$scope.layerList = data.root;
						$scope.loadLayer();
					}
					
				}).error(function(data, status, headers, config) {
					console.log("layer non Ottenuti " + status);
			
				})
		$scope.cancel();
		

	}
	
	
		
	
};


	
