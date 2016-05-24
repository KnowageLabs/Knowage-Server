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
	
	$scope.selectedOptionForce = function(m) {
		$scope.metadataType = m;
		console.log($scope.metadataType);
	}
	
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
    	dsMetaValue   :$scope.dsMetaValue,
    	filterMetaValues: function(value,row){
    		console.log(row);
    		row.dsMetaValue=[];
    		if(value.toLowerCase()==="type".toLowerCase()){
    			for(i=0;i<this.dsMetaValue.length;i++){
    			 if(this.dsMetaValue[i].VALUE_CD.toLowerCase()==="string".toLowerCase()||
    			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="double".toLowerCase()||
    			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="integer".toLowerCase())
    				 row.dsMetaValue.push(this.dsMetaValue[i]);
    			
    			}
    			
    		}else if(value.toLowerCase()==="fieldType".toLowerCase()){
    			for(i=0;i<this.dsMetaValue.length;i++){
       			 if(this.dsMetaValue[i].VALUE_CD.toLowerCase()==="attribute".toLowerCase()||
       			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="measure".toLowerCase())
       				 row.dsMetaValue.push(this.dsMetaValue[i]);
       			
       			}
    			
    		}else{
    			
    			angular.copy(this.dsMetaValue,row.dsMetaValue);
    		}
    		
    	}
    };
}