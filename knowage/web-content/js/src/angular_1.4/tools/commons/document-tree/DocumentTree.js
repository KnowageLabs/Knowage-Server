/**
 * @authors Alessio Conese (alessio.conese@eng.it)
 * 
 */
var scripts = document.getElementsByTagName("script")
var currentScriptPathDocumentTree = "http://localhost:8080/knowage/js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js";

angular.module('document_tree', [ 'ngMaterial', 'ui.tree'])
.directive('documentTree',
		function($compile) {
	return {
		templateUrl: currentScriptPathDocumentTree.substring(0, currentScriptPathDocumentTree.lastIndexOf('/') + 1) + 'template/document-tree.html',
		transclude : true,
			scope: {
				ngModel : '='
				, id : "@"
				, linearToTreeJson:"=?" //if true, the ngModel data will be parsed, if not the JSON is already in correct form
				, clickFunction : "&" //function to call when click into element list
				, selectedItem : "=?" //optional to get the selected  item value
				, showFiles : '=?'
				, multiSelect : "=?"
			},
	    controller: DocumentTreeControllerFunction,
	    controllerAs: 'ctrl',
	    link: function(scope, element, attrs, ctrl, transclude) {
			
	    	var folders = {};
			if (scope.ngModel !== undefined){
				folders = scope.ngModel;
				for (var i = 0 ; i < folders.length; i ++ ){
					folders[i].checked = false;
					folders[i].isOpen = false;
					folders[i].type = "folder";
					for (var j = 0; j < folders[i].biObjects.length ; j++){
						 folders[i].biObjects[j].type = "biObject";
						 folders[i].biObjects[j].checked = false;
					}
				}
			}
			
			if (folders.length > 0 && attrs.lineartotreejson && (attrs.lineartotreejson ==true || attrs.lineartotreejson =="true")){
				//create an hash map for quick search
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
				/*
				var folderOrig = folders;
				var current = [folderOrig[0]]; //root
				while (current.length != 0) {
					var folderCurrent=current.shift();
					folderCurrent.subfolders=[];
					folderCurrent.checked = false;
					for (var i=0;i<folderOrig.length;i++) {
					  var folder=folderOrig[i];
					  if (folder.PARENT_FUNCT_ID == folderCurrent.FUNCT_ID) {
						current.push(folder);
						folderCurrent.subfolders.push(folder);
						folder.parent=folderCurrent; //add link to the parent folder
					  }
					}
				  }*/
			}
			scope.folders=folders;
			scope.ngModel=scope.folders;
			
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
	$scope.toogleSelected = function(element){
		if (element !== undefined && $scope.multiSelect){
			element.checked = !element.checked; 
			//different insertion if is allowed the multi-selection
			if ( element.checked ){ //if the element is just checked, insert into selectedItem, else remove it
					$scope.selectedItem.push(element);
			}else{
				var idx = $scope.selectedItem.indexOf(element);
				$scope.selectedItem.splice(idx, 1);
			}
		
			if (element.type == "folder"){
				for (var i =0 ; i < element.subfolders.length; i++){
					$scope.toogleSelected(element.subfolders[i]);
				}
				for (var j=0; j < element.biObjects.length ; j++ ){
					$scope.toogleSelected(element.biObjects[j]);
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
}
