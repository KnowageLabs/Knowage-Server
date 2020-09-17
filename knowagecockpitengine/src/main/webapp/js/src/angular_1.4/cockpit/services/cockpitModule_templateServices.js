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

	this.getDatasetIdsInSameSheet = function(widgetId){
        var dsList = [];

        var sheetIndex = -1;
        for(var i=0; i<cockpitModule_template.sheets.length && sheetIndex == -1; i++){
            var sheet = cockpitModule_template.sheets[i];
            for(var j=0; j<sheet.widgets.length; j++){
                var widget = sheet.widgets[j];
                if(widget.id == widgetId){
                    sheetIndex = i;
                    break;
                }
            }
        }

        if(sheetIndex > -1){
            var widgets = cockpitModule_template.sheets[sheetIndex].widgets;
            for(var i=0; i<widgets.length; i++){
                if(widgets[i].dataset) {
                    var id = widgets[i].dataset.dsId;
                    if(dsList.indexOf(id) == -1){
                        dsList.push(id);
                    }
                }
            }
        }

        return dsList;
    }

	this.getNumberOfWidgets = function(){
		var total = 0;
		for(var i=0;i<cockpitModule_template.sheets.length;i++){
			total += cockpitModule_template.sheets[i].widgets.length;
		}
		return total;
	}

	this.getAllCockpitWidgets = function(){
		var widgets = [];
		for(var i=0; i<cockpitModule_template.sheets.length; i++){
			var sheetWidgets = cockpitModule_template.sheets[i].widgets;
			for(var j=0; j<sheetWidgets.length; j++){
				var w = sheetWidgets[j];
				widgets.push(w);
			}
		}
		return widgets;
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

	// Function used to retrieve the dataset with parameters list

	this.getDatasetUsetByWidgetWithParams = function(){
		var taintedColumns = {};
		for (var k in cockpitModule_template.configuration.associations) {
			var tempFields = {};
			var taintedAssociations = false;
			for (var j in cockpitModule_template.configuration.associations[k].fields) {
				if (cockpitModule_template.configuration.associations[k].fields[j].column.match(/\$P\{/g)) {
					taintedAssociations = true;
				}
				else {
					tempFields[cockpitModule_template.configuration.associations[k].fields[j].store] = cockpitModule_template.configuration.associations[k].fields[j].column;
				}
			}
			if (taintedAssociations) {
				for (var y in tempFields) {
					if (taintedColumns[y]) {
						if (taintedColumns[y].indexOf(tempFields[y]) != -1) {
							taintedColumns[y].push(tempFields[y]);
						}
					}
					else {
						taintedColumns[y] = [tempFields[y]];
					}
				}
			}
		}
		cockpitModule_properties.TAINTED_ASSOCIATIONS = taintedColumns;
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

	this.getAssociatedDatasetLabels=function(datasetLabels, associations){
        if(associations == undefined){
            associations = cockpitModule_template.configuration.associations;
        }

        associatedDsLabels = [];

        for(var i in associations){
            var association = associations[i];
            for(var j in association.fields){
                var field = association.fields[j];
                if(field.type=="dataset" && datasetLabels.indexOf(field.store) > -1){
                    for(var k in association.fields){
                        var associatedField = association.fields[k];
                        if(associatedField.type=="dataset" && associatedDsLabels.indexOf(associatedField.store) == -1){
                            associatedDsLabels.push(associatedField.store);
                        }
                    }
                    break;
                }
            }
        }

        return associatedDsLabels;
    }
});