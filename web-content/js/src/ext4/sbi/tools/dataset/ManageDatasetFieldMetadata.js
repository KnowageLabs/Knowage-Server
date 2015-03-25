Ext.define('Sbi.tools.dataset.ManageDatasetFieldMetadata', {
	extend: 'Ext.Panel'
	
	,config: {
		id: 'dsMetaGrid',
		border: false,
		frame: false,
		fieldsColumns:null,
		selModel:null,
		emptyStore: true,
        store: null,		        
        frame: true,
        autoScroll: true,
        layout: 'fit'
	}


	, constructor: function(config) {
		
		var panelItems;
		panelItems = this.initMetadataPanel(panelItems,config);
		
		config.items = [panelItems];
		
		thisMetadataPanel = this;


		
		Ext.apply(this, config || {});


		
	    this.callParent(arguments);
	    
		//invokes before each ajax request 
	    Ext.Ajax.on('beforerequest', this.showMask, this);   
	    // invokes after request completed 
	    Ext.Ajax.on('requestcomplete', this.hideMask, this);            
	    // invokes if exception occured 
	    Ext.Ajax.on('requestexception', this.hideMask, this); 
	    
		this.addEvents('openSimpleGUI');	

	}
	
	,initMetadataPanel : function(items,config){
		
		
		//Store-----------------------------------
		this.fieldStore = new Ext.data.JsonStore({
		    id : 'datasetColumnsStore',
		    fields: ['columnName' ],
		    idIndex: 0,
		    data: []
		});
		
	
		//Store for Columns Grid Metadata
		this.storeMetadata = config.storeMetadata;
//		this.storeMetadata = new Ext.data.JsonStore({
//		    id : 'metaStoreData',
//		    fields: ['column', 'pname','pvalue' ],
//		    idIndex: 0,
//		    data: []
//		});
//		
//		//Load Metadata if already present
//		if ((config.meta != undefined) && (config.meta.columns != undefined)){
//			//iterate store to modify type and remove prefix java.lang.
//			var typeValue;
//			for (var i = 0; i < config.meta.columns.length; i++) {
//				var element = config.meta.columns[i];
//				if (element.pname.toUpperCase() == 'type'.toUpperCase()){
//					typeValue = element.pvalue;
//					typeValue = typeValue.replace("java.lang.","");
//					element.pvalue = typeValue;
//				}
//			}
//
//			this.storeMetadata.loadData(config.meta.columns,false); 
//			this.doLayout();	
//		}
		
		//Store for Dataset Grid Metadata
		this.datasetMetadataStore = config.datasetMetadataStore;
//		this.datasetMetadataStore = new Ext.data.JsonStore({
//		    id : 'datasetMetadataStore',
//		    fields: ['pname','pvalue' ],
//		    idIndex: 0,
//		    data: []
//		});
//		
//		//Load Metadata if already present
//		if ((config.meta != undefined) && (config.meta.dataset != undefined)){
//			this.datasetMetadataStore.loadData(config.meta.dataset,false); 			
//			this.doLayout();	
//		}
		
		
		//-----------------------------------------
		
		//Combobox to hide/show the specific Grid
		
		this.metadataType = new Ext.form.ComboBox({
			name : 'metadataType',
			store: new Ext.data.ArrayStore({
		        fields: [
		            'metadataTypeName',
		            'metadataTypeValue'
		        ],
		        data: [ ['Column', 'Column'], ['Dataset', 'Dataset']]
		    }),
			width : 150,
			fieldLabel : LN('sbi.ds.metadata.dataset.title2'),
			displayField : 'metadataTypeName', 
			valueField : 'metadataTypeValue', 
			typeAhead : true, forceSelection : true,
			mode : 'local',
			triggerAction : 'all',
			value : 'Column', //default value selected on creation
			selectOnFocus : true, 
			editable : false,
			allowBlank : false, 
			validationEvent : false,
			region: 'north'
		});	
		this.metadataType.addListener('select',this.activateMetadataGrid, this);

		
		// Columns Metadata Grid  --------	
		
		
		
		//In cell editors
		this.columnTextFieldEditor = new Ext.form.TextField();
		this.attributeTextFieldEditor = new Ext.form.TextField();
		this.valueTextFieldEditor = new Ext.form.TextField();
		this.comboColumn = new Ext.form.ComboBox({
			name : 'columnCombo',
			store: this.fieldStore,
			//width : 150,
			displayField : 'columnName', 
			valueField : 'columnName', 
			typeAhead : true, forceSelection : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true, 
			editable : false,
			allowBlank : false, 
			validationEvent : false,	
			queryMode: 'local'
			});	
		
		this.comboProperties = new Ext.form.ComboBox({
			name : 'comboProperties',
			store: config.datasetPropertiesStore,
			//width : 150,
			displayField : 'VALUE_NM', 
			valueField : 'VALUE_NM', 
			typeAhead : true, forceSelection : false,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true, 
			editable : true,
			allowBlank : false, 
			validationEvent : false,	
			queryMode: 'local'
			});	
		
		this.comboValues = new Ext.form.ComboBox({
			name : 'comboValues',
			store: config.datasetValuesStore,
			displayField : 'VALUE_NM', 
			valueField : 'VALUE_NM', 
			typeAhead : true, forceSelection : false,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true, 
			editable : true,
			allowBlank : false, 
			validationEvent : false
		});	
		this.comboValues.addListener('focus',this.filterValueCombo, this);

		

		
		
		var toolbarColumnsMetadata = new Ext.Toolbar({
			id:'columnsGridToolbar',
	    	buttonAlign : 'left',
	    	items:[
	    	    new Ext.button.Button({
	            text: LN('sbi.ds.metadata.addProperty'),
	            iconCls: 'icon-restore',
	            handler: function(){
	                // access the Record constructor through the grid's store
	            	var p = {column: '',
	            			pname: '',
	            			pvalue: ''}
	            	this.gridColumnsMetadata.store.insert(0, p);
	            	//thisMetadataPanel.gridColumnsMetadata.store.insert(0, p);
	            },
	            scope: this
	        }), '-', new Ext.button.Button({
	            text: LN('sbi.ds.metadata.deleteProperty'),
	            iconCls: 'icon-remove',
	            handler: this.onDelete,
	            scope: this
	        }), '-', new Ext.button.Button({
	            text: LN('sbi.ds.metadata.clear'),
	            iconCls: 'icon-clear',
	            handler: this.onDeleteAll,
	            scope: this
	        }), '-', new Ext.button.Button({
	            text: LN('sbi.ds.metadata.dataset.hierarchy.simple'),
	            handler: function() {
					this.fireEvent('openSimpleGUI', this);				
				},
	            scope: this,
	            id: 'simpleModeButton'
	        })
	    	]
	    });
		
		var columnsDefinition =  [
				             		{
				             	    	header: LN('sbi.ds.metadata.column.column'), 
				             	    	width: '33%', 
				            			sortable: true, 
				             			id:'column',
				             			dataIndex:'column',
				             			editor: this.comboColumn
				             	    },{
				             	    	header: LN('sbi.ds.metadata.column.attribute'), 
				             	    	width: '33%', 
				            			sortable: true, 
				             			id:'pname',
				             			dataIndex:'pname',
				             			//editor: this.attributeTextFieldEditor
				             			editor: this.comboProperties
				             	    },{
				             	    	header: LN('sbi.ds.metadata.column.value'), 
				             	    	width: '33%', 
				            			sortable: true, 
				             			id:'pvalue',
				             			dataIndex:'pvalue',
				             			//editor: this.valueTextFieldEditor
				             			editor: this.comboValues
				             	    }			
				             	];		
		
		var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
	        clicksToEdit: 1
	    });
		
	
		
		this.gridColumnsMetadata = new Ext.grid.Panel({        
	        store: this.storeMetadata,
	        columns: columnsDefinition,
	        width: '100%',
	        autoHeight:true,
	        title: LN('sbi.ds.metadata.column.title'),
	        autoScroll: true,
			selModel: {selType: 'rowmodel'},
			plugins: [cellEditing],
	        tbar: toolbarColumnsMetadata,
			region: 'center',
			layout: 'fit',
			split: true


	    });
		//----------------------------------------------
		
		//Dataset Metadata Grid ---------------------------
		
		this.comboGenericProperties = new Ext.form.ComboBox({
			name : 'comboGenericProperties',
			store: config.datasetGenericPropertiesStore,
			//width : 150,
			displayField : 'VALUE_NM', 
			valueField : 'VALUE_NM', 
			typeAhead : true, forceSelection : false,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true, 
			editable : true,
			allowBlank : false, 
			validationEvent : false,	
			queryMode: 'local'
			});	
		
		var datasetGridColumnsDefinition =  [
		                            {
				             	    	header:LN('sbi.ds.metadata.column.attribute'), 
				             	    	width: '50%', 
				            			sortable: true, 
				             			id:'pnameDs',
				             			dataIndex:'pname',
				             			//editor: this.attributeTextFieldEditor
				             			editor: this.comboGenericProperties
				             	    },{
				             	    	header: LN('sbi.ds.metadata.column.value'), 
				             	    	width: '49%', 
				            			sortable: true, 
				             			id:'pvalueDs',
				             			dataIndex:'pvalue',
				             			editor: this.valueTextFieldEditor
				             	    }			
				             	];	
		

		
		var cellEditingDs = Ext.create('Ext.grid.plugin.CellEditing', {
	        clicksToEdit: 1
	    });
		
		var toolbarDatasetMetadata = new Ext.Toolbar({
	    	buttonAlign : 'left',
	    	items:[
	    	    new Ext.button.Button({
	            text: LN('sbi.ds.metadata.addProperty'),
	            iconCls: 'icon-restore',
	            handler: function(){
	                // access the Record constructor through the grid's store
	            	var rec = {
	            			pname: '',
	            			pvalue: ''}
	            	
	            	this.gridDatasetMetadata.store.insert(0, rec);
	            	//thisMetadataPanel.gridDatasetMetadata.store.insert(0, rec);
	            },
	            scope: this
	        }), '-', new Ext.button.Button({
	            text: LN('sbi.ds.metadata.deleteProperty'),
	            iconCls: 'icon-remove',
	            handler: this.onDeleteDs,
	            scope: this
	        }), '-', new Ext.button.Button({
	            text: LN('sbi.ds.metadata.clear'),
	            iconCls: 'icon-clear',
	            handler: this.onDeleteAllDs,
	            scope: this
	        })
	    	]
	    });
		
		//The Dataset Metadata grid
		this.gridDatasetMetadata = new Ext.grid.Panel({        
	        store: this.datasetMetadataStore,
	        columns: datasetGridColumnsDefinition,
	        width: '100%',
	        autoHeight:true,
	        title: LN('sbi.ds.metadata.dataset.title'),
	        autoScroll: true,
			selModel: {selType: 'rowmodel'},
			plugins: [cellEditingDs],
	        tbar: toolbarDatasetMetadata,
			region: 'center',
			layout: 'fit',
			split: true

	    });
		this.gridDatasetMetadata.setVisible(false); //hide by default

		
		//-------------------------------------------------
		

		
		// Main Panel ----------------------
		
		this.mainPanel = new Ext.Panel({
			  margins: '50 50 50 50',
	          labelAlign: 'left',
	          bodyStyle:'padding:5px',
			  defaultType: 'textfield',
			  height: 330, //320,
			  autoScroll: true,
			  layout: 'border',
			  items: [this.metadataType, this.gridColumnsMetadata, this.gridDatasetMetadata]
			});
		
		return this.mainPanel;
	}
	
	//Public Methods
	
	,loadItems: function(fieldsColumns, record){
  		this.record = record;
  		if(fieldsColumns){
  			this.fieldStore.loadData(fieldsColumns);
  			this.emptyStore = false;
  		}else{
  			this.emptyStore = true;
  		}
	}

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

	,updateRecord: function(){

		this.record.data.meta = this.getFormState();
	}

	,updateData: function(columnlist){
		this.fieldStore.loadData(columnlist,false);
		this.doLayout();	
	}
	
	//Update the Store of the Dataset Grid and Column Grid
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
		
		this.doLayout();	
	}
	
	//Listener Combobox -----------------
	
	,activateMetadataGrid : function(combo, record, index) {
		if (Array.isArray(record)) record = record[0];
		var metadataSelected = record.get('metadataTypeValue');
		if (metadataSelected != null && metadataSelected == 'Dataset') {
			this.gridColumnsMetadata.setVisible(false);
			this.gridDatasetMetadata.setVisible(true);
		} else if (metadataSelected != null && metadataSelected == 'Column') {
			this.gridColumnsMetadata.setVisible(true);
			this.gridDatasetMetadata.setVisible(false);
		}
	}
	
	//This will filter the data of comboValues based on comboProperties' selection
	,filterValueCombo : function(component, The, eOpts) {
		propertySelected = this.gridColumnsMetadata.getSelectionModel().getSelection()[0].data.pname;
		var comboValues = this.comboValues;
		comboValues.store.clearFilter(false);
		//filter value combo data
		if (propertySelected != null && propertySelected.toUpperCase() == 'fieldType'.toUpperCase()) {
			comboValues.store.filter(function(r) {
			    var value = r.get('VALUE_NM');
			    value = value.toUpperCase();
			    return (value == 'MEASURE'.toUpperCase() || value == 'ATTRIBUTE'.toUpperCase());
			});
		} 
		else if (propertySelected != null && propertySelected.toUpperCase() == 'type'.toUpperCase()) {
			comboValues.store.filter(function(r) {
			    var value = r.get('VALUE_NM');
			    value = value.toUpperCase();
			    return (value == 'String'.toUpperCase() || value == 'Integer'.toUpperCase() || value == 'Double'.toUpperCase());
			});
		} 
	}
	
	
	
	//Listeners Toolbars ------------------
	,onDelete: function() { 
		var deleteRow = this.gridColumnsMetadata.getSelectionModel().getSelection();
		this.storeMetadata.remove(deleteRow);
		this.storeMetadata.commitChanges();
	}
	
	,onDeleteAll: function() {   
		this.storeMetadata.removeAll();
		this.storeMetadata.commitChanges();
	}
	
	,onDeleteDs: function() { 
		var deleteRow = this.gridDatasetMetadata.getSelectionModel().getSelection();
		this.datasetMetadataStore.remove(deleteRow);
		this.datasetMetadataStore.commitChanges();
	}
	
	,onDeleteAllDs: function() {   
		this.datasetMetadataStore.removeAll();
		this.datasetMetadataStore.commitChanges();
	}
	
	//-----------------------------------
	
	
	/**
	 * Opens the loading mask 
	*/
    , showMask : function(){
    	this.un('afterlayout',this.showMask,this);
    	if (this.loadMask == null) {    		
    		this.loadMask = new Ext.LoadMask(Ext.getBody(), {msg: "  Wait...  "});
    	}
    	if (this.loadMask){
    		this.loadMask.show();
    	}
    }

	/**
	 * Closes the loading mask
	*/
	, hideMask: function() {
    	if (this.loadMask && this.loadMask != null) {	
    		this.loadMask.hide();
    	}
	} 
});
