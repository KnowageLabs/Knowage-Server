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
.directive('cockpitToolbar',function(){
	   return{
		   templateUrl: baseScriptPath+ '/directives/cockpit-toolbar/templates/cockpitToolbar.html',
		   controller: cockpitToolbarControllerFunction,
		   scope: {
			   config: '='
		   	},
		   	compile: function (tElement, tAttrs, transclude) {
                return {
                    pre: function preLink(scope, element, attrs, ctrl, transclud) {
                    },
                    post: function postLink(scope, element, attrs, ctrl, transclud) {
                    }
                };
		   	}
	   }
});

function cockpitToolbarControllerFunction($scope,cockpitModule_widgetServices,cockpitModule_properties,cockpitModule_template,$mdDialog,sbiModule_translate,sbiModule_restServices,cockpitModule_gridsterOptions,$mdPanel,cockpitModule_widgetConfigurator,$mdToast,cockpitModule_generalServices,cockpitModule_widgetSelection,$rootScope){
	$scope.translate = sbiModule_translate;
	$scope.cockpitModule_properties=cockpitModule_properties;
	$scope.cockpitModule_template=cockpitModule_template;
	$scope.cockpitModule_widgetServices=cockpitModule_widgetServices;

	$scope.openGeneralConfigurationDialog=function(){
		cockpitModule_generalServices.openGeneralConfiguration();
	}

	$scope.openDataConfigurationDialog=function(){
		cockpitModule_generalServices.openDataConfiguration();
	}

	$scope.fabSpeed = {
			isOpen : false
	}


	$scope.saveCockpit=function(){
		var haveSel=false;
		for(var i=0;i<cockpitModule_template.configuration.aggregations.length;i++){
			if(Object.keys(cockpitModule_template.configuration.aggregations[i].selection).length>0){
				haveSel=true;
				break;
			}
		}
		if(Object.keys(cockpitModule_template.configuration.filters).length>0){
			haveSel=true;
		}
		if(haveSel){
			var confirm = $mdDialog.confirm()
			.title(sbiModule_translate.load('sbi.cockpit.widgets.save.keepselections'))
			.textContent('')
			.ariaLabel('save cockpit')
			.ok(sbiModule_translate.load('sbi.qbe.messagewin.yes'))
			.cancel(sbiModule_translate.load('sbi.qbe.messagewin.no'));
			$mdDialog.show(confirm).then(function() {
				cockpitModule_generalServices.saveCockpit();
			}, function() {
				for(var i=0;i<cockpitModule_template.configuration.aggregations.length;i++){
					cockpitModule_template.configuration.aggregations[i].selection = {};
				}
				cockpitModule_template.configuration.filters={};
				cockpitModule_generalServices.saveCockpit();
			});
		}else{
			cockpitModule_generalServices.saveCockpit();
		}
	};

	$scope.cleanCache = function(){
		cockpitModule_generalServices.cleanCache();
	}

	$scope.openSelections = function(){
		$mdDialog.show({
		      templateUrl: baseScriptPath+ '/directives/cockpit-toolbar/templates/selectionsList.html',
		      parent: angular.element(document.body),
		      clickOutsideToClose:true,
		      escapeToClose :true,
	          preserveScope: true,
		      fullscreen: true,
		      controller: cockpitSelectionControllerFunction

		});
	}

	$scope.addWidget=function(){
		$mdDialog.show({
		      templateUrl: baseScriptPath+ '/directives/cockpit-toolbar/templates/addWidget.html',
		      parent: angular.element(document.body),
		      clickOutsideToClose:true,
		      escapeToClose :true,
	          preserveScope: true,
		      fullscreen: true,
		      controller: function($scope,sbiModule_translate,cockpitModule_template,cockpitModule_properties,cockpitModule_widgetServices){
		    	  $scope.translate=sbiModule_translate;
		    	  $scope.addWidget=function(type){
		    		  var tmpWidget={
		    				  id:(new Date()).getTime(),
		    				  sizeX	:6,
		    				  sizeY:6,
		    				  content:{name:"new Widget"},
		    				  type:type,
		    				  isNew : true,
		    				  updateble : true,
		    				  cliccable : true
		    		  }
		    		  if(cockpitModule_widgetConfigurator[type].initialDimension !=undefined){
		    			  if(cockpitModule_widgetConfigurator[type].initialDimension.width != undefined){
			    			  tmpWidget.sizeX = cockpitModule_widgetConfigurator[type].initialDimension.width;

		    			  }
		    			  if(cockpitModule_widgetConfigurator[type].initialDimension.height != undefined){
			    			  tmpWidget.sizeY = cockpitModule_widgetConfigurator[type].initialDimension.height;

		    			  }
		    		  }
		    		  cockpitModule_widgetServices.addWidget(cockpitModule_properties.CURRENT_SHEET,tmpWidget);
		    		  $mdDialog.hide();

		    	  };

		    	  //to-do recuperare questa informazione dal backend
		    	  $scope.widgetType=[
		    	         		{
		    	        			name:"Text",
		    	        			description: $scope.translate.load("sbi.cockpit.editor.newwidget.description.text"),
		    	        			tags : ["text"],
		    	        			img : "1.png",
		    	        			class: "fa fa-font",
		    	        			type : "text"
		    	        		},{
		    	        			name:"Image",
		    	        			description: $scope.translate.load("sbi.cockpit.editor.newwidget.description.image"),
		    	        			tags : ["image"],
		    	        			img : "2.png",
		    	        			class: "fa fa-picture-o",
		    	        			type : "image"
		    	        		},{
		    	        			name:"Chart",
		    	        			description: $scope.translate.load("sbi.cockpit.editor.newwidget.description.chart"),
		    	        			tags : ["chart"],
		    	        			img : "4.png",
		    	        			class: "fa fa-bar-chart",
		    	        			type : "chart"
		    	        		},{
		    	        			name:"Table",
		    	        			description: $scope.translate.load("sbi.cockpit.editor.newwidget.description.table"),
		    	        			tags : ["table"],
		    	        			img : "5.png",
		    	        			class: "fa fa-table",
		    	        			type : "table"
		    	        		},{
							    	name:"Cross Table",
							    	description: $scope.translate.load("sbi.cockpit.editor.newwidget.description.cross"),
							    	tags : ["table","pivot","cross"],
							    	img : "6.png",
							    	class: "fa fa-table",
							    	type : "static-pivot-table"
							      }
							      ,{
							    	  name:"Document",
							    	  description: $scope.translate.load("sbi.cockpit.editor.newwidget.description.document"),
							    	  tags : ["document","datasource"],
							    	  img : "7.png",
							    	  class: "fa fa-file",
							    	  type : "document"
							      },{
							    	  name:"Active Selections",
							    	  description: $scope.translate.load("sbi.cockpit.editor.newwidget.description.selection"),
							    	  tags : ["selection"],
							    	  class: "fa fa-check-square-o",
							    	  img : "8.png",
							    	  type : "selection"
							      },{
							    	  name:"Selector",
							    	  description: $scope.translate.load("sbi.cockpit.editor.newwidget.description.selector"),
							    	  tags : ["selector"],
							    	  class: "fa fa-caret-square-o-down",
							    	  img : "9.png",
							    	  type : "selector"
							      }
		    	        	];

		    	  $scope.saveConfiguration=function(){
		    		  $mdDialog.hide();
		    	  }
		    	  $scope.cancelConfiguration=function(){
		    		  $mdDialog.cancel();
		    	  }


		      }
		    })
	};

	$scope.closeNewCockpit=function(){
		cockpitModule_generalServices.closeNewCockpit();
	}
	$scope.isFromNewCockpit= cockpitModule_generalServices.isFromNewCockpit();

//	$scope.toggleEditMode=function(){
//		cockpitModule_gridsterOptions.draggable.enabled=cockpitModule_properties.EDIT_MODE;
//		cockpitModule_gridsterOptions.resizable.enabled=cockpitModule_properties.EDIT_MODE;
//	}
};



