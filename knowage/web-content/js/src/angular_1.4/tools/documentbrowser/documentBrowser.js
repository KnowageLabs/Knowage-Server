angular.module('documentBrowserModule').controller( 'documentBrowserController', ['$scope', '$http', '$mdSidenav', '$mdDialog', 'sbiModule_translate', 'sbiModule_restServices', 'sbiModule_config', 'setFocus',documentBrowserFunction]);

function documentBrowserFunction($scope, $http, $mdSidenav, $mdDialog, sbiModule_translate, sbiModule_restServices, sbiModule_config, setFocus){
	$scope.translate=sbiModule_translate;
	$scope.folders = [];
	$scope.folderDocuments = [];
	$scope.searchDocuments = [];
	$scope.breadCrumbControl;
	$scope.folderBread=[]
	$scope.selectedFolder;
	$scope.selectedDocument = undefined;
	$scope.lastDocumentSelected = null;
	$scope.showDocumentDetail = false;
	$scope.showDocumentGridView = false;
	
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
		sbiModule_restServices.promiseGet("2.0","documents?folderId=" +folderId)
		.then(function(response) {
			angular.copy(response.data,$scope.folderDocuments);
		},function(response){
			alert(respone.data)
		});
	}
	$scope.loadFolders=function(){
		sbiModule_restServices.promiseGet("2.0/folders", "")
		.then(function(response) {
			angular.copy(response.data,$scope.folders);
		},function(response){
			alert(respone.data)
		});
	}
	$scope.loadFolders();
	  
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
		if (document !== null) {
			$scope.lastDocumentSelected = document;
		}
		var alreadySelected = (document !== null && $scope.selectedDocument === document);
		$scope.selectedDocument = document;
		if (alreadySelected) {
			$scope.selectedDocument=undefined;
			$scope.setDetailOpen(!$scope.showDocumentDetail);
		} else {
			$scope.setDetailOpen(document !== null);
		}
	};
	
	$scope.executeDocument = function(document) {
		console.log('document -> ', document);
		
		var params = {};
		
		var url = sbiModule_config.contextName 
			+ '/servlet/AdapterHTTP?ACTION_NAME=EXECUTE_DOCUMENT_ANGULAR_ACTION&SBI_ENVIRONMENT=DOCBROWSER'
			+ '&OBJECT_ID=' + document.id
			+ '&OBJECT_LABEL=' + document.label
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
		$scope.searchInput = newSearchInput;
		setFocus("searchInput");
		if (newSearchInput.length > 0) {
			sbiModule_restServices.get("2.0/documents", "searchDocument?attributes=all&value=" + newSearchInput + "*", null)
			.success(function(data) {
				$scope.searchDocuments = data;
			});
		} else {
			$scope.searchDocuments = [];
		}
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
		$scope.selectDocument(null);
	};
	
	 
	$scope.setFocus = function(elementName) {
		setFocus(elementName);
	};
 
	$scope.alert = function(message) {
		alert(message);
	};
	
	
	$scope.deleteRelativeDoc = function(Document){
		
		var confirm = $mdDialog.confirm()
		.title($scope.translate.load("sbi.browser.document.delete.ask.title"))
		.content($scope.translate.load("sbi.browser.document.delete.ask"))
		.ariaLabel('delete Document') 
		.ok($scope.translate.load("sbi.general.yes"))
		.cancel($scope.translate.load("sbi.general.No"));
			$mdDialog.show(confirm).then(function() {

			var index = $scope.folderDocuments.indexOf(Document);
			sbiModule_restServices.promiseDelete("1.0/documents", Document.label)
			.then(function(response) {
			$scope.folderDocuments.splice(index,1);
			},function(response) {
				console.log("DELETE FAILLLLLLLLLLLLLLL");
			});



		}, function() {
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
				console.log(response.data);
			$scope.folderDocuments.push(response.data);
			},function(response) {
				console.log("Clone FAILLLLLLLLLLLLLLL");
			});



		}, function() {
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
	
	$scope.getImageUrl=function(document){
		switch(document.typeCode){
		case "CHART" : return "/knowage/themes/sbi_default/img/analiticalmodel/browser/document_locatiointelligence.png";
		}
	}
	
};

 

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

var assert = function(condition, message) {
	if (!condition) {
		message = message || "Assertion failed";
		if (typeof Error !== "undefined") {
			throw new Error(message);
		}
		throw message; // Fallback
	}
};

var reverseInPlace = function (array) {
	assert(array != null);
	for (var i=0;i<Math.floor(array.length/2);i++) {
		var tmp=array[i];
		array[i]=array[array.length-1-i];
		array[array.length-1-i]=tmp;
	}
};

var getFolderById = function(folderId, folders) {
	for (var i = 0; i < folders.length; i++) {
		var folder = folders[i];
		if (folder.id === folderId) {
			return folder;
		}
		var subFolder = getFolderById(folderId, folder.subfolders);
		if(subFolder !== null) {
			return subFolder; 
		}
	}
	return null;
}


var stringStartsWith=function (s, prefix) {
	return s.toLowerCase().slice(0, prefix.length) == prefix.toLowerCase();
};