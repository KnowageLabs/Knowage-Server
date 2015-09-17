/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


Ext.ns("Sbi.cockpit.widgets.document");


Sbi.cockpit.widgets.document.DocumentWidgetDesigner = function(config) {

	var defaultSettings = {
		name: 'documentWidgetDesigner',
		title: LN('sbi.cockpit.widgets.document.documentWidgetDesigner.title'),
	};
	
	if (Sbi.settings && Sbi.settings.cockpit && Sbi.settings.cockpit.widgets && Sbi.settings.cockpit.widgets.document && Sbi.settings.cockpit.widgets.document.documentWidgetDesigner) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.cockpit.widgets.document.documentWidgetDesigner);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);

	this.initDocumentPanel();
	
	c = {
		layout: 'fit',
		height: 400,
		items: [this.documentPanel]
	};

	Sbi.cockpit.widgets.document.DocumentWidgetDesigner.superclass.constructor.call(this, c);

	this.on(
		'beforerender' ,
		function (thePanel, attribute) {
			var state = {};
			state.documentLabel=thePanel.documentLabelHidden.getValue();
			state.documentName=thePanel.documentNameText.getValue();
			if(thePanel.documentLabelHidden && thePanel.documentLabelHidden.getValue()){
				var myParamStore = Ext.data.StoreManager.lookup('myParamStore');
	        	myParamStore.setProxy({
	        		url: Sbi.config.contextName+'/restful-services/2.0/documents/'+thePanel.documentLabelHidden.getValue()+'/parameters',
	            	type: 'ajax',
	                method: 'GET',
	                reader: {
	                    type: 'json'
	                }
	        	});
	        	var parameters = thePanel.parameters;
	        	myParamStore.load(function(records, operation, success) {
	        	    if(parameters){
	        	    	Ext.Array.forEach( parameters, function(param){
	        	    		myParamStore.getById(param.parameterUrlName).set('value',param.value);
	            		});
	        	    }
	        	});
			}
			state.wtype = 'document';
			this.setDesignerState(state);
		},
		this
	);
};

