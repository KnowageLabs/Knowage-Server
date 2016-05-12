/**
 * 
 */

var app = angular.module('businessModelCatalogueModule',['ngMaterial', 'angular_list', 'angular_table','sbiModule', 'angular_2_col','file_upload','angular-list-detail']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);
app.controller('businessModelCatalogueController',["sbiModule_translate", "sbiModule_restServices", "$scope", "$mdDialog", "$mdToast","multipartForm", "sbiModule_download","sbiModule_messaging",businessModelCatalogueFunction]);

function businessModelCatalogueFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,multipartForm,sbiModule_download,sbiModule_messaging){
	
	//variables
	///////////////////////////////////////////////////////////
	$scope.isDirty = false;
	$scope.isCWMDirty = false;
	$scope.showMe = false;				//boolean
	$scope.versionLoadingShow;
	$scope.bmLoadingShow;
	$scope.bmImportingShow;
	$scope.bmCWMProcessingShow;
	$scope.bmCWMImportingShow;
	$scope.isNew;
	
	$scope.translate = sbiModule_translate;
	$scope.businessModelList=[];		//All Business Models list
	$scope.listOfDatasources = [];		//Dropdown
	$scope.listOfCategories=[];			//Dropdown
	$scope.bmVersions=[];				//All versions of BM list
	$scope.selectedBusinessModels=[];	//Selected Business Models table multiselect
	$scope.selectedVersions=[];			//Selected BM Versions table multiselect
	$scope.selectedBusinessModel = {}; //Selected model for editing or new model data
	$scope.bmVersionsRadio;
	$scope.bmVersionsActive;
	$scope.fileObj ={};
	$scope.fileObjCWM ={};

	$scope.fileClicked =false;
	$scope.fileCWMClicked =false;


	angular.element(document).ready(function () {
        $scope.getData();
    });

	
	//methods
	//////////////////////////////////////////////////////////
	   
	 $scope.getData = function(){
		 $scope.getBusinessModels();
		 $scope.getDataSources();
		 $scope.getCategories();
	 }

	
	$scope.createBusinessModel = function(){
		$scope.selectedBusinessModel = {};
		$scope.bmVersions=[];
		$scope.isNew = true;
		$scope.showMe = true;
		$scope.fileClicked = false;
		$scope.fileCWMClicked = false;
		
		$scope.isDirty=false;
		$scope.isCWMDirty = false;
	}
	
	
	
	$scope.cancel = function(){
		$scope.showMe = false;
		$scope.isDirty = false;
		$scope.isCWMDirty = false;
		$scope.selectedBusinessModel = {};
		$scope.bmVersions=[];
	}
	
	$scope.businessModelLock = function(){
		//should also check if user is allowed to do this
		$scope.selectedBusinessModel.modelLocked = !$scope.selectedBusinessModel.modelLocked;
		if($scope.selectedBusinessModel.modelLocked){
			$scope.selectedBusinessModel.modelLocker = valueUser;
		}
		else{
			$scope.selectedBusinessModel.modelLocker = "";
		}
		$scope.checkChange();
	}
	
	$scope.lockBusinessModel = function(){
		//should also check if user is allowed to do this
		$scope.selectedBusinessModel.modelLocked = true;
		console.log($scope.selectedBusinessModel);
	}
	
	$scope.leftTableClick = function(item){
		$scope.isNew = false;
		if($scope.isDirty){
		    $mdDialog.show($scope.confirm).then(function(){
		    	$scope.isDirty=false;   
		    	$scope.isCWMDirty = false;
		    	$scope.selectedBusinessModel=angular.copy(item);
		    	$scope.showMe=true;
		    },
		     function(){		       
		    	  $scope.showMe = true;
		      });
		      
		     }else{		    
		    	 $scope.selectedBusinessModel=angular.copy(item);
		    	 console.log("selecetd bm:");
		    	 console.log($scope.selectedBusinessModel);
		    	 $scope.getVersions(item.id);
		    	 $scope.showMe=true;
		     }

	}
	
	$scope.downloadFile = function(item,ev){

					var link = "/restful-services/2.0/businessmodels/"+$scope.selectedBusinessModel.id+"/versions/"+item.id+"/file";
					sbiModule_download.getLink(link);

	}
	//Export Metamodel as a CWM Metamodel
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
	
	 $scope.bmSpeedMenu= [
		                      {
		                    	  label:sbiModule_translate.load("sbi.generic.delete"),
		                    	  icon:'fa fa-trash',
		                    	  //icon:'fa fa-trash-o fa-lg',
		                    	  //color:'#153E7E',
		                    	  action:function(item,event){
		                    		  $scope.deleteItem(item,event);
		                    	  }
		                      	}
		                     ];
	 
	 $scope.bmSpeedMenu2= [
	                       {
	                    	   label:sbiModule_translate.load("sbi.generic.download"),
		                       icon:'fa fa-download',
		                       //color:'#153E7E',
		                       action:function(item,event){
		                    	   $scope.downloadFile(item,event);
		                       }
	                       },
	                       
	                       {
	                    	   label:sbiModule_translate.load("sbi.generic.delete"),
	                    	   icon:'fa fa-trash',
	                    	   //icon:'fa fa-trash-o fa-lg',
	                    	   //color:'#153E7E',
	                    	   action:function(item,event){
		                    		  $scope.deleteItemVersion(item,event);
		                       }
		                   }
	                       
	                       ];
	 
	 
	 //functions that use services
	 //////////////////////////////////////////////////////////////////
	 
	 //calling service for getting Business Models @GET
	 $scope.getBusinessModels = function(){
		 
		 sbiModule_restServices.promiseGet("2.0", 'businessmodels')
			.then(function(response) {
				
				$scope.bmLoadingShow = true;
					$scope.businessModelList = [];
					
					setTimeout(function(){
  					for(var i = 0; i < response.data.length; i++){
  						$scope.businessModelList.push(response.data[i]);
  						$scope.bmLoadingShow = false;
  						$scope.$apply();
		  			}
					},1000);	
			
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			});	
	 }
	 
	 //calling service for getting data sources @GET
	 $scope.getDataSources = function(){
		 
		 sbiModule_restServices.promiseGet("datasources","")
			.then(function(response) {
				$scope.listOfDatasources = response.data.root;
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				
			});	
	 }
	 
	 //Calling service for getting Categories  @GET
	 $scope.getCategories = function(){
		 
		 sbiModule_restServices.promiseGet("domains","listValueDescriptionByType","DOMAIN_TYPE=BM_CATEGORY")
			.then(function(response) {
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
						$scope.bmVersions = response.data;
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
				
				sbiModule_restServices.promisePost("2.0/businessmodels","",$scope.selectedBusinessModel)
				.then(function(response) {
					
					$scope.selectedBusinessModel.id = response.data.id;
					$scope.businessModelList.push(response.data);
					$scope.selectedVersions=[];
					$scope.isDirty = false;			
					$scope.isCWMDirty = false;
					
					if($scope.fileObj.fileName !== undefined)
						$scope.saveBusinessModelFile();
					
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
				}, function(response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
					
				});	
			}	
			else{
				
				sbiModule_restServices.promisePut("2.0/businessmodels", $scope.selectedBusinessModel.id, $scope.selectedBusinessModel)
				.then(function(response) {
					
					if($scope.fileObj.fileName !== undefined)
						$scope.saveBusinessModelFile();
					
					if($scope.bmVersionsActive != null){
						
						sbiModule_restServices.promisePut("2.0/businessmodels/" + $scope.selectedBusinessModel.id+"/versions/"+ $scope.bmVersionsActive,"")
						.then(function(response) {
				
						}, function(response) {
							sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
							
						});	
						}
					
					$scope.businessModelList=[];
					$scope.getBusinessModels();
					$scope.isDirty = false;
					$scope.selectedBusinessModel.modelLocker = response.data.modelLocker;
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');

					
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
					 sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');

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
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');

				}, function(response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
					
				});

			}, function() {
	
			});
		}
		//my util functions
		//////////////////////////////////////////////////
	 
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
				    var toast = $mdToast.simple()
				    .content(msg)
				    .action('OK')
				    .highlightAction(false)
				    .hideDelay(3000)
				    .position('top')
		
				    $mdToast.show(toast).then(
				    		function(response) {
				    			if ( response == 'ok' ) {
				    			}
				    		});
		};
		
		$scope.fileChange = function(){
			$scope.fileClicked = true;  // tells that file input has been clicked
		}
		
		$scope.fileCWMChange = function(){
			$scope.fileCWMClicked = true;  // tells that file input has been clicked
		}
		
		//check if is name dirty 
		$scope.checkChange = function(){

			if($scope.selectedBusinessModel.name === undefined || $scope.selectedBusinessModel.name === "" 
				|| ( $scope.selectedBusinessModel.id === undefined && $scope.fileClicked === false)   // if file is new check also file has been added
			){
					$scope.isDirty = false;
			}
			else{
				$scope.isDirty = true;
			}
			//$scope.isDirty = true;
		}
		
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
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.imported"), 'Success!');
					$scope.bmImportingShow = false;

				}, function(response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
					$scope.bmImportingShow = false;					
				});

			}, function() {
	
			});

		}
		
		
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
					//Upload file
					multipartForm.post("2.0/metadata/"+bmId+"/importCWM",$scope.fileObjCWM).success(

							function(data,status,headers,config){
								if(data.hasOwnProperty("errors")){						
									console.log("[UPLOAD]: DATA HAS ERRORS PROPERTY!");		
									sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.metadata.cwm.error")+":"+data.errors[0].message, 'Error');  

								}else{
									sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.metadata.cwm.success"), 'Success!'); 
									console.log("[UPLOAD]: SUCCESS!");
									$scope.fileObjCWM.fileName = "";
									$scope.fileObjCWM = {};
								}
								$scope.bmCWMImportingShow = false;

							}).error(function(data, status, headers, config) {
								console.log("[UPLOAD]: FAIL!"+status);
								sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.ds.failedToUpload"), 'Error');
								$scope.bmCWMImportingShow = false;
							});
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
			 $scope.bmVersionsActive = item.id;
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
