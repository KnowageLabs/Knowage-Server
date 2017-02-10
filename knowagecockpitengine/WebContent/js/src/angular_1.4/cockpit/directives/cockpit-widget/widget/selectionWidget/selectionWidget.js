/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
 * @authors Alessandro Piovani (alessandro.piovani@eng.it)
 * v0.0.1
 * 
 */
(function() {
angular.module('cockpitModule')
.directive('cockpitSelectionWidget',function(cockpitModule_widgetServices,$mdDialog){
	   return{
		   templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/selectionWidget/templates/selectionWidgetTemplate.html',
		   controller: cockpitSelectionWidgetControllerFunction,
		   compile: function (tElement, tAttrs, transclude) {
                return {
                    pre: function preLink(scope, element, attrs, ctrl, transclud) {
                    	element[0].classList.add("flex");
                    	element[0].classList.add("layout");
                    	element[0].style.overflow="auto"
                    },
                    post: function postLink(scope, element, attrs, ctrl, transclud) {
                    	//init the widget
                    	element.ready(function () {
                    		scope.initWidget();
                        });
                    	
                    	
                    	
                    }
                };
		   	}
	   };
});

function cockpitSelectionWidgetControllerFunction($scope,cockpitModule_widgetConfigurator,$mdPanel,cockpitModule_template,cockpitModule_datasetServices,$mdDialog,sbiModule_translate,$q,sbiModule_messaging,cockpitModule_documentServices,cockpitModule_widgetSelection,cockpitModule_properties){
	$scope.property={
			style:{}
		};
	
	$scope.selection = [];
	$scope.translate = sbiModule_translate;
	$scope.tmpSelection = [];
	angular.copy(cockpitModule_template.configuration.aggregations,$scope.tmpSelection);
	$scope.tmpFilters = {};
	angular.copy(cockpitModule_template.configuration.filters,$scope.tmpFilters);
	
	$scope.init=function(element,width,height){
		$scope.refreshWidget();
	};
	
	$scope.refresh=function(element,width,height){

	};
	
	$scope.filterForInitialSelection=function(obj){
		if(!cockpitModule_properties.EDIT_MODE){
			for(var i=0;i<cockpitModule_properties.STARTING_SELECTIONS.length;i++){
				if(angular.equals(cockpitModule_properties.STARTING_SELECTIONS[i],obj)){
					return true;
				}
			}
		}
		return false;
	}
	$scope.filterForInitialFilter=function(obj){
		if(!cockpitModule_properties.EDIT_MODE){
			for(var i=0;i<cockpitModule_properties.STARTING_FILTERS.length;i++){
				if(angular.equals(cockpitModule_properties.STARTING_FILTERS[i],obj)){
					return true;
				}
			}
		}
		return false;
	}
	
	
	$scope.getSelections=function()
	{
		
		$scope.selection = [];
		$scope.translate = sbiModule_translate;
		$scope.tmpSelection = [];
		angular.copy(cockpitModule_template.configuration.aggregations,$scope.tmpSelection);
		$scope.tmpFilters = {};
		angular.copy(cockpitModule_template.configuration.filters,$scope.tmpFilters);
		
		if($scope.tmpSelection.length >0){
			for(var i=0;i<$scope.tmpSelection.length;i++){
				var selection = $scope.tmpSelection[i].selection;
				for(var key in selection){
					var string = key.split(".");
					var obj = {
							ds : string[0],
							columnName : string[1],
							value : selection[key],
							aggregated:true
					};
					if(!$scope.filterForInitialSelection(obj)){
						$scope.selection.push(obj);
					}
				}
			}
		}
		

		for(var ds in $scope.tmpFilters){
			for(var col in $scope.tmpFilters[ds]){
				var tmpObj={
						ds :ds,
						columnName : col,
						value : $scope.tmpFilters[ds][col],
						aggregated:false
				}
				 
				if(!$scope.filterForInitialFilter(tmpObj)){
					$scope.selection.push(tmpObj);
				}
			}
		}
		
		
	}
	
	$scope.getSelections();
	
	$scope.columnTableSelection =[
	  {
		  label:"Dataset",
		  name:"ds",
		  hideTooltip:true
	  },
	  {
		  label:"Column Name",
		  name:"columnName",
		  hideTooltip:true
	  },
	  ,
	  {
		  label:"Values",
		  name:"value",
		  hideTooltip:true
	  }
    ];


	$scope.actionsOfSelectionColumns = [

	    {
	    	icon:'fa fa-trash' ,   
	    	action : function(item,event) {	
	    		$scope.deleteSelection(item);
	    		
	    	}
	    } 
    ];
	
	$scope.deleteSelection=function(item){
		if(item.aggregated){
			var key = item.ds + "." + item.columnName;
			
			for(var i=0;i<$scope.tmpSelection.length;i++){
				if($scope.tmpSelection[i].datasets.indexOf(item.ds) !=-1){
					var selection  = $scope.tmpSelection[i].selection;
					delete selection[key];
				}
			}
			
			var index=$scope.selection.indexOf(item);
			$scope.selection.splice(index,1);
		}else{
			delete $scope.tmpFilters[item.ds][item.columnName];
			if(Object.keys($scope.tmpFilters[item.ds]).length==0){
				delete $scope.tmpFilters[item.ds];
			}
			var index=$scope.selection.indexOf(item);
			$scope.selection.splice(index,1);
		}

	}

	$scope.clearAllSelection = function(){
		while($scope.selection.length!=0){
			$scope.deleteSelection($scope.selection[0]);
		}
	}
	
	$scope.cancelConfiguration=function(){
		$mdDialog.cancel();
	}
	
	$scope.saveConfiguration = function(){
		var reloadAss=false;
		var reloadFilt=[];
		  if(!angular.equals($scope.tmpSelection,cockpitModule_template.configuration.aggregations )){
	  		  angular.copy($scope.tmpSelection,cockpitModule_template.configuration.aggregations);
	  		 reloadAss=true;
		  }
		  if(!angular.equals($scope.tmpFilters,cockpitModule_template.configuration.filters )){
			  angular.forEach(cockpitModule_template.configuration.filters,function(val,dsLabel){
				  if($scope.tmpFilters[dsLabel]==undefined || !angular.equals($scope.tmpFilters[dsLabel],val)){
					  reloadFilt.push(dsLabel)
				  }
			  })
			  angular.copy($scope.tmpFilters,cockpitModule_template.configuration.filters);
		  }
		  if(reloadAss){
			  cockpitModule_widgetSelection.getAssociations(true);
		  }
		  if(!reloadAss && reloadFilt.length!=0){
			  cockpitModule_widgetSelection.refreshAllWidgetWhithSameDataset(reloadFilt);
		  }
		  
		  var hs=false;
		  for(var i=0;i<$scope.tmpSelection.length;i++){
				if(Object.keys($scope.tmpSelection[i].selection).length>0){
					hs= true;
					break;
				}
			}
		  
		  if(hs==false && Object.keys($scope.tmpFilters).length==0 ){
			  cockpitModule_properties.HAVE_SELECTIONS_OR_FILTERS=false;
		  }
		  
		$mdDialog.cancel();
	}
	
	
	// general widget event  'WIDGET_EVENT' without ID
	$scope.$on('WIDGET_EVENT',function(config,eventType,config){
		
		switch(eventType){
		case "UPDATE_FROM_DATASET_FILTER"  : 
			
			$scope.getSelections();
			
			
			break;
		default: 
		}
	});
	
		$scope.editWidget=function(index){
			var finishEdit=$q.defer();
			var config = {
					attachTo:  angular.element(document.body),
					locals: {finishEdit:finishEdit,model:$scope.ngModel},
					controller: function($scope,finishEdit,sbiModule_translate,model,mdPanelRef,$mdToast){
				    	  $scope.localModel = {};
				    	  angular.copy(model,$scope.localModel);
				    	  $scope.translate=sbiModule_translate;

				    	  $scope.saveConfiguration=function(){
				    		  angular.copy($scope.localModel,model);
				    		  mdPanelRef.close();
				    		  $scope.$destroy();
				    		  finishEdit.resolve();

				    	  }
				    	  
				    	  $scope.cancelConfiguration=function(){
				    		  mdPanelRef.close();
				    		  $scope.$destroy();
				    		  finishEdit.reject();

				    	  }
				    	  
				      },
					disableParentScroll: true,
					templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/selectionWidget/templates/selectionWidgetEditPropertyTemplate.html',
					position: $mdPanel.newPanelPosition().absolute().center(),
					fullscreen :true,
					hasBackdrop: true,
					clickOutsideToClose: false,
					escapeToClose: false,
					focusOnOpen: true,
					preserveScope: true,
					
				
			};

			$mdPanel.open(config);
			return finishEdit.promise;
			
		
		}

	
};


//this function register the widget in the cockpitModule_widgetConfigurator factory
addWidgetFunctionality("selection",{'initialDimension':{'width':20, 'height':20},'updateble':true,'cliccable':true});

})();