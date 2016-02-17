var app = angular.module('importExportMenuModule', 
		['ngMaterial', 'sbiModule', 'angular_table',
		 'document_tree', 'componentTreeModule', 'file_upload', 'bread_crumb']);

app.factory('importExportMenuModule_importConf', function() {
	return {
		fileImport : {},
		associationsFileImport : {},
		associations : 'noAssociations',
		fileAssociation : '',
		roles : {
			currentRoles : [],
			exportedRoles : [],
			associationsRole : {}
		}
	};
});
app.config(['$mdThemingProvider', function($mdThemingProvider) {

    $mdThemingProvider.theme('knowage')

$mdThemingProvider.setDefaultTheme('knowage');
}]);
app.controller('importExportMenuController', 
		['sbiModule_download',
		 'sbiModule_device',
		 '$scope',
		 '$mdDialog',
		 '$timeout',
		 'sbiModule_logger',
		 'sbiModule_translate',
		 'sbiModule_restServices',
		 'sbiModule_config',
		 '$mdToast',
		 impExpFuncController]);

function impExpFuncController(sbiModule_download,sbiModule_device,$scope,$mdDialog,	$timeout,sbiModule_logger,sbiModule_translate,sbiModule_restServices,sbiModule_config,$mdToast) {
	$scope.importFile = {};
	sbiModule_translate.addMessageFile('component_impexp_messages');
	$scope.translate = sbiModule_translate;
	$scope.menu=[];
	$scope.currentMenu=[];
	$scope.currentRoles=[];
	$scope.exportedRoles=[];
	$scope.tree=[];
	$scope.treeCopy=[];
	$scope.treeInTheDB=[];
	

	$scope.upload = function(ev){
		if($scope.importFile.fileName == "" || $scope.importFile.fileName == undefined){
			$scope.showAction(sbiModule_translate.load("sbi.impexpusers.missinguploadfile"));
		}else{
			var fd = new FormData();
		
			fd.append('exportedArchive', $scope.importFile.file);
			sbiModule_restServices.post("1.0/serverManager/importExport/menu", 'import', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}})
			.success(function(data, status, headers, config) {
				
				if(data.STATUS=="NON OK"){
					$mdToast.show($mdToast.simple().content("data.ERROR").position('top').action(
					'OK').highlightAction(false).hideDelay(5000));
				}
				else if(data.STATUS=="OK"){
					//check role missing
					
					//clean the vector 
					$scope.menu=[];
					$scope.currentMenu=[];
					$scope.currentRoles=[];
					$scope.exportedRoles=[];
					$scope.tree=[];
					$scope.treeCopy=[];
					$scope.treeInTheDB=[];
					//get response
					$scope.currentRoles=data.currentRoles;
					$scope.exportedRoles=data.exportedRoles;
					$scope.currentObjects = data.currentObjects;
					$scope.menu = data.menu;
					if($scope.checkRole() && $scope.checkObjects()){
						//if role is not present stop the import.
						$scope.currentMenu = data.currentMenu;
						$scope.parseToTree($scope.menu,$scope.tree);
						$scope.parseToTree($scope.currentMenu,$scope.treeInTheDB);
						$scope.treeCopy=$scope.treeInTheDB;
					
					}
					
				}
				
				
			})
			.error(function(data, status, headers, config) {
				$mdToast.show($mdToast.simple().content("errore").position('top').action(
				'OK').highlightAction(false).hideDelay(5000));
				
			});
		}
	}

	
	$scope.parseToTree = function(arr, dest){
		for(var i=0;i<arr.length;i++){
			if(arr[i].parentId==false || arr[i].parentId==null ){
				arr[i]["children"]=[];
				dest.push(arr[i]);
			}
		}
		
		for(var i=0;i<arr.length;i++){
			if(arr[i].parentId==false ||arr[i].parentId==null  ){
				//it is  a father, no action
			}else{
				//it is a child 
				for(var j=0;j<dest.length;j++){
					if(dest[j].menuId==arr[i].parentId){
						//arr[i].parentId=dest[j].name;
						arr[i]["parentName"]=dest[j].name;
						dest[j]["children"].push(arr[i]); 
						
					}
				}
			}
		}
	}
	$scope.checkObjects = function(){
		for(var i=0;i<$scope.menu.length;i++){
			var index = $scope.indexObjectsInList($scope.menu[i],$scope.currentObjects );
			if(index==-1){
				$scope.showAction("missing objects");
				return false;
				
			}
		}
		return true;
	}
	$scope.checkRole = function(){
		
		for(var i=0;i<$scope.exportedRoles.length;i++){
			var index = $scope.indexInList($scope.exportedRoles[i],$scope.currentRoles );
			if(index==-1){
				var text = sbiModule_translate.load("sbi.importusers.importfailed");
				$scope.showAction(text);
				return false;
				
			}
		}
		//all roles present
		return true;
		
	}
	$scope.save = function(ev){
		if($scope.typeSaveMenu == 'Missing'){
			//missing user
			$scope.removeCircularDependences($scope.tree);
			
			
			sbiModule_restServices.post("1.0/serverManager/importExport/menu","addmissingmenu",$scope.tree)
			.success(function(data, status, headers, config) {
				if(data.STATUS=="OK"){
					$scope.showConfirm(sbiModule_translate.load("sbi.importusers.importuserok"));
				
					//reload tree
					$scope.reload();
				}else{
					$scope.showAction(data.ERROR)
				}
				
				
			}).error(function(data, status, headers, config) {
				console.log("ERRORS "+status,4000);
			})
		}else if($scope.typeSaveMenu == 'Override'){
			//override
			//remove the field $parent (add from the directive component-tree) because make the tree a circular object
			$scope.removeCircularDependences($scope.tree);
						
			sbiModule_restServices.post("1.0/serverManager/importExport/menu","overridemenu",$scope.tree)
			.success(function(data, status, headers, config) {
				if(data.STATUS=="OK"){
					$scope.showConfirm(sbiModule_translate.load("sbi.importusers.importuserok"));
					$scope.reload();
				
				}else{
					$scope.showAction(data.ERROR)
				}
			
			}).error(function(data, status, headers, config) {
				console.log("ERRORS "+status,4000);
			})
		}else{
			$scope.showAction(sbiModule_translate.load("sbi.importmenu.selectmode"))
		}		

	}
	$scope.reload = function(){
		
		sbiModule_restServices.get("1.0/serverManager/importExport/menu","getAllMenu")
		.success(function(data, status, headers, config) {
			$scope.currentMenu = [];
			$scope.treeInTheDB= [];
			$scope.currentMenu = data.currentMenu;
			$scope.parseToTree($scope.currentMenu,$scope.treeInTheDB);
			$scope.treeCopy=$scope.treeInTheDB;
		}).error(function(data, status, headers, config) {
			console.log("ERRORS "+status,4000);
		})
	}
	$scope.reloadTree = function(value){

		
		if(value == 'Missing'){
			for(var i=0;i<$scope.menu.length;i++){
				var index = $scope.indexInList($scope.menu[i],$scope.currentMenu );
				if(index==-1){
					$scope.currentMenu.push($scope.menu[i]);
				}
			}
			
			$scope.treeInTheDB=[];
			$scope.parseToTree($scope.currentMenu, $scope.treeInTheDB)
		}else{
			$scope.treeInTheDB = $scope.tree;
		}
		
		
		
		
	}
	$scope.removeCircularDependences = function(tree){
		for(var i=0;i<tree.length;i++){
			delete tree[i].$parent;
			if(tree[i].children.length!=0){
				for(var j=0;j<tree[i].children.length;j++){
					delete tree[i].children[j].$parent
				}
			}
				
		}
	}
	
	$scope.indexInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.name==item.name){
				return i;
			}
		}

		return -1;
	};
	$scope.indexObjectsInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.label==item.labelObj){
				return i;
			}
		}

		return -1;
	};
	$scope.showConfirm = function(text) {
	    // Appending dialog to document.body to cover sidenav in docs app
	    var confirm = $mdDialog.alert()
	          .title(text)
	          .ariaLabel('Lucky day')
	          .ok('Ok')
	         
	    $mdDialog.show(confirm).then(function() {

	    }, function() {
	      
	    });
	  };
	  
		$scope.showAction = function(text) {
			var toast = $mdToast.simple()
			.content(text)
			.action('OK')
			.highlightAction(false)
			.hideDelay(3000)
			.position('top')

			$mdToast.show(toast).then(function(response) {

				if ( response == 'ok' ) {


				}
			});
		};
};

