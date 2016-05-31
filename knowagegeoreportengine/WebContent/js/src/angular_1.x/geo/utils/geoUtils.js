/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 

var geoM=angular.module('geoModule');

/**
 * Allows user to load old templates: in a property 
 * with the new name is not found we look for the old name.
 */
geoM.service('geoReportCompatibility',function($map){
	this.resolveCompatibility=function(geoModule_template){
		//convert targetLayrConf from object to array of object if necessary
		if(!angular.isArray(geoModule_template.targetLayerConf)){
			geoModule_template.targetLayerConf=[geoModule_template.targetLayerConf];
		}
		
		
		//     transform indicators in array of json if they arent
		if(geoModule_template.hasOwnProperty('indicators') && geoModule_template.indicators.length>0){
			var tmp=[];
			if(Object.prototype.toString.call(geoModule_template.indicators[0])=="[object Array]"){
				for(var i=0;i<geoModule_template.indicators.length;i++){
					tmp.push({name:geoModule_template.indicators[i][0],label:geoModule_template.indicators[i][1]});
				}
				geoModule_template.indicators=tmp;
			}
		}

		if(geoModule_template.hasOwnProperty("geoId")){
			geoModule_template.layerJoinColumns=geoModule_template.geoId;
			delete geoModule_template.geoId;
		} 

		if(geoModule_template.hasOwnProperty("businessId")){
			geoModule_template.datasetJoinColumns=geoModule_template.businessId;
			delete geoModule_template.businessId;
		} 



		//compatibility of baseLayersConf with old template
		if(geoModule_template.hasOwnProperty("baseLayersConf") && geoModule_template.baseLayersConf.length!=0 ){
			for(var i=0;i<geoModule_template.baseLayersConf.length;i++){

				if(geoModule_template.baseLayersConf[i].hasOwnProperty("name")){
					geoModule_template.baseLayersConf[i].label=geoModule_template.baseLayersConf[i].name;
					delete geoModule_template.baseLayersConf[i].name;
				} 

				if(geoModule_template.baseLayersConf[i].hasOwnProperty("options")){
					geoModule_template.baseLayersConf[i].layerOptions=geoModule_template.baseLayersConf[i].options;
					delete geoModule_template.baseLayersConf[i].options;
				} 

				if(geoModule_template.baseLayersConf[i].hasOwnProperty("url")){
					geoModule_template.baseLayersConf[i].layerURL=geoModule_template.baseLayersConf[i].url;
					delete geoModule_template.baseLayersConf[i].url;
				} 

				if(geoModule_template.baseLayersConf[i].hasOwnProperty("isBaseLayer")){
					geoModule_template.baseLayersConf[i].baseLayer=geoModule_template.baseLayersConf[i].isBaseLayer;
					delete geoModule_template.baseLayersConf[i].isBaseLayer;
				} 
			}
		}

	}
});

/**
 * Set of method to manage 
 * 
 * */
