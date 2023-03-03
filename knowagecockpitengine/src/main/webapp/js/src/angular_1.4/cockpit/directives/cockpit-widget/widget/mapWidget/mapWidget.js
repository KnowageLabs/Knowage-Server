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
(function() {
	angular
		.module('cockpitModule')
		.filter('i18n', function(sbiModule_i18n) {
			return function(label) {
				return sbiModule_i18n.getI18n(label);
			}
		})
		.directive('draggable', ['$document', function($document) {
			return {
				restrict: 'A',
				link: function(scope, elm, attrs) {
					var startX, startY, initialMouseX, initialMouseY;

					var draggable = scope.$eval(attrs.draggable);
					
					if (draggable && draggable.position === "drag") {
						var currPosition = draggable.coordinates = draggable.coordinates || [150,10];
						
						elm.css({
							top:  currPosition[0],
							left: currPosition[1]
						});
						
						elm.bind('mouseover', function($event) {
							elm.css({ cursor: "grab" });
						});
	
						elm.bind('mouseout', function($event) {
							elm.css({ cursor: "unset" });
						});
	
						elm.bind('mousedown', function($event) {
							startX = elm.prop('offsetLeft');
							startY = elm.prop('offsetTop');
							initialMouseX = $event.clientX;
							initialMouseY = $event.clientY;
							$document.bind('mousemove', mousemove);
							$document.bind('mouseup', mouseup);
							return false;
						});
	
						function mousemove($event) {
							var dx = $event.clientX - initialMouseX;
							var dy = $event.clientY - initialMouseY;
							
							draggable.coordinates[0] = startY + dy;
							draggable.coordinates[1] = startX + dx;
							
							var parenteElementBoundingRect = elm[0].previousElementSibling.getBoundingClientRect();
							var parentWidth = parenteElementBoundingRect.width;
							var parentHeight = parenteElementBoundingRect.height;
							
							var subElementBoundingRect = elm[0].querySelector(".mapWidgetLegend:not(.ng-hide)").getBoundingClientRect();
							var legendWidth = subElementBoundingRect.width;
							var legendHeight = subElementBoundingRect.height;
							
							draggable.coordinates[0] = Math.max(draggable.coordinates[0], 0 + legendHeight);
							draggable.coordinates[1] = Math.max(draggable.coordinates[1], 0);
							
							draggable.coordinates[0] = Math.min(draggable.coordinates[0], parentHeight);
							draggable.coordinates[1] = Math.min(draggable.coordinates[1], parentWidth  - legendWidth);
							
							elm.css({
								top:  draggable.coordinates[0],
								left: draggable.coordinates[1]
							});
							return false;
						}
	
						function mouseup() {
							$document.unbind('mousemove', mousemove);
							$document.unbind('mouseup', mouseup);
						}
					}
				}
			};
		}])
		.directive('mapWidgetLabelPanel', ['$document', function($document) {
			return {
				restrict: 'E',
				scope: false,
				replace: true,
				link: function(scope, elm, attrs) {
					scope.isDraggable = function() {
						if (scope.ngModel.style.legend.position === "drag") {
							return scope.ngModel.style.legend.coordinates;
						} else {
							return false;
						}
					}
				},
				templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/mapWidget/templates/mapWidgetLegendPanel.html'
			};
		}])
		.directive('cockpitMapWidget',function(){
			return{
				templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/mapWidget/templates/mapWidgetTemplate.html',
				controller: cockpitMapWidgetControllerFunction,
				compile: function (tElement, tAttrs, transclude) {
					return {
						pre: function preLink(scope, element, attrs, ctrl, transclud) {
							//defines new id for clone widget action
							if (scope.ngModel.content.mapId){
								scope.ngModel.content.mapId =  'map-' + scope.ngModel.id;
							}
						},
						post: function postLink(scope, element, attrs, ctrl, transclud) {
							element.ready(function () {
								scope.initWidget();
								scope.initializeTemplate();
							});
						}
					};
				}
			}
		})
		// From https://github.com/angular/material/issues/4987
		.directive('evalAttrAsExpr', function evalAttrAsExpr() {
			/*
			 * This directive is a workaround for the md-component-id attribute of the
			 * mdSidenav directive.
			 *
			 * The mdSidenav directive, in its controller function, registers the element
			 * using the md-component-id attribute. If we need this value to be an
			 * expression to be evaluated in the scope, we can't do
			 *
			 * <md-sidenav md-component-id="{{ expr }}" [...]>
			 *
			 * because the curly braces are replaced in a subsequent stage. To work around
			 * this, this directive replace the value of md-component-id with the value of
			 * that expression in the scope. So the previous example becomes
			 *
			 * <md-sidenav md-component-id="expr" eval-attr-as-expr="mdComponentId" [...]>
			 */
			return {
				restrict: 'A',
				controller: function($scope, $element, $attrs) {
					var attrToEval = $attrs.evalAttrAsExpr;
					$attrs[attrToEval] = $scope.$eval($attrs[attrToEval]);
				},
				priority: 9999
			};
		})

	function cockpitMapWidgetControllerFunction(
			$scope,
			$mdDialog,
			$mdToast,
			$timeout,
			$interval,
			$mdPanel,
			$mdSidenav,
			$q,
			$sce,
			$filter,
			$location,
			sbiModule_translate,
			sbiModule_messaging,
			sbiModule_restServices,
			sbiModule_config,
			sbiModule_i18n,
			cockpitModule_mapServices,
			cockpitModule_mapThematizerServices,
			cockpitModule_datasetServices,
			cockpitModule_generalServices,
			cockpitModule_widgetConfigurator,
			cockpitModule_widgetServices,
			cockpitModule_widgetSelection,
			cockpitModule_properties){

		//ol objects
		$scope.popupContainer; 		//popup detail
		$scope.closer; 				//popup detail closer icon
		$scope.tooltipContainer;
		$scope.layers = [];  		//layers with features
		$scope.values = {};  		//layers with values
		$scope.savedValues = {};
		$scope.configs = {}; 		//layers with configuration
		$scope.columnsConfig = {} 	//layers with just columns definition
		$scope.optionSidenavId = "optionSidenav-" + Math.random(); // random id for sidenav id
		$scope.layerVisibility = [];
		$scope.exploded = {}; // is heatp/cluster exploded?
		$scope.zoomControl = undefined; // Zoom control on map
		$scope.scaleControl = undefined; // Scale indicator
		$scope.mouseWheelZoomInteraction = undefined; // Manage the mouse wheel on map
		$scope.isShowLegend = true; //legend is on by default
		$scope.i18n = sbiModule_i18n;
		$scope.dataSetStats = {};

		$scope.i18n.loadI18nMap();

		$scope.init = function(element,width,height) {

			// Prevent errors about $digest
			$timeout(function() {
				$scope.initializeTemplate();
				$scope.createMap();
				$scope.addAllLayers();
				$scope.setMapSize();
			});
		};

		$scope.realTimeSelections = cockpitModule_widgetServices.realtimeSelections;
		//set a watcher on a variable that can contains the associative selections for realtime dataset
		var realtimeSelectionsWatcher = $scope.$watchCollection('realTimeSelections',function(newValue,oldValue,scope){
			if(newValue != oldValue){
				if(scope.ngModel && scope.ngModel.dataset && scope.ngModel.dataset.dsId){
					var dataset = cockpitModule_datasetServices.getDatasetById(scope.ngModel.dataset.dsId);
					if(dataset.isRealtime && dataset.useCache){
						if(cockpitModule_properties.DS_IN_CACHE.indexOf(dataset.label)==-1){
							cockpitModule_properties.DS_IN_CACHE.push(dataset.label);
						}
						//get layer (by dataset label or by dataset name)
						var layer = $scope.getLayerByName(dataset.label);
						if (!layer)
							layer = $scope.getLayerByName(dataset.name);

						if(newValue.length > 0){
							// save unfiltered data if not already saved
							if(!$scope.savedValues[layer.name]){
								$scope.savedValues[layer.name] = {};
								angular.copy($scope.values[layer.name], $scope.savedValues[layer.name]);
							}

							// calc filtered data
							scope.filterDataset(scope.values[layer.name],scope.reformatSelections(newValue));

							// apply filtered data
							$scope.createLayerWithData(layer.name, scope.values[layer.name], false, false);
						}else{
							// restore unfiltered data
							angular.copy(scope.savedValues[layer.name], scope.values[layer.name]);
							delete scope.savedValues[layer.name];

							// apply unfiltered data
							$scope.createLayerWithData(layer.name, scope.values[layer.name], false, false);
						}
					}
				}
			}
		});

		//reformatting the selections to have the same model of the filters
		$scope.reformatSelections = function(realTimeSelections){
			if (realTimeSelections && $scope.ngModel && $scope.ngModel.dataset && $scope.ngModel.dataset.dsId){
				var widgetDatasetId = $scope.ngModel.dataset.dsId;
				var widgetDataset = cockpitModule_datasetServices.getDatasetById(widgetDatasetId)

				for (var i=0; i< realTimeSelections.length; i++){
					//search if there are selection on the widget's dataset
					if (realTimeSelections[i].datasetId == widgetDatasetId){
						var selections = realTimeSelections[i].selections;
						var formattedSelection = {};
						var datasetSelection = selections[widgetDataset.label];
						for(var s in datasetSelection){
							var columnObject = $scope.getColumnObjectFromName($scope.ngModel.content.columnSelectedOfDataset,s);
							if (!columnObject){
								columnObject = $scope.getColumnObjectFromName(widgetDataset.metadata.fieldsMeta,s);
							}

							formattedSelection[columnObject.aliasToShow || columnObject.alias] = {"values":[], "type": columnObject.fieldType};
							for(var k in datasetSelection[s]){
								// clean the value from the parenthesis ( )
								if (columnObject.fieldType == "SPATIAL_ATTRIBUTE") {
									//for spatial attribute doens't split value
									var x = datasetSelection[s][k].replace(/[()]/g, '').replace(/['']/g, '');
									formattedSelection[columnObject.aliasToShow || columnObject.alias].values.push(x);
								}else{
									var x = datasetSelection[s][k].replace(/[()]/g, '').replace(/['']/g, '').split(/[,]/g);
									for(var i=0; i<x.length; i++){
										formattedSelection[columnObject.aliasToShow || columnObject.alias].values.push(x[i]);
									}
								}
							}
						}
					}
				}
				return formattedSelection;
			}
		}

		/**
		 * Returns the column object that satisfy the original name (not aliasToShow) passed as argument
		 */
		$scope.getColumnObjectFromName = function(columnSelectedOfDataset, originalName) {
			for (i = 0; i < columnSelectedOfDataset.length; i++){
				if (columnSelectedOfDataset[i].name === originalName){
					return columnSelectedOfDataset[i];
				}
			}
		}

		// filtering the table for realtime dataset
		$scope.filterDataset = function(datastore,selection){
			//using the reformatted filters
			var filters = selection ? selection : $scope.reformatSelections();
			for(var f in filters){
				for(var i = datastore.rows.length - 1; i>= 0; i--){
					var columnName;
					for(var j in datastore.metaData.fields){
						if(datastore.metaData.fields[j].header == f){
							columnName = datastore.metaData.fields[j].name;
							break;
						}
					}
					//if the column is an attribute check in filter
					if (filters[f].type == 'ATTRIBUTE'){
						var value = datastore.rows[i][columnName];
						if(typeof value == "number"){
							value = String(value);
						}
						if (filters[f].values.indexOf(value)==-1){
							datastore.rows.splice(i,1);
						}
					//if the column is a measure cast it to number and check in filter
					} else if (filters[f].type == 'MEASURE'){
						var columnValue = Number(datastore.rows[i][columnName]);
						var filterValue = filters[f].values.map(function (x) {
						    return Number(x);
						});
						//check operator
						var operator = String(filters[f].operator);
						if (operator == "="){
							operator = "==";
						}
						var leftOperand = String(columnValue);
						var rightOperand = String(filterValue[0]);
						var expression =  leftOperand + operator + rightOperand;

						//if (filterValue.indexOf(columnValue)==-1){
						if (eval(expression) == false){
							datastore.rows.splice(i,1);
						}
					}else if (filters[f].type == 'SPATIAL_ATTRIBUTE'){
						var value = datastore.rows[i][columnName];

						if (filters[f].values.indexOf(value)==-1){
							datastore.rows.splice(i,1);
						}
					}
				}
			}
			return datastore;
		}

		$scope.getTemplateUrl = function(template){
			return cockpitModule_generalServices.getTemplateUrl('mapWidget',template);
		}

		$scope.addAllLayers = function() {
			$scope.addBaseLayer();
			$scope.addBackgroundLayer();
			$scope.getLayers();
		}

		$scope.reinit = function(){

			// Prevent errors about $digest
			$timeout(function() {
				var nature = "refresh";
				$scope.refreshWidget(null, nature);
			});
		}

		$scope.setMapSize = function() {
			if (!$scope.map.getSize()){
				$scope.map.setSize([cockpitModule_widgetConfigurator.map.initialDimension.width,
									cockpitModule_widgetConfigurator.map.initialDimension.height]);
			}else{
				$scope.map.setSize($scope.map.getSize());
			}
		}

		$mdSidenav($scope.optionSidenavId, true).then(
			function(instance) {
				var oldCloseFn = instance.close;

				instance.close = function() {
					oldCloseFn();
					$scope.sideNavOpened = instance.isOpen();
				};
			}
		);

		$scope.toggleSidenav = function(){
			var optionSidenav = $mdSidenav($scope.optionSidenavId);
			optionSidenav.toggle();
			$scope.sideNavOpened = optionSidenav.isOpen();
		}

		$scope.clearAllLayers = function() {
			$scope.map.setLayerGroup(new ol.layer.Group());
		}

		$scope.refresh = function(element,width,height, data, nature, associativeSelection, changedChartType, chartConf, options) {
			if (nature == 'fullExpand' || nature == 'resize'){
				$timeout(function() {
					$scope.map.updateSize();
				}, 500);
				return;
			} else if (nature == "refresh") {
				// Delete all layers
				$scope.clearAllLayers();
				$scope.clearInternalData();

				$scope.resetFilter();
				$scope.resetAnimation();

				$scope.addAllLayers();
				$scope.setZoomControl();
				$scope.setScaleControl();
				$scope.setMouseWheelZoomInteraction();
				$scope.setMapSize();
			}

			if (!options) options = {};
			var layerName = (Array.isArray(options.label)) ? options.label[0] : options.label; //on delete of selections options is an array !!!

			// save unfiltered data
			$scope.values[layerName] = data;

			if($scope.realTimeSelections.length > 0){
				// save unfiltered data
				$scope.savedValues[layerName] = {};
				angular.copy(data, $scope.savedValues[layerName]);

				// calc & save filtered data
				$scope.filterDataset($scope.values[layerName], $scope.reformatSelections($scope.realTimeSelections));
			}

			// apply (filtered) data
			var tmpLayer = $scope.getLayerByName(layerName);
			if (!tmpLayer){
				tmpLayer = {};
				tmpLayer.isCluster = false;
				tmpLayer.isHeatmap = false;
			}
			$scope.createLayerWithData(layerName, $scope.values[layerName], tmpLayer.isCluster, tmpLayer.isHeatmap);
			$scope.thematizeMeasure(layerName, null);
		}

		$scope.getOptions =function(){
			var obj = {};
			var targetLayer = $scope.getTargetDataset();
			obj["type"] = $scope.ngModel.type;
			obj["label"] = (targetLayer) ? targetLayer.name : "";
			return obj;
		}

		$scope.editWidget=function(index){
			var finishEdit=$q.defer();
			var config = {
					attachTo:  angular.element(document.body),
					controller: mapWidgetEditControllerFunction,
					disableParentScroll: true,
					templateUrl: $scope.getTemplateUrl('mapWidgetEditPropertyTemplate'),
					position: $mdPanel.newPanelPosition().absolute().center(),
					fullscreen :true,
					hasBackdrop: true,
					clickOutsideToClose: true,
					escapeToClose: false,
					focusOnOpen: true,
					preserveScope: false,
					locals: {finishEdit:finishEdit,model:$scope.ngModel},
			};
			$mdPanel.open(config);
			return finishEdit.promise;
		}

//############################################## SPECIFIC MAP WIDGET METHODS #########################################################################

	    $scope.getLegend = function(referenceId, visualizationType){
	    	$scope.legend = cockpitModule_mapThematizerServices.getLegend(referenceId, visualizationType);
	    }

		function syncDatasetMetadata(layerDef) {

			var toNames = function(el) { return el.name; };

			var dsId = layerDef.dsId;
			var ds = cockpitModule_datasetServices.getDatasetById(dsId);

			var currMetadata = ds.metadata.fieldsMeta;

			var currMetadataNames     = currMetadata.map(toNames);
			var deletedCols = [];

			layerDef.dataset.metadata.fieldsMeta = layerDef.dataset
				.metadata
				.fieldsMeta
				.reduce(function(acc, el1) {
					if (currMetadataNames.some(function(el2) { return el2 == el1.name; })) {
						acc.push(el1);
					} else {
						deletedCols.push(el1);
					}
					return acc;
				}, []);

			deletedCols.forEach(function(el1) {
				layerDef.content
					.columnSelectedOfDataset = layerDef.content
						.columnSelectedOfDataset
						.filter(function(el2) {
							return !(el1.alias == el2.alias);
						});
			});
		}

		$scope.getLayers = function () {
			for (var l in $scope.ngModel.content.layers){
				var currLayer = $scope.ngModel.content.layers[l];
				var layerDef =  currLayer;
				var layerID = $scope.ngModel.id + "|" + layerDef.name;
				$scope.configs[layerID] = layerDef;
				if (layerDef.type === 'DATASET') {
					syncDatasetMetadata(layerDef);
					$scope.getFeaturesFromDataset(layerDef);
				} else if (layerDef.type === 'CATALOG') {
					//TODO implementare recupero layer da catalogo
				} else {
					sbiModule_messaging.showInfoMessage(sbiModule_translate.load('sbi.cockpit.map.typeLayerNotManaged'), 'Title', 3000);
					console.log("Layer with type ["+layerDef.type+"] not managed! ");
					$timeout(function() {
						$scope.hideWidgetSpinner();
					}, 3000);
				}
			}
		}

		$scope.initializeTemplate = function (){
			if (!$scope.ngModel.content.currentView)  $scope.ngModel.content.currentView = {};
			if (!$scope.ngModel.content.layers) $scope.ngModel.content.layers = [];
			if (!$scope.ngModel.content.baseLayersConf) $scope.ngModel.content.baseLayersConf = [];
			if (!$scope.ngModel.content.columnSelectedOfDataset) $scope.ngModel.content.columnSelectedOfDataset = {} ;

			if (!$scope.ngModel.content.currentView.center) $scope.ngModel.content.currentView.center = [0,0];
			if (!$scope.ngModel.content.mapId){
				$scope.ngModel.content.mapId =  'map-' + $scope.ngModel.id;
			}

			var currLayers = $scope.ngModel.content.layers;
			for (l in currLayers){
				var currLayer = currLayers[l];
				var currDsId = currLayer.dsId;
				var isCluster = $scope.isCluster(currLayer);
				var isHeatmap = $scope.isHeatmap(currLayer);

				// set default indicator (first one) for each layer
				var columns = $scope.getColumnSelectedOfDataset(currDsId);
				for (var c in columns){
					if (columns[c].properties && columns[c].properties.showMap){
						currLayer.defaultIndicator = columns[c].name;
						break;
					}
				}
				// all attributes that don't have aggregateBy properties need a default value to true
				for (var c in columns) {
					var currCol = columns[c];
					if (!currCol.properties) {
						currCol.properties = {};
					}
					if (!currCol.properties.hasOwnProperty("aggregateBy")) {
						currCol.properties.aggregateBy = (currCol.fieldType == "ATTRIBUTE" || currCol.fieldType == "SPATIAL_ATTRIBUTE");
					}
					if (currCol.fieldType == "MEASURE") {
						currCol.properties.aggregateBy = false;
					}
				}
				// Set exploded flag for heatmap and cluster
				if (isHeatmap || isCluster) {
					$scope.exploded[currDsId] = false;
				}
			}
			
			// Retrocompatibility
			if (Array.isArray($scope.ngModel.style.legend.position)) {
				$scope.ngModel.style.legend.coordinates = $scope.ngModel.style.legend.position;
				$scope.ngModel.style.legend.position = "drag";
			}
			$scope.ngModel.style.legend.position = $scope.ngModel.style.legend.position || "east";
			
			if (!$scope.ngModel.content.hasOwnProperty("enableBaseLayer")) $scope.ngModel.content.enableBaseLayer = true;

		}
		
		$scope.fixStats = function(layerDef, data) {
			var fields = data.metaData.fields;
			stat = $scope.dataSetStats[layerDef.name] = {}
			for (i in fields) {
				var curr = fields[i];
				
				if (typeof curr === 'object') {
					var otherMetaData = layerDef.dataset
						.metadata
						.fieldsMeta
						.find(function(e) { return e.name == curr.header })
				
					stat[i] = data.stats[i];
					stat[i].name = curr.name;
					stat[i].header = curr.header;
					stat[i].fieldType = otherMetaData ? otherMetaData.fieldType : "MEASURE";
				}
			}
		}

		$scope.checkLayer = function(layerDef) {
			var itsOk = true;
			
			if (layerDef.visualizationType == "pies") {
				var layerName = layerDef.alias;
				var categorizeBy = layerDef.pieConf && layerDef.pieConf.categorizeBy;
				var categoryColumn = layerDef.content.columnSelectedOfDataset.find(function(e) { return e.name == categorizeBy; })
				if (categoryColumn == null || categoryColumn.fieldType != "ATTRIBUTE") {
					var message = sbiModule_translate.load('sbi.cockpit.map.edit.visualization.pie.errorMissingCategory');
					message = message.replace("{0}", categorizeBy)
					message = message.replace("{1}", layerName)
					sbiModule_messaging.showInfoMessage(message, 'Title', 0);
					itsOk = false;
				} 
			}
			
			return itsOk;
		}

		$scope.createLayerWithData = function(label, data, isCluster, isHeatmap){
			//prepare object with metadata for desiderata dataset columns
			var geoColumn, selectedMeasure = null;
			var columnsForData, isHeatmap;
			var layerID = $scope.ngModel.id + "|" + label;
			var layerDef =  $scope.configs[layerID];

			if (!layerDef) return;

			$scope.fixStats(layerDef, data);
	
			columnsForData = $scope.getColumnSelectedOfDataset(layerDef.dsId) || [];

			if (!$scope.checkLayer(layerDef)) {
				return;
			}

			//remove old layer
			var previousLayer = $scope.getLayerByName(label);
			if (previousLayer) {
				$scope.map.removeLayer(previousLayer); //ol obj
			}
			$scope.removeLayer(label);

			for (f in columnsForData){
				var tmpField = columnsForData[f];
				if (tmpField && tmpField.fieldType == "SPATIAL_ATTRIBUTE"){
					geoColumn = tmpField.name;
					break;
				}
			}

			layerDef.layerID = layerID;
			cockpitModule_mapServices.clearCache(layerID);
			var featuresSource = cockpitModule_mapServices.getFeaturesDetails(geoColumn, selectedMeasure, layerDef, columnsForData, data);
			if (featuresSource == null){
				// creates a fake layer for internal object (because isn't the first loop anymore)
				layer = {};
				layer.name = layerDef.name;
				layer.dsId = layerDef.dsId;
				$scope.addLayer(layerDef.name, layer);	//add layer to internal object
				return;
			}

			cockpitModule_mapThematizerServices.setActiveConf($scope.ngModel.id + "|" + layerDef.name, layerDef);
			cockpitModule_mapThematizerServices.updateLegend($scope.ngModel.id + "|" + layerDef.name, data, $scope.ngModel.style.legend); //add legend to internal structure
			cockpitModule_mapThematizerServices.clearCache($scope.ngModel.id + "|" + layerDef.name);
			if (layerDef.visualizationType == 'choropleth') {
				if ($scope.ngModel.style.legend)
					$scope.getLegend($scope.ngModel.id, $scope.ngModel.style.legend.visualizationType);
				else
					$scope.getLegend($scope.ngModel.id);
			}
			var layer;
			if (isCluster) {
				var clusterSource = new ol.source.Cluster({source: featuresSource
														  });
				layer = new ol.layer.Vector({source: clusterSource,
					style: cockpitModule_mapThematizerServices.layerStyle
				});
			} else if (isHeatmap) {
				layer = new ol.layer.Heatmap({source: featuresSource,
					blur: layerDef.heatmapConf.blur,
					radius: layerDef.heatmapConf.radius,
					weight: cockpitModule_mapThematizerServices.setHeatmapWeight
				});
			} else {
				layer = new ol.layer.Vector({source: featuresSource,
					style: cockpitModule_mapThematizerServices.layerStyle
				});
			}

			// Reference to the original layer
			layer.set("originalLayer", layerDef);
			layer.set("stats", $scope.dataSetStats[layerDef.name]);

			//add decoration to layer element
			layer.name = layerDef.name;
			layer.dsId = layerDef.dsId;
			layer.setZIndex(
					/* a little offset to get space for background */
					10 + /* then */ layerDef.order*1000);
			layer.modalSelectionColumn = layerDef.modalSelectionColumn;
			layer.hasShownDetails = layerDef.hasShownDetails;
			layer.isHeatmap = isHeatmap;
			layer.isCluster = isCluster;
			layer.filterBy = {};

			if ($scope.map){
				$scope.map.addLayer(layer); // add layer to ol.Map
			}
			else{
				$timeout(function() {
					 console.log("Waiting 3000 ms for creation object map!");
					 if ($scope.map){
						$scope.map.addLayer(layer); 			//add layer to ol.Map
						$scope.hideWidgetSpinner();
					}
				}, 3000)


			}

			// Setting default visibility
			if (!layerDef.hasOwnProperty("defaultVisible")) {
				// Default visibility for layers that don't specify one
				layerDef.defaultVisible = true;
			}
			if ($scope.layerVisibility[label] == undefined) {
				$scope.layerVisibility[label] = layerDef.defaultVisible;
			}
			layer.setVisible($scope.layerVisibility[label]);

			// Setting default for isStatic property
			if (!layerDef.hasOwnProperty("isStatic")) {
				layerDef.isStatic = false;
			}

			$scope.addLayer(layerDef.name, layer);	//add layer to internal object
			$scope.setLayerProperty (layerDef.name, 'geoColumn',geoColumn),
			$scope.values[layerDef.name] = data; //add values to internal object
			cockpitModule_mapServices.updateCoordinatesAndZoom($scope.ngModel, $scope.map, layer, true);

			$scope.getPerLayerFilters(layerDef);
		}


		$scope.getColumnSelectedOfDataset = function(dsId) {
			var layers = $scope.ngModel.content.layers;

			for (var i = 0; i<layers.length; i++) {
				var currLayer = layers[i];
				if (currLayer.dsId == dsId) {
					return currLayer.content.columnSelectedOfDataset;
				}
			}
			return null;
		}

	    $scope.getTargetDataset = function() {
	    	for (l in $scope.ngModel.content.layers){
	    		if ($scope.isTargetLayer($scope.ngModel.content.layers[l])){
	    			return $scope.ngModel.content.layers[l];
	    		}
	    	}
	    	return null;
	    }

	    $scope.addViewEvents = function(){
	    	//view events
	    	var view = $scope.map.getView();
            view.on("change:resolution", function(e) {
            	//zoom action
        	    if (Number.isInteger(e.target.getZoom())) {
	            	var previousZoom = $scope.ngModel.content.currentView.zoom;
	            	var newZoom =  e.target.getZoom();
	            	if (previousZoom > newZoom ){
	            		for (l in $scope.ngModel.content.layers){
		    		    	var layerDef =  $scope.ngModel.content.layers[l];
		    		    	var isCluster = $scope.isCluster(layerDef);
		    		    	var isHeatmap = $scope.isHeatmap(layerDef);
		    		    	if (isCluster){
			    				var values = $scope.values[layerDef.name];
				        		$scope.createLayerWithData(layerDef.name, values, true, false); //return to cluster view
			    			}
			    			if (isHeatmap){
			    				var values = $scope.values[layerDef.name];
				        		$scope.createLayerWithData(layerDef.name, values, false, true); //return to cluster view
			    			}
	            		}
	            	}
        	    }
        	    $scope.ngModel.content.currentView.zoom = e.target.getZoom();
        	    $scope.ngModel.content.currentView.center = e.target.getCenter();
            });
	    }

		$scope.addMapEvents = function (){
			if ($scope.closer){
				$scope.closer.onclick = function(){
					$scope.popupOverlay.setPosition(undefined);
					if ($scope.closer && $scope.closer.blur) $scope.closer.blur();
					return false;
				}
			}

			function locateClickedLayer(evt) {
				var featureFounded = false;
				$scope.selectedLayer = undefined;
				$scope.selectedFeature = undefined;
				$scope.props = {};
				$scope.clickOnFeature = false;

				/*
				 * This function does exactly what it says: it cycles
				 * through EVERY feature of EVERY layer at specific
				 * pixel. If the z-index value of the layer is correct
				 * we can just stop at the first layer (first call).
				 */
				evt.map.forEachFeatureAtPixel(evt.pixel,
					function(feature, layer) {
						
						if (!layer.get("originalLayer").isStatic && !featureFounded) {
							$scope.selectedLayer = layer;
							$scope.selectedFeature = (Array.isArray(feature.get('features')) && feature.get('features').length == 1) ? feature.get('features')[0] : feature;
							$scope.props = $scope.selectedFeature.getProperties();
							$scope.clickOnFeature = true;
							featureFounded = true;
						}
				});
			}

			$scope.map.on("moveend", function(e) {
				$scope.ngModel.content.currentView.center = e.target.getView().getCenter();
			});

			$scope.map.on("zoomend", function(e) {
				$scope.ngModel.content.currentView.zoom = e.target.getView().getZoom();
			});

			$scope.map.on('singleclick', function(evt) {

				locateClickedLayer(evt);

				//modal selection management
				if ($scope.clickOnFeature && $scope.selectedLayer.modalSelectionColumn){
					$scope.doSelection($scope.selectedLayer.modalSelectionColumn, $scope.props[$scope.selectedLayer.modalSelectionColumn].value, null, null, $scope.props, null, $scope.selectedLayer.dsId);
				}

				//popup isn't shown with cluster and heatmap
				if ($scope.selectedLayer && ($scope.selectedLayer.isCluster || $scope.selectedLayer.isHeatmap  || !$scope.selectedLayer.hasShownDetails)){
					$scope.closer.onclick();
					return;
				}

				//when a feature is clicked
				if ($scope.clickOnFeature && $scope.selectedFeature) {
					if ($scope.props.features && Array.isArray($scope.props.features)) return;
					$scope.$apply()

					if (cockpitModule_properties.EDIT_MODE || !$scope.columnsConfig[$scope.selectedLayer.name]){
						$scope.layerConfig = $scope.getColumnSelectedOfDataset($scope.selectedLayer.dsId);
						$scope.columnsConfig[$scope.selectedLayer.name] =  $scope.layerConfig;
					}else{
						$scope.layerConfig = $scope.columnsConfig[$scope.selectedLayer.name];
					}

					var geometry = $scope.selectedFeature.getGeometry();
					var coordinate = evt.coordinate;
					$scope.popupOverlay.setPosition(coordinate);
					$scope.popupContainer.style["visibility"] = 'visible';
				}

				//when no feature is clicked, close the details popup
				if ($scope.selectedFeature == undefined) {
					$scope.closer.onclick();
					$scope.popupContainer.style["visibility"] = 'hidden';
					return;
				}

			 });

			$scope.map.on('dblclick', function(evt) {

				locateClickedLayer(evt);

				if ($scope.selectedLayer){
					var layerDef = $scope.selectedLayer.get("originalLayer");
					var dsId = layerDef.dsId;

					var isCluster = $scope.isCluster(layerDef);
					var isHeatmap = $scope.isHeatmap(layerDef);

					// Don't recreate the map if it's not needed
					if (isCluster || isHeatmap) {

						$scope.exploded[dsId] = !$scope.exploded[dsId];

						if ($scope.exploded[dsId]) {
							var values = $scope.values[layerDef.name];
							$scope.createLayerWithData(layerDef.name, values, false, false);
						} else {
							var values = $scope.values[layerDef.name];
							$scope.createLayerWithData(layerDef.name, values, isCluster, isHeatmap);
						}
					}
				}
			});

		}

		$scope.addMapEventForTooltip = function (){
			$scope.map.on('pointermove', function(evt) {
				var featureFounded = false;
				var selectedLayer = undefined;
				var selectedFeature = undefined;
				var props = {};

				/*
				 * This function does exactly what it says: it cycles
				 * through EVERY feature of EVERY layer at specific
				 * pixel. If the z-index value of the layer is correct
				 * we can just stop at the first layer (first call).
				 */
				evt.map.forEachFeatureAtPixel(evt.pixel,
					function(feature, layer) {
						if (!featureFounded) {
							selectedLayer = layer;
							selectedFeature = (Array.isArray(feature.get('features')) && feature.get('features').length == 1) ? feature.get('features')[0] : feature;
							props = selectedFeature.getProperties();
							featureFounded = true;
						}
				});

				var coordinate = evt.coordinate;
				if (selectedLayer) {
					var originalLayer = selectedLayer.get("originalLayer");
					if (originalLayer.showTooltip) {
						var tooltipCol = originalLayer.tooltipColumn;
						var prop = $scope.getColumnSelectedOfDataset(selectedLayer.dsId)
							.find(function(e) { return tooltipCol == e.name; });
						var value = $scope.getPropValueFromProps(props, prop);

						// A little offset just to let the user click the underneath feature
						coordinate[0] = coordinate[0]+25;
						coordinate[1] = coordinate[1]+25;

						$scope.tooltipOverlay.setPosition(coordinate);
						$scope.tooltipContainer.style["visibility"] = 'visible';
						$scope.tooltip = value || "n.d.";
					}
				} else {
					$scope.tooltipContainer.style["visibility"] = 'hidden';
				}

				$scope.$apply();
			});
		}

	    $scope.isDisplayableProp = function (p, config){
	    	for (c in config){
	    		if (p == config[c].name && config[c].properties.showDetails){
	    			return config[c];
	    		}
	    	}
	    	return null;
	    }

		$scope.getFeaturesFromDataset = function(layerDef){
			//prepare object with metadata for desiderata dataset columns
			var geoColumn = null;
			var selectedMeasure = null;
			var columnsForData = [];
			var isCluster = $scope.isCluster(layerDef);
			var isHeatmap = $scope.isHeatmap(layerDef);

			columnsForData = $scope.getColumnSelectedOfDataset(layerDef.dsId) || [];

			// exclude from model all attributes that are not needed for aggregation
			columnsForData = columnsForData.filter(function(el) {
					var type = el.fieldType;
					return !(type == "ATTRIBUTE" && !el.properties.aggregateBy);
				})
				.map(function(el) {
					/*
					 * The following step can edit the objs and
					 * we don't want that.
					 */
					return angular.copy(el);
				})
				.map(function(el) {
					/*
					 * Reset aggregation function because the backend service
					 * expects this.
					 */
					if (el.properties.aggregateBy) {
						el.aggregationSelected = 'NONE';
					}

					return el;
				})
				.map(function(el) {
					var type = el.fieldType;

					/*
					 * Convert spatial attribute to measure when the aggregation
					 * is disabled..
					 */
					if (type == "SPATIAL_ATTRIBUTE" && !el.properties.aggregateBy) {
						el.fieldType = "MEASURE";
					}

					return el;
				});

			for (f in columnsForData){
				var tmpField = columnsForData[f];
				if (tmpField.properties
						&& tmpField.properties.showMap) {
					//first measure
					selectedMeasure = tmpField.aliasToShow;
					if (!layerDef.defaultIndicator) {
						layerDef.defaultIndicator = selectedMeasure;
					}
				}

			}

			var model = { content: { columnSelectedOfDataset: columnsForData }, updateble: true };
			if($scope.ngModel.filters) model.filters = $scope.ngModel.filters;

			//get the dataset columns values
			cockpitModule_datasetServices.loadDatasetRecordsById(layerDef.dsId, undefined, undefined, undefined, undefined, model).then(
				function(response){

					$scope.createLayerWithData(layerDef.name, response, isCluster, isHeatmap);
					$scope.hideWidgetSpinner();
					
					$scope.map.render();
					// Seams to fix invisible layer problem before the first map interaction
					$scope.map.updateSize();
			},function(error){
				console.log("Error loading dataset with id [ "+layerDef.dsId+"] ");
				sbiModule_messaging.showInfoMessage($scope.translate.load('sbi.cockpit.map.datasetLoadingError').replace("{0}",layerDef.dsId), 'Title', 3000);
				$timeout(function() {
					$scope.hideWidgetSpinner();
				}, 3000);

			});
		}
		
		$scope.createMap = function (){

			var layers = [];

			if (!$scope.popupContainer){
				$scope.popupContainer = document.getElementById('popup-' + $scope.ngModel.id);
				$scope.closer = document.getElementById('popup-closer-' +$scope.ngModel.id);

				$scope.tooltipContainer = document.getElementById('tooltip-' + $scope.ngModel.id);
			}

			//create overlayers (popup..)
			$scope.popupOverlay = new ol.Overlay({
				element: $scope.popupContainer,
				autoPan: true,
				autoPanAnimation: {
					duration: 250
				}
			});

			$scope.tooltipOverlay = new ol.Overlay({
				element: $scope.tooltipContainer,
				autoPan: true,
				autoPanAnimation: {
					duration: 250
				}
			});

			//setting coordinates (from the first layer if they aren't set into the template)
			if ($scope.ngModel.content.currentView.center[0] == 0 && $scope.ngModel.content.currentView.center[1] == 0 && $scope.layers.length > 0){
				var tmpLayer = $scope.layers[0].layer;
				cockpitModule_mapServices.updateCoordinatesAndZoom($scope.ngModel, $scope.map, tmpLayer, false);
			}

			$scope.map = new ol.Map({
				target: 'map-' + $scope.ngModel.id,
				layers: layers,
				overlays: [$scope.popupOverlay, $scope.tooltipOverlay],
				controls: [],
				interactions: [
					new ol.interaction.DragPan(),
					new ol.interaction.PinchRotate(),
					new ol.interaction.PinchZoom()
				]
			});
			console.log("Created obj map with id [" + 'map-' + $scope.ngModel.id + "]", $scope.map);

			$scope.setZoomControl();
			$scope.setScaleControl();
			$scope.setMouseWheelZoomInteraction();

			$scope.setMapView();

			//just for refresh
			$scope.setMapSize();

			//add events methods
			$scope.addViewEvents();
			$scope.addMapEvents();
			$scope.addMapEventForTooltip();
			$scope.loading = false;
			$timeout(function(){
				$scope.widgetIsInit=true;
				cockpitModule_properties.INITIALIZED_WIDGETS.push($scope.ngModel.id);
			},1500);

		}

	    //control panel events
	    $scope.toggleLayer = function(e,n){
			e.stopPropagation();
			console.log($scope.ngModel.mutualExclusion);
			if($scope.ngModel.mutualExclusion === false) {
				console.log("Non mutual exclusion");
				var l = $scope.getLayerByName(n);
				if (!l) return; //do nothing
				var toggle = !l.getVisible();
				$scope.layerVisibility[n] = toggle;
				l.setVisible(toggle);
			} else {
				console.log("Mutual exclusion, layer selected: "+n);
				var l = $scope.getLayerByName(n);
				if (!l) return; //do nothing
				$scope.layerVisibility[n] = true;
				l.setVisible($scope.layerVisibility[n]);
				for(var i in $scope.ngModel.content.layers) {
					var layerName = $scope.ngModel.content.layers[i].name;
					console.log(layerName);
					if(n !== layerName ) {
						console.log("setting to false");
						var l_tmp = $scope.getLayerByName(layerName);
						if (!l_tmp) return; //do nothing
						$scope.layerVisibility[layerName] = false;
						l_tmp.setVisible($scope.layerVisibility[layerName]);
					}
				}
			}
	    }

		$scope.toggleLayerExpanse = function(layer){
			if ($scope.hasMeasures(layer)) {
				layer.expandedNav = !layer.expandedNav;
			} else {
				layer.expandedNav = false;
			}
		}

	    $scope.getLayerVisibility = function(n){
	    	var l = $scope.getLayerByName(n);
	    	if (!l || !l.getVisible) return; //do nothing
	    	return l.getVisible();
	    }

	    $scope.getVisibleLayersCount = function(){
	    	var visibleLayersCount = 0;
	    	for (var i=0; i<$scope.layers.length; i++) {
	    		var l = $scope.layers[i].layer;
	    		if (l && l.getVisible && l.getVisible()) {
	    			visibleLayersCount++;
	    		}
	    	}
	    	return visibleLayersCount;
	    }

	    $scope.getIndicatorVisibility = function(l,n){
	    	for (lpos in  $scope.ngModel.content.layers){
	    		if ( $scope.ngModel.content.layers[lpos].name == l)
		    	for (var i in $scope.ngModel.content.layers[lpos].indicators){
		    		if ($scope.ngModel.content.layers[lpos].indicators[i].label == n){
		    			return $scope.ngModel.content.layers[lpos].indicators[i].showMap || false;
		    		}
		    	}
	    	}
	    	return false;
	    }

		//Thematization
		$scope.thematizeMeasure = function (l, m){
			var layer = $scope.getLayerByName(l);
			var layerValues = $scope.values[l];
			var layerKeyColumn =  $scope.getLayerProperty(l, 'geoColumn');
			var layerConfig;
			for (var c=0; c<$scope.ngModel.content.layers.length;c++){
				if ($scope.ngModel.content.layers[c].name === l){
					layerConfig =$scope.ngModel.content.layers[c];
					break;
				}
			}
			var layerColumnConfig = $scope.getColumnSelectedOfDataset(layerConfig.dsId) || [];
			$scope.refreshStyle(layer, m, layerConfig, layerColumnConfig, layerValues, layerKeyColumn);
		}

		//thematizer functions
	    $scope.refreshStyle = function (layer, measure, config, configColumns, values, geoColumn){
			//prepare object for thematization
	    	var layerID = $scope.ngModel.id + "|" + config.name;
	    	var elem = null;

			if (measure != null) {
				elem = cockpitModule_mapServices.getColumnConfigByProp(configColumns, 'aliasToShow', measure);
			} else {
				var activeIndicator = cockpitModule_mapThematizerServices.getActiveIndicator();
				elem = cockpitModule_mapServices.getColumnConfigByProp(configColumns, 'name', activeIndicator);
			}

	    	if (elem){
		    	cockpitModule_mapThematizerServices.setActiveIndicator(elem.name);
		    	config.defaultIndicator = elem.name;

		    	cockpitModule_mapThematizerServices.loadIndicatorMaxMinVal(config.name +'|'+ elem.name, values);
		    	cockpitModule_mapThematizerServices.updateLegend(layerID, values,$scope.ngModel.style.legend);

		    	$scope.getLegend($scope.ngModel.id, $scope.ngModel.style.legend.visualizationType);
			}

			layer.getSource().changed();

		}

	    //Utility functions
	    $scope.getLayerByName = function(n){
	    	var tmpName = n.split("|");
	    	if (tmpName.length > 1) n = tmpName[1];
	    	for (l in $scope.layers){
	    		if ($scope.layers[l].name === n)
	    			return $scope.layers[l].layer;
	    	}
	    	return null;
	    }

	    $scope.addLayer = function(n,l){
	    	$scope.layers.push({"name": n,"layer":l});
	    }

	    $scope.removeLayer = function(n){
	    	for (l in $scope.layers){
	    		if ($scope.layers[l].name == n)
	    			$scope.layers.splice(l,1);
	    	}
	    }

		$scope.clearInternalData = function(){
			$scope.layers = [];
			$scope.values = {};
			$scope.savedValues = {};
			$scope.configs = {};
			$scope.legend = [];
			$scope.exploded = {};
			$scope.layerVisibility = [];
			cockpitModule_mapThematizerServices.removeLegends();
			cockpitModule_mapThematizerServices.clearDefaultMarkerCache();
		}

	    $scope.setLayerProperty = function(l, p, v){
	    	for (o in $scope.layers){
	    		if ($scope.layers[o].name === l)
	    			$scope.layers[o][p] = v;
	    	}
	    }

	    $scope.getLayerProperty = function(l, p){
	    	for (o in $scope.layers){
	    		if ($scope.layers[o].name === l)
	    			return $scope.layers[o][p] || null;
	    	}
	    }

	    $scope.setLayerProperty = function(l, p, v){
	    	for (o in $scope.layers){
	    		if ($scope.layers[o].name === l)
	    			$scope.layers[o][p] = v;
	    	}
	    }

		/**
		 * @deprecated Not used
		 */
		$scope.selectPropValue = function(dsId, prop, modalSelectionColumn){
			if (!modalSelectionColumn && prop.fieldType !== "MEASURE"){
				$scope.doSelection(prop.alias, $scope.props[prop.name].value, null, null, $scope.props, null, dsId);
			}
		}

	    $scope.crossNavigate = function(dsId,layerConfig,props) {
	    	$scope.doSelection(layerConfig.alias, $scope.props[layerConfig[0].name].value, null, null, props, null, dsId);
	    }

		$scope.checkCrossNavigation = function(){
			var targetLayer = $scope.getTargetDataset();
			var selectedLayer = $scope.selectedLayer;

			if(selectedLayer
					&& targetLayer
					&& targetLayer.name == selectedLayer.name
					&& $scope.ngModel.cross
					&& $scope.ngModel.cross.cross
					&& $scope.ngModel.cross.cross.enable) {
				return true;
			}
			return false;
		}

		var BASE_LAYER_TYPE = "baseLayer";
		var BACKGROUND_LAYER_TYPE = "backgroundLayer";
		var PROPERTY_LAYER_TYPE = "layerType";

		// Base layer
		$scope.baseLayer = undefined;
		$scope.addBaseLayer = function() {
			if ($scope.needsBaseLayer()) {

				$scope.baseLayer = $scope.createBaseLayer();

				$scope.map.addLayer($scope.baseLayer);
			}
		}

		$scope.createBaseLayer = function() {
			var ret = cockpitModule_mapServices.getBaseLayer($scope.ngModel.content.baseLayersConf[0]);
			ret.set(PROPERTY_LAYER_TYPE, BACKGROUND_LAYER_TYPE);
			ret.setZIndex(0);
			return ret;
		}

		$scope.needsBaseLayer = function() {
			return $scope.ngModel.content.enableBaseLayer;
		}

		// Background layer
		$scope.backgroundLayer = undefined;
		$scope.addBackgroundLayer = function() {

			if ($scope.needsBackgroundLayer()) {
				var backgroundLayerId = $scope.ngModel.content.backgroundLayerId;
				var url = sbiModule_config.externalBasePath + "/restful-services/layers/" + backgroundLayerId + "/download/geojson";

				var layer = new ol.layer.Vector({
					source: new ol.source.Vector({
						url: url,
						format: new ol.format.GeoJSON()
					})
				});
				layer.setZIndex(/* see other setZIndex() call */ 9);
				layer.set(PROPERTY_LAYER_TYPE, BACKGROUND_LAYER_TYPE);
				layer.set("layerId", backgroundLayerId);

				$scope.backgroundLayer = layer;

				$scope.map.addLayer($scope.backgroundLayer);

			}
		}

		$scope.createBackgroundLayer = function() {
			var backgroundLayerId = $scope.ngModel.content.backgroundLayerId;

			var url = sbiModule_config.externalBasePath + "/restful-services/layers/" + backgroundLayerId + "/download/geojson";

			var ret = new ol.layer.Vector({
				source: new ol.source.Vector({
					url: url,
					format: new ol.format.GeoJSON()
				})
			});
			ret.setZIndex(/* see other setZIndex() call */ 9);
			ret.set(PROPERTY_LAYER_TYPE, BACKGROUND_LAYER_TYPE);
			ret.set("layerId", backgroundLayerId);

			return ret;
		}

		$scope.needsBackgroundLayer = function() {
			var backgroundLayerId = $scope.ngModel.content.backgroundLayerId;

			return backgroundLayerId != undefined
				&& backgroundLayerId != "";
		}

		$scope.isFilterableCol = function(currCol) {
			if (currCol.properties
					&& currCol.properties.showFilter) {
				return true;
			}
			return false;
		}
		
		$scope.getColumnStats = function(layer, column) {
			return getPerLayerFiltersValues(layer, column);
		}

		$scope.animationStatus = {};
		
		$scope.getAnimatedLayers = function() {
			var layers = $scope.ngModel.content.layers || [];

			return layers.filter($scope.isLayerAnimated);
		}

		$scope.toggleAnimation = function(layerName) {
			if (layerName != undefined) {
				if (!$scope.animationStatus[layerName]) {
					$scope.animationStatus[layerName] = {};
					$scope.animationStatus[layerName].inPlay = true;
				} else {
					$scope.animationStatus[layerName].inPlay = !$scope.animationStatus[layerName].inPlay;
				}
				
				if ($scope.animationStatus[layerName].inPlay) {
					setTimeout(() => {
						$scope.animateLayer(layerName);
					}, 0);
				}
			}
		}

		$scope.resetAnimation = function() {
			Object.values($scope.animationStatus).forEach(function(e) {
				e.inPlay = false;
			});
		}

		$scope.isAnimationInPlay = function() {
			return Object.values($scope.animationStatus).find(function(e) { return e.inPlay; }) != null;
		}

		$scope.getAnimationCurrentValue = function() {
			var animationInPlay = Object.values($scope.animationStatus).find(function(e) { return e.inPlay; });
			return animationInPlay && animationInPlay.currValue || "...";
		}

		$scope.animateLayer = function(layerName) {
			if (layerName != null) {
				var animatedLayer = $scope.ngModel.content.layers.find(function(e) { return e.name == layerName; });
				var animatedColumn = animatedLayer.content.columnSelectedOfDataset.find($scope.isColumnAnimated);
	
				var layerName = animatedLayer.name;
				var columnName = animatedColumn.name;
	
				var animationStatus = $scope.animationStatus[layerName];
				var stats = $scope.dataSetStats[layerName];
				var statsValues = Object.values(stats);
				var distinctValues = statsValues.find(function(e) { return e.header == columnName; }).distinct;
				
				var olLayer = $scope.map.getLayers().getArray().find(function(e) { return e.name == layerName; });
				var olSource = olLayer.getSource();
				var olFeatures = olFeatures = olSource.getFeatures();
				var featureMapByAnimatedCol = new Map();
				
				for (var olFeature of olFeatures) {
					var columnNameVal = olFeature.get(columnName).value;
					
					if (!featureMapByAnimatedCol.has(columnNameVal)) {
						featureMapByAnimatedCol.set(columnNameVal, []);
					}
					
					var oldStyleFunction = olFeature.getStyleFunction();
					olFeature.set("_animation_old_style_func", oldStyleFunction);
					
					olFeature.setStyle((feature, resolution) => {
						var styleFunct = cockpitModule_mapThematizerServices.layerStyle;
					
						return styleFunct(feature, resolution);
					});
					
					featureMapByAnimatedCol.get(columnNameVal).push(olFeature);
				}
				
				var styleHidden  = new ol.style.Style({ visibility: 'hidden' });
				var styleVisible = new ol.style.Style({ visibility: 'visible' });
				
				setTimeout(() => {
					$scope.animationLoop(0, animationStatus, distinctValues, styleHidden, styleVisible, featureMapByAnimatedCol);
				}, 100);
			}
		}

		$scope.animationLoop = function(index, animationStatus, distinctValues, styleHidden, styleVisible, featureMapByAnimatedCol) {

			i = distinctValues[index];
			animationStatus.currValue = i;

			var valuesToHide = distinctValues.filter(function(e) { return e != i; });

			featureMapByAnimatedCol.get(i).forEach(function(e) { e.setStyle(null); });

			for (var j of valuesToHide) {
				featureMapByAnimatedCol.get(j).forEach(function(e) { e.setStyle(styleHidden); });
			}

			index++;
			
			if (index == distinctValues.length) {
				index = 0;
			} 

			$scope.map.render();

			if (animationStatus.inPlay) {
				setTimeout(() => {
					$scope.animationLoop(index, animationStatus, distinctValues, styleHidden, styleVisible, featureMapByAnimatedCol);
				}, 1000);
			} else {
				animationStatus.currValue = null;
				featureMapByAnimatedCol.forEach(function(value, key) {
					value.forEach(function(e) {
						var oldStyleFunction = e.get("_animation_old_style_func");
						e.setStyle(oldStyleFunction);
					});
				});
			}

			$scope.$digest();

		}

		$scope.hasAnimatedLayer = function() {
			var layers = $scope.ngModel.content.layers || [];

			return layers.find($scope.isLayerAnimated) != undefined;
		}

		$scope.isLayerAnimated = function(layer) {
			var columns = layer.content.columnSelectedOfDataset;
			return columns.find($scope.isColumnAnimated) != undefined;
		}
		
		$scope.isColumnAnimated = function(column) {
			return column.properties.animateOn == true;
		}

		$scope.hasPerLayerFilters = function(ds) {

			var dsIds = [];

			if (ds != undefined) {
				// If a ds is specified, checks its dsId only
				dsIds.push(ds.dsId);
			} else {
				// Else checks all dsId of all layers
				var layers = $scope.ngModel.content.layers;
				for (var currLayerIdx in layers) {
					var currLayer = layers[currLayerIdx];
					if (!$scope.isFilterDisabled(currLayer)) {
						dsIds.push(currLayer.dsId);
					}
				}
			}

			for (var currDsIdIdx in dsIds) {
				var currDsId = dsIds[currDsIdIdx];
				var cols = $scope.getColumnSelectedOfDataset(currDsId);
				for (var currColIdx in cols) {
					var currCol = cols[currColIdx];
					if ($scope.isFilterableCol(currCol)) {
						return true;
					}
				}
			}

			return false;
		}

		$scope.getPerLayerFilters = function(ds) {

			var dsName = ds.name;

			if (!(dsName in $scope.perLayerFilters)) {
				var dsId = ds.dsId;
				var ret = [];

				var cols = $scope.getColumnSelectedOfDataset(dsId);
				for (var currColIdx in cols) {
					var currCol = cols[currColIdx];
					if ($scope.isFilterableCol(currCol)) {
						ret.push(currCol);
					}
				}
				$scope.perLayerFilters[dsName] = ret;
			}

			return $scope.perLayerFilters[dsName];
		}

		// Filterable columns
		$scope.perLayerFilters = {};
		// Cache filter values
		$scope.perLayerFiltersValues = {};
		// Contains selected values by the user
		$scope.selectedFilterValues = {};
		// Current search value
		$scope.searchFilterValue = "";

		$scope.clearSearchFilterValue = function() {
			$scope.searchFilterValue = "";
		}

		$scope.resetFilter = function() {
			$scope.perLayerFilters = {};
			$scope.perLayerFiltersValues = {};
			$scope.selectedFilterValues = {};
		}

		$scope.getPerLayerFiltersValues = function(layer, col) {

			var layerName = layer.name;
			var colName = col.name;
			var stats = $scope.dataSetStats[layerName];
			var statsValues = Object.values(stats);
			var distinctValues = statsValues.find(function(e) { return e.header == colName; }).distinct;

			return Promise.resolve(distinctValues);

		}

		$scope.filterLayerBy = function(currLayer) {

			var layerName = currLayer.name;
			var layer = $scope.getLayerByName(layerName);

			var filtersOrder = $scope.getPerLayerFilters(currLayer)
				.map(function(elem) { return elem.name; });

			var filters = layer.filterBy;

			var source = layer.getSource();
			var features = source.getFeatures();
			features.forEach(function(feature) {

				var currStyle = null;

				// Respect the order set by the user
				for (var j in filtersOrder) {
					var currFilterName = filtersOrder[j];

					if (!(currFilterName in filters)) {
						continue;
					}

					// Filters are in AND-condition
					//
					// A feature is visible only when all filters
					// are respected.
					for (var i in filters) {
						var currFilterVal = filters[i];
						var propVal = feature.get(i).value;

						if (currFilterVal != undefined
								&& currFilterVal.length > 0
								&& currFilterVal.indexOf(propVal) == -1) {
							currStyle = new ol.style.Style({ visibility: 'hidden' });
							break;
						}
					}
				}

				feature.setStyle(currStyle);

			});

			source.changed();

		}

		$scope.openMultiSelectFilterValueDialog = function(ev, currLayer, currCol){
			var olLayer = $scope.getLayerByName(currLayer.name);
			var filterBy = olLayer.filterBy;

			$mdDialog.show({
				controller: MultiSelectFilterValueDialogController,
				fullscreen: false,
				templateUrl: $scope.getTemplateUrl('mapWidgetMultiSelectFilterValueDialogTemplate'),
				parent: angular.element(document.body),
				targetEvent: ev,
				clickOutsideToClose: true,
				bindToController: true,
				locals: {
					actualSelection: filterBy[currCol.name] || [],
					perLayerFiltersValues: $scope.perLayerFiltersValues,
					currLayer: currLayer,
					currCol: currCol,
					title: currCol.name
				}
			}).then(function(selectedFields) {
				filterBy[currCol.name] = selectedFields;
				$scope.filterLayerBy(currLayer)
			},function(pendingSelection){
			});


		}

		function MultiSelectFilterValueDialogController(
				$rootScope,
				scope,
				$mdDialog,
				$filter,
				sbiModule_translate,
				actualSelection,
				perLayerFiltersValues,
				currLayer,
				currCol,
				title) {

			scope.translate = sbiModule_translate;
			scope.perLayerFiltersValues = perLayerFiltersValues;
			scope.currLayer = currLayer;
			scope.currCol = currCol;

			scope.selectables = [];
			scope.allSelected = false;

			scope.loading = true;

			$scope.getPerLayerFiltersValues(currLayer, currCol)
				.then(function(allAvailablesValues) {

					scope.selectables =
						allAvailablesValues.map(function(elem) {
							return {
								value: elem,
								selected: actualSelection.indexOf(elem) != -1
							};
						});

					// If sizes match, all items are already selected
					var allAvailablesValuesSize = allAvailablesValues.length;
					var selectablesSize = scope.selectables
						.filter(function(elem) { return elem.selected; })
						.length;

					scope.allSelected = allAvailablesValuesSize == selectablesSize;

					scope.loading = false;
				});

			scope.close = function() {
				var ret = scope.selectables
					.filter(function(elem) {
						return elem.selected;
					})
					.map(function(elem) {
						return elem.value;
					});

				$mdDialog.hide(ret);
			};

			scope.cancel = function(){
				$mdDialog.cancel();
			};

			scope.selectAll = function(){
				scope.allSelected = !scope.allSelected;
				for(var s in scope.selectables) {
					scope.selectables[s].selected = scope.allSelected;
				}
			}

		}

		$scope.dragUtils = { dragObjectType:undefined };
		$scope.dropCallback = function(event, newIndex, list, item, external, type, currLayer) {
			$scope.perLayerFilters[currLayer.name] = $scope.perLayerFilters[currLayer.name]
				.filter(function(elem) {
					return elem.name != item.name;
				});
			$scope.perLayerFilters[currLayer.name].splice(newIndex, 0, item);

			$scope.filterLayerBy(currLayer)
		}

		$scope.getPropValue = function(prop) {
			return $scope.getPropValueFromProps($scope.props, prop);
		}

		$scope.getPropValueFromProps = function(props, prop) {
			// Props can be an empty object
			if (!(prop.name in props)) {
				return null;
			}

			var currProp = props[prop.name];
			var currPropValue = currProp.value;
			var ret = "";
			if (prop.style) {
				var style = prop.style;
				var prefix = style.prefix;
				var suffix = style.suffix;
				var precision = style.precision;
				var asString = style.asString;

				if (asString == undefined) {
					asString = false;
				}
				if (precision == undefined) {
					precision = 0;
				}

				if (prefix != undefined) {
					ret += "" + prefix + "";
				}

				if (asString) {
					ret += currPropValue;
				} else {
					ret += isNaN(currPropValue) ? currPropValue : $filter("number")(currPropValue, precision);
				}

				if (suffix != undefined) {
					ret += "" + suffix;
				}

			} else {
				ret = currPropValue;
			}
			return ret;
		}

		$scope.isFilterDisabled = function(layerDef) {
			var dsId = layerDef.dsId;
			var isCluster = $scope.isCluster(layerDef);
			var isHeatmap = $scope.isHeatmap(layerDef);
			var isExploded = $scope.exploded.hasOwnProperty(dsId) ? $scope.exploded[dsId] : true;

			return (isCluster || isHeatmap) && !isExploded;
		}

		$scope.isHeatmap = function(layerDef) {
			return (layerDef.heatmapConf && layerDef.heatmapConf.enabled) ? true : false;
		}

		$scope.isCluster = function(layerDef) {
			return (layerDef.clusterConf && layerDef.clusterConf.enabled) ? true : false;
		}

		$scope.setMapView = function() {
			var newView = new ol.View({
				center: $scope.ngModel.content.currentView.center,
				zoom: $scope.ngModel.content.currentView.zoom || 3
			});

			$scope.map.setView(newView);
		}

		$scope.getZoomFactor = function() {
			return ($scope.ngModel.content.zoomFactor || 1);
		}

		$scope.createZoomControl = function() {
			var delta = $scope.getZoomFactor();

			return new ol.control.Zoom({
				delta: delta
			});
		}

		$scope.createScaleControl = function() {
			return new ol.control.ScaleLine();
		}

		$scope.createMouseWheelZoomInteraction = function() {
			var delta = $scope.getZoomFactor();

			return new ol.interaction.MouseWheelZoom({
				maxDelta: delta
			});
		}

		$scope.setZoomControl = function() {

			if (!(typeof $scope.zoomControl == "undefined")) {
				$scope.map.removeControl($scope.zoomControl);
			}

			$scope.zoomControl = $scope.createZoomControl();

			$scope.map.addControl($scope.zoomControl);
		}

		$scope.setScaleControl = function() {

			if (!(typeof $scope.scaleControl == "undefined")) {
				$scope.map.removeControl($scope.scaleControl);
			}

			if ($scope.ngModel.content.showScale) {
				$scope.scaleControl = $scope.createScaleControl();

				$scope.map.addControl($scope.scaleControl);
			} else {
				$scope.scaleControl = undefined;
			}
		}

		$scope.setMouseWheelZoomInteraction = function() {

			$scope.map.removeInteraction($scope.mouseWheelZoomInteraction);

			$scope.mouseWheelZoomInteraction = $scope.createMouseWheelZoomInteraction();

			$scope.map.addInteraction($scope.mouseWheelZoomInteraction);
		}

		$scope.isTargetLayer = function(layer) {
			return layer && layer.targetDefault || false;
		}

		// In edit mode, if a remove dataset from cokpit it has to be deleted also from widget
		if (cockpitModule_properties.EDIT_MODE) {
			$scope.$watchCollection("cockpitModule_template.configuration.datasets", function (newValue, oldValue, $scope) {
				var newIds = [];
				for (var i in newValue) {
					newIds.push(newValue[i].dsId);
				}
				var changed = false;
				for (var i in $scope.ngModel.content.layers) {
					var currDsId = $scope.ngModel.content.layers[i].dsId;
					if (newIds.indexOf(currDsId) == -1) {
						$scope.ngModel.content.layers.splice(i, 1);
						changed = true;
					}
				}
				if (changed) {
					$scope.reinit();
				}
			});
		}

		$scope.getPerWidgetDatasetIds = function() {
			return $scope.ngModel.content.layers
				&& $scope.ngModel.content.layers.map(function(e) { return e.dataset.id.dsId; })
				|| [];
		}

		$scope.hideLegend = function() {
			$scope.isShowLegend = false;
		}

		$scope.showLegend = function() {
			$scope.isShowLegend = true;
		}

		// Manage resize of the window
		window.addEventListener('resize', function(){
			setTimeout( function() { if ($scope.map) { $scope.map.updateSize(); } }, 200);
		});

		$scope.hasMeasures = function(layer) {
			return layer.content.columnSelectedOfDataset.some(function(e) { return e.properties && e.properties.showMap == true; });
		}

		$scope.getMapLayout = function() {
			var ret = "layout-row";
			
			if ($scope.isShowLegend
				&& ($scope.ngModel.style.legend.position == "north" || $scope.ngModel.style.legend.position == "south")) {
				ret = "layout-column";
			}
			
			return ret;
		}

	}
	
	// this function register the widget in the cockpitModule_widgetConfigurator factory
	addWidgetFunctionality("map",{'initialDimension':{'width':20, 'height':20},'updateble':true,'cliccable':true});
})();