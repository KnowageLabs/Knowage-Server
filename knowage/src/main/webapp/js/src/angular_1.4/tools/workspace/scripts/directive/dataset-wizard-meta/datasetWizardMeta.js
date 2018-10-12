/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

(function() {

	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);
	
angular
	.module('dataset_wizard_meta', [])

	.directive('datasetWizardMeta', function () {		
		 return {			 
		      restrict: 'E',
		      replace: 'true',
//		      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/scripts/directive/dataset-wizard-meta/datasetWizardMeta.html',
		      templateUrl: currentScriptPath + 'datasetWizardMeta.html',
		      controller: datasetWizardMetaController
		  };	  
	});

function datasetWizardMetaController($scope,$mdDialog,sbiModule_translate){
	
	$scope.translate = sbiModule_translate;	
	
	/**
	 * WORKAROUND: Re-initialize the collection of metadata types, since for some reason this collection is changed after moving back/forward from the Step 2. 
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)  
	 */
	$scope.metadataTypes = 
	[
	 	{name:"Columns",value:1},
	 	{name:"Dataset",value:2}
 	];
	
	$scope.markSelectedOptMetadataType = function(md) {
		
		for (var i = 0; i < $scope.metadataTypes.length; i++) {			
			if ($scope.metadataTypes[i].value == md) {
				$scope.metadataType=$scope.metadataTypes[i];
			}			
		}
		console.log($scope.metadataType);
	}
	
    $scope.tableColumns = 
    [
	     {
	      name:"columnView", 
	      label:"Column",
	      hideTooltip:true
	     },
	     
	     {
	         name:"pnameView",
	         label:"Attribute",
	         hideTooltip:true
	     },
	     
	     {
	         name:"pvalueView",
	         label:"Value",
	         hideTooltip:true
	     },
	     
	     /**
	      * A new column on the Step 2 of the Dataset wizard. It contains a graphic description of a validation state 
	      * for all metadata column separately.
	      * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	      */
	     {
	    	 name: "metaValid",
	    	 label: "Valid",
	    	 hideTooltip: true
	     }
     ];
    
    $scope.tableDataset =
	[
	 	{
	        name:"pnameView",
	        label:"Attribute",
	        hideTooltip:true
	    },
	    
	    {
	        name:"pvalueView",
	        label:"Value",
	        hideTooltip:true
	    }
    ];
    
    $scope.table=[];
    
    $scope.metaScopeFunctions={    		    		
    	translate: sbiModule_translate,	
    	datasetColumns:$scope.datasetColumns,
    	dsMetaProperty:$scope.dsMetaProperty,
    	dsMetaValue   :$scope.dsMetaValue,
    	filterMetaValues: function(value,row){
    		console.log(row);
    		row.dsMetaValue=[];
    		if(value.toLowerCase()==="type".toLowerCase()){
    			for(i=0;i<this.dsMetaValue.length;i++){
    			 if(this.dsMetaValue[i].VALUE_CD.toLowerCase()==="string".toLowerCase() ||
    			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="double".toLowerCase() ||
    			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="integer".toLowerCase() ||
    			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="date".toLowerCase() || 
    			    this.dsMetaValue[i].VALUE_CD.toLowerCase()==="timestamp".toLowerCase())
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
    
    /**
     * A click-listener function that will take care when user clicks on the Value combo that is aligned with the particular 
     * Field type row, in order to indicate that we are changing this value. This information will be useful when user picks
     * e.g. a MEASURE, in which case the previous row of type Type (it belonging column, e.g. city) will remove its "String"
     * item, since the MEASURE cannot be if type String.
     * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
    $scope.metaScopeFunctions.valueChanged = function(item,index) {
    	    	
    	if (item.VALUE_CD=="MEASURE" || item.VALUE_CD=="ATTRIBUTE") {
    		$scope.prepareMetaForView(item.VALUE_CD,index);
    	}
    	
    }
    
    $scope.deleteMetaColumn=function(item){
    	var index=$scope.dataset.meta.columns.indexOf(item);
    	if(index>-1){
    		$scope.dataset.meta.columns.splice(index,1);
    	}
    }
    
    $scope.metaScopeFunctions.addNewMetaRow = function() {
    	var newRow = {
    			column:"",
    			pname:"",
    			pvalue:"",
    			dsMetaValue: [],
    			columnView:'<md-select ng-model=row.column class="noMargin"><md-option ng-repeat="col in scopeFunctions.datasetColumns" value="{{col.columnName}}">{{col.columnName}}</md-option></md-select>',
    			pnameView:'<md-select ng-model=row.pname class="noMargin"><md-option ng-repeat="col in scopeFunctions.dsMetaProperty" value="{{col.VALUE_CD}}" ng-click="scopeFunctions.filterMetaValues(col.VALUE_CD,row)">{{col.VALUE_NM}}</md-option></md-select>',
    			pvalueView:'<md-select ng-model=row.pvalue class="noMargin"><md-option ng-repeat="col in row.dsMetaValue" value="{{col.VALUE_CD}}">{{col.VALUE_NM}}</md-option></md-select>'
    	}
    	angular.copy($scope.dsMetaValue,newRow.dsMetaValue);
    	$scope.dataset.meta.columns.push(newRow);

    }
    
    $scope.metaScopeFunctions.clearAllMeta = function() {
    	$scope.dataset.meta.columns = [];
    }
    
    $scope.metaScopeFunctions.dsGenMetaProperty = $scope.dsGenMetaProperty;
    
    $scope.metaScopeFunctions.addNewDatasetRow = function() {
    	var newRow = {
    			pname:"",
    			pvalue:"",
    			pnameView:'<md-select ng-model=row.pname class="noMargin"><md-option ng-repeat="col in scopeFunctions.dsGenMetaProperty" value="{{col.VALUE_CD}}">{{col.VALUE_NM}}</md-option></md-select>',
    			pvalueView:'<div><md-input-container"><input type="text" ng-model="row.pvalue">	</md-input-container></div>'
    	}
    	$scope.dataset.meta.dataset.push(newRow);
    }
    $scope.metaScopeFunctions.clearAllDatasets = function() {
    	$scope.dataset.meta.dataset = [];
    }
    
    $scope.deleteMeta=[{
    	label:'delete',
    	icon: 'fa fa-trash',
    	action:function(item){
    		 $scope.deleteMetaColumn(item);
    		
    	}
    }
                         ];
}
})();