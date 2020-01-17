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
 * @author Marco Balestri <marco.balestri@eng.it>
 */

angular
	.module('cockpitModule')
	.controller('pythonWidgetEditControllerFunction', pythonWidgetEditControllerFunction)

function pythonWidgetEditControllerFunction(
		$scope,
		finishEdit,
		model,
		sbiModule_translate,
		$mdDialog,
		mdPanelRef,
		cockpitModule_datasetServices,
		cockpitModule_analyticalDrivers,
		cockpitModule_helperDescriptors,
		sbiModule_restServices) {

	$scope.translate = sbiModule_translate;
	$scope.newModel = angular.copy(model);

	sbiModule_restServices.restToRootProject();
	sbiModule_restServices.promiseGet('2.0/configs/category', 'PYTHON_CONFIGURATION')
	.then(function(response){
		$scope.newModel.pythonEnvs = $scope.buildEnvironments(response.data);
		$scope.newModel.pythonEnvsKeys = Object.keys($scope.newModel.pythonEnvs);
	}, function(error){
	});

	$scope.buildEnvironments = function (data) {
		toReturn = {}
		for (i=0; i<data.length; i++) {
			key = data[i].label;
			val = data[i].valueCheck;
			toReturn[key] = val;
		}
		return toReturn;
	}

	$scope.newModel.outputTypes = {
        "Image":"img",
        "HTML":"html",
        "Bokeh application":"bokeh",
	};

	$scope.newModel.outputTypesKeys = Object.keys($scope.newModel.outputTypes);

	$scope.toggleTag = function(tag){
		tag.opened = !tag.opened;
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
		$scope.helper = cockpitModule_helperDescriptors.pythonHelperJSON(newValue,$scope.dataset ? $scope.dataset.metadata.fieldsMeta : null,$scope.formattedAnalyticalDrivers,$scope.aggregations,$scope.newModel.cross,$scope.availableDatasets);

	})

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
		if($scope.newModel.pythonCode) $scope.newModel.pythonCode += tempString;
		else  $scope.newModel.pythonCode = tempString;
	}

	$scope.editorOptionsPython = {
        theme: 'eclipse',
        lineWrapping: true,
        lineNumbers: true,
        mode: {name: "python"},
        onLoad: $scope.codemirrorLoaded
	};

	//codemirror initializer
	$scope.codemirrorLoaded = function (_editor) {
		$scope._doc = _editor.getDoc();
		$scope._editor = _editor;
		_editor.focus();
		$scope._doc.markClean()
		_editor.on("beforeChange", function () {});
		_editor.on("change", function () {});
	};

	$scope.saveConfiguration = function () {
		mdPanelRef.close();
		angular.copy($scope.newModel,model);
		$scope.$destroy();
		finishEdit.resolve();
	};

	$scope.cancelConfiguration = function () {
		mdPanelRef.close();
		$scope.$destroy();
		finishEdit.reject();
	};

}
