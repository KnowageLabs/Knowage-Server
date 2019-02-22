angular.module('metaManager').controller('businessModelOutboundController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout','$mdDialog','sbiModule_config','metaModelServices',businessModelOutboundControllerFunction ]);
function businessModelOutboundControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout,$mdDialog,sbiModule_config,metaModelServices){
	$scope.isOutbound = function(item) {
		return angular.equals(item.sourceTableName,$scope.selectedBusinessModel.uniqueName);
	}

	$scope.outboundColumns = [{label:sbiModule_translate.load("sbi.generic.name"),name:'name'},
		                      {label:sbiModule_translate.load("sbi.meta.source.table"),name:'sourceTableName'},
		                      {label:sbiModule_translate.load("sbi.meta.source.columns"),name:'sourceColumns',transformer:function(data){
		                    	  var ret = [];
		                    	  data.forEach(function(entry) {
		                    		    ret.push(entry.name);
		                    		  }, this);
		                    	  return ret.join(", ")
		                      }},
		                      {label:sbiModule_translate.load("sbi.meta.target.table"),name:'destinationTableName'},
		                      {label:sbiModule_translate.load("sbi.meta.target.columns"),name:'destinationColumns',transformer:function(data){
		                    	  var retD = [];
		                    	  data.forEach(function(entry) {
		                    		    retD.push(entry.name);
		                    		  }, this);
		                    	  return retD.join(", ")
		                      }}
		                      ];

	$scope.addNewOutbound = function(){
		$mdDialog.show({
			controller: outboundModelPageControllerFunction,
			preserveScope: true,
			locals: {businessModel:$scope.meta.businessModels,businessViews:$scope.meta.businessViews, selectedBusinessModel:$scope.selectedBusinessModel, sbiModule_restServices:sbiModule_restServices,metaModelServices:metaModelServices},
			templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/outboundModel.jsp',
			clickOutsideToClose:true,
			escapeToClose :true,
			fullscreen: true
		});
}

	$scope.outboundFunctions = {
			translate:sbiModule_translate,
			addNewOutbound:$scope.addNewOutbound
	};

	$scope.outboundActionButton=[
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


function outboundModelPageControllerFunction($scope,$mdDialog, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout, businessModel, selectedBusinessModel,metaModelServices,businessViews){
	$scope.translate = sbiModule_translate;
	$scope.cardinality = [{name:'1..N to 1',value:'many-to-one'}, 				// many-to-one
        				  {name:'0..N to 1',value:'optional-many-to-one'} 		// many-to-optional-one
	                     ];
	$scope.businessName;
	$scope.businessModel = angular.copy(businessModel);
	$scope.businessViews = angular.copy(businessViews);
	$scope.selectedBusinessModel = selectedBusinessModel;
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

	$scope.createOutbound = function(){
		$scope.dataSend.sourceColumns = [];
		$scope.dataSend.destinationColumns = [];
		$scope.dataSend.sourceTableName = $scope.selectedBusinessModel.uniqueName;
		$scope.dataSend.destinationTableName =  $scope.rightElement.uniqueName;
		$scope.simpleRight.forEach(function(entry) {
			if (entry.links.length > 0){
				$scope.dataSend.sourceColumns.push(entry.links[0].uname);
				$scope.dataSend.destinationColumns.push(entry.uname);
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
		})
	}

	$scope.cancel = function(){
		$mdDialog.cancel();
	}

	$scope.checkData = function(){
		var x = 0;
		$scope.simpleRight.forEach(function(item){
			if (item.links.length > 0)
				x += 1;
		});
		return x > 0 ? false : true ;
	}

}

