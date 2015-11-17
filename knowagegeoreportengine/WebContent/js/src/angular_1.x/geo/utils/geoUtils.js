var geoM=angular.module('geo_module');

geoM.service('geoReportCompatibility',function($map){
	 this.resolveCompatibility=function(geo_template){
			//     transform indicators in array of json if they arent
		    if(geo_template.hasOwnProperty('indicators') && geo_template.indicators.length>0){
		    	var tmp=[];
		    	if(Object.prototype.toString.call(geo_template.indicators[0])=="[object Array]"){
		    		for(var i=0;i<geo_template.indicators.length;i++){
		    			tmp.push({name:geo_template.indicators[i][0],label:geo_template.indicators[i][1]});
		    		}
		    		geo_template.indicators=tmp;
		    	}
		    }
		    
		    if(geo_template.hasOwnProperty("geoId")){
				geo_template.layer_join_columns=geo_template.geoId;
				delete geo_template.geoId;
			} 
		    
		    if(geo_template.hasOwnProperty("businessId")){
				geo_template.dataset_join_columns=geo_template.businessId;
				delete geo_template.businessId;
			} 
		    
		    
			
		  //compatibility of baseLayersConf with old template
		    if(geo_template.hasOwnProperty("baseLayersConf") && geo_template.baseLayersConf.length!=0 ){
				for(var i=0;i<geo_template.baseLayersConf.length;i++){
					
					if(geo_template.baseLayersConf[i].hasOwnProperty("name")){
						geo_template.baseLayersConf[i].label=geo_template.baseLayersConf[i].name;
						delete geo_template.baseLayersConf[i].name;
					} 
					
					if(geo_template.baseLayersConf[i].hasOwnProperty("options")){
						geo_template.baseLayersConf[i].layerOptions=geo_template.baseLayersConf[i].options;
						delete geo_template.baseLayersConf[i].options;
					} 
					
					if(geo_template.baseLayersConf[i].hasOwnProperty("url")){
						geo_template.baseLayersConf[i].layerURL=geo_template.baseLayersConf[i].url;
						delete geo_template.baseLayersConf[i].url;
					} 
					
					if(geo_template.baseLayersConf[i].hasOwnProperty("isBaseLayer")){
						geo_template.baseLayersConf[i].baseLayer=geo_template.baseLayersConf[i].isBaseLayer;
						delete geo_template.baseLayersConf[i].isBaseLayer;
					} 
				}
			}
			
		}
});

