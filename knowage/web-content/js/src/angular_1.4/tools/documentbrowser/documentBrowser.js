'use strict';

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

var getFolderAncestors = function (folder, folders) {
	if (folder==null) {
		return []; //avoid error when invisible
	}
	var res = [folder]; //with folder himself
	while (folder.parentId !== null) {
		folder = getFolderById(folder.parentId, folders);
		if (folder !== null) {
			res.push(folder);
		}
	}
	// res.pop();
	//no called by the root
	assert(res.length != 0);
	reverseInPlace(res);
	return res;
};

var stringStartsWith=function (s, prefix) {
	return s.toLowerCase().slice(0, prefix.length) == prefix.toLowerCase();
};

var app = angular.module('documentBrowserModule', ['md.data.table', 'ngMaterial', 'ui.tree', 'sbiModule', 'document_tree']);

app.controller( 'documentBrowserController', ['$scope', '$http', '$mdSidenav', 'sbiModule_translate', 'sbiModule_restServices', 'sbiModule_config', 'setFocus', 
                                              function($scope, $http, $mdSidenav, sbiModule_translate, sbiModule_restServices, sbiModule_config, setFocus) {
	
	$scope.folders = [];
	
	sbiModule_restServices.get("2.0/folders", "", null).success(function(data) {
		$scope.folders = data;
	});
	
	$scope.folderDocuments = [];
	$scope.searchDocuments = [];
	$scope.getFolderAncestors = getFolderAncestors;

	//Folder selection
	
	$scope.selectedFolder = null;
	
	var lastSelectedFolder = null;

	$scope.setSelectedFolder = function (folder) {
		if (folder !== $scope.selectedFolder) {
			
			if ($scope.selectedFolder !== null){
				$scope.selectedFolder.selected = false; // disable selection on previous folder
			}
			
			$scope.selectedFolder = folder;
			$scope.selectedDocument = null;
			$scope.showDocumentDetail = false;
			
			if (folder !== null) {
				$scope.showSearchView = false;
				lastSelectedFolder = folder;
				folder.selected = true;
				folder.showSubfolders = (folder.showSubfolders ? false : true);
				
				sbiModule_restServices
				.get("2.0/documents", "?folderId=" + folder.id, null)
				.success(function(data) {
					$scope.folderDocuments = data;
				});
			}
		}
	};
	
	$scope.isSelectedFolderValid = function() {
		return $scope.selectedFolder !== null;
	};
	
	$scope.showDocumentDetails = function() {
		return $scope.showDocumentDetail && $scope.isSelectedDocumentValid();
	};
	
	$scope.isSelectedDocumentValid = function() {
		return $scope.selectedDocument !== null;
	};

	// Document selection and sorting
	
	$scope.selectedDocument = null;
	$scope.lastDocumentSelected = null;
	$scope.showDocumentDetail = false;
	
	$scope.redirectIframe = function(url){
		document.location.replace(url);
	}
	
	$scope.setDetailOpen = function(isOpen) {
		if (isOpen && !$mdSidenav('right').isLockedOpen() && !$mdSidenav('right').isOpen()) {
			$scope.toggleDocumentDetail();
		}

		$scope.showDocumentDetail = isOpen;
	};
	
	$scope.selectDocument = function ( document ) { 
		if (document !== null) {
			$scope.lastDocumentSelected = document;
		}
		var alreadySelected = (document !== null && $scope.selectedDocument === document);
		$scope.selectedDocument = document;
		if (alreadySelected) {
			$scope.setDetailOpen(!$scope.showDocumentDetail);
		} else {
			$scope.setDetailOpen(document !== null);
		}
	};
	
	$scope.executeDocument = function(document) {
		console.log('document -> ', document);
		
		var params = {};
		
		var url = sbiModule_config.contextName 
			+ '/servlet/AdapterHTTP?ACTION_NAME=EXECUTE_DOCUMENT_ACTION&SBI_ENVIRONMENT=DOCBROWSER'
			+ '&OBJECT_ID=' + document.id
			+ '&OBJECT_LABEL=' + document.label
			+ '&LIGHT_NAVIGATOR_DISABLED=TRUE'
			+ '&SBI_EXECUTION_ID=null'
			;
		
		$scope.redirectIframe(url);
	};

	$scope.wasSelected = function(document) {
		return $scope.selectedDocument === document;
	};
	
	$scope.setDetailOpen(false);
	
	$scope.documentsOrderProperty="BIOBJ_TYPE_CD";

	//Search
	
	$scope.showSearchView = false;
	$scope.searchInput = "";
	$scope.isSearchInputFocused = false;
	
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
		
	// GUI

	$scope.showDocumentGridView = false;

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
	
	$scope.showSearchResultHeader = function() {
		return $scope.showSearchView && $scope.searchInput.length>0;
	};
	
	$scope.showDefaultHeader = function() {
		return (!$scope.showSearchView && $scope.folderDocuments.length==0)
				|| ($scope.showSearchView && $scope.searchInput.length==0 );
	};
	
	$scope.showBreadcrumbHeader = function() {
		return !$scope.showSearchView && $scope.isSelectedFolderValid();
	};
	
	$scope.setFocus = function(elementName) {
		setFocus(elementName);
	};
	
	// Utility
	
	$scope.alert = function(message) {
		alert(message);
	};
	
}]);

app.filter('limitEllipses', function() {
	return function(s, max) {
		if (s == null) {
			return null;
		}
		if (s.length > max) {
			s = s.substring(0, Math.max(1, max - 3)) + "...";
		}
		return s;
	};
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