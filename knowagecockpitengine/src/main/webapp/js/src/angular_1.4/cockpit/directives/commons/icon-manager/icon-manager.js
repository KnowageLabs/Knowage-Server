(function () {
	angular.module('cockpitModule')
	.directive('iconManager',function(sbiModule_config, knModule_fontIconsService){
		return{
			templateUrl: sbiModule_config.dynamicResourcesEnginePath + '/angular_1.4/cockpit/directives/commons/icon-manager/templates/icon-manager.html',
			scope: {
				selectedIcon: "=?",
				onSelection: "&",
				gridWidth: "@?",
				availableFamilies: "=?"
			},
			controller: function($scope,$filter,sbiModule_translate){
				
				$scope.availableIcons = knModule_fontIconsService.icons;
				$scope.translate = sbiModule_translate;
				
				$scope.familyFilter = function(value){
					if(!$scope.availableFamilies) return value;
					else{
						return $scope.availableFamilies.indexOf(value.name) != -1;
					}
				}
				
				function setChunks(array, dimension){
					var newArray = [];
					for(var f in array){
						var familyArray = {"name":array[f].name,"className":array[f].className,icons:[]};
						var iterator = 0;
						for(var k in array[f].icons){
							if (iterator == 0) var tempArray = [];
							if (iterator < dimension) {
								tempArray.push(array[f].icons[k]);
								iterator ++;
							}
							if (iterator == dimension) {
								familyArray.icons.push(tempArray);
								iterator = 0;
							}
						}
						newArray.push(familyArray);
					}

					return newArray;
				}
				
				$scope.iconChunks = setChunks($scope.availableIcons,$scope.gridWidth || 4);
				$scope.iconFamily = $scope.availableIcons[0].name;
				
				$scope.openFamily = function(familyName){
					if($scope.iconFamily == familyName) $scope.iconFamily = "";
					else $scope.iconFamily = familyName;
				}
				
				$scope.setIcon = function(icon){
					$scope.onSelection({icon:icon})
				}

			}
		}
	})
})();