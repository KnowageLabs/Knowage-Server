angular.module("cockpitModule").service("cockpitModule_datasetServices",function(sbiModule_translate,sbiModule_restServices,cockpitModule_template, $q, $mdPanel,cockpitModule_widgetSelection,cockpitModule_properties,cockpitModule_utilstServices, $rootScope,sbiModule_messaging){
	var ds=this;

	this.datasetList=[];
	this.infoColumns = [];


	this.loadDatasetList=function(){
		var def=$q.defer();
		sbiModule_restServices.restToRootProject();
		sbiModule_restServices.promiseGet("2.0/datasets","listDataset")
		.then(function(response){
			angular.copy(response.data.item,ds.datasetList);
			ds.checkForDSChange();
			cockpitModule_widgetSelection.getAssociations(cockpitModule_properties.HAVE_SELECTIONS_OR_FILTERS,undefined,def);
//			def.resolve();
		},function(response){
			sbiModule_restServices.errorHandler(response.data,"");
			def.reject();
		})
		return def.promise;
	};

	this.checkForDSChange=function(){
		var changed=[];
		angular.forEach(cockpitModule_template.configuration.datasets,function(item){
			var actualDs=ds.getDatasetById(item.dsId);
			//check if label change
			if(!angular.equals(actualDs.label,item.dsLabel)){
				var oldlab=angular.copy(item.dsLabel);
				//update the label of dataset
				this.push("DATASET LABEL: "+item.dsLabel+" -> "+actualDs.label)
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

			//check if name change
			if(!angular.equals(actualDs.name,item.name)){
				//update the name of dataset
				this.push("DATASET NAME: "+item.name+" -> "+actualDs.name)
				item.name=actualDs.name;
			}
			
			//check if parameter change
			var parChange=false;
			if(actualDs.parameters!=undefined && item.parameters!=undefined){
				
				if((actualDs.parameters==undefined && item.parameters!=undefined) || (actualDs.parameters!=undefined && item.parameters==undefined)){
					parChange=true;
				}else if(actualDs.parameters.length != Object.keys(item.parameters).length){
					parChange=true;
				}else{
					for(var p=0;p<actualDs.parameters.length;p++){
						if(!item.parameters.hasOwnProperty(actualDs.parameters[p].name)){
							parChange=true;
							break;
						}
					}
				}
			}
			if(parChange){
				this.push("<b>!! The parameter of "+item.dsLabel+" dataset are changed. The cockpit may not work !!</b>")
			}
			
		},changed)
		if(changed.length>0){
			sbiModule_messaging.showErrorMessage(changed.join("<br>"),"Dataset information change")
		}
	}
	this.getDatasetList=function(){
		return angular.copy(ds.datasetList);
	}

	//return a COPY of dataset with specific id or null
	this.getDatasetById=function(dsId){
		for(var i=0;i<ds.datasetList.length;i++){
			if(angular.equals(ds.datasetList[i].id.dsId,dsId)){
				var tmpDS={};
				angular.copy(ds.datasetList[i],tmpDS);
				return tmpDS;
			}
		}
	}
	//return a COPY of dataset with specific label or null
	this.getDatasetByLabel=function(dsLabel){
		for(var i=0;i<ds.datasetList.length;i++){
			if(angular.equals(ds.datasetList[i].label,dsLabel)){
				var tmpDS={};
				angular.copy(ds.datasetList[i],tmpDS);
				return tmpDS;
			}
		}
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
			var dsIl=ds.getDatasetById(cockpitModule_template.configuration.datasets[i].dsId);
			if(dsIl!=undefined){
				dsIl.useCache=cockpitModule_template.configuration.datasets[i].useCache;
				dsIl.frequency=cockpitModule_template.configuration.datasets[i].frequency;
				if(cockpitModule_template.configuration.datasets[i].parameters!=undefined){
					angular.forEach(dsIl.parameters,function(item){
						item.value=cockpitModule_template.configuration.datasets[i].parameters[item.name];
					})
				}
				fad.push(dsIl);
			}else{
				console.error("ds with id "+cockpitModule_template.configuration.datasets[i].dsId +" not found;")
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
		tmpDS.useCache=avDataset.useCache;
		tmpDS.frequency=avDataset.frequency;
		tmpDS.parameters={};
		if(avDataset.parameters!=undefined){
			for(var p=0;p<avDataset.parameters.length;p++){
				tmpDS.parameters[avDataset.parameters[p].name]=avDataset.parameters[p].value;
			}
		}
		
		cockpitModule_template.configuration.datasets.push(tmpDS);
	}

	this.getDatasetParameters=function(dsId){
		var param={};
		for(var i=0;i<cockpitModule_template.configuration.datasets.length;i++){
			if(angular.equals(cockpitModule_template.configuration.datasets[i].dsId,dsId)){
				angular.forEach(cockpitModule_template.configuration.datasets[i].parameters,function(item,key){
						this[key]=cockpitModule_utilstServices.getParameterValue(item);
				},param)
			}
		}
		
		var datasetLabel=ds.getDatasetById(dsId).label;		
		var selections=cockpitModule_widgetSelection.getCurrentSelections(datasetLabel);
		if(selections!=undefined && selections.hasOwnProperty(datasetLabel)){
			for(var parName in selections[datasetLabel]){
				if(parName.startsWith("$P{") && parName.endsWith("}")){
					var parValue=selections[datasetLabel][parName];
					if(parValue!=undefined){
						var finalP=[];
						angular.forEach(parValue,function(item){
							this.push(item.substring(2,item.length-2))
						},finalP)
						param[parName.substring(3,parName.length-1)]=finalP.join(",");
					}
				}
			}
		}
		return param;
	}

	
	//TODO missing maxRows
	this.loadDatasetRecordsById = function(dsId, page, itemPerPage,columnOrdering, reverseOrdering, ngModel){
		//after retry LabelDataset by Id call service for data
		var dataset = this.getAvaiableDatasetById(dsId);
		var deferred = $q.defer();
		var params="";

		var aggregation = cockpitModule_widgetSelection.getAggregation(ngModel,dataset,columnOrdering, reverseOrdering);

		var aggr = encodeURIComponent(JSON.stringify(aggregation))
		.replace(/'/g,"%27")
		.replace(/"/g,"%22");

		var par = encodeURIComponent(JSON.stringify(ds.getDatasetParameters(dsId)))
		.replace(/'/g,"%27")
		.replace(/"/g,"%22");
		params =  "?aggregations=" +aggr+"&parameters="+par;
		if(page !=undefined && itemPerPage !=undefined){
			params=params+"&offset="+(page*itemPerPage)+"&size="+itemPerPage;
		}

		if(ngModel.style !=undefined && ngModel.style.showSummary ==true){
			var summaryrow = encodeURIComponent(JSON.stringify(ds.getSummaryRow(ngModel)))
				.replace(/'/g,"%27")
				.replace(/"/g,"%22");
			params =  params+"&summaryRow=" +summaryrow;
		}

		if(dataset.useCache==false){
			params+="&realtime=true";
		}
		
		var dataToSend=cockpitModule_widgetSelection.getCurrentSelections(dataset.label);
		if(Object.keys(dataToSend).length==0){
			dataToSend=cockpitModule_widgetSelection.getCurrentFilters(dataset.label);
		}
		
		var dataToSendWithoutParams = {};
		angular.copy(dataToSend,dataToSendWithoutParams);
		angular.forEach(dataToSendWithoutParams, function(item){
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
		
		sbiModule_restServices.restToRootProject();
		sbiModule_restServices.promisePost("2.0/datasets",encodeURIComponent(dataset.label)+"/data"+params,dataToSendWithoutParams)
		.then(function(response){
			if(cockpitModule_properties.DS_IN_CACHE.indexOf(dataset.label)==-1){
				cockpitModule_properties.DS_IN_CACHE.push(dataset.label);
			}
			deferred.resolve(response.data);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,"");
			deferred.reject('Error');
		})

		return deferred.promise;

	}

	this.getSummaryRow = function(ngModel){
		var measures = [];
		var ds = ngModel.dataset.label;
		var columns = ngModel.content.columnSelectedOfDataset;
		
		if(columns != undefined){
			//create aggregation
			for(var i=0;i<columns.length;i++){
				var col = columns[i];
				var obj = {};
				obj["id"] = col.alias;
				obj["alias"] = ngModel.type == "table" ? col.aliasToShow : col.alias;
				obj["funct"] = col.funcSummary == undefined? "" : col.funcSummary;
//				if(col.isCalculated == true){
//					obj["columnName"] = col.formula;
//				}else{
					obj["columnName"] = col.name;
//				}
				
			
				if(col.fieldType=="ATTRIBUTE"){
					//none
				}else{
					//it is measure add it
					measures.push(obj);
				}
			}
		}
		var result = {};
		result["measures"] = measures;
		result["dataset"] = ds;

		return result;

	}
	
	this.addDataset=function(attachToElementWithId,container,multiple,autoAdd){
		var deferred = $q.defer();
		var eleToAtt=document.body;
		if(attachToElementWithId!=undefined){
			eleToAtt=angular.element(document.getElementById(attachToElementWithId))
		}

		var config = {
				attachTo: eleToAtt,
				locals :{currentAvaiableDataset:container,multiple:multiple,deferred:deferred},
				controller: function($scope,mdPanelRef,sbiModule_translate,cockpitModule_datasetServices,currentAvaiableDataset,multiple,deferred,$mdDialog){
					$scope.tmpCurrentAvaiableDataset;
					if(multiple){
						tmpCurrentAvaiableDataset=[];
					}else{
						tmpCurrentAvaiableDataset={};
					}
					$scope.multiple=multiple;
					$scope.cockpitDatasetColumn=[{label:"Label",name:"label"},{label:"Name",name:"name" } ];
					$scope.datasetList=cockpitModule_datasetServices.getDatasetList();
					//TODO rimuovere i dataset già presenti
					$scope.datasetList=cockpitModule_datasetServices.getDatasetList();
					//remove avaiable dataset
					for(var i=0;i<currentAvaiableDataset.length;i++){
						for(var j=0;j<$scope.datasetList.length;j++){
							if(angular.equals(currentAvaiableDataset[i].id.dsId,$scope.datasetList[j].id.dsId)){
								$scope.datasetList.splice(j,1);
								break;
							}
						}
					}
					$scope.closeDialog=function(){
						mdPanelRef.close();
						$scope.$destroy();
						deferred.reject();
					}
					$scope.saveDataset=function(){
						if(multiple){
							for(var i=0;i<$scope.tmpCurrentAvaiableDataset.length;i++){
								if(autoAdd){
									ds.addAvaiableDataset($scope.tmpCurrentAvaiableDataset[i])
								}else{
									currentAvaiableDataset.push($scope.tmpCurrentAvaiableDataset[i]);
								}
							}
							
							deferred.resolve(angular.copy($scope.tmpCurrentAvaiableDataset));
							mdPanelRef.close();
							$scope.$destroy();
							
						}else{
							if($scope.tmpCurrentAvaiableDataset.parameters!=null && $scope.tmpCurrentAvaiableDataset.parameters.length>0){
								//fill the parameter
								 
								 $mdDialog.show({
								      controller: function($scope,sbiModule_translate,parameters){
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
								      locals:{parameters:$scope.tmpCurrentAvaiableDataset.parameters}
								    })
								    .then(function(data) {
								    	angular.copy(data,$scope.tmpCurrentAvaiableDataset.parameters)
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
								if(autoAdd){
									ds.addAvaiableDataset($scope.tmpCurrentAvaiableDataset)
								}else{
									angular.copy($scope.tmpCurrentAvaiableDataset,currentAvaiableDataset);
								}
								deferred.resolve(angular.copy($scope.tmpCurrentAvaiableDataset));
								mdPanelRef.close();
								$scope.$destroy();
								
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
	
	this.addDatasetInCache = function(listDataset){
		var def=$q.defer();
		var dataToSend = [];
		angular.forEach(listDataset, function(item){
			var dataset = ds.getAvaiableDatasetByLabel(item);
			if(dataset!=undefined){
				var params ={};
				params.datasetLabel = dataset.label;
				params.aggregation = cockpitModule_widgetSelection.getAggregation(undefined,dataset,undefined, undefined);
				params.parameters = ds.getDatasetParameters(dataset.id.dsId);
				if(dataset.useCache==false){
					params.realtime = true;
				}
				this.push(params);
			}
		},dataToSend)
		sbiModule_restServices.restToRootProject();
		sbiModule_restServices.promisePost("2.0/datasets","addDatasetInCache",dataToSend)
		.then(function(response){
			angular.forEach(listDataset, function(item){
				this.push(item);
			},cockpitModule_properties.DS_IN_CACHE);
			
			def.resolve()
		},function(response){
			sbiModule_restServices.errorHandler(response.data,"");
			def.reject()
		})
		
		return def.promise;
	}

})