app.controller('exportController', 
		['$http',
		 'sbiModule_download',
		 'sbiModule_device',
		 '$scope',
		 '$mdDialog',
		 '$timeout',
		 'sbiModule_logger',
		 'sbiModule_translate',
		 'sbiModule_restServices',
		 'sbiModule_config',
		 '$mdToast',
		 exportFuncController]);

function exportFuncController($http,sbiModule_download,sbiModule_device,$scope,$mdDialog,$timeout, sbiModule_logger, sbiModule_translate, sbiModule_restServices,sbiModule_config,$mdToast) {
	
	$scope.pathRest = { // /restful-services/1.0/menu/enduser
		restfulServices : '1.0',
		menuPath : 'menu/enduser',
	};
	
	$scope.restServices = sbiModule_restServices;
	$scope.download = sbiModule_download;
	$scope.log = sbiModule_logger;
	$scope.selected = [] ;
	$scope.customs = [];
	$scope.menu=[];
	$scope.fileAssociation = {};
	
	$scope.flags = {
		waitExport : false,
		enableDownload : false
	};
	
	$scope.checkboxs = {
		exportSubObj : false,
		exportSnapshots : false
	};
	
	$scope.exportFiles = function(){
		$scope.flags.waitExport = true;
		
		sbiModule_restServices.post('1.0/serverManager/importExport/menu', 'export', 
			{'EXPORT_FILE_NAME': $scope.exportName}, 
			{'responseType': 'arraybuffer'}
			)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty('errors')) {
				showToast(data.errors[0].message,4000);
			
			}else if(status==200){
				$scope.flags.enableDownload = true;
				$scope.downloadedFileName = $scope.exportName;
				
				$scope.download.getBlob(data, $scope.exportName, 'application/zip', 'zip');
				$scope.flags.enableDownload = false
			}
			$scope.flags.waitExport = false;
		}).error(function(data, status, headers, config) {
			$scope.flags.waitExport = false;
			showToast('ERRORS ' + status,4000);
		})
	};
	
	$scope.submitDownForm = function(form){
		$scope.flags.submitForm= true;
	};
	
	$scope.toggleEnableDownload = function(){
		$scope.flags.enableDownload = !$scope.flags.enableDownload;
	};
	
	
	
	
	$scope.showAlert = function (title, message){
		$mdDialog.show( 
				$mdDialog.alert()
				.parent(document.body)
				.clickOutsideToClose(true)
				.title(title)
				.textContent(message) //FROM angular material 1.0 
				.ok('Ok')
		);
	};
	
	$scope.debug= function(){
	};
	
	function showToast(text, time) {
		var timer = time == undefined ? 6000 : time;
		
		$mdToast.show(
				$mdToast
				.simple()
				.content(text)
				.position('top')
				.action('OK')
				.highlightAction(false)
				.hideDelay(timer)
		);
	};
};

app.controller('importController', 
		['sbiModule_download',
		 'sbiModule_device',
		 '$scope',
		 '$mdDialog',
		 '$timeout',
		 'sbiModule_logger',
		 'sbiModule_translate',
		 'sbiModule_restServices',
		 'sbiModule_config',
		 '$mdToast',
		 'importExportMenuModule_importConf',
		 importFuncController]);

function importFuncController(
		sbiModule_download,
		sbiModule_device,
		$scope,
		$mdDialog,
		$timeout,
		sbiModule_logger,
		sbiModule_translate,
		sbiModule_restServices,
		sbiModule_config,
		$mdToast,
		importExportMenuModule_importConf) {
	
	$scope.stepItem = [{name:'step0'}];
	$scope.selectedStep = 0;
	$scope.stepControl;
	$scope.IEDConf = importExportMenuModule_importConf;
};

app.config(['$mdThemingProvider', function($mdThemingProvider) {

    $mdThemingProvider.theme('knowage')

$mdThemingProvider.setDefaultTheme('knowage');
}]);
