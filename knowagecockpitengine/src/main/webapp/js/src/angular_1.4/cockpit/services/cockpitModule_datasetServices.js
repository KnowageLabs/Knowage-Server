angular.module("cockpitModule").service("cockpitModule_datasetServices",function(sbiModule_translate,sbiModule_util,sbiModule_i18n,sbiModule_restServices,cockpitModule_template, $filter, $q, 
$mdPanel,cockpitModule_widgetSelection,cockpitModule_properties,cockpitModule_utilstServices, $rootScope,sbiModule_messaging,sbiModule_user,cockpitModule_templateServices,driversExecutionService,cockpitModule_analyticalDrivers){
	var ds=this;

	this.datasetList=[];
	this.datasetMapById={};
	this.datasetMapByLabel={};

	this.infoColumns = [];
	this.datasetTypes = {
			"SbiQueryDataSet": sbiModule_translate.load('kn.cockpit.dataset.type.query'),
			"SbiCkanDataSet": sbiModule_translate.load('kn.cockpit.dataset.type.ckan'),
			"SbiCustomDataSet": sbiModule_translate.load('kn.cockpit.dataset.type.custom'),
			"SbiFileDataSet": sbiModule_translate.load('kn.cockpit.dataset.type.file'),
			"SbiFlatDataSet": sbiModule_translate.load('kn.cockpit.dataset.type.flat'),
			"SbiJClassDataSet": sbiModule_translate.load('kn.cockpit.dataset.type.jclass'),
			"SbiScriptDataSet": sbiModule_translate.load('kn.cockpit.dataset.type.script'),
			"SbiFederatedDataSet": sbiModule_translate.load('kn.cockpit.dataset.type.federated'),
			"SbiQbeDataSet": sbiModule_translate.load('kn.cockpit.dataset.type.qbe'),
			"SbiSolrDataSet": sbiModule_translate.load('kn.cockpit.dataset.type.solr'),
			"SbiSPARQLDataSet": sbiModule_translate.load('kn.cockpit.dataset.type.sparql'),
			"SbiRESTDataSet": sbiModule_translate.load('kn.cockpit.dataset.type.rest'),
			"SbiPythonDataSet": sbiModule_translate.load('kn.cockpit.dataset.type.python')
	}

	this.isDatasetFromTemplateLoaded = false;

	this.selectedDSWithDrivers = [];

	this.loadDatasetsFromTemplate=function(){
		var def=$q.defer();
		if(!ds.isDatasetFromTemplateLoaded){
			if(cockpitModule_template.configuration.datasets.length == 0){
				ds.isDatasetFromTemplateLoaded = true;
				def.resolve();
			}else{
				var dsIds = [];
				angular.forEach(cockpitModule_template.configuration.datasets, function(item){
					this.push(item.dsId);
				}, dsIds);

				sbiModule_restServices.restToRootProject();
				sbiModule_restServices.promiseGet("2.0/datasets", "", "asPagedList=true&seeTechnical=true&ids=" + dsIds.join())
				.then(function(response){
					for(var i in response.data.item){
						if(typeof response.data.item[i] == 'object'){
							var dataset = response.data.item[i];
							ds.datasetList.push(dataset);
							ds.datasetMapById[dataset.id.dsId] = dataset;
							ds.datasetMapByLabel[dataset.label] = dataset;
						}
					}

					ds.initNearRealTimeValues(ds.datasetList);
					ds.initSpatialAttributesFlag(ds.datasetList);
					ds.checkForDSChange();
					cockpitModule_widgetSelection.getAssociations(cockpitModule_properties.HAVE_SELECTIONS_OR_FILTERS,undefined,def);

					ds.isDatasetFromTemplateLoaded = true;
					def.resolve();

				},function(response){
					sbiModule_restServices.errorHandler(response.data,"");
					def.reject();
				});
			}
		}else{
			def.resolve();
		}
		return def.promise;
	};

	this.isDatasetListLoaded = false;

	this.loadDatasetList=function(){
		var def=$q.defer();
		if(!ds.isDatasetListLoaded){
			// --- sbiModule_user.isTechnicalUser returning "true" or "false" as STRING --- //
			sbiModule_restServices.restToRootProject();
			sbiModule_restServices.promiseGet("2.0/datasets", "", "asPagedList=true&seeTechnical=" + sbiModule_user.isTechnicalUser)
			.then(function(response){
				var allDatasets = response.data.item;
				var newDatasets = [];
				angular.forEach(allDatasets,function(item){
					var found = false;
					var itemId = item.id;
					for(var i=0; i<ds.datasetList.length; i++){
						var currId = ds.datasetList[i].id;
						if(itemId.dsId == currId.dsId
								&& itemId.versionNum == currId.versionNum
								&& itemId.organization == currId.organization){
							found = true;
							break;
						}
					}
					if(!found){
						this.push(item);
					}
				}, newDatasets);

				ds.initNearRealTimeValues(newDatasets);
				ds.initSpatialAttributesFlag(newDatasets);

				for(var k in newDatasets){
					if(newDatasets[k].id){
						ds.datasetList.push(newDatasets[k]);
						ds.datasetMapById[newDatasets[k].id.dsId] = newDatasets[k];
						ds.datasetMapByLabel[newDatasets[k].label] = newDatasets[k];
					}
				}

				ds.isDatasetListLoaded = true;
				def.resolve();

			},function(response){
				sbiModule_restServices.errorHandler(response.data,"");
				def.reject();
			});
		}else{
			def.resolve();
		}
		return def.promise;
	};

	this.initNearRealTimeValues=function(datasets){
		for(var i=0; i < datasets.length; i++){
			var dataset = datasets[i];
			if(dataset.useCache == undefined){
				dataset.useCache = !(dataset.isRealtime ? cockpitModule_templateServices.isDatasetUsedByAssociations(dataset.label) : dataset.isNearRealtimeSupported);
			}
			if(dataset.frequency == undefined){
				dataset.frequency = 0;
			}
		}
	}
	this.initSpatialAttributesFlag=function(datasets){
		for(var i in datasets){
			var dataset = datasets[i];
			var hasSpatialAttributes = false;
			if(dataset.metadata && dataset.metadata.fieldsMeta){
				for(var j in dataset.metadata.fieldsMeta){
					if(dataset.metadata.fieldsMeta[j].fieldType == "SPATIAL_ATTRIBUTE"){
						hasSpatialAttributes = true;
						break;
					}
				}
			}
			dataset.hasSpatialAttributes = hasSpatialAttributes;
		}
	}


	this.forceNearRealTimeValues=function(datasets, associations){
		if(datasets == undefined){
			datasets = ds.datasetList;
		}
		for(var i=0; i < datasets.length; i++){
			var dataset = datasets[i];
			if(dataset.isRealtime){
				dataset.useCache = !cockpitModule_templateServices.isDatasetUsedByAssociations(dataset.label, associations);
			}
		}
	}

	this.getDatasetLabelById=function(dsId){
		if(ds.datasetMapById[dsId]){
			return ds.datasetMapById[dsId].label;
		}else{
			return null;
		}
	}

	this.getDatasetLabelsByIds=function(dsIds){
		dsLabels = [];
		for(var i in dsIds){
			dsLabels.push(ds.getDatasetLabelById(dsIds[i]));
		}
		return dsLabels;
	}

	this.checkForDSChange=function(){
		var changed=[];
		var removedDatasetParams=[];

		angular.forEach(cockpitModule_template.configuration.datasets,function(item){
			var actualDs=ds.getDatasetById(item.dsId);
			if(actualDs==undefined){
				this.push(sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.unabletoloaddataset").replace("{0}", "<b>" + item.dsLabel + "</b>"));
			}else{
				var addedParams=[];
				var removedParams=[];

				//check if label changed
				if(!angular.equals(actualDs.label,item.dsLabel)){
					var oldlab=angular.copy(item.dsLabel);
					//update the label of dataset
					this.push(sbiModule_translate.load("sbi.generic.label")+": "+item.dsLabel+" -> "+actualDs.label)
					item.dsLabel=actualDs.label;

					//update the dataset label in the associations
					for(var i=0;i<cockpitModule_template.configuration.associations.length;i++){
						var ass=cockpitModule_template.configuration.associations[i];
						if(ass.description.search("="+oldlab+"\.")!=-1){
							ass.description=ass.description.replace("="+oldlab+"\.", "="+item.dsLabel+".");
							for(var f=0;f<ass.fields.length;f++){
								if(angular.equals(ass.fields[f].store,oldlab)){
									ass.fields[f].store=item.dsLabel;
									break;
								}
							}
						}
					}

					//update the dataset in the filters
					if(cockpitModule_template.configuration.filters.hasOwnProperty(oldlab)){
						cockpitModule_template.configuration.filters[item.dsLabel]=cockpitModule_template.configuration.filters[oldlab];
						delete cockpitModule_template.configuration.filters[oldlab];
					}

					//update the dataset label in the aggregations
					for(var i=0;i<cockpitModule_template.configuration.aggregations.length;i++){
						var aggr=cockpitModule_template.configuration.aggregations[i];
						//check if this aggregations have this ds
						var ind=aggr.datasets.indexOf(oldlab);
						if(ind!=-1){
							//alter the label in dstasets variable
							aggr.datasets[ind]=item.dsLabel;
							//alter the label in selections
							var alteration={add:{},remove:[]}
							angular.forEach(aggr.selection,function(selVal,selInd){
								if(selInd.startsWith(oldlab)){
									this.add[item.dsLabel+"."+selInd.split(".")[1]]=selVal;
									this.remove.push(selInd);
								}
							},alteration)

							angular.forEach(alteration.add,function(val,ind){
								this[ind]=val;
							},aggr.selection)

							angular.forEach(alteration.remove,function(val){
								delete this[val];
							},aggr.selection)
						}
					}
				}

				//check if name changed
				if(!angular.equals(actualDs.name,item.name)){
					//update the name of dataset
					this.push(sbiModule_translate.load("sbi.generic.name")+": "+item.name+" -> "+actualDs.name);
					item.name=actualDs.name;
				}

				//check if parameters changed
				removedDatasetParams[item.dsLabel] = [];
				if(actualDs.parameters!=undefined && item.parameters!=undefined){

					//check added params
					for(var i=0; i<actualDs.parameters.length; i++){
						var paramName = actualDs.parameters[i].name;
						if(!item.parameters.hasOwnProperty(paramName)){
							addedParams.push(paramName);
							this.push(sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.addedParameter")
									.replace("{0}", "<b>" + item.dsLabel + ".$P{" + paramName + "}</b>"));
						}
					}

					//check removed params
					for (var paramName in item.parameters) {
						if (item.parameters.hasOwnProperty(paramName)) {
							var removed = true;
							for(var i=0; i<actualDs.parameters.length; i++){
								if(actualDs.parameters[i].name == paramName){
									removed = false;
									break;
								}
							}
							if(removed){
								removedParams.push(paramName);
								removedDatasetParams[item.dsLabel].push(paramName);
								this.push(sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.removedParameter")
										.replace("{0}", "<b>" + item.dsLabel + ".$P{" + paramName + "}</b>"));
							}
						}
					}
				}

				//fix template parameters
				for(var i=0; i<addedParams.length; i++){
					var addedParam = addedParams[i];
					item.parameters[addedParam] = null;
				}
				for(var i=0; i<removedParams.length; i++){
					var removedParam = removedParams[i];
					delete item.parameters[removedParam];
				}
			}
		},changed);

		var modifiedAssociations = 0;
		angular.forEach(cockpitModule_template.configuration.associations,function(item){
			//fix fields & description
			var modifiedAssociation = 0;

			for(var i=item.fields.length-1; i>=0; i--){
				var field = item.fields[i];
				var paramName = (field.column.startsWith("$P{") && field.column.endsWith("}")) ? field.column.substring(3, field.column.length - 1) : field.column;
				if(field.type == "dataset" && removedDatasetParams[field.store] && removedDatasetParams[field.store].indexOf(paramName) > -1){
					item.description = item.description.replace(field.store + "." + field.column, "");
					if(item.description.startsWith("=")){
						item.description = item.description.substring(1);
					}else if(item.description.endsWith("=")){
						item.description = item.description.substring(0, item.description.length - 1);
					}else{
						item.description = item.description.replace("==", "=");
					}

					item.fields.splice(i, 1);

					modifiedAssociation = 1;
				}
			}

			modifiedAssociations += modifiedAssociation;
		},changed)

		//remove degenerated associations
		var removedAssociations = 0;
		for(var i=cockpitModule_template.configuration.associations.length-1; i>=0; i--){
			var association=cockpitModule_template.configuration.associations[i];
			if(association.fields.length < 2){
				cockpitModule_template.configuration.associations.splice(i, 1);
				removedAssociations++;
				modifiedAssociations--;
			}
		}

		if(modifiedAssociations > 0){
			changed.push(sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.modifiedAssociations")
					.replace("{0}", "" + modifiedAssociations));
		}

		if(removedAssociations > 0){
			changed.push(sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.removedAssociations")
					.replace("{0}", "" + removedAssociations));
		}

		angular.forEach(cockpitModule_template.sheets,function(sheet){
			angular.forEach(sheet.widgets,function(widget){
				if(widget.dataset && widget.dataset.dsId){
					var actualDs=ds.getDatasetById(widget.dataset.dsId);
					if(actualDs!=undefined){
						if(Array.isArray(actualDs)) actualDs = actualDs[0];
						var selectedColumnsDs = widget.content.columnSelectedOfDataset;
						if(selectedColumnsDs !== undefined){
							selectedColumnsDs = (selectedColumnsDs instanceof Array) ? widget.content.columnSelectedOfDataset : widget.content.columnSelectedOfDataset[widget.dataset.dsId];
						}
						angular.forEach(selectedColumnsDs,function(widgetColumn){
							var isWidgetColumnMatching = false;
							for(var i = 0; i < actualDs.metadata.fieldsMeta.length; i++){
								if(widget.type == 'text' || actualDs.metadata.fieldsMeta[i].name == widgetColumn.name || actualDs.metadata.fieldsMeta[i].alias == widgetColumn.name || widgetColumn.formula || widgetColumn.boundFunction){
									isWidgetColumnMatching = true;
									break;
								}
							}
							if(!isWidgetColumnMatching){
								this.push(sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.unabletoloadcolumnforwidget")
										.replace("{0}", "<b>" + actualDs.name + "." + widgetColumn.alias + "</b>")
										.replace("{1}", "<b>" + widget.content.name + "</b>"));
							}
						}, changed);
					}
				}
			});
		});

		if(changed.length>0){
			changed.push(sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.checkconfigandsave"));
			sbiModule_messaging.showErrorMessage(changed.join("<br>"), sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.title"));
		}
	}

	this.getDatasetList=function(){
		return angular.copy(ds.datasetList);
	}

	this.setDatasetList=function(dsList){
		return ds.datasetList = dsList;
	}

	//return a COPY of dataset with specific id or null
	this.getDatasetById=function(dsId){
		if(Array.isArray(dsId)){
			var tempDatasets = [];
			for(var k in dsId){
				tempDatasets.push(ds.datasetMapById[dsId[k]]);
			}
			return tempDatasets;
		}else return angular.copy(ds.datasetMapById[dsId]);
	}

	this.setDatasetById=function(dsIds){
		ds.datasetMapById = dsIds;
	}

	//return a COPY of dataset with specific label or null
	this.getDatasetByLabel=function(dsLabel){
		return angular.copy(ds.datasetMapByLabel[dsLabel]);
	}

	this.setDatasetByLabel=function(dsLabels){
		ds.datasetMapByLabel = dsLabels;
	}

	//return a COPY of avaiable dataset with specific id or null
	this.getAvaiableDatasetById=function(dsId){
		var dsAvList=ds.getAvaiableDatasets();
		for(var i=0;i<dsAvList.length;i++){
			if(angular.equals(dsAvList[i].id.dsId,dsId)){
				var tmpDS={};
				angular.copy(dsAvList[i],tmpDS);
				return tmpDS;
			}
		}
	}

	//return a COPY of avaiable dataset with specific label or null
	this.getAvaiableDatasetByLabel=function(dsLabel){
		var dsAvList=ds.getAvaiableDatasets();
		for(var i=0;i<dsAvList.length;i++){
			if(angular.equals(dsAvList[i].label,dsLabel)){
				var tmpDS={};
				angular.copy(dsAvList[i],tmpDS);
				return tmpDS;
			}
		}
	}

	this.getLabelDatasetsUsed = function(){
		var string = "";
		for(var i=0;i<cockpitModule_template.sheets.length;i++){
			var sheet = cockpitModule_template.sheets[i];
			for(var j=0;j<sheet.widgets.length;j++){
				var widget = sheet.widgets[j];
				if(widget.dataset !=undefined){
					var ds = this.getDatasetById(widget.dataset.dsId)
					//array.push(ds.label);
					string = string + ds.label;
					if(j<sheet.widgets.length-1){
						string = string + ","
					}
				}
			}
		}

		return string;
	}

	this.getDatasetsUsed = function(){
		var array = [];
		var result = {};
		for(var i=0;i<cockpitModule_template.sheets.length;i++){
			var sheet = cockpitModule_template.sheets[i];
			for(var j=0;j<sheet.widgets.length;j++){
				var widget = sheet.widgets[j];
				if(widget.dataset !=undefined){
					array.push(widget.dataset.dsId);
				}
			}
		}

		return array;

	}



	//return a list of avaiable dataset with all parameters
	this.getAvaiableDatasets=function(){
		var fad=[];
		for(var i=0;i<cockpitModule_template.configuration.datasets.length;i++){
			var dataset = cockpitModule_template.configuration.datasets[i];
			var dsIl=ds.getDatasetById(dataset.dsId);
			if(dsIl!=undefined){
				if(!dsIl.isRealtime){ // load useCache from template only if dataset is not realtime
					dsIl.useCache = (dataset.useCache == undefined) ? !dsIl.isNearRealtimeSupported : dataset.useCache;
				}
				dsIl.frequency = (dataset.frequency == undefined) ? 0 : dataset.frequency;
				dsIl.expanded = true;
				if(dataset.parameters!=undefined){
					angular.forEach(dsIl.parameters,function(item){
						item.value=dataset.parameters[item.name];
					})
				}
				if(dataset.drivers) {
					for(var k = 0; k < dataset.drivers.length; k++) {
						for(var j = 0; j < dsIl.drivers.length; j++) {
							if(dataset.drivers[k].id == dsIl.drivers[j].id) {
								dsIl.drivers[j].parameterValue = dataset.drivers[k].parameterValue;
								dsIl.drivers[j].parameterDescription = dataset.drivers[k].parameterDescription;
							}
						}
					}
				}
				fad.push(dsIl);
			}else{
				console.error("ds with id "+dataset.dsId +" not found;")
			}
		}
		
		return fad;
	}

	this.getAvailableDatasetsInCache=function(){
		var fad=[];
		var tmpAvailableDatasets=this.getAvaiableDatasets();
		for(var i=0;i<tmpAvailableDatasets.length;i++){
			var dataset = cockpitModule_template.configuration.datasets[i];
			if (dataset.useCache) {
				fad.push(ds.getDatasetById(dataset.dsId));
			}
		}

		return fad;
	}

	//get in input a full list of avaiable dataset and save only the attributes needed in template
	this.setAvaiableDataset=function(adl){
		angular.copy([],cockpitModule_template.configuration.datasets);
		var tmpList=[];
		for(var i=0;i<adl.length;i++){
			ds.addAvaiableDataset(adl[i])
		}
	}

	this.addAvaiableDataset=function(avDataset){
		var tmpDS={};
		tmpDS.dsId=avDataset.id.dsId;
		tmpDS.name=avDataset.name;
		tmpDS.dsLabel=avDataset.label;
		tmpDS.useCache = (avDataset.useCache == undefined) ? true : avDataset.useCache;
		tmpDS.frequency = (avDataset.frequency == undefined) ? 0 : avDataset.frequency;
		tmpDS.parameters={};
		tmpDS.drivers=avDataset.drivers;
		if(avDataset.parameters!=undefined){
			for(var p=0;p<avDataset.parameters.length;p++){
				tmpDS.parameters[avDataset.parameters[p].name]=avDataset.parameters[p].value;
			}
		}

		cockpitModule_template.configuration.datasets.push(tmpDS);
	}

	this.getDatasetParameters=function(dsId){
		var params={};
		if(dsId){
			for(var i=0;i<cockpitModule_template.configuration.datasets.length;i++){
				if(angular.equals(cockpitModule_template.configuration.datasets[i].dsId,dsId)){
					angular.forEach(cockpitModule_template.configuration.datasets[i].parameters,function(item,key){
						this[key]=[cockpitModule_utilstServices.getParameterValue(item)];
						if (item == undefined) {

							var datasetFound=ds.getDatasetById(dsId);
							var paramsFound = datasetFound.parameters;
							for (var j = 0; j < paramsFound.length; j++) {

								if((paramsFound[j].name == key) && paramsFound[j].defaultValue != undefined) {
									this[key] = paramsFound[j].defaultValue;
									cockpitModule_template.configuration.datasets[i].parameters[key] = this[key] ;
								}

							}

						}
					},params);


				}
			}


			var datasetLabel=ds.getDatasetById(dsId).label;
			var selections=cockpitModule_widgetSelection.getCurrentSelections(datasetLabel);
			if(selections!=undefined && selections.hasOwnProperty(datasetLabel)){
				for(var parName in selections[datasetLabel]){
					if(parName.startsWith("$P{") && parName.endsWith("}")){
						var parValue=selections[datasetLabel][parName];
						if(parValue!=undefined){

							var finalParams = []; // params to be overriden
							angular.forEach(parName.match(new RegExp('\\$P\\{(.*?)\\}','g')),function(item){
								this.push(item.substring(3, item.length - 1));
							}, finalParams);

							var finalValues = []; // all values to be replaced (flattened tuples)
							angular.forEach(parValue,function(item){
								var nowrapping = item.substring(2,item.length - 2);
								this.push(nowrapping.split("','"));
//								angular.forEach(items,function(value){
//								this.push(value.substring(1, value.length - 1));
//								},finalValues);
							}, finalValues);

							for(var i=0; i<finalParams.length; i++){
								var key = finalParams[i];
								var values = [];
								for(var j=i; j<finalValues.length; j += finalParams.length){
									values.push(finalValues[j]);
								}
								params[key] = values;
							}
						}
					}
				}
			}
		}
		return params;
	}
	var savedFilters = null;
	this.getFiltersWithoutParams=function(){
		return savedFilters;
	}

	this.formatDriverValueForExecution = function(driverValue) {
		 var a = driverValue.lastIndexOf("{");
		 var b = driverValue.indexOf("}");
		 var newDriverValue = driverValue.slice(a+1, b);
		 if(newDriverValue.includes(";")) {
			 newDriverValue = newDriverValue.split(";");
		 }
		 return newDriverValue;
	}

	//TODO missing maxRows
	this.loadDatasetRecordsById = function(dsId, page, itemPerPage,columnOrdering, reverseOrdering, ngModel, loadDomainValues, nature){

		if(loadDomainValues == undefined){
			loadDomainValues = false;
		}

		//after retry LabelDataset by Id call service for data
		var dataset = this.getAvaiableDatasetById(dsId);
		var deferred = $q.defer();

		var params="?";
		var bodyString = "{";

		var bodyJSON = {};


		var newModel = angular.copy(ngModel);
		if (Array.isArray(ngModel.content.columnSelectedOfDataset)){
			//converts the columns array in a jsonObject of arrays
			var newcolumnSelectedOfDataset = {};
			newcolumnSelectedOfDataset[dsId] = newModel.content.columnSelectedOfDataset;
			delete newModel.content.columnSelectedOfDataset;
			newModel.content.columnSelectedOfDataset = newcolumnSelectedOfDataset;
		}

		var aggregations = cockpitModule_widgetSelection.getAggregation(ngModel,dataset,columnOrdering, reverseOrdering);
        
		if(ngModel.type=='chart'){
			var template = this.getI18NTemplate(ngModel.content.chartTemplate);
			// replacing variables occurences in template and aggregations, to avoid disalignaments
			template = this.replaceStringVariables(template);
			aggregations = this.replaceStringVariables(aggregations);	 
			this.addNewColumnToAggregations(template.CHART, aggregations, ngModel.content.columnSelectedOfDatasetAggregations)

		}
		// apply sorting column & order
		if(ngModel.settings && ngModel.settings.sortingColumn && ngModel.settings.sortingColumn!=""){
			var isSortingAlreadyDefined = false;

			// check if a sorting order is alredy defined on categories
			for(var i=0; i<aggregations.categories.length; i++){
				var category = aggregations.categories[i];
				if(category.orderType && category.orderType.trim() != ""){
					isSortingAlreadyDefined = true;
					break;
				}
			}

			// check if a sorting order is alredy defined on measures
			if(!isSortingAlreadyDefined){
				for(var i=0; i<aggregations.measures.length; i++){
					var measure = aggregations.measures[i];
					if(measure.orderType && measure.orderType.trim() != ""){
						isSortingAlreadyDefined = true;
						break;
					}
				}
			}

			if(!isSortingAlreadyDefined){
				var isSortingApplied = false;

				// apply sorting order on categories
				for(var i=0; i<aggregations.categories.length; i++){
					var category = aggregations.categories[i];
					if(category.columnName == ngModel.settings.sortingColumn && !category.orderType){
						category.orderType = ngModel.settings.sortingOrder;
						isSortingApplied = true;
						break;
					}
				}

				// apply sorting order on measures
				if(!isSortingApplied){
					for(var i=0; i<aggregations.measures.length; i++){
						var measure = aggregations.measures[i];
						if(measure.columnName == ngModel.settings.sortingColumn && !measure.orderType){
							measure.orderType = ngModel.settings.sortingOrder;
							isSortingApplied = true;
							break;
						}
					}
				}

				// add a new category if necessary (workaround for Oracle queries)
				if(!isSortingApplied){

					var isSortingAppliedCategory = false;
					var isSortingAppliedMeasures = false;

					for(var i=0; i<aggregations.categories.length; i++){
						var category = aggregations.categories[i];
						if(category.columnName == ngModel.settings.sortingColumn){
							category.orderType = ngModel.settings.sortingOrder;
							isSortingAppliedCategory = true;
							break;
						}
					}

					if(isSortingAppliedCategory) {
						var newCategory = {
								alias : ngModel.settings.sortingColumn,
								columnName : ngModel.settings.sortingColumn,
								id : ngModel.settings.sortingColumn,
								orderType : ngModel.settings.sortingOrder
						}
						aggregations.categories.push(newCategory);
					}

					for(var i=0; i<aggregations.measures.length; i++){
						var measure = aggregations.measures[i];
						if(measure.columnName == ngModel.settings.sortingColumn){
							measure.orderType = ngModel.settings.sortingOrder;
							isSortingAppliedMeasures = true;
							break;
						}
					}
					if(isSortingAppliedMeasures) {
						var newMeas = {
								alias : ngModel.settings.sortingColumn,
								columnName : ngModel.settings.sortingColumn,
								id : ngModel.settings.sortingColumn,
								orderType : ngModel.settings.sortingOrder
						}
						aggregations.measures.push(newMeas);
					}


				}
			}
		}

		var parameters = ds.getDatasetParameters(dsId);
		var parameterErrors = [];
		for (var parameter in parameters) {
			if (parameters.hasOwnProperty(parameter)){
				for(var i in dataset.parameters){
					if(dataset.parameters[i].name==parameter){
						var valueCount = parameters[parameter].length;
						if(!dataset.parameters[i].multiValue && valueCount > 1){
							var parameterError = sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.unabletoapplyvaluestosinglevalueparameter")
							.replace("{0}", "<b>" + valueCount + "</b>")
							.replace("{1}", "<b>" + dataset.name + ".$P{" + parameter + "}</b>")
							parameterErrors.push(parameterError);
						}
						break;
					}
				}
			}
		}
		if(parameterErrors.length > 0){
			var title = sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.widget")
			.replace("{0}", "<b>" + ngModel.content.name + "</b>");
			sbiModule_restServices.errorHandler(parameterErrors.join("<br>"), title);
			deferred.reject('Error');
		}

		var parametersString = ds.getParametersAsString(parameters);

		// if cross navigation referes to a non present column (and widget is a table ) avoid sending aggregation because all columns are needed
		if(ngModel.type === 'table'){
			// if cross navigation is defined
			if (ngModel.cross != null && ngModel.cross.cross != null && ngModel.cross.cross.column){
				var crossCol = ngModel.cross.cross.column;
				var found = false;
				// if column passed is not among visible ones
				if(aggregations.categories != null){
					for(var i=0;i<aggregations.categories.length && !found;i++){
						var id = aggregations.categories[i].id;
						if(id === crossCol){
							found = true;
						}
					}
					// get all data
					if(found==false){
						aggr = '';
					}
				}
			}
		}

		bodyString = bodyString + "aggregations:" + JSON.stringify(aggregations) + ",parameters:" + parametersString;

		if(this.newDataSet && this.newDataSet.drivers) {
			bodyJSON.drivers =  driversExecutionService.prepareDriversForSending(this.newDataSet.drivers);
			this.driversAreSet(this.newDataSet);
		} else if(dataset && dataset.drivers && dataset.drivers.length > 0 && cockpitModule_analyticalDrivers) {
			for(var i = 0; i < dataset.drivers.length; i++) {
				var urlName = dataset.drivers[i].urlName;
				var driverValue = null;
				if(cockpitModule_analyticalDrivers[urlName]) {
					if(cockpitModule_analyticalDrivers[urlName].includes("{")) {
						driverValue = this.formatDriverValueForExecution(cockpitModule_analyticalDrivers[urlName]);
					} else {
						driverValue = cockpitModule_analyticalDrivers[urlName];
					}
					var driverDescription = cockpitModule_analyticalDrivers[urlName+"_description"];
					dataset.drivers[i].parameterValue = driverValue;
					dataset.drivers[i].parameterDescription = driverDescription;
				}
			}
			bodyJSON.drivers = driversExecutionService.prepareDriversForSending(dataset.drivers);
			this.driversAreSet(dataset);
		}

		if(bodyJSON.drivers && this.driversAreSet(dataset)) {
			bodyString = bodyString + ",drivers:" + JSON.stringify(bodyJSON.drivers);
		}

		bodyJSON.aggregations = aggregations;
		bodyJSON.parameters = JSON.parse(parametersString);

		if(page!=undefined && page>-1 && itemPerPage!=undefined && itemPerPage>-1){
			params = params + "offset=" + (page * itemPerPage) + "&size=" + itemPerPage;
		}else{
			params = params + "offset=-1&size=-1";
		}

		if(ngModel.settings && ngModel.settings.summary && ngModel.settings.summary.enabled){
			var summaryRow = ds.getSummaryRow(ngModel);
			bodyString = bodyString + ",summaryRow:" + JSON.stringify(summaryRow);
			bodyJSON.summaryRow = summaryRow;
		}

		if(!dataset || (dataset && dataset.useCache==false)){
			params+="&nearRealtime=true";
		}

		var limitRows;
		if(ngModel.limitRows){
			limitRows = ngModel.limitRows;
		}else if(ngModel.content && ngModel.content.limitRows){
			limitRows = ngModel.content.limitRows;
		}
		if(limitRows != undefined && limitRows.enable && limitRows.rows > 0){
			params += "&limit=" + limitRows.rows;
		}

		var filtersToSendWithoutParams = ds.getWidgetSelectionsAndFilters(ngModel, dataset, loadDomainValues);

		if(ngModel.search){
			var searchData = {};
			var likeSelections = {};
			if(ngModel.search.text && ngModel.search.text!="" && ngModel.search.columns && ngModel.search.columns.length>0){
				var columns = ngModel.search.columns.join(",");
				searchData[columns] = ngModel.search.text;
			}
			if(ngModel.search.facets && Object.keys(ngModel.search.facets).length > 0){
				for(var f in ngModel.search.facets){
					searchData[f] = ngModel.search.facets[f].filterVals[0];
				}
			}
			if(Object.keys(searchData).length > 0){
				likeSelections[dataset.label] = searchData;
				bodyString = bodyString + ",likeSelections:" + JSON.stringify(likeSelections);
				bodyJSON.likeSelections = likeSelections;
			}
		}

		savedFilters = filtersToSendWithoutParams;

		if(dataset.type == "SbiSolrDataSet" && ngModel.type != "discovery"){
			bodyString = bodyString + ",options:{solrFacetPivot:true}";
			bodyJSON.options = {solrFacetPivot:true};
		}
		if(dataset.type == "SbiSolrDataSet" && ngModel.type == "discovery"){
			if(ngModel.settings.facets.limit) {
				bodyString += ",options:{'facetsLimit':"+ngModel.settings.facets.limit+"}";
				bodyJSON.options = {'facetsLimit':ngModel.settings.facets.limit};
			}
		}

		bodyString = bodyString + ",selections:" + JSON.stringify(filtersToSendWithoutParams);
		bodyJSON.selections = filtersToSendWithoutParams;

		var indexes = cockpitModule_template.configuration.indexes == undefined ? [] : cockpitModule_template.configuration.indexes;
		bodyString = bodyString + ",indexes:" + JSON.stringify(indexes)  + "}";
		bodyJSON.indexes = indexes;

		this.parametersAreSet(dataset);

		params += "&widgetName=" + encodeURIComponent(ngModel.content.name);
		if(ngModel.content.wtype=="chart"){
			var chartTemplate = this.getI18NTemplate(ngModel.content.chartTemplate);
			chartTemplate.CHART.outcomingEventsEnabled = true;
			chartTemplate.CHART.cliccable = ngModel.cliccable;
			chartTemplate.CHART.drillable = ngModel.drillable;
			this.replaceVariables(chartTemplate.CHART);
			var body = {"aggregations":bodyJSON, "chartTemp":chartTemplate, "exportWebData":false}
			sbiModule_restServices.promisePost("1.0/chart/jsonChartTemplate", encodeURIComponent(dataset.label) + "/getDataAndConf" + params, body)
			.then(function(response){
				if(cockpitModule_properties.DS_IN_CACHE.indexOf(dataset.label)==-1){
					cockpitModule_properties.DS_IN_CACHE.push(dataset.label);
				}
				deferred.resolve(response.data);
			},function(response){
				var regex = /(.*)1.0\/chart\/jsonChartTemplate\/(.*)\/getDataAndConf(.*)widgetName=(.*)/g;
				var array = regex.exec(decodeURIComponent(response.config.url));
				var datasetLabel = array[2];
				var widgetName = array[4];
				var title = sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.widget")
				.replace("{0}", "<b>" + widgetName + "</b>");
				var text = sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.unabletoloaddatafromdataset")
				.replace("{0}", "<b>" + datasetLabel + "</b>")
				text += "<br>";
				text += sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.checkdatasetandwidgetconfig");
				sbiModule_restServices.errorHandler(text, title);
				deferred.reject('Error');
			})

			return deferred.promise;
		} else {
			sbiModule_restServices.restToRootProject();
			sbiModule_restServices.promisePost("2.0/datasets", encodeURIComponent(dataset.label) + "/data" + params, bodyString)
			.then(function(response){
				if(cockpitModule_properties.DS_IN_CACHE.indexOf(dataset.label)==-1){
					cockpitModule_properties.DS_IN_CACHE.push(dataset.label);
				}
				deferred.resolve(response.data);
			},function(response){
				var regex = /(.*)2.0\/datasets\/(.*)\/data(.*)widgetName=(.*)/g;
				var array = regex.exec(decodeURIComponent(response.config.url));
				var datasetLabel = array[2];
				var widgetName = array[4];
				var title = sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.widget")
				.replace("{0}", "<b>" + widgetName + "</b>");
				var text = sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.unabletoloaddatafromdataset")
				.replace("{0}", "<b>" + datasetLabel + "</b>")
				text += "<br>";
				text += sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.checkdatasetandwidgetconfig");
				sbiModule_restServices.errorHandler(text, title);
				deferred.reject('Error');
			})

			return deferred.promise;
		}
	}

	this.areParametersSet = function(dsId) {
		return this.areParametersSetMap[dsId] || true;
	}
	
	this.areDriversSet = function(dsId) {
		return this.areDriversSetMap[dsId] || true;
	}

	this.areParametersSetMap = {};
	this.areDriversSetMap = {};

	this.parametersAreSet = function(dataset) {

		var dsId = dataset.id.dsId;
		
		if (dataset.type == "SbiQbeDataSet") {
			var parameters = dataset.parameters;
			for(var i = 0; i < parameters.length; i++) {
				if(parameters[i].value) {
					this.areParametersSetMap[dsId] = true;
				} else {
					this.areParametersSetMap[dsId] = false;
				}
			}
		} else {
			this.areParametersSetMap[dsId] = true;
		}

		return this.areParametersSetMap[dsId];
	}
	this.driversAreSet = function(dataset) {

		var dsId = dataset.id.dsId;
		
		var drivers = dataset.drivers;
		if (dataset.type == "SbiQbeDataSet" && drivers) {
			for(var i = 0; i < drivers.length; i++) {
				if(drivers[i].parameterValue) {
					this.areDriversSetMap[dsId] = true;
				} else {
					this.areDriversSetMap[dsId] = false;
				}
			}
		} else {
			this.areDriversSetMap[dsId] = true;
		}

		return this.areDriversSetMap[dsId];
	}

	this.replaceVariables = function (obj){
		for (var attrname in obj) {
			if(!(typeof obj[attrname] == 'object')){
				if(typeof obj[attrname] =='string')
					obj[attrname]  =  obj[attrname].replace(/(\$V\{)([a-zA-Z0-9\-\_\s]*)(\})/g,function(match,p1,p2){
						return cockpitModule_properties.VARIABLES[p2];
					})
			} else {
				this.replaceVariables(obj[attrname]);
			}
		}
	}
	
	this.replaceStringVariables = function (obj){
	var objString = angular.copy(obj);
	objString = JSON.stringify(objString);
    objString = objString.replace(/\$V\{([a-zA-Z0-9\-\_]*){1}(?:.([a-zA-Z0-9\-\_]*){1})?\}/g,function(match,p1,p2){
					return p2 ? cockpitModule_properties.VARIABLES[p1][p2] : cockpitModule_properties.VARIABLES[p1];
				})
				
	return JSON.parse(objString);
	}

	this.addNewColumnToAggregations = function (obj, aggregations, columns){
		for (var attrname in obj) {
			if(!(typeof obj[attrname] == 'object')){
				if(typeof obj[attrname] =='string')
					obj[attrname]  =  obj[attrname].replace(/(\$F\{)([a-zA-Z0-9\-\_\s]*)(\})/g,function(match,p1,p2){
						var columnIndex = sbiModule_util.findInArray(columns, 'alias', p2);
						if(columnIndex>-1 && columns[columnIndex].fieldType.toUpperCase()=="MEASURE"){
							var aggregationIndex = sbiModule_util.findInArray(aggregations.measures, 'columnName', p2);
							if(aggregationIndex==-1){
								aggregations.measures.push({
									alias:columns[columnIndex].alias,
									columnName:columns[columnIndex].name,
									funct:columns[columnIndex].funcSummary,
									id:columns[columnIndex].alias,
									orderColumn:"",
									orderType:""
								})
							}
						}
						//missing part for attributes
					})
			} else {
				this.addNewColumnToAggregations(obj[attrname], aggregations, columns);
			}
		}
	}

	// Returns Selections with Filters for Single Widget
	this.getWidgetSelectionsAndFilters = function(widgetObject, dataset, loadDomainValues) {
		var filtersToSend = {};
		var datasetLabel = dataset.label;
		if(loadDomainValues == undefined){
			loadDomainValues = false;
		}

		if(!loadDomainValues && widgetObject.updateble){
			filtersToSend = angular.copy(cockpitModule_widgetSelection.getCurrentSelections(datasetLabel));
			var filters = angular.copy(cockpitModule_widgetSelection.getCurrentFilters(datasetLabel));
			angular.merge(filtersToSend, filters);

			if(widgetObject.type=="selector"){
				var isColumnInAssociation = false;
				for(var i=0; !isColumnInAssociation && i<cockpitModule_template.configuration.aggregations.length; i++){
					var aggregation = cockpitModule_template.configuration.aggregations[i];
					for(var j=0; !isColumnInAssociation && j<aggregation.associations.length; j++){
						var association = aggregation.associations[j];
						for(var k=0; !isColumnInAssociation && k<association.fields.length; k++){
							var field = association.fields[k];
							if(field.type=="dataset" && field.store==datasetLabel && field.column==widgetObject.content.selectedColumn.name){
								isColumnInAssociation = true;
								break;
							}
						}
					}
				}

				if(!isColumnInAssociation && !cockpitModule_widgetSelection.isLastTimestampedSelection(widgetObject.dataset.label,widgetObject.content.selectedColumn.name)){

					if(widgetObject.dataset && widgetObject.dataset.label && filtersToSend[widgetObject.dataset.label]
					&& widgetObject.content && widgetObject.content.selectedColumn && widgetObject.content.selectedColumn.name
					&& filtersToSend[widgetObject.dataset.label][widgetObject.content.selectedColumn.name]){
						delete filtersToSend[widgetObject.dataset.label][widgetObject.content.selectedColumn.name];
					}
				}
			}
		}

		var filters;
		if(widgetObject.filters){
			filters = widgetObject.filters;
		}else if(widgetObject.content && widgetObject.content.filters){
			filters = widgetObject.content.filters;
		}
		if(filters){
			for(var i=0;i<filters.length;i++){

				var filterElement=filters[i];

				// if filter.dataset is not defined means filter coming from old interface and then set it to current dataset
				if(!filterElement.dataset || !filterElement.dataset.dsId){
					filterElement.dataset = {"label":datasetLabel, "dsId":dataset.id.dsId};
				}

				var tempDatasetId = dataset.dsId || dataset.id.dsId;
				// if filter.dataset is defined check dataset is current one
				if(filterElement.dataset != undefined && filterElement.dataset.dsId === tempDatasetId){

					var colName=filterElement.colName;
					var type=filterElement.type;

					var filterOperator;
					if(filterElement.filterOperator != undefined){
						filterOperator=filterElement.filterOperator;
					}
					else {
						if(filterElement.filterVals && filterElement.filterVals.length>0){
							filterOperator = "=";
						}
						else{
							filterOperator = "";
						}

					}

					// if type is undefined get it from metadata
					if(type == undefined){
						var found = false;
						for(var j=0; j<dataset.metadata.fieldsMeta.length && !found; j++){
							var metaElement=dataset.metadata.fieldsMeta[j];
							if(metaElement.name == colName){
								filterElement.type = metaElement.type;
								found = true;
							}
						}
					}

					var filterVals=filterElement.filterVals;
					if(filterOperator != ""){
						var values=[];
						// if filterOperator is IN and filterVals has "," then filterVals must be splitted
						if((filterOperator == "IN" || filterOperator == "not IN") && filterVals[0] && filterVals[0].indexOf(",") != -1 ){
							filterVals = filterVals[0].split(",");
						}
						angular.forEach(filterVals, function(item){
							this.push("('" + item + "')");
						}, values);

						var filter = { filterOperator: filterOperator, filterVals: values};

						if(!filtersToSend[datasetLabel]){
							filtersToSend[datasetLabel] = {};
						}

						if(!filtersToSend[datasetLabel][colName]){
							filtersToSend[datasetLabel][colName] = filter;
						}else{
							filtersToSend[datasetLabel][colName].push(filter);
						}
					}
				}
			}
		}

		var filtersToSendWithoutParams = {};

		angular.copy(filtersToSend, filtersToSendWithoutParams);
		angular.forEach(filtersToSendWithoutParams, function(item){
			var paramsToDelete = [];
			for (var property in item) {
				if (item.hasOwnProperty(property) && property.startsWith("$P{") && property.endsWith("}")) {
					paramsToDelete.push(property);
				}
			}
			angular.forEach(paramsToDelete, function(prop){
				delete item[prop];
			});
		});

		return filtersToSendWithoutParams;
	}

  	this.returnParametersArray = function() {
	var arrayToReturn = {};
	for (var k in this.datasetList) {
	
		for (var j in this.datasetList[k].parameters) {
			
			arrayToReturn[this.datasetList[k].parameters[j].name]= this.datasetList[k].parameters[j];			
		}		
		
	}
	return arrayToReturn;
}

	this.getParametersAsString = function(parameters){		
		if(!cockpitModule_properties.PARAMETERS) cockpitModule_properties.PARAMETERS = this.returnParametersArray();
		var delim = "";
		var output = "{";
		for (var parameter in parameters) {
			if (parameters.hasOwnProperty(parameter)){
				if (parameters[parameter] == null || parameters[parameter] == undefined) {
					output += delim + "\"" + parameter + "\":null";
				}else{
//					var tempJSN = JSON.stringify(parameters[parameter]);
					var tempJSN = '"';
					if(Array.isArray(parameters[parameter])) {
						var splittedValues = parameters[parameter];
						if(Array.isArray(parameters[parameter][0])) {
							splittedValues = [];
							if (parameters[parameter][0].length > 1) {
								for (var i in parameters[parameter][0]) {
									splittedValues.push(parameters[parameter][0][i]);
								}
							}
							else {
								for (var y in parameters[parameter]) {
									splittedValues.push(parameters[parameter][y][0]);
								}
							}
						}
						if (typeof parameters[parameter][0] == "string" ) {
							splittedValues = parameters[parameter][0].split(',');
						}

						// CASE 1 [pippo , pluto]
						// CASE 2 [""]
						// CASE 3 ['pippo'],['pluto']
						// CASE 4 [[pippo],[pluto]]
						
						for (var j in splittedValues) {
							if (splittedValues[j] != "") {
								if (j!=0) {
									tempJSN  +=",";
								}
								if(cockpitModule_properties.PARAMETERS[parameter].type == "String"){
									tempJSN  +=  (splittedValues[j].charAt(0) == "'" ? "" : "'") + splittedValues[j] + (splittedValues[j].charAt(splittedValues[j].length - 1) == "'" ? "" : "'") ;
								}
								else {
									tempJSN  += splittedValues[j];
								}
							}
							else tempJSN += "";
						}

//						tempJSN = JSON.stringify(tempJSN);
//						tempJSN = tempJSN.substring(1,tempJSN.length-1);
					}else{
						if(parameters[parameter]!= "") tempJSN += parameters[parameter];
					}

//					tempJSN = tempJSN.replace(/[\[\]]/g,"");
					tempJSN += '"';

					output += delim + "\"" + parameter + "\":" + tempJSN ;
				}
			}
			delim = ",";
		}
		output += "}";

		return output;
	}

	// returns the internationalized template
	this.getI18NTemplate = function (chartTemplate) {
		var clone = angular.copy(chartTemplate);

		// looks for all "text" properties and apply I18N to them
		var func = function (key, object) {
			if (object.hasOwnProperty("text")) {
				object.text = sbiModule_i18n.getI18n(object.text);
			}
		}

		this.traverse(clone, func);
		if(clone.CHART.VALUES.SERIE) {
		for (i=0; i<clone.CHART.VALUES.SERIE.length;i++){
		var newSerie = clone.CHART.VALUES.SERIE[i];
		if (newSerie.hasOwnProperty("name")) {
				newSerie.name = sbiModule_i18n.getI18n(newSerie.name);
		}
		clone.CHART.VALUES.SERIE[i] = newSerie;
		}
		}
		return clone;

	}

	this.traverse = function(o, func) {
		for (var i in o) {
			if (o[i] !== null && typeof(o[i])=="object") {
				func.apply(this, [i, o[i]]);
				//going one step down in the object tree!!
				this.traverse(o[i], func);
			}
		}
	}

	this.getSummaryRow = function(ngModel){
		var summaryArray = [];
		var columns = ngModel.content.columnSelectedOfDataset;
		for(var k in ngModel.settings.summary.list){
			var measures = [];
			if(columns){
				//create aggregation
				for(var i=0;i<columns.length;i++){
					var col = columns[i];

					if(col.fieldType!="ATTRIBUTE"){
						var obj = {};
						obj["id"] = col.name || col.alias;
						obj["alias"] = col.aliasToShow || col.alias;
						//in case of non aggregated measures, default summary row aggregation is set to SUM
						if(k == 0){
							obj["funct"] = col.aggregationSelected == 'NONE' ? 'SUM' : col.aggregationSelected;
						}else{
							obj["funct"] = ngModel.settings.summary.list[k].aggregation || 'SUM';
						}


						if(ngModel.type == "table"){
							if(col.isCalculated) {
								obj["formula"] = col.formula;

							}else obj["columnName"] = col.name;
						}else obj["columnName"] = col.alias;

						measures.push(obj);
					}
				}
			}
			var result = {};
			result["measures"] = measures;
			result["dataset"] = ngModel.dataset.dsId;
			summaryArray.push(result);
		}

		return summaryArray;

	}

	this.addDataset=function(attachToElementWithId,container,multiple,autoAdd,typeAvailable,typeExclusion,skipParameters){
		var deferred = $q.defer();
		var eleToAtt=document.body;
		if(attachToElementWithId!=undefined){
			eleToAtt=angular.element(document.getElementById(attachToElementWithId))
		}

		var config = {
				attachTo: eleToAtt,
				locals :{currentAvaiableDataset:container,multiple:multiple,deferred:deferred},
				controller: function($scope,mdPanelRef,sbiModule_translate,cockpitModule_datasetServices,currentAvaiableDataset,multiple,deferred,$mdDialog){

					$scope.translate = sbiModule_translate;
					$scope.datasetSearchText = '';
					$scope.filterDataset = function(){
						var tempDatasetList = $filter('filter')($scope.datasetList,function(value,index,array) {
							var lowercaseDatasetSearchText = angular.lowercase($scope.datasetSearchText);
							return angular.lowercase(value["name"]).indexOf(lowercaseDatasetSearchText) != -1
								|| angular.lowercase(value["label"]).indexOf(lowercaseDatasetSearchText) != -1
								|| value["tags"].some(function(e) { return angular.lowercase(e.name).indexOf(lowercaseDatasetSearchText) != -1; })
								|| angular.lowercase(value["type"]).indexOf(lowercaseDatasetSearchText) != -1;
						});
						$scope.cockpitDatasetGrid.api.setRowData(tempDatasetList);
					}

					$scope.tmpCurrentAvaiableDataset;
					if(multiple){
						tmpCurrentAvaiableDataset=[];
					}else{
						tmpCurrentAvaiableDataset={};
					}

					$scope.multiple=multiple;

					$scope.cockpitDatasetColumn = [
						{"headerName": sbiModule_translate.load('kn.cockpit.dataset.label'),"field":"label",headerCheckboxSelection: multiple, checkboxSelection: multiple},
						{"headerName": sbiModule_translate.load('kn.cockpit.dataset.name'),"field":"name"},
						{"headerName": sbiModule_translate.load('kn.cockpit.dataset.type'),"field":"type",cellRenderer:typeRenderer,width: 150,suppressSizeToFit:true,suppressMovable:true},
						{"headerName": "Tags","field":"tags", cellRenderer:tagsRenderer},
						{"headerName": sbiModule_translate.load('kn.cockpit.dataset.hasDrivers'),"field":"drivers","cellStyle":
						{"display":"inline-flex","justify-content":"center", "align-items": "center"},cellRenderer:hasDriversRenderer,suppressSorting:true,suppressFilter:true,width: 150,suppressSizeToFit:true,suppressMovable:true},
						{"headerName": sbiModule_translate.load('kn.cockpit.dataset.hasParameters'),"field":"parameters","cellStyle":
						{"display":"inline-flex","justify-content":"center", "align-items": "center"},cellRenderer:hasParametersRenderer,suppressSorting:true,suppressFilter:true,width: 150,suppressSizeToFit:true,suppressMovable:true}];

					$scope.cockpitDatasetGrid = {
							enableColResize: false,
							enableFilter: true,
							enableSorting: true,
							pagination: true,
							paginationAutoPageSize: true,
							rowSelection: multiple ? 'multiple' : 'single',
									rowMultiSelectWithClick: multiple,
									onGridSizeChanged: resizeColumns,
									columnDefs : $scope.cockpitDatasetColumn
					};

					function resizeColumns(){
						$scope.cockpitDatasetGrid.api.sizeColumnsToFit();
					}
					function hasDriversRenderer(params){
						return (params.data.type=='SbiQbeDataSet' && params.data.drivers.length > 0) ? '<i class="fa fa-check"></i>' : '';
					}

					function hasParametersRenderer(params){
						return (params.value.length > 0) ? '<i class="fa fa-check"></i>' : '';
					}

					function typeRenderer(params){
						return cockpitModule_datasetServices.datasetTypes[params.value] || sbiModule_translate.load('kn.cockpit.dataset.type.generic');
					}

					function tagsRenderer(params){
						if(params.value && params.value.length > 0) {
							var cell = '';
							for(var i in params.value){
								cell += '<span class="miniChip">'+params.value[i].name+'</span>';
							}
							return cell;
						}
					}

					$scope.isDatasetListLoaded = false;

					cockpitModule_datasetServices.loadDatasetList().then(function(response){
						var datasetList = cockpitModule_datasetServices.getDatasetList();
						//remove available dataset
						for(var i=0;i<currentAvaiableDataset.length;i++){
							for(var j=0; j<datasetList.length; j++){
								if(angular.equals(currentAvaiableDataset[i].id.dsId, datasetList[j].id.dsId)){
									datasetList.splice(j,1);
									break;
								}
							}
						}
						if(typeAvailable){
							var specificDatasets = [];
							for(var y=0; y<datasetList.length; y++){
								for(var k in typeAvailable){
									if(datasetList[y].type == typeAvailable[k].type){
										if(typeAvailable[k].configuration){
											if(datasetList[y].configuration[typeAvailable[k].configuration.property] == typeAvailable[k].configuration.value) {
												specificDatasets.push(datasetList[y]);
												break;
											}
										}
										else {
											specificDatasets.push(datasetList[y]);
											break;
										}
									}
								}

							}
							datasetList = specificDatasets;
						}
						if(typeExclusion){
							for(var y=datasetList.length-1; y>0; y--){
								for(var k in typeExclusion){
									if(datasetList[y].type == typeExclusion[k].type){
										if(typeExclusion[k].configuration){
											if(datasetList[y].configuration[typeExclusion[k].configuration.property] == typeExclusion[k].configuration.value) {
												datasetList.splice(y,1);
												break;
											}
										}
										else {
											datasetList.splice(y,1);
											break;
										}
									}
								}

							}
						}


						$scope.datasetList = datasetList;
						$scope.isDatasetListLoaded = true;
						$scope.cockpitDatasetGrid.api.setRowData($scope.datasetList);
					},function(response){
						sbiModule_restServices.errorHandler(response.data,"");
						def.reject();
					});

					$scope.closeDialog=function(){
						mdPanelRef.close();
						$scope.$destroy();
						deferred.reject();
					}

					$scope.getDocDrivers = function() {
						if(cockpitModule_properties.DOCUMENT_ID) {
							$scope.docDrivers = [];
							var params = {
									label: cockpitModule_properties.DOCUMENT_LABEL,
									role: cockpitModule_properties.SELECTED_ROLE,
									parameters: {}
							};
							sbiModule_restServices.restToRootProject();
							sbiModule_restServices.promisePost("1.0/documentexecution", "filters", params)
							.then(function(response){
								angular.copy(response.data.filterStatus, $scope.docDrivers);
							},function(response){
								sbiModule_restServices.errorHandler(response.data,"error while attempt to load document drivers")
							})
						}
					}

					$scope.getDocDrivers();

					$scope.addDataset = function() {
						if(multiple) {
							for(var i=0;i<$scope.tmpCurrentAvaiableDataset.length;i++){
								$scope.tmpCurrentAvaiableDataset[i].expanded = true;
								if(autoAdd){
									ds.addAvaiableDataset($scope.tmpCurrentAvaiableDataset[i])
								}else{
									currentAvaiableDataset.push($scope.tmpCurrentAvaiableDataset[i]);									
								}
							}
							cockpitModule_properties.PARAMETERS = ds.returnParametersArray();
							deferred.resolve(angular.copy($scope.tmpCurrentAvaiableDataset));
							mdPanelRef.close();
							$scope.$destroy();
						} else {
							if(($scope.tmpCurrentAvaiableDataset.parameters && $scope.tmpCurrentAvaiableDataset.parameters.length>0 && !skipParameters) ||
									($scope.tmpCurrentAvaiableDataset.drivers && $scope.tmpCurrentAvaiableDataset.drivers.length>0)){
								//fill the parameter

								$mdDialog.show({
									controller: function($scope,sbiModule_translate,parameters,drivers){
										$scope.translate=sbiModule_translate;
										$scope.tmpParam=angular.copy(parameters);

										$scope.saveConfiguration=function(){
											$mdDialog.hide($scope.tmpParam);
										}
										$scope.cancelConfiguration=function(){
											$mdDialog.cancel();
										}
									},
									templateUrl: baseScriptPath+ '/directives/cockpit-data-configuration/templates/CockpitDataConfigurationDatasetParameterFill.html',
									clickOutsideToClose:false,
									parent: mdPanelRef._panelContainer[0].querySelector(".md-panel md-card"),
									hasBackdrop :true,
									preserveScope :true,
									locals:{
										parameters:$scope.tmpCurrentAvaiableDataset.parameters,
										drivers:$scope.tmpCurrentAvaiableDataset.drivers
										}
								})
								.then(function(data) {
									angular.copy(data,$scope.tmpCurrentAvaiableDataset.parameters);
									$scope.tmpCurrentAvaiableDataset.expanded = true;
									if(autoAdd){
										ds.addAvaiableDataset($scope.tmpCurrentAvaiableDataset)
									}else{
										angular.copy($scope.tmpCurrentAvaiableDataset,currentAvaiableDataset);
									}
									deferred.resolve(angular.copy($scope.tmpCurrentAvaiableDataset));
									mdPanelRef.close();
									$scope.$destroy();

								}, function() {
									$scope.status = 'You cancelled the dialog.';
								});


							}else{
								$scope.tmpCurrentAvaiableDataset.expanded = true;
								if(autoAdd){
									ds.addAvaiableDataset($scope.tmpCurrentAvaiableDataset)
								}else{
									angular.copy($scope.tmpCurrentAvaiableDataset,currentAvaiableDataset);
								}
								deferred.resolve(angular.copy($scope.tmpCurrentAvaiableDataset));
								mdPanelRef.close();
								$scope.$destroy();

							}
							cockpitModule_properties.PARAMETERS = this.returnParametersArray();
						}
					}

					$scope.saveDataset=function(){
						if(multiple){
							$scope.tmpCurrentAvaiableDataset = [];
							var tempMultiDataset = $scope.cockpitDatasetGrid.api.getSelectedRows();
							for(var i=0; i < tempMultiDataset.length; i++) {
								if(tempMultiDataset[i].type == "SbiQbeDataSet") {
									if((cockpitModule_datasetServices.selectedDSWithDrivers.length==1 || ($scope.docDrivers && $scope.docDrivers.length > 0)) && tempMultiDataset[i].drivers && tempMultiDataset[i].drivers.length > 0) {
										sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.cockpit.parameter.error.one.dataset"), 'Error');
									} else {
										$scope.tmpCurrentAvaiableDataset.push(tempMultiDataset[i]);
									}
								} else {
									$scope.tmpCurrentAvaiableDataset.push(tempMultiDataset[i]);
								}
							}
							$scope.addDataset();
						}else{
							$scope.tmpCurrentAvaiableDataset = $scope.cockpitDatasetGrid.api.getSelectedRows()[0];
							if((cockpitModule_datasetServices.selectedDSWithDrivers.length==1 || ($scope.docDrivers && $scope.docDrivers.length > 0)) && $scope.tmpCurrentAvaiableDataset.drivers && $scope.tmpCurrentAvaiableDataset.drivers.length > 0) {
								sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.cockpit.parameter.error.one.dataset"), 'Error');
							} else {
								$scope.addDataset();
							}
						}
					}
				},
				disableParentScroll: true,
				templateUrl: baseScriptPath+ '/directives/cockpit-data-configuration/templates/cockpitDataConfigurationDatasetChoice.html',
//				hasBackdrop: true,
				position: $mdPanel.newPanelPosition().absolute().center(),
				trapFocus: true,
				zIndex: 150,
				fullscreen :true,
				clickOutsideToClose: true,
				escapeToClose: false,
				focusOnOpen: false,
				onRemoving :function(){
				}
		};

		$mdPanel.open(config);
		return deferred.promise;
	}

	this.addDatasetInCache = function(datasets){
		var def=$q.defer();
		var dataToSend = [];
		angular.forEach(datasets, function(item){
			var dataset = ds.getAvaiableDatasetByLabel(item);
			if(dataset!=undefined){
				var params ={};
				params.datasetLabel = dataset.label;
				params.aggregation = cockpitModule_widgetSelection.getAggregation(undefined,dataset,undefined, undefined);
				params.parameters = ds.getParametersAsString(ds.getDatasetParameters(dataset.id.dsId));
				if(dataset.useCache==false){
					params.nearRealtime = true;
				}
				this.push(params);
			}
		},dataToSend)
		sbiModule_restServices.restToRootProject();
		sbiModule_restServices.promisePost("2.0/datasets","addDatasetInCache",dataToSend)
		.then(function(response){
			angular.forEach(datasets, function(item){
				this.push(item);
			},cockpitModule_properties.DS_IN_CACHE);

			def.resolve()
		},function(response){
			sbiModule_restServices.errorHandler(response.data,"");
			def.reject()
		})

		return def.promise;
	}

	this.autodetect=function(attachToElementWithId,tmpAvaiableDatasets,tmpAssociations){
		var deferred = $q.defer();
		var elemToAtt=document.body;
		if(attachToElementWithId!=undefined){
			elemToAtt=angular.element(document.getElementById(attachToElementWithId))
		}

		var config = {
				attachTo: elemToAtt,
				locals :{datasets:tmpAvaiableDatasets,associations:tmpAssociations,deferred:deferred},
				controller: function($scope,mdPanelRef,sbiModule_translate,cockpitModule_datasetServices,datasets,associations,deferred,$mdDialog){

					$scope.translate = sbiModule_translate;

					// table columns
					$scope.cockpitAutodetectColumns=[{label:sbiModule_translate.load("sbi.cockpit.association.editor.wizard.list.autodetect.similarity"),
						name:"___similarity",
						transformer:function(input){return $filter('number')(input * 100, 2) + '%';}
					}];
					angular.forEach(datasets,function(item){
						var column = {label:item.label, name:item.label};
						this.push(column);
					},$scope.cockpitAutodetectColumns);

					// table search columns
					$scope.cockpitAutodetectColumnsSearch=[];
					angular.forEach(datasets,function(item){
						this.push(item.label);
					},$scope.cockpitAutodetectColumnsSearch);

					// table selected row
					$scope.cockpitAutodetectSelectedRow = null;

					$scope.saveAutodetect=function(){
						deferred.resolve(angular.copy($scope.cockpitAutodetectSelectedRow));
						mdPanelRef.close();
						$scope.$destroy();
					}

					$scope.closeDialog=function(){
						mdPanelRef.close();
						$scope.$destroy();
						deferred.reject();
					}

					// Similarity filter management

					$scope.minSimilarity = 0.2;

					$scope.minSimilarityValues = [0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2];
					for(var i=$scope.minSimilarityValues.length-1; i>=0; i--){
						if($scope.minSimilarityValues[i] < $scope.minSimilarity){
							$scope.minSimilarityValues.splice(i, 1);
						}
					}

					$scope.selectedMinSimilarityValue = $scope.minSimilarity;

					$scope.$watch("selectedMinSimilarityValue",function(newValue,oldValue){
						$scope.filterCockpitAutodetectRows(newValue, $scope.selectedMinLengthValue);
					});

					// Length filter management

					$scope.minLength = 2;

					$scope.minLengthValues = [];
					for(var i=$scope.minLength; i<=datasets.length; i++){
						$scope.minLengthValues.unshift(i);
					}

					$scope.selectedMinLengthValue = $scope.minLength;

					$scope.$watch("selectedMinLengthValue",function(newValue,oldValue){
						$scope.filterCockpitAutodetectRows($scope.selectedMinSimilarityValue, newValue);
					});

					// Filtered table model

					$scope.cockpitAutodetectRows = [];
					$scope.cockpitAutodetectFilteredRows = [];
					$scope.showTable = false;

					$scope.filterCockpitAutodetectRows=function(minSimilarity, minLength){
						var rows = [];
						angular.copy($scope.cockpitAutodetectRows, rows);

						for(var i=rows.length-1; i>=0; i--){
							var row = rows[i];
							if(row["___similarity"] < minSimilarity || row["___length"] < minLength){
								rows.splice(i, 1);
							}
						}

						angular.copy(rows, $scope.cockpitAutodetectFilteredRows);
					}

					var datasetNames = {};
					angular.forEach(datasets,function(item){
						var params = {};
						angular.forEach(item.parameters,function(parameter){
							this[parameter.name] = (parameter.value ? parameter.value : parameter.defaultValue);
						},params);
						this[item.label] = params;
					},datasetNames);

					var payload = JSON.stringify(datasetNames);
					sbiModule_restServices.restToRootProject();
					sbiModule_restServices.promisePost("2.0/datasetsee","associations/autodetect?wait=true&aggregate=true&evaluateNumber=true&threshold=" + $scope.minSimilarity, payload)
					.then(function(response){
						// get table rows from REST service response
						$scope.cockpitAutodetectRows = [];
						angular.forEach(response.data,function(item, key){
							var row = {};
							row["___id"] = key;
							row["___similarity"] = item.coefficient;
							row["___length"] = item.fields.length;
							angular.forEach(datasets,function(dataset){
								row[dataset.label] = null;
							}, row);
							angular.forEach(item.fields,function(field){
								row[field.datasetLabel] = field.datasetColumn;
							}, row);
							this.push(row);
						},$scope.cockpitAutodetectRows);

						// remove rows equal to existing associations
						for(var i=$scope.cockpitAutodetectRows.length-1; i>=0; i--){
							var autodetectRow = $scope.cockpitAutodetectRows[i];
							for(var j=0; j<associations.length; j++){
								var association = associations[j];
								var isEqual = true;
								for(var k=0; k<association.fields.length; k++){
									var field = association.fields[k];
									if(!autodetectRow.hasOwnProperty(field.store) || autodetectRow[field.store] != field.column){
										isEqual = false;
									}
								}
								if(isEqual){
									$scope.cockpitAutodetectRows.splice(i, 1);
								}
							}
						}

						angular.copy($scope.cockpitAutodetectRows, $scope.cockpitAutodetectFilteredRows);
						$scope.showTable = true;
					},function(response){
						$scope.showTable = true;
						sbiModule_restServices.errorHandler(response.data,"");
					});
				},
				disableParentScroll: true,
				templateUrl: baseScriptPath+'/directives/cockpit-data-configuration/templates/dataAssociationAutodetectChoice.html',
//				hasBackdrop: true,
				position: $mdPanel.newPanelPosition().absolute().center(),
				trapFocus: true,
				zIndex: 150,
				fullscreen :true,
				clickOutsideToClose: true,
				escapeToClose: false,
				focusOnOpen: false,
				onRemoving :function(){
				}
		};

		$mdPanel.open(config);
		return deferred.promise;
	}

	this.substitutePlaceholderValues = function(text, datasetLabel, model){

		if(text != undefined){
			var deferred = $q.defer();
			var dataset = this.getDatasetByLabel(datasetLabel);
			if (dataset != undefined) {
				var columnsToshow = [];
				var columnsToshowMeta = [];
				var columnsToshowIndex = [];
				var localModel = model;
				var datasetId = dataset.id.dsId;
				//model.dataset = {}
				//model.dataset.dsId = datasetId;

				var regAggFunctions = 'AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT'

					//get columnsSelected metadata: adds aggregation functions if required
					for(var dsField in dataset.metadata.fieldsMeta){
						var dsObject = dataset.metadata.fieldsMeta[dsField];
						var header = dsObject.alias;
						var reg = new RegExp('('+regAggFunctions+')\\(\\$F{'+dataset.label+'.'+header+'}\\)|\\$F{'+dataset.label+'.'+header+'}','g');
						var matches = text.match(reg);
						if(matches){
							dsObject.aggregationSelected = [];
							var noAggregation = false;

							for(var j = 0; j < matches.length; j++){
								//aggregation function management (ie: COUNT($F{xxx}) )
								var regAgg = new RegExp('^'+regAggFunctions,'g');
								var matchAgg = matches[j].match(regAgg);
								if (matchAgg){
									var aggFunc = matchAgg[0];
									if(dsObject.aggregationSelected.indexOf(aggFunc) == -1){
										dsObject.aggregationSelected.push(aggFunc);

									}
								}else{
									noAggregation = true;
								}














							}

							if(noAggregation){
								dsObject.aggregationSelected.push('NONE');
							}



							if(dsObject.aggregationSelected != undefined){
								for(var i = 0; i<dsObject.aggregationSelected.length; i++){
									var agg = dsObject.aggregationSelected[i];
									columnsToshow.push(dataset.label+'.'+header+':'+agg);
									columnsToshowMeta.push(dsObject);
								}


							}

						}
					}
//				model.content.columnSelectedOfDataset = dataset.metadata.fieldsMeta;
				model.content.columnSelectedOfDataset = columnsToshowMeta;

				this.loadDatasetRecordsById(datasetId, undefined, undefined, undefined, undefined, model).then(function(allDatasetRecords){

					//get columnsSelected dataIndex

					var fieldCounterInserted = {};
					var alreadyInserted = [];
					var currentCounter = 0;
					var fieldCounter;

					for (var col in columnsToshow){
						var headerToSearchTmp = columnsToshow[col].substring(columnsToshow[col].indexOf('.')+1);
						var headerToSearch;
						if(headerToSearchTmp.indexOf(':')>=0){
							headerToSearch = headerToSearchTmp.substring(0, headerToSearchTmp.indexOf(':'));
						}
						else{
							headerToSearch = headerToSearchTmp;
						}

						if(fieldCounterInserted[headerToSearch] == undefined) fieldCounterInserted[headerToSearch]=0;
						fieldCounter = fieldCounterInserted[headerToSearch];
						currentCounter = 0;

						for (var field in allDatasetRecords.metaData.fields){
							if (allDatasetRecords.metaData.fields[field] && allDatasetRecords.metaData.fields[field].header){
								var header = allDatasetRecords.metaData.fields[field].header;
								if (header == headerToSearch){

									if(alreadyInserted.indexOf(headerToSearchTmp) >= 0){
										// this means that field with aggregation was already inserted
										break;
									}
									else{
										if(currentCounter >= fieldCounter){

											// if fieldCounter > 0 means there are more occurrences of that field with different aggregation, than jump to next
											columnsToshowIndex.push(columnsToshow[col] + '|' +allDatasetRecords.metaData.fields[field].dataIndex);

											var counter = fieldCounterInserted[headerToSearch];
											var counter = counter+1;
											fieldCounterInserted[headerToSearch] = counter;

											alreadyInserted.push(headerToSearchTmp);

											break;
										}
									}
									currentCounter++;
								}
							}
						}
					}
					//get columnsSelected values and replace placeholders
					var row = allDatasetRecords.rows[0] || []; //get the first row
					for (var col in columnsToshowIndex){
						var colAliasTmp =  columnsToshowIndex[col].substring(0,  columnsToshowIndex[col].indexOf('|'));
						var colAlias;
						if(colAliasTmp.indexOf(':')>=0){
							colAlias =  colAliasTmp.substring(0,  colAliasTmp.indexOf(':'));
						}
						else{
							colAlias =  colAliasTmp;
						}

						var aggregation = '';
						if(colAliasTmp.indexOf(':')>=0){
							aggregation= colAliasTmp.substring(colAliasTmp.indexOf(':')+1);
						}
						aggregation = aggregation.toUpperCase();

						var colIdx = columnsToshowIndex[col].substring( columnsToshowIndex[col].indexOf('|')+1);
						var colValue = row[colIdx];
						if(colValue == undefined) colValue ='';

						if(allDatasetRecords.metaData.fields[1].type == 'float' && model.numbers){
							if(model.numbers.format) colValue = $filter('number')(colValue,model.numbers.precision);
							else colValue = model.numbers.precision ? parseFloat(colValue).toFixed(model.numbers.precision) : colValue;
							colValue = (model.numbers.prefix || '') + colValue + (model.numbers.suffix || '');
						}



						// if aggregation is specified search for right match and not for a generic one

						var reg;
						if(aggregation != ''){
							reg = new RegExp(aggregation+'(\\(\\$F{'+colAlias+'}\\))','g');
						}
						else{
							reg = new RegExp('\\$F{'+colAlias+'}','g');
						}


						//at first check for aggregation functions , than for simple values
						//var reg = new RegExp('(AVG|MIN|MAX|SUM|COUNT_DISTINCT|COUNT|DISTINCT COUNT)(\\(\\$F{'+colAlias+'}\\))','g');
						var matches = text.match(reg);
						if (matches){
							text = text.replace(reg, colValue);
						}else{
							var reg = new RegExp('\\$F\\{('+colAlias+')\\}','g');
							matches = text.match(reg);
							if (matches){
								text = text.replace(reg, colValue);
							}
						}
					}
					deferred.resolve(text);
				},function(error){
					deferred.reject(error);
				});
				return deferred.promise;

			}

		}

	}

	//conditional value formatting
//	ds.formatValue = function (value, numbersModel){
//	var output = value;
//	if(!numbersModel || !numbersModel.precision || numbersModel.precision < 0) numbersModel.precision = 2;

//	//setting the number precision when format is not present
//	if (numbersModel && numbersModel.precision && !numbersModel.format) {
//	output = parseFloat(value).toFixed(numbersModel.precision);
//	}

//	if (numbersModel && numbersModel.format){
//	switch (numbersModel.format) {
//	case "#.###":
//	output = ds.numberFormat(value, 0, ',', '.');
//	break;
//	case "#,###":
//	output = ds.numberFormat(value, 0, '.', ',');
//	break;
//	case "#.###,##":
//	output = ds.numberFormat(value, numbersModel.precision, ',', '.');
//	break;
//	case "#,###.##":
//	output = ds.numberFormat(value, numbersModel.precision, '.', ',');
//	break;
//	default:
//	break;
//	}
//	}
//	return (numbersModel.prefix||'') + output + (numbersModel.suffix||'');
//	}

//	//formatting function with the given parameters
//	ds.numberFormat = function (value, dec, dsep, tsep) {

//	if (isNaN(value) || value == null) return value;

//	value = parseFloat(value).toFixed(~~dec);
//	tsep = typeof tsep == 'string' ? tsep : ',';

//	var parts = value.split('.'), fnums = parts[0],
//	decimals = parts[1] ? (dsep || '.') + parts[1] : '';

//	return fnums.replace(/(\d)(?=(?:\d{3})+$)/g, '$1' + tsep) + decimals;
//	}

})
.run(function() {
	//adds methods for IE11
	if (!String.prototype.startsWith) {
		String.prototype.startsWith = function(searchString, position){
			position = position || 0;
			return this.substr(position, searchString.length) === searchString;
		};
	}

	if (!String.prototype.endsWith) {
		String.prototype.endsWith = function(searchString, position) {
			var subjectString = this.toString();
			if (typeof position !== 'number' || !isFinite(position) || Math.floor(position) !== position || position > subjectString.length) {
				position = subjectString.length;
			}
			position -= searchString.length;
			var lastIndex = subjectString.lastIndexOf(searchString, position);
			return lastIndex !== -1 && lastIndex === position;
		};
	}

});