function cockpitSelectionControllerFunction($scope,cockpitModule_template,cockpitModule_datasetServices,$mdDialog,sbiModule_translate,$q,sbiModule_messaging,cockpitModule_documentServices,cockpitModule_widgetSelection,cockpitModule_properties){
	$scope.selection = [];
	$scope.translate = sbiModule_translate;
	$scope.tmpSelection = [];


	angular.copy(cockpitModule_template.configuration.aggregations,$scope.tmpSelection);
	$scope.tmpFilters = {};
	angular.copy(cockpitModule_template.configuration.filters,$scope.tmpFilters);

	$scope.filterForInitialSelection=function(obj){
		if(!cockpitModule_properties.EDIT_MODE){
			for(var i=0;i<cockpitModule_properties.STARTING_SELECTIONS.length;i++){
				if(angular.equals(cockpitModule_properties.STARTING_SELECTIONS[i],obj)){
					return true;
				}
			}
		}
		return false;
	}
	$scope.filterForInitialFilter=function(obj){
		if(!cockpitModule_properties.EDIT_MODE){
			for(var i=0;i<cockpitModule_properties.STARTING_FILTERS.length;i++){
				if(angular.equals(cockpitModule_properties.STARTING_FILTERS[i],obj)){
					return true;
				}
			}
		}
		return false;
	}

	if($scope.tmpSelection.length >0){
		for(var i=0;i<$scope.tmpSelection.length;i++){
			var selection = $scope.tmpSelection[i].selection;
			for(var key in selection){
				var string = key.split(".");

				var obj = {
						ds : string[0],
						columnName : string[1],
						value : selection[key],
						aggregated:true
				};
				if(!$scope.filterForInitialSelection(obj)){
					$scope.selection.push(obj);
				}
			}
		}
	}


	for(var ds in $scope.tmpFilters){
		var currentDs = cockpitModule_datasetServices.getDatasetByLabel(ds).metadata.fieldsMeta;
		for(var col in $scope.tmpFilters[ds]){
			var aliasColumnName;
			for(var a in cockpitModule_template.configuration.aliases){
				if(cockpitModule_template.configuration.aliases[a].column == col){
					aliasColumnName = cockpitModule_template.configuration.aliases[a].alias;
				}
			}
			var tmpObj={
					ds :ds,
					columnName : aliasColumnName,
					column	: col,
					value : $scope.tmpFilters[ds][col],
					aggregated:false
			}

			if(!$scope.filterForInitialFilter(tmpObj)){
				$scope.selection.push(tmpObj);
			}
		}
	}

	$scope.columnTableSelection =[
	                              {
	                            	  label:"Dataset",
	                            	  name:"ds",

	                            	  hideTooltip:true
	                              },
	                              {
	                            	  label:"Column Name",
	                            	  name:"columnName",

	                            	  hideTooltip:true
	                              },
	                              ,
	                              {
	                            	  label:"Values",
	                            	  name:"value",

	                            	  hideTooltip:true
	                              }
	                              ];


	$scope.actionsOfSelectionColumns = [

	                                    {
	                                    	icon:'fa fa-trash iconAlignFix' ,
	                                    	action : function(item,event) {
	                                    		$scope.deleteSelection(item);

	                                    	}
	                                    }
	                                    ];

	$scope.deleteSelection=function(item){
		if(item.aggregated){
			var key = item.ds + "." + item.columnName;

			for(var i=0;i<$scope.tmpSelection.length;i++){
				if($scope.tmpSelection[i].datasets.indexOf(item.ds) !=-1){
					var selection  = $scope.tmpSelection[i].selection;
					delete selection[key];
				}
			}

			var index=$scope.selection.indexOf(item);
			$scope.selection.splice(index,1);
		}else{
			delete $scope.tmpFilters[item.ds][item.column];
			if(Object.keys($scope.tmpFilters[item.ds]).length==0){
				delete $scope.tmpFilters[item.ds];
			}
			var index=$scope.selection.indexOf(item);
			$scope.selection.splice(index,1);
		}

	}

	$scope.clearAllSelection = function(){
		while($scope.selection.length!=0){
			$scope.deleteSelection($scope.selection[0]);
		}
	}

	$scope.cancelConfiguration=function(){
		$mdDialog.cancel();
	}

	$scope.saveConfiguration = function(){
		var reloadAss=false;
		var reloadFilt=[];
		  if(!angular.equals($scope.tmpSelection,cockpitModule_template.configuration.aggregations )){
	  		  angular.copy($scope.tmpSelection,cockpitModule_template.configuration.aggregations);
	  		 reloadAss=true;
		  }
		  if(!angular.equals($scope.tmpFilters,cockpitModule_template.configuration.filters )){
			  angular.forEach(cockpitModule_template.configuration.filters,function(val,dsLabel){
				  if($scope.tmpFilters[dsLabel]==undefined || !angular.equals($scope.tmpFilters[dsLabel],val)){
					  reloadFilt.push(dsLabel)
				  }
			  })
			  angular.copy($scope.tmpFilters,cockpitModule_template.configuration.filters);
		  }
		  if(reloadAss){
			  cockpitModule_widgetSelection.getAssociations(true);
		  }
		  if(!reloadAss && reloadFilt.length!=0){
			  cockpitModule_widgetSelection.refreshAllWidgetWhithSameDataset(reloadFilt);
		  }

		  var hs=false;
		  for(var i=0;i<$scope.tmpSelection.length;i++){
				if(Object.keys($scope.tmpSelection[i].selection).length>0){
					hs= true;
					break;
				}
			}

		  if(hs==false && Object.keys($scope.tmpFilters).length==0 ){
			  cockpitModule_properties.HAVE_SELECTIONS_OR_FILTERS=false;
		  }

		$mdDialog.cancel();
	}

}
})();