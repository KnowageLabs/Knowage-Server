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
angular
	.module('cockpitModule')
	.controller('discoveryWidgetEditControllerFunction',discoveryWidgetEditControllerFunction)

function discoveryWidgetEditControllerFunction(
		$scope,
		finishEdit,
		model,
		sbiModule_translate,
		cockpitModule_datasetServices,
		cockpitModule_generalServices,
		cockpitModule_generalOptions,
		mdPanelRef,
		$mdDialog
		){
	$scope.translate=sbiModule_translate;
	$scope.cockpitModule_generalOptions = cockpitModule_generalOptions;
	$scope.newModel = angular.copy(model);
	$scope.textAlignments = [{text:'left',align:'flex-start'},{text:'center',align:'center'},{text:'right',align:'flex-end'}];
	$scope.availableAggregations = [
		{name:'COUNT',available:['MEASURE','ATTRIBUTE']},
		{name:'SUM',available:['MEASURE']},
		{name:'AVG',available:['MEASURE']},
		{name:'MIN',available:['MEASURE','ATTRIBUTE']},
		{name:'MAX',available:['MEASURE','ATTRIBUTE']}];
	
	if($scope.newModel.dataset && $scope.newModel.dataset.dsId){
		$scope.local = cockpitModule_datasetServices.getDatasetById($scope.newModel.dataset.dsId);
	}
	
	$scope.colorPickerPropertyTh = {
			format:'rgb', 
			placeholder:sbiModule_translate.load('sbi.cockpit.color.select'), 
			disabled:($scope.newModel.style.th && $scope.newModel.style.th.enabled === false)
	};
	
	$scope.toggleTh = function(){
		$scope.colorPickerPropertyTh.disabled = $scope.newModel.style.th.enabled;
	}
	
  	$scope.getTemplateUrl = function(template){
  		return cockpitModule_generalServices.getTemplateUrl('discoveryWidget',template);
  	}
  	
  	$scope.initProperty = function(property){
  		if(typeof property == 'undefined') return true
  		return property;
  	}
  	
  	$scope.initTh = function(){
  		return typeof($scope.newModel.style.th.enabled) != 'undefined' ? $scope.newModel.style.th.enabled : true;
  	}
  	
  	$scope.initAggregation = function(col){
  		if(typeof col.aggregationSelected == 'undefined') return 'COUNT';
  		return col.aggregationSelected;
  	}
  	
  	$scope.resetAggregations = function(col){
  		if(col.fieldType == 'ATTRIBUTE') col.aggregationSelected = 'COUNT';
  	}
  	
  	$scope.handleEvent = function(event ,dsId){
  		if($scope.newModel.dataset) $scope.newModel.dataset.dsId = dsId;
  		else $scope.newModel.dataset = {"dsId":dsId};
  		$scope.local = cockpitModule_datasetServices.getDatasetById($scope.newModel.dataset.dsId);
  		$scope.newModel.dataset.label = $scope.local.label;
  		$scope.newModel.content.columnSelectedOfDataset = $scope.local.metadata.fieldsMeta;
  	}
  	
  	$scope.addColumn = function(){
  		$scope.newModel.content.columnSelectedOfDataset.push({});
  	}
  	
  	$scope.deleteColumn = function(col){
  		for(var k in $scope.newModel.content.columnSelectedOfDataset){
  			if($scope.newModel.content.columnSelectedOfDataset[k].$$hashKey == col.$$hashKey) {
  				$scope.newModel.content.columnSelectedOfDataset.splice(k,1);
  				return;
  			}
  		}
  	}
  	
  	$scope.showSettingsDialog = function(ev,col){
	    $mdDialog.show({
	      controller: DialogContent,
	      templateUrl: $scope.getTemplateUrl('discoveryWidgetColumnStyleTemplate'),
	      parent: angular.element(document.body),
	      targetEvent: ev,
	      clickOutsideToClose:true,
	      locals: {column:col}
	    })
        .then(function(column) {
        	for(var k in $scope.newModel.content.columnSelectedOfDataset){
    			if($scope.newModel.content.columnSelectedOfDataset[k].name == column.name) $scope.newModel.content.columnSelectedOfDataset[k] = column;
    		}
        	
        }, function() {
        });
  	}
  	
  	function DialogContent($scope, $mdDialog, column){
  		$scope.translate=sbiModule_translate;
  		$scope.selectedColumn = angular.copy(column);
  		$scope.textAlignments = [{text:'left',align:'flex-start'},{text:'center',align:'center'},{text:'right',align:'flex-end'}];
  		$scope.colorPickerProperty = {format:'rgb', placeholder:sbiModule_translate.load('sbi.cockpit.color.select')};
  		
  		$scope.cancel = function(){
  			$mdDialog.cancel();
  		}
  		
  		$scope.save = function(){
  			$mdDialog.hide($scope.selectedColumn);
  		}
  	}
  	
  	//MAIN DIALOG BUTTONS
	$scope.saveConfiguration=function(){
		mdPanelRef.close();
		angular.copy($scope.newModel,model);
		finishEdit.resolve();
		$scope.$destroy();
  	}

	$scope.cancelConfiguration=function(){
  		mdPanelRef.close();
  		finishEdit.reject();
  		$scope.$destroy();
  	}
}