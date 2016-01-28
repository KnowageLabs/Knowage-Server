angular.module('layer_tree', ['ng-context-menu','ngMaterial','ui.tree', 'angular_rest'])
.directive('layerTree', function() {
	return {
		// templateUrl: '/knowage/js/src/angular_1.4/tools/glossary/commons/templates/glossary-tree.html',
		controller: controllerFunction,
		scope: {
			treeId: '@',
			treeOptions:'=',
			layer:'=',
			showRoot:'@', //default true
			addChild:'&',
			addWord:'&',
			removeChild:'&',
			modifyChild:'&',
			modifyLayer:'&',
			cloneLayer:'&',
			deleteLayer:'&',
			showSelectLayer:'=',
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
			if(attrs.modifyLayer){
				scope.functionality.push("modifyLayer");
			}
			if(attrs.cloneLayer) {
				scope.functionality.push("cloneLayer");
			}
			if(attrs.deleteLayer){
				scope.functionality.push("deleteLayer");
			}

			if(attrs.showRoot){
				scope.showRoot=true;
			}
			if(attrs.showSelectLayer) {
				scope.loadAllLay();
				if(attrs.deleteLayer)  scope.functionality.push("deleteLayer");
				scope.functionality.push("showSelectLayer");
			}
			if(attrs.showSearchBar){
				scope.functionality.push("showSearchBar");
			}
		}
	};
});

function controllerFunction($scope,restServices,translate,$mdDialog,$mdToast,$timeout){
	$scope.functionality=[];
	$scope.Alllayer=[];
	$scope.translate=translate;
	$scope.isDefined = function(obj){return ($scope.functionality.indexOf(obj)!=-1)};
	$scope.preloaderTree=false;
	$scope.searchNode;

	$scope.loadAllLay=function(){
		console.log("load laye")
		restServices.get("1.0/layer", "listLayer")
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty("errors")) {
				console.log(data.errors[0].message);
			} else {
				$scope.Alllayer=data;
			}
		}).error(function(data, status, headers, config) {
			console.log("Layer non Ottenuti " + status);
		});
	}

	$scope.toggleNode = function(scope, item, lay) {
		if($scope.prevSWSG!="" && $scope.prevSWSG!=undefined ){
			scope.toggle();
			return;	
		}

		item.preloader = true;
		if (scope.collapsed) {
			$scope.getLayerNode(lay, item, scope)
		} else {
			scope.toggle();
			item.preloader = false;
		}
	};

	$scope.getLayerNode = function(lay, node, togg) {
		var PARENT_ID = (node == null ? null : node.CONTENT_ID);
		var LAYER_ID = (lay == null ? null : lay.LAYER_ID);

		restServices
		.get("1.0/layer", "listContents", "LAYER_ID=" + LAYER_ID + "&PARENT_ID=" + PARENT_ID)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty("errors")) {
				showErrorToast(data.errors[0].message);
				showToast(translate.load("sbi.layer.load.error"), 3000);
			} else {
				if(togg==undefined || togg.collapsed){
					//check if parent is node or glossary
					node==null ? lay.SBI_GL_CONTENTS = data : node.CHILD = data;
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
			showToast(translate.load("sbi.layer.load.error"), 3000);
			if (togg != undefined) {
				togg.expand();
				node.preloader = false;
			}
		})
	}

	$scope.showSelectedLayer = function(lay) {
		console.log("showSelectedLayer",lay)
		if($scope.layer!=undefined && $scope.layer.LAYER_ID==lay.LAYER_ID){return;}
		$scope.layer=lay;
		$scope.getLayerNode($scope.layer, null);
	};

	$scope.prevSWSG = "";
	$scope.tmpSWSG = "";
	$scope.SearchWordInSelectedLay= function(ele){
		console.log("SearchWordInSelectedLay  " + ele);
		$scope.tmpSWSG = ele;
		$timeout(function() {
			if ($scope.tmpSWSG != ele || $scope.prevSWSG == ele) {
				console.log("interrompo la ricerca  di ele " + ele)
				return;
			}

			$scope.prevSWSG = ele;

			console.log("cerco "+ele);
			showTreePreloader('preloaderTree');
			restServices.get("1.0/layer", "laytreeLike", "WORD=" + ele+"&LAYER_ID="+$scope.layer.LAYER_ID)
			.success(function(data, status, headers, config) {
				console.log("laytreeLike Ottenuti " + status);
				console.log(data)

				if (data.hasOwnProperty("errors")) {
					showToast(translate.load("sbi.layer.load.error"),3000);
				} else {
					$scope.layer=data.LaySearch;

					if(ele!=""){
						$timeout(function() {
							$scope.expandAllTree($scope.treeId);
						},500);
					}
				}
				hideTreePreloader('preloaderTree');
			}).error(function(data, status, headers, config) {
				console.log("laytreeLike non Ottenuti " + status);
				showToast(translate.load("sbi.layer.load.error"), 3000);
				hideTreePreloader('preloaderTree');
			});
		}, 1000);
	};

	$scope.expandAllTree= function(tree){
		console.log("$scope.id",tree);
		console.log(angular.element(document.getElementById(tree)));
		angular.element(document.getElementById(tree)).scope().expandAll();
	};

	$scope.showInfoWORD=function(ewordid){
		$mdDialog.show({  
			controllerAs : 'infCtrl',
			scope: $scope,preserveScope: true,
			controller : function($mdDialog) {
				var iwctrl = this;
				restServices.get("1.0/layer", "getWord", "WORD_ID=" + wordid)
				.success(
						function(data, status, headers, config) {
							if (data.hasOwnProperty("errors")) {
								showToast(translate.load("sbi.layer.load.error"), 3000);
							} else {
								iwctrl.info = data;
							}
						}).error(function(data, status, headers, config) {
							showToast(translate.load("sbi.layer.load.error"), 3000);

						})
			},
			//	templateUrl : '/knowage/js/src/angular_1.4/tools/glossary/commons/templates/info_word.html',
			clickOutsideToClose :true
		});
	};

	$scope.showInfoNode=function(contentid){
		$mdDialog.show({  
			controllerAs : 'infCtrl',
			scope: $scope,preserveScope: true,
			controller : function($mdDialog) {
				var iwctrl = this;
				restServices.get("1.0/layer", "getContent", "CONTENT_ID=" + contentid)
				.success(
						function(data, status, headers, config) {
							if (data.hasOwnProperty("errors")) {
								showToast(translate.load("sbi.layer.load.error"), 3000);
							} else {
								iwctrl.info = data;
							}
						}).error(function(data, status, headers, config) {
							showToast(translate.load("sbi.layer.load.error"), 3000);

						})
			},
			//	templateUrl : '/knowage/js/src/angular_1.4/tools/glossary/commons/templates/info_content.html',
			clickOutsideToClose :true
		});
	};

	function showTreePreloader(pre) {
		$scope[pre] = true;
	};
	
	function hideTreePreloader(pre) {
		$scope[pre] = false;
	};

	function showToast(text, time) {
		var timer = time == undefined ? 6000 : time;

		console.log(text);
		$mdToast
		.show(
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