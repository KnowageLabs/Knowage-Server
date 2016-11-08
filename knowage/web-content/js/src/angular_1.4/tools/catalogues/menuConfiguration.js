/**
   @author Radmila Selakovic (rselakov, radmila.selakovic@mht.net) 
 */

var app = angular.module("MenuConfigurationModule", ['angular-list-detail', 'ui.tree', 'sbiModule', 'angular_table' ]);
app.config([ '$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
} ]);

app.controller('MenuConfigurationController', [ "$scope","sbiModule_restServices", "sbiModule_translate", "$mdDialog", "sbiModule_messaging",
		MenuConfigurationFunction ]);
function MenuConfigurationFunction($scope, sbiModule_restServices,sbiModule_translate, $mdDialog,sbiModule_messaging) {
	// getting all menus
	$scope.getListOfMenu = function() {
		sbiModule_restServices.promiseGet("2.0/menu", "").then(
				function(response) { 
					for(var i=0; i<response.data.length; i++){
						response.data[i].expanded=true;	 
							
					}
					$scope.listOfMenu = angular.copy(response.data); 
					$scope.listOfMenu.unshift({name: "Menu tree", menuId: null});
					$scope.listOfMenu_copy = angular.copy($scope.listOfMenu);
					console.log($scope.listOfMenu_copy );
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	};
	// getting all roles
	$scope.getRoles = function() {
		sbiModule_restServices.promiseGet("2.0/roles/short", "").then(
				function(response) {
					$scope.roles = response.data; 
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	};
	$scope.getHTMLFiles= function() {
		
		 
		sbiModule_restServices.promiseGet("2.0/menu/htmls",!$scope.selectedMenu.menuId ? 0 : $scope.selectedMenu.menuId).then(
				function(response) {
					$scope.files = response.data; 
					console.log($scope.files);
					 $scope.checkHtml($scope.selectedMenu);
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	};
	angular.element(document).ready(function() { // on page load function
		$scope.getListOfMenu();
		$scope.getRoles();
		 

	});

	$scope.showme = false;
	$scope.showPlusButton = false;
	$scope.translate = sbiModule_translate;
	$scope.listOfMenu = [];
	$scope.selectedMenu = {};
	$scope.roles = [];
	$scope.role = [];
	$scope.dirtyForm = false;
	$scope.files =[];
	 
	 
	$scope.cancel = function() { // on cancel button
		$scope.selectedMenu ={};
		$scope.role = [];
		$scope.showme = false;
		$scope.dirtyForm=false;
		
	}
	$scope.fake = {};
	$scope.save = function() {
		$scope.formatMenu();

		console.log($scope.selectedMenu.roles);
   	 console.log($scope.selectedMenu.hasOwnProperty("menuId") + " " + $scope.selectedMenu);
		 if($scope.selectedMenu.hasOwnProperty("menuId")){ 
	    	 // if item already exists do update PUT
	    	 $scope.fake = {
	    			 menuId : $scope.selectedMenu.menuId,
	    			 descr : $scope.selectedMenu.descr,
	    			 
	    			 externalApplicationUrl : $scope.selectedMenu.externalApplicationUrl,
	    			 
	    			 hasChildren :  $scope.selectedMenu.hasChildren,
	    			 hideSliders :  $scope.selectedMenu.hideSliders,
	    			 hideToolbar :  $scope.selectedMenu.hideToolbar,
	    			 level :  $scope.selectedMenu.level,
	    			 lstChildren :  $scope.selectedMenu.lstChildren,
	    			 name : $scope.selectedMenu.name,
	    			 objId :	!$scope.selectedMenu.document ? null : $scope.selectedMenu.document.fromDocId,
	    			 objParameters : $scope.selectedMenu.objParameters,
	    			 parentId : $scope.selectedMenu.parentId,
	    			 prog : $scope.selectedMenu.prog,
	    			 roles : $scope.selectedMenu.roles,
	    			 staticPage :$scope.selectedMenuItem.page,
	    			 viewIcons : $scope.selectedMenu.viewIcons,
	    			 adminsMenu :$scope.selectedMenu.adminsMenu 
	    			 
	    	 }
	    	 console.log($scope.fake);
	    		sbiModule_restServices.promisePut("2.0/menu",$scope.fake.menuId, $scope.fake)
				.then(function(response) {
					$scope.listOfMenu_copy = $scope.getListOfMenu(); 
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');
					$scope.selectedMenu ={};
					$scope.showme = false;
					$scope.dirtyForm=false;
					
				}, function(response) {
					console.log("pukao")
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});		
				
		}else{
		// post create new item
			 $scope.fake = {
					 descr : $scope.selectedMenu.descr,
	    			 externalApplicationUrl : $scope.selectedMenu.externalApplicationUrl,
	    			  
	    			 hideSliders :  $scope.selectedMenu.hideSliders,
	    			 hideToolbar :  $scope.selectedMenu.hideToolbar,
	    			 level :  $scope.selectedMenu.level,
	    			  
	    			 name : $scope.selectedMenu.name,
	    			 objId :	!$scope.selectedMenu.document ? null : $scope.selectedMenu.document.fromDocId,
	    			 objParameters : $scope.selectedMenu.objParameters,
	    			 parentId : $scope.parentID ,
	    		 
	    			 roles : $scope.selectedMenu.roles,
	    			 staticPage :$scope.selectedMenuItem.page,
	    			 viewIcons : $scope.selectedMenu.viewIcons,
	    			 adminsMenu :$scope.selectedMenu.adminsMenu 
	    			 
	    	 }
			 
			 console.log($scope.fake);
			 sbiModule_restServices.promisePost('2.0/menu','',$scope.fake)
				.then(function(response) {
					$scope.listOfMenu_copy = $scope.getListOfMenu(); 
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
					$scope.selectedMenu ={};
					$scope.showme = false;
					$scope.dirtyForm=false;
				}, function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
					
				});
		}
		
	}

	$scope.setDirty = function() {
		$scope.dirtyForm = true;
	}
	
	 
	$scope.nodeTemp = {};
	
	$scope.mouseenter = function(node) {
		$scope.nodeTemp = node;
	}
	
	$scope.mouseleave = function(node) {
		$scope.nodeTemp = null;
	}
	
	$scope.tooggle = function (scope){ 
		console.log(scope);
		scope.toggle();
	}
	
	 
	
	$scope.createMenu = function() {
		$scope.parentID = $scope.selectedMenu.menuId;
		if($scope.selectedMenu.menuId==null){
			$scope.role=$scope.roles;
			 $scope.selectedMenu.level= 0;
			 $scope.parentID = null;
		}
		console.log($scope.selectedMenu.menuId)
		
 
		$scope.selectedMenu.descr= "";
		$scope.selectedMenu.name= "";

		delete $scope.selectedMenu.menuId ;
		delete $scope.selectedMenu.prog ;
		 $scope.selectedMenu.hideToolbar = false;
		 $scope.selectedMenu.hideSliders =false;
		 $scope.selectedMenuItem = null;
		 $scope.selectedMenu.externalApplicationUrl = "";
		 $scope.selectedMenu.viewIcons = false ;		
		 $scope.selectedMenu.document = null;
		 $scope.selectedMenu.objParameters= "";
		 $scope.selectedMenu.staticPage= null;
		$scope.selectedMenu.parentId= $scope.parentID;
		console.log($scope.selectedMenu.parentId)
		$scope.showme = true;
		
	}
	$scope.columnsArray = [
	           			{
	           				"label" : sbiModule_translate.load("sbi.menu.columns.roles"),
	           				"name" : "name",
	           				"size" : "50px",
	           				hideTooltip : true
	           			}
	           	];
	
	
	$scope.allTypes = [ {
		name : "",
		id : 0,
		label : sbiModule_translate.load("sbi.menu.nodeEmpty")

		}, {
		name : "page",
		id : 3,
		label : sbiModule_translate.load("sbi.menu.nodeStaticPage")
		}, {
		name : "doc",
		id : 1,
		label : sbiModule_translate.load("sbi.menu.nodeDocument")
			}, {
		name : "app",
		id : 2,
		label : sbiModule_translate.load("sbi.menu.nodeExternalApp")
		}

	];
	$scope.selectedMenuItem ={};
	
	$scope.checkHtml  = function (menuItem) {
		console.log("fff");
		if(menuItem.staticPage) {

			for (i=0; i<$scope.files.length;i++) {
				if ($scope.files[i].name.toLowerCase()===menuItem.staticPage) {
					$scope.selectedMenuItem.page= $scope.files[i].name;
					break;
				}	
			}

		}
		
	}
	$scope.checkPropertiesFromSelectedMenu  = function (menuItem) {
		console.log("fff");
		if (menuItem.externalApplicationUrl!=null) {
			for (i=0; i<$scope.allTypes.length;i++) {
				if ($scope.allTypes[i].name.toLowerCase()==="app") {
					$scope.selectedMenuItem.typeId = $scope.allTypes[i].id;
					break;
				}	
			}	
		}
		else if(menuItem.staticPage) {

			for (i=0; i<$scope.allTypes.length;i++) {
				if ($scope.allTypes[i].name.toLowerCase()==="page") {
					$scope.selectedMenuItem.typeId= $scope.allTypes[i].id;
					break;
				}	
			}

		}
		else if(menuItem.objId) {

			for (i=0; i<$scope.allTypes.length;i++) {
				if ($scope.allTypes[i].name.toLowerCase()==="doc") {
					$scope.selectedMenuItem.typeId = $scope.allTypes[i].id;
					break;
				}	
			}

		}
		else   {
			for (i=0; i<$scope.allTypes.length;i++) {
				if ($scope.allTypes[i].name.toLowerCase()==="") {
					$scope.selectedMenuItem.typeId = $scope.allTypes[i].id;
					break;
				}	
			}

		}
		
	
	}
	$scope.setPropertiesForCombo = function(){
		
		console.log($scope.selectedMenuItem.typeId);

		 $scope.selectedMenuItem.page = null;		
		 $scope.selectedMenu.document = null;
		 $scope.selectedMenu.objParameters= null;
		 $scope.selectedMenu.hideToolbar = false;
		 $scope.selectedMenu.hideSliders =false;
		 $scope.selectedMenu.externalApplicationUrl = null; 
		 if($scope.selectedMenuItem.typeId===3) {
			 $scope.getHTMLFiles();
		 }
	}
	 $scope.setRoles = function () {
	        $scope.role = [];
	        for (var i = 0; i < $scope.roles.length; i++) {
	            for (var j = 0; j < $scope.selectedMenu.roles.length; j++) {
	                if ($scope.selectedMenu.roles[j].id == $scope.roles[i].id) {
	                    $scope.role.push($scope.roles[i]);
	                }
	            }
	        }
 
	    }
	 $scope.formatMenu = function () {
	        var tmpR = [];
	        var tmpA = {};
	        for (var i = 0; i < $scope.role.length; i++) {
	            tmpR.push($scope.role[i]);
	        }
	        $scope.selectedMenu.roles= tmpR;
	   
	    }

	$scope.listAllDocuments = function() {
		$scope.listDocuments(function(item, listId, closeDialog){
			if(!$scope.selectedMenu.document)$scope.selectedMenu.document = {};
			$scope.selectedMenu.document.fromDocId = item.DOCUMENT_ID;
			$scope.selectedMenu.document.fromDoc = item.DOCUMENT_NAME;

			console.log($scope.selectedMenu.document.fromDoc);
			loadInputParameters(item.DOCUMENT_LABEL,function(data){
				$scope.selectedMenu.fromPars = data;
				loadOutputParameters(item.DOCUMENT_ID,function(data){
					$scope.selectedMenu.fromPars = $scope.selectedMenu.fromPars.concat(data);
					closeDialog();
				});
			});
		});
	};

	function loadOutputParameters(documentId, callbackFunction) {
		console.log("outnput")
		sbiModule_restServices.promiseGet('2.0/documents/'+documentId+'/listOutParams', "", null)
		.then(function(response) {
			var data = response.data;
			var parameters = [];
			for(var i=0;i<data.length;i++){
				parameters.push({'id':data[i].id,'name':data[i].name,'type':0});
			}
			callbackFunction(parameters);
		},function(response){});
		
	}
	
	$scope.listDocuments = function(clickOnSelectedDocFunction, item) {
		$mdDialog.show({
			controller: DialogController,
			templateUrl: 'dialog1.tmpl.html',
			parent: angular.element(document.body),
			clickOutsideToClose:false,
			locals: {
				clickOnSelectedDoc: clickOnSelectedDocFunction
				,translate: sbiModule_translate
			}
		});
		
		function DialogController($scope, $mdDialog, clickOnSelectedDoc, translate) {
			$scope.closeDialog = function() {
				$mdDialog.hide();
			};
			$scope.changeDocPage=function(searchValue, itemsPerPage, currentPageNumber , columnsSearch,columnOrdering, reverseOrdering){
				if(searchValue==undefined || searchValue.trim().lenght==0 ){
					searchValue='';
				}
				var item="Page="+currentPageNumber+"&ItemPerPage="+itemsPerPage+"&label=" + searchValue;
				$scope.loadListDocuments(item);
			};
			$scope.clickOnSelectedDoc = clickOnSelectedDoc;
			$scope.translate = translate;
			$scope.loading = true;
			$scope.totalCount = 0
			$scope.loadListDocuments = function(item){
				console.log(item);
				sbiModule_restServices.promiseGet("2.0/documents", "listDocument",item).then(
						function(response){
							$scope.loading = false;
							$scope.listDoc = response.data.item;
							$scope.totalCount = response.data.itemCount;
						},
							
						function(response){
								sbiModule_restServices.errorHandler(response.data,"")
							}
						) 
			}
		
		}
	}
	function loadInputParameters(documentLabel, callbackFunction) {
		console.log("iinput")
		sbiModule_restServices.promiseGet('2.0/documents/'+documentLabel+'/parameters', "", null)
		.then(function(response) {
			var data = response.data;
			var parameters = [];
			for(var i=0;i<data.length;i++){
				parameters.push({'id':data[i].id,'name':data[i].label,'type':1});
			}
			callbackFunction(parameters);
		},function(response){});
	}
	$scope.openDocuments = function() {

	}
	$scope.styleO = "";
	$scope.showSelectedMenu = function(item) {
		$scope.nodeTempT = item;
		console.log(item);
		if ($scope.selectedMenu.menuId != item.menuId) {
			$scope.selectedMenu = {};
			$scope.selectedMenuItem = {
				page : ""
			};
			if ($scope.dirtyForm) {
				$mdDialog.show($scope.confirm).then(function() {
					$scope.showme = true;
					$scope.dirtyForm = false;
					$scope.selectedMenu = angular.copy(item);
					$scope.setRoles();
					$scope.checkPropertiesFromSelectedMenu($scope.selectedMenu);
					if ($scope.selectedMenu.staticPage) {
						$scope.getHTMLFiles();
					}
					if ($scope.selectedMenu.objId) {
						$scope.getDocName(item.objId);
					} 
				}, function() {
					$scope.showme = true;
				});

			} else {
				$scope.selectedMenu = angular.copy(item);
				$scope.setRoles();
				$scope.showme = true;
				$scope.checkPropertiesFromSelectedMenu($scope.selectedMenu);
				if ($scope.selectedMenu.staticPage) {

					$scope.getHTMLFiles();
				}
				if ($scope.selectedMenu.objId) {

					$scope.getDocName(item.objId);
				}
			}
		}
		console.log("deeeeeeeeeeeeeeeeeeeeee");
		console.log($scope.selectedMenu.menuId==null);
		
		if($scope.selectedMenu.menuId==null){
			console.log("deeeeeeeeeeeeeeeeeeeeee");
			console.log($scope.selectedMenu.menuId==null);
			$scope.showme = false;
			$scope.dirtyForm=false;
			console.log($scope.showme);
			console.log($scope.dirtyForm);
		}
	}
	var map = {};
	$scope.canBeMovedDown = function (item){
		
		Array.prototype.max = function() {
			  return Math.max.apply(null, this);
			};

			Array.prototype.min = function() {
			  return Math.min.apply(null, this);
			};
		
		
		if(item.lstChildren!= undefined){
			
			if(item.parentId==null){
				
				if (!map.hasOwnProperty("null")) {
					map["null"] = new Array();					
				}
				else {
					if (map["null"].indexOf(item.prog) < 0) {
						map["null"].push(item.prog);
					}
				}
			} 
			if(item.lstChildren.length>0){
			for (j=0; j<item.lstChildren.length; j++) {
				if (!map.hasOwnProperty(item.lstChildren[j].parentId + "")) {
						map[item.lstChildren[j].parentId + ""] = new Array();					
				}
				else {
					if (map[item.lstChildren[j].parentId + ""].indexOf(item.lstChildren[j].prog) < 0) {
						map[item.lstChildren[j].parentId + ""].push(item.lstChildren[j].prog);
					}
				}
				
			}}
			 
//			console.log(map[item.parentId+""]);
			if(map[item.parentId+""] && map[item.parentId+""].length>0 && map[item.parentId+""].max() > item.prog) {
				return true;
			}
			else {
				return false;
			}
		}
		
	}
	
	$scope.moveUp = function (item){
		console.log(item)
		sbiModule_restServices.promiseGet("2.0/menu/moveUp", item.menuId).then(
				function(response) {	
					$scope.getListOfMenu();
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	}
	$scope.moveDown = function (item){
		console.log(item)
		sbiModule_restServices.promiseGet("2.0/menu/moveDown", item.menuId).then(
				function(response) {	
					$scope.getListOfMenu();
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	}
	$scope.changeWithFather = function (item){
		console.log(item)
		sbiModule_restServices.promiseGet("2.0/menu/changeWithFather", item.menuId).then(
				function(response) {	
					$scope.getListOfMenu();
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	}
	$scope.getDocName = function(objId) {
		sbiModule_restServices.promiseGet("2.0/documents/docName", objId).then(
				function(response) {
					$scope.selectedMenu.document = {
						fromDoc : null
					};
					$scope.selectedMenu.document.fromDoc = response.data.name;

				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate
							.load(response.data.errors[0].message), 'Error');
				});
	}
	
	$scope.deleteMenu= function (item){
		sbiModule_restServices.promiseDelete("2.0/menu", item.menuId).then(
				function(response) {
					$scope.getListOfMenu();
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
					$scope.selectedMenu = {};
					$scope.showme=false;
					$scope.dirtyForm=false;	;
				},
				function(response) {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load(response.data.errors[0].message), 'Error');
				});
	}
	$scope.confirm = $mdDialog.confirm().title(
			sbiModule_translate.load("sbi.catalogues.generic.modify")).content(
			sbiModule_translate.load("sbi.catalogues.generic.modify.msg"))
			.ariaLabel('toast').ok(
					sbiModule_translate.load("sbi.general.continue")).cancel(
					sbiModule_translate.load("sbi.general.cancel"));



};

