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

var getFolderAncestors = function (folder) {
	if (folder==null) {
		return []; //avoid error when invisible
	}
	var res = [folder]; //with folder himself
	while (folder.parent != null) {
		folder=folder.parent;
		res.push(folder);
	}
	res.pop();
	//no called by the root
	assert(res.length != 0);
	reverseInPlace(res);
	return res;
};

var getBreadCrumbs = function (folder) {
	assert(folder != null);
	var ancestors = getFolderAncestors(folder);
	assert(ancestors!=null && ancestors.length!=0); //at least the folder
	var res ="";
	//from 1, remove the 'Functionalities' root
	for (var i = 0; i < ancestors.length; i++) {
		res+=" > "+ancestors[i].NAME;
	};
	return res.substring(3);
};

var stringStartsWith=function (s, prefix) {
	return s.toLowerCase().slice(0, prefix.length) == prefix.toLowerCase();
};

var app = angular.module('documentBrowserModule', ['md.data.table','ngMaterial','ui.tree','sbiModule','document_tree']);

app.controller( 'documentBrowserController', ['$scope', '$mdSidenav', 'sbiModule_translate', 'sbiModule_restServices', function($scope, $mdSidenav, sbiModule_translate, sbiModule_restServices) {
	
	$scope.folders = [];
	
	sbiModule_restServices.get("2.0/folders", "", null).success(function(data) {
		$scope.folders = data;
	});
	
	$scope.folderDocuments = [];
	$scope.searchDocuments = [];
	$scope.getBreadCrumbs = getBreadCrumbs;
	$scope.getFolderAncestors = getFolderAncestors;

	//Folder selection
	
	$scope.selectedFolder = null;
	
	var lastSelectedFolder = null;

	$scope.setSelectedFolder = function (folder) {
		if (folder !== $scope.selectedFolder) {
			
			if ($scope.selectedFolder != null){
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
				
				sbiModule_restServices.get("2.0/documents", "?folderId=" + folder.id, null)
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
	
	$scope.setDetailOpen = function(isOpen) {
		var detailSidenav = $mdSidenav('right');
		if (isOpen && !detailSidenav.isLockedOpen() && !detailSidenav.isOpen()) {
			$scope.toggleDocumentDetail();
		}

		$scope.showDocumentDetail = isOpen;
	};
	
	$scope.selectDocument = function ( document ) { 
		if (document !== null) {
			$scope.lastDocumentSelected = document;
		}
		var alreadySelected = document !== null && $scope.selectedDocument === document;
		$scope.selectedDocument = document;
		if (alreadySelected) {
			$scope.setDetailOpen(!$scope.showDocumentDetail);
		} else {
			$scope.setDetailOpen(document !== null);
		}
	};

	$scope.wasSelected = function(document) {
		return $scope.selectedDocument === document;
	};
	
	//check if the document selected is still in documents shown
	var checkDocumentSelected = function() {
		for (i=0; i<$scope.documents.length; i++) {
			if ($scope.documents[i] === $scope.selectedDocument) {
				return; // the document is already shown
			}
		}
		$scope.selectDocument(null); // the document is not shown
	};
	
	$scope.setDetailOpen(false);
	
	$scope.documentsOrderProperty="BIOBJ_TYPE_CD";

	//Search
	
	$scope.showSearchView = false;
	
	$scope.previousSearchInput = "";
	$scope.previousSearchDocumentsResult = [];
	//properties to show
	var properties=["BIOBJ_TYPE_CD","NAME","CREATION_DATE","CREATION_USER"];
	
	$scope.setSearchInput = function (newSearchInput) {
		if (newSearchInput.length > 0) {
			sbiModule_restServices.get("2.0/documents", "searchDocument?attributes=all&value=" + newSearchInput, null)
			.success(function(data) {
				$scope.searchDocuments = data;
			});
		}
	}
	
	$scope.setSearchInputZ = function (newSearchInput) {
		if (newSearchInput.length == 0) {
			$scope.showSearchView = false;
			$scope.setSelectedFolder(lastSelectedFolder);
			$scope.selectDocument($scope.lastDocumentSelected);
			if (lastSelectedFolder === null) {
				//no folder selected before
				$scope.documents = [];
				checkDocumentSelected();
			}
		} else {
			$scope.showSearchView = true;
			$scope.setSelectedFolder(null);
			
			var documentsOnResearch;
			
			if ($scope.previousSearchInput.length !== 0 && stringStartsWith(newSearchInput,$scope.previousSearchInput)) {
				documentsOnResearch = $scope.previousSearchDocumentsResult;
			}
			
			sbiModule_restServices.get("2.0/documents", "searchDocument?attributes=all&value=" + newSearchInput, null)
			.success(function(data) {
				documentsOnResearch = data;
			});
	
			$scope.searchDocumentsResult = [];
			for (var i=0;i<documentsOnResearch.length;i++) {
				var doc = documentsOnResearch[i];
				for (var k=0;k<properties.length;k++) {
					var tokens=doc[properties[k]].split(" ");
					var found = false;
					for (var j=0;j<tokens.length;j++) {
						if (stringStartsWith(tokens[j],newSearchInput)) {
							$scope.searchDocumentsResult.push(doc);
							found =true;
							break;
						}
					}
					if (found) {
						break;
					}
				}
			}
			
			$scope.previousSearchInput = newSearchInput;
			$scope.previousSearchDocumentsResult = $scope.searchDocumentsResult;
			$scope.documents = $scope.searchDocumentsResult;
			checkDocumentSelected();
		}
	};
	
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
	
	$scope.showSearchResultHeader = function() {
		return $scope.showSearchView && $scope.searchInput.length>0;
	};
	
	$scope.showDefaultHeader = function() {
		return (!$scope.showSearchView && $scope.folderDocuments.length==0)
				|| ($scope.showSearchView && $scope.searchInput.length==0);
	};
	
	$scope.showBreadcrumbHeader = function() {
		return !$scope.showSearchView && $scope.isSelectedFolderValid();
	};
	
	// Utility
	
	$scope.alert = function(message) {
		alert(message);
	};
	
}]);

app.filter('limitEllipses', function() {
	return function(s,max) {
		if (s==null) {
			return null;
		}
		if (s.length>max) {
			s=s.substring(0, Math.max(1, max - 3)) + "...";
		}
		return s;
	};
});