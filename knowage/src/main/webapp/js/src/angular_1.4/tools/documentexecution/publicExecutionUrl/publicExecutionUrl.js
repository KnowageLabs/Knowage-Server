var app = angular.module('documentExecutionModule').controller('publicExecutionUrlController',
		['$scope', '$mdDialog', '$timeout', '$window', 'sbiModule_translate', publicExecutionUrlControllerFunction ]);

function publicExecutionUrlControllerFunction(scope, $mdDialog, $timeout, $window,sbiModule_translate, publicUrl, embedHTML, isPublic) {

	scope.publicUrl = publicUrl;

	scope.embedHTML = embedHTML;

	scope.isPublic = isPublic;

	scope.translate = sbiModule_translate;

	scope.closeDialog = function() {
		$mdDialog.hide();
	}

//	scope.showCopyLinkInfo= function(ev){
//		$mdDialog.show(
//				$mdDialog.alert()
//					.clickOutsideToClose(true)
//					.content(sbiModule_translate.load("sbi.execution.linkToDocumentInfo"))
//					.ok(sbiModule_translate.load("sbi.general.close"))
//					.targetEvent(ev)
//		);
//	}

}

