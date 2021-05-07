(function () {
	angular.module('cockpitModule')
	.directive('gallerySelector',function(sbiModule_config){
		return{
			templateUrl: sbiModule_config.dynamicResourcesEnginePath + '/angular_1.4/cockpit/directives/commons/gallery-selector/template/gallerySelector.html',
			scope: {
				widgetType: "@",
				callback: "&",
				noItems: "&?"
			},
			controller: function($scope,$filter,$http,sbiModule_translate,sbiModule_user){
				$scope.loading = true;
				$scope.emptyTemplate = {
						name:"Empty Template",
						description:"Basic empty template",
						code: {
							html:"",
							css:"",
							python:"",
							js:""
						},
						image: "",
						tags: []
					};
				$http.get('/knowage-api/api/1.0/widgetgallery/widgets/' + $scope.widgetType,
						{headers:{
							"x-Kn-Authorization":"Bearer "+ sbiModule_user.userUniqueIdentifier
						}}
				).then(function(resolve){
					$scope.availableGallery = resolve.data;
					if($scope.availableGallery.length == 0 && $scope.noItems) $scope.noItems(); 
					$scope.availableGallery.splice(0,0,$scope.emptyTemplate);
					$scope.loading = false;
				},
				function(error){
					$scope.loading = false;
					if($scope.noItems) $scope.noItems(); 
				})

				$scope.setSelectedTemplate = function(template){
					$scope.callback({template:template})
				}
			
			}
		}
	})
})();