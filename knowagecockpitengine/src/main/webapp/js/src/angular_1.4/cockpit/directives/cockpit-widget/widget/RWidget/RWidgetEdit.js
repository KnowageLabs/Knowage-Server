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
	.controller('RWidgetEditControllerFunction', RWidgetEditControllerFunction)

function RWidgetEditControllerFunction(
		$scope,
		$http,
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
	$scope.formattedAnalyticalDrivers = [];

	$scope.setLibraries = function () {
		sbiModule_restServices.restToRootProject();
		sbiModule_restServices.promiseGet('2.0/backendservices/widgets/RWidget/libraries', $scope.newModel.RAddress)
		.then(function(response){
			$scope.newModel.libraries = [];
			var librariesArray = JSON.parse((response.data.result));
			for (idx in librariesArray) {
				lib = librariesArray[idx];
				name = lib[0];
				version = lib[1];
				$scope.newModel.libraries.push({"name": name, "version": version})
			}
		}, function(error){
		});
  	}

	sbiModule_restServices.restToRootProject();
	sbiModule_restServices.promiseGet('2.0/configs/category', 'R_CONFIGURATION')
	.then(function(response){
		$scope.newModel.REnvs = $scope.buildEnvironments(response.data);
		$scope.newModel.REnvsKeys = Object.keys($scope.newModel.REnvs);
	}, function(error){
	});

	for(var a in cockpitModule_analyticalDrivers){
		$scope.formattedAnalyticalDrivers.push({'name':a});
	}

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

		var ds_label;
		if (newValue) {
			ds_label = cockpitModule_datasetServices.getDatasetById(newValue).label;
		}
		$scope.helper = cockpitModule_helperDescriptors.rHelperJSON(newValue, ds_label, $scope.dataset ? $scope.dataset.metadata.fieldsMeta : null, $scope.formattedAnalyticalDrivers, $scope.aggregations, $scope.newModel.cross, $scope.availableDatasets);

	})

	$scope.editorOptionsR = {
        theme: 'eclipse',
        lineWrapping: true,
        lineNumbers: true,
        mode: {name: "r"},
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
