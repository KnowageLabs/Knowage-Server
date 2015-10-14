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

app.directive("fileread", [function () {
    return {
        scope: {
            fileread: "="
        },
        link: function (scope, element, attributes) {
            element.bind("change", function (changeEvent) {
                scope.$apply(function () {
                    scope.fileread = changeEvent.target.files[0];
                    // or all selected files:
                    // scope.fileread = changeEvent.target.files;
                });
            });
        }
    }
}]);

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
var flag = false;


app.controller('Controller', [ "translate", "restServices", "$scope", funzione ]);




function funzione(translate, restServices, $scope) {
	$scope.translate = translate;
	$scope.layerList = [];
	$scope.selectLayer = {};
	$scope.newLayer = function(){
	//EmptyLayer serve poi per il reset
	$scope.newLayer = JSON.parse(JSON.stringify(EmptyLayer));
		
	
	}
	$scope.loadLayer = function(){
		restServices.get("layers", '').success(
				function(data, status, headers, config) {
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						console.log("layer non Ottenuti");
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
		if(flag){
			flag=false;
			console.log("dentro if")
			restServices.put("layers", '', $scope.selectedLayer).success(
					function(data, status, headers, config) {
						console.log(data)
						if (data.hasOwnProperty("errors")) {
							console.log("layer non Ottenuti");
							
						} else {
							//$scope.layerList = selectedLayer;
	
						}
						
					}).error(function(data, status, headers, config) {
						console.log("layer non Ottenuti " + status);
				
					})
		} else{
			console.log("dentro else")
			restServices.post("layers", '', $scope.selectedLayer).success(
					function(data, status, headers, config) {
						console.log(data)
						if (data.hasOwnProperty("errors")) {
							console.log("layer non Ottenuti");
							
						} else {
							//$scope.layerList = selectedLayer;
	
						}
						
					}).error(function(data, status, headers, config) {
						console.log("layer non Ottenuti " + status);
				
					})
		}
				
	}

	$scope.loadLayerList = function(item){
		//function calls when you clic on the list of layers
		console.log(item);
		flag = true;
		$scope.selectedLayer = item;

	}
		
	

	$scope.cancel = function(){
		console.log("Reset");
		$scope.selectedLayer = EmptyLayer;
		flag=false;
		
	}
	
		  
};


	
