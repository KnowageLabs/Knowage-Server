angular.module("cockpitModule").service("cockpitModule_documentServices",function(sbiModule_translate,sbiModule_restServices,cockpitModule_template, $q,$mdPanel){
	var ds=this;
	
	
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
		return cockpitModule_template.configuration.documents;
	}
	
	this.setAvaiableDocument=function(avDoc){
		angular.copy(avDoc,cockpitModule_template.configuration.documents);	
	}
	this.addAvaiableDocument=function(avDoc){
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
	
	this.loadDocumentList=function(searchValue, itemsPerPage, currentPageNumber , columnsSearch,columnOrdering, reverseOrdering,excludeItem){
		var deferred = $q.defer();
		if(searchValue==undefined || searchValue.trim().lenght==0 ){
			searchValue='';
		}
		var item="Page="+currentPageNumber+"&ItemPerPage="+itemsPerPage+"&label=" + searchValue+"&name=" + searchValue+"&loadObjPar=true&excludeType=DOCUMENT_COMPOSITE";
		
		if(excludeItem!=undefined && excludeItem.length>0){
			item+="&objLabelNotIn="+excludeItem.join(",");
		}
		sbiModule_restServices.restToRootProject();
		sbiModule_restServices.promiseGet("2.0/documents", "listDocument",item).then(
				function(response){
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
				    		var excludeItem=[];
				    		for(var i=0;i<currentAvaiableDocument.length;i++){
				    			excludeItem.push(currentAvaiableDocument[i].DOCUMENT_LABEL)
				    		}
							ds.loadDocumentList(searchValue, itemsPerPage, currentPageNumber , columnsSearch,columnOrdering, reverseOrdering,excludeItem).then(
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
				    				if(autoAdd){
					    				ds.addAvaiableDocument($scope.tmpCurrentAvaiableDocument[i])
					    			}else{
					    				currentAvaiableDocument.push($scope.tmpCurrentAvaiableDocument[i]);
					    			}
				    			}
				    		}else{
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
	
	
});