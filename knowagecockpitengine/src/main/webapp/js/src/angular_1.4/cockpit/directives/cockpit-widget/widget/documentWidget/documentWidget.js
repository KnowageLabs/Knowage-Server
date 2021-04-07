/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 * v0.0.1
 *
 */
(function() {
angular.module('cockpitModule')
.directive('cockpitDocumentWidget',function(cockpitModule_widgetServices,$mdDialog){
	   return{
		   templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/documentWidget/templates/documentWidgetTemplate.html',
		   controller: cockpitDocumentWidgetControllerFunction,
		   compile: function (tElement, tAttrs, transclude) {
                return {
                    pre: function preLink(scope, element, attrs, ctrl, transclud) {
                    	element[0].classList.add("flex");
                    	element[0].classList.add("layout-column");
                    },
                    post: function postLink(scope, element, attrs, ctrl, transclud) {
                    	//init the widget
                    	element.ready(function () {
                    		scope.initWidget();
                        });



                    }
                };
		   	}
	   };
});

function cockpitDocumentWidgetControllerFunction($scope,cockpitModule_widgetConfigurator,$q,$mdPanel,$timeout,sbiModule_config,cockpitModule_properties,cockpitModule_utilstServices,cockpitModule_widgetSelection){
	var currentDocId;

	$scope.finishLoadingIframe=function(){
		$scope.hideWidgetSpinner();
	}
	$scope.init=function(element,width,height){
		$scope.refreshWidget(null, 'init');
	};
	$scope.cockpitModule_properties = cockpitModule_properties;
	$scope.refresh=function(element,width,height,data,nature){
		if(nature == 'resize' || nature == 'gridster-resized' || nature == 'fullExpand'){
			return;
		}
		var doc= $scope.getDocument();
		if(doc!=undefined && (angular.equals(nature,'selections') || angular.equals(nature,'parameter_change') ||  angular.equals(nature,'filters') || !angular.equals(currentDocId,doc.DOCUMENT_ID))){
			//show the spinner, and then the spinner is hide by the directive  iframe-finish-load
			$scope.showWidgetSpinner();
			currentDocId=doc.DOCUMENT_ID;
			var pathUrl="";
			pathUrl+="&OBJECT_ID="+doc.DOCUMENT_ID;
			pathUrl+="&OBJECT_LABEL="+doc.DOCUMENT_LABEL;
			pathUrl+="&OBJECT_NAME="+doc.DOCUMENT_NAME;
			pathUrl+="&SELECTED_ROLE="+cockpitModule_properties.SELECTED_ROLE

			//load document parameter
			if (doc.objParameter && doc.objParameter.length > 0){
				var docPa={};
				angular.forEach(doc.objParameter,function(param){
					this[param.urlName]=cockpitModule_utilstServices.getDocumentWidgetDriverArray(param.value);
				},docPa)
				var assSel=cockpitModule_widgetSelection.getCurrentSelections(doc.DOCUMENT_LABEL)
				if(assSel!=undefined && assSel.hasOwnProperty(doc.DOCUMENT_LABEL)){
					for(var parName in assSel[doc.DOCUMENT_LABEL]){
						var parV=assSel[doc.DOCUMENT_LABEL][parName];
						if(parV!=undefined){
							var finalP=[];
							angular.forEach(parV,function(item){
								this.push(item.substring(2,item.length-2))
							},finalP)
							docPa[parName.substring(3,parName.length-1)]=finalP.join(",");
						}

					}
				}
				pathUrl+="&COCKPIT_PARAMETER="+encodeURIComponent(JSON.stringify(docPa));
			}
			pathUrl+="&IS_FROM_DOCUMENT_WIDGET=true";

			var tmpUrl = sbiModule_config.externalBasePath+'/servlet/AdapterHTTP?ACTION_NAME=EXECUTE_DOCUMENT_ANGULAR_ACTION&SBI_ENVIRONMENT=DOCBROWSER&IS_SOURCE_DOCUMENT=true&SBI_EXECUTION_ID=null'+pathUrl;
			if(!angular.equals($scope.documentViewerUrl,tmpUrl)){
				$scope.documentViewerUrl=tmpUrl;
			}else{
				$scope.hideWidgetSpinner();
			}
		}
		if(nature == 'init'){
			$timeout(function(){
				$scope.widgetIsInit=true;
				cockpitModule_properties.INITIALIZED_WIDGETS.push($scope.ngModel.id);
			},3000);
		}
	};

	$scope.editWidget=function(index){
		var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				controller: function($scope,finishEdit,sbiModule_translate,model,mdPanelRef,$mdToast){
					  $scope.localModel = {};
					  angular.copy(model,$scope.localModel);
					  $scope.translate=sbiModule_translate;
					  $scope.saveConfiguration=function(){
						  if($scope.localModel.document == undefined){
							$scope.showAction($scope.translate.load('sbi.cockpit.widgets.document.missingdoc'));
							return;

						  }
						  angular.copy($scope.localModel,model);
						  mdPanelRef.close();
						  $scope.$destroy();
						  finishEdit.resolve();

					  };
					  $scope.cancelConfiguration=function(){
						  mdPanelRef.close();
						  $scope.$destroy();
						  finishEdit.reject();

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
				  },
				disableParentScroll: true,
				templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/documentWidget/templates/documentWidgetEditPropertyTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				hasBackdrop: true,
				clickOutsideToClose: false,
				escapeToClose: false,
				focusOnOpen: true,
				preserveScope: true,
				locals: {finishEdit:finishEdit,model:$scope.ngModel},
				onRemoving :function(){
					$scope.refreshWidget();
				}

		};

		$mdPanel.open(config);
		return finishEdit.promise;
	};

	$scope.getDocumentViewerUrl=function(){
		return $scope.documentViewerUrl;
	};
};


//this function register the widget in the cockpitModule_widgetConfigurator factory
addWidgetFunctionality("document",{'initialDimension':{'width':5, 'height':5},'updateble':true,'cliccable':false});

})();