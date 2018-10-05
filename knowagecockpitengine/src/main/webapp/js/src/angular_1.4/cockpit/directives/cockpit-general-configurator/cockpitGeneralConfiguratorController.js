function cockpitGeneralConfigurationController($scope,$rootScope,sbiModule_translate,cockpitModule_template,sbiModule_restServices,cockpitModule_properties,mdPanelRef){
  $scope.translate=sbiModule_translate;
   
  //clone template to reset it if user cancel the dialog
	  $scope.clonedTemplate={};
	  $scope.clonedDocumentProperty={};
	  angular.copy(cockpitModule_template.configuration,$scope.clonedTemplate);
	  angular.copy(cockpitModule_properties,$scope.clonedDocumentProperty);

	  $scope.saveConfiguration=function(){
		  
		  $rootScope.cockpitBackgroundColor = $scope.clonedTemplate.style.backgroundColor;
		  angular.copy($scope.clonedTemplate,cockpitModule_template.configuration);
		  angular.copy($scope.clonedDocumentProperty,cockpitModule_properties);

		  mdPanelRef.close();
		  $scope.$destroy();
	  }
	  $scope.cancelConfiguration=function(){
		  mdPanelRef.close();
		  $scope.$destroy();
	  }
  }