geoM.service('geoModule_reportUtils',function(geoModule_thematizer,baseLayer,$map,sbiModule_config,sbiModule_restServices,$q,sbiModule_logger,geoModule_template,geoModule_indicators,geoModule_filters,geoModule_dataset,geModule_datasetJoinColumnsItem,geoModule_layerServices, sbiModule_translate){
	var gru=this;
	this.osm_getTileURL= function(bounds) {
		var res = $map.getResolution();
		var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
		var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
		var z = $map.getZoom();
		var limit = Math.pow(2, z);

		if (y < 0 || y >= limit) {
			console.log("####################### implementare  OpenLayers.Util.getImagesLocation() + ''404.png'")
//			return OpenLayers.Util.getImagesLocation() + "404.png";
		} else {
			x = ((x % limit) + limit) % limit;
			return this.url + z + "/" + x + "/" + y + "." + this.type;
		}
	}

	function getFeatureIdsFromStore(){
		var elem=[];
		if(geoModule_dataset.hasOwnProperty("metaData")){
			if(geoModule_dataset.metaData.hasOwnProperty("fields")){
				var storeIdFiledName;
				var fields=geoModule_dataset.metaData.fields;
				for( var i=0;i<fields.length;i++){
					if(fields[i].hasOwnProperty("header") && fields[i].header==geoModule_template.datasetJoinColumns){
						storeIdFiledName = fields[i].name; 
						break;
					}
				}

				for(var i=0;i<geoModule_dataset.rows.length;i++){
					if(geoModule_dataset.rows[i].hasOwnProperty(storeIdFiledName) && elem.indexOf(geoModule_dataset.rows[i][storeIdFiledName])==-1){
						elem.push(geoModule_dataset.rows[i][storeIdFiledName])
					}

				}
			}

		}
		return elem;

	}


	/**
	 * Loads the target layer using a REST service
	 * */
	this.GetTargetLayer=function(){
		//load Layer object from layer catalogue
		var data={items:[]};
		for(var i=0;i<geoModule_template.targetLayerConf.length;i++){
			data.items.push(geoModule_template.targetLayerConf[i].label);
		}
		
		sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath+'restful-services/');
		sbiModule_restServices.promisePost("layers", 'getLayerFromList',data).then(
				function(response, status, headers, config) {
				 		if(response.data.root[0]==undefined){
							sbiModule_restServices.errorHandler(sbiModule_translate.load("gisengine.errorLayer.errorrole"), sbiModule_translate.load("gisengine.errorLayer.error"));
						}else{
							for(var i=0;i<response.data.root.length;i++){
								var data=response.data.root[i];
								if(data.type=='WMS'){
									//if is a WMS
									geoModule_layerServices.setTemplateLayer(data); 
								}else{
									//if is a WFS or file 
									gru.getGEOJsonFromFileOrWfs(data);
								}
							}
							
						}

				},function(response, status, headers, config) {
					sbiModule_restServices.errorHandler(response.data, sbiModule_translate.load("gisengine.errorLayer.error"));
				});
	}

	/**
	 * Loads the GEOJson from wfs or file  layer using a REST service
	 * */
	this.getGEOJsonFromFileOrWfs=function(dataLayer){
		var params = {
				layer: dataLayer.label,
				layerJoinColumns: geoModule_template.layerJoinColumns,
				noDataset : (geoModule_template.noDatasetReport==true)
		};

		params.featureIds=getFeatureIdsFromStore();
		sbiModule_restServices.promisePost("1.0/geo", 'getTargetLayer',params)
		.then(
			function(response, status, headers, config) {
				
				//add the label on the response.data
				response.data.layerName=dataLayer.name; 
				response.data.properties=dataLayer.properties; 
				 geoModule_layerServices.setTemplateLayer(response.data); 
				 
			},function(response, status, headers, config) {
			sbiModule_restServices.errorHandler(response.data,"Error while attempt to load targetlayer")
		});
	}
 
	/**
	 * Loads the dataset using a REST service
	 * */
	this.getTargetDataset=function(){
		sbiModule_restServices.promiseGet("1.0/geo", 'getTargetDataset').then(
				function(response, status, headers, config) {
					  
				angular.copy(response.data,geoModule_dataset); 
				gru.initRigthMenuVariable(); 
				gru.GetTargetLayer();
						 
				},function(response, status, headers, config) {
					sbiModule_restServices.errorHandler(response,"No dataset")
				});
	};
	
	 
	/**
	 * Initialization for Indicators and Filters
	 **/
	this.initRigthMenuVariable=function(){
		if(geoModule_dataset.hasOwnProperty("metaData")){
			if(geoModule_dataset.metaData.hasOwnProperty("fields")){
				var fields=geoModule_dataset.metaData.fields;

				//search if in template are present indicator and just load them, else load all
				var templ_indic=[];
				if(geoModule_template.hasOwnProperty("indicators")){
					for(var j=0;j<geoModule_template.indicators.length;j++){
						templ_indic.push(geoModule_template.indicators[j].name);
					}
				}

				//search if in template are present filters and just load them, else load all
				var templ_filters=[];
				var templ_filters_obj={};
				if(geoModule_template.hasOwnProperty("filters")){
					for(var j=0;j<geoModule_template.filters.length;j++){
						templ_filters.push(geoModule_template.filters[j].name);
						templ_filters_obj[geoModule_template.filters[j].name]=geoModule_template.filters[j];
					}
				}

				for( var i=0;i<fields.length;i++){
					if(fields[i].hasOwnProperty("role")){
						if(fields[i].role=="MEASURE"){
							if(templ_indic.length==0 || templ_indic.indexOf(fields[i].header)>-1){
								geoModule_indicators.push(fields[i]);
							}

						}else if(fields[i].role=="ATTRIBUTE"){
							if(templ_filters.length==0 || templ_filters.indexOf(fields[i].header)>-1){

								fields[i].label=templ_filters_obj[fields[i].header]!=undefined ? templ_filters_obj[fields[i].header].label : fields[i].header;
								geoModule_filters.push(fields[i]);
								if(!geoModule_template.selectedFilters.hasOwnProperty(fields[i].name)){
									geoModule_template.selectedFilters[fields[i].name]="-1";
								}
							}

							//if this measure is the datasetJoinColumns load the variable
							if(fields[i].header==geoModule_template.datasetJoinColumns){
								angular.copy(fields[i],geModule_datasetJoinColumnsItem)
//								Object.assign(geModule_datasetJoinColumnsItem, fields[i]); 
							}
						}else{
							sbiModule_restServices.errorHandler("dataset->metaData->fields->role="+fields[i].role+"not managed ","") ;
						}
					}
				}

				//select first indicator if not selected in template
				if(geoModule_template.selectedIndicator==undefined && geoModule_indicators.length!=0){
					geoModule_template.selectedIndicator=geoModule_indicators[0];
				}

			}else{
				sbiModule_restServices.errorHandler("fields property in metaData property of dataset not present",""); 
			}

		}else{
			sbiModule_restServices.errorHandler("metaData property non present in dataset","");
		}

	}	
});

