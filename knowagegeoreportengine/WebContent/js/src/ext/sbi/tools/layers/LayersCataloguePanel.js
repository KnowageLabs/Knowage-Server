/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

 
Ext.ns("Sbi.geo.tools");

Sbi.geo.tools.LayersCataloguePanel = function(config) {
	var defaultSettings = {
			layout: 'fit',
			contextPath: "SpagoBI",
			columnsRef: ['label', 'descr', 'type','baseLayer']

	};
	
	if(Sbi.settings && Sbi.settings.georeport && Sbi.settings.georeport.tools && Sbi.settings.georeport.tools.layerscatalogue) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.georeport.tools.layerscatalogue);
	}
	
	Ext.apply(this,defaultSettings);
	
	var sm = new Ext.grid.CheckboxSelectionModel({SingleSelect:false});
	var cm = this.buildColumns(sm);
	
	var c = ({
		store: this.buildStore(),
		cm: cm,
		sm: sm,
		viewConfig: {
			 forceFit: true
		}
	});
	
	Sbi.geo.tools.LayersCataloguePanel.superclass.constructor.call(this,c);

};

Ext.extend(Sbi.geo.tools.LayersCataloguePanel, Ext.grid.GridPanel, {
	buildColumns: function(sm){
		var thisPanel = this;
		
		var columnsDesc = [];
		columnsDesc.push(sm);
		
		//Builds the columns of the grid
		for(var i=0; i<this.columnsRef.length; i++){
			var column = this.columnsRef[i];
			var object = {
//					header: OpenLayers.Lang.translate('sbi.tools.catalogue.layers.column.header.'+column), 
					header:LN('sbi.tools.catalogue.layers.column.header.'+column), 
					sortable: true,
					dataIndex: column
				};

			columnsDesc.push(object);
		}
		
		return new Ext.grid.ColumnModel(columnsDesc);
	}
	,buildStore: function(){
		this.store = new Ext.data.Store({
			
			proxy:new Ext.data.HttpProxy({
				type: 'json',
				url : Sbi.config.serviceRegistry.getRestServiceUrl({serviceName: 'layers' ,baseUrl:{contextPath: this.contextPath}
				})
			}),
			reader: new  Ext.data.JsonReader({
				fields: [
				         "id",
				         "type",
				         "name",
				         "label",
				         "type",
				         "descr",
				         "baseLayer"
				         ],
				         root: 'root' 
			}),

			autoLoad:true,
			
			sortInfo:{field: 'type', direction: "ASC"}


		});
		
		return this.store;
	}
	
	,getSelectedLayers: function(){
		var layersLabels = new Array();
		var selected = this.getSelectionModel().getSelections();
		if(selected!=null && selected!=undefined && selected.length>0){
			for(var i=0; i<selected.length; i++){
				layersLabels.push(selected[i].data.label);
			}
		}
		return layersLabels;
	}
	
	, getUnselectedLayers: function(){
		var layersLabels = new Array();
		var selections = this.getSelectionModel().getSelections();
        var all = this.store.getRange();
        var diff = this.difference(all, selections);
        if(diff!=null && diff!=undefined && diff.length>0){
			for(var i=0; i<diff.length; i++){
				layersLabels.push(diff[i].data.label);
			}
		}
        
		return layersLabels;
	}
	
	 /**
     * Perform a set difference A-B by subtracting all items in array B from array A.
     *
     * @param {Array} array A
     * @param {Array} array B
     * @return {Array} difference
     */
    , difference: function(arrayA, arrayB) {
        var clone = arrayA.slice(0),
            ln = clone.length,
            i, j, lnB;

        for (i = 0,lnB = arrayB.length; i < lnB; i++) {
            for (j = 0; j < ln; j++) {
                if (clone[j] === arrayB[i]) {
                    clone.splice(j, 1);
                    j--;
                    ln--;
                }
            }
        }

        return clone;
    }
});