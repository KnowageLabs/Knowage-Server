/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.tools.dataset.DatasetMetadataMainPage', {
	extend: 'Ext.Panel'
		
		,config: {
			id: 'datasetMetadataMainPage',
			border: false,
			frame: false,
	        autoScroll: true,
	        categoryName: null
		}

	, constructor: function(config) {
		
		//Layout must be initialized here to work!
		this.layout = 'card';
		
		//Initialize metadata stores
		this.initStores(config);
		
		config.datasetMetadataStore = this.datasetMetadataStore;
		config.storeMetadata = this.storeMetadata;
		
		
		//Initialize metadata GUIs
		
		// ManageDatasetFieldMetadata
		this.expertGUI = new Sbi.tools.dataset.ManageDatasetFieldMetadata(config)
		this.expertGUI.on('openSimpleGUI',function(panel){
			 this.simpleGeoGUI.syncComboWithStore(this.storeMetadata);
			 this.layout.setActiveItem(1);
		},this);
		
		//Simplified GUI (for GEOBI)
		this.simpleGeoGUI = new Sbi.tools.dataset.GeoBIDatasetFieldMetadata(config)
		this.simpleGeoGUI.on('openExpertGUI',function(panel){
			 this.layout.setActiveItem(0);
		},this);
		
		
		config.items = [ this.expertGUI, this.simpleGeoGUI];
		
		
		Ext.apply(this, config || {});

	    this.callParent(arguments);
	    
	    //Set default interface to display
	    this.layout.setActiveItem(0);

	}
	
	//Private Methods --------------------------------
	,initStores: function(config){
		
		//Store for Dataset Grid Metadata
		this.datasetMetadataStore = new Ext.data.JsonStore({
		    id : 'datasetMetadataStore',
		    fields: ['pname','pvalue' ],
		    idIndex: 0,
		    data: []
		});
		
		//Load Metadata if already present
		if ((config.meta != undefined) && (config.meta.dataset != undefined)){
			this.datasetMetadataStore.loadData(config.meta.dataset,false); 			
		}
		
		//Store for Columns Grid Metadata
		this.storeMetadata = new Ext.data.JsonStore({
		    id : 'metaStoreData',
		    fields: ['column', 'pname','pvalue' ],
		    idIndex: 0,
		    data: []
		});
		
		//Load Metadata if already present
		if ((config.meta != undefined) && (config.meta.columns != undefined)){
			//iterate store to modify type and remove prefix java.lang.
			var typeValue;
			for (var i = 0; i < config.meta.columns.length; i++) {
				var element = config.meta.columns[i];
				if (element.pname.toUpperCase() == 'type'.toUpperCase()){
					typeValue = element.pvalue;
					typeValue = typeValue.replace("java.lang.","");
					element.pvalue = typeValue;
				}
			}

			this.storeMetadata.loadData(config.meta.columns,false); 
		}
		
		
	}
	
	//Public Methods --------------------------------
	,getFormState: function(){
		var data = this.storeMetadata.data.items;
		var values =[];
		for(var i=0; i<data.length; i++){
			values.push(data[i].data);
		}
		
		var dataDs = this.datasetMetadataStore.data.items;
		var valuesDs =[];
		for(var i=0; i<dataDs.length; i++){
			valuesDs.push(dataDs[i].data);
		}
		
		var jsonData = {				
					version: 1,
					dataset: [],
					columns: []		
		};

		jsonData.columns = values;	
		jsonData.dataset = valuesDs;				

		return jsonData;
	}
	
	,updateData: function(columnlist){
		this.expertGUI.updateData(columnlist);
		
		this.simpleGeoGUI.updateData(columnlist);
	}
	
	,updateGridData: function(meta){
		if ((meta != undefined) && (meta.dataset != undefined)){
			this.datasetMetadataStore.loadData(meta.dataset,false); 			
		}
		
		if ((meta != undefined) && (meta.columns != undefined)){
			//iterate store to modify type and remove prefix java.lang.
			var typeValue;
			for (var i = 0; i < meta.columns.length; i++) {
				var element = meta.columns[i];
				if (element.pname.toUpperCase() == 'type'.toUpperCase()){
					typeValue = element.pvalue;
					typeValue = typeValue.replace("java.lang.","");
					element.pvalue = typeValue;
				}
			}
			this.storeMetadata.loadData(meta.columns,false); 			
		}
		this.expertGUI.doLayout();
		
		//Initialize ComboBoxes in SimpleGeoGUI
		if ((meta != undefined) ){
			this.simpleGeoGUI.initializeCombos(meta);
		}
		
	}
	
	,setDatasetCategory: function(category){
		this.categoryName = category;
		
		//Check if the category of the dataset has a specific Dataset Metadata Editor GUI associated
		var categoryMapping = Sbi.DatasetMetadataEditorMapping.mapping;
		var guiNameToUse = null;
		for(var i=0; i < categoryMapping.length; i++) {
			var cat = categoryMapping[i].category;
			if (cat.toLowerCase() == this.categoryName.toLowerCase()){
				guiNameToUse = categoryMapping[i].guiName;
				break;
			}
		}
		//button used to switch from export to simple gui
		var simpleButton = Ext.getCmp('simpleModeButton');
		
		if (guiNameToUse != undefined && guiNameToUse != null){
			for (var j=0; j < this.items.items.length; j++){
				if ( this.items.items[j].id.toLowerCase() == guiNameToUse.toLowerCase()){
					this.layout.setActiveItem(j); //set the active GUI to use
					if (simpleButton != undefined){
						simpleButton.setVisible(true); 
					}
					break;
				}
			}
		} else {
			if (simpleButton != undefined){
				simpleButton.setVisible(false);//hide the simpleModeButton in expertGui
			}
			this.layout.setActiveItem(0);
		}
		

		

	}

});