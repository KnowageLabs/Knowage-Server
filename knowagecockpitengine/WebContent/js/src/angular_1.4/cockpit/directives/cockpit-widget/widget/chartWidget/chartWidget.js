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

/*
 * This directive acts like an Ext.form.FormPanel
 * Create an IFrame with an URL called by posting parameters
 * 
 * */
.directive('postIframe',function(){
	var i = 0;
	return {
		restrict: 'E',
	    templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/chartWidget/templates/postIframe.html',
	    link: function(scope, elem, attrs) {
	    	var genId = i++;
	    	scope.iframeName = attrs.id + "_frameName" + genId;
	    	scope.formId = attrs.id + "_formId" + genId;
	    	scope.iframeId = attrs.iframeId ? attrs.iframeId : (attrs.id + "_iframeId" + genId);
	    	scope.iframeClass = attrs.class;
	    },
	    controller: function($scope, $element){
	    	$scope.updateAction = function(actionUrl){
	    		$scope.actionUrl = actionUrl;
	    	};
	    	$scope.updateParameters = function(parameters){
	    		$scope.formParameters = parameters;
	    	};
	    	$scope.updateContent = function(actionUrl, parameters, nature, width, height){
	    		var iframe = $element.find('iframe')[0];
	    		if(nature != 'refresh'){
	    			
	    			if(actionUrl){
	    				$scope.updateAction(actionUrl);
	    			}
	    			if(parameters){
	    				$scope.updateParameters(parameters);
	    			}
	    			var formId = $element[0].id + "_formId";
	    			var formAction = $scope.actionUrl;
	    			var form = angular.element('<form id="'+formId+'" action="'+formAction+'" method="POST" style="display:none;"></form>');
	    			for(var x=0;x<$scope.formParameters.length;x++){
	    				var param = $scope.formParameters[x];
	    				form.append('<input type="hidden" name="' + param.name + '" value=\'' + JSON.stringify(param.value) + '\'>');
	    			}
	    			var doc = iframe.contentWindow.document;
	    			doc.open();
	    			doc.write(form.wrap(doc.createElement('div')).parent().html());
	    			doc.close();
	    			doc.getElementById(formId).submit();
	    			$scope.showWidgetSpinner();
	    		}
	    		if(height != undefined){
	    			iframe.style="height:"+height+"px;min-height:"+height+"px";
	    		}
	    	};
	    	
	    	
	    }
	};
})
.directive('cockpitChartWidget',function(cockpitModule_widgetServices,$mdDialog,buildParametersForExecution,$compile){
	return{
		   templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/chartWidget/templates/chartWidgetTemplate.html',
		   controller: cockpitChartWidgetControllerFunction,
		   compile: function (tElement, tAttrs, transclude) {
                return {
                    pre: function preLink(scope, element, attrs, ctrl, transclud) {
                    	element[0].classList.add("flex");
                    	element[0].classList.add("layout");
                    },
                    post: function postLink(scope, element, attrs, ctrl, transclud) {
                    	scope.postIframe = element.find('post-iframe')[0];
                    	//init the widget
                    	element.ready(function () {
                    		scope.initWidget();
                    	});
                    }
                };
		   }
	}
})
.factory('buildParametersForExecution',function(sbiModule_user, sbiModule_config){
	var formAction = function(service){
		return '/' + sbiModule_config.chartEngineContextName 
		+ "/api/1.0/pages/" + service
		+ "?SBICONTEXT=" + sbiModule_config.externalBasePath
		+ "&SBI_HOST=localhost"
		+ "&SBI_LANGUAGE=" + sbiModule_config.curr_language
		+ "&SBI_COUNTRY=" + sbiModule_config.curr_country
		+ "&user_id=" + sbiModule_user.userId;
	}
	var formEditAction =  formAction('edit_cockpit');
	var formExecAction =  formAction('execute_cockpit');
	
	var ret = function(widgetData, newData, editMode){
		var ret = {};
		if(editMode){
			ret.formAction = formEditAction;
		}else{
			ret.formAction = formExecAction;
		}
		if(widgetData && widgetData.chartTemplate && widgetData.chartTemplate.CHART){
			widgetData.chartTemplate.CHART.outcomingEventsEnabled = true;
		}
		if(newData){
			widgetData.jsonData = newData;
		}
		ret.formParameters = [{name: 'widgetData', value: {"widgetData": widgetData} }];
		return ret;
	};
	return {
		edit: function(widgetData, newData){
			return ret(widgetData, newData, true);
		},
		execute: function(widgetData, newData){
			return ret(widgetData, newData);
		}
	}
});