geoM.service('geoReportUtils',function(baseLayer,$map,sbiModule_restServices,$q,sbiModule_logger,geo_template,geo_indicators,geo_filters,geo_dataset,geo_dataset,dataset_join_columns_item,layerServices){
	var gru=this;
	 this.osm_getTileURL= function(bounds) {
			var res = $map.getResolution();
			var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
			var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
			var z = $map.getZoom();
			var limit = Math.pow(2, z);

			if (y < 0 || y >= limit) {
				console.log("####################### implementare  OpenLayers.Util.getImagesLocation() + ''404.png'")
//				return OpenLayers.Util.getImagesLocation() + "404.png";
			} else {
				x = ((x % limit) + limit) % limit;
				return this.url + z + "/" + x + "/" + y + "." + this.type;
			}
		}
	 
	 function getFeatureIdsFromStore(){
		 var elem=[];
		 if(geo_dataset.hasOwnProperty("metaData")){
			 if(geo_dataset.metaData.hasOwnProperty("fields")){
				 var storeIdFiledName;
				 var fields=geo_dataset.metaData.fields;
				 for( var i=0;i<fields.length;i++){
					 if(fields[i].hasOwnProperty("header") && fields[i].header==geo_template.dataset_join_columns){
						 storeIdFiledName = fields[i].name; 
						 break;
					 }
				 }
				 
				 for(var i=0;i<geo_dataset.rows.length;i++){
					 if(geo_dataset.rows[i].hasOwnProperty(storeIdFiledName) && elem.indexOf(geo_dataset.rows[i][storeIdFiledName])==-1){
						 elem.push(geo_dataset.rows[i][storeIdFiledName])
					 }
					 
				 }
			 }

		 }
		 return elem;
		 
	 }
	 
	this.GetTargetLayer=function(){
		var params = {
	     		layer: geo_template.targetLayerConf.label
	     		, layer_join_columns: geo_template.layer_join_columns
	     	};
		
		params.featureIds=getFeatureIdsFromStore();
		
		sbiModule_restServices.post("1.0/geo", 'GetTargetLayer',params).success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						sbiModule_logger.log("GetTargetLayer non Ottenuto");
					} else {
					sbiModule_logger.trace("GetTargetLayer caricato",data);
				
						layerServices.setTemplateLayer(data); 
						
					}
					
				}).error(function(data, status, headers, config) {
					sbiModule_logger.log("GetTargetLayer non Ottenuto");
				});
 }
	
	 
	 this.GetTargetDataset=function(){
		
		 sbiModule_restServices.get("1.0/geo", 'GetTargetDataset').success(
					function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
							sbiModule_logger.log("dataset non Ottenuto");
							alert("Errore nel recupero del dataset"+data);
						} else {
							Object.assign(geo_dataset, data); 
						sbiModule_logger.trace("dataset caricato",data);		
						gru.initRigthMenuVariable();
						gru.GetTargetLayer();
						
						}
					}).error(function(data, status, headers, config) {
						sbiModule_logger.log("dataset non Ottenuto");
					});
	 }
	 
	 this.initRigthMenuVariable=function(){
		 if(geo_dataset.hasOwnProperty("metaData")){
			 if(geo_dataset.metaData.hasOwnProperty("fields")){
				 var fields=geo_dataset.metaData.fields;
				 
				 //search if in template are present indicator and just load them, else load all
				 var templ_indic=[];
				 if(geo_template.hasOwnProperty("indicators")){
					 for(var j=0;j<geo_template.indicators.length;j++){
						 templ_indic.push(geo_template.indicators[j].name);
					 }
				 }
				 
				//search if in template are present filters and just load them, else load all
				 var templ_filters=[];
				 if(geo_template.hasOwnProperty("filters")){
					 for(var j=0;j<geo_template.filters.length;j++){
						 templ_filters.push(geo_template.filters[j].name);
					 }
				 }
				 
				 for( var i=0;i<fields.length;i++){
					 if(fields[i].hasOwnProperty("role")){
						 if(fields[i].role=="MEASURE"){
							 if(templ_indic.length==0 || templ_indic.indexOf(fields[i].header)>-1){
								 geo_indicators.push(fields[i]);
							 }
							
						 }else if(fields[i].role=="ATTRIBUTE"){
							 if(templ_filters.length==0 || templ_filters.indexOf(fields[i].header)>-1){
									geo_filters.push(fields[i]);
									if(!geo_template.selectedFilters.hasOwnProperty(fields[i].name)){
										geo_template.selectedFilters[fields[i].name]="-1";
									}
							 }
							 
							 //if this measure is the dataset_join_columns load the variable
							 if(fields[i].header==geo_template.dataset_join_columns){
								 Object.assign(dataset_join_columns_item, fields[i]); 
							 }
						 }else{
							 console.error("dataset->metaData->fields->role="+fields[i].role+"    not managed ") 
							}
					 }
				 }
				 
				 //select first indicator if not selected in template
				 if(geo_template.selectedIndicator==undefined && geo_indicators.length!=0){
					 geo_template.selectedIndicator=geo_indicators[0];
				 }
				 
			 }else{
				 console.error("fields property in metaData property of dataset not present") 
			 }
			 
		 }else{
			 console.error("metaData property non present in dataset")
		 }
		 
	 }	
});

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