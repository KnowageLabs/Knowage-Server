/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.tools.dataset.GeoBIDatasetFieldMetadata', {
	extend: 'Ext.Panel'
		
		,config: {
			id: 'GeoBIDatasetFieldMetadata',
			border: false,
			frame: false,
	        autoScroll: true,
	        layout: 'fit'
		}


		,constructor: function(config) {
			thisMetadataPanel = this;

			var panelItems;
			panelItems = this.initMetadataPanel(panelItems,config);
			
			config.items = [panelItems];

			Ext.apply(this, config || {});
	
		    this.callParent(arguments);
		    
			this.addEvents('openExpertGUI');	

		}
		
		//Private methods ----------------------------------------------------------------------------------------------------
		
		
		
		,initMetadataPanel : function(items,config){
			
			
			//Stores -----------------------------------
			this.fieldStore = new Ext.data.JsonStore({
			    id : 'datasetColumnsStore',
			    fields: ['columnName' ],
			    idIndex: 0,
			    data: []
			});
			//------------------------------------------
			
			//Combo with columns names
			this.comboColumn = new Ext.form.ComboBox({
				name : 'columnCombo',
				store: this.fieldStore,
				width : 400,
				style: {
		            marginTop: '20px'
		        },
				displayField : 'columnName', 
				valueField : 'columnName', 
				typeAhead : true, 
				//forceSelection : true,
				mode : 'local',
				triggerAction : 'all',
				//selectOnFocus : true, 
				editable : false,
				allowBlank : true, 
				validationEvent : false,	
				queryMode: 'local',
				fieldLabel: LN('sbi.ds.metadata.dataset.hierarchy.column'),
				listConfig: {
			        listeners: {
			            itemclick: function(list, record) {
			                //alert(record.get('columnName') + ' clicked');
			                var columnName = record.get('columnName');
			                
			                var hierarchy = {};
							hierarchy.column = columnName;
							hierarchy.pname = 'hierarchy';
							hierarchy.pvalue = 'geo';
							
							thisMetadataPanel.removeExistingRecordHierarchy(hierarchy);			                
			                
							thisMetadataPanel.storeMetadata.insert(0, hierarchy);

							thisMetadataPanel.comboValues.setDisabled(false); 
			                
			            }
			        }
			    }
			});	
			
			
			//Combo with level names 
			this.comboValues = new Ext.form.ComboBox({
				name : 'comboValues',
				store: config.datasetValuesStore,
				width : 400,
				style: {
		            marginTop: '20px',
		            marginBottom: '20px'
		        },
				displayField : 'VALUE_NM', 
				valueField : 'VALUE_NM', 
				typeAhead : true,
				forceSelection : false,
				mode : 'local',
				triggerAction : 'all',
				//selectOnFocus : true, 
				editable : false,
				allowBlank : true, 
				validationEvent : false,
				fieldLabel: LN('sbi.ds.metadata.dataset.hierarchy.level'),
				listConfig: {
			        listeners: {
			            itemclick: function(list, record) {
			                //alert(record.get('VALUE_NM') + ' clicked');
			                
			    			var levelName =record.get('VALUE_NM')
			    			var columnName = thisMetadataPanel.comboColumn.getValue();

			    			if (columnName != null){
					    		var hierarchy_level = {};
								hierarchy_level.column = columnName;
								hierarchy_level.pname = 'hierarchy_level';
								hierarchy_level.pvalue = levelName;			    				
			    			}
							thisMetadataPanel.removeExistingRecordHierarchyLevel(hierarchy_level);			                
			                
							thisMetadataPanel.storeMetadata.insert(0, hierarchy_level);
	
			            }
			        }
			    }
			});	
			//filter values to show
			this.comboValues.addListener('focus',this.filterValueCombo, this);

			//until a value is selected on the comboColumn, this combo is disabled
			this.comboValues.setDisabled(true); 
			
			//Set combos with values previously set (opening an already saved dataset)
			if (config.meta != undefined && config.meta != null){
				this.initializeCombos(config.meta);
			}

			
			this.clearButton = new Ext.button.Button({
				text: LN('sbi.ds.metadata.dataset.hierarchy.clear'),
				handler: function() {
				    //remove previous hierarchy(geo) and relative hierarchy_level from store
					thisMetadataPanel.removeAllHierarchyMetadata();
					
					//Reset comboboxes
					thisMetadataPanel.comboValues.clearValue();
					thisMetadataPanel.comboValues.setDisabled(true);
					thisMetadataPanel.comboColumn.clearValue();

				}
			});
			
			this.openExpertGUIButton = new Ext.button.Button({
				text: LN('sbi.ds.metadata.dataset.hierarchy.expert'),
				style: {
		            marginLeft: '10px'
		        },
				handler: function() {
					thisMetadataPanel.fireEvent('openExpertGUI', this);	

				}
			});
			if (Sbi.DatasetMetadataEditorMapping.showExpertButton == true){
				this.openExpertGUIButton.setVisible(true); 
			} else {
				this.openExpertGUIButton.setVisible(false); 
			}
			
			
			// Main Panel ----------------------
			
			this.mainPanel = new Ext.Panel({
				  defaultType: 'textfield',
				  autoScroll: true,
		          bodyStyle:'padding:20px',
		          style: {
			            marginLeft: '100px',
			            marginTop: '50px'
			            	
			        },
				  border: false,
				  frame: false,
				  height: 350,
				  items: [this.comboColumn, this.comboValues,this.clearButton, this.openExpertGUIButton ]
				});
			
			return this.mainPanel;
			
			
		}
		
		//Set the combos values according to the values found in the passed store
		, syncComboWithStore: function(store){
			this.comboValues.clearValue();
			this.comboValues.setDisabled(true);
			this.comboColumn.clearValue();
			
			this.comboValues.store.clearFilter(false);
			
			if (store != undefined && store != null){
				if (store.data != undefined && store.data != null ){
					var dataArray = store.data.items;
					var columnName;
					for (var i=0; i<dataArray.length; i++){
						var columnsMetadata = dataArray[i].data
						if (columnsMetadata.pname == 'hierarchy' && columnsMetadata.pvalue == 'geo'){
							columnName = columnsMetadata.column;
							this.comboColumn.setValue(columnName);
							this.comboValues.setDisabled(false);
							break;
						}

					}
					if (columnName != null){
						for (var j=0; j<dataArray.length; j++){
							var columnsMetadata = dataArray[j].data
							if (columnsMetadata.pname == 'hierarchy_level' && columnsMetadata.column == columnName){
								this.comboValues.setValue(columnsMetadata.pvalue);
								break;
							} 
						}
					}	

				}
			}
		}		
		
		, initializeCombos: function(meta){
			this.comboValues.clearValue();
			this.comboValues.setDisabled(true);
			this.comboColumn.clearValue();
			
			this.comboValues.store.clearFilter(false);

			
			if (meta != undefined && meta != null){
				if (meta.columns != undefined && meta.columns != null ){
					var columnsMetadata = meta.columns;
					var columnName;
					
					for (var i=0; i<columnsMetadata.length; i++){
						if (columnsMetadata[i].pname == 'hierarchy' && columnsMetadata[i].pvalue == 'geo'){
							columnName = columnsMetadata[i].column;
							this.comboColumn.setValue(columnName);
							this.comboValues.setDisabled(false);
						} 
					}
					if (columnName != null){
						for (var j=0; j<columnsMetadata.length; j++){
							if (columnsMetadata[j].pname == 'hierarchy_level' && columnsMetadata[j].column == columnName){
								this.comboValues.setValue(columnsMetadata[j].pvalue);
							} 
						}
					}
				}
			}
		}
		
		//remove the geo hierarchy metadata and associated hierarchy_level metadata from the store
		,removeAllHierarchyMetadata: function(){
			var found = false;
			var i;
			var data = this.storeMetadata.data.items;
			var record;
			
			//Search record with pname='hierarchy' and pvalue='geo'
			for(i=0; i<data.length; i++){
				record = data[i].data
				
				if ((record.pname == 'hierarchy') && ( record.pvalue == 'geo') ){
					found = true;
					break;
				}
			}
			if (found == true) {
				var columnName = record.column;
				//fake record used only for removing the real one
				var fakeRecord = {};
				fakeRecord.column = columnName;
				fakeRecord.pname = 'hierarchy_level';
				this.storeMetadata.removeAt(i);
				this.storeMetadata.commitChanges();
				this.removeExistingRecordHierarchyLevel(fakeRecord);

			}
		}
		
		//if the column is changed then the hierarchy level metadata associated must be updated
		,updateExistingRecordHierarchyLevel: function (columnName ,newHierarchyRecord) {
			var found = false;
			var i;
			var data = this.storeMetadata.data.items;
			var updated_hierarchy_level;
			
			//Search record with pname='hierarchy_level' and same column value
			for(i=0; i<data.length; i++){
				var record = data[i].data
				
				if ((record.pname == 'hierarchy_level') && ( record.column == columnName) ){
					found = true;
					
					updated_hierarchy_level = {};
					updated_hierarchy_level.column = newHierarchyRecord.column;
					updated_hierarchy_level.pname = 'hierarchy_level';
					updated_hierarchy_level.pvalue = record.pvalue;
					break;
				}
			}
			if (found == true) {
				this.storeMetadata.removeAt(i);
				this.storeMetadata.commitChanges();
				
				this.storeMetadata.insert(0, updated_hierarchy_level);

			}
		}
		
		//check if the metadata of the same type is already in the storeMetadata and remove it
		,removeExistingRecordHierarchy: function( newRecord ){
			var found = false;
			var i;
			var data = this.storeMetadata.data.items;
			var record;
			
			//Search record with pname='hierarchy' and pvalue='geo'
			for(i=0; i<data.length; i++){
				record = data[i].data
				
				if ((record.pname == newRecord.pname) && ( record.pvalue == newRecord.pvalue) ){
					found = true;
					break;
				}
			}
			if (found == true) {
				this.storeMetadata.removeAt(i);
				this.storeMetadata.commitChanges();

				this.updateExistingRecordHierarchyLevel(record.column, newRecord)

			}
			

		}
		
		,removeExistingRecordHierarchyLevel: function( newRecord ){
			var found = false;
			var i;
			var data = this.storeMetadata.data.items;
			
			//Search record with pname='hierarchy_level' and same column value
			for(i=0; i<data.length; i++){
				var record = data[i].data
				
				if ((record.pname == newRecord.pname) && ( record.column == newRecord.column) ){
					found = true;
					break;
				}
			}
			if (found == true) {
				this.storeMetadata.removeAt(i);
				this.storeMetadata.commitChanges();

			}

		}
		
		//This will filter the data of comboValues based on comboProperties' selection
		,filterValueCombo : function(component, The, eOpts) {
			var comboValues = this.comboValues;
			comboValues.store.clearFilter(false);
		    //if no admissibleValues are specified this means no filter at all!
		    if (Sbi.DatasetMetadataEditorMapping.domainValues.length > 0) {
				//filter value combo data
				comboValues.store.filter(function(r) {
				    var value = r.get('VALUE_NM');
				    var admissibleValues = Sbi.DatasetMetadataEditorMapping.domainValues;
				    value = value.toUpperCase();
				    
				    for(i=0; i<admissibleValues.length; i++){
				    	if (value == admissibleValues[i].toUpperCase() ){
				    		return true
				    	}
				    }
				    return false

				});
		    }

		}
		
		//Public methods -----------------------------------------------------------------------------------------------------
		
		,updateData: function(columnlist){
			this.fieldStore.loadData(columnlist,false);
			this.doLayout();	
		}
		
		,getFormState: function(){
			var values =[];
			
			var columnName = this.comboColumn.getValue();
			var levelName = this.comboValues.getValue();
			
			if (columnName != null && levelName != null){
				var hierarchy = {};
				hierarchy.column = columnName;
				hierarchy.pname = 'hierarchy';
				hierarchy.pvalue = 'geo';
				
				var hierarchy_level = {};
				hierarchy_level.column = columnName;
				hierarchy_level.pname = 'hierarchy_level';
				hierarchy_level.pvalue = levelName;
				
				values.push(hierarchy);
				values.push(hierarchy_level);		

			}

			return values;

			
		}
});		