function cockpitChartWidgetControllerFunction($scope,cockpitModule_widgetSelection,cockpitModule_datasetServices,cockpitModule_widgetConfigurator,$q,$mdPanel,sbiModule_restServices,$httpParamSerializerJQLike,sbiModule_config,buildParametersForExecution,$mdToast){
	$scope.property={style:{}};
	$scope.selectedTab = {'tab' : 0};
	$scope.init=function(element,width,height){
		$scope.refreshWidget(undefined,'init');
	};
	
	$scope.refresh=function(element,width,height,data,nature){
		var widgetData = angular.copy($scope.ngModel.content);
		$scope.postIframe = element.find('post-iframe').scope();
		var execPar = buildParametersForExecution.execute(widgetData,data);
		$scope.postIframe.updateContent(execPar.formAction, execPar.formParameters,nature,width,height);
		
	};
	
	$scope.editWidget=function(index){
		var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				controller: function($scope,sbiModule_translate,model,mdPanelRef,doRefresh){
					  $scope.translate=sbiModule_translate;
					  $scope.confSpinner=false;
					  $scope.localModel = angular.copy(model.content);
					  $scope.datasetChecked = $scope.localModel.datasetId != undefined ? 1 : 0;
			    	  $scope.changeDatasetFunction=function(dsId){
			    		  $scope.confChecked = 0;
			    		  var ds = cockpitModule_datasetServices.getDatasetById(dsId);
			    		  if(ds){
			    			  if(ds.id.dsId != $scope.localModel.datasetId && ds.id.dsLabel != $scope.localModel.datasetLabel){
			    				  // Clearing chart configurations
			    				  delete $scope.localModel.aggregations;
			    				  delete $scope.localModel.chartTemplate;
			    				  delete $scope.localModel.columnSelectedOfDataset;
			    				  $scope.confChecked = 0;
			    			  }
			    			  $scope.localModel.datasetLabel = ds.label;
			    		  }
			    		  $scope.datasetChecked = 1;
			    	  }
			    	  $scope.checkConfiguration=function(){
			    		  var extWindow = document.getElementById("chartConfigurationIframe").contentWindow;
			    		  if(extWindow.Sbi!=undefined){
			    			  var designer = extWindow.Sbi.chart.designer.Designer;
			    			  var error = designer.validateTemplate(true);
			    			  if(error==false){
				    			  $scope.localModel.chartTemplate = designer.exportAsJson(true);
			    				  setAggregationsOnChartEngine($scope.localModel);
			    				  $scope.confChecked = 1;
			    			  }else{
			    				  $scope.confChecked = 0;
			    				  $scope.showAction(error);
			    			  }
			    			  return error;
			    		  }
			    		  return false;
			    	  }
			    	  $scope.saveConfiguration=function(){
			    		  if($scope.datasetChecked == 0){
			    			  // Warning: Please select a dataset
			    			  $scope.showAction($scope.translate.load('sbi.cockpit.table.missingdataset'));
			    		  }else{
			    			  if($scope.checkConfiguration() == false){
			    				  if($scope.confChecked == 0){
			    					  // Warning: Please configure chart
			    					  $scope.showAction($scope.translate.load('sbi.cockpit.widgets.chartengine.conf.missing'));
			    				  }else{
			    					  
			    					  $scope.localModel.wtype = "chart";
			    					  $scope.localModel.designer = "Chart Engine Designer";
			    					  
			    					  angular.copy($scope.localModel, model.content);
			    					  model.dataset = {dsId: $scope.localModel.datasetId, dsLabel: $scope.localModel.datasetLabel};
			    					  
			    					  mdPanelRef.close();
			    					  $scope.$destroy();
			    					  doRefresh(undefined,'init');
			    					  finishEdit.resolve();
			    				  }
			    			  }
			    		  }
			    	  }
			    	  $scope.cancelConfiguration=function(){
			    		  mdPanelRef.close();
			    		  $scope.$destroy();
			    		  finishEdit.reject();
			    	  }
			    	  $scope.showChartConfiguration=function(){
				    	  var widgetData = angular.extend({"datasetLabel":$scope.localModel.datasetLabel||''},$scope.localModel);
				    	  var execPar = buildParametersForExecution.edit(widgetData);
				    	  angular.element(document.getElementById("chartConfigurationIframe")).scope()
				  		  .updateContent(execPar.formAction, execPar.formParameters, 'init');
			    	  }
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
			  		  }
			    	  $scope.showWidgetSpinner=function(){
			    		  $scope.confSpinner=true;
			    	  }
			    	  $scope.hideWidgetSpinner=function(){
			    		  $scope.confSpinner=false;
			    		  $scope.safeApply();
			    	  }
			    	  $scope.finishLoadingIframe=function(){
			    		  $scope.hideWidgetSpinner();
			    		  $scope.safeApply();
			    	  }
			    	  $scope.safeApply=function(){
			    			if ($scope.$root.$$phase != '$apply' && $scope.$root.$$phase !='$digest') {
			    				$scope.$apply();
			    			}
			    		}
			      },
				disableParentScroll: true,
				templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/chartWidget/templates/chartWidgetEditPropertyTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				hasBackdrop: true,
				clickOutsideToClose: false,
				escapeToClose: false,
				focusOnOpen: true,
				preserveScope: true,
				locals: {finishEdit:finishEdit, model:$scope.ngModel, doRefresh:$scope.refreshWidget}
		};
		$mdPanel.open(config);
		return finishEdit.promise;
	}

	$scope.reloadWidgetsByChartEvent = function(event){
		var columnValue = event.point.name;
		var columnName = $scope.ngModel.content.chartTemplate.CHART.VALUES.CATEGORY.name
		$scope.doSelection(columnName,columnValue);
	}
	
	$scope.finishLoadingIframe=function(){
			$scope.hideWidgetSpinner();
	}
};
function setAggregationsOnChartEngine(wconf){
	
	var aggregations = [];
	var chartTemplate = wconf.chartTemplate;
	if(chartTemplate && chartTemplate.CHART && chartTemplate.CHART.VALUES) {
		
		if(chartTemplate.CHART.VALUES.SERIE) {
		
			var chartSeries = chartTemplate.CHART.VALUES.SERIE;
			
			for(var i = 0; i < chartSeries.length; i++){
				
				var obj = {};
//				obj['id'] = chartSeries[i].name;
				obj['name'] = chartSeries[i].column;
				obj['aggregationSelected'] = chartSeries[i].groupingFunction ? chartSeries[i].groupingFunction : 'SUM';
				obj['alias'] = obj.name + '_' + obj.aggregationSelected;
				obj['aliasToShow'] = obj.name;
//				obj['orderType'] = chartSeries[i].orderType;
				obj['fieldType'] = "MEASURE";
				aggregations.push(obj);					
			}
			
		}
		
		if(chartTemplate.CHART.VALUES.CATEGORY){
			
			var chartCategory= chartTemplate.CHART.VALUES.CATEGORY;
			
			if(Array.isArray(chartCategory)){
				for(var i = 0; i < chartCategory.length; i++){
					
					var obj = {};
//					obj['id'] = chartCategory[i].name;
					obj['name'] = chartCategory[i].column;
					obj['alias'] = chartCategory[i].name;
					obj['aliasToShow'] = category.alias;
//					obj['orderColumn'] = chartCategory[i].orderColumn;
//					obj['orderType'] = chartCategory[i].orderType;
					obj['fieldType'] = "ATTRIBUTE";
					aggregations.push(obj);
				}
			} else {
				var obj = {};
//				obj['id'] = chartCategory.name;
				obj['name'] = chartCategory.column;
				obj['alias'] = chartCategory.name;
				obj['aliasToShow'] = chartCategory.alias;
				obj['fieldType'] = "ATTRIBUTE";
				
				/**
				 * Set the category's ordering column and its ordering type, if they
				 * are set for the first category of the chart document.
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
//				obj['orderColumn'] = chartCategory.orderColumn;
//				obj['orderType'] = chartCategory.orderType;
				
				aggregations.push(obj);
			};
			
		}
	}
	wconf.columnSelectedOfDataset = aggregations;
}

//this function register the widget in the cockpitModule_widgetConfigurator factory
addWidgetFunctionality("chart",{'initialDimension':{'width':20, 'height':20}});

})();