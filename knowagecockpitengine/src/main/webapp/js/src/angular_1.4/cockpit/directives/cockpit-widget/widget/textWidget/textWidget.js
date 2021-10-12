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
.directive('textWidgetTextRender', function ($compile,cockpitModule_utilstServices,cockpitModule_datasetServices,cockpitModule_generalServices, cockpitModule_variableService) {
    return {
        restrict: 'A',
        replace: true,
        css: baseScriptPath+ '/directives/cockpit-widget/widget/textWidget/templates/editorCss.css',
        link: function (scope, ele, attrs) {
            scope.$watch(attrs.textWidgetTextRender, function (html) {
                var model = scope.ngModel;
                scope.ngModel.isReady=(cockpitModule_generalServices.isFromNewCockpit())?true:false;
                if (html && (html.indexOf("$F{") >= 0  || html.indexOf("$P{") >= 0  || html.indexOf("$V{")  >= 0 )){
                	var elems = [];
	                for (var dsLabel in model.datasets){
	                	elems.push(dsLabel);
	                }

	                scope.checkPlaceholders= function(counter, refreshBool,callback){
		                	if(counter == 0 && refreshBool != undefined && refreshBool == true){
		                		html = scope.ngModel.content.text;
		                	}

		                	if (elems.length != 0 && (counter < elems.length)){
		                		// call this only if reference is really contained
		                		if(html.indexOf(elems[counter])>=0){
		                			cockpitModule_datasetServices.substitutePlaceholderValues(html, elems[counter], model).then(function(htmlReturned){
		                				html = htmlReturned;
		                				ele.html(html);
		                				$compile(ele.contents())(scope);
		                				counter++;
		                				scope.checkPlaceholders(counter, null, callback);
		                			},function(error){
		                			});
		                		}
		                		else{
		                			counter++;
		                			scope.checkPlaceholders(counter, null, callback);
		                		}
		                	}else{
		                		html = cockpitModule_utilstServices.getParameterValue(html);
		                    	html = cockpitModule_variableService.getVariablePlaceholders(html);
		                    	ele.html(html);
		                    	$compile(ele.contents())(scope);
		                		scope.ngModel.isReady=true; //view the content replaced
		                		if (callback && typeof callback === "function") {
		                			return callback();
		                		}
		                	}

	                }

	                scope.checkPlaceholders(0);
                }else{
                	html = cockpitModule_utilstServices.getParameterValue(html);
                	html = cockpitModule_variableService.getVariablePlaceholders(html);
                	 ele.html(html);
                     $compile(ele.contents())(scope);
                     scope.ngModel.isReady=true;
                }


            });
        }
    };
})
.directive('cockpitTextWidget',function(cockpitModule_widgetServices,cockpitModule_datasetServices,$mdDialog){
	   return{
		   templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/textWidget/templates/textWidgetTemplate.html',
		   controller: cockpitTextWidgetControllerFunction,
		   compile: function (tElement, tAttrs, transclude) {
                return {
                    pre: function preLink(scope, element, attrs, ctrl, transclud) {
                    	element[0].classList.add("flex");
                    	element[0].classList.add("layout");
                    	element[0].style.overflow="auto"
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

function cockpitTextWidgetControllerFunction($scope,cockpitModule_widgetConfigurator,cockpitModule_generalServices,cockpitModule_properties,cockpitModule_datasetServices,sbiModule_translate,$q,$mdPanel,$timeout,cockpitModule_variableService){

	$scope.property={style:{}};
	$scope.init=function(element,width,height){
		$scope.refreshWidget(null,'init');
	};

	$scope.refresh=function(element,width,height,data, nature){

		var fontSize = 0;
		var textLength = 0;
		var c = document.createElement('canvas');
		var Cctx = c.getContext('2d');
		while (textLength <= width && fontSize <= height) {
			fontSize++;
			Cctx.font = fontSize + 'px Arial';
			var mesTxt = Cctx.measureText($scope.ngModel.content.text);
			textLength = mesTxt.width;
			$scope.safeApply();
		}

		$scope.property.style["font-size"]= fontSize+"px";
		$scope.property.style["line-height"]= fontSize+"px";

		if($scope.checkPlaceholders) $scope.checkPlaceholders(0, true);

		if(nature == 'init'){
			$timeout(function(){
				$scope.widgetIsInit=true;
				cockpitModule_properties.INITIALIZED_WIDGETS.push($scope.ngModel.id);
			},500);
		}


	};

	$scope.editWidget=function(index){

		var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				controller: function($scope,finishEdit,sbiModule_translate,model,mdPanelRef,$mdToast){

			    	  $scope.getcssUrl = function(){
			  	  		return cockpitModule_generalServices.getTemplateUrl('textWidget','editor','.css');
			  	  	}

			    	  $scope.localModel = angular.copy(model);
			    	  if(!$scope.localModel.style) $scope.localModel.style = {};
			    	  $scope.translate=sbiModule_translate;

			    	  // trick to have drawn the text again when going into edit (could be altyered dataset, filters)
			    	  //otherwiser watch on renderer does not start
			    	  if($scope.localModel.content.text != undefined){
			    		  $scope.localModel.content.text+=' ';
			    	  }

			    	  $scope.editorConfig = {
			    	            sanitize: false,
			    	            toolbar: [
			    	            { name: 'basicStyling', items: ['bold', 'italic', 'underline', 'strikethrough', 'subscript', 'superscript', '-', 'leftAlign', 'centerAlign', 'rightAlign', 'blockJustify', '-'] },
			    	            { name: 'paragraph', items: ['orderedList', 'unorderedList', 'outdent', 'indent', '-'] },
			    	            { name: 'colors', items: ['fontColor', 'backgroundColor', '-'] },
			    	            { name: 'styling', items: ['font', 'size', 'format'] }
			    	            ]
			    	  };

			    	  $scope.handleEvent=function(event, arg1){
			    		  if(event=='datasetChanged'){
							  changeDatasetFunction(arg1);
						  }
					  }

			    	  $scope.formatPattern = ['#.###','#,###','#.###,##','#,###.##'];

			    	  var changeDatasetFunction=function(dsIdArray){
			    		  if(dsIdArray != undefined){
			    			  // clean datasets
			    			  $scope.localModel.datasets = {};
			    			  $scope.localModel.dataset = {dsId : []};

			    			  for(var i = 0; i< dsIdArray.length; i++){
			    				  var dsId = dsIdArray[i];
			    				  var ds = cockpitModule_datasetServices.getDatasetById(dsId);
			    				  if(ds){
			    					  $scope.localModel.dataset.dsId.push(dsId);
			    					  $scope.localModel.datasets[ds.label] = ds.metadata.fieldsMeta;
			    					  $scope.localModel.viewDatasetsDett = {};
			    					  $scope.localModel.viewDatasetsDett[ds.label] = false;
			    					  $scope.localModel.functions=['SUM', 'AVG', 'MIN', 'MAX','COUNT', 'COUNT_DISTINCT'];
			    					  $scope.localModel.viewDatasets = true;
			    				  }
			    			  }
			    		  }
			    	  }

			    	  $scope.saveConfiguration=function(){
			    		  if($scope.localModel.content.text == undefined || $scope.localModel.content.text ==""){
			  				$scope.showAction($scope.translate.load('sbi.cockpit.widget.text.missingtext'));
			    			  return;
			    		  }
			    		  angular.copy($scope.localModel,model);
			    		  mdPanelRef.close();
			    		  $scope.$destroy();
			    		  finishEdit.resolve();

			    	  }
			    	  $scope.cancelConfiguration=function(){
			    		  mdPanelRef.close();
			    		  $scope.$destroy();
			    		  finishEdit.reject();

			    	  }
			    	  $scope.showAction = function(text) {
			  			var toast = $mdToast.simple()
			  			.content(text)
			  			.action('OK')
			  			.highlightAction(false)
			  			.hideDelay(3000)
			  			.position('top')

			  			$mdToast.show(toast).then(function(response) {
			  				if ( response == 'ok' ) {}
			  			});
			  		};
			      },
				disableParentScroll: true,
				templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/textWidget/templates/textWidgetEditPropertyTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				hasBackdrop: true,
				clickOutsideToClose: false,
				escapeToClose: false,
				focusOnOpen: true,
				preserveScope: false,
				locals: {finishEdit:finishEdit,model:$scope.ngModel},

		};

		$mdPanel.open(config);
		return finishEdit.promise;


	}


	$scope.getOptions =function(){
		var obj = {};

		obj["type"] = $scope.ngModel.type;

		return obj;

	}

	$scope.getPerWidgetDatasetIds = function() {
		if(typeof $scope.ngModel.dataset != 'undefined'){
			if(Array.isArray($scope.ngModel.dataset.dsId)){
				return $scope.ngModel.dataset.dsId;
			}else return new Array($scope.ngModel.dataset.dsId);
		}else return [];
	}

};


//this function register the widget in the cockpitModule_widgetConfigurator factory
addWidgetFunctionality("text",{'initialDimension':{'width':5, 'height':5},'updateble':true,'cliccable':false});

})();
