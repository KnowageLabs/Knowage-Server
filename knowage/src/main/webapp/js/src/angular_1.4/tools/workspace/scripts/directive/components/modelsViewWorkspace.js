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
	.module('models_view_workspace', [])

	/**
	 * The HTML content of the Recent view (recent documents).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	.directive('modelsViewWorkspace', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
//		      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/modelsViewWorkspace.html',
		      templateUrl: currentScriptPath + '../../../templates/modelsViewWorkspace.html',
		      controller: modelsController
		  };
	});

function modelsController($scope, sbiModule_restServices, sbiModule_translate, $mdDialog, sbiModule_config, $window,
			$mdSidenav, $qbeViewer, sbiModule_user, toastr){

	$scope.businessModelsInitial=[];
	$scope.federationDefinitionsInitial=[];
	$scope.rolesIds = [];
	$scope.categoriesForUser = [];
	$scope.selectedModel = undefined;

	/**
	 * The Business Model interface is improved: when models are set to be viewed as a list - the 'Label' column is removed (since there
	 * is no 'label' property of this object) and the 'Description' column is provided instead. Columns for Federation models remain the
	 * same. Their columns are now just separated (independent).
	 * @author Ana Tomic (atomic, ana.tomic@mht.net)
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.tableColumnsFederation = [{"label":"Label","name":"label"},{"label":"Name","name":"name"}];
	$scope.tableColumnsModels = [{"label":"Name","name":"name"}, {"label":"Description","name":"description"}];

	$scope.showModelInfo = false;
	$scope.idsOfFederationDefinitionsUsediNFederatedDatasets = [];
	$scope.allFederatedDatasets = [];

	$scope.federationsEnabled= function (){
		return datasetParameters.CAN_USE_FEDERATED_DATASET_AS_FINAL_USER === "true";
	}

	$scope.getFederatedDatasets = function() {
		sbiModule_restServices.promiseGet("1.0/datasets", "federated")
		.then(function(response) {
			var allDatasets = response.data.root;
			for (var i = 0; i < allDatasets.length; i++) {
				if(allDatasets[i].hasOwnProperty('federationId')) {
					if($scope.idsOfFederationDefinitionsUsediNFederatedDatasets.indexOf(allDatasets[i].federationId)==-1){
						$scope.idsOfFederationDefinitionsUsediNFederatedDatasets.push(allDatasets[i].federationId);
					}
					$scope.allFederatedDatasets.push(allDatasets[i]);
				}
			}
		},function(response){

		});
	}


	$scope.getFederatedDatasets();


	$scope.loadFederations = function(){
		if(datasetParameters.CAN_USE_FEDERATED_DATASET_AS_FINAL_USER==="true"){
			sbiModule_restServices.promiseGet("federateddataset", "")
			.then(function(response) {
				angular.copy(response.data,$scope.federationDefinitions);
				angular.copy($scope.federationDefinitions,$scope.federationDefinitionsInitial);
				console.info("[LOAD END]: Loading of Federation definitions is finished.");
			},function(response){

				// Take the toaster duration set inside the main controller of the Workspace. (danristo)
				toastr.error(response.data, sbiModule_translate.load('sbi.workspace.federations.load.error'), $scope.toasterConfig);

			});
		}
	}

	//TODO move business models to separate controller
    $scope.handleBusinessModels= function(categoriesForUser){
    	sbiModule_restServices.promiseGet("2.0/businessmodels", "", "fileExtension=jar")
		.then(function(response) {
			angular.copy(response.data,$scope.businessModels);
			// S.Lupo - businessModels must be filtered by categories backend side
			/*
			for (var i = 0; i < response.data.length; i++) {
				for (var j = 0; j < categoriesForUser.length; j++) {
					if(categoriesForUser[j].valueId == response.data[i].category) {
						$scope.businessModels.push(response.data[i]);
					}
				}
			}*/
			angular.copy($scope.businessModels,$scope.businessModelsInitial);
			console.info("[LOAD END]: Loading of Business models is finished.");
		},function(response){

			// Take the toaster duration set inside the main controller of the Workspace. (danristo)
			toastr.error(response.data, sbiModule_translate.load('sbi.workspace.categories.error'), $scope.toasterConfig);

		});
	}

    $scope.loadBusinessModelsCategories= function(roleIds){

    	sbiModule_restServices.promiseGet("2.0/domains", "rolesCategories", queryParamRolesIds(roleIds))
		.then(function(response) {
			$scope.handleBusinessModels(response.data);
		},function(response){

			// Take the toaster duration set inside the main controller of the Workspace. (danristo)
			toastr.error(response.data, sbiModule_translate.load('sbi.workspace.bmmodels.load.error'), $scope.toasterConfig);

		});

	}

    queryParamRolesIds = function(roleIds){
		   var q="?";
		   for(var i=0; i<roleIds.length;i++){
			   q+="id="+roleIds[i]+"&";
		   }
		   return q;
	};

	 queryParamRolesNames = function(){
		   var q="?";
		   for(var i=0; i<sbiModule_user.roles.length;i++){
			   q+="name="+sbiModule_user.roles[i]+"&";
		   }
		   return q;
	};

    $scope.loadBusinessModels= function(){
    	sbiModule_restServices.promiseGet("2.0/roles", "idsByNames", queryParamRolesNames())
		.then(function(response) {
			$scope.loadBusinessModelsCategories(response.data);
		},function(response){

			// Take the toaster duration set inside the main controller of the Workspace. (danristo)
			toastr.error(response.data, sbiModule_translate.load('sbi.workspace.roles.error'), $scope.toasterConfig);

		});
	}

	$scope.showModelDetails = function() {
		return $scope.showModelInfo && $scope.isSelectedModelValid();
	};


	$scope.isSelectedModelValid = function() {
		return $scope.selectedModel !== undefined;
	};

	$scope.setDetailOpenModel = function(isOpen) {
		if (isOpen && !$mdSidenav('rightModel').isLockedOpen() && !$mdSidenav('rightModel').isOpen()) {
			$scope.toggleModelDetail();
		}

		$scope.showModelInfo = isOpen;
	};

	$scope.toggleModelDetail = function() {
		$mdSidenav('rightModel').toggle();
	};

	$scope.selectModel= function ( model ) {
		if (model !== undefined) {
			//$scope.lastDatasetSelected = dataset;
		}
		var alreadySelected = (model !== undefined && $scope.selectedModel === model);
		$scope.selectedModel = model;
		if (alreadySelected) {
			$scope.selectedModel=undefined;
			$scope.setDetailOpenModel(!$scope.showModelDetail);
		} else {
			$scope.setDetailOpenModel(model !== undefined);
		}
	};

	$scope.showQbeModel = function(model){

		if($scope.currentModelsTab=='federations'){
			$scope.showQbeFederation(model);
		}else if($scope.currentModelsTab=='businessModels'){
			$scope.showQbeFromBM(model);
		}
	}

	$scope.showQbeFederation = function(federation){

		var federationId = federation.federation_id;

		var url = datasetParameters.qbeEditFederationServiceUrl
			+ '&FEDERATION_ID=' + federationId
			+ (isTechnicalUser != undefined ? '&isTechnicalUser=' + isTechnicalUser : '');

		$qbeViewer.openQbeInterfaceFromModel($scope,url);

//		 $window.location.href=url;

	}

	$scope.editFederation=function(federation){
//		console.log(federation);
		var id = federation.federation_id;
		var label = federation.label;
		//$window.location.href=sbiModule_config.contextName+"/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp&id="+id+"&label="+label;

    	 $mdDialog.show({
    		  scope:$scope,
			  preserveScope: true,
		      controller: DialogEditFederationController,
		      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/documentbrowser/template/documentDialogIframeTemplate.jsp',
		      clickOutsideToClose:true,
		      escapeToClose :true,
		      fullscreen: true,
		      locals:{federation:federation }
		    })

	}

	$scope.deleteFederation=function(federation){
		var usedInDatasets = [];
		var fds = $scope.allFederatedDatasets;
		if ($scope.idsOfFederationDefinitionsUsediNFederatedDatasets.indexOf(federation.federation_id)>-1) {
			for (var i = 0; i < $scope.allFederatedDatasets.length; i++) {
				if($scope.allFederatedDatasets[i].federationId==federation.federation_id){
					usedInDatasets.push($scope.allFederatedDatasets[i].label)
				}
			}

			// Take the toaster duration set inside the main controller of the Workspace. (danristo)
			toastr.error(sbiModule_translate.load("sbi.federationdefinition.models.delete")+"["+usedInDatasets+"]",
					sbiModule_translate.load("sbi.generic.error"), $scope.toasterConfig);

		} else {
			var confirm = $mdDialog.confirm()
			.title(sbiModule_translate.load("sbi.workspace.delete.confirm.title"))
			.content(sbiModule_translate.load("sbi.federationdefinition.confirm.delete"))
			.ariaLabel('delete Document')
			.ok(sbiModule_translate.load("sbi.general.yes"))
			.cancel(sbiModule_translate.load("sbi.general.No"));
				$mdDialog.show(confirm).then(function() {

				sbiModule_restServices.promiseDelete("2.0/federateddataset",federation.federation_id)
				.then(function(response) {

					$scope.loadFederations();
					$scope.selectModel(undefined);

					/**
					 * If some federation is removed from the filtered set of datasets, clear the search input, since all federations are refreshed.
					 *  @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					$scope.searchInput = "";

					// Take the toaster duration set inside the main controller of the Workspace. (danristo)
					toastr.success(sbiModule_translate.load("sbi.federationdefinition.models.delete.success.msg"),
							sbiModule_translate.load("sbi.generic.success"), $scope.toasterConfig);

				},function(response) {

					// Take the toaster duration set inside the main controller of the Workspace. (danristo)
					toastr.error(response.data, sbiModule_translate.load('sbi.browser.document.delete.error'), $scope.toasterConfig);

				});
			});
		}
	}

	$scope.createFederation=function(){

		$mdDialog.show({
			  scope:$scope,
			  preserveScope: true,
		      controller: DialogEditFederationController,
		      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/documentbrowser/template/documentDialogIframeTemplate.jsp',
		      clickOutsideToClose:true,
		      escapeToClose :true,
		      fullscreen: true,
		      locals:{federation:undefined}
		    })
	}

	$scope.showQbeFromBM=function(businessModel){

		var modelName= businessModel.name;
		var dataSource=businessModel.dataSourceLabel;
		var url = datasetParameters.qbeFromBMServiceUrl
		        +'&MODEL_NAME='+modelName
		        +'&DATA_SOURCE_LABEL='+ dataSource
		        + (isTechnicalUser != undefined ? '&isTechnicalUser=' + isTechnicalUser : '');

		// $window.location.href=url;
		$qbeViewer.openQbeInterfaceFromModel($scope,url);
	}

	function DialogEditFederationController($scope,$mdDialog,sbiModule_config,federation){

		/**
		 * The function that will be called when a set time is counted and the iframe of the Federation is closed automatically.
		 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		$scope.closeFederationDialog = function(){

			$mdDialog.cancel();
			$scope.loadFederations();

			/**
			  * If the user previously selected some already existing (created) federation definition (model), showing its details inside
			  * the right-side panel and now it creates a new one or updating an existing one, on its successful saving of this one, hide
			  * (close) the right-side panel, since the details are not consistent.
			  * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			  */
			$scope.hideRightSidePanel();

		}

		$scope.cancelDialog = function() {

			$mdDialog.cancel();
			$scope.loadFederations();	// Refresh the federation models list in the workspace after closing the iframe manually. (danristo)

			 /**
			  * If the user previously selected some already existing (created) federation definition (model), showing its details inside
			  * the right-side panel and now it creates a new one or updating an existing one, on its successful saving of this one, hide
			  * (close) the right-side panel, since the details are not consistent. This is needed if the user after a saving manually
			  * closes the iframe of the federation definition interface.
			  * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			  */
			$scope.hideRightSidePanel();

		}

		if(federation!==undefined){
			var id =federation.federation_id;
			var label = federation.label;
			$scope.iframeUrl=sbiModule_config.contextName+"/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp&id="+id+"&label="+label;
		}else{
			$scope.iframeUrl=sbiModule_config.contextName+"/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp";
		}

		}

	/**
	 * Set the currently active Models tab. Initially, the 'Business Models' tab is selected (active).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.currentModelsTab = "businessModels";

	$scope.switchModelsTab = function(modelsTab) {

		$scope.currentModelsTab = modelsTab;

		if($scope.selectedModel !== undefined){
			$scope.selectModel(undefined);
		}

	}


	if(initialOptionMainMenu){
		if(initialOptionMainMenu.toLowerCase() == 'models'){
			var selectedMenu = $scope.getMenuFromName('models');
			$scope.leftMenuItemPicked(selectedMenu,true);
		}
	}


}
})();