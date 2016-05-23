angular
	.module('dataset_wizard_meta', [])

	.directive('datasetWizardMeta', function () {		
		 return {			 
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/scripts/directive/dataset-wizard-meta/datasetWizardMeta.html',
		      controller: datasetWizardMetaController
		  };	  
	});

function datasetWizardMetaController($scope){
	
	$scope.metadataTypes=[{name:"Columns",id:"1"},{name:"Dataset",id:"2"}];
	$scope.metadataType=undefined;
    $scope.tableColumns=[
                         {
                          name:"columnView", 
                          label:"Column"
                         
                         },
                         {
                             name:"pnameView",
                             label:"Attribute"
                         },
                         {
                             name:"pvalueView",
                             label:"Value"
                         }
                         ];
    
    $scope.tableDataset=[{
        name:"pname",
        label:"Attribute"
    },
    {
        name:"pvalue",
        label:"Value"
    }];
    
    $scope.table=[];
    
    $scope.metaScopeFunctions={
    	datasetColumns:$scope.datasetColumns,
    	dsMetaProperty:$scope.dsMetaProperty,
    	dsMetaValue   :$scope.dsMetaValue
    };
}