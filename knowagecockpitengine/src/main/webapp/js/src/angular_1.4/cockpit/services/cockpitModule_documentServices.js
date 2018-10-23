angular.module("cockpitModule").service("cockpitModule_documentServices",function(sbiModule_translate,sbiModule_restServices,cockpitModule_template, $q,$mdPanel,sbiModule_messaging){
	var ds=this;

	this.loadDocumentsFromTemplate=function(){
		var def=$q.defer();
		if(cockpitModule_template.configuration.documents.length == 0){
			def.resolve();
		}else{
			var documentLabels = [];
			angular.forEach(cockpitModule_template.configuration.documents, function(item){
				this.push(item.DOCUMENT_LABEL);
			}, documentLabels);

			ds.loadDocumentList("", undefined, undefined, undefined, undefined, false, documentLabels, undefined, true);
		}
		return def.promise;
	};

	//return a COPY of document with specific id or null
	this.getDocumentById=function(docId){
		for(var i=0;i<cockpitModule_template.configuration.documents.length;i++){
			if(angular.equals(cockpitModule_template.configuration.documents[i].DOCUMENT_ID,docId)){
				var tmpDOC={};
				angular.copy(cockpitModule_template.configuration.documents[i],tmpDOC);
				return tmpDOC;
			}
		}
	}
	//return a COPY of document with specific label or null
	this.getDocumentByLabel=function(docLabel,tmpList){
		var list=tmpList==undefined? cockpitModule_template.configuration.documents : tmpList;
		for(var i=0;i<list.length;i++){
			if(angular.equals(list[i].DOCUMENT_LABEL,docLabel)){
				var tmpDOC={};
				angular.copy(list[i],tmpDOC);
				return tmpDOC;
			}
		}
	}

	//return a list of avaiable document
	this.getAvaiableDocuments=function(){
		for(var i=0; i<cockpitModule_template.configuration.documents; i++){
			cockpitModule_template.configuration.documents[i].expanded = true;
		}
		return cockpitModule_template.configuration.documents;
	}

	this.setAvaiableDocument=function(avDoc){
		angular.copy(avDoc,cockpitModule_template.configuration.documents);
		for(var i=0; i<cockpitModule_template.configuration.documents; i++){
			cockpitModule_template.configuration.documents[i].expanded = true;
		}
	}
	this.addAvaiableDocument=function(avDoc){
		avDoc.expanded = true;
		cockpitModule_template.configuration.documents.push(avDoc);
	}

	this.getDocumentsUsed = function(){
		var array = [];
		var result = {};
		for(var i=0;i<cockpitModule_template.sheets.length;i++){
			var sheet = cockpitModule_template.sheets[i];
			for(var j=0;j<sheet.widgets.length;j++){
				var widget = sheet.widgets[j];
				if(widget.document !=undefined){
					array.push(widget.document.docId);
				}
			}
		}
		return array;

	}

	this.loadDocumentList=function(searchValue, itemsPerPage, currentPageNumber, columnsSearch, columnOrdering, reverseOrdering,includeItems, excludeItems, enableCheckForChanges){
		var deferred = $q.defer();

		if(searchValue==undefined || searchValue.trim().lenght==0 ){
			searchValue='';
		}
		var item = "label=" + searchValue+"&name=" + searchValue+"&loadObjPar=true&excludeType=DOCUMENT_COMPOSITE";
		if(currentPageNumber != undefined){
			item += "&Page=" + currentPageNumber;
		}
		if(itemsPerPage != undefined){
			item += "&ItemPerPage=" + itemsPerPage;
		}
		if(includeItems!=undefined && includeItems.length>0){
			item += "&objLabelIn="+includeItems.join(",");
		}
		if(excludeItems!=undefined && excludeItems.length>0){
			item += "&objLabelNotIn="+excludeItems.join(",");
		}
		item += "&forceVis=true";

		sbiModule_restServices.restToRootProject();
		sbiModule_restServices.promiseGet("2.0/documents", "listDocument",item).then(
				function(response){
					if(enableCheckForChanges){
						ds.checkForChanges(response.data.item);
					}
					deferred.resolve({items:response.data.item,count:response.data.itemCount});
				},

				function(response){
						sbiModule_restServices.errorHandler(response.data,"")
						deferred.reject();
					}
				)

		return deferred.promise;
	}

	this.addDocument=function(attachToElementWithId,container,multiple,autoAdd){
		var deferred = $q.defer();
		var eleToAtt=document.body;
		 if(attachToElementWithId!=undefined){
			 eleToAtt=angular.element(document.getElementById(attachToElementWithId))
		 }
		  var config = {
				    attachTo:eleToAtt ,
				    locals :{currentAvaiableDocument:container,multiple:multiple,deferred:deferred},
				    controller: function($scope,mdPanelRef,sbiModule_translate,cockpitModule_datasetServices,currentAvaiableDocument,multiple,deferred){
				    	$scope.tmpCurrentAvaiableDocument;
				    	if(multiple){
				    		tmpCurrentAvaiableDocument=[];
				    	}else{
				    		tmpCurrentAvaiableDocument={};
				    	}
				    	$scope.multiple=multiple;
				    	$scope.cockpitDocumentColumn=[{"label":"Label","name":"DOCUMENT_LABEL"}, {"label":"Name","name":"DOCUMENT_NAME"}];
				    	$scope.documentList=[];
				    	$scope.totalCount=0;
				    	$scope.changeDocPage=function(searchValue, itemsPerPage, currentPageNumber , columnsSearch,columnOrdering, reverseOrdering){
				    		var excludeItems=[];
				    		for(var i=0;i<currentAvaiableDocument.length;i++){
				    			excludeItems.push(currentAvaiableDocument[i].DOCUMENT_LABEL)
				    		}
							ds.loadDocumentList(searchValue, itemsPerPage, currentPageNumber , columnsSearch,columnOrdering, reverseOrdering, undefined, excludeItems).then(
									function(data){
										angular.copy(data.items,$scope.documentList);
										$scope.totalCount=data.count
									});

				    	}

				    	$scope.closeDialog=function(){
				    		mdPanelRef.close();
				    		$scope.$destroy();
				    		deferred.reject();
				    	}
				    	$scope.saveDocuments=function(){
				    		if(multiple){
				    			for(var i=0;i<$scope.tmpCurrentAvaiableDocument.length;i++){
				    				$scope.tmpCurrentAvaiableDocument[i].expanded = true;
				    				if(autoAdd){
					    				ds.addAvaiableDocument($scope.tmpCurrentAvaiableDocument[i])
					    			}else{
					    				currentAvaiableDocument.push($scope.tmpCurrentAvaiableDocument[i]);
					    			}
				    			}
				    		}else{
				    			$scope.tmpCurrentAvaiableDocument.expanded = true;
				    			if(autoAdd){
				    				ds.addAvaiableDocument($scope.tmpCurrentAvaiableDocument)
				    			}else{
				    				angular.copy($scope.tmpCurrentAvaiableDocument,currentAvaiableDocument);
				    			}
				    		}
				    		deferred.resolve(angular.copy($scope.tmpCurrentAvaiableDocument));

				    		mdPanelRef.close();
				    		$scope.$destroy();
				    	}
				    },
				    disableParentScroll: true,
				   templateUrl: baseScriptPath+ '/directives/cockpit-data-configuration/templates/cockpitDataConfigurationDocumentChoice.html',
				    position: $mdPanel.newPanelPosition().absolute().center(),
				    trapFocus: true,
				    zIndex: 150,
				    fullscreen :true,
				    clickOutsideToClose: false,
				    escapeToClose: false,
				    focusOnOpen: true,
				    onRemoving :function(){
				    }
				  };

		  $mdPanel.open(config);
		  return deferred.promise;
	}

	this.checkForChanges = function(docs){
		var changed=[];
		var removedDocumentParams=[];

		angular.forEach(cockpitModule_template.configuration.documents,function(item){
			var actualDoc;
			for(var i=0; i<docs.length;i++){
				var doc = docs[i];
				if(doc.DOCUMENT_ID == item.DOCUMENT_ID){
					actualDoc = doc;
					break;
				}
			}
			if(actualDoc == null){
				this.push(sbiModule_translate.load("sbi.cockpit.load.documentsInformation.removedDocument")
						.replace("{0}", "<b>" + item.DOCUMENT_ID + "</b>"));
			}else{
				item.expanded = true;
				
				var addedParams=[];
				var removedParams=[];

				//check if label changed
				if(!angular.equals(actualDoc.DOCUMENT_LABEL,item.DOCUMENT_LABEL)){
					var oldLabel=angular.copy(item.DOCUMENT_LABEL);
					//update the label of document
					this.push(sbiModule_translate.load("sbi.generic.label")+": "+item.DOCUMENT_LABEL+" -> "+actualDoc.DOCUMENT_LABEL)
					item.DOCUMENT_LABEL=actualDoc.DOCUMENT_LABEL;

					//update the dataset label in the associations
					for(var i=0;i<cockpitModule_template.configuration.associations.length;i++){
						var ass=cockpitModule_template.configuration.associations[i];
						if(ass.description.search("="+oldLabel+"\.")!=-1){
							ass.description=ass.description.replace("="+oldLabel+"\.", "="+item.DOCUMENT_LABEL+".");
							for(var f=0;f<ass.fields.length;f++){
								if(angular.equals(ass.fields[f].store,oldLabel)){
									ass.fields[f].store=item.DOCUMENT_LABEL;
									break;
								}
							}
						}
					}

					//update the document in the filters
					if(cockpitModule_template.configuration.filters.hasOwnProperty(oldLabel)){
						cockpitModule_template.configuration.filters[item.DOCUMENT_LABEL] = cockpitModule_template.configuration.filters[oldLabel];
						delete cockpitModule_template.configuration.filters[oldLabel];
					}
				}

				//check if name changed
				if(!angular.equals(actualDoc.DOCUMENT_NAME,item.DOCUMENT_NAME)){
					//update the name of document
					this.push(sbiModule_translate.load("sbi.generic.name")+": "+item.DOCUMENT_NAME+" -> "+actualDoc.DOCUMENT_NAME)
					item.DOCUMENT_NAME=actualDoc.DOCUMENT_NAME;
				}

				//check if parameters changed
				removedDocumentParams[item.DOCUMENT_LABEL] = [];
				if(actualDoc.objParameter!=undefined && item.objParameter!=undefined){
					var actualParameters = []; // create a copy of actual parameters that can be modified
					angular.copy(actualDoc.objParameter, actualParameters);

					//check removed params, removing matching ones
					for(var i=0; i<item.objParameter.length; i++){
						var param = item.objParameter[i];
						var removed = true;
						for(var j=actualParameters.length-1; j>=0; j--){
							if(param.label == actualParameters[j].label){
								removed = false;
								actualParameters.splice(j, 1);
								break;
							}
						}
						if(removed){
				    		removedParams.push(param.label);
				    		removedDocumentParams[item.DOCUMENT_LABEL].push(param.label);
				    		this.push(sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.removedParameter")
				    				.replace("{0}", "<b>" + item.DOCUMENT_LABEL + ".$P{" + param.label + "}</b>"));
				    	}
					}

					for(var i=0; i<actualParameters.length; i++){
						var actualParameter = actualParameters[i];
						addedParams.push(actualParameter);
						this.push(sbiModule_translate.load("sbi.cockpit.load.datasetsInformation.addedParameter")
								.replace("{0}", "<b>" + item.DOCUMENT_LABEL + ".$P{" + actualParameter.label + "}</b>"));
					}
				}

				//fix template parameters
				for(var i=0; i<addedParams.length; i++){
					var addedParam = addedParams[i];
					item.objParameter.push(addedParam);
				}
				for(var i=0; i<removedParams.length; i++){
					var removedParam = removedParams[i];
					for(var j=item.objParameter.length-1; j>=0; j--){
						if(item.objParameter[j].label == removedParam){
							item.objParameter.splice(j, 1);
						}
					}
				}
			}
		},changed)

		var modifiedAssociations = 0;
		angular.forEach(cockpitModule_template.configuration.associations,function(item){
			//fix fields & description
			var modifiedAssociation = 0;

			for(var i=item.fields.length-1; i>=0; i--){
				var field = item.fields[i];
				var paramName = (field.column.startsWith("$P{") && field.column.endsWith("}")) ? field.column.substring(3, field.column.length - 1) : field.column;
				if(field.type == "document" && removedDocumentParams[field.store] && removedDocumentParams[field.store].indexOf(paramName) > -1){
					item.description = item.description.replace(field.store + "." + field.column, "");
					if(item.description.startsWith("=")){
						item.description = item.description.substring(1);
					}else if(item.description.endsWith("=")){
						item.description = item.description.substring(0, str.length - 1);
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

		if(changed.length>0){
			changed.push(sbiModule_translate.load("sbi.cockpit.load.documentsInformation.checkconfigandsave"));
			sbiModule_messaging.showErrorMessage(changed.join("<br>"), sbiModule_translate.load("sbi.cockpit.load.documentsInformation.title"));
		}
	}
});