function cockpitGeneralConfigurationController($scope,$rootScope,sbiModule_translate,cockpitModule_template,sbiModule_restServices,cockpitModule_properties,mdPanelRef){
  $scope.translate=sbiModule_translate;

  //clone template to reset it if user cancel the dialog
	  $scope.clonedTemplate={};
	  $scope.clonedDocumentProperty={};
	  angular.copy(cockpitModule_template.configuration,$scope.clonedTemplate);
	  angular.copy(cockpitModule_properties,$scope.clonedDocumentProperty);

	  $scope.codemirrorLoaded = function(_editor) {
	        $scope._doc = _editor.getDoc();
	        $scope._editor = _editor;
	        _editor.focus();
	        $scope._doc.markClean()
	        _editor.on("beforeChange", function() {});
	        _editor.on("change", function() {});
	    };

	    //codemirror options
	    $scope.editorOptionsCss = {
	        theme: 'eclipse',
	        lineWrapping: true,
	        lineNumbers: true,
	        mode: {name:'css'},
	        onLoad: $scope.codemirrorLoaded
	    };

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