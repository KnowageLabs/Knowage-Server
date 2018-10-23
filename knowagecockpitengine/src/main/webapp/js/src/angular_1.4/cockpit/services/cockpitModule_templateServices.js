angular.module("cockpitModule").service("cockpitModule_templateServices",function(sbiModule_translate,sbiModule_restServices,cockpitModule_template, $q, $mdPanel,$rootScope,cockpitModule_properties){
	var ts=this;

	this.getLabelDatasetsUsed = function(){
		var array = [];
		for(var i=0;i<cockpitModule_template.sheets.length;i++){
			var sheet = cockpitModule_template.sheets[i];
			for(var j=0;j<sheet.widgets.length;j++){
				var widget = sheet.widgets[j];
				if(widget.dataset !=undefined){
					for(var k=0;k<cockpitModule_template.configuration.datasets.length;k++){
						var obj = cockpitModule_template.configuration.datasets[k];
						if(obj.dsId == widget.dataset.dsId){
							array.push( obj.dsLabel);
							break;
						}
					}
				}
			}
		}
		return array;
	}


	this.getNumberOfWidgets = function(){
		var total = 0;
		for(var i=0;i<cockpitModule_template.sheets.length;i++){
			total += cockpitModule_template.sheets[i].widgets.length;
		}
		return total;
	}

	this.getDatasetAssociatedNotUsedByWidget = function(){
		var dsNotInCache = [];
		var dsUsed = ts.getLabelDatasetsUsed();
		var dsUsedByAssociation = ts.getDatasetInAssociation();

			for(var k=0;k<dsUsedByAssociation.length;k++){
				var ds = dsUsedByAssociation[k];
				if(dsUsed.indexOf(ds) ==-1){
					//ds not used
					dsNotInCache.push(ds);
				}
			}
			return dsNotInCache;
	}

	this.getDatasetUsetByWidgetNotAssociated = function(){
		var dsNotAss = [];
		var dsUsed = ts.getLabelDatasetsUsed();
		var dsUsedByAssociation = ts.getDatasetInAssociation();

			for(var k=0;k<dsUsed.length;k++){
				var ds = dsUsed[k];
				if(dsUsedByAssociation.indexOf(ds) ==-1 && dsNotAss.indexOf(ds)==-1){
					//ds not used
					dsNotAss.push(ds);
				}
			}
			return dsNotAss;
	}


	this.getDatasetInAssociation = function(){
		var dsList = [];
		for(var i=0;i<cockpitModule_template.configuration.aggregations.length;i++){
			var aggr = cockpitModule_template.configuration.aggregations[i];
			for(var j=0;j<aggr.datasets.length;j++){
				if(!ts.isDoc(aggr.datasets[j])){
					dsList.push(aggr.datasets[j]);
				}
			}
		}

		return dsList;
	}

	this.isDoc=function(label){
		for(var j=0;j<cockpitModule_template.configuration.documents.length;j++){
			if(angular.equals(cockpitModule_template.configuration.documents[j].DOCUMENT_LABEL,label)){
				return true;
			}
		}
		return false;
	}

	this.isDatasetUsedByAssociations=function(datasetLabel, associations){
		if(associations == undefined){
			associations = cockpitModule_template.configuration.associations;
		}
		for(var i in associations){
			var association = associations[i];
			for(var j in association.fields){
				var field = association.fields[j];
				if(field.type=="dataset" && field.store==datasetLabel){
					return true;
				}
			}
		}
		return false;
	}
});