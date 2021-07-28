/**
 *
 */

var app = angular.module('businessModelCatalogueModule',['ngMaterial', 'ngMessages', 'angular_list', 'angular_table','sbiModule', 'DriversModule', 'angular_2_col','file_upload','angular-list-detail']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);
app.controller('businessModelCatalogueController',["sbiModule_translate", "sbiModule_restServices", "DriversService",  "kn_regex",
                                                   "$scope", "$mdDialog", "$mdToast","multipartForm", "sbiModule_download",
                                                   "sbiModule_messaging","sbiModule_config","sbiModule_user","sbiModule_messaging",businessModelCatalogueFunction]);

function businessModelCatalogueFunction(sbiModule_translate, sbiModule_restServices, DriversService, kn_regex, $scope, $mdDialog,
		$mdToast,multipartForm,sbiModule_download,sbiModule_messaging,sbiModule_config,sbiModule_user,sbiModule_messaging){
	var d = this;
	$scope.regex = kn_regex;
	$scope.isDirty = false;
	$scope.isCWMDirty = false;
	$scope.showMe = false;
	$scope.versionLoadingShow;
	$scope.bmImportingShow;
	$scope.bmCWMProcessingShow;
	$scope.bmCWMImportingShow;
	$scope.bmCWMDisableImportButton = true;

	$scope.isNew;
	$scope.metaWebFunctionality=false;

	$scope.translate = sbiModule_translate;
	$scope.businessModelList=[];		//All Business Models list
	$scope.listOfDatasources = [];		//Dropdown
	$scope.listOfCategories=[];			//Dropdown
	$scope.bmVersions=[];				//All versions of BM list
	$scope.selectedBusinessModels=[];	//Selected Business Models table multiselect
	$scope.selectedVersions=[];			//Selected BM Versions table multiselect
	$scope.selectedBusinessModel = {}; //Selected model for editing or new model data
	$scope.savedBusinessModel = {};
	$scope.bmVersionsRadio;
	$scope.bmVersionsActive;
	$scope.fileObj ={};
	$scope.fileObjCWM ={};

	$scope.fileClicked =false;
	$scope.fileCWMClicked =false;

	$scope.togenerate = false;

	$scope.varTablePrefixLikeValue;
	$scope.varTablePrefixNotLikeValue;

	var requiredPath = "2.0/businessmodels";
    var businessModelBasePath =""+ $scope.selectedBusinessModel.id;
    var driversService = DriversService;

	angular.element(document).ready(function () {
        $scope.getData();
    });

	$scope.openMenu = function(menu, e) {
		e.stopPropagation();
		menu(e);
	}

	 $scope.getData = function(){
		 $scope.getBusinessModels();
		 $scope.getDataSources();
		 $scope.getCategories();
	 }

	$scope.createBusinessModel = function(){
		if(angular.equals($scope.savedBusinessModel,$scope.selectedBusinessModel)){
		//no change
			angular.copy({},$scope.selectedBusinessModel);
			angular.copy($scope.selectedBusinessModel,$scope.savedBusinessModel);
			$scope.bmVersions=[];
			$scope.isNew = true;
			$scope.showMe = true;
			$scope.fileClicked = false;
			$scope.fileCWMClicked = false;
			$scope.togenerate = false;
			$scope.isDirty=false;
			$scope.isCWMDirty = false;
			$scope.metaWebFunctionality=false;
			$scope.businessModelForm.$setPristine();
			$scope.businessModelForm.$setUntouched();
			$scope.selectedBusinessModel.smartView = true;
		}else{
			$mdDialog.show($scope.confirm).then(function(){
				angular.copy({},$scope.selectedBusinessModel);
				angular.copy($scope.selectedBusinessModel,$scope.savedBusinessModel);
				$scope.bmVersions=[];
				$scope.isNew = true;
				$scope.showMe = true;
				$scope.fileClicked = false;
				$scope.fileCWMClicked = false;
				$scope.togenerate = false;
				$scope.isDirty=false;
				$scope.isCWMDirty = false;
				$scope.metaWebFunctionality=false;
				$scope.businessModelForm.$setPristine();
				$scope.businessModelForm.$setUntouched();
		    });
		}

	}

	$scope.getAllDatasets = function() {
		sbiModule_restServices.promiseGet("1.0/datasets","pagopt")
		.then(function(response) {
			$scope.datasetsListTemp = angular.copy(response.data.root);
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		});
	}
	$scope.getAllDatasets();

	$scope.cancel = function(){
		$scope.showMe = false;
		$scope.isDirty = false;
		$scope.isCWMDirty = false;
		$scope.selectedBusinessModel = {};
		$scope.bmVersions=[];
		$scope.fileObj = {};
	}

	$scope.businessModelLock = function(){
		if($scope.selectedBusinessModel.modelLocked){
			$scope.selectedBusinessModel.modelLocker = valueUser;
		}
		else{
			$scope.selectedBusinessModel.modelLocker = "";
		}

		$scope.checkChange();
	};

	$scope.lockBusinessModel = function(){
		//should also check if user is allowed to do this
		$scope.selectedBusinessModel.modelLocked = true;
		console.log($scope.selectedBusinessModel);
	}

	$scope.leftTableClick = function(item){

		if(angular.equals($scope.savedBusinessModel,$scope.selectedBusinessModel)){
			//no change
				angular.copy(item,$scope.selectedBusinessModel);
				angular.copy($scope.selectedBusinessModel,$scope.savedBusinessModel);
				$scope.bmVersions=[];
				$scope.getVersions(item.id);
				$scope.isNew = false;
				$scope.showMe = true;
				$scope.fileClicked = false;
				$scope.fileCWMClicked = false;
				$scope.togenerate = false;
				$scope.isDirty=false;
				$scope.isCWMDirty = false;
				$scope.metaWebFunctionality=false;
				$scope.businessModelForm.$setPristine();
				$scope.businessModelForm.$setUntouched();
			    driversService.setDriverRelatedObject($scope.selectedBusinessModel);
			    $scope.varTablePrefixLikeValue=$scope.selectedBusinessModel.tablePrefixLike;
			    $scope.varTablePrefixNotLikeValue=$scope.selectedBusinessModel.tablePrefixNotLike;
			}else{
				$mdDialog.show($scope.confirm).then(function(){
					angular.copy(item,$scope.selectedBusinessModel);
					angular.copy($scope.selectedBusinessModel,$scope.savedBusinessModel);
					$scope.bmVersions=[];
					$scope.getVersions(item.id);
					$scope.isNew = false;
					$scope.showMe = true;
					$scope.fileClicked = false;
					$scope.fileCWMClicked = false;
					$scope.togenerate = false;
					$scope.isDirty=false;
					$scope.isCWMDirty = false;
					$scope.metaWebFunctionality=false;
					$scope.businessModelForm.$setPristine();
					$scope.businessModelForm.$setUntouched();
					$scope.varTablePrefixLikeValue=$scope.selectedBusinessModel.tablePrefixLike;
					$scope.varTablePrefixNotLikeValue=$scope.selectedBusinessModel.tablePrefixNotLike;
			    });
			}

		$scope.driverPostBasePath = $scope.selectedBusinessModel.id + '/drivers';

	    driversService.setDriverRelatedObject($scope.selectedBusinessModel);
		driversService.getDriversOnRelatedObject(requiredPath, $scope.driverPostBasePath);
		$scope.analyticalDrivers = driversService.analyticalDrivers;
		d.selected = $scope.selectedBusinessModel;
		if(driversService.loadingDriversOnBM) {
			driversService.loadingDriversOnBM(d.selected);
		}
	}

	$scope.downloadFile = function(item,ev,filetype){

					var link = "/restful-services/2.0/businessmodels/"+$scope.selectedBusinessModel.id+"/versions/"+item.id+"/"+filetype+"/file";
					sbiModule_download.getLink(link);

	}
	//Export Metamodel as a CWM Metamodel
	/*
	$scope.downloadCWMFile= function(id){
		$scope.bmCWMProcessingShow = true;

		 sbiModule_restServices.promiseGet("2.0/metadata/"+id+"/exportCWM","")
			.then(function(response) {
				sbiModule_download.getBlob(response.data,"exportCWM",'application/xml','xmi');
				$scope.bmCWMProcessingShow = false;

			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				$scope.bmCWMProcessingShow = false;
			});
	}
	*/

	 $scope.bmSpeedMenu= [
		                      {
		                    	  label:sbiModule_translate.load("sbi.generic.delete"),
		                    	  icon:'fa fa-trash',
		                    	  action:function(item,event){
		                    		  $scope.deleteItem(item,event);
		                    	  }
		                      	}
		                     ];

	 //functions that use services

	 //calling service for getting Business Models @GET
	 $scope.getBusinessModels = function(){

		 sbiModule_restServices.promiseGet("2.0", 'businessmodels')
			.then(function(response) {
				angular.copy(response.data,$scope.businessModelList)
			}, function(response) {
				sbiModule_restServices.errorHandler(response.data, 'Error');
			});
	 }

	 //calling service for getting data sources @GET
	 $scope.getDataSources = function(){

		 sbiModule_restServices.promiseGet("2.0/datasources","", "type=meta")
			.then(function(response) {
				$scope.listOfDatasources=[];
				$scope.listOfDatasources = response.data;
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

			});
	 }

	 //Calling service for getting Categories  @GET
	 $scope.getCategories = function(){

		 sbiModule_restServices.promiseGet("domains","listValueDescriptionByType","DOMAIN_TYPE=BM_CATEGORY")
			.then(function(response) {
				$scope.listOfCategories=[];
				$scope.listOfCategories = response.data;
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

			});
	 }

	 //Calling service for file versions @GET
	 $scope.getVersions = function (id){
		 sbiModule_restServices.promiseGet("2.0/businessmodels/"+id+"/versions","")
			.then(function(response) {
				$scope.versionLoadingShow = true;
				$scope.bmVersions = [];
				setTimeout(function(){
					$scope.togenerate=response.data.togenerate

					$scope.bmVersions = response.data.versions;
  					activeFlagStyle();
  					millisToDate($scope.bmVersions);
  					$scope.versionLoadingShow = false;
  					$scope.$apply();
				 },600);
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			});
	 }

	 // TODO fix this
	 $scope.saveBusinessModelFile = function(){
		multipartForm.post("2.0/businessmodels/"+$scope.selectedBusinessModel.id+"/versions",$scope.fileObj).success(
			function(data,status,headers,config){
				if(data.hasOwnProperty("errors")){
					console.log("[UPLOAD]: DATA HAS ERRORS PROPERTY!");
				}else{
					console.log("[UPLOAD]: SUCCESS!");
					$scope.bmVersions = $scope.getVersions($scope.selectedBusinessModel.id);
					console.log($scope.bmVersions);
					document.getElementById("businessModelFile").value = "";
					$scope.isDirty = false;
					$scope.fileObj.fileName = "";
					$scope.fileObj = {};
				}
			}).error(function(data, status, headers, config) {
				console.log("[UPLOAD]: FAIL!"+status);
			});
	 }

	 //calling service for saving BM @POST and @PUT
	 $scope.saveBusinessModel = function(){
		 	if($scope.selectedBusinessModel.modelLocked === undefined)
		 		$scope.selectedBusinessModel.modelLocked = false;
			if(typeof $scope.selectedBusinessModel.id === "undefined"){
				$scope.varTablePrefixLikeValue=$scope.selectedBusinessModel.tablePrefixLike;
			    $scope.varTablePrefixNotLikeValue=$scope.selectedBusinessModel.tablePrefixNotLike;

				sbiModule_restServices.promisePost("2.0/businessmodels","",$scope.selectedBusinessModel)
				.then(function(response) {
					$scope.selectedBusinessModel.id = response.data.id;
					angular.copy($scope.selectedBusinessModel,$scope.savedBusinessModel);
					$scope.businessModelList.push(response.data);
					$scope.selectedVersions=[];
					DriversService.driverRelatedObject = $scope.selectedBusinessModel;
					$scope.isDirty = false;
					$scope.isCWMDirty = false;
					if($scope.fileObj.fileName !== undefined)
						$scope.saveBusinessModelFile();
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'check');
				}, function(response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				});
			}else{
				$scope.varTablePrefixLikeValue=$scope.selectedBusinessModel.tablePrefixLike;
			    $scope.varTablePrefixNotLikeValue=$scope.selectedBusinessModel.tablePrefixNotLike;

				sbiModule_restServices.promisePut("2.0/businessmodels", $scope.selectedBusinessModel.id, $scope.selectedBusinessModel)
				.then(function(response) {

					if($scope.fileObj.fileName !== undefined)
						$scope.saveBusinessModelFile();

					if($scope.bmVersionsActive != null){
						sbiModule_restServices.promisePut("2.0/businessmodels/" + $scope.selectedBusinessModel.id+"/versions/"+ $scope.bmVersionsActive,"")
						.then(function(response) {
							angular.copy($scope.selectedBusinessModel,$scope.savedBusinessModel);
						}, function(response) {
							sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
						});
					}
					$scope.businessModelList=[];
					$scope.getBusinessModels();
					$scope.isDirty = false;
					$scope.selectedBusinessModel.modelLocker = response.data.modelLocker;
					DriversService.persistDrivers($scope.selectedBusinessModel.id, requiredPath);
					DriversService.persistVisualDependency($scope.selectedBusinessModel.id, requiredPath);
					DriversService.deleteDrivers($scope.selectedBusinessModel.id, requiredPath);
					DriversService.deleteVisualDependencies($scope.selectedBusinessModel.id, requiredPath);

					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'check');
				}, function(response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				});
			}
	 }

	 //calling service method DELETE/{bmId} deleting single item
	 $scope.deleteItem=function(item,event){
			 var id = item.id;
				var confirm = $mdDialog
				.confirm()
				.title(sbiModule_translate.load("sbi.businessModelsCatalogue.confirm.delete"))
				.content(
						sbiModule_translate
						.load("sbi.businessModelsCatalogue.confirm.delete.content"))
						.ariaLabel('Lucky day').ok(
								sbiModule_translate.load("sbi.general.continue")).cancel(
										sbiModule_translate.load("sbi.general.cancel"));

				$mdDialog.show(confirm).then(function() {

					sbiModule_restServices.promiseDelete("2.0/businessmodels",id)
					.then(function(response) {
						removeFromBMs(id,"left");
						 if($scope.selectedBusinessModel.id == id){
							 $scope.selectedBusinessModel={};
						 }
						 $scope.selectedBusinessModels=[];
						 sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'check');

					}, function(response) {
						sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

					});

				}, function() {

				});

	 }

		//calling service method DELETE/{bmId}/versions/{vId} deleting single version of selected model
		$scope.deleteItemVersion=function(item,event){

			var bmId = $scope.selectedBusinessModel.id;
			var id = item.id;

			var confirm = $mdDialog
			.confirm()
			.title(sbiModule_translate.load("sbi.businessModelsCatalogue.confirm.versionDelete"))
			.content(
					sbiModule_translate
					.load("sbi.businessModelsCatalogue.confirm.versionDelete.content"))
					.ariaLabel('Lucky day').ok(
							sbiModule_translate.load("sbi.general.continue")).cancel(
									sbiModule_translate.load("sbi.general.cancel"));

			$mdDialog.show(confirm).then(function() {

				sbiModule_restServices.promiseDelete("2.0/businessmodels/"+bmId+"/versions/"+id,"")
				.then(function(response) {
					removeFromBMs(id,"right");
					$scope.getVersions(bmId);
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'check');

				}, function(response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

				});

			}, function() {

			});
		}
		//my util functions


		//make path

		makeDeletePath = function(selectedItems){
			 var s="?";

			 for(var i=0; i<selectedItems.length;i++){
				 s+="id="+selectedItems[i].id+"&";
			 }

			 return s;
		}

		//list updating

		removeFromBMs = function(id,table){

			 if(table === "left")
				 var array = $scope.businessModelList;
			 else
				 var array = $scope.bmVersions;

			 for(var i=0;i<array.length;i++){
				 if(array[i].id == id)
					 array.splice(i,1);
			 }

			 if(table === "left")
				 $scope.businessModelList = array;
			 else{
				 $scope.bmVersions = array;
			 }

		}

		//date/time format
		millisToDate = function(data){
			 for(var i=0; i<data.length;i++){
				 var date = new Date(data[i].creationDate);

				 var dd = date.getDate().toString();
				 var mm = (date.getMonth()+1).toString();

				 var h = date.getHours().toString();
				 var m = date.getMinutes().toString();
				 var s = date.getSeconds().toString();

				 data[i].creationDate =
					 (dd[1]?dd:"0"+dd)+"/"
					 +(mm[1]?mm:"0"+mm)+"/"
					 +date.getFullYear()+" "
					 +(h[1]?h:"0"+h)+":"
					 +(m[1]?m:"0"+m)+":"
					 +(s[1]?s:"0"+s);
			 }
		}

		//toast
		$scope.showActionOK = function(msg) {
//				    var toast = $mdToast.simple()
//				    .content(msg)
//				    .action('OK')
//				    .highlightAction(false)
//				    .hideDelay(3000)
//				    .position('top')
//
//				    $mdToast.show(toast).then(
//				    		function(response) {
//				    			if ( response == 'ok' ) {
//				    			}
//				    		});

				    sbiModule_messaging.showInfoMessage(msg,"");

		};

		$scope.fileChange = function(){
			$scope.fileClicked = true;  // tells that file input has been clicked
		}

		/*
		$scope.fileCWMChange = function(){
			$scope.fileCWMClicked = true;  // tells that file input has been clicked
			$scope.bmCWMDisableImportButton = false;

		}
		*/


		/*
		//check if is name dirty
		$scope.checkCWMChange = function(){

			// if file is new check also file has been added
			if( $scope.fileCWMClicked === false) {
					$scope.isCWMDirty = false;
			}
			else{
				$scope.isCWMDirty = true;
			}
		}
		*/

		//get item by id
		getItemById = function(id){
			for(var i = 0; i < $scope.businessModelList.length ; i++){
				if($scope.businessModelList[i].id == id)
					return $scope.businessModelList[i];
			}
		}

		//comparing bms
		compareBusinessModels = function(bm1,bm2){
			for(var i in bm1){
				if(bm1[i] !== bm2[i])
					return false;
			}
				return true;
		}

		// import the model into the metadata tabels
		$scope.importMetadata = function(bmId) {
			var confirm = $mdDialog
			.confirm()
			.title(sbiModule_translate.load("sbi.catalogues.generic.import"))
			.content(
					sbiModule_translate
					.load("sbi.catalogues.generic.import.msg"))
					.ariaLabel('ImportMetadata').ok(
							sbiModule_translate.load("sbi.general.continue")).cancel(
									sbiModule_translate.load("sbi.general.cancel"));

			$mdDialog.show(confirm).then(function() {
				$scope.bmImportingShow = true;

				sbiModule_restServices.promisePost("2.0/metadata/"+bmId+"/bmExtract","")
				.then(function(response) {
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.imported"), 'check');
					$scope.bmImportingShow = false;

				}, function(response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
					$scope.bmImportingShow = false;
				});

			}, function() {

			});

		}

		$scope.saveBtnDisabled = function(){
			if($scope.selectedBusinessModel.name === undefined || $scope.selectedBusinessModel.name === ""
				|| ( $scope.selectedBusinessModel.dataSourceLabel == undefined || $scope.selectedBusinessModel.dataSourceLabel == null)
			){
					return true;
			}
			else{
				return false;
			}
		};

		/*
		//import CWM Metamodel informations
		$scope.importCWMFile = function(bmId) {
			var confirm = $mdDialog
			.confirm()
			.title(sbiModule_translate.load("sbi.metadata.cwm.import"))
			.content(
					sbiModule_translate
					.load("sbi.metadata.cwm.import.msg"))
					.ariaLabel('ImportMetadata').ok(
							sbiModule_translate.load("sbi.general.continue")).cancel(
									sbiModule_translate.load("sbi.general.cancel"));


			if( $scope.fileObjCWM.fileName !== undefined){
				$mdDialog.show(confirm).then(function() {
					$scope.bmCWMImportingShow = true;
					$scope.bmCWMDisableImportButton = true;
					//Upload file
					multipartForm.post("2.0/metadata/"+bmId+"/importCWM",$scope.fileObjCWM).success(

							function(data,status,headers,config){
								if(data.hasOwnProperty("errors")){
									console.log("[UPLOAD]: DATA HAS ERRORS PROPERTY!");
									sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.metadata.cwm.error")+":"+data.errors[0].message, 'Error');

								}else{
									sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.metadata.cwm.success"), 'check');
									console.log("[UPLOAD]: SUCCESS!");
									$scope.fileObjCWM.fileName = "";
									$scope.fileObjCWM = {};
								}
								$scope.bmCWMImportingShow = false;
								$scope.bmCWMDisableImportButton = true;

							}).error(function(data, status, headers, config) {
								console.log("[UPLOAD]: FAIL!"+status);
								sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.ds.failedToUpload"), 'Error');
								$scope.bmCWMImportingShow = false;
								$scope.bmCWMDisableImportButton = true;
							});
				});

			}
		}
		*/


		 $scope.confirm = $mdDialog
	      .confirm()
	      .title(sbiModule_translate.load("sbi.catalogues.generic.modify"))
	      .content(
	              sbiModule_translate
	              .load("sbi.catalogues.generic.modify.msg"))
	              .ariaLabel('toast').ok(
	                      sbiModule_translate.load("sbi.general.continue")).cancel(
	                              sbiModule_translate.load("sbi.general.cancel"));

		 activeFlagStyle = function(){
				 for(var i=0; i<$scope.bmVersions.length;i++){
					 $scope.bmVersions[i]["ACTION"] = '<md-radio-button value="'+$scope.bmVersions[i].id+'"></md-radio-button>';
					 if($scope.bmVersions[i].active){
						 $scope.bmVersionsRadio = $scope.bmVersions[i].id;
						 $scope.bmVersionsActive = $scope.bmVersions[i].id;
					 }
				 }
		 }

		 $scope.clickRightTable = function(item){
			 for(var i=0; i<$scope.bmVersions.length;i++){
				 if($scope.bmVersions[i].id == $scope.bmVersionsActive) {
					 $scope.bmVersions[i].active = false;
				 }
			 }
			 $scope.bmVersionsActive = item.id;
			 item.active = true;
		 }


		 /**
		  * Generate Datamart Options Dialog
		  */

		 $scope.openGenerateDatamartDialog = function() {
			 sbiModule_restServices.alterContextPath(sbiModule_config.contextMetaName);
				sbiModule_restServices.promiseGet("1.0/metaWeb", "modelInfos/"+$scope.selectedBusinessModel.id+"?user_id="+sbiModule_user.userUniqueIdentifier)
				.then(
						function(response) {
							//parse the response to get schema and catalog
							var catalogName = "";
							var schemaName  = "";
							if (response.data != undefined){
								if (response.data.catalogName != undefined && response.data.catalogName != null){
									catalogName = response.data.catalogName;
								}
								if (response.data.schemaName != undefined && response.data.schemaName != null){
									schemaName = response.data.schemaName;
								}
							}

							$mdDialog.show({
								controller: generateDatamartOptionsController,
								preserveScope: true,
								locals: {selectedBusinessModel:$scope.selectedBusinessModel,userId:sbiModule_user.userId,catalogName:catalogName,schemaName:schemaName,parentController:$scope},
								templateUrl:sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/catalogues/templates/generateDatamartOptions.html',
								clickOutsideToClose:false,
								escapeToClose :false,
								fullscreen: true
							});
						},
						function(response) {
							//errors case
							sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.catalogues.generation.infomation.error"));
						}
				);


		 }

		 $scope.createBusinessModels=function(){


			 if ($scope.varTablePrefixLikeValue != $scope.selectedBusinessModel.tablePrefixLike ||
					 $scope.varTablePrefixNotLikeValue != $scope.selectedBusinessModel.tablePrefixNotLike) {

				 $scope.varTablePrefixLikeValue = $scope.selectedBusinessModel.tablePrefixLike;
				 $scope.varTablePrefixNotLikeValue = $scope.selectedBusinessModel.tablePrefixNotLike;

				 $mdDialog.show(
					      $mdDialog.alert()
					       .parent(angular.element(document.querySelector('#popupContainer')))
					       .clickOutsideToClose(true)
					       .title('Save operation required')
					       .ok('OK')
					   );
			 } else {

			 var dsId;
			 for(var i=0;i<$scope.listOfDatasources.length;i++){
				 if(angular.equals($scope.listOfDatasources[i].label, $scope.selectedBusinessModel.dataSourceLabel)){
					 dsId=$scope.listOfDatasources[i].dsId;
					 break;
				 }
			 }

				window.addEventListener("message", (event) => {
					if (event.data && event.data.action == 'closeDialog') {
						$mdDialog.cancel();
					}
				}, false);

				$mdDialog.show({
					preserveScope: true,
					controller: function($scope,$mdDialog,url){
						$scope.metaUrl=url;
						$scope.closeMetaWeb=function(){
							$mdDialog.hide();
						}


					},
					template:   '<md-dialog aria-label="Open meta"  style="width: 100%;  height: 100%;max-width: 100%;  max-height: 100%;" ng-cloak>'+
								'<md-dialog-content flex layout="column" class="metaContent" >'+
								'<iframe layout-fill id="metaWebEditor" class=" noBorder" ng-src="{{metaUrl}}" name="metaIframe"  ></iframe>'+
								'<div loading id="loadMask" layout-fill style="position:fixed;z-index: 500;background:rgba(0,0,0, 0.3);">'+
								'<md-progress-circular  md-mode="indeterminate" style="top:50%;left:50%" ></md-progress-circular></div>'+
								'</md-dialog-content> '+
								'</md-dialog>',
					clickOutsideToClose:true,
					escapeToClose :true,
					fullscreen: true,
					locals:{
						url : sbiModule_config.contextMetaName + "/restful-services/1.0/pages/edit?"
							+ "datasourceId=" + dsId
							+ "&user_id=" + sbiModule_user.userUniqueIdentifier
							+ "&bmId=" + $scope.selectedBusinessModel.id
							+ "&bmName=" + encodeURIComponent($scope.selectedBusinessModel.name)
							+ (($scope.selectedBusinessModel.tablePrefixLike) ? "&tablePrefixLike=" + $scope.selectedBusinessModel.tablePrefixLike : "")
							+ (($scope.selectedBusinessModel.tablePrefixNotLike) ? "&tablePrefixNotLike=" + $scope.selectedBusinessModel.tablePrefixNotLike : "")
						}
				}).then(function(){
					//refresh
					$scope.getVersions($scope.selectedBusinessModel.id);
				})
			 }
				;
			}

		 $scope.isEdit = function(model) {
			 if(model.hasOwnProperty('id')){
				 return true;
			 } else {
				 return false;
			 }
		 }

		 $scope.resetLikeConditions = function() {
			 if(!$scope.metaWebFunctionality){
				 if ($scope.selectedBusinessModel && $scope.selectedBusinessModel.tablePrefixLike) delete $scope.selectedBusinessModel.tablePrefixLike;
				 if ($scope.selectedBusinessModel && $scope.selectedBusinessModel.tablePrefixNotLike) delete $scope.selectedBusinessModel.tablePrefixNotLike;
			 }
		 }

};



app.directive('fileModel',['$parse',function($parse){

		return {
			restrict:'A',
			link: function(scope,element,attrs){
				var model = $parse(attrs.fileModel);
				var modelSetter = model.assign;

				element.bind('change',function(){
					scope.$apply(function(){
						modelSetter(scope,element[0].files[0]);

					})
				})
			}
		}

	}]);

app.service('multipartForm',['$http',function($http){

		this.post = function(uploadUrl,data){

			var formData = new FormData();

			formData.append("file",data.file);

			return	$http.post(uploadUrl,formData,{
					transformRequest:angular.identity,
					headers:{'Content-Type': undefined}
				})
		}

	}]);

