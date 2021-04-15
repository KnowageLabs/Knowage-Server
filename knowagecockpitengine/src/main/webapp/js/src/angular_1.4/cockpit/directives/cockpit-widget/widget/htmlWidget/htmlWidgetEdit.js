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
	.controller('htmlWidgetEditControllerFunction',htmlWidgetEditControllerFunction)

function htmlWidgetEditControllerFunction($scope,finishEdit,model,sbiModule_translate,$mdDialog,mdPanelRef,$mdToast,$timeout,cockpitModule_datasetServices,cockpitModule_analyticalDrivers,cockpitModule_analyticalDriversUrls,cockpitModule_helperDescriptors,cockpitModule_properties){
	$scope.translate=sbiModule_translate;
	$scope.newModel = angular.copy(model);
	$scope.helper = {'column' : {},'parameter':{}};
	$scope.formattedAnalyticalDrivers = [];
	$scope.aggregations = [{'name':'SUM'},{'name':'AVG'},{'name':'MIN'},{'name':'MAX'},{'name':'COUNT'},{'name':'COUNT_DISTINCT'}];
	
	function getAnalyticalDrivers(){
		var tempAnalyticalDrivers = [];
		for(var k in cockpitModule_analyticalDriversUrls){
			var url = cockpitModule_analyticalDriversUrls[k].url;
			tempAnalyticalDrivers.push({name:k, value:url});
		}
		return tempAnalyticalDrivers;
	}
	
	$scope.formattedAnalyticalDrivers = getAnalyticalDrivers();

	if($scope.newModel.cssOpened) $scope.newModel.cssOpened = false;
	
	$scope.toggleTag = function(tag){
		tag.opened = !tag.opened;
	}
	
	for(var k in cockpitModule_properties.VARIABLES){
		if($scope.availableVariables) $scope.availableVariables.push({name:k});
		else $scope.availableVariables = [{name:k}];
	}
	
	$scope.$watch('newModel.dataset.dsId',function(newValue,oldValue){
		if(newValue){
			$scope.availableDatasets=cockpitModule_datasetServices.getAvaiableDatasets();
			var dsIndex;
			for(var d in $scope.availableDatasets){
				if($scope.availableDatasets[d].id.dsId == newValue) dsIndex = d;
			}
			if(!newValue || typeof dsIndex != 'undefined'){
				$scope.dataset = $scope.availableDatasets[dsIndex];
				$scope.newModel.content.columnSelectedOfDataset = $scope.dataset.metadata.fieldsMeta;			
			}
		}else{
			if($scope.newModel.content && $scope.newModel.content.columnSelectedOfDataset) $scope.newModel.content.columnSelectedOfDataset = [];
		}
		$scope.helper.tags = cockpitModule_helperDescriptors.htmlHelperJSON(newValue,$scope.dataset ? $scope.dataset.metadata.fieldsMeta : null,$scope.formattedAnalyticalDrivers,$scope.aggregations,$scope.newModel.cross,$scope.availableDatasets, $scope.availableVariables);
		
	})
	
	$scope.showGallery = $scope.newModel.isNew;
	$scope.selectedTab = $scope.newModel.isNew ? 0 : 1;
	
	$scope.setGalleryTemplate = function(template){
		if(template){
			$scope.newModel.htmlToRender = template.code.html;
			$scope.newModel.cssToRender = template.code.css;
		}
		$scope.showGallery = false;
		$scope.selectedTab = 1;
	}
	
	
	$scope.insertCode = function(tag){
		var tempString = tag.tag;
		for(var i in tag.inputs){
			if($scope.helper[tag.name] && (typeof $scope.helper[tag.name][tag.inputs[i].name] != 'undefined')) {
				tempString = tempString.replace('%%'+tag.inputs[i].name+'%%', function(match){
					if(tag.inputs[i].replacer){
						return tag.inputs[i].replacer.replace('***', $scope.helper[tag.name][tag.inputs[i].name]);
					}else return $scope.helper[tag.name][tag.inputs[i].name];
				});
			}else tempString = tempString.replace('%%'+tag.inputs[i].name+'%%','');
		}
		if($scope.newModel.htmlToRender) $scope.newModel.htmlToRender += tempString;
		else  $scope.newModel.htmlToRender = tempString;
	}

	$scope.toggleCss = function() {
		$scope.newModel.cssOpened = !$scope.newModel.cssOpened;
	}

    //codemirror initializer
    $scope.codemirrorLoaded = function(_editor) {
        $scope._doc = _editor.getDoc();
        $scope._editor = _editor;
        _editor.focus();
        $scope._doc.markClean()
        _editor.on("beforeChange", function() {});
        _editor.on("change", function() {});
    };

    //codemirror options
    $scope.editorOptionsCss = {
        theme: 'eclipse',
        lineWrapping: true,
        lineNumbers: true,
        mode: {name:'css'},
        onLoad: $scope.codemirrorLoaded
    };
    $scope.editorOptionsHtml = {
        theme: 'eclipse',
        lineWrapping: true,
        lineNumbers: true,
        mode: {name: "xml", htmlMode: true},
        onLoad: $scope.codemirrorLoaded
    };

	$scope.saveConfiguration=function(){
		 mdPanelRef.close();
		 if($scope.newModel.isNew) $scope.newModel.isNew = false;
		 angular.copy($scope.newModel,model);
		 $scope.$destroy();
		 finishEdit.resolve();
  	}
  	$scope.cancelConfiguration=function(){
  		mdPanelRef.close();
  		$scope.$destroy();
  		finishEdit.reject();
  	}

}
