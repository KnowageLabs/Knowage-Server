/**
 * 
 */
var app = angular.module('dataSourceModule', ['ngMaterial', 'angular_list', 'angular_table' ,'sbiModule', 'angular_2_col']);

app.controller('dataSourceController', ["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast", dataSourceFunction]);

var EmptyDataSource = {
		
		label:"",
		descr : "",
		dialect_id: "",
		multishcema:"",
		readOnly:"",
		writeDefault:"",
		type:"",
		url:"",
		user:"",
		password:"",
		driver:""
			
};

function dataSourceFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast){
	
	$scope.showme=false;
	
	$scope.translate = sbiModule_translate;
	$scope.dataSourceList = [];
	$scope.dialects = [];
	$scope.selectedDataSource = {};
		
	$scope.getDataSources = function(){
		console.log("Get DSRC");
		sbiModule_restServices.get("datasources", '').success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						//change sbi.glossary.load.error
						console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
					} else {
						$scope.dataSourceList = data.root;
						console.log($scope.dataSourceList);
						
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

				})	
	}
	
	$scope.getDataSources();
	
	$scope.loadDialects = function() {
		sbiModule_restServices.get("domains", "listValueDescriptionByType","DOMAIN_TYPE=DIALECT_HIB").success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						//change sbi.glossary.load.error
						/*showToast(sbiModule_translate.load("sbi.glossary.load.error"),
								3000);*/
						console.log(sbiModule_translate.load("sbi.glossary.load.error"),3000);
					} else {

						$scope.dialects = data;
						console.log("took the domains")
						console.log($scope.dialects);
					
					
					}
				}).error(function(data, status, headers, config) {
			showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

		})
	}
	$scope.loadDialects();
	
	$scope.loadDataSourceList = function(item) {
		
		$scope.showme=true;
		console.log(item)
		$scope.selectedDataSource = item;
		console.log(item)
		
		
	}
	
	$scope.fdsSpeedMenuOptAD = [ 			 		               	
		 		               	{
		 		               		label: sbiModule_translate.load("sbi.federationdefinition.info"),
		 		               		icon:"fa fa-info-circle",
		 		               		backgroundColor:'green',
		 		               		action : function(ev) {
		 		               				ctr.showDSDetails(ev);
		 		               			}
		 		               	},
		 		               {
		 		               		label: sbiModule_translate.load("sbi.federationdefinition.info"),
		 		               		icon:"fa fa-info-circle",
		 		               		backgroundColor:'green',
		 		               		action : function(ev) {
		 		               				ctr.showDSDetails(ev);
		 		               			}
		 		               	}
		 		             ];
	
	$scope.menuDataSource= [{
		label : sbiModule_translate.load('sbi.generic.delete'),
		action : function(item,event) {
			$scope.selectedDataSource = item;

			var confirm = $mdDialog
			.confirm()
			.title(sbiModule_translate.load("sbi.layer.delete.action"))
			.content(
					sbiModule_translate
					.load("sbi.layer.modify.progress.message.modify"))
					.ariaLabel('Lucky day').ok(
							sbiModule_translate.load("sbi.general.continue")).cancel(
									sbiModule_translate.load("sbi.general.cancel"));

			$mdDialog.show(confirm).then(function() {
				//$scope.deleteLayer();	

			}, function() {
				console.log('Annulla');
			});


		}
	}];
	
	$scope.saveDataSource = function(){
				
		console.log("***********************")
		console.log($scope.selectedDataSource);
		console.log("***********************")
		//$scope.item = JSON.Parse($scope.selectedDataSource);
		
		sbiModule_restServices.post("datasources",'',$scope.selectedDataSource).success(
				function(data, status, headers, config) {
					console.log(data)

					/*if (data.hasOwnProperty("errors")) {
						console.log("has errors property");
						

					} else {
						console.log("sacuvao!")
					
					}*/

				}).error(function(data, status, headers, config) {
					console.log("nije sacuvao")
				})
				
		$scope.showme = false;
		$scope.loadDialects();
	}
	
	$scope.cancel = function(){
		console.log("CANCEL");
		$scope.showme=false;
		
		/*if($scope.flag==true){
			//c'è un layer caricato
			$scope.isRequired=false;
			$scope.selectedLayer = angular.copy($scope.object_temp);
			$scope.rolesItem=$scope.loadRolesItem($scope.selectedLayer);
			$scope.filter_set = [];
			for(var i=0;i<$scope.selectedLayer.properties.length;i++){
				console.log($scope.selectedLayer.properties[i]);
				var prop = $scope.selectedLayer.properties[i];
				var obj={"property":prop};
				$scope.filter_set.push(obj );
				console.log(obj);
			}
			
			
		} else{
			console.log("Reset");
			$scope.selectedLayer = angular.copy({});
			$scope.rolesItem=[];
			$scope.flag=false;
			$scope.isRequired=false;
			$scope.filter_set=[];
			$scope.filter =[];
	
		}*/
		
		//$scope.forms.contactForm.$setPristine();
		//$scope.forms.contactForm.$setUntouched();
		
	}
	
	$scope.deleteItem=function(){
		  console.log("delete");
		 }
	
	$scope.paSpeedMenu= [
	                      {
	                      label:'delete',
	                      icon:'fa fa-minus',
	                      backgroundColor:'red',
	                      color:'white',
	                      action:function(item){
	                       $scope.deleteItem(item);
	                      }
	                      }
	                     ];

	
};