 /** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one ats http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.data.editor.association.AssociationEditorDatasetContainer', {
	extend: 'Ext.Panel'
	, layout: 'column'
	, config:{
		stores: null
	}

	, services: null
	, dsContainerPanel: null
	, engineAlreadyInitialized : false
	, border : false
	, autoScroll: true

	, constructor : function(config) {
		Sbi.trace("[AssociationEditorDatasetContainer.constructor]: IN");
		this.initConfig(config);
		this.init();
		this.callParent(arguments);
		Sbi.trace("[AssociationEditorDatasetContainer.constructor]: OUT");
	}


	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	, init: function() {
		var items = new Array();

		for (var i=0; i < this.stores.length; i++){
				var item = new Sbi.data.editor.association.AssociationEditorDataset({
					border: false,
					height : 225,
					width : 180,
					dataset: this.stores[i]
				});
				items.push(item);
		}

		this.items = items;
	}

	, resetSelections: function(){
		for (var i=0; i<this.items.length; i++){
			var item = this.items.get(i);
			var sm = item.grid.getSelectionModel();
			if (sm != null && sm !== undefined){
				sm.deselectAll(true);
//				sm.deselectAll(false);
			}
		}
	}

	// PUBLIC METHODS
	, getDatasetItem: function(idx){
		return this.items.get(idx);
	}

	, getDatasetItemByLabel: function(l){
		var toReturn = null;

		for (var i=0; i<this.items.length; i++){
			if (this.items.get(i).dataset === l){
				toReturn = this.items.get(i);
				break;
			}
		}
		return toReturn;
	}


	, getAllDatasets: function(){
		return this.items;
	}

	, getSelection: function(l){
		var toReturn = null;

		Sbi.trace("IN");

		var dataSetItem = this.getDatasetItemByLabel(l);

		if(Sbi.isNotValorized(dataSetItem)) {
			Sbi.warn("No dataset item associated to label [" + l + "]");
			Sbi.trace("OUT");
			return null;
		}

		if(Sbi.isNotValorized(dataSetItem.grid)) {
			Sbi.warn("Grid associate to dataset item [" + l + "] is undefined");
			Sbi.trace("OUT");
			return null;
		}

		var slectionModel = dataSetItem.grid.getSelectionModel();
		if(Sbi.isNotValorized(slectionModel)) {
			Sbi.warn("Impossible to get selection from dataset item  [" + l + "]");
			Sbi.trace("OUT");
			return null;
		}

		if(slectionModel.getSelection().length == 0) {
			Sbi.warn("There are no selected row in dataset item  [" + l + "]");
			Sbi.trace("OUT");
			return null;
		}

		toReturn = slectionModel.getSelection()[0].data;
		Sbi.trace("OUT");

		return toReturn;
	}

	, setSelection: function(el){
		var dsLabel = el[0];
		var dsField = el[1];

		var ds = this.getDatasetItemByLabel(dsLabel);
		if (ds !== null && ds !== undefined){
			var recId = ds.grid.store.find('alias', dsField);
			ds.grid.getSelectionModel().select(recId,true,true);
//			ds.grid.getSelectionModel().select(recId,true,false);
		}
	}

});
