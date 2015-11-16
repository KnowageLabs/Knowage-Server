angular.module('glossary_tree', ['ng-context-menu','ngMaterial','ui.tree', 'sbiModule'])
.directive('glossaryTree', function() {
  return {
    templateUrl: '/knowage/js/src/angular_1.4/tools/glossary/commons/templates/glossary-tree.html',
    controller: controllerFunction,
    scope: {
    	treeId: '@',
        treeOptions:'=',
        glossary:'=',
        showRoot:'@', //default true
        addChild:'&',
        addWord:'&',
        removeChild:'&',
        modifyChild:'&',
        modifyGlossary:'&',
        cloneGlossary:'&',
        deleteGlossary:'&',
        showSelectGlossary:'=',
        showSearchBar:'=',
        dragLogicalNode:'=',
        dragWordNode:'=',
        cloneItem:'=', //default false
        enableDrag:'=', //default true
        showInfo:'=',   //default false
        showInfoMenu:'='   //default false
      },
      link: function (scope, elm, attrs) {
    	  scope.nodeContextMenu=false;

    	  if(attrs.addChild){
    		  scope.functionality.push("addChild");
    		  scope.nodeContextMenu=true;
    	  }
    	  if(attrs.addWord){
    		  scope.functionality.push("addWord");
    		  scope.nodeContextMenu=true;
    	  }
    	  if(attrs.showInfoMenu){
    		  scope.functionality.push("showInfoMenu");
    		  scope.nodeContextMenu=true;
    	  }
    	  if(attrs.removeChild){
    		  scope.functionality.push("removeChild");
    		  scope.nodeContextMenu=true;
    	  }
    	  if(attrs.modifyChild){
    		  scope.functionality.push("modifyChild");
    		  scope.nodeContextMenu=true;
    	  }
    	  if(attrs.modifyGlossary){
    		  scope.functionality.push("modifyGlossary");
    	  }
    	  if(attrs.cloneGlossary) {
    		  scope.functionality.push("cloneGlossary");
    	  }
    	  if(attrs.deleteGlossary){
    		  scope.functionality.push("deleteGlossary");
    	  }
    	  
    	  
    	  if(attrs.showRoot){
    		  scope.showRoot=true;
    	  }
    	  if(attrs.showSelectGlossary) {
    		  scope.loadAllGloss();
    		  if(attrs.deleteGlossary)  scope.functionality.push("deleteGlossary");
    		  scope.functionality.push("showSelectGlossary");
    	  }
    	  if(attrs.showSearchBar){
    		  scope.functionality.push("showSearchBar");
    	  }

      }
  }
  	});