/**
 * Used for TODO
 * 
 * */
geoM.filter('unique', function () {

	return function (items, filterOn) {

		if (filterOn === false) {
			return items;
		}

		if ((filterOn || angular.isUndefined(filterOn)) && angular.isArray(items)) {
			var hashCheck = {}, newItems = [];

			var extractValueToCompare = function (item) {
				if (angular.isObject(item) && angular.isString(filterOn)) {
					return item[filterOn];
				} else {
					return item;
				}
			};

			angular.forEach(items, function (item) {
				var valueToCheck, isDuplicate = false;

				for (var i = 0; i < newItems.length; i++) {
					if (angular.equals(extractValueToCompare(newItems[i]), extractValueToCompare(item))) {
						isDuplicate = true;
						break;
					}
				}
				if (!isDuplicate) {
					newItems.push(item);
				}

			});
			items = newItems;
		}
		return items;
	};
})

/**
 * TODO add comment here
 * */
geoM.factory('geo_interaction',function(){
	var interact={
			type: "identify",
			distance_calculator: false,
			selectedFilterType: 'intersect',
			selectedFeatures: [],
			selectedFeaturesCallbackFunctions: [],
			
	};

	interact.setSelectedFeatures = function(newSelectedFeatures) {
		interact.selectedFeatures = newSelectedFeatures;

		if (interact.selectedFeaturesCallbackFunctions.length > 0) {
			for(var funcIndex = 0; funcIndex < interact.selectedFeaturesCallbackFunctions.length; funcIndex++) {
				var func = interact.selectedFeaturesCallbackFunctions[funcIndex];
				func();
			}
		}
	};

	interact.addSelectedFeaturesCallbackFunction = function(callbackFunction) {
		interact.selectedFeaturesCallbackFunctions.push(callbackFunction);
	};
	
	interact.clearSelectedFeaturesCallbackFunction = function() {
		while (interact.selectedFeaturesCallbackFunctions.length > 0) {
			delete interact.selectedFeaturesCallbackFunctions[0];
		}
	};

	return interact;
});

geoM.factory('geo_intersectFunctions', function() {
	var toReturn = {};
	
	toReturn.getFeaturesExtraction = function(newSet, oldSet) {
		var includedFeatures = [];
		var excludedFeatures = [];
		
		for(var oldSetIndex = 0; oldSetIndex < oldSet.length; oldSetIndex++) {
			var oldItem = oldSet[oldSetIndex];
			
			var hasToBeIncluded = false; 
			
			for(var newSetIndex = 0; newSetIndex < newSet.length && !hasToBeIncluded; newSetIndex++) {
				var newItem = newSet[newSetIndex];
				
				if(newItem.getId() == oldItem.getId()) {
					hasToBeIncluded = true;
				}
			}
			if(hasToBeIncluded) {
				includedFeatures.push(oldItem);
			} else {
				excludedFeatures.push(oldItem);
			}
		}
		
		return {
			'includedFeatures': includedFeatures,
			'excludedFeatures': excludedFeatures
		}
	};
	
	return toReturn;
});

