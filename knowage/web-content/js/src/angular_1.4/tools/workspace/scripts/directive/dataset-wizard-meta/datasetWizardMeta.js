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
                          label:"Column",
                          name:"column"
                         },
                         {
                             name:"pname",
                             label:"Attribute"
                         },
                         {
                             name:"pvalue",
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
    
}