function controllerFunction($scope,sbiModule_restServices,sbiModule_translate,$mdDialog,$mdToast,$timeout){
	$scope.functionality=[];
	$scope.Allglossary=[];
	$scope.translate=sbiModule_translate;
	$scope.isDefined = function(obj){return ($scope.functionality.indexOf(obj)!=-1)};
	$scope.preloaderTree=false;
	$scope.searchNode;
	 
	$scope.loadAllGloss=function(){
		  console.log("load glossa")
		  sbiModule_restServices.get("1.0/glossary", "listGlossary").success(
					function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
							console.log(data.errors[0].message);
						} else {
							$scope.Allglossary=data;
						}

					}).error(function(data, status, headers, config) {
						console.log("Glossary non Ottenuti " + status);
					})
	   }
	
	 $scope.toggleNode = function(scope, item, gloss) {
	if($scope.prevSWSG!="" && $scope.prevSWSG!=undefined ){
				scope.toggle();
				return;	
			}
		 
			item.preloader = true;
			if (scope.collapsed) {
				 $scope.getGlossaryNode(gloss, item, scope)
			} else {
				scope.toggle();
				item.preloader = false;
			}
		};
		
		
	$scope.getGlossaryNode = function(gloss, node, togg) {
		var PARENT_ID = (node == null ? null : node.CONTENT_ID);
		var GLOSSARY_ID = (gloss == null ? null : gloss.GLOSSARY_ID);
	
		sbiModule_restServices
				.get(
						"1.0/glossary",
						"listContents",
						"GLOSSARY_ID=" + GLOSSARY_ID + "&PARENT_ID="
								+ PARENT_ID)
				.success(
						function(data, status, headers, config) {

							if (data.hasOwnProperty("errors")) {
								showErrorToast(data.errors[0].message);
								showToast(sbiModule_translate
										.load("sbi.glossary.load.error"), 3000);

							} else {
								if(togg==undefined || togg.collapsed){
								//check if parent is node or glossary
								node==null ? gloss.SBI_GL_CONTENTS = data : node.CHILD = data;
								}else{
									if(node!=null){
										node.CHILD.sort(function(a,b) {return (a.CONTENT_NM > b.CONTENT_NM) ? 1 : ((b.CONTENT_NM > a.CONTENT_NM) ? -1 : 0);} ); 
									}
								}

								if (togg != undefined) {
									togg.expand();
								}
								if(node!=null && node.hasOwnProperty("preloader")){
									node.preloader = false;
								}
							}
						}).error(function(data, status, headers, config) {
					showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
					if (togg != undefined) {
						togg.expand();
						node.preloader = false;
					}
				})
	}
	
	
	$scope.showSelectedGlossary = function(gloss) {
		console.log("showSelectedGlossary",gloss)
		if($scope.glossary!=undefined && $scope.glossary.GLOSSARY_ID==gloss.GLOSSARY_ID){return;}
		$scope.glossary=gloss;
		$scope.getGlossaryNode($scope.glossary, null);
	}
	
	
	$scope.prevSWSG = "";
	$scope.tmpSWSG = "";
	$scope.SearchWordInSelectedGloss= function(ele){
		console.log("SearchWordInSelectedGloss  "+ele);
		$scope.tmpSWSG = ele;
		$timeout(function() {
			if ($scope.tmpSWSG != ele || $scope.prevSWSG == ele) {
				console.log("interrompo la ricerca  di ele " + ele)
				return;
			}

			$scope.prevSWSG = ele;

			console.log("cerco "+ele)
			showTreePreloader('preloaderTree');
			sbiModule_restServices.get("1.0/glossary", "glosstreeLike", "WORD=" + ele+"&GLOSSARY_ID="+$scope.glossary.GLOSSARY_ID).success(
					function(data, status, headers, config) {
						console.log("glosstreeLike Ottenuti " + status)
						console.log(data)

						if (data.hasOwnProperty("errors")) {
							showToast(sbiModule_translate.load("sbi.glossary.load.error"),3000);

						} else {
							$scope.glossary=data.GlossSearch;

							if(ele!=""){
								$timeout(function() {
									$scope.expandAllTree($scope.treeId);
								},500);
							}
						}
						hideTreePreloader('preloaderTree');

					}).error(function(data, status, headers, config) {
						console.log("glosstreeLike non Ottenuti " + status);
						showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
						hideTreePreloader('preloaderTree');
					})

		}, 1000);
	}
	
	$scope.expandAllTree= function(tree){
		console.log("$scope.id",tree)
		console.log(angular.element(document.getElementById(tree)))
		angular.element(document.getElementById(tree)).scope().expandAll();
	}
	
	$scope.showInfoWORD=function(ev,wordid){
		$mdDialog
		.show({  
			controllerAs : 'infCtrl',
			 scope: $scope,preserveScope: true,
			controller : function($mdDialog) {
				var iwctrl = this;
				sbiModule_restServices.get("1.0/glossary", "getWord", "WORD_ID=" + wordid)
				.success(
						function(data, status, headers, config) {
							if (data.hasOwnProperty("errors")) {
								showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
							} else {
								iwctrl.info = data;
							}
						}).error(function(data, status, headers, config) {
							showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

						})
			},
			templateUrl : '/knowage/js/src/angular_1.4/tools/glossary/commons/templates/info_word.html',
			targetEvent : ev,
			clickOutsideToClose :true
		})
	}
	
	$scope.showInfoNode=function(ev,contentid){
		$mdDialog
		.show({  
			controllerAs : 'infCtrl',
			 scope: $scope,preserveScope: true,
			controller : function($mdDialog) {
				var iwctrl = this;
				sbiModule_restServices.get("1.0/glossary", "getContent", "CONTENT_ID=" + contentid)
				.success(
						function(data, status, headers, config) {
							if (data.hasOwnProperty("errors")) {
								showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
							} else {
								iwctrl.info = data;
							}
						}).error(function(data, status, headers, config) {
							showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);

						})
			},
			templateUrl : '/knowage/js/src/angular_1.4/tools/glossary/commons/templates/info_content.html',
			targetEvent : ev,
			clickOutsideToClose :true
		})
	}
	
	
	function showTreePreloader(pre) {
		$scope[pre] = true;

	}
	function hideTreePreloader(pre) {
		$scope[pre] = false;
	}
	
	function showToast(text, time) {
		var timer = time == undefined ? 6000 : time;

		console.log(text)
		$mdToast.show($mdToast.simple().content(text).position('top').action(
				'OK').highlightAction(false).hideDelay(timer));
	}
		
}


       