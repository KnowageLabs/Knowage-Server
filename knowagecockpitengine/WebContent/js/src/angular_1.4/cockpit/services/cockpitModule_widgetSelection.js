angular.module("cockpitModule").service("cockpitModule_widgetSelection",function(sbiModule_translate,sbiModule_restServices,cockpitModule_template, $q, $mdPanel,$rootScope,cockpitModule_properties,cockpitModule_widgetSelectionUtils,cockpitModule_templateServices,cockpitModule_realtimeServices,sbiModule_messaging){
	var ws=this;

	this.getSelectionLoadAssociative = function(){
		return cockpitModule_template.configuration.aggregations;
	}
	this.getCurrentSelections = function(datasetLabel){
		for(var i=0;i< cockpitModule_widgetSelectionUtils.responseCurrentSelection.length;i++){
			if(cockpitModule_widgetSelectionUtils.responseCurrentSelection[i].hasOwnProperty(datasetLabel)){
				return cockpitModule_widgetSelectionUtils.responseCurrentSelection[i];
			}
		}
		return {};
	}
	this.getCurrentFilters = function(datasetLabel){
		var toRet={};
		if(cockpitModule_template.configuration.filters[datasetLabel]!=undefined && Object.keys(cockpitModule_template.configuration.filters[datasetLabel]).length>0){
			toRet[datasetLabel]={};
			for(col in cockpitModule_template.configuration.filters[datasetLabel]){
				toRet[datasetLabel][col]=["('"+cockpitModule_template.configuration.filters[datasetLabel][col]+"')"]
			}
		}
		return toRet;
	}
	this.getAggregation = function(ngModel,dataset,columnOrdering, reverseOrdering){
		var measures = [];
		var categories = [];
		var ds = dataset.label;
		
		var columns = ngModel==undefined ? undefined : ngModel.content.columnSelectedOfDataset;
		
		if(columns != undefined){
			//create aggregation
			for(var i=0;i<columns.length;i++){
				var col = columns[i];
				var obj = {};
				obj["id"] = col.alias;
				obj["alias"] = col.alias;
				obj["orderType"] = "";
				if(col.isCalculated == true){
					obj["columnName"] = col.formula;
				}else{
					obj["columnName"] = col.name;
				}
				if(columnOrdering !=undefined){
					if(columnOrdering.name == col.name){
						obj["orderType"] = reverseOrdering==true ? 'ASC' : 'DESC';
					}
				}
			
				if(col.fieldType=="ATTRIBUTE"){
					categories.push(obj)
				}else{
					//it is measure
					obj["funct"] = col.aggregationSelected.toUpperCase();
					measures.push(obj);
				}
			}
		}
		var result = {};
		result["measures"] = measures;
		result["categories"] = categories;
		result["dataset"] = ds;

		return result;

	}
	
	
	this.getAssociations=function(reloadSelection,tmpObj){
		var payload = {};
		payload["items"] = tmpObj==undefined ? cockpitModule_template.configuration.associations: tmpObj.associations;
		
		sbiModule_restServices.promisePost("1.0/associations","", payload)
		.then(function(response){
			if(tmpObj!=undefined){
				ws.updateAggregation(response.data,tmpObj.tmpAggregations,tmpObj.currentDsList,false);
				angular.copy(response.data,tmpObj.tmpAggregations);
			}else{
				angular.copy(response.data,cockpitModule_widgetSelectionUtils.associations);
				var someDel= ws.updateAggregation(response.data,cockpitModule_template.configuration.aggregations,cockpitModule_template.configuration.datasets,true);
				angular.copy(response.data,cockpitModule_template.configuration.aggregations);
				if(reloadSelection || (!reloadSelection && someDel)){
					cockpitModule_widgetSelectionUtils.responseCurrentSelection = [];
					ws.refreshAllAssociations();
					
				}
			}
		},function(response){
			sbiModule_restServices.errorHandler(response.data,"");
			
		})
	}
	
	this.removeDatasetFromFilters=function(dsList){
		var someDelete=false;
		angular.forEach(dsList,function(ds){
			if(cockpitModule_template.configuration.filters.hasOwnProperty(ds)){
				someDelete=true;
				delete cockpitModule_template.configuration.filters[ds];
			}
		})
		//return true if some filters are deleted
		return someDelete;
	}
	this.updateAggregation=function(newAggr,oldAggr,dsList,updateFilters){
		var someDelete=false;
		
		angular.forEach(newAggr,function(newItem){
			//remove the associations item of respose
			delete newItem.associations;
			if(updateFilters){
				var dl=ws.removeDatasetFromFilters(newItem.datasets);
				if(dl){
					someDelete=true;
				}
			}
			//get the old value
			for(var i=0;i<oldAggr.length;i++){
				if(ws.arrayContainsAll(oldAggr[i].datasets,newItem.datasets)){
					newItem.frequency=oldAggr[i].frequency;
					newItem.selection=oldAggr[i].selection;
					break;
				}
			}
			
			//create new val if not exist
			if(newItem.selection==undefined){
				newItem.selection={};
			}
			if(newItem.frequency==undefined){
				var minFreq={value:-1};
				angular.forEach(newItem.datasets,function(ds){
					var dsF=ws.getRealTimeFrequency(ds,dsList)
					if(this.value==-1){
						if(dsF>-1){
							this.value=dsF;
						}
					}else{
						if(dsF!=-1 && dsF<this.value){
							this.value=dsF;
						}
					}
					
				},minFreq)
				if(minFreq.value!=-1){
					newItem.frequency=minFreq.value;
				}
			}
			
		})
		return someDelete;
	}
	
	this.arrayContainsAll=function(arr1,arr2){
		if(arr1.length!=arr2.length){
			return false;
		}
		for(var i=0;i<arr1.length;i++){
			if(arr2.indexOf(arr1[i])==-1){
				return false;
			}
		}
		return true;
	}
	
	this.getRealTimeFrequency=function(dsLabel,dsList){
		for(var i=0;i<dsList.length;i++){
			//dsList can be the dataset of template or the dataset of cockpitModule_datasetServices.getAvaiableDatasets()
			if(angular.equals(dsList[i].dsLabel,dsLabel) || angular.equals(dsList[i].label,dsLabel)){
				return !dsList[i].useCache ?  (dsList[i].frequency || -1) : -1;
			}
		}
		return -1;
	}
	 
	this.haveSelection=function(){
		for(var i=0;i<cockpitModule_template.configuration.aggregations.length;i++){
			if(Object.keys(cockpitModule_template.configuration.aggregations[i].selection).length>0){
				return true;
			}
		}
		return false;
	}
	
	this.haveFilters=function(){
		return Object.keys(cockpitModule_template.configuration.filters).length>0;
	}
	
	this.getAssociations((this.haveSelection() || this.haveFilters()));
	
	this.getAssociativeSelections = function(column,columnName,datasetLabel){
		var defer = $q.defer();
		
		//check if all associated widget alre loaded
		var assoc=ws.getDatasetAssociation(datasetLabel);
		if(assoc!=undefined){
			var assDs=assoc.datasets
			var originalDSInCache=angular.copy(cockpitModule_properties.DS_IN_CACHE);
			var tmpSplittedDSInCache=angular.copy(cockpitModule_properties.DS_IN_CACHE);
			var naDSW= cockpitModule_templateServices.getDatasetAssociatedNotUsedByWidget();
			for(var i=0;i<assDs.length;i++){
				var dsIndex=tmpSplittedDSInCache.indexOf(assDs[i]);
				if(dsIndex==-1){
					//check if is not used by widget
					if(!found && naDSW.indexOf(assDs[i])!=-1){
						break;
					}
					//check if is document
					var found=false;
					for(var x=0;x<assoc.associations.length;x++){
						 for(var y=0;y<assoc.associations[x].fields.length;y++){
							if(angular.equals(assoc.associations[x].fields[y].store,assDs[i]) && angular.equals(assoc.associations[x].fields[y].type,"document")){
								found=true;
								break;
							}
						}
						if(found){
							break;
						}
					}
					if(!found){
						sbiModule_messaging.showWarningMessage(sbiModule_translate.load("sbi.cockpit.wait.loading.association.group"));
					return 
					}
					
				}
				tmpSplittedDSInCache.splice(dsIndex,1);
			}
			//remove the dataset from the DS_IN_CACHE variable
			angular.copy(tmpSplittedDSInCache,cockpitModule_properties.DS_IN_CACHE);
		}else{
			return "noAssoc";
		}
		
		
		var key = datasetLabel+"."+columnName
		var array = [];
		array.push(column);
		ws.addValueToSelection(key , array, datasetLabel);
		this.loadAssociativeSelection(defer,ws.getDatasetAssociation(datasetLabel));
		defer.promise.then(function(){
			
		},function(){
			if(assoc!=undefined){
				angular.copy(originalDSInCache,cockpitModule_properties.DS_IN_CACHE);
			}
		})
		
		return defer.promise;

	}
	
	this.addValueToSelection = function(key, value, dsLabel){
		for(var i=0;i<cockpitModule_template.configuration.aggregations.length ; i++){
			if(cockpitModule_template.configuration.aggregations[i].datasets.indexOf(dsLabel)!=-1){
				cockpitModule_template.configuration.aggregations[i].selection[key] = value;

			}
		}

	}
	
	this.refreshAllWidgetWhithSameDataset=function(itemLabel){
		$rootScope.$broadcast('WIDGET_EVENT','UPDATE_FROM_DATASET_FILTER',{label:itemLabel});
	}
	
	this.refreshAllAssociatedWidget = function(isInit,data){
		$rootScope.$broadcast('WIDGET_EVENT','UPDATE_FROM_SELECTION',{isInit:isInit,data:data});
	}
	
	this.execRefreshAllAssociations = function(isInit){
		console.log("in: execRefreshAllAssociations",(new Date()).getTime());
		var  assRefCount=0;
		angular.forEach(cockpitModule_widgetSelectionUtils.associations, function(item,index){
			var defer = $q.defer();
			ws.loadAssociativeSelection(defer, item)
			defer.promise.then(function(){
				assRefCount++;
				if(angular.equals(assRefCount,cockpitModule_widgetSelectionUtils.associations.length)){
					ws.refreshAllAssociatedWidget(isInit);
				} 
			})
			})
	}
	
	this.refreshAllAssociations = function(){
		console.log("in: refreshAllAssociation",(new Date()).getTime());
		if(cockpitModule_properties.all_widget_initialized==true){
			console.log("do: refreshAllAssociation",(new Date()).getTime());
			ws.execRefreshAllAssociations(false);
		}else{
			var AWI=$rootScope.$on('ALL_WIDGET_INITIALIZED',function(){
				console.log("do: refreshAllAssociation",(new Date()).getTime());
				ws.execRefreshAllAssociations(true);
				AWI();
			});
			
		}
		
	}
	
	
	
	this.loadCurrentSelections = function(dsLabel){
		var defer = $q.defer();
		if(cockpitModule_template.configuration.aggregations == undefined){
			defer.resolve({});
			return defer.promise;
		}
		
//		var associationsEncoded=encodeURIComponent(JSON.stringify(associations[0]))
		this.loadAssociativeSelection(defer,ws.getDatasetAssociation(dsLabel));
		return defer.promise;
	}
	
	this.getParameterFromDataset=function(dsList){
		var toret={};
		for(var i=0;i<cockpitModule_template.configuration.datasets.length;i++){
			if(dsList.indexOf(cockpitModule_template.configuration.datasets[i].dsLabel)!=-1){
				toret[cockpitModule_template.configuration.datasets[i].dsLabel]=cockpitModule_template.configuration.datasets[i].parameters
			}
		}
		return toret;
	}
	this.checkIfDatasetAreLoaded = function(){
		cockpitModule_templateServices.getDatasetNotInCache();
		
		for(var i=0;i<cockpitModule_properties.DS_NOT_IN_CACHE.length;i++){
			var dsLabel = cockpitModule_properties.DS_NOT_IN_CACHE[i];
			
		}
	}
	
	this.loadAssociativeSelection = function(defer,ass){
		
		if(ass==undefined){
			defer.reject();
			return;
		}
		
		var dsSel=ws.getSelection(ass.datasets);
		if(Object.keys(dsSel).length>0){
			var selection = encodeURIComponent(JSON.stringify(dsSel))
			.replace(/'/g,"%27")
			.replace(/"/g,"%22");
			var associationsEncoded=encodeURIComponent(JSON.stringify(ass))
			.replace(/'/g,"%27")
			.replace(/"/g,"%22");
			var datasets = encodeURIComponent(JSON.stringify(ws.getParameterFromDataset(ass.datasets)))
			.replace(/'/g,"%27")
			.replace(/"/g,"%22");
			var realTimeDs=encodeURIComponent(JSON.stringify(cockpitModule_realtimeServices.getRealTimeDatasetFromList(ass.datasets)))
			.replace(/'/g,"%27")
			.replace(/"/g,"%22");
			
			var param = "?associationGroup="+associationsEncoded+"&selections="+selection+"&datasets="+datasets+"&realtime="+realTimeDs;
			
			sbiModule_restServices.restToRootProject();
			sbiModule_restServices.promiseGet("2.0/datasets","loadAssociativeSelections"+param)
			.then(function(response){
				var index = ws.currentSelectionContainsAss(response.data);
				if(index==-1){
					cockpitModule_widgetSelectionUtils.responseCurrentSelection.push(response.data);
				}else{
					cockpitModule_widgetSelectionUtils.responseCurrentSelection[index] = response.data;
				}
				defer.resolve(response.data);
			},function(response){
				sbiModule_restServices.errorHandler(response.data,"");
				defer.reject();
			})
		}else{
			var objDS={};
			angular.forEach(ass.datasets,function(item){
				this[item]={};
			},objDS)
			angular.copy([],cockpitModule_widgetSelectionUtils.responseCurrentSelection);
			defer.resolve(objDS);
		}
		
		
	}
	
	this.currentSelectionContainsAss = function(data){
		for(var i=0;i<cockpitModule_widgetSelectionUtils.responseCurrentSelection.length;i++){
			if(angular.equals(Object.keys(data), Object.keys(cockpitModule_widgetSelectionUtils.responseCurrentSelection[i]))){
				return i;
			}
		}
		return -1;
	}
	this.getSelectionByDsLabel =function(label){
		for(var j=0;j<cockpitModule_widgetSelectionUtils.associations.length;j++){
			if(cockpitModule_widgetSelectionUtils.associations[j].datasets.indexOf(label) != -1){
				this.getSelection(cockpitModule_widgetSelectionUtils.associations[j].datasets);
			}
		}
		return {};
	}
	this.getSelection = function(associations){
		for(var i = 0;i<cockpitModule_template.configuration.aggregations.length;i++){
			if(angular.equals(cockpitModule_template.configuration.aggregations[i].datasets, associations)){
				return cockpitModule_template.configuration.aggregations[i].selection;
			}
		}
		return {};
	}
	
	this.getDatasetAssociation=function(dsLabel){
		for(var i=0;i<cockpitModule_widgetSelectionUtils.associations.length;i++){
			if(cockpitModule_widgetSelectionUtils.associations[i].datasets.indexOf(dsLabel)!=-1){
				return cockpitModule_widgetSelectionUtils.associations[i];
			}
		}
	}
	
	
})