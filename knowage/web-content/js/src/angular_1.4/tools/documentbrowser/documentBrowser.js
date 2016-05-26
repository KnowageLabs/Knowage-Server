angular.module('documentBrowserModule')
.controller('documentBrowserController', 
		[ '$mdMedia', '$scope', '$http', '$mdSidenav', 
		  '$mdDialog', 'sbiModule_translate', 'sbiModule_restServices', 
		  'sbiModule_config', 'setFocus','$timeout', '$cookies', 
		  'sbiModule_user','$interval','$q',documentBrowserFunction]);

function documentBrowserFunction(
		$mdMedia, $scope, $http, $mdSidenav, 
		$mdDialog, sbiModule_translate, sbiModule_restServices, 
		sbiModule_config, setFocus,$timeout, $cookies,
		sbiModule_user,$interval,$q) {
	
	$scope.translate=sbiModule_translate;
	$scope.folders = [];
	$scope.folderDocuments = [];
	$scope.searchDocuments = [];
	$scope.breadCrumbControl; 
	$scope.breadModel=[];
	$scope.selectedFolder;
	$scope.selectedDocument = undefined;
	$scope.lastDocumentSelected = null;
	$scope.showDocumentDetail = false;
	$scope.orderElements = [{"label":"Type","name":"typeCode"},{"label":"Name","name":"name"},{"label":"Author","name":"creationUser"},{"label":"Date","name":"creationDate"}]
	$scope.selectedOrder = $scope.orderElements[1].name;	
	$scope.showDocumentGridView = ($mdMedia('gt-sm') ? $scope.showDocumentGridView = false : $scope.showDocumentGridView = true);
	$scope.smallScreen = false;
	$scope.hideProgressCircular=true;
	$scope.searchingDocuments=false;
	$scope.lastSearchInputInserted = "";
	$scope.$watch(function() { return !$mdMedia('gt-sm'); }, function(big) {
	    $scope.smallScreen = big;
	  });
//	$scope.setDetailOpen(false);
	
	$scope.moveBreadCrumbToFolder=function(folder,index){
		if(folder!=null){
			$scope.selectedDocument = undefined;
			$scope.showDocumentDetail = false;
			$scope.loadFolderDocuments(folder.id)
		}
	}
	
	$scope.setSelectedFolder = function (folder) {
		if ($scope.selectedFolder==undefined || folder.name !== $scope.selectedFolder.name) {
			$scope.selectedDocument = undefined;
			$scope.showDocumentDetail = false;
			
			$scope.breadCrumbControl.resetBreadCrumb(); 

			var pathObj=[];
			var tmpFolder=angular.extend({},folder); 
			do{
				var tmp=angular.extend({},tmpFolder);
				tmpFolder=tmp.$parent;
				delete tmp.$parent;
				pathObj.push(tmp); 
			}	while(tmp.parentId!=null)

			for(var i=pathObj.length-1;i>=0;i--){
				$scope.breadCrumbControl.insertBread(pathObj[i]);
			}
				
			if(folder!=null){
				$scope.loadFolderDocuments(folder.id)
			}
		
		}		
	};
 
	
	
	
	
	$scope.loadFolderDocuments=function(folderId){
		$scope.hideProgressCircular=false;
		sbiModule_restServices.promiseGet("2.0","documents/getDocumentsByFolder?folderId=" +folderId)
		.then(function(response) {
			angular.copy(response.data,$scope.folderDocuments);
			$scope.hideProgressCircular=true;
			//PUT bread crumb in cookies 
			var foldersId = [];
			for(var i=0; i<$scope.breadModel.length; i++){
				foldersId[i] = $scope.breadModel[i].id;
			}
			$cookies.putObject('breadCrumb_'+sbiModule_user.userId, foldersId);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	$scope.loadFolders=function(){
		sbiModule_restServices.promiseGet("2.0/folders", "")
		.then(function(response) {
			if(response.data && response.data.length>0){
				//check cookies configuration tree
				var cookiesObj = 'breadCrumb_'+sbiModule_user.userId;
				if($cookies.getObject(cookiesObj) && $cookies.getObject(cookiesObj).length>0){
					$scope.hideProgressCircular=false;
					var breadIdx = 0;
					var folderToOpen = {id: 0};
					for(var i=0; i<response.data.length; i++){
						if(breadIdx<$cookies.getObject(cookiesObj).length){
							if(response.data[i].id==$cookies.getObject(cookiesObj)[breadIdx]){
								response.data[i].expanded=true;	 
								$scope.breadCrumbControl.insertBread(response.data[i]);
								breadIdx++;
								folderToOpen=response.data[i];
							}							
						}else{
							break;
						}
					}
					//load folder 
					$timeout(function(){
						$scope.loadFolderDocuments(folderToOpen.id);
					},0,true);
				
				}else{
					response.data[0].expanded=true;					
				}
				response.data[0].name='Root';
			}
			angular.copy(response.data,$scope.folders);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
		
	
	 
	var initLoadFolders= $interval(function() {
        if ($scope.breadCrumbControl.insertBread==undefined) {
          } else {
        	  if (angular.isDefined(initLoadFolders)) {
        		  $interval.cancel(initLoadFolders);
                  initLoadFolders = undefined;
                  $scope.loadFolders();
                }
          }
        }, 500,10); 
	
	  
	$scope.isSelectedFolderValid = function() {
		return $scope.selectedFolder !== null;
	};
	
	$scope.showDocumentDetails = function() {
		return $scope.showDocumentDetail && $scope.isSelectedDocumentValid();
	};
	
	$scope.isSelectedDocumentValid = function() {
		return $scope.selectedDocument !== undefined;
	};
 
	 
	$scope.redirectIframe = function(url){
		document.location.replace(url);
	}
	
	
	
	$scope.setDetailOpen = function(isOpen) {
		if (isOpen && !$mdSidenav('right').isLockedOpen() && !$mdSidenav('right').isOpen()) {
			$scope.toggleDocumentDetail();
		}

		$scope.showDocumentDetail = isOpen;
	};

	
	$scope.selectDocument= function ( document ) { 
		if (document !== undefined) {
			$scope.lastDocumentSelected = document;
		}
		var alreadySelected = (document !== undefined && $scope.selectedDocument === document);
		$scope.selectedDocument = document;
		if (alreadySelected) {
			$scope.selectedDocument=undefined;
			$scope.setDetailOpen(!$scope.showDocumentDetail);
		} else {
			$scope.setDetailOpen(document !== undefined);
		}
	};
	
	$scope.executeDocument = function(document) {
		
		var params = {};
		
		var url = sbiModule_config.contextName 
			+ '/servlet/AdapterHTTP?ACTION_NAME=EXECUTE_DOCUMENT_ANGULAR_ACTION&SBI_ENVIRONMENT=DOCBROWSER'
			+ '&OBJECT_ID=' + document.id
			+ '&OBJECT_LABEL=' + document.label
			+ '&IS_SOURCE_DOCUMENT=true'
			+ '&LIGHT_NAVIGATOR_DISABLED=TRUE'
			+ '&SBI_EXECUTION_ID=null'
			+ '&OBJECT_NAME=' + document.name
			;
		
		var tmpDoc={};
		angular.copy(document,tmpDoc);
		tmpDoc.url=url;
		$scope.runningDocuments.push(tmpDoc);
		 
	};

	$scope.wasSelected = function(document) {
		return $scope.selectedDocument === document;
	};
	 
	$scope.setSearchInput = function (newSearchInput) {
		
		$scope.lastSearchInputInserted = newSearchInput;
		$scope.searchInput = newSearchInput;
		setFocus("searchInput");

		$timeout(function(){
			if (newSearchInput == $scope.searchInput) {
				if (newSearchInput.length > 0){
					$scope.searchingDocuments=true;
					sbiModule_restServices.promiseGet("2.0/documents", "searchDocument?attributes=all&value=" + newSearchInput + "*", null)
					.then(function(response) {
						$scope.searchDocuments = response.data;
						$scope.searchingDocuments=false;
					},function(response){
						sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.document.search.error'))
						.finally(function(){
							$scope.searchDocuments = [];
						});
						$scope.searchingDocuments=false;
					});
				}else{
					$scope.searchDocuments = [];
					$scope.searchingDocuments=false;
				}
			}
		}, 400);
	}

	 
	$scope.toggleDocumentView = function() {
		$scope.showDocumentGridView = !$scope.showDocumentGridView;
	};

	$scope.toggleFolders = function() {
		$mdSidenav('left').toggle();
	};

	$scope.toggleDocumentDetail = function() {
		$mdSidenav('right').toggle();
	};
	
	$scope.toggleSearchView = function() {
		$scope.showSearchView = !$scope.showSearchView;
		if ($scope.showSearchView) {
			setFocus('searchInput');
		}
		$scope.selectDocument();
	};
	
	 
	$scope.setFocus = function(elementName) {
		setFocus(elementName);
	};
 
	$scope.alert = function(message) {
		alert(message);
	};
	
	$scope.editDocument=function(document){
		var deferred = $q.defer();
		 $mdDialog.show({
 		      controller: DialogEditDocumentController,
 		      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/documentbrowser/template/documentDialogIframeTemplate.jsp',  
 		      clickOutsideToClose:false,
 		      escapeToClose :false,
 		      fullscreen: true,
 		      locals:{document:document, folderDocument : $scope.folderDocuments , searchDocuments:$scope.searchDocuments}
		 
		 })
		 .then(function(answer) {
				$scope.status = 'You said the information was "' + answer + '".';
				$scope.reloadAll();
				return ;
		}, function() {
				$scope.status = 'You cancelled the dialog.';
				$scope.reloadAll();
		});
	};
	
	$scope.reloadAll = function(){
		$scope.showDocumentDetail = false;
		$scope.loadFolderDocuments($scope.selectedFolder.id);
		$scope.setSearchInput($scope.lastSearchInputInserted);
	}
	$scope.newDocument=function(type){
		$mdDialog.show({
			controller: DialogNewDocumentController,
			templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/documentbrowser/template/documentDialogIframeTemplate.jsp',  
			clickOutsideToClose:false,
			escapeToClose :false,
			fullscreen: true,
			locals:{
				selectedFolder: $scope.selectedFolder,
				typeDocument:type}
		}) .then(function() { 
			if($scope.selectedFolder!=undefined){
				$scope.loadFolderDocuments($scope.selectedFolder.id);
			}
	    } );
	};
	
	$scope.deleteDocument = function(Document){
		
		var confirm = $mdDialog.confirm()
		.title($scope.translate.load("sbi.browser.document.delete.ask.title"))
		.content($scope.translate.load("sbi.browser.document.delete.ask"))
		.ariaLabel('delete Document') 
		.ok($scope.translate.load("sbi.general.yes"))
		.cancel($scope.translate.load("sbi.general.No"));
			$mdDialog.show(confirm).then(function() {
			var index = $scope.folderDocuments.indexOf(Document);
			var index2= $scope.searchDocuments.indexOf(Document);
			sbiModule_restServices.promiseDelete("1.0/documents", Document.label)
			.then(function(response) {
				if(index!=-1){
					$scope.folderDocuments.splice(index,1);
				}
				if(index2!=-1){
					$scope.searchDocuments.splice(index2,1);
				}
			$scope.selectedDocument = undefined;
			},function(response) {
				sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.document.delete.error'));
			});
		});
	
	}
	
	
	$scope.cloneDocument = function(Document){
		var confirm = $mdDialog.confirm()
		.title($scope.translate.load("sbi.browser.document.clone.ask.title"))
		.content($scope.translate.load("sbi.browser.document.clone.ask"))
		.ariaLabel('delete Document') 
		.ok($scope.translate.load("sbi.general.yes"))
		.cancel($scope.translate.load("sbi.general.No"));
			$mdDialog.show(confirm).then(function() {

			//var index = $scope.folderDocuments.indexOf(Document);
			sbiModule_restServices.promisePost("documents","clone?docId="+Document.id)
			.then(function(response) {
			$scope.folderDocuments.push(response.data);
			$scope.searchDocuments.push(response.data);
			},function(response) {
				sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.document.clone.error'));
			});
		});
	}
	
	
	
	$scope.documentTableButton=[{
		label : sbiModule_translate.load('sbi.generic.run'),
		icon:'fa fa-play-circle' ,
		backgroundColor:'transparent',	
		action : function(item,event) {
			$scope.executeDocument(item);
		}

	} ];
	 
	
};

app.filter("asDate", function () {
    return function (input) {
        return new Date(input);
    }
});


app.filter("translateLoad", function (sbiModule_translate) {
    return function (input) {
    	if(input!=undefined){
    		return sbiModule_translate.load(input);    		
    	}else{
    		return '';
    	}
    }
});

app.directive('focusOn', function() {
	return function(scope, elem, attr) {
		scope.$on('focusOn', function(e, name) {
			if (name === attr.focusOn) {
				elem[0].focus();
			}
		});
	};
});





app.factory('setFocus', function($rootScope, $timeout) {
	return function(name) {
		$timeout(function() {
			$rootScope.$broadcast('focusOn', name);
		});
	}
});


function DialogEditDocumentController($scope,$mdDialog,sbiModule_config,document,folderDocument,searchDocuments){
	$scope.closeDialogFromExt=function(){
		 $mdDialog.cancel();
		 //reload documents 
		 
	}
	$scope.iframeUrl=sbiModule_config.contextName+"/servlet/AdapterHTTP?PAGE=DetailBIObjectPage&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=FALSE&MESSAGEDET=DETAIL_SELECT&OBJECT_ID="+document.id;
}

function DialogNewDocumentController($scope,$mdDialog,$mdBottomSheet,sbiModule_config,selectedFolder,typeDocument,sbiModule_config,sbiModule_user,sbiModule_translate){

	var folderId= selectedFolder==undefined? "" : "&FUNCT_ID="+selectedFolder.id;
	$scope.iframeUrl=sbiModule_config.contextName+"/servlet/AdapterHTTP?PAGE=DetailBIObjectPage&SBI_ENVIRONMENT=DOCBROWSER&LIGHT_NAVIGATOR_DISABLED=FALSE&MESSAGEDET=DETAIL_NEW"+folderId;

	 if(typeDocument=="cockpit"){
			$scope.iframeUrl= sbiModule_config.engineUrls.cockpitServiceUrl +  '&SBI_ENVIRONMENT=DOCBROWSER&IS_TECHNICAL_USER=' + sbiModule_user.isTechnicalUser + "&documentMode=EDIT";
		}
	
	$scope.closeDialogFromExt=function(reloadFolder){
		if(reloadFolder){
			$mdDialog.hide();
		}else{
			$mdDialog.cancel();
		}
	}
	
		$scope.closeConfirm=function(confirm,reloadFolder){
			
				if(confirm){
					$mdBottomSheet.show({
					      template: '<md-bottom-sheet layout="column">'+
					    	  '<p><b>{{translate.load("sbi.browser.close")}}</b></p>'+
					    	  '<span>{{translate.load("sbi.browser.close.confirm")}}</span>'+
					    	  '<div layout="row">'+
					    	  '	<md-button ng-click=" confirm(false)" class="md-raised" flex>{{translate.load("sbi.general.cancel")}}</md-button>'+
					    	  '	<md-button ng-click=" confirm(true)" class="md-raised" flex>{{translate.load("sbi.general.continue")}}</md-button>'+
					    	  '</div>'+
					    	  '</md-bottom-sheet>',
					       clickOutsideToClose: false,
					       escapeToClose :false,
					       parent:  angular.element(document.querySelector(".dialogFrameContent")),
					       disableParentScroll:true,
					       controller:function($scope,$mdBottomSheet,sbiModule_translate){
					    		$scope.translate=sbiModule_translate;
					    	   $scope.confirm=function(resp){ 
					    		   $mdBottomSheet.hide(resp)
					    	   }
					       }
					    }).then(function(close) {
					    	if(close){
					    		 $scope.closeDialogFromExt(reloadFolder);
					    		 
					    	}
					    });
					
				}else{
					$scope.closeDialogFromExt(reloadFolder);
				}
	}
		
}

//directive for Enter keypress
angular.module('documentBrowserModule').directive('keyEnter', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if(event.which === 13) {
                scope.$apply(function (){
                    scope.$eval(attrs.keyEnter);
                });

                event.preventDefault();
            }
        });
    };
});
