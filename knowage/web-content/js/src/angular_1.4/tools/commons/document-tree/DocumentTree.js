/**
 * @authors Alessio Conese (alessio.conese@eng.it)
 * 
 */
var scripts = document.getElementsByTagName("script")
var currentScriptPathDocumentTree = scripts[scripts.length-1].src;

angular.module('document_tree', [ 'ngMaterial', 'ui.tree'])
.directive('documentTree',
		function($compile) {
	return {
		templateUrl: currentScriptPathDocumentTree.substring(0, currentScriptPathDocumentTree.lastIndexOf('/') + 1) + 'template/document-tree.html',
		transclude : true,
			scope: {
				ngModel : '='
				, id : "@"
				, createTree:"=?" //if true, the ngModel data will be parsed, if not the JSON is already in correct form
				, clickFunction : "&" //function to call when click into element list
				, selectedItem : "=?" //optional to get the selected  item value
				, showFiles : '=?'
				, multiSelect : "=?"
			},
	    controller: DocumentTreeControllerFunction,
	    controllerAs: 'ctrl',
	    link: function(scope, element, attrs, ctrl, transclude) {
	    	
	    	scope.createTreeStructure = function (folders){
	    		if (attrs.createTree !==undefined  && (attrs.createTree ==true || attrs.createTree =="true")){
		    		if (folders !== undefined && folders.length > 0 && folders[0].subfolders === undefined){
			    		var mapFolder = {};	
						
						for (var i = 0 ; i < folders.length; i ++ ){
							folders[i].subfolders = [];
							mapFolder[folders[i].id] = folders[i]; 
						}
						
						var treeFolders = [];
						for (var i = 0 ; i < folders.length; i ++ ){
							//if folder has not father, is a root folder
							if (folders[i].parentId == null || folders[i].parentId == "null"){
								treeFolders.push(folders[i]);
							}
							else{
								//search parent folder with hasmap and attach the son
								mapFolder[folders[i].parentId].subfolders.push(folders[i]);
							}
							//update linear structure with tree structure
						}
						folders=treeFolders; 
		    		}
	    		}
	    		return folders;
	    	}
	    	
	    	scope.initializeFolders = function (folders){
	    		for (var i = 0 ; i < folders.length; i ++ ){
					folders[i].checked = false;
					folders[i].isOpen = false;
					folders[i].type = "folder";
					for (var j = 0; folders[i].biObjects !==undefined && j < folders[i].biObjects.length ; j++){
						 folders[i].biObjects[j].type = "biObject";
						 folders[i].biObjects[j].checked = false;
					}
				}
	    	}

	    	scope.initializeFolders(scope.ngModel);
	    	scope.ngModel = scope.createTreeStructure(scope.ngModel);
			scope.folders=scope.ngModel;
	    	
			var id="dcTree";
			if(attrs.id){
				id=attrs.id;
			}
			
			if(attrs.multiselect && (attrs.multiselect == true || attrs.multiselect == "true") ){
				if (!attrs.selecteditem) {
					scope.selectedItem = [];
				}
			}
		}
	}
});


function DocumentTreeControllerFunction($scope,$timeout){
	$scope.toogleSelected = function(element, parent){
		if (element !== undefined && $scope.multiSelect){
			//check the element as the parent. If not the parent doesn't exist, toggle the element check
			element.checked = parent === undefined ? !element.checked : parent.checked; 
			//different insertion if is allowed the multi-selection
			if ( element.checked ){ //if the element is just checked, insert into selectedItem, else remove it
					$scope.selectedItem.push(element);
			}else{
				var idx = $scope.selectedItem.indexOf(element);
				$scope.selectedItem.splice(idx, 1);
			}
		
			if (element.type == "folder"){
				for (var i =0 ; i < element.subfolders.length; i++){
					$scope.toogleSelected(element.subfolders[i],element);
				}
				for (var j=0; element.biObjects !==undefined && j < element.biObjects.length ; j++ ){
					$scope.toogleSelected(element.biObjects[j],element);
				}
			}
		}
	}
	
	$scope.openFolder = function (folder){
		folder.isOpen = !folder.isOpen;
		$scope.setSelected(folder);
	}
	
	$scope.setSelected = function (element){
		if (!$scope.multiSelect){
			$scope.selectedItem=element;
		}
		//if present a click function, use it
		if (typeof $scope.clickFunction == "function"){
			$scope.clickFunction({item : element});
		}
	}
	
	$scope.$watchCollection( 
			'ngModel'
    	, function(){
			$scope.initializeFolders($scope.ngModel);
			$scope.ngModel = $scope.createTreeStructure($scope.ngModel);
			$scope.folders= $scope.ngModel;
    	});

	$scope.detectBrowser = function(){
        var userAgent = window.navigator.userAgent;
        var browsers = {chrome: /chrome/i, safari: /safari/i, firefox: /firefox/i, ie: /internet explorer/i};

        for(var key in browsers) {
            if (browsers[key].test(userAgent)) {
            	return key;
            }
       };
       return 'unknown';
	}
	
	$scope.browser = $scope.detectBrowser();
}
