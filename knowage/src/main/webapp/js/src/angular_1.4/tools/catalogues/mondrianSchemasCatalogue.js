/**
 *
 */

var app = angular.module('mondrianSchemasCatalogueModule',['ngMaterial', 'ngMessages', 'angular_list' , 'angular_table' , 'sbiModule' , 'angular_2_col','file_upload','angular-list-detail', 'angularXRegExp']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);

app.controller('mondrianSchemasCatalogueController',["sbiModule_translate","sbiModule_restServices", "kn_regex", "$scope","$mdDialog","$mdToast","$timeout","$filter","multipartForm","sbiModule_messaging","sbiModule_download","sbiModule_user",mondrianSchemasCatalogueFunction]);

function mondrianSchemasCatalogueFunction(sbiModule_translate, sbiModule_restServices,kn_regex, $scope, $mdDialog, $mdToast,$timeout,$filter,multipartForm,sbiModule_messaging,sbiModule_download,sbiModule_user){

	$scope.regex = kn_regex;
	$scope.translate = sbiModule_translate;
	$scope.showMe = false;
	$scope.catalogLoadingShow = false;
	$scope.versionLoadingShow = false;
	$scope.showCatalogs = false;
	$scope.showVersions = false;
	$scope.selectedMondrianSchema={};
	$scope.selectedVersion = {};
	$scope.itemList = [];
	$scope.progressValue = 50;
	$scope.fileList =[];
	$scope.servicePath = "2.0/mondrianSchemasResource";
	$scope.file ={};
	$scope.isDirty = false;


	$scope.disableWorkFlow = !(sbiModule_user.functionalities.indexOf("WorkFlowManagment")>-1);
	//workflow variables
	$scope.isStartedWf=false;
	$scope.allUsers =[];
	$scope.availableUsersList=[];
	$scope.wfSelectedUserList =[];
	$scope.wfEdited = false;//is edited?
	$scope.wfStarted = false;//is still editable
	$scope.wfExists = false;
	var waitForSave = false;
	var startAfterCreation;
	$scope.userInProg ;

	//workflow methods
	$scope.moveToWorkflow = function(item, fromInterface){
		if(!fromInterface)
			goodToGo = true;
		else if(!$scope.isStartedWf)
				goodToGo = true;
		else
			goodToGo = false;

		if(goodToGo){
			$scope.wfEdited = true;
			var index = $scope.availableUsersList.indexOf(item);

			$scope.wfSelectedUserList.push(item);

			$scope.availableUsersList.splice(index,1);
		}
		else{
			sbiModule_messaging.showInfoMessage("Workflow is already started", 'Info');
		}
	}

	$scope.removeFromWorkflow = function(item){
		$scope.wfEdited = true;
		var index = $scope.wfSelectedUserList.indexOf(item);

		$scope.availableUsersList.push(item);

		$scope.wfSelectedUserList.splice(index,1);
	}

	loadAllUsers = function(){
		sbiModule_restServices.promiseGet("2.0/users","")
		.then(function(response) {
			$scope.allUsers =response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	};

	isWorkflowStarted = function(modelId){
		sbiModule_restServices.promiseGet("2.0/workflow/isStarted/"+modelId,"")
		.then(function(response) {
			if(response.data > 0){
				$scope.isStartedWf = true;
				$scope.userInProg = response.data;
			}
			else
				$scope.isStartedWf = false;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	};

	loadUsersInWF = function(modelId){
		sbiModule_restServices.promiseGet("2.0/workflow/"+modelId,"")
		.then(function(response) {
			$scope.availableUsersList=[];
			$scope.availableUsersList = $scope.allUsers.slice();
			$scope.wfSelectedUserList =[];

			if(response.data.length > 0){
				$scope.wfExists = true;
				for(var i=0; i<response.data.length;i++){
					for(var j=0;j< $scope.availableUsersList.length;j++){
						if(response.data[i] == $scope.availableUsersList[j].id){
							$scope.moveToWorkflow($scope.availableUsersList[j],false);
							break;
						}
					}
				}
				$scope.wfEdited = false;
			}
			else{
				waitForSave = true;
				$scope.wfExists = false;
			}

		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	};

	$scope.createNewWorkflow = function(mid){
		var toSend = {}
		toSend.workflowArr = $scope.wfSelectedUserList;
		toSend.modelId = mid;
		var encoded = encodeURI('workflow');

		sbiModule_restServices.promisePost("2.0",encoded,toSend).then(
				function(response){
					$scope.wfEdited = false;
					waitForSave = false;
					sbiModule_messaging.showSuccessMessage("Workflow created", 'Success');
					if(startAfterCreation)
						$scope.startWorkflow(mid);
				},
				function(response){
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				});
	};

	$scope.startWorkflow = function(modelId){
		if(!waitForSave){
			if(modelId != undefined){
				sbiModule_restServices.promisePut("2.0/workflow/startWorkflow/"+modelId,"")
				.then(function(response) {
					$scope.isStartedWf = true;
					sbiModule_messaging.showSuccessMessage("Workflow started", 'Success');
				}, function(response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

				});
			}
			else{
				startAfterCreation = true;
			}
		}
		else{
			sbiModule_messaging.showInfoMessage("To start workflow you have to save schema first", 'Info');
		}

	};

	updateWorkflow = function(mid){
		var toSend = {}
		toSend.workflowArr = $scope.wfSelectedUserList;
		toSend.modelId = mid;
		var encoded = encodeURI('workflow/update');

		sbiModule_restServices.promisePut("2.0",encoded,toSend).then(
				function(response){

					sbiModule_messaging.showSuccessMessage("Workflow updated", 'Success');

				},
				function(response){
					sbiModule_messaging.showErrorMessage("Error occured while updating workflow", 'Error');
				});
	};

	moveUserInWf = function(direction, id){
		$scope.wfEdited = true;
		var index = getIndexOfId(id);
		var len = $scope.wfSelectedUserList.length;
		var pom;

		if(direction == "up" && index > 0){
			pom = $scope.wfSelectedUserList[index-1];
			$scope.wfSelectedUserList[index-1] = $scope.wfSelectedUserList[index];
			$scope.wfSelectedUserList[index] = pom;
		}
		if(direction == "down" && index < len-1){
			pom = $scope.wfSelectedUserList[index+1];
			$scope.wfSelectedUserList[index+1] = $scope.wfSelectedUserList[index];
			$scope.wfSelectedUserList[index] = pom;
		}
	};

	getIndexOfId = function(id){
		for(var i=0; i<$scope.wfSelectedUserList.length;i++){
			if($scope.wfSelectedUserList[i].id == id)
				return i;
		}
	};

	$scope.isStartVisible = function(){
		if($scope.wfSelectedUserList.length == 0 || $scope.isStartedWf){
			return false;
		}
		else{
			return true;
		}
	};

	$scope.workflowSpeedMenu = [
	    {
	    	label:"Move up",
	    	icon:'fa fa-arrow-circle-up',
	    	color:'#3B678C',
	    	visible:function(){
	    		return !$scope.isStartedWf;
	    	},
	    	action:function(item,event){
	    		moveUserInWf("up", item.id);
	    	}
	    },
	    {
	    	label:"Move down",
	    	icon:'fa fa-arrow-circle-down',
	    	color:'#3B678C',
	    	visible:function(){
	    		return !$scope.isStartedWf;
	    	},
	    	action:function(item,event){
	    		moveUserInWf("down", item.id);
	    	}
	    },
	    {
	    	label:"Remove from workflow",
	    	icon:'fa fa-times-circle',
	    	color:'#aa0000',
	    	visible:function(){
	    		return !$scope.isStartedWf;
	    	},
	    	action:function(item,event){
	    		$scope.removeFromWorkflow(item);
	    	}
	    }
	];

	$scope.workflowSpeedMenuSt = [

	    {
	    	label:"Active user",
	    	icon:'fa fa-check-circle',
	    	color:'#1E9144',
	    	visible:function(item){
	    		if(item.id == $scope.userInProg)
	    			return true;
	    		else
	    			return false;
	    	}
	    }
	];
	//old Dragan stuff
	$scope.isDisabled = function(){
		if($scope.selectedMondrianSchema.id == undefined){
			if($scope.selectedMondrianSchema.name == undefined || $scope.selectedMondrianSchema.name=="" || $scope.file.file == undefined)
				return true;
			else
				return false;
		}
		return false;

	}

		$scope.downloadFile = function(item){

			var link = "/restful-services/2.0/mondrianSchemasResource/"+$scope.selectedMondrianSchema.id+"/versions/"+item.id+"/file";
			sbiModule_download.getLink(link);

			}


	$scope.unlockModel= function(){

		$scope.selectedMondrianSchema.modelLocked = false;
		sbiModule_messaging.showInfoMessage("Mondrian Schema "+$scope.selectedMondrianSchema.name+" is unlocked", 'Information!');
	}

	$scope.isUniqueInList = function(propertyName,obj,listOfObjs){

		if(obj.hasOwnProperty(propertyName)&&itemList.length){

			for(var item in itemList){


			}

		}
	}




	angular.element(document).ready(function () {
        $scope.getMondrianSchemas();
        loadAllUsers();

    });

	$scope.catalogueSpeedOptions =  [


		{

		label:sbiModule_translate.load("sbi.generic.delete"),
		icon:'fa fa-trash',
		//icon:'fa fa-trash-o fa-lg',
	    // color:'#153E7E',
	    action:function(item){
	    $scope.confirmDelete(item,event);


	    	}
	 	}

	];

	$scope.versionsSpeedOptions =  [

	  {

		label:sbiModule_translate.load("sbi.generic.download"),
	    icon:'fa fa-download',
	   // color:'#153E7E',
	    action:function(item,event){
	    $scope.downloadFile(item,event);

	    }
	  }
		  ,

	   {

		label:sbiModule_translate.load("sbi.generic.delete"),
	    icon:'fa fa-trash',
		//icon:'fa fa-trash-o fa-lg',
	    //color:'#153E7E',
	    action:function(item){
	    $scope.confirmDelete(item,event);


	    }
	  }

	  ];

	 $scope.confirmDelete = function(item,ev) {
		    var confirm = $mdDialog.confirm()
		          .title(sbiModule_translate.load("sbi.catalogues.toast.confirm.title"))
		          .content(sbiModule_translate.load("sbi.catalogues.toast.confirm.content"))
		          .ariaLabel("confirm_delete")
		          .targetEvent(ev)
		          .ok(sbiModule_translate.load("sbi.general.continue"))
		          .cancel(sbiModule_translate.load("sbi.general.cancel"));
		    $mdDialog.show(confirm).then(function() {

		    	if (item.name) {
		    		console.log("deleting schemaa");
		    		$scope.deleteMondrianSchema(item);
		    	} else if (item.fileName) {
		    		console.log("deleting version");
		    		$scope.deleteVersion(item);
		    	}

		    }, function() {

		    });
		  };


	$scope.saveMondrianCatalogue = function(){

		console.log("saving Schema...");

		if(!isNaN($scope.selectedMondrianSchema.id)){

				console.log("updating...");
				$scope.modifyMondrianSchema();

		}else{

			if($scope.selectedMondrianSchema.name===undefined){

					console.log("name is required");
					console.log($scope.selectedMondrianSchema.name);
					$scope.showActionOK("Name is required");

				}else{

					console.log("adding new...");
					$scope.addNewMondrianSchema();

				}
		}

	};
	$scope.createMondrianSchema = function(){
		startAfterCreation = false;
		$scope.isStartedWf = false;
		$scope.wfEdited = false;
		$scope.availableUsersList = [];
		$scope.availableUsersList = $scope.allUsers.slice(); //load all users
		$scope.wfSelectedUserList =[]; // clear selected when creatig new

		if($scope.isDirty){
			$mdDialog.show($scope.confirm).then(function(){
		    	$scope.isDirty=false;
		    	$scope.selectedMondrianSchema = {};
				$scope.selectedVersion = {};
				$scope.fileList = [];
				$scope.file ={};
		    },
		     function(){
		    	  $scope.showMe = true;
		      });
		}
		else{
			$scope.isDirty=false;
	    	$scope.selectedMondrianSchema = {};
			$scope.selectedVersion = {};
			$scope.fileList = [];
			$scope.file ={};
		}
		$scope.showMe = true;

	}

	$scope.cancel = function(){
		$scope.showMe = false;
	}

	//CLICK FUNCTION FOR CATALOQUE TABLE
	$scope.catalogueClickFunction = function(item){
		if(!$scope.disableWorkFlow ){
			loadUsersInWF(item.id); // loading users for existing document legit state of catalog
			isWorkflowStarted(item.id);
		}


		if(item!=$scope.selectedMondrianSchema){
			if($scope.isDirty){
				$mdDialog.show($scope.confirm).then(function(){
			    	$scope.isDirty=false;
			    	$scope.selectedMondrianSchema ={};
					$scope.selectedMondrianSchema = angular.copy(item);
					$scope.fileList= $scope.getMondrianSchemasVersion();
			    },
			     function(){
			    	  $scope.showMe = true;
			      });

			}
			else{
				$scope.selectedMondrianSchema ={};
				$scope.selectedMondrianSchema = angular.copy(item);
				$scope.fileList= $scope.getMondrianSchemasVersion();
			}
		}
		$scope.showMe = true;
		console.log($scope.selectedMondrianSchema);

	}

	$scope.changeApplied = function(){
		$scope.isDirty = true;
	};

	//CLICK FUNCTION FOR VERSIONS TABLE
	$scope.versionClickFunction = function(item){
		$scope.showMe = true;
		if(item!=$scope.selectedMondrianSchema){

			$scope.selectedVersion = angular.copy(item);

		}

		console.log($scope.selectedVersion);

	}




	//REST

	//GET ALL MONDRIAN SCHEMAS
	$scope.getMondrianSchemas = function(){

		sbiModule_restServices.promiseGet($scope.servicePath,"")
		.then(function(response) {
			$scope.catalogLoadingShow =true;
			$scope.showCatalogs = false;
			$scope.itemList=[];


			setTimeout(function(){
				$scope.itemList = response.data;
				$scope.catalogLoadingShow = false;
				$scope.showCatalogs = true;
				$scope.$apply();
			},10)
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});

	};

	//GET ALL MONDRIAN SCHEMAS VERSIONS
	$scope.getMondrianSchemasVersion = function(){

		sbiModule_restServices.promiseGet("2.0/mondrianSchemasResource/"+$scope.selectedMondrianSchema.id+"/versions","")
		.then(function(response) {
			$scope.showVersions=false;
			$scope.versionLoadingShow = true;
			$scope.fileList =[];


				setTimeout(function(){


						$scope.fileList=response.data;
						for(var i= 0 ; i<$scope.fileList.length;i++){

							$scope.fileList[i].creationDate = new Date(response.data[i].creationDate).toLocaleString();
							$scope.fileList[i].actives = "<md-radio-button ng-value = "+$scope.fileList[i].id+" aria-label='label'></md-radio-button>";

							if($scope.fileList[i].active){

								$scope.selectedMondrianSchema.currentContentId = $scope.fileList[i].id;
								for(var j =0; j<$scope.itemList.length;j++){
									if($scope.itemList[j].id===$scope.selectedMondrianSchema.id){

										for(var key in $scope.selectedMondrianSchema){
											$scope.itemList[j][key] = $scope.selectedMondrianSchema[key];
										}

									}
								}


							}


						}

						$scope.versionLoadingShow = false;
						$scope.showVersions=true;


						$scope.$apply();

					},500)
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
		};

	//POST NEW MONDRIAN SCHEMA
	$scope.addNewMondrianSchema = function(){
		$scope.isDirty = false;
		$scope.selectedMondrianSchema.type ="MONDRIAN_SCHEMA";
		console.log($scope.selectedMondrianSchema);

		sbiModule_restServices.promisePost($scope.servicePath,"",$scope.selectedMondrianSchema)
		.then(function(response) {
			$scope.selectedMondrianSchema = response.data;
			$scope.itemList.unshift($scope.selectedMondrianSchema);
			sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');

			if($scope.file.file){
					console.log("uploading...");
					$scope.uploadFile();

				}
			if($scope.wfEdited)
				$scope.createNewWorkflow(response.data.id);
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});

		};

	//POST UPLOAD FILE
	$scope.uploadFile= function(){

		multipartForm.post("2.0/mondrianSchemasResource/"+$scope.selectedMondrianSchema.id+"/versions",$scope.file).success(

			function(data,status,headers,config){
				if(data.hasOwnProperty("errors")){

					console.log("[UPLOAD]: DATA HAS ERRORS PROPERTY!");

				}else{

					console.log("[UPLOAD]: SUCCESS!");
					sbiModule_messaging.showSuccessMessage("Mondrian schema version "+$scope.file.fileName+" successfully uploaded", 'Success!');
					$scope.getMondrianSchemasVersion();
					$scope.file={};


				}
			}).error(function(data, status, headers, config) {
						console.log("[UPLOAD]: FAIL!"+status);
					});

	}


	//PUT MODIFY MONDRIAN SCHEMA
	$scope.modifyMondrianSchema = function(){
		$scope.isDirty = false;
		var mId = $scope.selectedMondrianSchema.id;
		sbiModule_restServices.promisePut($scope.servicePath,$scope.selectedMondrianSchema.id,$scope.selectedMondrianSchema)
		.then(function(response) {

			console.log("[PUT]: SUCCESS!");
			sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');

			for(var j =0; j<$scope.itemList.length;j++){
							if($scope.itemList[j].id===response.data.id){
								for(var key in response.data){
									$scope.itemList[j][key] = response.data[key];
								}
								$scope.selectedMondrianSchema = angular.copy($scope.itemList[j]);
							}
						}


		if($scope.file.file){
			console.log("uploading...");
			$scope.uploadFile();

		}

		if($scope.wfEdited && !$scope.wfExists)
			$scope.createNewWorkflow(mId);
		if($scope.wfEdited && $scope.wfExists)
			updateWorkflow(mId);
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	}


	//DELETE MONDRIAN SCHEMA
	$scope.deleteMondrianSchema = function(item){

		if($scope.selectedMondrianSchema.modelLocker!=null){

			sbiModule_messaging.showInfoMessage("Model is locked", 'Information!');
			console.log("model is locked");

		}else{
			console.log($scope.selectedMondrianSchema.modelLocker);

			sbiModule_restServices.promiseDelete($scope.servicePath,item.id)
			.then(function(response) {
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
				$scope.getMondrianSchemas();
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

			});
		}
	}

	//DELETE VERSION
	$scope.deleteVersion = function(item){

		if($scope.selectedMondrianSchema.modelLocker!=null){

			sbiModule_messaging.showInfoMessage("Model is locked", 'Information!');
			console.log("model is locked");

		}else{

			sbiModule_restServices.promiseDelete("2.0/mondrianSchemasResource/"+$scope.selectedMondrianSchema.id+"/versions",item.id)
			.then(function(response) {
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
				$scope.getMondrianSchemasVersion();
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

			});
		}
	}

	$scope.confirm = $mdDialog
    .confirm()
    .title(sbiModule_translate.load("sbi.catalogues.generic.modify"))
    .content(
            sbiModule_translate
            .load("sbi.catalogues.generic.modify.msg"))
            .ariaLabel('toast').ok(
                    sbiModule_translate.load("sbi.general.continue")).cancel(
                            sbiModule_translate.load("sbi.general.cancel"));

};

/*
app.directive('fileModel',['$parse',function($parse){

	return {
		restrict:'A',
		link: function(scope,element,attrs){

			var model = $parse(attrs.fileModel);
			var modelSetter = model.assign;
			console.log(modelSetter+"model");

			element.bind('change',function(){
				scope.$apply(function(){
					modelSetter(scope,element[0].files[0]);


				})
			})
		}
	}


}]);*/


app.service('multipartForm',['$http',function($http){

	this.post = function(uploadUrl,data){

		var formData = new FormData();

		for(var key in data){


				formData.append(key,data[key]);
			}

		return $http.post(uploadUrl,formData,{
			transformRequest:angular.identity,
			headers:{'Content-Type': undefined}
		})
	}

}]);