geoM.service('crossNavigation', function(geoModule_template, geoModule_driverParameters, sbiModule_translate) {	
	this.navigateTo = function(selectedElements){
		 
		if(geoModule_template.crossNavigation==true){
			var crossData=[];
			if(Array.isArray(selectedElements)){
				for(var key in selectedElements){
					crossData.push(selectedElements[key].getProperties());
				}
			}else{
				crossData.push(selectedElements.getProperties());
			}
			parent.execExternalCrossNavigation(crossData,geoModule_driverParameters,undefined,geoModule_driverParameters.DOCUMENT_NAME[0]);
		}
	}
});

//geoM.service('crossNavigation', function(geoModule_template, geoModule_driverParameters, sbiModule_translate) {	
//	this.navigateTo = function(selectedElements){
//
//		var crossnav = geoModule_template.crossnav;
//
//		var multiSelect = crossnav && crossnav.multiSelect? 
//				crossnav.multiSelect : null;
//
//		if(!crossnav ) {
//			alert(sbiModule_translate.load('gisengine.crossnavigation.error.wrongtemplatedata'));
//			return;
//
//		} else {
//			var parametersAsString = '';
//
//			// Cross Navigation Static parameters
//			if(crossnav.staticParams 
//					&& (typeof (crossnav.staticParams) == 'object')) {
//
//				var staticParams = crossnav.staticParams;
//				var staticParamsKeys = Object.keys(staticParams);
//
//				for(var i = 0; i < staticParamsKeys.length; i++) {
//					var staticParameterKey = staticParamsKeys[i];
//					var staticParameterValue = staticParams[staticParameterKey];
//
//					parametersAsString += staticParameterKey + '=' + staticParameterValue + '&';
//				}
//			}
//
//			// Cross Navigation Dynamic parameters
//			if(crossnav.dynamicParams && Array.isArray(crossnav.dynamicParams)) {
//
//				var dynamicParams = crossnav.dynamicParams;
//				for(var i = 0; i < dynamicParams.length; i++) {
//					var param = dynamicParams[i];
//					var type = param.type ? param.type: 'string';
//					var delimiter = type == 'string' ? "'" : "";
//
//					if(param.scope.toLowerCase() == 'feature') {
//						if(Array.isArray(selectedElements) && multiSelect) {
//							parametersAsString += param.state + '=';
//
//							for(var elementIndex = 0; elementIndex < selectedElements.length; elementIndex++) {
//								var element = selectedElements[elementIndex];
//								var elementProperties = element.getProperties();
//
//								if (elementIndex > 0) {
//									parametersAsString += ',';
//								}
//								parametersAsString += delimiter + elementProperties[param.state] + delimiter;
//							}
//						}
//						// else selectedElements is a single feature
//						else{
//							var selectedElementProperties = selectedElements.getProperties();
//							parametersAsString += param.state + '=' + selectedElementProperties[param.state];
//						}
//
//						parametersAsString += '&';
//
//					} else if(param.scope.toLowerCase() == 'env') {
//						var paramInputName = param.inputpar;
//						var paramOutputName = param.outputpar;
//
//						//If the "paramInputName" is not set in the parameter mask (on the right side)
//						if(!geoModule_driverParameters[paramInputName]) {
//							continue;
//						} else {
//							parametersAsString += 
//								(paramOutputName ? paramOutputName : paramInputName)
//								+ '=' + geoModule_driverParameters[paramInputName] + '&';
//						}
//					}
//				}
//			}
//
//			var frameName = "iframe_"+geoModule_template.executionContext.DOCUMENT_LABEL;
//			debugger;
//			parent.execCrossNavigation(frameName, crossnav.label, parametersAsString);
//		}
//	}
//});



