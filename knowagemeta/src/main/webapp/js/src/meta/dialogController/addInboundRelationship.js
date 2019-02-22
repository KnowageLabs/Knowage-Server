angular.module('metaManager').controller('businessModelInboundController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout','$mdDialog','sbiModule_config','metaModelServices',businessModelInboundControllerFunction ]);
function businessModelInboundControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout,$mdDialog,sbiModule_config,metaModelServices){
	$scope.isInbound = function(item) {
		return !angular.equals(item.sourceTableName,$scope.selectedBusinessModel.uniqueName);
	}

	$scope.inboundColumns = [{label:'Name',name:'name'},
	                      {label:sbiModule_translate.load("sbi.meta.source.table"),name:'sourceTableName'},
	                      {label:sbiModule_translate.load("sbi.meta.source.columns"),name:'sourceColumns',transformer:function(data){
	                    	  var retD = [];
	                    	  data.forEach(function(entry) {
	                    		    retD.push(entry.name);
	                    		  }, this);
	                    	  return retD.join(", ")
	                      }},
	                      {label:sbiModule_translate.load("sbi.meta.target.table") ,name:'destinationTableName'},
	                      {label:sbiModule_translate.load("sbi.meta.target.columns"),name:'destinationColumns',transformer:function(data){
	                    	  var ret = [];
	                    	  data.forEach(function(entry) {
	                    		    ret.push(entry.name);
	                    		  }, this);
	                    	  return ret.join(", ")
	                      }}
	                      ];

	$scope.addNewInbound = function(){
			$mdDialog.show({
				controller: inboundModelPageControllerFunction,
				preserveScope: true,
				locals: {businessModel:$scope.meta.businessModels,businessViews:$scope.meta.businessViews, selectedBusinessModel:$scope.selectedBusinessModel, sbiModule_restServices:sbiModule_restServices,metaModelServices:metaModelServices},
				templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/inboundModel.jsp',
				clickOutsideToClose:true,
				escapeToClose :true,
				fullscreen: true,
				});
	}

	$scope.inboundFunctions = {
			translate:sbiModule_translate,
			addNewInbound:$scope.addNewInbound
	};

	$scope.inboundActionButton=[
	                            {
								label : 'delete',
								 icon:'fa fa-trash' ,
								action : function(item,event) {
									//call server to apply delete on model
									var send = metaModelServices.createRequestRest(item);
									sbiModule_restServices.promisePost("1.0/metaWeb","deleteBusinessRelation",send)
									.then(function(response){
										metaModelServices.applyPatch(response.data);
									}
									,function(response){
										sbiModule_restServices.errorHandler(response.data,"");
									})
								 }
                    			}
						];


}

function inboundModelPageControllerFunction($scope,$mdDialog, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout, businessModel, selectedBusinessModel,metaModelServices,businessViews){
	$scope.translate = sbiModule_translate;
	$scope.cardinality = [{name:'1..N to 1',value:'many-to-one'},
        				  {name:'0..N to 1',value:'optional-many-to-one'}];
	$scope.businessName;
	$scope.businessModel = angular.copy(businessModel);
	$scope.businessViews = angular.copy(businessViews);
	$scope.selectedBusinessModel = angular.copy(selectedBusinessModel);
	$scope.leftElement = {};
	$scope.rightElement = {};
	$scope.dataSend = {};

	$scope.cardinalityValue = 0;

	$scope.tableToSimpleBound = function( model ){
		var a = [];
		if(model){
			if(model.columns)
				model.columns.forEach(function(item){
					//add only the column ( not calculated field)
					if(!item.hasOwnProperty("referencedColumns")){
						a.push({name:item.name,uname:item.uniqueName, links:[]});
					}
					});
				}
		return a;
	};

	$scope.simpleLeft = $scope.tableToSimpleBound($scope.selectedBusinessModel);
	 $scope.simpleRight = [];


	$scope.alterTableToSimpleBound = function(item){
		$scope.simpleRight = $scope.tableToSimpleBound(item);
	}

	$scope.createInbound = function(){
		$scope.dataSend.sourceColumns = [];
		$scope.dataSend.destinationColumns = [];
		$scope.dataSend.sourceTableName = $scope.rightElement.uniqueName;
		$scope.dataSend.destinationTableName =  $scope.selectedBusinessModel.uniqueName;
		$scope.simpleLeft.forEach(function(entry) {
			if (entry.links.length > 0){
				$scope.dataSend.destinationColumns.push(entry.uname);
				$scope.dataSend.sourceColumns.push(entry.links[0].uname );
			}
		});

		var send = metaModelServices.createRequestRest($scope.dataSend);
		sbiModule_restServices.promisePost("1.0/metaWeb","addBusinessRelation",send)
		.then(function(response){
			metaModelServices.applyPatch(response.data);
		    $mdDialog.hide();
		}
		,function(response){
			sbiModule_restServices.errorHandler(response.data,"");
		});
	}

	$scope.cancel = function(){
		$mdDialog.cancel();
	}

	$scope.checkData = function(){
		var x = 0;
		$scope.simpleLeft.forEach(function(item){
			if (item.links.length > 0)
				x += 1;
		});
		return x > 0 ? false : true ;
		}
}