Ext.extend(Sbi.cockpit.widgets.document.DocumentWidgetDesigner, Sbi.cockpit.core.WidgetDesigner, {
	documentPanel: null
	,documentName: null
	,documentLabel: null
	
	, getDesignerState: function(running) {
		Sbi.trace("[DocumentWidgetDesigner.getDesignerState]: IN");

		var state = Sbi.cockpit.widgets.document.DocumentWidgetDesigner.superclass.getDesignerState(this);
		state.designer = 'Document Designer';
		state.wtype = 'document';
		
		state.documentLabel=this.documentLabelHidden.getValue();
		state.documentName=this.documentNameText.getValue();
		var myParamStore = Ext.data.StoreManager.lookup('myParamStore');
		if(myParamStore){
			state.parameters=[];
			myParamStore.each(function(rec){
				Ext.Array.push(state.parameters,{parameterUrlName: rec.data.parameterUrlName, label: rec.data.label, value: rec.data.value, fieldType: rec.data.fieldType});
			});
		}
		
		Sbi.trace("[DocumentWidgetDesigner.getDesignerState]: OUT");
		return state;
	}

	, setDesignerState: function(state) {
		Sbi.trace("[DocumentWidgetDesigner.setDesignerState]: IN");
		Sbi.cockpit.widgets.document.DocumentWidgetDesigner.superclass.setDesignerState(this, state);
		if(state.documentLabel){
			this.documentLabelHidden.setValue(state.documentLabel);
		}
		if(state.documentName){
			this.documentNameText.setValue(state.documentName);
		}
		if(state.parameters){
			this.parameters=state.parameters;
		}
		
		Sbi.trace("[DocumentWidgetDesigner.setDesignerState]: OUT");
	}

	, validate: function(validFields){
		Sbi.trace("[DocumentWidgetDesigner.validate]");
		return Sbi.cockpit.widgets.document.DocumentWidgetDesigner.superclass.validate(this, validFields);
	}

	, initDocumentPanel: function (){
		Ext.create('Ext.data.Store', {
		    storeId:'myParamStore',
		    model: 'DocumentParamModel',
		    proxy: {
		    	extraParams: {'SBI_EXECUTION_ID': Sbi.config.SBI_EXECUTION_ID,
					'user_id': Sbi.config.userId},
		    	type: 'ajax',
		        method: 'GET',
		        reader: {
		            type: 'json'
		        }
		    }
		});
		Ext.create('Ext.data.Store', {
			storeId:'documentStore',
			model: 'DocumentModel',
		    proxy: {
		    	type: 'ajax',
		        url: Sbi.config.contextName+'/restful-services/2.0/documents/listDocument',
		        extraParams: {'SBI_EXECUTION_ID': Sbi.config.SBI_EXECUTION_ID,
					'user_id': Sbi.config.userId,
					'excludeType': 'DOCUMENT_COMPOSITE'},
		        method: 'GET',
		        reader: {
		            type: 'json',
		            root: 'item'
		        }
		    }
		});
		this.documentNameText = new Ext.form.Text({
			id: 'documentName',
			fieldLabel: LN('sbi.cockpit.widgets.document.documentWidgetDesigner.document'),
			padding: '0 5 5 0',
			value: this.documentName
		});
		this.documentLabelHidden = new Ext.form.field.Hidden({
			id: 'documentLabel',
			value: this.documentLabel
		});
		this.lookup = new Ext.Button({
			id: 'lookup',
			text: LN('sbi.cockpit.widgets.document.documentWidgetDesigner.lookup'),
			handler: function(obj) {
				var filterValue = Ext.getCmp('documentName').getValue();
				var documentStore = Ext.data.StoreManager.lookup('documentStore');
				documentStore.load(
					{params: {'name': filterValue,
							'label': filterValue,
							'description': filterValue}
					}
				);
				Ext.create('Ext.window.Window', {
					id: 'documentsWindow',
				    title: LN('sbi.cockpit.widgets.document.documentWidgetDesigner.selectDoc'),
				    height: 400,
				    width: 600,
				    overflowY: 'auto',
				    items: [
				    {layout:'column',
				     bodyStyle: 'border:0;padding:5',
				     items:[
						{	xtype: 'textfield',
					    	id: 'filterField',
					    	padding: '0 5 0 0',
					    	value: Ext.getCmp('documentName').getValue()
					    },{
					    	xtype: 'button',
					    	text: LN('sbi.cockpit.widgets.document.documentWidgetDesigner.lookup'),
							handler: function(obj) {
								var filterField = Ext.getCmp('filterField').getValue();
								var documentStore = Ext.data.StoreManager.lookup('documentStore');
								documentStore.load({
									params: {'name': filterField,
											'label': filterField,
											'description': filterField}
								});
							}
					    }]
					},{
				        xtype: 'grid',
				        columns: [{header: LN('sbi.cockpit.widgets.document.documentWidgetDesigner.documentName'),dataIndex: 'name'},
				                  {header: LN('sbi.cockpit.widgets.document.documentWidgetDesigner.documentLabel'),dataIndex: 'label'},
				                  {header: LN('sbi.cockpit.widgets.document.documentWidgetDesigner.documentDescr'),dataIndex: 'descr',flex:1}],               
				        store: documentStore,
				        listeners:{select:function(obj, record, index, eOpts ){
				        	var myParamStore = Ext.data.StoreManager.lookup('myParamStore');
				        	myParamStore.setProxy({
				        		url: Sbi.config.contextName+'/restful-services/2.0/documents/'+record.data.label+'/parameters',
				            	type: 'ajax',
				                method: 'GET',
				                reader: {
				                    type: 'json'
				                }
				        	});
				        	myParamStore.load();
				        	Ext.getCmp('documentLabel').setValue(record.data.label);
				        	Ext.getCmp('documentName').setValue(record.data.name);
				        	Ext.getCmp('documentsWindow').close();
				        }}
				    }]
				}).show();
			}
		});
		
		this.panel = Ext.create('Ext.grid.Panel', {
			store: 'myParamStore',
		    columns: [
		        {header: LN('sbi.cockpit.widgets.document.documentWidgetDesigner.paramName'),dataIndex: 'label'},
		        {header: LN('sbi.cockpit.widgets.document.documentWidgetDesigner.paramUrlName'),dataIndex: 'parameterUrlName'},
		        {header: LN('sbi.cockpit.widgets.document.documentWidgetDesigner.paramType'),dataIndex: 'fieldType'},
		        {header: LN('sbi.cockpit.widgets.document.documentWidgetDesigner.default'),dataIndex: 'value',
		        	flex: 1, editor: 'textfield'}
		    ],
		    selType: 'cellmodel',
		    plugins: [
		        Ext.create('Ext.grid.plugin.CellEditing', {
		            clicksToEdit: 1
		        })
		    ],
		    height: 200,
		    width: 400
		});
		var items = new Array();
		items.push({layout:'column',bodyStyle: 'border:0',items:[this.documentNameText,this.lookup]});
		items.push(this.panel);
		
		
		this.documentPanel = new Ext.Panel({
			id: 'mainDocumentPanel',
			bodyPadding: 5,
			items: items
		});
	}
	
});

Ext.define('DocumentModel', {
    extend: 'Ext.data.Model',
    fields: [
       {name: 'id', type: 'long', mapping: 'DOCUMENT_ID'},
       {name: 'name', type: 'string', mapping: 'DOCUMENT_NAME'},
       {name: 'label', type: 'string', mapping: 'DOCUMENT_NM'},
       {name: 'descr', type: 'string', mapping: 'DOCUMENT_DESCR'}
    ]
});
Ext.define('DocumentParamModel', {
    extend: 'Ext.data.Model',
    idProperty: 'parameterUrlName',
    fields: [
       {name: 'label', type: 'string'},
       {name: 'parameterUrlName', type: 'string'},
       {name: 'fieldType', type: 'string', mapping: 'parameter.type'},
       {name: 'value', defaultValue: ''} //Here "defaultValue" is needed to fix a bug on firefox SBI-530/ATHENA-136 
    ]
});


