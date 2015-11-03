/** 
 * SpagoBI, the Open Source Business Intelligence suite
 * 
 * Cross navigation feature panel
 * 
 * @author Benedetto Milazzo
 */


Ext.define('Sbi.chart.designer.CrossNavigationPanel', {
	extend: 'Ext.panel.Panel',
	requires: [
	           'Sbi.chart.designer.DocumentParamModel',
	           'Sbi.chart.designer.DocumentModel',
	           ],
	
	constructor: function(config) {
		this.callParent(config);

		this.title = config.title;
		
		this.contextName = config.contextName;
		this.mainContextName = config.mainContextName;
		this.userId = config.userId;
		this.hostName = config.hostName;
		this.sbiExecutionId = config.sbiExecutionId;
		
		this.documentName = '';
		
		this.documentPanel = null;
		this.documentLabel = null;
		
		var CROSS_NAVIGATION_PANEL = this;
		
		// The store containing the document parameters associated to this chart document
		this.documentParamStore = Ext.create('Ext.data.Store', {
			storeId: 'documentParamStore',
			model: 'Sbi.chart.designer.DocumentParamModel',
			
			proxy: {
				extraParams: {
					'SBI_EXECUTION_ID': config.sbiExecutionId,
					'user_id': config.userId
				},
				type: 'ajax',
				method: 'POST',
				reader: {
					type: 'json'
				}
			}, 
//			listeners: {
//				load: function( store, records, successful, eOpts ) {
//					console.log('store-> ', store);
//					console.log('records-> ', records);
//					console.log('successful-> ', successful);
//					console.log('eOpts-> ', eOpts);
//				}
//			}
		});
		var documentParamStore = this.documentParamStore;
		
		// The store with the rest call results of listed documents present in the environment
		this.documentStore = Ext.create('Ext.data.Store', {
			storeId: 'documentStore',
			model: 'Sbi.chart.designer.DocumentModel',
			
			proxy: {
				type: 'ajax',
				url: '/' + config.mainContextName +'/restful-services/2.0/documents/listDocument',
				extraParams: {
					'SBI_EXECUTION_ID': config.sbiExecutionId,
					'user_id': config.userId,
					'excludeType': 'DOCUMENT_COMPOSITE'
				},
				method: 'GET',
				reader: {
					type: 'json',
					rootProperty: 'item'
				}
			}
		});
		var documentStore = this.documentStore;

		this.documentNameFilterText = Ext.create('Ext.form.Text', {
//			id: 'documentName',
			fieldLabel: LN('sbi.chartengine.designer.crossnavigation.document'),
			padding: '0 5 5 0'
		});
		var documentNameText = this.documentNameFilterText;
		
		this.searchButton = Ext.create('Ext.Button', {
//			id: 'lookup',
			text: LN('sbi.chartengine.designer.crossnavigation.search'),
			handler: function(obj) {
				var filterValue = documentNameText.getValue();
				documentStore.load({
					params: {
						'name': filterValue,
						'parameterUrlName': filterValue,
						'label': filterValue,
						'description': filterValue
					}
				});
				
				var documentsWindow = Ext.create('Ext.window.Window', {
					id: 'documentsWindow',
				    title: LN('sbi.chartengine.designer.crossnavigation.selectDoc'),
				    height: 500,
				    width: 600,
				    resizable: false,
				    overflowY: 'auto',
				    
				    modal: true,
				    closeAction: 'destroy',
				    
				    items: [{
				    	layout:'column',
				    	bodyStyle: 'border:0;padding:5',
				    	items:[{
				    		xtype: 'textfield',
				    		id: 'filterField',
				    		padding: '0 5 0 0',
				    		value: documentNameText.getValue()
				    	},{
				    		xtype: 'button',
				    		text: LN('sbi.chartengine.designer.crossnavigation.lookup'),
				    		handler: function(obj) {
//				    			var filterField = Ext.getCmp('filterField').getValue();
				    			
				    			documentStore.load(
//		    					{
//				    				params: {
//				    					'name': filterField,
//				    					'label': filterField,
//				    					'description': filterField
//				    				}
//				    			}
		    					);
				    		}
				    	}]
				    },
					    Ext.create('Ext.grid.Panel', {
					    	minHeight: 00,
					    	height: '100%',
					    	
					    	columns: [
					    	          {header: LN('sbi.chartengine.designer.crossnavigation.documentName'),dataIndex: 'name',flex:1},
					    	          {header: LN('sbi.chartengine.designer.crossnavigation.documentLabel'),dataIndex: 'label',flex:1},
					    	          {header: LN('sbi.chartengine.designer.crossnavigation.documentDescr'),dataIndex: 'descr',flex:2}
					    	          ],               
			    	        store: documentStore,
			    	        
			    	        listeners:{
			    	        	select: function(obj, record, index, eOpts ) {
			    	        		documentParamStore.setProxy({
			    	        			extraParams: {
			    	    		    		'SBI_EXECUTION_ID': config.sbiExecutionId,
			    	    		    		'user_id': config.userId
			    	    		    	},
			    	        			url: '/' + config.mainContextName + '/restful-services/2.0/documents/' + record.data.label + '/parameters',
			    	        			type: 'ajax',
			    	        			method: 'POST',
			    	        			reader: {
			    	        				type: 'json'
			    	        			}
			    	        		});
			    	        		
			    	        		CROSS_NAVIGATION_PANEL.documentName = record.data.label;
			    	        		
			    	        		documentParamStore.load();
	
			    	        		documentsWindow.close();
			    	        	}
			    	        }
					    })
				    ]
				}).show();
			}
		});
		
		
		var paramTypeStore = Ext.create('Ext.data.Store', {
			fields: ['label', 'value'],
			data : [
				{label: LN('sbi.chartengine.designer.crossnavigation.paramType.serie'), value: 'SERIE'},
				{label: LN('sbi.chartengine.designer.crossnavigation.paramType.category'), value: 'CATEGORY'},
				{label: LN('sbi.chartengine.designer.crossnavigation.paramType.absolute'), value: 'ABSOLUTE'},
				{label: LN('sbi.chartengine.designer.crossnavigation.paramType.relative'), value: 'RELATIVE'}
			]
		});
		
		this.parameterGridPanel = Ext.create('Ext.grid.Panel', {
			store: this.documentParamStore,
		    columns: [{
	        		header: LN('sbi.chartengine.designer.crossnavigation.paramName'),
	        		dataIndex: 'label',
	        		flex: 1 
	        	}, {
	        		header: LN('sbi.chartengine.designer.crossnavigation.paramUrlName'),
	        		dataIndex: 'parameterUrlName',
        			flex: 1 
    			}, {
    				header: LN('sbi.chartengine.designer.crossnavigation.paramType'),
    				dataIndex: 'fieldType',
    				layout: 'fit',
    				flex: 1, 
    				editor: Ext.create('Ext.form.ComboBox', {
//						xtype: 'combobox',
						editable: false,
						queryMode: 'local',
						valueField: 'value',
						displayField: 'label',
						
						store: paramTypeStore
//						, listeners: {
//							select: function( combo, records, eOpts ) {
//								console.log('documentParamStore.data.items-> ', documentParamStore.data.items);
//							}
//						}
					}),
					renderer: function(val){
		                var index = paramTypeStore.findExact('value', val); 
		                if (index != -1){
		                    rs = paramTypeStore.getAt(index).data; 
		                    return rs.label; 
		                }
		            }
				}, {
					header: LN('sbi.chartengine.designer.crossnavigation.default'),
					dataIndex: 'value', 
					flex: 3, 
					editor: 'textfield'
				}
		    ],
		    selType: 'cellmodel',
		    plugins: [
		        Ext.create('Ext.grid.plugin.CellEditing', {
		            clicksToEdit: 1
		        })
		    ],
		    minHeight: 200,
		    height: '100%',
		    width: '100%'
		});
		
		this.mainDocumentPanel = Ext.create('Ext.Panel', {
	    	id: 'mainDocumentPanel',
			bodyPadding: 5,
			items: [
	        {
				layout: 'column',
				bodyStyle: 'border:0',
				items: [
				       this.documentNameFilterText,
				       this.searchButton
				       ]
			},
				this.parameterGridPanel          
			]
	    });
		
		this.add(this.mainDocumentPanel);
    },
    
    /**
     * @returns the "DRILL" json object representing the Cross navigation data;
     * this function must be used in order to create the "DRILL" node of the jsonTemplate.
     */
	getCrossNavigationData: function() {
		if (this.documentParamStore.getCount() == 0) {
			return null;
		} else {
			var crossNavigationData = {};
			crossNavigationData['document'] = this.documentName;
			
			var params = [];
			
			var documentParamStoreItems = this.documentParamStore.data.items;
			for(var i = 0; i < documentParamStoreItems.length; i++) {
				var storeItem = documentParamStoreItems[i];
				var param = {
					name: storeItem.get('label'),
					parameterUrlName: storeItem.get('parameterUrlName'),
					type: storeItem.get('fieldType'),
					value: storeItem.get('value')
				};
				params.push(param);
			}
			
			var paramList = {};
			paramList['PARAM'] = params;
			crossNavigationData['PARAM_LIST'] = paramList;
			
			return crossNavigationData;
		}
	}
    
    /**
     * Sets the Cross navigation starting by "DRILL" object present in jsonTemplate.
     */
	, setCrossNavigationData: function(crossNavigationData) {
		crossNavigationData = crossNavigationData || {};
		
		if(!crossNavigationData 
				|| !crossNavigationData.document 
				|| !crossNavigationData.PARAM_LIST 
				|| !crossNavigationData.PARAM_LIST.PARAM) {
			return;
		} else {
			this.documentName = crossNavigationData['document'];
			
			var crossNavigationDataParamListItems = crossNavigationData.PARAM_LIST.PARAM;
			
			var params = Array.isArray(crossNavigationDataParamListItems) ? 
					crossNavigationDataParamListItems : [crossNavigationDataParamListItems];
			
			for (var i = 0; i < params.length; i++) {
				var param = params[i];
				
				var paramAsModel = Ext.create('Sbi.chart.designer.DocumentParamModel', {
					label: param.name,
					parameterUrlName: param.parameterUrlName,
				    fieldType: param.type,
				    value: param.value
				});
				
				this.documentParamStore.add(paramAsModel);
			}
		}
	}
});